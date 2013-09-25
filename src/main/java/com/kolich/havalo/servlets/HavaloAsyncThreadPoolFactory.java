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

import static com.kolich.havalo.HavaloConfigurationFactory.getMaxConcurrentRequests;
import static java.lang.Thread.MAX_PRIORITY;

import java.util.concurrent.ExecutorService;

import com.kolich.servlet.util.AsyncServletThreadPoolFactory;

public final class HavaloAsyncThreadPoolFactory {
	
	// Singleton.
	// Note that this isn't a "singleton per JVM", rather it's a singleton
	// per invocation of the web-application in the Servlet container.  In
	// other words, when the container starts, a new/fresh instance of this
	// class is created to which the "pool" here is consumed.  When you stop
	// the web-application but leave the container running, then hot deploy
	// the application again, you will get a "new" instance of this class
	// and hence an entirely new thread pool.  Even though we're initializing
	// the pool here in a static context (via a static initializer) a new
	// instance of this thread pool factory will ultimately mean a new
	// instance of the underlying pool that this factory constructs.
	private static final ExecutorService pool__;
	static {
		final int maxConcurrentRequests = getMaxConcurrentRequests();
		pool__ = new AsyncServletThreadPoolFactory(maxConcurrentRequests)
			.setDaemon(true)
			.setThreadNameFormat("havalo-async-servlet-%d")
			.setPriority(MAX_PRIORITY)
			.build();
	}
	
	// Cannot instantiate.
	private HavaloAsyncThreadPoolFactory() { }
	
	/**
	 * Returns "the thread pool instance" singleton which manages a pool
	 * of threads that are tasked with handling incoming requests into
	 * the Havalo service via the Servlet container. 
	 */
	public static synchronized final ExecutorService getPoolInstance() {
		return pool__;
	}

}
