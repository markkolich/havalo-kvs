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

package com.kolich.havalo.entities.types;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.kolich.common.util.crypt.Base64Utils.encodeBase64URLSafe;
import static com.kolich.havalo.entities.types.UserRole.ADMIN;
import static org.apache.commons.codec.binary.StringUtils.newStringUtf8;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.annotations.SerializedName;
import com.kolich.common.util.secure.KolichSecureRandom;
import com.kolich.havalo.entities.HavaloEntity;

public final class KeyPair extends HavaloEntity implements Serializable {
	
	private static final long serialVersionUID = -844726755873142920L;

	private static final KolichSecureRandom random__ = new KolichSecureRandom();
	
	@SerializedName("key")
	private HavaloUUID idKey_;
	
	@SerializedName("secret")
	private String secret_;
		
	@SerializedName("roles")
	private List<UserRole> roles_;
	
	public KeyPair(HavaloUUID idKey, String secret, List<UserRole> roles) {
		idKey_ = idKey;
		secret_ = secret;
		roles_ = roles;
	}
	
	public KeyPair(HavaloUUID idKey, List<UserRole> roles) {
		this(idKey, generateRandomSecret(), roles);
	}
	
	public KeyPair(List<UserRole> roles) {
		this(new HavaloUUID(), roles);
	}
	
	// For GSON
	public KeyPair() {
		this(Arrays.asList(new UserRole[]{UserRole.USER}));
	}
		
	public HavaloUUID getKey() {
		return idKey_;
	}
	
	public KeyPair setKey(HavaloUUID id) {
		idKey_ = id;
		return this;
	}

	public String getSecret() {
		return secret_;
	}

	public KeyPair setSecret(String secret) {
		secret_ = secret;
		return this;
	}
		
	public List<UserRole> getRoles() {
		return new ArrayList<UserRole>(roles_);
	}
		
	public KeyPair setRoles(List<UserRole> roles) {
		roles_ = roles;
		return this;
	}
	
	public boolean isAdmin() {
		checkNotNull(roles_, "Checking for admin status, role " +
			"list cannot be null.");
		return roles_.contains(ADMIN);
	}
	
	private static final String generateRandomSecret() {
		return newStringUtf8(encodeBase64URLSafe(random__.getRandom()));
	}

	// Straight from Eclipse
	// Only compares the ID and secret fields.
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((idKey_ == null) ? 0 : idKey_.hashCode());
		result = prime * result + ((secret_ == null) ? 0 : secret_.hashCode());
		return result;
	}

	// Straight from Eclipse
	// Only compares the ID and secret fields.
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		KeyPair other = (KeyPair) obj;
		if (idKey_ == null) {
			if (other.idKey_ != null)
				return false;
		} else if (!idKey_.equals(other.idKey_))
			return false;
		if (secret_ == null) {
			if (other.secret_ != null)
				return false;
		} else if (!secret_.equals(other.secret_))
			return false;
		return true;
	}
	
}
