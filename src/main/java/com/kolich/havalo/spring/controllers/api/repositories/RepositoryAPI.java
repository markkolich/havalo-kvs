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

package com.kolich.havalo.spring.controllers.api.repositories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.kolich.bolt.ReentrantReadWriteEntityLock;
import com.kolich.havalo.entities.types.HavaloUUID;
import com.kolich.havalo.entities.types.KeyPair;
import com.kolich.havalo.entities.types.ObjectList;
import com.kolich.havalo.entities.types.Repository;
import com.kolich.havalo.entities.types.UserRole;
import com.kolich.havalo.io.managers.RepositoryManager;
import com.kolich.havalo.spring.controllers.HavaloControllerClosure;
import com.kolich.havalo.spring.controllers.api.AbstractHavaloAPIController;

@Controller
@RequestMapping(value="/repository")
public class RepositoryAPI extends AbstractHavaloAPIController {
	
	private static final Logger logger__ =
		LoggerFactory.getLogger(RepositoryAPI.class);
			
	@Autowired
	public RepositoryAPI(RepositoryManager repositoryManager) {
		super(repositoryManager);
	}
	
	@RequestMapping(method={RequestMethod.GET})
	public ResponseEntity<byte[]> list(
		@RequestParam(required=false) final String startsWith,
		final HavaloUUID userId) {
		return new HavaloControllerClosure<ResponseEntity<byte[]>>(
			"GET:/api/repository", logger__) {
			@Override
			public ResponseEntity<byte[]> doit() throws Exception {
				final Repository repo = getRepository(userId);
				return new ReentrantReadWriteEntityLock<ResponseEntity<byte[]>>(repo) {
					@Override
					public ResponseEntity<byte[]> transaction() throws Exception {
						final ObjectList list = repo.startsWith((startsWith != null) ?
							// Only load objects that start with the given
							// prefix, if one was provided.
							startsWith : "");
						return getJsonResponseEntity(list, HttpStatus.OK);
					}
				}.read(false); // Shared read lock on repo, no wait
			}
		}.execute();
	}
	
	@RequestMapping(method={RequestMethod.POST})
	public ResponseEntity<byte[]> create() {
		return new HavaloControllerClosure<ResponseEntity<byte[]>>(
			"POST:/api/repository", logger__) {
			@Override
			public ResponseEntity<byte[]> doit() throws Exception {				
				// Create a new KeyPair; this is a new user access key
				// and access secret.  NOTE: Currently key pair identities
				// always associated with "normal" user roles.  The first
				// admin user is created via the HavaloBootstrap bean on
				// first boot.  Only the first admin user has the rights
				// to call this specific API function.
				final KeyPair kp = new KeyPair(UserRole.USER);
				// Create a base repository for the new access key.  All of
				// the resources associated with this access key will sit
				// under this base repository (some directory on disk).
				createRepository(kp.getIdKey(), kp);
				return getJsonResponseEntity(kp, HttpStatus.CREATED);
			}
		}.execute();
	}
	
	@RequestMapping(method={RequestMethod.DELETE}, value="{repoId}")
	public ResponseEntity<byte[]> delete(@PathVariable final String repoId) {
		return new HavaloControllerClosure<ResponseEntity<byte[]>>(
			"DELETE:/api/repository/" + repoId, logger__) {
			@Override
			public ResponseEntity<byte[]> doit() throws Exception {
				// Attempt to delete the repository, its meta data, and all
				// objects inside of it.
				deleteRepository(new HavaloUUID(repoId));
				return getEmptyResponseEntity(HttpStatus.NO_CONTENT);
			}
		}.execute();
	}
	
}
