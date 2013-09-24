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
