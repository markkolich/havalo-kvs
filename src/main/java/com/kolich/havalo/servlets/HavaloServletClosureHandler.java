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

import static com.kolich.common.util.URLEncodingUtils.urlDecode;
import static com.kolich.havalo.servlets.filters.HavaloAuthenticationFilter.HAVALO_AUTHENTICATION_ATTRIBUTE;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_CONFLICT;

import javax.servlet.AsyncContext;

import org.slf4j.Logger;

import com.kolich.bolt.exceptions.LockConflictException;
import com.kolich.havalo.entities.types.KeyPair;
import com.kolich.servlet.closures.ServletClosureHandler;
import com.kolich.servlet.entities.ServletClosureEntity;
import com.kolich.servlet.exceptions.ServletClosureException;

public abstract class HavaloServletClosureHandler<S extends ServletClosureEntity>
	extends ServletClosureHandler<S> {
	
	public HavaloServletClosureHandler(final Logger logger,
		final AsyncContext context) {
		super(logger, context);
	}

	public HavaloServletClosureHandler(final AsyncContext context) {
		super(context);
	}
	
	@Override
	public final S handle() throws Exception {
		try {
			return execute(getUserFromRequest());
		} catch (IllegalArgumentException e) {
			throw new ServletClosureException.WithStatus(SC_BAD_REQUEST, e);
		} catch (LockConflictException e) {
			throw new ServletClosureException.WithStatus(SC_CONFLICT, e);
		}
	}
	
	public abstract S execute(final KeyPair userKp) throws Exception;
	
	protected final KeyPair getUserFromRequest() {
		return (KeyPair)request_.getAttribute(HAVALO_AUTHENTICATION_ATTRIBUTE);
	}
	
	protected final String getEndOfRequestURI() {
		return requestUri_.substring(requestUri_.lastIndexOf("/")+1);
	}
	
	/**
	 * Returns the last slash separated component of the request URI,
	 * URL-decoded.  For example, if the request URI is
	 * "/foobar/dog/kewl%2Fdood" then this method will return "kewl/dood".  
	 */
	protected final String getRequestObject() {
		return urlDecode(getEndOfRequestURI());
	}
	
	protected final String getHeader(final String headerName) {
		return request_.getHeader(headerName);
	}
	
	/**
	 * A return value of -1 indicates that no Content-Length
	 * header was found, or the Content-Length header could not
	 * be parsed.
	 */
	protected final long getHeaderAsLong(final String headerName) {
		long result = -1L;
		try {
			result = Long.parseLong(getHeader(headerName));
		} catch (Exception e) {
			// Kinda questionable that this is the right thing to do, but
			// it seemed to make sense.  If the Content-Length header was
			// unparsable (or didn't exist) then we just return a -1 instead
			// of bubbling up such exceptions to the caller.
			result = -1L;
		}
		return result;
	}
	
	protected final void setHeader(final String headerName,
		final String headerValue) {
		response_.setHeader(headerName, headerValue);
	}
	
	protected final void setStatus(final int status) {
		response_.setStatus(status);
	}

}