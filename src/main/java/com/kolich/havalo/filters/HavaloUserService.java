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

package com.kolich.havalo.filters;

import com.kolich.havalo.entities.types.HavaloUUID;
import com.kolich.havalo.entities.types.KeyPair;
import com.kolich.havalo.exceptions.authentication.NullorEmptySecretException;
import com.kolich.havalo.exceptions.authentication.UsernameNotFoundException;
import com.kolich.havalo.io.managers.RepositoryManager;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public final class HavaloUserService {
	
	private final RepositoryManager repoManager_;

	public HavaloUserService(final RepositoryManager repoManager) {
		repoManager_ = checkNotNull(repoManager, "Repository manager cannot be null.");
	}
	
	public KeyPair loadKeyPairById(final UUID id) {
		try {
			final KeyPair kp = repoManager_.getRepository(
				new HavaloUUID(id)).getKeyPair();
			if(kp.getSecret() == null) {
				throw new NullorEmptySecretException("Oops, KeyPair secret " +
					"for user (" + id + ") was null or unknown.");
			}
			return kp;
		} catch (Exception e) {
			throw new UsernameNotFoundException("Failed to load required " +
				"user details for ID: " + id, e);
		}
	}
		
}
