/**
 * Copyright (c) 2012 Mark S. Kolich
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

package com.kolich.havalo.spring.beans;

import static com.kolich.havalo.entities.types.UserRole.ADMIN;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.kolich.havalo.entities.types.HavaloUUID;
import com.kolich.havalo.entities.types.KeyPair;
import com.kolich.havalo.exceptions.HavaloException;
import com.kolich.havalo.exceptions.repositories.RepositoryCreationException;
import com.kolich.havalo.io.managers.RepositoryManager;

public class HavaloBootstrap implements InitializingBean {
	
	private static final Logger logger__ =
		LoggerFactory.getLogger(HavaloBootstrap.class);
	
	private RepositoryManager manager_;
	
	private HavaloUUID adminApiUUID_;
	private String adminApiSecret_;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		try {
			// Create a new keypair for the default ADMIN level user.
			final KeyPair adminKeyPair = new KeyPair(adminApiUUID_,
				adminApiSecret_, ADMIN);
			// Actually attempt to create a new Repository for the Admin user.
			// This should work, if not, bail the whole app.
			manager_.createRepository(adminKeyPair.getIdKey(), adminKeyPair);
			logger__.info("Successfully created ADMIN user repository (key=" +
				adminKeyPair.getIdKey() + ", secret=" +
					adminKeyPair.getSecret() + ")");
		} catch (RepositoryCreationException e) {
			// Log in DEBUG and continue silently.  This is a normal case,
			// when the admin repo has already been created on firstboot
			// but Havalo is being re-started.
			logger__.debug("Failed to create ADMIN user repository -- " +
				"repository already exists.", e);
		} catch (Exception e) {
			// Hm, something else went wrong on startup, need to log
			// and then bail.  The application cannot continue at this point.
			logger__.error("Failed to create ADMIN user repository -- " +
				"cannot continue, giving up.", e);
			throw e;
		}
	}

	public void setRepositoryManager(RepositoryManager manager) {
		manager_ = manager;
	}
	
	public void setAdminApiUUID(String adminApiUUID) {
		try {
			adminApiUUID_ = new HavaloUUID(adminApiUUID);
		} catch (IllegalArgumentException e) {
			throw new HavaloException("Provided Admin API UUID is not " +
				"a valid UUID.", e);
		}
	}
	
	public void setAdminApiSecret(String adminApiSecret) {
		adminApiSecret_ = adminApiSecret;
	}

}
