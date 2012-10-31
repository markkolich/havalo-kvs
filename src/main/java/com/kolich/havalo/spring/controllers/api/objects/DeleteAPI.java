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
import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.http.HttpHeaders.IF_MATCH;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.kolich.havalo.entities.types.HavaloUUID;
import com.kolich.havalo.io.managers.RepositoryManager;
import com.kolich.havalo.spring.controllers.HavaloControllerClosure;
import com.kolich.havalo.spring.controllers.api.AbstractHavaloAPIController;

@Controller
@RequestMapping(value="/object")
public class DeleteAPI extends AbstractHavaloAPIController {
	
	private static final Logger logger__ =
		LoggerFactory.getLogger(DeleteAPI.class);
		
	@Autowired
	public DeleteAPI(RepositoryManager repositoryManager) {
		super(repositoryManager);
	}
	
	@RequestMapping(method={RequestMethod.DELETE}, value="{key:.*}")
	public ResponseEntity<byte[]> delete(
		// The object key.
		@PathVariable final String key,
		// Only apply the DELETE if the object ETag matches the provided
		// HTTP If-Match request header.
		@RequestHeader(value=IF_MATCH, required=false) final String ifMatch,
		final HttpServletResponse response,
		final HavaloUUID userId) {
		return new HavaloControllerClosure<ResponseEntity<byte[]>>(
			"DELETE:/api/object/" + key, logger__) {
			@Override
			public ResponseEntity<byte[]> doit() throws Exception {
				notEmpty(key, "Key cannot be null or empty.");
				// The delete operation does return a pointer to the "deleted"
				// HFO, but we're not using it, we're just dropping it on the
				// floor (intentionally not returning it to the caller).
				deleteHashedFileObject(userId,
					// URL-decode the incoming key on the path.
					urlDecode(key),
					// Only delete the object if the provided ETag via the
					// If-Match header matches the object on disk.
					ifMatch);
				// Return an empty HTTP 204 No Content on success.
				return getEmptyResponseEntity(HttpStatus.NO_CONTENT);
			}
		}.execute();
	}
	
}
