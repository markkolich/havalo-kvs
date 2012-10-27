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

import static org.springframework.http.MediaType.parseMediaType;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;

import com.google.gson.annotations.SerializedName;
import com.kolich.havalo.entities.HavaloFileEntity;

public final class HashedFileObject extends HavaloFileEntity
	implements Serializable {
	
	private static final long serialVersionUID = 7496664023986725650L;
						
	@SerializedName("headers")
	private HttpHeaders headers_;
		
	public HashedFileObject(String name, HttpHeaders headers) {
		super(name);
		headers_ = headers;
	}
	
	public HashedFileObject(String name) {
		this(name, new HttpHeaders());
	}
		
	// For GSON
	public HashedFileObject() {
		this(null, null);
	}
	
	public HashedFileObject setLastModified(Date lastModified) {
		return setLastModified(lastModified.getTime());
	}
	
	public HashedFileObject setLastModified(long lastModified) {
		synchronized(headers_) {
			headers_.setLastModified(lastModified);
		}
		return this;
	}
	
	public String getETag() {
		synchronized(headers_) {
			return headers_.getETag();
		}
	}
	
	public HashedFileObject setETag(String eTag, boolean quote) {
		synchronized(headers_) {
			headers_.setETag((quote) ? String.format("\"%s\"", eTag) : eTag);
		}
		return this;
	}
	
	public HashedFileObject setETag(String eTag) {
		return setETag(eTag, true);
	}
	
	/**
	 * Set the Content-Length of this entity.
	 * @param contentLength
	 * @return
	 */
	public HashedFileObject setContentLength(long contentLength) {
		synchronized(headers_) {
			headers_.setContentLength(contentLength);
		}
		return this;
	}	
	
	/**
	 * Set the Content-Type of this entity.
	 * @param contentLength
	 * @return
	 */
	public HashedFileObject setContentType(String contentType) {
		synchronized(headers_) {
			headers_.setContentType(parseMediaType(contentType));
		}
		return this;
	}
	
	/**
	 * Set the given, single header value under the given name.  If a header
	 * already exists with the given name, the old value is overwritten.
	 * @param headerName
	 * @param headerValue
	 * @return
	 */
	public HashedFileObject setHeader(String headerName, String headerValue) {
		synchronized(headers_) {
			headers_.set(headerName, headerValue);
		}
		return this;
	}
		
	/**
	 * Returns a thread-safe (deep) copy of the underlying {@link HttpHeaders}
	 * object associated with this {@link HashedFileObject} entity.  Any
	 * modifications made to the returned {@link HttpHeaders} will not impact
	 * the {@link HttpHeaders} stored with this entity.
	 * @return
	 */
	public HttpHeaders getHeaders() {
		final HttpHeaders headers = new HttpHeaders();
		synchronized(headers_) {
			for(final Map.Entry<String, List<String>> entry : headers_.entrySet()) {
				final String headerName = entry.getKey();
				for(final String headerValue : entry.getValue()) {
					headers.add(headerName, headerValue);
				}
			}
		}
		return headers;
	}
	
	// Straight from Eclipse
	// Only compares the name
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name_ == null) ? 0 : name_.hashCode());
		return result;
	}

	// Straight from Eclipse
	// Only compares the name
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HashedFileObject other = (HashedFileObject) obj;
		if (name_ == null) {
			if (other.name_ != null)
				return false;
		} else if (!name_.equals(other.name_))
			return false;
		return true;
	}
	
}
