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

package com.kolich.havalo.servlets.api;

import static com.kolich.havalo.HavaloServletContext.HAVALO_ADMIN_API_UUID_PROPERTY;
import static javax.servlet.http.HttpServletResponse.SC_NO_CONTENT;
import static org.apache.commons.lang3.Validate.notEmpty;

import javax.servlet.AsyncContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kolich.bolt.ReentrantReadWriteEntityLock;
import com.kolich.havalo.entities.types.HavaloUUID;
import com.kolich.havalo.entities.types.KeyPair;
import com.kolich.havalo.entities.types.ObjectList;
import com.kolich.havalo.entities.types.Repository;
import com.kolich.havalo.exceptions.repositories.RepositoryForbiddenException;
import com.kolich.havalo.servlets.HavaloApiServlet;
import com.kolich.havalo.servlets.HavaloServletClosureHandler;
import com.kolich.servlet.entities.ServletClosureEntity;

public final class RepositoryApi extends HavaloApiServlet {
	
	private static final long serialVersionUID = -2934103705538663343L;
	
	private static final Logger logger__ =
		LoggerFactory.getLogger(RepositoryApi.class);
	
	@Override
	public final HavaloServletClosureHandler<ObjectList> get(
		final AsyncContext context) {
		return new HavaloServletClosureHandler<ObjectList>(logger__, context) {
			@Override
			public ObjectList execute(final KeyPair userKp) throws Exception {
				final String startsWith = request_.getParameter("startsWith");
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
		};
	}
	
	@Override
	public final HavaloServletClosureHandler<KeyPair> post(
		final AsyncContext context) {
		return new HavaloServletClosureHandler<KeyPair>(logger__, context) {
			@Override
			public KeyPair execute(final KeyPair userKp) throws Exception {
				// Only admin level users have the right to delete repositories.
				if(!userKp.isAdmin()) {
					throw new RepositoryForbiddenException("Authenticated " +
						"user does not have permission to create repositories: " +
						"(userId=" + userKp.getKey() + ")");
				}
				// Create a new KeyPair; this is a new user access key
				// and access secret.  NOTE: Currently key pair identities
				// always associated with "normal" user roles.  The first
				// admin user is created via the HavaloBootstrap bean on
				// first boot.  Only the first admin user has the rights
				// to call this specific API function.
				final KeyPair kp = new KeyPair();
				// Create a base repository for the new access key.  All of
				// the resources associated with this access key will sit
				// under this base repository (some directory on disk).
				createRepository(kp.getKey(), kp);
				return kp;
			}
		};
	}
	
	@Override
	public final <S extends ServletClosureEntity> HavaloServletClosureHandler<S>
		delete(final AsyncContext context) {
		return new HavaloServletClosureHandler<S>(logger__, context) {
			@Override
			public S execute(final KeyPair userKp) throws Exception {
				// URL-decode the incoming key (the UUID of the repo)
				final String key = getRequestObject();							
				notEmpty(key, "Key cannot be null or empty.");
				// Only admin level users have the right to delete repositories.
				if(!userKp.isAdmin()) {
					throw new RepositoryForbiddenException("Authenticated " +
						"user does not have permission to delete repositories: " +
						"(userId=" + userKp.getKey() + ", repoId=" + key + ")");
				}
				final HavaloUUID toDelete = new HavaloUUID(key);
				final HavaloUUID adminId = new HavaloUUID(
					getHavaloConfig().getString(HAVALO_ADMIN_API_UUID_PROPERTY));
				// Admin users cannot delete the root "admin" repository.
				if(adminId.equals(toDelete)) {
					throw new RepositoryForbiddenException("Authenticated " +
						"admin user attempted to delete admin repository: " +
						toDelete.getId());
				}
				// Attempt to delete the repository, its meta data, and all
				// objects inside of it.
				deleteRepository(toDelete);
				// Send an empty HTTP 204 No Content back on success.
				setStatus(SC_NO_CONTENT);
				// Return null to tell the parent that we've
				// handled the response ourselves.
				return null;
			}
		};
	}
	
}
