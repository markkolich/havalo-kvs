package com.kolich.havalo.servlets;

import static com.kolich.havalo.HavaloServletContextBootstrap.HAVALO_CONFIG_ATTRIBUTE;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

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

}
