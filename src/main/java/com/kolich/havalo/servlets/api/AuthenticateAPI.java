package com.kolich.havalo.servlets.api;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kolich.havalo.servlets.HavaloApiServlet;

public final class AuthenticateAPI extends HavaloApiServlet {
	
	private static final Logger logger__ =
		LoggerFactory.getLogger(AuthenticateAPI.class);

	private static final long serialVersionUID = 1087288709731427991L;
	
	@Override
	public void doGet(final HttpServletRequest request,
		final HttpServletResponse response) {
		Writer writer = null;
		try {
			writer = response.getWriter();
			writer.append("foobar!");
		} catch (IOException e) {
			logger__.error("failed", e);
		} finally {
			IOUtils.closeQuietly(writer);
		}
	}

}
