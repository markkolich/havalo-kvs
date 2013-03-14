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

package com.kolich.havalo.io.stores;

import static com.kolich.common.DefaultCharacterEncoding.UTF_8;
import static java.util.zip.Deflater.BEST_COMPRESSION;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.kolich.havalo.entities.StoreableEntity;
import com.kolich.havalo.exceptions.objects.ObjectFlushException;
import com.kolich.havalo.exceptions.objects.ObjectLoadException;
import com.kolich.havalo.io.MetaStore;

public abstract class MetaObjectStore extends ObjectStore implements MetaStore {
		
	protected static final String JSON_EXTENSION = ".json";
	
	public MetaObjectStore(final File storeDir) {
		super(storeDir);
	}
		
	/**
	 * The caller is most definitely responsible for closing the
	 * returned {@link Reader} when finished with it.
	 * @param index
	 * @return
	 */
	@Override
	public Reader getReader(final String index) {
		Reader reader = null;
		try {
			final InputStream gis = new GZIPInputStream(
				new FileInputStream(getCanonicalFile(index, false)));			
			reader = new InputStreamReader(gis, UTF_8);
		} catch (Exception e) {
			throw new ObjectLoadException("Failed to read entity: " +
				index, e);
		}
		return reader;
	}
	
	@Override
	public void save(final StoreableEntity entity) {
		FileOutputStream fos = null;
		GZIPOutputStream gos = null;
		Writer writer = null; 
		try {
			fos = new FileOutputStream(getCanonicalFile(entity));
			gos = new GZIPOutputStream(fos) {
				// Ugly anonymous constructor hack to set the compression
				// level on the underlying Deflater to "max compression".
				{ def.setLevel(BEST_COMPRESSION); }
			};
			writer = new OutputStreamWriter(gos, UTF_8);
			// Call the entity to write itself to the output stream.
			entity.toWriter(writer);
			writer.flush(); // Muy importante
			gos.finish(); // Muy importante mucho!
		} catch (Exception e) {
			throw new ObjectFlushException("Failed to save entity: " +
				entity.getKey(), e);
		} finally {
			closeQuietly(writer);
			closeQuietly(gos);
			closeQuietly(fos);
		}
	}
	
	@Override
	public void delete(final String index) {
		try {
			// Actually attempt to delete it, or report failure.
			if(!deleteQuietly(getCanonicalFile(index, false))) {
				throw new IOException("Deletion of index " + index +
					" failed.");
			}
		} catch (Exception e) {
			throw new ObjectFlushException("Failed to delete entity: " +
				index, e);
		}
	}
		
	private File getCanonicalFile(final StoreableEntity entity) {
		return getCanonicalFile(entity.getKey(), true);
	}
	
	private File getCanonicalFile(final String index, final boolean create) {
		return getCanonicalObject(storeDir_, index + JSON_EXTENSION,
			create).getFile();
	}
	
}
