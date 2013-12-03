/**
 * Copyright (c) 2013 Mark S. Kolich
 * http://mark.koli.ch
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package com.kolich.havalo.components;

import com.kolich.curacao.annotations.Component;
import com.kolich.curacao.handlers.components.CuracaoComponent;
import com.kolich.havalo.entities.types.HavaloUUID;
import com.kolich.havalo.entities.types.KeyPair;
import com.kolich.havalo.entities.types.UserRole;
import com.kolich.havalo.exceptions.BootstrapException;
import com.kolich.havalo.exceptions.repositories.RepositoryCreationException;
import com.kolich.havalo.io.managers.RepositoryManager;
import com.typesafe.config.Config;
import org.slf4j.Logger;

import javax.servlet.ServletContext;
import java.io.File;
import java.util.Arrays;
import java.util.UUID;

import static com.kolich.havalo.HavaloConfigurationFactory.*;
import static com.kolich.havalo.entities.types.UserRole.ADMIN;
import static org.apache.commons.lang3.StringUtils.abbreviate;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class RepositoryManagerComponent implements CuracaoComponent {

    private static final Logger logger__ =
        getLogger(RepositoryManagerComponent.class);

    private static final String REPO_BASE_DEFAULT = "WEB-INF/work";

    private static final Config config__ = getConfigInstance();

    private RepositoryManager repositoryManager_;

    @Override
    public void initialize(final ServletContext context) throws Exception {
        repositoryManager_ = createInitialAdminRepository(context, config__);
    }

    @Override
    public void destroy(final ServletContext context) throws Exception {
        // Nothing, intentional.
    }

    public RepositoryManager getRepositoryManager() {
        return repositoryManager_;
    }

    private static final RepositoryManager getRepositoryManager(
        final ServletContext context) {
        String repositoryBase = getRepositoryBase();
        if(repositoryBase == null) {
            logger__.warn("Config property '" + HAVALO_REPO_BASE_CONFIG_PROPERTY +
                "' was not set, using default repository base: " +
                REPO_BASE_DEFAULT);
            repositoryBase = REPO_BASE_DEFAULT;
        }
        // If the provided repository base path starts with a slash, then
        // interpret the location as an absolute path on disk.  Otherwise,
        // no preceding slash indicates a path relative to the web application
        // root directory.
        final File realPath;
        if(repositoryBase.startsWith("/")) {
            realPath = new File(repositoryBase);
        } else {
            realPath = new File(context.getRealPath("/" + repositoryBase));
        }
        logger__.info("Using repository root at: " + realPath.getAbsolutePath());
        final int maxFilenameLength = getMaxFilenameLength();
        logger__.info("Max repository object filename length: " +
            maxFilenameLength);
        return new RepositoryManager(realPath, maxFilenameLength);
    }

    private static final RepositoryManager createInitialAdminRepository(
        final ServletContext context, final Config config) {
        RepositoryManager repoManager = null;
        try {
            repoManager = getRepositoryManager(context);
            final String adminUUID = getHavaloAdminUUID();
            if(adminUUID == null) {
                final String msg = "Config property '" +
                    HAVALO_ADMIN_API_UUID_PROPERTY + "' not set. Cannot " +
                    "start until this property contains a valid UUID.";
                logger__.error(msg);
                throw new BootstrapException(msg);
            } else {
                try {
                    UUID.fromString(adminUUID);
                } catch (Exception e) {
                    logger__.error("Config property '" +
                        HAVALO_ADMIN_API_UUID_PROPERTY + "' was set, but " +
                        "did not contain a valid UUID. Cannot " +
                        "start until this property contains a well " +
                        "formed UUID.", e);
                    throw new BootstrapException(e);
                }
            }
            // Verify a proper admin API accout secret is set.
            final String adminSecret = getHavaloAdminSecret();
            if(adminSecret == null) {
                final String msg = "Config property '" +
                    HAVALO_ADMIN_API_SECRET_PROPERTY + "' not set. Cannot " +
                    "start until this property contains a valid secret.";
                logger__.error(msg);
                throw new BootstrapException(msg);
            }
            logger__.debug("Admin API account initialized (uuid=" + adminUUID +
                ", secret=" + abbreviate(adminSecret, 8) + ")");
            // Create a new keypair for the default ADMIN level user.
            final KeyPair adminKeyPair = new KeyPair(new HavaloUUID(adminUUID),
                adminSecret, Arrays.asList(new UserRole[]{ADMIN}));
            // Actually attempt to create a new Repository for the Admin user.
            // This should work, if not, bail the whole app.
            repoManager.createRepository(adminKeyPair.getKey(), adminKeyPair);
            return repoManager;
        } catch (RepositoryCreationException e) {
            // Log in TRACE and continue silently.  This is a normal case,
            // when the admin repo has already been created on firstboot
            // but Havalo is being re-started.
            logger__.trace("Failed to create ADMIN user repository -- " +
                "repository already exists.", e);
        } catch (Exception e) {
            // Hm, something else went wrong on startup, need to log
            // and then bail.  The application cannot continue at this point.
            logger__.error("Failed to create ADMIN user repository -- " +
                "cannot continue, giving up.", e);
            throw new BootstrapException(e);
        }
        return repoManager;
    }

}
