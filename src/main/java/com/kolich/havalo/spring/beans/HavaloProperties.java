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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kolich.havalo.entities.types.HavaloUUID;
import com.kolich.havalo.exceptions.HavaloException;
import com.kolich.spring.beans.KolichWebAppProperties;

public final class HavaloProperties extends KolichWebAppProperties {
	
	private static final Logger logger__ =
		LoggerFactory.getLogger(HavaloProperties.class);
	
	private static final String PROPERTY_REPOSITORY_BASE_DIR = "repository.base";
	
	private static final String PROPERTY_ADMIN_API_UUID = "api.admin.uuid";
	private static final String PROPERTY_ADMIN_API_SECRET = "api.admin.secret";
	
	private static final String PROPERTY_MAX_UPLOAD_BYTES = "max.upload.bytes";
	private static final String PROPERTY_MAX_IN_MEMORY_BYTES = "max.in-memory.bytes";
	private static final String PROPERTY_TEMP_UPLOAD_DIR = "temp.upload.directory";
	
	//import static com.kolich.havalo.entities.types.HavaloUUID.HAVALO_ADMIN_UUID;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		final String adminUUID = (String)getProperty(PROPERTY_ADMIN_API_UUID);
		logger__.debug("Loaded API Admin UUID: " + adminUUID);
		try {
			new HavaloUUID(adminUUID);
		} catch (Exception e) {
			throw new HavaloException("Loaded API Admin UUID was not " +
				"a valid UUID: " + adminUUID, e);
		}
	}
	
	public String getRepositoryBaseDir() {
		return (String)getProperty(PROPERTY_REPOSITORY_BASE_DIR);
	}
	
	public HavaloUUID getAdminApiUUID() {
		return new HavaloUUID((String)getProperty(PROPERTY_ADMIN_API_UUID));
	}
	
	public String getAdminApiSecret() {
		return (String)getProperty(PROPERTY_ADMIN_API_SECRET);
	}
	
	public Long getMaxUploadBytes() {
		return Long.parseLong((String)getProperty(PROPERTY_MAX_UPLOAD_BYTES));
	}
	
	public Long getMaxInMemoryBytes() {
		return Long.parseLong((String)getProperty(PROPERTY_MAX_IN_MEMORY_BYTES));
	}
	
	public String getTempUploadDir() {
		return (String)getProperty(PROPERTY_TEMP_UPLOAD_DIR);
	}

}
