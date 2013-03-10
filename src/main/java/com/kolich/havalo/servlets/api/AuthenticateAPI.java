package com.kolich.havalo.servlets.api;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kolich.havalo.entities.types.KeyPair;
import com.kolich.havalo.servlets.HavaloApiServlet;
import com.kolich.havalo.servlets.HavaloApiServletClosure;

public final class AuthenticateAPI extends HavaloApiServlet {
	
	private static final Logger logger__ =
		LoggerFactory.getLogger(AuthenticateAPI.class);

	private static final long serialVersionUID = 1087288709731427991L;
	
	@Override
	public KeyPair get(final AsyncContext context,
		final HttpServletRequest request, final HttpServletResponse response) {
		return new HavaloApiServletClosure<KeyPair>("GET:/api/authenticate",
			logger__, context, request, response) {
			@Override
			public KeyPair doit() throws Exception {
				response.setStatus(HttpServletResponse.SC_NO_CONTENT);
				return null;
			}
		}.execute();
	}
	
	/*
	@Override
	public final void doPost(final HttpServletRequest request,
		final HttpServletResponse response) {
		final KeyPair kp = getUserKeyPair(request);
		Writer writer = null;
		try {
			writer = response.getWriter();
			writer.append(kp.toString());
		} catch (IOException e) {
			logger__.error("failed", e);
		} finally {
			IOUtils.closeQuietly(writer);
		}
	}
	*/

}
