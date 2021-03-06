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

package com.kolich.havalo.entities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.kolich.common.entities.gson.KolichDefaultDateTypeAdapter;
import curacao.gson.GsonAppendableCuracaoEntity;
import com.kolich.havalo.entities.types.HashedFileObject;
import com.kolich.havalo.entities.types.HavaloError;
import com.kolich.havalo.entities.types.HavaloUUID;
import com.kolich.havalo.entities.types.Repository;
import org.apache.commons.collections4.Trie;
import org.slf4j.Logger;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.kolich.common.date.ISO8601DateFormat.getPrimaryFormat;
import static java.util.TimeZone.getTimeZone;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Any entity should extend this abstract class, {@link HavaloEntity}.
 */
public abstract class HavaloEntity extends GsonAppendableCuracaoEntity {
	
	private static final Logger logger__ = getLogger(HavaloEntity.class);

	private static final DateFormat iso8601Format__;
	static {
		iso8601Format__ = new SimpleDateFormat(getPrimaryFormat());
		iso8601Format__.setTimeZone(getTimeZone("GMT"));
	}
	
	public HavaloEntity() {
		super(getHavaloGsonInstance());
	}
	
	/**
	 * Get a new {@link GsonBuilder} instance, configured accordingly.
	 * @return
	 */
	public static final GsonBuilder getHavaloGsonBuilder() {
		final GsonBuilder builder = new GsonBuilder().serializeNulls();
		// Register a type adapter for the HavaloUUID entity type.
		builder.registerTypeAdapter(new TypeToken<HavaloUUID>(){}.getType(),
			new HavaloUUID.HavaloUUIDTypeAdapter());
		builder.registerTypeAdapter(new TypeToken<Date>(){}.getType(),
			new KolichDefaultDateTypeAdapter(iso8601Format__));
		builder.registerTypeAdapter(new TypeToken<File>(){}.getType(),
			new Repository.FileTypeAdapter());
		builder.registerTypeAdapter(new TypeToken<Trie<String, HashedFileObject>>(){}.getType(),
			new Repository.TrieTypeAdapter());
		builder.registerTypeAdapter(new TypeToken<Exception>(){}.getType(), 
			new HavaloError.ExceptionTypeAdapter());
		return builder;
	}
	
	public static final Gson getHavaloGsonInstance() {
		return getHavaloGsonBuilder().create();
	}
	
	@Override
	public int getStatus() {
		return SC_OK;
	}
	
}
