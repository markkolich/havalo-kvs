package com.kolich.havalo.servlets;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;

import com.kolich.havalo.entities.HavaloEntity;

public abstract class HavaloServletClosure<S extends HavaloEntity>
	implements Runnable {
	
	protected final Logger logger_;
	
	protected final AsyncContext context_;
	
	protected final HttpServletRequest request_;
	protected final HttpServletResponse response_;
	
	protected final String method_;
	protected final String requestUri_;
		
	public HavaloServletClosure(final Logger logger,
		final AsyncContext context) {
		checkNotNull(logger, "The provided logger cannot be null.");
		checkNotNull(context, "The provided async context cannot be null.");
		logger_ = logger;
		context_ = context;
		request_ = (HttpServletRequest)context_.getRequest();
		response_ = (HttpServletResponse)context_.getResponse();
		method_ = request_.getMethod();
		requestUri_ = request_.getRequestURI();
	}
	
	public abstract S doit() throws Exception;
		
	protected final String getComment() {
		return String.format("%s:%s", method_, requestUri_);
	}
	
}
