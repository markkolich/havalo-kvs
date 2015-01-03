/**
 * Copyright (c) 2015 Mark S. Kolich
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

import com.kolich.havalo.entities.types.DiskObject;
import com.kolich.havalo.exceptions.objects.ObjectLoadException;

import java.io.File;
import java.io.IOException;

import static com.kolich.common.util.crypt.Base32Utils.encodeBase32;
import static org.apache.commons.io.FileUtils.forceMkdir;

public abstract class ObjectStore {
	
	/**
	 * The max filename length of the ext3 filesystem is 255-bytes. The
	 * max filename length of ext4 is 256-bytes.  NTFS claims that their
	 * max filename length is also 255-bytes.  The filename length is the
	 * max length of any component along the path, not the entire path
	 * itself.
	 */
	public static final int DEFAULT_MAX_FILENAME_LENGTH = 255;
		
	/**
	 * The max length of a cache filename before it's split up
	 * into multiple directories.  Default is 255 to work on most
	 * file systems.
	 */
	protected final int maxFileNameLength_;
	
	protected final File storeDir_;
	
	public ObjectStore(final File storeDir,
                       final int maxFileNameLength) {
		storeDir_ = storeDir;
		maxFileNameLength_ = maxFileNameLength;
	}
	
	public ObjectStore(final File storeDir) {
		this(storeDir, DEFAULT_MAX_FILENAME_LENGTH);
	}
	
	/**
	 * Given an index of arbitrary length, returns a {@link File} object
	 * that points to a file system safe location on disk that won't blow
	 * up because a filename is too long.  This may mean that the filename
	 * is split up into multiple directories if necessary.  If the caller
	 * wishes to have this method create the required parent directories
	 * for the cached entity, set makeParents to true.
	 */
	protected final DiskObject getCanonicalObject(final File parent,
                                                  final String index,
                                                  final int maxFileNameLength,
                                                  final boolean makeParentDirs) {
		File f = null;
		try {
			// Get the encoded file name (with extension if one exists);
			// this may be longer than the file system supports, but that's OK
			// because we will split this up accordingly.
			//final String fileName = encode(index, UTF_8);
			final String fileName = encodeBase32(index);
			// If the encoded filename is longer than the max filename length
			// this bean supports, split up the index into a series of directories.
			if(fileName.length() > maxFileNameLength) {
				// Get a series of tokens that represent this cache file in
				// a split set of directories where each element in the path is
				// no longer than the maximum allowed by the file system.
				File root = parent;
				int beginIndex = 0;
				while(beginIndex <= fileName.length()) {
					int endIndex = ((beginIndex + maxFileNameLength) > fileName.length()) ?
						fileName.length() : beginIndex + maxFileNameLength;
					root = new File(root, fileName.substring(beginIndex, endIndex));
					beginIndex += maxFileNameLength;
				}
				f = root;
			} else {
				f = new File(parent, fileName);
			}
			// If the new file does not exist, it's a directory, and we
			// were asked to create it -- then create the parent directories.
			if(makeParentDirs) {				
				try {
					final File parentDir = f.getParentFile();
					forceMkdir(parentDir);
				} catch (Exception e) {
					throw new IOException("Could not create required parent " +
						"directories for object (file=" + f.getAbsolutePath() +
							")", e);
				}
			}
			return new DiskObject(index, f, new File(parent, fileName));
		} catch (Exception e) {
			throw new ObjectLoadException("Failed to build canonical disk " +
				"object for index/key: " + index, e);
		}
	}
		
	protected final DiskObject getCanonicalObject(final File parent,
                                                  final String index,
                                                  final boolean makeParentDirs) {
		return getCanonicalObject(parent, index, maxFileNameLength_,
			makeParentDirs);
	}
	
	/*
	protected ObjectList listFiles(final File directory,
		final boolean recursive) {
		// A new index.
		final ObjectList index = new ObjectList();
		try {
			// The cache directory path compiled as a valid regular expression.
			final Pattern cacheDirPattern = compile(quote(
				directory.getAbsolutePath()));
			final Collection<File> files = FileUtils.listFiles(directory,
				null, recursive);
			final Iterator<File> it = files.iterator();
			while(it.hasNext()) {
				final File cache = it.next();
				try {
					final StringBuilder sb = new StringBuilder();
					// Strip the cache directory from the front of the file-name.
					final String path = cacheDirPattern.matcher(
						cache.getAbsolutePath()).replaceAll("");
					// For each token in the path, separated by the file separator
					// extract it, then append to the StringBuilder.
					for(final String element : path.split(quote(FILE_SEPARATOR))) {
						sb.append(element);
					}
					// At this point, we have a full path.  Decode the entire
					// String at once, to avoid the case where the file name
					// may have been split right in the middle of an escape
					// pattern (e.g., "%3/B" can only be decoded as "%3B" so we
					// have to decode the entire path string at once, instead of
					// each element one at a time).
					index.addObject(new DiskObject(decodeBase32(sb.toString()),
						cache));
				} catch (Exception e) {
					// Ignore problems with individual files, and move on.
				}
			}
		} catch (Exception e) {
			throw new RepositoryListObjectsException("Failed to build object " +
				"listing of directory: " + ((directory != null) ?
					directory.getAbsolutePath() : "null"), e);
		}
		return index;
	}
		
	protected ObjectList listFiles(final File directory) {
		return listFiles(directory, true);
	}
	*/
	
	protected File getStoreDir() {
		return storeDir_;
	}
	
}
