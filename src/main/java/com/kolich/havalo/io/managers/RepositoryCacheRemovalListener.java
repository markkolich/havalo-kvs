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

package com.kolich.havalo.io.managers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.kolich.havalo.entities.types.HavaloUUID;
import com.kolich.havalo.entities.types.Repository;
import com.kolich.havalo.exceptions.repositories.RepositoryFlushException;

public final class RepositoryCacheRemovalListener
	implements RemovalListener<HavaloUUID,Repository> {
	
	// http://code.google.com/p/guava-libraries/wiki/CachesExplained#Eviction
	// Warning: removal listener operations are executed
	// __synchronously__ by default, and since cache maintenance is
	// normally performed during normal cache operations, expensive
	// removal listeners can slow down normal cache function!
	
	private static final Logger logger__ =
		LoggerFactory.getLogger(RepositoryCacheRemovalListener.class);
	
	private final RepositoryMetaWriter metaWriter_;
	
	public RepositoryCacheRemovalListener(final RepositoryMetaWriter metaWriter) {
		metaWriter_ = metaWriter;
	}

	@Override
	public void onRemoval(final RemovalNotification<HavaloUUID,Repository> removed) {
		Repository repo = null;
		try {
			// The repo that was "evicted" could be null if it was
			// garbage collected between the time it was evicted
			// and this listener was called.  This should _not_
			// happen given that we've asked the Cache to maintain
			// _STRONG_ references to the keys and values in the
			// cache, but we should check just in case.
			if((repo = removed.getValue()) != null) {
				// If the repository directory has been deleted (invalidated),
				// we should not attempt to flush the meta data for it
				// to disk -- the underlying repository has been deleted.
				if(repo.getFile().exists()) {
					// Queue the repository to be flushed to disk.
					metaWriter_.queue(repo);
				} else {
					logger__.debug("Not flushing repo meta " +
						"data, underlying repo directory is " +
						"missing (id=" + repo.getKey() + ", " +
						"file=" + repo.getFile().getCanonicalPath() +
						") -- was probably for a deleted repository.");
				}
			} else {
				// Should really _not_ happen based on the notes
				// provided above.
				throw new RepositoryFlushException("Could not " +
					"flush NULL repository -- was perhaps " +
						"already GC'ed?");
			}
		} catch (Exception e) {
			logger__.error("Failed miserably to flush repository " +
				"(id=" + ((repo != null) ? repo.getRepoId() : "NULL") +
					") -- could be trouble!", e);
		}
	}

}
