package com.kolich.havalo.servlets.api.handlers;

import static javax.servlet.http.HttpServletResponse.SC_NO_CONTENT;

import java.util.Arrays;

import javax.servlet.AsyncContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kolich.bolt.ReentrantReadWriteEntityLock;
import com.kolich.havalo.entities.HavaloEntity;
import com.kolich.havalo.entities.types.HavaloUUID;
import com.kolich.havalo.entities.types.KeyPair;
import com.kolich.havalo.entities.types.ObjectList;
import com.kolich.havalo.entities.types.Repository;
import com.kolich.havalo.entities.types.UserRole;
import com.kolich.havalo.servlets.api.HavaloApiServlet;
import com.kolich.havalo.servlets.api.HavaloApiServletClosure;

public final class RepositoryApi extends HavaloApiServlet {
	
	private static final long serialVersionUID = -2934103705538663343L;
	
	private static final Logger logger__ =
		LoggerFactory.getLogger(RepositoryApi.class);
	
	@Override
	public final HavaloApiServletClosure<ObjectList> get(
		final AsyncContext context) {
		return new HavaloApiServletClosure<ObjectList>(logger__, context) {
			@Override
			public ObjectList execute(final HavaloUUID userId) throws Exception {
				final String startsWith = request_.getParameter("startsWith");
				final Repository repo = getRepository(userId);
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
	public final HavaloApiServletClosure<KeyPair> post(
		final AsyncContext context) {
		return new HavaloApiServletClosure<KeyPair>(logger__, context) {
			@Override
			public KeyPair execute(final HavaloUUID userId) throws Exception {
				// Create a new KeyPair; this is a new user access key
				// and access secret.  NOTE: Currently key pair identities
				// always associated with "normal" user roles.  The first
				// admin user is created via the HavaloBootstrap bean on
				// first boot.  Only the first admin user has the rights
				// to call this specific API function.
				final KeyPair kp = new KeyPair(Arrays.asList(
					new UserRole[]{UserRole.USER}));
				// Create a base repository for the new access key.  All of
				// the resources associated with this access key will sit
				// under this base repository (some directory on disk).
				createRepository(kp.getIdKey(), kp);
				return kp; 
			}
		};
	}
	
	@Override
	public final <S extends HavaloEntity> HavaloApiServletClosure<S> delete(
		final AsyncContext context) {
		return new HavaloApiServletClosure<S>(logger__, context) {
			@Override
			public S execute(final HavaloUUID userId) throws Exception {
				final HavaloUUID toDelete = new HavaloUUID(getEndOfRequestURI());
				// TODO need to get the admin repo UUID here and prevent
				// deletion of that UUID
				/*
				// Admin users cannot delete the "admin" repository.
				if(properties_.getAdminApiUUID().equals(toDelete)) {
					throw new RepositoryForbiddenException("Authenticated " +
						"admin user attempted to delete admin repository: " +
						toDelete.getId());
				}
				*/
				// Attempt to delete the repository, its meta data, and all
				// objects inside of it.
				deleteRepository(toDelete);
				// Send an empty HTTP 204 No Content back on success.
				response_.setStatus(SC_NO_CONTENT);
				// Return null to tell the parent that we've
				// handled the response.
				return null;
			}
		};
	}
	
}
