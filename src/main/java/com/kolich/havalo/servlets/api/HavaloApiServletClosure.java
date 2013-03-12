package com.kolich.havalo.servlets.api;

import static com.kolich.common.DefaultCharacterEncoding.UTF_8;
import static com.kolich.havalo.authentication.HavaloAuthenticationFilter.HAVALO_AUTHENTICATION_ATTRIBUTE;
import static com.kolich.havalo.entities.types.HavaloError.exceptionToErrorType;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.servlet.AsyncContext;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import com.kolich.havalo.entities.HavaloEntity;
import com.kolich.havalo.entities.types.HavaloError;
import com.kolich.havalo.entities.types.KeyPair;
import com.kolich.havalo.exceptions.HavaloException;
import com.kolich.havalo.servlets.HavaloServletClosure;

public abstract class HavaloApiServletClosure<S extends HavaloEntity>
	extends HavaloServletClosure<S> {
		
	protected final KeyPair userKeyPair_;
	
	public HavaloApiServletClosure(final Logger logger,
		final AsyncContext context) {
		super(logger, context);
		userKeyPair_ = getUserFromRequest();
	}
		
	@Override
	public final void run() {
		final String comment = getComment();
		try {
			logger_.info(comment);
			final S result = doit();
			
		} catch (HavaloException e) {
			final HavaloError error = exceptionToErrorType(e);
			renderError(error);
		} catch (Exception e) {
			final HavaloError error = new HavaloError(SC_INTERNAL_SERVER_ERROR,
				e.getMessage(), e);
			renderError(error);
		} finally {
			context_.complete();
		}
	}
	
	private final KeyPair getUserFromRequest() {
		return (KeyPair)request_.getAttribute(HAVALO_AUTHENTICATION_ATTRIBUTE);
	}
	
	private final void renderError(final HavaloError error) {
		OutputStream os = null;
		OutputStreamWriter writer = null;
		try {
			response_.setStatus(error.getStatus());
			os = response_.getOutputStream();
			writer = new OutputStreamWriter(os, UTF_8);
			error.toWriter(writer);
			writer.flush();
		} catch (Exception e) {
			logger_.error("Failed to send proper error type to servlet " +
				"output stream.", e);
		} finally {
			IOUtils.closeQuietly(os);
			IOUtils.closeQuietly(writer);
		}
	}
	
}
