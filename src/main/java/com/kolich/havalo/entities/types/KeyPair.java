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

package com.kolich.havalo.entities.types;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.kolich.common.util.crypt.Base64Utils.encodeBase64URLSafe;
import static org.apache.commons.codec.binary.StringUtils.newStringUtf8;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

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
		
	@SerializedName("role")
	private UserRole role_;
	
	public KeyPair(HavaloUUID idKey, String secret, UserRole role) {
		idKey_ = idKey;
		secret_ = secret;
		role_ = role;
	}
	
	public KeyPair(HavaloUUID idKey, UserRole role) {
		this(idKey, generateRandomSecret(), role);
	}
	
	public KeyPair(UserRole role) {
		this(new HavaloUUID(), role);
	}
	
	// For GSON
	public KeyPair() {
		this(UserRole.USER);
	}
		
	public HavaloUUID getIdKey() {
		return idKey_;
	}
	
	public KeyPair setIdKey(HavaloUUID id) {
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
		
	public UserRole getRole() {
		return role_;
	}
		
	public KeyPair setRole(UserRole role) {
		role_ = role;
		return this;
	}
	
	public List<GrantedAuthority> getAuthorities() {
		checkNotNull(role_);
		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		for(final String r : role_.getRoles()) {
			authorities.add(new SimpleGrantedAuthority(r));
		}
		return authorities;
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
