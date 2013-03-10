package com.kolich.havalo.servlets;

import static com.kolich.havalo.HavaloServletContextBootstrap.HAVALO_CONFIG_ATTRIBUTE;
import static com.kolich.havalo.authentication.HavaloAuthenticationFilter.HAVALO_AUTHENTICATION_ATTRIBUTE;

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

import com.kolich.common.either.Either;
import com.kolich.havalo.entities.HavaloEntity;
import com.kolich.havalo.entities.types.KeyPair;
import com.kolich.havalo.exceptions.HavaloException;
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
		// The size of the authentication thread pool should match the
		// size of the total number of allowed concurrent requests.
		pool_ = Executors.newFixedThreadPool(config_.getInt("havalo.api.max.concurrent.requests"));
	}
		
	protected final Config getAppConfig() {
		return config_;
	}
	
	protected static final KeyPair getUserKeyPair(final HttpServletRequest request) {
		return (KeyPair)request.getAttribute(HAVALO_AUTHENTICATION_ATTRIBUTE);
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
		pool_.submit(new Runnable() {
			@Override
			public void run() {
				try {
					get(context);
				} catch (Exception e) {
					logger__.warn("Uncaught exception, falling back to " +
						"global error handler to render error response.", e);
					// TODO
				} finally {
					context.complete();
				}
			}
		});
	}
	
	public void get(final AsyncContext context) {
		new HavaloApiServletClosure<HavaloException,HavaloEntity>(
			"GET:default", logger__, context) {
			@Override
			public Either<HavaloException,HavaloEntity> doit() throws Exception {
				throw new MethodNotNotAllowedException();
			}
		}.execute();
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
