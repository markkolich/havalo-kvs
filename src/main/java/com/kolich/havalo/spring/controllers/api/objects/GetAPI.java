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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.kolich.common.util.URLEncodingUtils.urlDecode;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copyLarge;
import static org.apache.commons.lang3.Validate.notEmpty;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.kolich.bolt.ReentrantReadWriteEntityLock;
import com.kolich.havalo.entities.types.DiskObject;
import com.kolich.havalo.entities.types.HashedFileObject;
import com.kolich.havalo.entities.types.HavaloUUID;
import com.kolich.havalo.entities.types.Repository;
import com.kolich.havalo.exceptions.objects.ObjectNotFoundException;
import com.kolich.havalo.io.managers.RepositoryManager;
import com.kolich.havalo.spring.controllers.HavaloControllerClosure;
import com.kolich.havalo.spring.controllers.api.AbstractHavaloAPIController;

@Controller
@RequestMapping(value="/object")
public class GetAPI extends AbstractHavaloAPIController {
	
	// http://benramsey.com/blog/2008/05/206-partial-content-and-range-requests/
	
	private static final Logger logger__ =
		LoggerFactory.getLogger(GetAPI.class);
		
	@Autowired
	public GetAPI(RepositoryManager repositoryManager) {
		super(repositoryManager);
	}
	
	@RequestMapping(method={RequestMethod.GET}, value="{key:.*}")
	public void get(@PathVariable final String key,
		final HttpServletResponse response,
		final HavaloUUID userId) {
		new HavaloControllerClosure<Void>(
			"GET:/api/object/" + key, logger__) {
			@Override
			public Void doit() throws Exception {
				notEmpty(key, "Key cannot be null or empty.");
				final Repository repo = getRepository(userId);
				// In theory, another thread could have "deleted" the repository
				// from the time we fetched it and from the time we attempt to
				// capture a write() lock on it to load the HFO.  If this happens,
				// however, the underlying HFO would have been deleted on disk
				// and therefore the request to get the HFO would fail gracefully.
				final HashedFileObject hfo = getHashedFileObject(repo,
					// URL-decode the incoming key on the path.
					urlDecode(key),
					// Fail if not found.
					true);
				new ReentrantReadWriteEntityLock<HashedFileObject>(hfo) {
					@Override
					public HashedFileObject transaction() throws Exception {
						final DiskObject object = getCanonicalObject(repo, hfo);
						streamHeaders(object, hfo, response);
						streamObject(object, response);
						return hfo;
					}
				}.read(); // Shared read lock on file object
				return null; // Meh, Void
			}
		}.execute();
	}
	
	@RequestMapping(method={RequestMethod.HEAD}, value="{key:.*}")
	public void head(@PathVariable final String key,
		final HttpServletResponse response,
		final HavaloUUID userId) {
		new HavaloControllerClosure<Void>(
			"HEAD:/api/object/" + key, logger__) {
			@Override
			public Void doit() throws Exception {
				notEmpty(key, "Key cannot be null or empty.");
				final Repository repo = getRepository(userId);
				final HashedFileObject hfo = getHashedFileObject(repo,
					// URL-decode the incoming key on the path.
					urlDecode(key),
					// Fail if not found.
					true);
				new ReentrantReadWriteEntityLock<HashedFileObject>(hfo) {
					@Override
					public HashedFileObject transaction() throws Exception {
						final DiskObject object = getCanonicalObject(repo, hfo);
						streamHeaders(object, hfo, response);
						return hfo;
					}
				}.read(); // Shared read lock on file object
				return null; // Meh, Void
			}
		}.execute();
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
		final HttpHeaders headers = hfo.getHeaders();
		// Always set the Content-Length header to the actual length of the
		// file on disk -- effectively overriding any "Content-Length" meta
		// data header set by the user in the PUT request.
		headers.setContentLength(object.getFile().length());
		// Set the Content-Type header to a default if one was not set by
		// the consumer in the meta data.
		if(headers.getContentType() == null) {
			headers.setContentType(APPLICATION_OCTET_STREAM);
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
			// as we basically can.
			logger__.error("Failed to stream object to consumer.", e);
		} finally {
			closeQuietly(is);
			closeQuietly(os);
		}
	}
	
}
