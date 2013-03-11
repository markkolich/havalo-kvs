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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.kolich.havalo.entities.HavaloEntity;
import com.kolich.havalo.exceptions.MethodNotNotAllowedException;
import com.typesafe.config.Config;

public abstract class HavaloApiServlet extends HttpServlet {

	private static final long serialVersionUID = 8388599956708926598L;
	
	private static final Logger logger__ =
		LoggerFactory.getLogger(HavaloApiServlet.class);
	
	private Config config_;
	
	private ExecutorService pool_;
	
	@Override
	public final void init(final ServletConfig config) throws ServletException {
		final ServletContext context = config.getServletContext();		
		config_ = (Config)context.getAttribute(HAVALO_CONFIG_ATTRIBUTE);
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
		
	}
		
	@Override
	public final void doHead(final HttpServletRequest request,
		final HttpServletResponse response) {
		final AsyncContext context = request.startAsync(request, response);
		
	}
	
	@Override
	public final void doGet(final HttpServletRequest request,
		final HttpServletResponse response) {
		final AsyncContext context = request.startAsync(request, response);
		/*
		context.addListener(new AsyncListener() {
			@Override
			public void onStartAsync(final AsyncEvent event) throws IOException {
				
			}
			@Override
			public void onComplete(final AsyncEvent event) throws IOException {
				
			}
			@Override
			public void onError(final AsyncEvent event) throws IOException {
				
			}
			@Override
			public void onTimeout(final AsyncEvent event) throws IOException {
				
			}
		});
		*/
		pool_.submit(get(context));
	}
	
	public <T extends HavaloEntity> HavaloApiServletClosure<? extends HavaloEntity> get(
		final AsyncContext context) {
		return new HavaloApiServletClosure<T>(logger__, context) {
			@Override
			public T doit() throws Exception {
				throw new MethodNotNotAllowedException();
			}
		};
	}
	
	@Override
	public final void doPost(final HttpServletRequest request,
		final HttpServletResponse response) {
		final AsyncContext context = request.startAsync(request, response);
		
	}
	
	@Override
	public final void doPut(final HttpServletRequest request,
		final HttpServletResponse response) {
		final AsyncContext context = request.startAsync(request, response);
		
	}
	
	@Override
	public final void doDelete(final HttpServletRequest request,
		final HttpServletResponse response) {
		final AsyncContext context = request.startAsync(request, response);
		
	}
	
}
