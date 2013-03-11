package com.kolich.havalo.servlets;

import static com.kolich.havalo.authentication.HavaloAuthenticationFilter.HAVALO_AUTHENTICATION_ATTRIBUTE;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;

import com.kolich.bolt.exceptions.LockConflictException;
import com.kolich.havalo.entities.HavaloEntity;
import com.kolich.havalo.entities.types.KeyPair;
import com.kolich.havalo.exceptions.BadHavaloUUIDException;
import com.kolich.havalo.exceptions.InvalidResourceException;
import com.kolich.havalo.exceptions.objects.ObjectConflictException;
import com.kolich.havalo.exceptions.objects.ObjectDeletionException;
import com.kolich.havalo.exceptions.objects.ObjectFlushException;
import com.kolich.havalo.exceptions.objects.ObjectLoadException;
import com.kolich.havalo.exceptions.objects.ObjectNotFoundException;
import com.kolich.havalo.exceptions.repositories.DuplicateRepositoryException;
import com.kolich.havalo.exceptions.repositories.RepositoryCreationException;
import com.kolich.havalo.exceptions.repositories.RepositoryDeletionException;
import com.kolich.havalo.exceptions.repositories.RepositoryFlushException;
import com.kolich.havalo.exceptions.repositories.RepositoryForbiddenException;
import com.kolich.havalo.exceptions.repositories.RepositoryLoadException;
import com.kolich.havalo.exceptions.repositories.RepositoryNotFoundException;

public abstract class HavaloApiServletClosure<T extends HavaloEntity> implements Runnable {
	
	protected final Logger logger_;
	
	protected final AsyncContext context_;
	
	protected final HttpServletRequest request_;
	protected final HttpServletResponse response_;
	
	protected final String method_;
	protected final String requestUri_;
	
	protected final KeyPair userKeyPair_;
	
	public HavaloApiServletClosure(final Logger logger,
		final AsyncContext context) {
		logger_ = logger;
		context_ = context;
		request_ = (HttpServletRequest)context_.getRequest();
		response_ = (HttpServletResponse)context_.getResponse();
		method_ = request_.getMethod();
		requestUri_ = request_.getRequestURI();
		userKeyPair_ = getUserFromRequest(request_);
	}
	
	public abstract T doit() throws Exception;
	
	@Override
	public final void run() {
		final String comment = getComment();
		try {
			final T result = doit();
			
		} catch (IllegalArgumentException e) {
			logger_.debug(comment, e);
			throw e;
		} catch (DuplicateRepositoryException e) {
			logger_.debug(comment, e);
			throw e;
		} catch (RepositoryCreationException e) {
			logger_.debug(comment, e);
			throw e;
		} catch (RepositoryDeletionException e) {
			logger_.debug(comment, e);
			throw e;
		} catch (RepositoryForbiddenException e) {
			logger_.debug(comment, e);
			throw e;
		} catch (RepositoryFlushException e) {
			logger_.debug(comment, e);
			throw e;
		} catch (RepositoryLoadException e) {
			logger_.debug(comment, e);
			throw e;
		} catch (RepositoryNotFoundException e) {
			logger_.debug(comment, e);
			throw e;
		} catch (ObjectConflictException e) {
			logger_.debug(comment, e);
			throw e;
		} catch (ObjectDeletionException e) {
			logger_.debug(comment, e);
			throw e;
		} catch (ObjectFlushException e) {
			logger_.debug(comment, e);
			throw e;
		} catch (ObjectLoadException e) {
			logger_.debug(comment, e);
			throw e;
		} catch (ObjectNotFoundException e) {
			logger_.debug(comment, e);
			throw e;
		} catch (LockConflictException e) {
			logger_.debug(comment, e);
			throw e;
		} catch (BadHavaloUUIDException e) {
			logger_.debug(comment, e);
			throw e;
		} catch (InvalidResourceException e) {
			logger_.debug(comment, e);
			throw e;
		} catch (Exception e) {
			logger_.debug(comment, e);
		} finally {
			context_.complete();
		}
	}
	
	private final String getComment() {
		return String.format("%s:%s", method_, requestUri_);
	}
	
	protected static final KeyPair getUserFromRequest(final HttpServletRequest request) {
		return (KeyPair)request.getAttribute(HAVALO_AUTHENTICATION_ATTRIBUTE);
	}
	
}
