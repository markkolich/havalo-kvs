package com.kolich.havalo.servlets.api;

import static com.kolich.havalo.authentication.HavaloAuthenticationFilter.HAVALO_AUTHENTICATION_ATTRIBUTE;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;

import com.kolich.common.either.Either;
import com.kolich.havalo.entities.HavaloEntity;
import com.kolich.havalo.entities.errors.HavaloError;
import com.kolich.havalo.entities.types.KeyPair;

public abstract
	class HavaloApiServletClosure<S extends HavaloEntity,E extends Either<HavaloError,S>>
	implements Runnable {
	
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
	
	public abstract E doit() throws Exception;
	
	@Override
	public final void run() {
		final String comment = getComment();
		try {
			final E either = doit();
			
		} catch (Exception e) {
			logger_.debug(comment, e);
		} finally {
			context_.complete();
		}
	}
	
	private final String getComment() {
		return String.format("%s:%s", method_, requestUri_);
	}
	
	private static final KeyPair getUserFromRequest(final HttpServletRequest request) {
		return (KeyPair)request.getAttribute(HAVALO_AUTHENTICATION_ATTRIBUTE);
	}
	
}
