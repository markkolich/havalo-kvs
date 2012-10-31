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

package com.kolich.havalo.spring.controllers.api.objects;

import static com.kolich.common.util.URLEncodingUtils.urlDecode;
import static com.kolich.common.util.secure.KolichChecksum.getSHA1HashAndCopy;
import static com.kolich.spring.controllers.KolichControllerClosure.getJsonResponseEntity;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpHeaders.IF_MATCH;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.kolich.bolt.ReentrantReadWriteEntityLock;
import com.kolich.havalo.entities.types.DiskObject;
import com.kolich.havalo.entities.types.HashedFileObject;
import com.kolich.havalo.entities.types.HavaloUUID;
import com.kolich.havalo.entities.types.Repository;
import com.kolich.havalo.exceptions.objects.ObjectConflictException;
import com.kolich.havalo.io.managers.RepositoryManager;
import com.kolich.havalo.spring.controllers.HavaloControllerClosure;
import com.kolich.havalo.spring.controllers.api.AbstractHavaloAPIController;

@Controller
@RequestMapping(value="/object")
public class PutAPI extends AbstractHavaloAPIController {
	
	private static final Logger logger__ =
		LoggerFactory.getLogger(PutAPI.class);
	
	// -Dorg.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH=true
		
	// http://stackoverflow.com/questions/7981640/spring-3-0-fileupload-only-with-post
	// http://stackoverflow.com/questions/1332691/how-to-configure-logs-catalina-out-of-tomcat-6-for-per-app-configure-web-app-s
	
	// http://static.springsource.org/spring/docs/3.0.x/spring-framework-reference/html/mvc.html
	
	// http://boplicity.nl/confluence/display/spring/Using+HTTP+PUT+and+Spring+MVC+to+upload+files
	
	@Autowired
	public PutAPI(RepositoryManager repositoryManager) {
		super(repositoryManager);
	}
	
	@RequestMapping(method={RequestMethod.POST})
	public ResponseEntity<byte[]> uploadPost(
		@RequestParam(required=true) final String key,
		@RequestParam(required=true) final String ifMatch,
		@RequestParam(required=true) final MultipartFile file,
		// On a POST upload, the HTTP Content-Type is passed via a
		// Servlet request parameter (not a header).
		@RequestParam(required=false) final String contentType,
		final HavaloUUID userId) {
		return new HavaloControllerClosure<ResponseEntity<byte[]>>(
			"POST:/api/object?key=" + key, logger__) {
			@Override
			public ResponseEntity<byte[]> doit() throws Exception {
				notEmpty(key, "Key cannot be null or empty.");
				return processUpload(key,
					// If-Match on the request, if any
					ifMatch,
					// The HTTP Content-Type of the request, if any
					contentType,
					// Will be closed in processUpload()
					file.getInputStream(),
					// The authenticated owner of this object
					userId);
			}
		}.execute();
	}
	
	@RequestMapping(method={RequestMethod.PUT}, value="{key:.*}")
	public ResponseEntity<byte[]> uploadPut(
		// The object key.
		@PathVariable final String key,
		// Only apply the PUT if the object ETag matches the provided
		// HTTP If-Match request header.
		@RequestHeader(value=IF_MATCH, required=false) final String ifMatch,
		// On a PUT load, the HTTP Content-Type is passed via the
		// HTTP Content-Type request header.
		@RequestHeader(value=CONTENT_TYPE, required=false) final String contentType,
		// In PUT land, there's really no such thing as "multipart" PUT
		// uploads -- a PUT is really just a stream of binary data written
		// to the server via HTTP.
		final HttpServletRequest request,
		final HavaloUUID userId) {
		return new HavaloControllerClosure<ResponseEntity<byte[]>>(
			"PUT:/api/object/" + key, logger__) {
			@Override
			public ResponseEntity<byte[]> doit() throws Exception {
				notEmpty(key, "Key cannot be null or empty.");
				return processUpload(key,
					// The HTTP If-Match header on the request, if any
					ifMatch,
					// The HTTP Content-Type of the request, if any
					contentType,
					// Will be closed in processUpload()
					request.getInputStream(),
					// The authenticated owner of this object
					userId);
			}
		}.execute();
	}
	
	private final ResponseEntity<byte[]> processUpload(final String key,
		final String ifMatch, final String contentType, final InputStream is,
		final HavaloUUID userId) throws Exception {
		final Repository repo = getRepository(userId);
		final HashedFileObject hfo = getHashedFileObject(repo,
			// URL-decode the incoming key (the name of the object)
			urlDecode(key));
		new ReentrantReadWriteEntityLock<HashedFileObject>(hfo) {
			@Override
			public HashedFileObject transaction() throws Exception {
				// If we have an incoming If-Match, we need to compare that
				// against the current HFO before we attempt to update.  If
				// the If-Match ETag does not match, fail.
				if(ifMatch != null && hfo.getETag() != null) {
					// OK, we have an incoming If-Match ETag, use it.
					// NOTE: HFO's will _always_ have an ETag attached to their
					// meta-data.  ETag's are always computed for HFO's upload.
					// But new HFO's (one's the repo have never seen before) may
					// not yet have an ETag.
					if(!ifMatch.equals(hfo.getETag())) {
						throw new ObjectConflictException("Failed to update " +
							"HFO; incoming If-Match ETag does not match (hfo=" +
								hfo.getName() + ", etag=" + hfo.getETag() +
									", if-match=" + ifMatch + ")");
					}
				}
				final DiskObject object = getCanonicalObject(repo, hfo,
					// Create the File on disk if it does not already
					// exist. Yay!
					true);
				// The file itself (should exist now).
				final File objFile = object.getFile();
				OutputStream os = null;
				try {
					// Create a new output stream which will point at
					// the new home of this HFO on the file system.
					os = new FileOutputStream(objFile);
					// Compute the ETag (an MD5 hash of the file) while
					// copying the file into place.  The bytes of the
					// input stream and piped into an MD5 digest _and_
					// to the output stream -- ideally computing the
					// hash and copying the file at the same time.
					// Set the resulting ETag header (meta data).
					hfo.setETag(getSHA1HashAndCopy(is, os));
					// Set the Last-Modified header (meta data).
					hfo.setLastModified(objFile.lastModified());
					// Set the Content-Length header (meta data).
					hfo.setContentLength(objFile.length());
					// Set the Content-Type header (meta data).
					if(contentType != null) {
						hfo.setContentType(contentType);
					}
				} finally {
					closeQuietly(os);
					closeQuietly(is);
				}
				return hfo;
			}
			@Override
			public void success(final HashedFileObject hfo) throws Exception {
				// On success only, ask the repo manager to asynchronously
				// flush this repository's meta data to disk.
				flushRepository(repo);
			}
		}.write(); // Exclusive lock on this HFO, no wait
		// Append an ETag header to the response for the PUT'ed object.
		final HttpHeaders headers = new HttpHeaders();
		headers.setETag(hfo.getETag());
		return getJsonResponseEntity(hfo, headers, HttpStatus.OK);
	}
	
}
