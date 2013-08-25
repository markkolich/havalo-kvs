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

import static java.lang.Thread.MAX_PRIORITY;
import static java.util.concurrent.Executors.newFixedThreadPool;

import java.util.concurrent.ExecutorService;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

public final class AsyncServletThreadPoolFactory {
	
	private static ExecutorService theInstance__ = null;
	
	// Cannot instantiate.
	private AsyncServletThreadPoolFactory() { }
	
	public static synchronized final ExecutorService getPoolInstance(
		final int maxConcurrentRequests) {
		if(theInstance__ == null) {
			theInstance__ = newFixedThreadPool(
				// Only support N-concurrent requests.
				maxConcurrentRequests,
				// Use a thread build to create new threads in the pool.
				new ThreadFactoryBuilder()
					.setDaemon(true)
					.setPriority(MAX_PRIORITY)
					.setNameFormat("havalo-async-servlet-pool-%d")
					.build());
		}
		return theInstance__;
	}

}