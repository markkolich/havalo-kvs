/**
 * Copyright (c) 2015 Mark S. Kolich
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

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;

import java.io.Serializable;
import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.SerializedName;
import com.kolich.havalo.entities.HavaloEntity;

public final class HavaloError extends HavaloEntity implements Serializable {

	private static final long serialVersionUID = -8338248355189878855L;
	
	@SerializedName("status")
	private final int status_;
	
	@SerializedName("message")
	private final String message_;
	
	@SerializedName("cause")
	private final Exception cause_;

	public HavaloError(int status, String message, Exception cause) {
		status_ = status;
		message_ = message;
		cause_ = cause;
	}
	
	public HavaloError(int status, String message) {
		this(status, message, null);
	}
	
	public HavaloError(String message) {
		this(SC_INTERNAL_SERVER_ERROR, message, null);
	}
		
	// For GSON
	public HavaloError() {
		this(null);
	}
	
	@Override
	public int getStatus() {
		return status_;
	}
	
	public String getMessage() {
		return message_;
	}
	
	public Exception getCause() {
		return cause_;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((message_ == null) ? 0 : message_.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HavaloError other = (HavaloError) obj;
		if (message_ == null) {
			if (other.message_ != null)
				return false;
		} else if (!message_.equals(other.message_))
			return false;
		return true;
	}
		
	public static final class ExceptionTypeAdapter
		implements JsonSerializer<Exception>, JsonDeserializer<Exception> {
		
		@Override
		public JsonElement serialize(final Exception src, final Type typeOfSrc, 
			final JsonSerializationContext context) {
			return new JsonPrimitive(getStackTrace(src));
		}
	
		@Override
		public Exception deserialize(final JsonElement json, final Type typeOfT, 
			final JsonDeserializationContext context) {
			return new Exception(json.getAsString());
		}
		
	}
			
}
