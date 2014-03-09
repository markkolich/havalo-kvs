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

package com.kolich.havalo.controllers.api;

import com.kolich.bolt.ReentrantReadWriteEntityLock;
import com.kolich.curacao.annotations.Controller;
import com.kolich.curacao.annotations.Injectable;
import com.kolich.curacao.annotations.methods.DELETE;
import com.kolich.curacao.annotations.methods.GET;
import com.kolich.curacao.annotations.methods.POST;
import com.kolich.curacao.annotations.parameters.Query;
import com.kolich.curacao.entities.CuracaoEntity;
import com.kolich.curacao.entities.empty.StatusCodeOnlyCuracaoEntity;
import com.kolich.curacao.handlers.requests.matchers.AntPathMatcher;
import com.kolich.havalo.components.RepositoryManagerComponent;
import com.kolich.havalo.controllers.HavaloApiController;
import com.kolich.havalo.entities.types.HavaloUUID;
import com.kolich.havalo.entities.types.KeyPair;
import com.kolich.havalo.entities.types.ObjectList;
import com.kolich.havalo.entities.types.Repository;
import com.kolich.havalo.exceptions.repositories.RepositoryForbiddenException;
import com.kolich.havalo.filters.HavaloAuthenticationFilter;
import com.kolich.havalo.mappers.ObjectKeyArgumentMapper.ObjectKey;

import static com.kolich.havalo.HavaloConfigurationFactory.getHavaloAdminUUID;
import static javax.servlet.http.HttpServletResponse.SC_NO_CONTENT;

@Controller
public class RepositoryApi extends HavaloApiController {

    private final HavaloUUID adminUUID_;

    @Injectable
    public RepositoryApi(final RepositoryManagerComponent component) {
        super(component.getRepositoryManager());
        adminUUID_ = new HavaloUUID(getHavaloAdminUUID());
    }

    @GET(value="/api/repository",
            matcher=AntPathMatcher.class,
            filter=HavaloAuthenticationFilter.class)
    public final ObjectList get(@Query("startsWith") final String startsWith,
                                final KeyPair userKp) throws Exception {
        final Repository repo = getRepository(userKp.getKey());
        return new ReentrantReadWriteEntityLock<ObjectList>(repo) {
            @Override
            public ObjectList transaction() throws Exception {
                return repo.startsWith((startsWith != null) ?
                    // Only load objects that start with the given
                    // prefix, if one was provided.
                    startsWith : "");
            }
        }.read(false); // Shared read lock on repo, no wait
    }

    @POST(value="/api/repository",
            matcher=AntPathMatcher.class,
            filter=HavaloAuthenticationFilter.class)
    public final KeyPair post(final KeyPair userKp) {
        // Only admin level users have the right to delete repositories.
        if(!userKp.isAdmin()) {
            throw new RepositoryForbiddenException("Authenticated " +
                "user does not have permission to create repositories: " +
                "(userId=" + userKp.getKey() + ")");
        }
        // Create a new KeyPair; this is a new user access key
        // and access secret.  NOTE: Currently key pair identities
        // always associated with "normal" user roles.  The first
        // admin user is created via the Havalo bootstrap process on
        // first boot.  Only the first admin user has the rights
        // to call this specific API function.
        final KeyPair kp = new KeyPair();
        // Create a base repository for the new access key.  All of
        // the resources associated with this access key will sit
        // under this base repository (some directory on disk).
        createRepository(kp.getKey(), kp);
        return kp;
    }

    @DELETE(value="/api/repository/{key}",
            matcher=AntPathMatcher.class,
            filter=HavaloAuthenticationFilter.class)
    public final CuracaoEntity delete(final ObjectKey key,
                                      final KeyPair userKp) throws Exception {
        // Only admin level users have the right to delete repositories.
        if(!userKp.isAdmin()) {
            throw new RepositoryForbiddenException("Authenticated " +
                "user does not have permission to delete repositories: " +
                "(userId=" + userKp.getKey() + ", repoId=" + key + ")");
        }
        final HavaloUUID toDelete = new HavaloUUID(key.getDecodedKey());
        // Admin users cannot delete the root "admin" repository.
        if(adminUUID_.equals(toDelete)) {
            throw new RepositoryForbiddenException("Authenticated " +
                "admin user attempted to delete admin repository: " +
                toDelete.getId());
        }
        // Attempt to delete the repository, its meta data, and all
        // objects inside of it.
        deleteRepository(toDelete);
        return new StatusCodeOnlyCuracaoEntity(SC_NO_CONTENT);
    }

}
