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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.kolich.common.util.secure.KolichChecksum.getSHA256Hash;
import static com.kolich.havalo.entities.HavaloEntity.getHavaloGsonInstance;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FileUtils.forceMkdir;
import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.kolich.bolt.ReentrantReadWriteEntityLock;
import com.kolich.bolt.exceptions.LockConflictException;
import com.kolich.havalo.entities.types.DiskObject;
import com.kolich.havalo.entities.types.HashedFileObject;
import com.kolich.havalo.entities.types.HavaloUUID;
import com.kolich.havalo.entities.types.KeyPair;
import com.kolich.havalo.entities.types.Repository;
import com.kolich.havalo.exceptions.objects.ObjectConflictException;
import com.kolich.havalo.exceptions.objects.ObjectDeletionException;
import com.kolich.havalo.exceptions.objects.ObjectLoadException;
import com.kolich.havalo.exceptions.objects.ObjectNotFoundException;
import com.kolich.havalo.exceptions.repositories.DuplicateRepositoryException;
import com.kolich.havalo.exceptions.repositories.RepositoryCreationException;
import com.kolich.havalo.exceptions.repositories.RepositoryDeletionException;
import com.kolich.havalo.exceptions.repositories.RepositoryFlushException;
import com.kolich.havalo.exceptions.repositories.RepositoryLoadException;
import com.kolich.havalo.exceptions.repositories.RepositoryNotFoundException;
import com.kolich.havalo.io.stores.MetaObjectStore;
import com.kolich.havalo.io.stores.ObjectStore;

public final class RepositoryManager extends ObjectStore
	implements InitializingBean {
	
	private static final Logger logger__ =
		LoggerFactory.getLogger(RepositoryManager.class);
		
	/**
	 * The intermediary between the repository manager and the actual
	 * repository meta data sitting on disk.
	 */
	private final static class RepositoryMetaStore extends MetaObjectStore {
		
		public RepositoryMetaStore(final File storeDir) {
			super(storeDir);
		}
		
		public Repository loadById(final HavaloUUID ownerId) {
			Reader reader = null;
			try {
				reader = super.getReader(ownerId.toString());
				return getHavaloGsonInstance().fromJson(reader, Repository.class);
			} finally {
				closeQuietly(reader);
			}
		}
				
	}
	
	private final static class RepositoryMetaWriter {
		
		private static final Logger logger__ =
			LoggerFactory.getLogger(RepositoryMetaWriter.class);
				
		private final RepositoryMetaStore metaStore_;
		
		private final int poolSize_;
		private final ExecutorService writerPool_;
				
		public RepositoryMetaWriter(RepositoryMetaStore metaStore, int poolSize) {
			metaStore_ = metaStore;
			poolSize_ = poolSize;
			writerPool_ = newFixedThreadPool(poolSize_,
				new ThreadFactoryBuilder()
					.setDaemon(true)
					.setNameFormat("havalo-meta-writer-%s")
					.setPriority(Thread.MAX_PRIORITY)
					.build());
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
							}.read();
						}
					} catch (Exception e) {
						logger__.error("Failed to flush repository to disk.", e);
					}
				}
			});
		}
		
	}
	
	private RepositoryMetaStore metaStore_;
	private RepositoryMetaWriter metaWriter_;

	private int maxRepositoryCacheSize_;
	
	/**
	 * Internal in-memory cache to cache a mapping of a {@link HavaloUUID}
	 * to its corresponding {@link Repository}.
	 */
	private Cache<HavaloUUID, Repository> repositories_;
	
	public RepositoryManager(Resource objectStoreDir) throws IOException {
		// Set the directory that will physicially store the repositories.
		super(objectStoreDir.getFile());
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		// Setup the meta store that's used to store meta data about each
		// repository on disk.  The root of the repository meta data store
		// is always the same as the repository root.
		metaStore_ = new RepositoryMetaStore(storeDir_);
		// Setup the meta store writer for this repository.
		metaWriter_ = new RepositoryMetaWriter(metaStore_, maxRepositoryCacheSize_);
		// Setup the in-memory repository cache.
		repositories_ = CacheBuilder
			.newBuilder()
			.maximumSize(maxRepositoryCacheSize_)
			//.expireAfterAccess(hoursTillCacheEviction_, TimeUnit.HOURS)
			.removalListener(new RemovalListener<HavaloUUID, Repository>() {
				// http://code.google.com/p/guava-libraries/wiki/CachesExplained#Eviction
				// Warning: removal listener operations are executed
				// __synchronously__ by default, and since cache maintenance is
				// normally performed during normal cache operations, expensive
				// removal listeners can slow down normal cache function!
				@Override
				public void onRemoval(final RemovalNotification<HavaloUUID, Repository> removed) {
					Repository repo = null;
					try {
						// The repo that was "evicted" could be null if it was
						// garbage collected between the time it was evicted
						// and this listener was called.  This should _not_
						// happen given that we've asked the Cache to maintain
						// _STRONG_ references to the keys and values in the
						// cache, but we should check just in case.
						if((repo = removed.getValue()) != null) {
							// If the repository directory has been deleted, we
							// should not attempt to flush the meta data about it
							// to disk -- the underlying repository has already
							// been lost/removed.
							if(repo.getFile().exists()) {
								// Queue the repository to be flushed to disk.
								metaWriter_.queue(repo);
							}
						} else {
							// Should really _not_ happen based on the notes
							// provided above.
							throw new RepositoryFlushException("Could not " +
								"flush NULL repository -- was perhaps " +
									"already garbage collected?");
						}
					} catch (Exception e) {
						logger__.error("Failed miserably to flush repository " +
							"(id=" + ((repo != null) ? repo.getRepoId() : "NULL") +
								") -- this could be trouble!", e);
					}
				}
			}).build();
	}
	
	public Repository createRepository(final HavaloUUID id,
		final KeyPair keyPair) {
		// Get a proper pointer to this Repository.  Do not fail
		// if the underlying File (directory) is not found yet -- it
		// won't exist yet because it hasn't been created.
		final Repository repo = getRepository(id, false);
		try {
			// Grab an exclusive "write" lock on the Repository and
			// attempt to actually create the corresponding/underlying
			// directory on disk.
			return new ReentrantReadWriteEntityLock<Repository>(repo) {
				@Override
				public Repository transaction() throws Exception {
					// Attempt to create the repository directory if it does
					// not already exist (it shouldn't).
					if(repo.getFile().exists()) {
						throw new DuplicateRepositoryException("Repository " +
							"already exists (file=" +
								repo.getFile().getAbsolutePath() + ", id=" +
									repo.getRepoId() + ")");
					}
					// Create the new directory (and any required parent
					// directories).
					forceMkdir(repo.getFile());
					// Set the access key pair on this repository to the one
					// provided by the caller.
					repo.setKeyPair(keyPair);
					return repo;
				}
				@Override
				public void success(final Repository repo) throws Exception {					
					// Queue the repository to be flushed to disk.
					metaWriter_.queue(repo);
				}
			}.write(true); // Exclusive lock, wait
		} catch (LockConflictException e) {
			throw e;
		} catch (Exception e) {
			throw new RepositoryCreationException("Failed to create " +
				"repository: " + id, e);
		}
	}
	
	public Repository getRepository(final HavaloUUID id,
		final boolean failIfNotFound) {
		checkNotNull(id, "ID of the repository (owner) cannot be null.");
		try {
			// Google Guava documentation claims that implementations of
			// "Cache" should be thread safe, and therefore, wrapping accesses
			// to a Cache instance with synchronized is unnecessary.
			return repositories_.get(id, new Callable<Repository>() {
				@Override
				public Repository call() throws Exception {
					Repository repo = null;
					final File repoFile = getCanonicalObject(storeDir_,
						id.toString(), false).getFile();
					if(repoFile.exists()) {
						// Repository already exists. Load from disk.
						repo = metaStore_.loadById(id);
					} else if(!repoFile.exists() && !failIfNotFound) {
						// The repository does not exist, and we are not
						// supposed to fail.  Create a new one!
						repo = new Repository(
							// Canonical File pointing at the repo directory
							repoFile,
							// Owner ID
							id);
					} else {
						// The repository does not exist, and we are
						// supposed to fail if it wasn't found.
						throw new RepositoryNotFoundException("Could not " +
							"find repository: " + id);
					}
					return repo;
				}
			});
		} catch (Exception e) {
			// Google Guava (the cache) wraps exceptions thrown from within its
			// Callable.call() method.  When the Exception e ultimately makes it
			// here, the real "cause" of the failure is embedded inside of
			// e.getCause().  Not a big deal, just a detail to be aware of.
			final Throwable cause = e.getCause();
			if(cause instanceof RepositoryNotFoundException) {
				throw (RepositoryNotFoundException)cause;
			} else {
				throw new RepositoryLoadException("Failed to load " +
					"repository: " + id, e);
			}
		}
	}
	
	public Repository getRepository(final HavaloUUID id) {
		return getRepository(id, true);
	}
	
	public void deleteRepository(final Repository repo) {
		try {
			new ReentrantReadWriteEntityLock<Void>(repo) {
				@Override
				public Void transaction() throws Exception {
					// Delete all in-memory mappings to all objects in the repo.
					repo.deleteAllObjects();
					// Get a handle to the repository directory and recursively
					// delete it and everything inside of it.
					final File repoFile = repo.getFile();
					if(!deleteQuietly(repoFile)) {
						throw new RepositoryDeletionException("Failed to " +
							"recursively delete repository (repo=" +
								repo.getRepoId() + ", file=" +
									repoFile.getCanonicalPath() + ")");
					}
					// Delete the meta data associated with the repository too.
					metaStore_.delete(repo);
					return null;
				}
				@Override
				public void success(final Void v) throws Exception {
					// On deletion success, remove the repository from the
					// local in-memory "cache".  If the repository was not
					// deleted successfully, then fail.
					repositories_.invalidate(repo.getRepoId());
				}
			}.write(); // Exclusive lock on repo, no wait
		} catch (LockConflictException e) {
			throw e;
		} catch (RepositoryDeletionException e) {
			throw e;
		} catch (Exception e) {
			throw new RepositoryDeletionException("Failed to delete " +
				"repository (repo=" + repo.getKey() + ")", e);
		}
	}
	
	public void deleteRepository(final HavaloUUID id) {
		deleteRepository(getRepository(id));		
	}
	
	public HashedFileObject getHashedFileObject(final HavaloUUID id,
		final String key, final boolean failIfNotFound) {
		return getHashedFileObject(getRepository(id), key, failIfNotFound);
	}
	
	public HashedFileObject getHashedFileObject(final Repository repo,
		final String key, final boolean failIfNotFound) {
		try {
			return new ReentrantReadWriteEntityLock<HashedFileObject>(repo) {
				@Override
				public HashedFileObject transaction() throws Exception {
					HashedFileObject hfo = repo.getObject(key);
					if(hfo == null && !failIfNotFound) {
						hfo = new HashedFileObject(key);
						// Add the object to repository.
						repo.addObject(key, hfo);
					} else if(hfo == null && failIfNotFound) {
						// The object with the given key did not exist in
						// this repository.
						throw new ObjectNotFoundException("Object not found " +
							"(id=" + repo.getRepoId() + ", key=" + key + ")");
					}
					return hfo;
				}
			}.read(false); // Shared reader lock on repo, no wait
		} catch (LockConflictException e) {
			throw e;
		} catch (ObjectNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new ObjectLoadException("Failed to load hashed file " +
				"object (id=" + repo.getRepoId() + ", key=" + key + ")", e);
		}
	}
	
	public HashedFileObject deleteHashedFileObject(final HavaloUUID id,
		final String key, final String ifMatch) {
		return deleteHashedFileObject(getRepository(id), key, ifMatch);
	}
		
	public HashedFileObject deleteHashedFileObject(final Repository repo,
		final String key, final String ifMatch) {
		try {
			return new ReentrantReadWriteEntityLock<HashedFileObject>(repo) {
				@Override
				public HashedFileObject transaction() throws Exception {
					// Get the object we're going to delete.
					final HashedFileObject hfo = repo.getObject(key);
					// Make sure the object exists -- if there was no object
					// with the given key in the repo then it does not exist.
					if(hfo == null) {
						throw new ObjectNotFoundException("Object " +
							"not found (id=" + repo.getRepoId() + ", key=" +
									key + ")");
					}
					// Now attempt to grab an exclusive write lock on the
					// file object do delete.  And, attempt to follow through
					// on the physical delete from the platters.
					return new ReentrantReadWriteEntityLock<HashedFileObject>(hfo) {
						@Override
						public HashedFileObject transaction() throws Exception {
							// If we have an incoming If-Match, we need to
							// compare that against the current HFO before we
							// attempt to delete.  If the If-Match ETag does not
							// match, fail.
							if(ifMatch != null && hfo.getETag() != null) {
								// OK, we have an incoming If-Match ETag, use it.
								if(!ifMatch.equals(hfo.getETag())) {
									throw new ObjectConflictException("Failed " +
										"to delete HFO; incoming If-Match " +
											"ETag does not match (hfo=" +
												hfo.getName() + ", etag=" +
													hfo.getETag() + ", if-match=" +
														ifMatch + ")");
								}
							}
							// OK, we either didn't have an incoming If-Match
							// to check, or we did and it passed -- grab a
							// pointer to the actual File on disk.
							final File hfoFile = getCanonicalObject(repo.getFile(),
								getSHA256Hash(key), false).getFile();
							// Validate that the file exists before we attempt
							// to physically remove it from disk.
							if(hfoFile.exists()) {
								if(deleteQuietly(hfoFile)) {
									repo.deleteObject(key);
								} else {
									throw new ObjectDeletionException("Failed " +
										"to delete object from disk (file=" +
											hfoFile.getAbsolutePath() +
												", id=" + repo.getRepoId() + ")");
								}
							} else {
								// So the object was in the index in memory, but
								// was not found on disk. Hm, probably a bigger
								// issue.
								throw new ObjectNotFoundException("Object " +
									"file on disk not found (file=" +
										hfoFile.getAbsolutePath() + ", id=" +
											repo.getRepoId() + ", key=" +
												key + ")");
							}
							return hfo;
						}
					}.write(); // Exclusive lock, fail immediately if HFO busy
				}
				@Override
				public void success(final HashedFileObject hfo) throws Exception {					
					// If we get here, then the deletion must have succeeded.
					// Queue the repository to be flushed to disk.
					metaWriter_.queue(repo);
				}
			}.read(false); // Shared reader lock on repo, no wait
		} catch (LockConflictException e) {
			throw e;
		} catch (ObjectNotFoundException e) {
			throw e;
		} catch (ObjectDeletionException e) {
			throw e;
		} catch (Exception e) {
			throw new ObjectDeletionException("Failed to delete file " +
				"object (id=" + repo.getRepoId() + ", key=" + key + ")", e);
		}
	}
	
	public final DiskObject getCanonicalObject(final Repository repo,
		final HashedFileObject hfo, final boolean makeParentDirs) {
		return getCanonicalObject(repo.getFile(),
			getSHA256Hash(hfo.getName()),
			makeParentDirs);
	}
		
	public void flushRepository(final Repository repo) {
		metaWriter_.queue(repo);
	}
	
	public void setMaxRepositoryCacheSize(int maxRepositoryCacheSize) {
		maxRepositoryCacheSize_ = maxRepositoryCacheSize;
	}
	
}
