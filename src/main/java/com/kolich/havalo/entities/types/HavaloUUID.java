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

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public final class HavaloUUID implements Comparable<HavaloUUID> {
		
	private final UUID id_;
	
	public HavaloUUID(final UUID id) {
		id_ = checkNotNull(id, "ID cannot be null.");
	}
	
	public HavaloUUID(final String id) {
		id_ = UUID.fromString(id);
	}
	
	// For GSON
	public HavaloUUID() {
		this(UUID.randomUUID());
	}
	
	public UUID getId() {
		return id_;
	}
	
	@Override
	public String toString() {
		return id_.toString();
	}
	
	// Straight from Eclipse
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id_ == null) ? 0 : id_.hashCode());
		return result;
	}
	
	// Straight from Eclipse
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HavaloUUID other = (HavaloUUID) obj;
		if (id_ == null) {
			if (other.id_ != null)
				return false;
		} else if (!id_.equals(other.id_))
			return false;
		return true;
	}
	
	@Override
	public int compareTo(HavaloUUID hid) {
		checkNotNull(id_);
		return id_.compareTo(hid.getId());
	}

	public static final class HavaloUUIDTypeAdapter
		implements JsonSerializer<HavaloUUID>, JsonDeserializer<HavaloUUID> {
		
		@Override
		public JsonElement serialize(final HavaloUUID src, final Type typeOfSrc, 
			final JsonSerializationContext context) {
			return new JsonPrimitive(src.toString());
		}

		@Override
		public HavaloUUID deserialize(final JsonElement json, final Type typeOfT, 
			final JsonDeserializationContext context) {
			return new HavaloUUID(json.getAsString());
		}
		
	}

}
