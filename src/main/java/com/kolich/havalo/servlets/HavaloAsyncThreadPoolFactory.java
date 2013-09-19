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

package com.kolich.havalo.servlets;

import static com.kolich.havalo.HavaloConfigurationFactory.HAVALO_MAX_CONCURRENT_REQUESTS_PROPERTY;
import static com.kolich.havalo.HavaloConfigurationFactory.getConfigInstance;
import static java.lang.Thread.MAX_PRIORITY;

import java.util.concurrent.ExecutorService;

import com.kolich.servlet.util.AsyncServletThreadPoolFactory;
import com.typesafe.config.Config;

public final class HavaloAsyncThreadPoolFactory {
	
	private static final Config havaloConfig__ = getConfigInstance();
	
	private static ExecutorService pool__ = null;
	
	// Cannot instantiate.
	private HavaloAsyncThreadPoolFactory() { }
	
	public static synchronized final ExecutorService getPoolInstance() {
		if(pool__ == null) {
			final int maxConcurrentRequests = havaloConfig__.getInt(
				HAVALO_MAX_CONCURRENT_REQUESTS_PROPERTY);
			pool__ = new AsyncServletThreadPoolFactory(maxConcurrentRequests)
				.setDaemon(true)
				.setThreadNameFormat("havalo-async-servlet-%d")
				.setPriority(MAX_PRIORITY)
				.build();
		}
		return pool__;
	}

}