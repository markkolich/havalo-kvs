package com.kolich.havalo.servlets;

import static com.kolich.havalo.HavaloServletContextBootstrap.HAVALO_CONFIG_ATTRIBUTE;

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
	
	private long asyncTimeout_;
	
	@Override
	public void init(final ServletConfig config) throws ServletException {
		final ServletContext context = config.getServletContext();		
		config_ = (Config)context.getAttribute(HAVALO_CONFIG_ATTRIBUTE);
		asyncTimeout_ = config_.getLong("havalo.api.request.timeout");
		pool_ = Executors.newCachedThreadPool(
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
		final AsyncContext context = request.startAsync(request, response);
		context.setTimeout(asyncTimeout_);
		pool_.submit(trace(context));
	}
			
	@Override
	public final void doHead(final HttpServletRequest request,
		final HttpServletResponse response) {
		final AsyncContext context = request.startAsync(request, response);
		context.setTimeout(asyncTimeout_);
		pool_.submit(head(context));
	}
		
	@Override
	public final void doGet(final HttpServletRequest request,
		final HttpServletResponse response) {
		final AsyncContext context = request.startAsync(request, response);
		context.setTimeout(asyncTimeout_);
		pool_.submit(get(context));
	}
		
	@Override
	public final void doPost(final HttpServletRequest request,
		final HttpServletResponse response) {
		final AsyncContext context = request.startAsync(request, response);
		context.setTimeout(asyncTimeout_);
		pool_.submit(post(context));
	}
		
	@Override
	public final void doPut(final HttpServletRequest request,
		final HttpServletResponse response) {
		final AsyncContext context = request.startAsync(request, response);
		context.setTimeout(asyncTimeout_);
		pool_.submit(put(context));
	}
		
	@Override
	public final void doDelete(final HttpServletRequest request,
		final HttpServletResponse response) {
		final AsyncContext context = request.startAsync(request, response);
		context.setTimeout(asyncTimeout_);
		pool_.submit(delete(context));
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
