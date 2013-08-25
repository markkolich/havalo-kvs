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

package com.kolich.havalo.servlets.api;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.io.Files.move;
import static com.google.common.net.HttpHeaders.CONTENT_LENGTH;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static com.google.common.net.HttpHeaders.ETAG;
import static com.google.common.net.HttpHeaders.IF_MATCH;
import static com.google.common.net.MediaType.OCTET_STREAM;
import static com.kolich.common.util.secure.KolichChecksum.getSHA1HashAndCopy;
import static com.kolich.havalo.HavaloConfigurationFactory.HAVALO_UPLOAD_MAX_SIZE_PROPERTY;
import static javax.servlet.http.HttpServletResponse.SC_NO_CONTENT;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copyLarge;
import static org.apache.commons.lang3.Validate.notEmpty;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;

import com.kolich.bolt.ReentrantReadWriteEntityLock;
import com.kolich.common.util.secure.KolichChecksum.KolichChecksumException;
import com.kolich.havalo.entities.types.DiskObject;
import com.kolich.havalo.entities.types.HashedFileObject;
import com.kolich.havalo.entities.types.KeyPair;
import com.kolich.havalo.entities.types.Repository;
import com.kolich.havalo.exceptions.objects.ObjectConflictException;
import com.kolich.havalo.exceptions.objects.ObjectLengthNotSpecifiedException;
import com.kolich.havalo.exceptions.objects.ObjectNotFoundException;
import com.kolich.havalo.exceptions.objects.ObjectTooLargeException;
import com.kolich.havalo.servlets.HavaloApiServlet;
import com.kolich.havalo.servlets.HavaloAuthenticatingServletClosureHandler;
import com.kolich.servlet.entities.ServletClosureEntity;

public final class ObjectApi extends HavaloApiServlet {
	
	private static final long serialVersionUID = 2047425072395464972L;
	
	private static final Logger logger__ = getLogger(ObjectApi.class);
	
	private static final String OCTET_STREAM_TYPE = OCTET_STREAM.toString();
	
	private final long uploadMaxSize_;
	
	public ObjectApi() {
		super();
		uploadMaxSize_ = getHavaloConfig().getLong(
			HAVALO_UPLOAD_MAX_SIZE_PROPERTY);
	}
	
	@Override
	public final <S extends ServletClosureEntity> HavaloAuthenticatingServletClosureHandler<S>
		head(final AsyncContext context) {
		return new HavaloAuthenticatingServletClosureHandler<S>(logger__, context) {
			@Override
			public S execute(final KeyPair userKp) throws Exception {
				final Repository repo = getRepository(userKp.getKey());
				new ReentrantReadWriteEntityLock<Void>(repo) {
					@Override
					public Void transaction() throws Exception {
						// URL-decode the incoming key (the name of the object)
						final String key = getRequestObject();
						notEmpty(key, "Key cannot be null or empty.");
						final HashedFileObject hfo = getHashedFileObject(repo,
							// The URL-decoded key of the object to delete.
							key,
							// Fail if not found.
							true);
						new ReentrantReadWriteEntityLock<HashedFileObject>(hfo) {
							@Override
							public HashedFileObject transaction() throws Exception {
								final DiskObject object = getCanonicalObject(repo, hfo);
								streamHeaders(object, hfo, response_);
								return hfo;
							}
						}.read(); // Shared read lock on file object
						return null;
					}
				}.read(); // Shared read lock on repo
				// Return null to tell the parent that we've
				// handled the response.
				return null;
			}
		};
	}
	
	@Override
	public final <S extends ServletClosureEntity> HavaloAuthenticatingServletClosureHandler<S>
		get(final AsyncContext context) {
		return new HavaloAuthenticatingServletClosureHandler<S>(logger__, context) {
			@Override
			public S execute(final KeyPair userKp) throws Exception {
				final Repository repo = getRepository(userKp.getKey());
				new ReentrantReadWriteEntityLock<Void>(repo) {
					@Override
					public Void transaction() throws Exception {
						// URL-decode the incoming key (the name of the object)
						final String key = getRequestObject();							
						notEmpty(key, "Key cannot be null or empty.");
						final HashedFileObject hfo = getHashedFileObject(repo,
							// The URL-decoded key of the object to delete.
							key,
							// Fail if not found.
							true);
						new ReentrantReadWriteEntityLock<HashedFileObject>(hfo) {
							@Override
							public HashedFileObject transaction() throws Exception {
								final DiskObject object = getCanonicalObject(repo, hfo);
								// Validate that the object file exists on disk
								// before we attempt to load it.
								if(!object.getFile().exists()) {
									throw new ObjectNotFoundException("Failed " +
										"to find canonical object on disk " +
										"(key=" + key + ", file=" +
										object.getFile().getAbsolutePath() +
										")");
								}
								streamHeaders(object, hfo, response_);
								streamObject(object, response_);
								return hfo;
							}
						}.read(); // Shared read lock on file object, wait
						return null;
					}
				}.read(false); // Shared read lock on repo, no wait
				// Return null to tell the parent that we've
				// handled the response.
				return null;
			}
		};
	}
	
	/*
	@Override
	public final HavaloApiServletClosure<HashedFileObject> post(
		final AsyncContext context) {
		return new HavaloApiServletClosure<HashedFileObject>(logger__, context) {
			@Override
			public HashedFileObject execute(final HavaloUUID userId) throws Exception {
				
				return null;
			}
		};
	}
	*/
	
	@Override
	public final HavaloAuthenticatingServletClosureHandler<HashedFileObject> put(
		final AsyncContext context) {
		return new HavaloAuthenticatingServletClosureHandler<HashedFileObject>(logger__, context) {
			@Override
			public HashedFileObject execute(final KeyPair userKp) throws Exception {
				final Repository repo = getRepository(userKp.getKey());
				return new ReentrantReadWriteEntityLock<HashedFileObject>(repo) {
					@Override
					public HashedFileObject transaction() throws Exception {
						// URL-decode the incoming key (the name of the object)
						final String key = getRequestObject();
						notEmpty(key, "Key cannot be null or empty.");
						final String contentType = getHeader(CONTENT_TYPE);
						final String ifMatch = getHeader(IF_MATCH);
						final long contentLength;
						// Havalo requires the consumer to send a Content-Length
						// request header with the request when uploading an
						// object.
						if((contentLength = getHeaderAsLong(CONTENT_LENGTH)) < 0L) {
							// A value of -1 indicates that no Content-Length
							// header was set.  Bail gracefully.
							throw new ObjectLengthNotSpecifiedException("Client " +
								"sent request where '" + CONTENT_LENGTH +
								"' header was not present or less than zero.");
						}
						// Only accept the object if the Content-Length of the
						// incoming request is less than or equal to the max
						// upload size.
						if(contentLength > uploadMaxSize_) {
							throw new ObjectTooLargeException("The '" +
								CONTENT_LENGTH + "' of the incoming request " +
								"is too large. Max upload size allowed is " +
								uploadMaxSize_ + "-bytes.");
						}
						final HashedFileObject hfo = getHashedFileObject(repo, key);
						return new ReentrantReadWriteEntityLock<HashedFileObject>(hfo) {
							@Override
							public HashedFileObject transaction() throws Exception {
								final String eTag = hfo.getFirstHeader(ETAG);
								// If we have an incoming If-Match, we need to compare
								// that against the current HFO before we attempt to
								// update.  If the If-Match ETag does not match, fail.
								if(ifMatch != null && eTag != null) {
									// OK, we have an incoming If-Match ETag, use it.
									// NOTE: HFO's will _always_ have an ETag attached
									// to their meta-data.  ETag's are always computed
									// for HFO's upload. But new HFO's (one's the repo
									// have never seen before) may not yet have an ETag.
									if(!ifMatch.equals(eTag)) {
										throw new ObjectConflictException("Failed " +
											"to update HFO; incoming If-Match ETag " +
											"does not match (hfo=" + hfo.getName() +
											", etag=" + eTag + ", if-match=" +
											ifMatch + ")");
									}
								}
								final DiskObject object = getCanonicalObject(
									repo, hfo,
									// Create the File on disk if it does not
									// already exist. Yay!
									true);
								// The file itself (should exist now).
								final File objFile = object.getFile();
								final File tempObjFile = object.getTempFile();
								InputStream is = null;
								OutputStream os = null;
								try {
									is = request_.getInputStream();
									// Create a new output stream which will point at
									// the new home of this temp HFO on the file system.
									os = new FileOutputStream(tempObjFile);
									// Compute the ETag (an MD5 hash of the file) while
									// copying the file into place.  The bytes of the
									// input stream and piped into an MD5 digest _and_
									// to the output stream -- ideally computing the
									// hash and copying the file at the same time.
									// Set the resulting ETag header (meta data).
									hfo.setETag(getSHA1HashAndCopy(is, os,
										// Only copy as much as the incoming
										// Content-Length header sez is going
										// to be sent.  Anything more than this
										// is caught gracefully and dropped.
										contentLength));
									// Move the uploaded file into place (moves
									// the file from the temp location to the
									// real destination inside of the repository
									// on disk).
									move(tempObjFile, objFile);
									// Set the Last-Modified header (meta data).
									hfo.setLastModified(objFile.lastModified());
									// Set the Content-Length header (meta data).
									hfo.setContentLength(objFile.length());
									// Set the Content-Type header (meta data).
									if(contentType != null) {
										hfo.setContentType(contentType);
									}
								} catch (KolichChecksumException e) {
									// Quietly delete the object on disk when
									// it has exceeded the max upload size allowed
									// by this Havalo instance.
									throw new ObjectTooLargeException("The " +
										"size of the incoming object is too " +
										"large. Max upload size is " +
										uploadMaxSize_ + "-bytes.", e);
								} finally {
									// Delete the file from the temp upload
									// location.  Note, this file may not exist
									// if the upload was successful and the object
									// was moved into place.. which is OK here.
									deleteQuietly(tempObjFile);
									// Close any associated streams for this
									// object upload.
									closeQuietly(os);
									closeQuietly(is);
								}
								// Append an ETag header to the response for the
								// PUT'ed object.
								setHeader(ETAG, hfo.getFirstHeader(ETAG));
								return hfo;
							}
							@Override
							public void success(final HashedFileObject e) throws Exception {
								// On success only, ask the repo manager to
								// asynchronously flush this repository's meta
								// data to disk.
								flushRepository(repo);
							}
						}.write(); // Exclusive lock on this HFO, no wait
					}
				}.read(false); // Shared read lock on repo, no wait
			}
		};
	}
	
	@Override
	public final <S extends ServletClosureEntity> HavaloAuthenticatingServletClosureHandler<S>
		delete(final AsyncContext context) {
		return new HavaloAuthenticatingServletClosureHandler<S>(logger__, context) {
			@Override
			public S execute(final KeyPair userKp) throws Exception {
				// URL-decode the incoming key (the name of the object)
				final String key = getRequestObject();							
				notEmpty(key, "Key cannot be null or empty.");
				final String ifMatch = getHeader(IF_MATCH);
				// The delete operation does return a pointer to the "deleted"
				// HFO, but we're not using it, we're just dropping it on the
				// floor (intentionally not returning it to the caller).
				deleteHashedFileObject(userKp.getKey(),
					// The URL-decoded key of the object to delete.
					key,
					// Only delete the object if the provided ETag via the
					// If-Match header matches the object on disk.
					ifMatch);
				// Send an empty HTTP 204 No Content back on success.
				setStatus(SC_NO_CONTENT);
				// Return null to tell the parent that we've
				// handled the response ourselves.
				return null;
			}
		};
	}
	
	private static final void streamHeaders(final DiskObject object,
		final HashedFileObject hfo, final HttpServletResponse response) {
		checkNotNull(hfo, "Hashed file object cannot be null.");
		//final File hfoFile = hfo.getFile();
		// Validate that the File object still exists.
		if(!object.getFile().exists()) {
			throw new ObjectNotFoundException("Object not " +
				"found (file=" + object.getFile().getAbsolutePath() +
				", key=" + hfo.getName() + ")");
		}
		// Extract any response headers from this objects' meta data.
		final Map<String,List<String>> headers = hfo.getHeaders();
		// Always set the Content-Length header to the actual length of the
		// file on disk -- effectively overriding any "Content-Length" meta
		// data header set by the user in the PUT request.
		headers.put(CONTENT_LENGTH, Arrays.asList(new String[]{
			Long.toString(object.getFile().length())}));
		// Set the Content-Type header to a default if one was not set by
		// the consumer in the meta data.
		if(headers.get(CONTENT_TYPE) == null) {
			headers.put(CONTENT_TYPE, Arrays.asList(new String[]{
				OCTET_STREAM_TYPE}));
		}
		// Now, send all headers to the response stream.
		for(final Map.Entry<String, List<String>> entry : headers.entrySet()) {
			final String key = entry.getKey();
			for(final String value : entry.getValue()) {
				response.addHeader(key, value);
			}
		}
	}
	
	private static final void streamObject(final DiskObject object,
		final HttpServletResponse response) {
		InputStream is = null;
		OutputStream os = null;
		try {
			// Open an input stream to read the file from disk.
			// Write it directly to the output stream of the response.
			is = new FileInputStream(object.getFile());
			os = response.getOutputStream();
			copyLarge(is, os);
		} catch (Exception e) {
			// On any Exception case, just log the failure and move on.
			// We're closing the output stream in the finally{} block below
			// so it's not like we can fail here then somehow return an error
			// message to the API consumer.  We're handling this as gracefully
			// as best we can.
			logger__.error("Failed to stream object to client.", e);
		} finally {
			closeQuietly(is);
			closeQuietly(os);
		}
	}
	
}
