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

package com.kolich.havalo;

import static com.typesafe.config.ConfigFactory.load;
import static java.lang.System.getProperty;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.util.Map;

import org.slf4j.Logger;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;

public final class HavaloConfigurationFactory {
	
	private static final Logger logger__ = 
		getLogger(HavaloConfigurationFactory.class);
	
	// Public static's
		
	public static final String HAVALO_REPO_BASE_CONFIG_PROPERTY =
		"havalo.repository.base";
	public static final String HAVALO_REPO_MAX_FILENAME_LENGTH_PROPERTY =
		"havalo.repository.maxfilename.length";
	public static final String HAVALO_ADMIN_API_UUID_PROPERTY =
		"havalo.api.admin.uuid";
	public static final String HAVALO_ADMIN_API_SECRET_PROPERTY =
		"havalo.api.admin.secret";
	
	public static final String HAVALO_UPLOAD_MAX_SIZE_PROPERTY =
		"havalo.upload.max.size";
	public static final String HAVALO_MAX_CONCURRENT_REQUESTS_PROPERTY =
		"havalo.api.max.requests";
	public static final String HAVALO_ASYNC_REQUEST_TIMEOUT_PROPERTY =
		"havalo.api.request.timeout.ms";
	
	// Private static's
	
	private static final String HAVALO_EXTERNAL_CONFIG_FILENAME =
		"havalo.conf";
	private static final String CONTAINER_CONF_DIRNAME =
		"conf";
	private static final String JETTY_HOME_SYS_PROPERTY =
		getProperty("jetty.home");
	private static final String CATALINA_HOME_SYS_PROPERTY =
		getProperty("catalina.home");
	
	// Singleton.
	private static final Config config__;
	static {
		final Config refConfConfig = load();
		// Load the external 'havalo.conf' application configuration file
		// specific to the internal Servlet container.
		final Config overrideConfig = loadHavaloOverrideConfig();
		// Load and build the application configuration, then attach the
		// loaded immutable config to the servlet context. 
		final Config havaloConfig;
		if(overrideConfig != null) {
			logger__.debug("Found valid override configuration; " +
				"using override.");
			havaloConfig = overrideConfig.withFallback(refConfConfig);
		} else {
			logger__.debug("Found no valid override configuration; " +
				"using default configuration provided by bundled " +
				"reference.conf");
			havaloConfig = refConfConfig;
		}
		for(final Map.Entry<String,ConfigValue> entry :
			havaloConfig.entrySet()) {
		    logger__.trace("Loaded config (key=" + entry.getKey() +
		    	", value=" + entry.getValue() + ")");
		}
		config__ = havaloConfig;
	}
	
	// Cannot instantiate.
	private HavaloConfigurationFactory() { }
	
	/**
	 * Returns an immutable, shared, configuration singleton instance that
	 * represents the configuration of this Havalo service.  Gracefully
	 * includes any custom configuration "overrides" placed into the Servlet
	 * container's "conf" (configuration) directory.
	 */
	public static final Config getConfigInstance() {
		return config__;
	}
	
	private static final Config loadHavaloOverrideConfig() {
		File configFile = null;
		if(JETTY_HOME_SYS_PROPERTY != null) {
			final File jettyConfigFile = new File(
				new File(JETTY_HOME_SYS_PROPERTY, CONTAINER_CONF_DIRNAME),
				HAVALO_EXTERNAL_CONFIG_FILENAME);
			if(jettyConfigFile.exists()) {
				logger__.info("Found Jetty specific " +
					HAVALO_EXTERNAL_CONFIG_FILENAME + " configuration file " +
					"at: " + jettyConfigFile.getAbsolutePath());
				configFile = jettyConfigFile;
			}
		} else if(CATALINA_HOME_SYS_PROPERTY != null) {
			final File catalinaConfigFile = new File(
				new File(CATALINA_HOME_SYS_PROPERTY, CONTAINER_CONF_DIRNAME),
				HAVALO_EXTERNAL_CONFIG_FILENAME);
			if(catalinaConfigFile.exists()) {
				logger__.info("Found Catalina (Tomcat) specific " +
					HAVALO_EXTERNAL_CONFIG_FILENAME + " configuration file " +
					"at: " + catalinaConfigFile.getAbsolutePath());
				configFile = catalinaConfigFile;
			}
		}
		Config config = null;
		try {
			// If we found a valid config file specific to the supported
			// Servlet container, then load it.  Otherwise, return null meaning
			// no override file was found.
			config = (configFile != null) ?
				// Load the external 'havalo.conf' Typesafe configuration
				// from an external file.
				ConfigFactory.parseFile(configFile) :
				// No valid external config file found, return null.
				null;
		} catch (Exception e) {
			// Usually get here when the provided havalo.conf override file
			// is malformed or something went wrong while loading it.
			logger__.warn("Failed to parse override " +
				HAVALO_EXTERNAL_CONFIG_FILENAME + " configuration file.", e);
			config = null;
		}
		return config;
	}
	
	// ******************************************************************
	// Config property getters (helper methods)
	// ******************************************************************
	
	public static final String getRepositoryBase() {
		return getConfigInstance().getString(
			HAVALO_REPO_BASE_CONFIG_PROPERTY);
	}
	
	public static final int getMaxFilenameLength() {
		return getConfigInstance().getInt(
			HAVALO_REPO_MAX_FILENAME_LENGTH_PROPERTY);
	}
	
	public static final String getHavaloAdminUUID() {
		return getConfigInstance().getString(
			HAVALO_ADMIN_API_UUID_PROPERTY);
	}
	
	public static final String getHavaloAdminSecret() {
		return getConfigInstance().getString(
			HAVALO_ADMIN_API_SECRET_PROPERTY);
	}

	public static final long getMaxUploadSize() {
		return getConfigInstance().getLong(
			HAVALO_UPLOAD_MAX_SIZE_PROPERTY);
	}
	
	public static final int getMaxConcurrentRequests() {
		return getConfigInstance().getInt(
			HAVALO_MAX_CONCURRENT_REQUESTS_PROPERTY);
	}
	
	public static final long getAsyncRequestTimeout() {
		return getConfigInstance().getLong(
			HAVALO_ASYNC_REQUEST_TIMEOUT_PROPERTY);
	}
	
}
