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

import static org.slf4j.LoggerFactory.getLogger;

import javax.servlet.AsyncContext;

import org.slf4j.Logger;

import com.kolich.havalo.entities.types.KeyPair;
import com.kolich.havalo.servlets.HavaloApiServlet;
import com.kolich.havalo.servlets.HavaloAuthenticatingServletClosureHandler;

public final class AuthenticateApi extends HavaloApiServlet {
	
	private static final Logger logger__ =
		getLogger(AuthenticateApi.class);

	private static final long serialVersionUID = 1087288709731427991L;
	
	@Override
	public final HavaloAuthenticatingServletClosureHandler<KeyPair> post(
		final AsyncContext context) {
		return new HavaloAuthenticatingServletClosureHandler<KeyPair>(logger__, context) {
			@Override
			public KeyPair execute(final KeyPair userKp) throws Exception {
				// A bit redundant, but the call to getRepository() here
				// just verifies that the user account exists ~and~ the
				// corresponding repository exists in the system as well.
				return getRepository(userKp.getKey()).getKeyPair();
			}
		};
	}
	
}
