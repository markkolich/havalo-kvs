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

import static com.google.common.net.MediaType.JSON_UTF_8;
import static com.kolich.common.DefaultCharacterEncoding.UTF_8;
import static com.kolich.havalo.servlets.filters.HavaloAuthenticationFilter.HAVALO_AUTHENTICATION_ATTRIBUTE;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_CONFLICT;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;

import com.kolich.bolt.exceptions.LockConflictException;
import com.kolich.havalo.entities.HavaloEntity;
import com.kolich.havalo.entities.types.HavaloError;
import com.kolich.havalo.entities.types.KeyPair;
import com.kolich.havalo.exceptions.HavaloException;
import com.kolich.havalo.servlets.HavaloServletClosure;

public abstract class HavaloApiServletClosure<S extends HavaloEntity>
	extends HavaloServletClosure<S> {
	
	private static final String JSON_UTF_8_TYPE = JSON_UTF_8.toString();
		
	public HavaloApiServletClosure(final Logger logger,
		final AsyncContext context) {
		super(logger, context);
	}
	
	@Override
	public final S doit() throws Exception {		
		return execute(getUserFromRequest());
	}
	
	public abstract S execute(final KeyPair userKp) throws Exception;
	
	@Override
	public final void run() {
		final String comment = getComment();
		try {
			logger_.debug("Starting handle of " + comment);
			final S result = doit();
			// If the extending closure implementation did not return a
			// result, it returned null, that means it handled+rendered the
			// response directly and does not need the super closure to
			// attempt to render a response.
			if(result != null) {
				renderEntity(logger_, response_, result);
			}
		} catch (HavaloException e) {
			logger_.debug(comment, e);
			renderHavaloException(logger_, response_, e);
		} catch (LockConflictException e) {
			logger_.debug(comment, e);
			renderError(logger_, response_, SC_CONFLICT, e);
		} catch (IllegalArgumentException e) {
			logger_.debug(comment, e);
			renderError(logger_, response_, SC_BAD_REQUEST, e);
		} catch (Exception e) {
			logger_.debug(comment, e);
			renderError(logger_, response_, e);
		} finally {
			logger_.debug("Finishing handle of " + comment);
			// Important, always finish the context.
			context_.complete();
		}
	}
	
	protected final KeyPair getUserFromRequest() {
		return (KeyPair)request_.getAttribute(HAVALO_AUTHENTICATION_ATTRIBUTE);
	}
	
	protected final String getEndOfRequestURI() {
		return requestUri_.substring(requestUri_.lastIndexOf("/")+1);
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
	
	public static final void renderHavaloException(final Logger logger,
		final HttpServletResponse response, final HavaloException e) {
		renderError(logger, response, new HavaloError(e.getStatusCode(),
			e.getMessage(), e));
	}
	
	public static final void renderError(final Logger logger,
		final HttpServletResponse response, final Exception e) {
		renderError(logger, response, SC_INTERNAL_SERVER_ERROR, e);
	}
	
	public static final void renderError(final Logger logger,
		final HttpServletResponse response, final int status,
		final Exception e) {
		renderError(logger, response, new HavaloError(status, e.getMessage(), e));
	}
	
	public static final void renderError(final Logger logger,
		final HttpServletResponse response, final HavaloError error) {
		renderEntity(logger, response, error.getStatus(), error);
	}
	
	public static final void renderEntity(final Logger logger,
		final HttpServletResponse response, final HavaloEntity entity) {
		renderEntity(logger, response, SC_OK, entity);
	}
	
	public static final void renderEntity(final Logger logger,
		final HttpServletResponse response, final int status,
		final HavaloEntity entity) {
		OutputStream os = null;
		OutputStreamWriter writer = null;
		try {
			response.setStatus(status);
			response.setContentType(JSON_UTF_8_TYPE);
			os = response.getOutputStream();
			writer = new OutputStreamWriter(os, UTF_8);
			entity.toWriter(writer);
			writer.flush();
		} catch (Exception e) {
			logger.error("Failed to render entity to servlet " +
				"output stream.", e);
		} finally {
			closeQuietly(os);
			closeQuietly(writer);
		}
	}
	
}
