package com.kolich.havalo;

import static com.kolich.havalo.entities.types.UserRole.ADMIN;

import java.io.File;
import java.util.Arrays;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kolich.havalo.authentication.HavaloUserService;
import com.kolich.havalo.entities.types.HavaloUUID;
import com.kolich.havalo.entities.types.KeyPair;
import com.kolich.havalo.entities.types.UserRole;
import com.kolich.havalo.exceptions.HavaloException;
import com.kolich.havalo.exceptions.repositories.RepositoryCreationException;
import com.kolich.havalo.io.managers.RepositoryManager;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;

public final class HavaloServletContextBootstrap implements ServletContextListener {
	
	private static final Logger logger__ =
		LoggerFactory.getLogger(HavaloServletContextBootstrap.class);
	
	public static final String HAVALO_CONFIG_ATTRIBUTE = "havalo.config";
	public static final String HAVALO_REPO_MANAGER_ATTRIBUTE = "havalo.repomanager";
	public static final String HAVALO_USER_SERVICE_ATTRIBUTE = "havalo.userservice";
	
	private ServletContext context_;
			
	@Override
	public void contextInitialized(final ServletContextEvent event) {
		logger__.info("Servlet context initialized.");
		// Get the default servlet context.
		context_ = event.getServletContext();
		// Load and build the application configuration, then attach the
		// loaded immutable config to the servlet context. 
		final Config config = ConfigFactory.load();
		context_.setAttribute(HAVALO_REPO_MANAGER_ATTRIBUTE, config);
		for(final Map.Entry<String,ConfigValue> entry : config.entrySet()) {
		    logger__.debug("Loaded config (key=" + entry.getKey() + ", value=" +
		    	entry.getValue() + ")");
		}
		// Attach the underlying configuration to the servlet context too.
		context_.setAttribute(HAVALO_CONFIG_ATTRIBUTE, config);
		// Create a new repository manager based on the desired
		// underlying repository root directory on disk.
		final RepositoryManager repoManager = createInitialAdminRepository(context_, config);
		context_.setAttribute(HAVALO_REPO_MANAGER_ATTRIBUTE, repoManager);
		// Create a new user lookup (auth) service.
		final HavaloUserService userService = createUserService(repoManager);
		context_.setAttribute(HAVALO_USER_SERVICE_ATTRIBUTE, userService);
	}

	@Override
	public void contextDestroyed(final ServletContextEvent event) {
		logger__.debug("Servlet context destroyed.");
		// TODO anything?
	}
	
	private static final RepositoryManager getRepositoryManager(
		final ServletContext context, final Config config) {
		final String repositoryBase = config.getString("havalo.repository.base");
		if(repositoryBase == null) {
			// TODO throw exception
		}
		// Interpret location as relative to the web application root directory.
		final File realPath = new File(context.getRealPath(
			(!repositoryBase.startsWith("/")) ? "/" + repositoryBase :
				repositoryBase));
		return new RepositoryManager(realPath);
	}
	
	private static final RepositoryManager createInitialAdminRepository(
		final ServletContext context, final Config config) {
		RepositoryManager repoManager = null;
		try {
			repoManager = getRepositoryManager(context, config);
			final String adminUUID = config.getString("havalo.api.admin.uuid");
			final String adminSecret = config.getString("havalo.api.admin.secret");
			// TODO log admin UUID and admin secret
			// Create a new keypair for the default ADMIN level user.
			final KeyPair adminKeyPair = new KeyPair(new HavaloUUID(adminUUID),
				adminSecret, Arrays.asList(new UserRole[]{ADMIN}));
			// Actually attempt to create a new Repository for the Admin user.
			// This should work, if not, bail the whole app.
			repoManager.createRepository(adminKeyPair.getIdKey(), adminKeyPair);
			return repoManager;
		} catch (RepositoryCreationException e) {
			// Log in TRACE and continue silently.  This is a normal case,
			// when the admin repo has already been created on firstboot
			// but Havalo is being re-started.
			logger__.trace("Failed to create ADMIN user repository -- " +
				"repository already exists.", e);
		} catch (Exception e) {
			// Hm, something else went wrong on startup, need to log
			// and then bail.  The application cannot continue at this point.
			logger__.error("Failed to create ADMIN user repository -- " +
				"cannot continue, giving up.", e);
			throw new HavaloException(e, 500);
		}
		return repoManager;
	}
	
	private static final HavaloUserService createUserService(
		final RepositoryManager repoManager) {
		return new HavaloUserService(repoManager);
	}
		
}
