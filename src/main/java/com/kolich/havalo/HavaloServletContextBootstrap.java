package com.kolich.havalo;

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;

public final class HavaloServletContextBootstrap implements ServletContextListener {
	
	private static final Logger logger__ =
		LoggerFactory.getLogger(HavaloServletContextBootstrap.class);
	
	public static final String HAVALO_CONFIG_ATTRIBUTE = "havalo.config";
	
	private ServletContext context_;
	
	@Override
	public void contextInitialized(final ServletContextEvent event) {
		logger__.info("Servlet context initialized.");
		// Get the default servlet context.
		context_ = event.getServletContext();
		// Load and build the application configuration, then attach the
		// loaded immutable config to the servlet context. 
		final Config config = ConfigFactory.load();
		context_.setAttribute(HAVALO_CONFIG_ATTRIBUTE, config);
		for(final Map.Entry<String,ConfigValue> entry : config.entrySet()) {
		    logger__.debug("Loaded config (key=" + entry.getKey() + ", value=" +
		    	entry.getValue() + ")");
		}
	}

	@Override
	public void contextDestroyed(final ServletContextEvent event) {
		logger__.debug("Servlet context destroyed.");
		// TODO anything?
	}
	
}
