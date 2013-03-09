package com.kolich.havalo.servlets;

import static com.kolich.havalo.HavaloServletContextBootstrap.HAVALO_CONFIG_ATTRIBUTE;
import static com.kolich.havalo.authentication.HavaloAuthenticationFilter.HAVALO_AUTHENTICATION_ATTRIBUTE;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import com.kolich.havalo.entities.types.KeyPair;
import com.typesafe.config.Config;

public abstract class HavaloApiServlet extends HttpServlet {

	private static final long serialVersionUID = 8388599956708926598L;
	
	private Config appConfig_;
	
	@Override
	public final void init(final ServletConfig config) throws ServletException {
		appConfig_ = (Config)config.getServletContext()
			.getAttribute(HAVALO_CONFIG_ATTRIBUTE);
	}
		
	protected final Config getAppConfig() {
		return appConfig_;
	}
	
	protected static final KeyPair getUserKeyPair(final HttpServletRequest request) {
		return (KeyPair)request.getAttribute(HAVALO_AUTHENTICATION_ATTRIBUTE);
	}

}
