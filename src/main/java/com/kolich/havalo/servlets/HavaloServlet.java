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

import static com.kolich.havalo.HavaloServletContext.HAVALO_API_MAX_CONCURRENT_REQUESTS_PROPERTY;
import static com.kolich.havalo.HavaloServletContext.HAVALO_API_REQUEST_TIMEOUT_PROPERTY;
import static com.kolich.havalo.HavaloServletContext.HAVALO_CONTEXT_CONFIG_ATTRIBUTE;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.AsyncContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.kolich.havalo.entities.HavaloEntity;
import com.typesafe.config.Config;

public abstract class HavaloServlet extends HttpServlet {

	private static final long serialVersionUID = 8388599956708926598L;
			
	private Config config_;
	private ExecutorService pool_;
	
	private int maxConcurrentRequests_;
	private long asyncTimeout_;
	
	@Override
	public void init(final ServletConfig config) throws ServletException {
		final ServletContext context = config.getServletContext();
		config_ = (Config)context.getAttribute(HAVALO_CONTEXT_CONFIG_ATTRIBUTE);
		maxConcurrentRequests_ = config_.getInt(HAVALO_API_MAX_CONCURRENT_REQUESTS_PROPERTY);
		asyncTimeout_ = config_.getLong(HAVALO_API_REQUEST_TIMEOUT_PROPERTY);
		// Creates a thread pool that creates new threads as needed, but will
		// reuse previously constructed threads when they are available.
		// Uses the provided ThreadFactory to create new threads as needed.
		pool_ = Executors.newFixedThreadPool(
			// Only support N-concurrent requests.
			maxConcurrentRequests_,
			// Use a thread build to create new threads in the pool.
			new ThreadFactoryBuilder()
				.setDaemon(true)
				.setPriority(Thread.MAX_PRIORITY)
				.setNameFormat("havalo-async-servlet-%d")
				.build());
	}
	
	protected final Config getAppConfig() {
		return config_;
	}
		
	@Override
	public final void doTrace(final HttpServletRequest request,
		final HttpServletResponse response) {
		pool_.submit(trace(doAsync(request, response)));
	}
			
	@Override
	public final void doHead(final HttpServletRequest request,
		final HttpServletResponse response) {
		pool_.submit(head(doAsync(request, response)));
	}
		
	@Override
	public final void doGet(final HttpServletRequest request,
		final HttpServletResponse response) {
		pool_.submit(get(doAsync(request, response)));
	}
		
	@Override
	public final void doPost(final HttpServletRequest request,
		final HttpServletResponse response) {
		pool_.submit(post(doAsync(request, response)));
	}
		
	@Override
	public final void doPut(final HttpServletRequest request,
		final HttpServletResponse response) {
		pool_.submit(put(doAsync(request, response)));
	}
		
	@Override
	public final void doDelete(final HttpServletRequest request,
		final HttpServletResponse response) {		
		pool_.submit(delete(doAsync(request, response)));
	}
	
	private final AsyncContext doAsync(final HttpServletRequest request,
		final HttpServletResponse response) {
		final AsyncContext context = request.startAsync(request, response);
		context.setTimeout(asyncTimeout_);
		return context;
	}
	
	public abstract <T extends HavaloEntity> HavaloServletClosure
		<? extends HavaloEntity> trace(final AsyncContext context);
	
	public abstract <T extends HavaloEntity> HavaloServletClosure
		<? extends HavaloEntity> head(final AsyncContext context);
	
	public abstract <T extends HavaloEntity> HavaloServletClosure
		<? extends HavaloEntity> get(final AsyncContext context);
	
	public abstract <T extends HavaloEntity> HavaloServletClosure
		<? extends HavaloEntity> post(final AsyncContext context);
	
	public abstract <T extends HavaloEntity> HavaloServletClosure
		<? extends HavaloEntity> put(final AsyncContext context);
	
	public abstract <T extends HavaloEntity> HavaloServletClosure
		<? extends HavaloEntity> delete(final AsyncContext context);
	
}
