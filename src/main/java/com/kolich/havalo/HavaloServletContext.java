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

package com.kolich.havalo;

import static com.kolich.havalo.entities.types.UserRole.ADMIN;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kolich.havalo.entities.types.HavaloUUID;
import com.kolich.havalo.entities.types.KeyPair;
import com.kolich.havalo.entities.types.UserRole;
import com.kolich.havalo.exceptions.BootstrapException;
import com.kolich.havalo.exceptions.repositories.RepositoryCreationException;
import com.kolich.havalo.io.managers.RepositoryManager;
import com.kolich.havalo.servlets.filters.HavaloUserService;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;

public final class HavaloServletContext implements ServletContextListener {
	
	private static final Logger logger__ =
		LoggerFactory.getLogger(HavaloServletContext.class);
	
	public static final String HAVALO_CONTEXT_CONFIG_ATTRIBUTE = "havalo.config";
	public static final String HAVALO_CONTEXT_REPO_MANAGER_ATTRIBUTE = "havalo.repomanager";
	public static final String HAVALO_CONTEXT_USER_SERVICE_ATTRIBUTE = "havalo.userservice";
	
	public static final String HAVALO_REPO_BASE_CONFIG_PROPERTY = "havalo.repository.base";
	public static final String HAVALO_ADMIN_API_UUID_PROPERTY = "havalo.api.admin.uuid";
	public static final String HAVALO_ADMIN_API_SECRET_PROPERTY = "havalo.api.admin.secret";
	
	public static final String HAVALO_API_REQUEST_TIMEOUT_PROPERTY = "havalo.api.request.timeout";
	
	private static final String REPO_BASE_DEFAULT = "WEB-INF/work";
	
	private ServletContext context_;
			
	@Override
	public void contextInitialized(final ServletContextEvent event) {
		logger__.info("Servlet context initialized.");
		// Get the default servlet context.
		context_ = event.getServletContext();
		// Load and build the application configuration, then attach the
		// loaded immutable config to the servlet context. 
		final Config config = ConfigFactory.load();
		context_.setAttribute(HAVALO_CONTEXT_REPO_MANAGER_ATTRIBUTE, config);
		for(final Map.Entry<String,ConfigValue> entry : config.entrySet()) {
		    logger__.trace("Loaded config (key=" + entry.getKey() +
		    	", value=" + entry.getValue() + ")");
		}
		// Attach the underlying configuration to the servlet context too.
		context_.setAttribute(HAVALO_CONTEXT_CONFIG_ATTRIBUTE, config);
		// Create a new repository manager based on the desired
		// underlying repository root directory on disk.
		final RepositoryManager repoManager =
			createInitialAdminRepository(context_, config);
		context_.setAttribute(HAVALO_CONTEXT_REPO_MANAGER_ATTRIBUTE,
			repoManager);
		// Create a new user lookup (auth) service.
		final HavaloUserService userService = createUserService(repoManager);
		context_.setAttribute(HAVALO_CONTEXT_USER_SERVICE_ATTRIBUTE,
			userService);
	}

	@Override
	public void contextDestroyed(final ServletContextEvent event) {
		logger__.info("Servlet context destroyed.");
	}
	
	private static final RepositoryManager getRepositoryManager(
		final ServletContext context, final Config config) {
		String repositoryBase = config.getString(HAVALO_REPO_BASE_CONFIG_PROPERTY);
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
		return new RepositoryManager(realPath);
	}
	
	private static final RepositoryManager createInitialAdminRepository(
		final ServletContext context, final Config config) {
		RepositoryManager repoManager = null;
		try {
			repoManager = getRepositoryManager(context, config);
			final String adminUUID = config.getString(HAVALO_ADMIN_API_UUID_PROPERTY);
			if(adminUUID == null) {
				logger__.error("Config property '" +
					HAVALO_ADMIN_API_UUID_PROPERTY + "' not set. Cannot " +
					"start until this property contains a valid UUID.");
				throw new BootstrapException();
			} else {
				try {
					UUID.fromString(adminUUID);
				} catch (Exception e) {
					logger__.error("Config property '" +
						HAVALO_ADMIN_API_UUID_PROPERTY + "' was set, but " +
						"did not contain a valid UUID. Cannot " +
						"start until this property contains a valid UUID.", e);
					throw new BootstrapException(e);
				}
			}
			// Verify a proper admin API accout secret is set.			
			final String adminSecret = config.getString(HAVALO_ADMIN_API_SECRET_PROPERTY);
			if(adminSecret == null) {
				logger__.error("Config property '" +
					HAVALO_ADMIN_API_SECRET_PROPERTY + "' not set. Cannot " +
					"start until this property contains a valid secret.");
				throw new BootstrapException();
			}
			logger__.debug("Admin API account initialized (uuid=" + adminUUID +
				", secret=" + adminSecret + ")");
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
	
	private static final HavaloUserService createUserService(
		final RepositoryManager repoManager) {
		return new HavaloUserService(repoManager);
	}
		
}