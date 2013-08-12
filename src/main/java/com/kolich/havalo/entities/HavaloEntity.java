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

package com.kolich.havalo.entities;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.net.MediaType.JSON_UTF_8;
import static com.kolich.common.date.ISO8601DateFormat.getPrimaryFormat;
import static java.util.TimeZone.getTimeZone;
import static javax.servlet.http.HttpServletResponse.SC_OK;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.ardverk.collection.Trie;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.kolich.common.entities.gson.KolichDefaultDateTypeAdapter;
import com.kolich.havalo.entities.types.HashedFileObject;
import com.kolich.havalo.entities.types.HavaloError;
import com.kolich.havalo.entities.types.HavaloUUID;
import com.kolich.havalo.entities.types.Repository;
import com.kolich.servlet.entities.common.KolichCommonAppendableServletClosureEntity;

/**
 * Any entity should extend this abstract class, {@link HavaloEntity}.
 */
public abstract class HavaloEntity
	extends KolichCommonAppendableServletClosureEntity {
	
	private static final String JSON_UTF_8_TYPE = JSON_UTF_8.toString();
	
	private static final DateFormat iso8601Format__;
	static {
		iso8601Format__ = new SimpleDateFormat(getPrimaryFormat());
		iso8601Format__.setTimeZone(getTimeZone("GMT"));
	}
				
	/**
	 * Get a new {@link GsonBuilder} instance, configured accordingly.
	 * @return
	 */
	public static final GsonBuilder getHavaloGsonBuilder() {
		final GsonBuilder builderBuilder = getDefaultGsonBuilder();
		// Register a type adapter for the HavaloUUID entity type.
		builderBuilder.registerTypeAdapter(new TypeToken<HavaloUUID>(){}.getType(),
			new HavaloUUID.HavaloUUIDTypeAdapter());
		builderBuilder.registerTypeAdapter(new TypeToken<Date>(){}.getType(),
			new KolichDefaultDateTypeAdapter(iso8601Format__));
		builderBuilder.registerTypeAdapter(new TypeToken<File>(){}.getType(),
			new Repository.FileTypeAdapter());
		builderBuilder.registerTypeAdapter(new TypeToken<Trie<String, HashedFileObject>>(){}.getType(), 
			new Repository.TrieTypeAdapter());
		builderBuilder.registerTypeAdapter(new TypeToken<Exception>(){}.getType(), 
			new HavaloError.ExceptionTypeAdapter());
		return builderBuilder;
	}
	
	public static final Gson getHavaloGsonInstance() {
		return getHavaloGsonBuilder().create();
	}
	
	/**
	 * Serialize this entity to the provided {@link Appendable} writer.
	 * @param writer
	 */
	@Override
	public final void toWriter(final Appendable writer) {
		checkNotNull(writer, "Cannot serialize entity using a null writer.");
		getHavaloGsonInstance().toJson(this, writer);
	}
	
	@Override
	public int getStatus() {
		return SC_OK;
	}
	
	@Override
	public String getContentType() {
		return JSON_UTF_8_TYPE;
	}
	
	/**
	 * Serialize this entity into a String; default behavior
	 * is usually to first to convert the entity into a {@link Gson}
	 * object then serializes that Gson object into a String.  Basically
	 * this method causes the entity to return a JSON serialized
	 * representation of itself.
	 */
	@Override
	public final String toString() {
		return getHavaloGsonInstance().toJson(this);
	}
			
}
