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

package com.kolich.havalo.spring.controllers.api;

import static com.kolich.havalo.entities.types.HavaloUUID.HAVALO_ADMIN_UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.kolich.havalo.entities.types.Repository;
import com.kolich.havalo.io.managers.RepositoryManager;
import com.kolich.havalo.spring.controllers.HavaloControllerClosure;

@Controller
@RequestMapping(value="api")
public class Api extends AbstractHavaloAPIController {
	
	private static final Logger logger__ = LoggerFactory.getLogger(Api.class);
	
	@Autowired
	public Api(RepositoryManager repositoryManager) {
		super(repositoryManager);
	}
	
	// 1) On startup, "admin repo" is created using empty secret.
	// 2) HavaloUserDetailsService immeaditely rejects all requests where
	//    secret in loaded keypair is empty string ..need right error handling
	//    there too.
	// 3) Admin consumer calls POST:/api to create and set new secret for admin
	//    user.  request is rejected if current secret attached to keypair is
	//    non-empty string.  new keypair is returned to user on call to POST:/api
	//    user should save this and not lose it.
	// 4) implement POST:/api to change initial empty string ("") KP secret
	
	@RequestMapping(method={RequestMethod.GET, RequestMethod.HEAD})
	public ResponseEntity<byte[]> index() {
		return new HavaloControllerClosure<ResponseEntity<byte[]>>(
			"[GET,HEAD]:/", logger__) {
			@Override
			public ResponseEntity<byte[]> doit() throws Exception {
				final Repository repo = getRepository(HAVALO_ADMIN_UUID);
				return getJsonResponseEntity(repo.getKeyPair(), HttpStatus.OK);
			}
		}.execute();
	}
	
}
