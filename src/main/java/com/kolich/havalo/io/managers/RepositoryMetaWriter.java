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

import static java.util.concurrent.Executors.newFixedThreadPool;

import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.kolich.bolt.ReentrantReadWriteEntityLock;
import com.kolich.havalo.entities.types.Repository;
import com.kolich.havalo.io.MetaStore;

public final class RepositoryMetaWriter {
	
	private static final Logger logger__ =
		LoggerFactory.getLogger(RepositoryMetaWriter.class);
	
	private static final int DEFAULT_WRITER_POOL_SIZE = 20;
			
	private final MetaStore metaStore_;		
	private final ExecutorService writerPool_;
			
	public RepositoryMetaWriter(final MetaStore metaStore,
		final int poolSize) {
		metaStore_ = metaStore;
		writerPool_ = newFixedThreadPool(poolSize,
			new ThreadFactoryBuilder()
				.setDaemon(true)
				.setNameFormat("havalo-meta-writer-%s")
				.setPriority(Thread.MAX_PRIORITY)
				.build());
	}
	
	public RepositoryMetaWriter(final MetaStore metaStore) {
		this(metaStore, DEFAULT_WRITER_POOL_SIZE);
	}
	
	public void queue(final Repository repo) {
		writerPool_.execute(new Runnable() {
			@Override
			public void run() {
				// Grab a read lock; ensures no writes will be allowed
				// during the flush-to-disk process.
				try {
					if(repo != null) {
						new ReentrantReadWriteEntityLock<Repository>(repo) {
							@Override
							public Repository transaction() throws Exception {
								// Flush the repository meta data to disk.
								metaStore_.save(repo);
								return repo;
							}
						}.read(); // Shared read, wait
					}
				} catch (Exception e) {
					logger__.error("Failed to flush repository to disk.", e);
				}
			}
		});
	}

}
