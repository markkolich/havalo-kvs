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

package com.kolich.havalo.io.managers;

import static com.kolich.havalo.entities.HavaloEntity.getHavaloGsonInstance;
import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.File;
import java.io.Reader;

import com.kolich.havalo.entities.types.HavaloUUID;
import com.kolich.havalo.entities.types.Repository;
import com.kolich.havalo.io.stores.MetaObjectStore;

/**
 * The intermediary between the repository manager and the actual
 * repository meta data sitting on disk.
 */
public final class RepositoryMetaStore extends MetaObjectStore {
	
	public RepositoryMetaStore(final File storeDir) {
		super(storeDir);
	}
	
	public Repository loadById(final HavaloUUID ownerId) {
		Reader reader = null;
		try {
			reader = super.getReader(ownerId.toString());
			return getHavaloGsonInstance().fromJson(reader,
				Repository.class);
		} finally {
			closeQuietly(reader);
		}
	}

}
