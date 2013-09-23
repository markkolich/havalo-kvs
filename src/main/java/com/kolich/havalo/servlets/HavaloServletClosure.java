package com.kolich.havalo.servlets;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.concurrent.ExecutorService;

import javax.servlet.AsyncContext;

import org.slf4j.Logger;

import com.kolich.havalo.entities.types.KeyPair;
import com.kolich.servlet.closures.AbstractServletClosure;
import com.kolich.servlet.entities.ServletClosureEntity;
import com.kolich.servlet.exceptions.MethodNotSupportedException;

public abstract class HavaloServletClosure extends AbstractServletClosure {

	private static final long serialVersionUID = -322996248748525648L;
	
	private static final Logger logger__ = getLogger(HavaloServletClosure.class);
	
	public HavaloServletClosure(final ExecutorService pool,
		final long asyncTimeoutMs) {
		super(pool, asyncTimeoutMs);
	}
	
	// NOTE:
	// Although not required, we're intentionally overriding the methods
	// below such that for any incoming request, we check if the request
	// is "authorized" (has the proper authorization) before even attempting
	// to do anything with it.  That is, even if the incoming request method
	// isn't supported by the implementing Servlet, the consumer of the API
	// (the client) has to pass authorization before they'll see a
	// "method not supported" error response.
	
	@Override
	public <T extends ServletClosureEntity>
		HavaloAuthenticatingServletClosureHandler<? extends ServletClosureEntity>
		trace(final AsyncContext context) {
		return new HavaloAuthenticatingServletClosureHandler<T>(logger__, context) {
			@Override
			public T execute(final KeyPair userKp) throws Exception {
				throw new MethodNotSupportedException();
			}
		};
	}
	
	@Override
	public <T extends ServletClosureEntity>
		HavaloAuthenticatingServletClosureHandler<? extends ServletClosureEntity>
		head(final AsyncContext context) {
		return new HavaloAuthenticatingServletClosureHandler<T>(logger__, context) {
			@Override
			public T execute(final KeyPair userKp) throws Exception {
				throw new MethodNotSupportedException();
			}
		};
	}
	
	@Override
	public <T extends ServletClosureEntity>
		HavaloAuthenticatingServletClosureHandler<? extends ServletClosureEntity>
		get(final AsyncContext context) {
		return new HavaloAuthenticatingServletClosureHandler<T>(logger__, context) {
			@Override
			public T execute(final KeyPair userKp) throws Exception {
				throw new MethodNotSupportedException();
			}
		};
	}
	
	@Override
	public <T extends ServletClosureEntity>
		HavaloAuthenticatingServletClosureHandler<? extends ServletClosureEntity>
		post(final AsyncContext context) {
		return new HavaloAuthenticatingServletClosureHandler<T>(logger__, context) {
			@Override
			public T execute(final KeyPair userKp) throws Exception {
				throw new MethodNotSupportedException();
			}
		};
	}
	
	@Override
	public <T extends ServletClosureEntity>
		HavaloAuthenticatingServletClosureHandler<? extends ServletClosureEntity>
		put(final AsyncContext context) {
		return new HavaloAuthenticatingServletClosureHandler<T>(logger__, context) {
			@Override
			public T execute(final KeyPair userKp) throws Exception {
				throw new MethodNotSupportedException();
			}
		};
	}
	
	@Override
	public <T extends ServletClosureEntity>
		HavaloAuthenticatingServletClosureHandler<? extends ServletClosureEntity>
		delete(final AsyncContext context) {
		return new HavaloAuthenticatingServletClosureHandler<T>(logger__, context) {
			@Override
			public T execute(final KeyPair userKp) throws Exception {
				throw new MethodNotSupportedException();
			}
		};
	}

}
