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
import com.google.gson.annotations.SerializedName;
import com.kolich.havalo.entities.StoreableEntity;
import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Repository extends StoreableEntity implements Serializable {
	
	private static final long serialVersionUID = 8231197578192198012L;
	
	@SerializedName("repo_dir")
	private final File repositoryDir_;
	
	@SerializedName("id")
	private HavaloUUID repoId_;
		
	@SerializedName("key_pair")
	private KeyPair keyPair_;
	
	@SerializedName("objects")
	private final Trie<String, HashedFileObject> objects_;
	
	public Repository(File repositoryDir, HavaloUUID repoId) {
		super((repoId != null) ? repoId.toString() : null);
		repositoryDir_ = repositoryDir;
		repoId_ = repoId;
		objects_ = new PatriciaTrie<>();
	}
	
	// For GSON
	public Repository() {
		this(null, null);
	}
	
	@Override
	public String getKey() {
		checkNotNull(repoId_);
		return repoId_.toString();
	}
	
	public File getFile() {
		return repositoryDir_;
	}
	
	public HavaloUUID getRepoId() {
		return repoId_;
	}
	
	public Repository setRepoId(HavaloUUID repoId) {
		repoId_ = repoId;
		return this;
	}
	
	public KeyPair getKeyPair() {
		return keyPair_;
	}
	
	public Repository setKeyPair(KeyPair keyPair) {
		keyPair_ = keyPair;
		return this;
	}
	
	public Repository addObject(String key, HashedFileObject object) {
		// Explicitly synchronized around the objects Trie such that no
		// internal conflicts occur during GSON serialization (when GSON
		// is serializing this object to JSON).  NOTE: The TrieTypeAdapter
		// is also synchronized on the Trie to be serialized (such that no
		// modifications are allowed to the Trie while it's being serialized).
		// We are _not_ synchronizing around individual operations on the Trie
		// (e.g., a single get, put, remove, etc.) but rather the entire Trie
		// on each operation.
		synchronized(objects_) {
			objects_.put(key, object);
		}
		return this;
	}
	
	public HashedFileObject deleteObject(String key) {
		// Explicitly synchronized around the objects Trie such that no
		// internal conflicts occur during GSON serialization (when GSON
		// is serializing this object to JSON).  NOTE: The TrieTypeAdapter
		// is also synchronized on the Trie to be serialized (such that no
		// modifications are allowed to the Trie while it's being serialized).
		// We are _not_ synchronizing around individual operations on the Trie
		// (e.g., a single get, put, remove, etc.) but rather the entire Trie
		// on each operation.
		synchronized(objects_) {
			return objects_.remove(key);
		}
	}
	
	public void deleteAllObjects() {
		// Explicitly synchronized around the objects Trie such that no
		// internal conflicts occur during GSON serialization (when GSON
		// is serializing this object to JSON).  NOTE: The TrieTypeAdapter
		// is also synchronized on the Trie to be serialized (such that no
		// modifications are allowed to the Trie while it's being serialized).
		// We are _not_ synchronizing around individual operations on the Trie
		// (e.g., a single get, put, remove, etc.) but rather the entire Trie
		// on each operation.
		synchronized(objects_) {
			objects_.clear();
		}
	}
	
	public HashedFileObject getObject(String key) {
		// Explicitly synchronized around the objects Trie such that no
		// internal conflicts occur during GSON serialization (when GSON
		// is serializing this object to JSON).  NOTE: The TrieTypeAdapter
		// is also synchronized on the Trie to be serialized (such that no
		// modifications are allowed to the Trie while it's being serialized).
		// We are _not_ synchronizing around individual operations on the Trie
		// (e.g., a single get, put, remove, etc.) but rather the entire Trie
		// on each operation.
		synchronized(objects_) {
			return objects_.get(key);
		}
	}
	
	public ObjectList startsWith(String prefix) {
		final ObjectList list = new ObjectList();
		// Explicitly synchronized around the objects Trie such that no
		// internal conflicts occur during GSON serialization (when GSON
		// is serializing this object to JSON).  NOTE: The TrieTypeAdapter
		// is also synchronized on the Trie to be serialized (such that no
		// modifications are allowed to the Trie while it's being serialized).
		// We are _not_ synchronizing around individual operations on the Trie
		// (e.g., a single get, put, remove, etc.) but rather the entire Trie
		// on each operation.
		synchronized(objects_) {
			final Map<String, HashedFileObject> startsWith =
				objects_.prefixMap(prefix);
			for(final Map.Entry<String, HashedFileObject> entry :
				startsWith.entrySet()) {
				final HashedFileObject hfo = entry.getValue();
				// Clone the underlying HFO for the return
				list.addObject(new HashedFileObject(
					//hfo.getFile(),
					hfo.getName(),
					hfo.getHeaders()));
			}
		}
		return list;
	}
	
	// Straight from Eclipse
	// Only uses the Repo ID
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((repoId_ == null) ? 0 : repoId_.hashCode());
		return result;
	}

	// Straight from Eclipse
	// Only uses the Repo ID
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Repository other = (Repository) obj;
		if (repoId_ == null) {
			if (other.repoId_ != null)
				return false;
		} else if (!repoId_.equals(other.repoId_))
			return false;
		return true;
	}
	
	public static final class TrieTypeAdapter
		implements JsonSerializer<Trie<String, HashedFileObject>>,
			JsonDeserializer<Trie<String, HashedFileObject>> {
		
		@Override
		public JsonElement serialize(final Trie<String, HashedFileObject> trie,
			final Type typeOfSrc, final JsonSerializationContext context) {
			final JsonObject json = new JsonObject();
			// Explicitly synchronized around the objects Trie such that no
			// internal conflicts occur during GSON serialization (when GSON
			// is serializing this object to JSON).  NOTE: The TrieTypeAdapter
			// is also synchronized on the Trie to be serialized (such that no
			// modifications are allowed to the Trie while it's being serialized).
			// We are _not_ synchronizing around individual operations on the Trie
			// (e.g., a single get, put, remove, etc.) but rather the entire Trie
			// on each operation.
			synchronized(trie) {
				for(final Map.Entry<String, HashedFileObject> entry :
					trie.entrySet()) {
					json.add(entry.getKey(),
						// The HashedFileObject of this entry. 
						context.serialize(entry.getValue(),
							HashedFileObject.class));
				}
			}
			return json;
		}
	
		@Override
		public Trie<String, HashedFileObject> deserialize(final JsonElement json,
			final Type typeOfT, final JsonDeserializationContext context) {
			if(!(json instanceof JsonObject)) {
				throw new JsonParseException("Dood! The Trie to " +
		    		"deserialize should be an object!");
			}
			final Trie<String, HashedFileObject> trie = new PatriciaTrie<>();
			for(final Map.Entry<String, JsonElement> entry :
				json.getAsJsonObject().entrySet()) {
				trie.put(entry.getKey(),
					// The HashedFileObject of this entry.
					context.<HashedFileObject>deserialize(entry.getValue(),
						HashedFileObject.class));
			}
			return trie;
		}
		
	}
	
	public static final class FileTypeAdapter
		implements JsonSerializer<File>, JsonDeserializer<File> {
		
		@Override
		public JsonElement serialize(final File src, final Type typeOfSrc, 
			final JsonSerializationContext context) {
			return new JsonPrimitive(src.getAbsolutePath());
		}
	
		@Override
		public File deserialize(final JsonElement json, final Type typeOfT, 
			final JsonDeserializationContext context) {
			return new File(json.getAsString());
		}
		
	}
	
}
