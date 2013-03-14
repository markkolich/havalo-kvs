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
