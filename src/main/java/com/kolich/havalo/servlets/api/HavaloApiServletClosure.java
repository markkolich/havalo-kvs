package com.kolich.havalo.servlets.api;

import static com.google.common.net.MediaType.JSON_UTF_8;
import static com.kolich.common.DefaultCharacterEncoding.UTF_8;
import static com.kolich.havalo.servlets.filters.HavaloAuthenticationFilter.HAVALO_AUTHENTICATION_ATTRIBUTE;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_OK;

import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import com.kolich.havalo.entities.HavaloEntity;
import com.kolich.havalo.entities.types.HavaloError;
import com.kolich.havalo.entities.types.HavaloUUID;
import com.kolich.havalo.entities.types.KeyPair;
import com.kolich.havalo.exceptions.HavaloException;
import com.kolich.havalo.servlets.HavaloServletClosure;

public abstract class HavaloApiServletClosure<S extends HavaloEntity>
	extends HavaloServletClosure<S> {
		
	public HavaloApiServletClosure(final Logger logger,
		final AsyncContext context) {
		super(logger, context);
	}
	
	@Override
	public final S doit() throws Exception {		
		return execute(getUserFromRequest().getIdKey());
	}
	
	public abstract S execute(final HavaloUUID userId) throws Exception;
	
	@Override
	public final void run() {
		final String comment = getComment();
		try {
			final S result = doit();
			// If the extending closure implementation did not return a
			// result, it returned null, that means it handled+rendered the
			// response directly and does not need the super closure to
			// attempt to render a response.
			if(result != null) {
				renderEntity(logger_, response_, result);
			}
		} catch (HavaloException e) {
			logger_.info(comment, e);
			renderHavaloException(logger_, response_, e);
		} catch (IllegalArgumentException e) {
			logger_.info(comment, e);
			renderError(logger_, response_, SC_BAD_REQUEST, e);
		} catch (Exception e) {
			logger_.info(comment, e);
			renderError(logger_, response_, e);
		} finally {
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
			response.setContentType(JSON_UTF_8.toString());
			os = response.getOutputStream();
			writer = new OutputStreamWriter(os, UTF_8);
			entity.toWriter(writer);
			writer.flush();
		} catch (Exception e) {
			logger.error("Failed to render entity to servlet " +
				"output stream.", e);
		} finally {
			IOUtils.closeQuietly(os);
			IOUtils.closeQuietly(writer);
		}
	}
	
}
