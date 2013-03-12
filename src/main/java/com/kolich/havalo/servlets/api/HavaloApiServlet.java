package com.kolich.havalo.servlets.api;

import javax.servlet.AsyncContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kolich.havalo.entities.HavaloEntity;
import com.kolich.havalo.exceptions.MethodNotNotSupportedException;
import com.kolich.havalo.servlets.HavaloServlet;
import com.kolich.havalo.servlets.HavaloServletClosure;

public abstract class HavaloApiServlet extends HavaloServlet {

	private static final long serialVersionUID = -7154044213558472481L;
	
	private static final Logger logger__ =
		LoggerFactory.getLogger(HavaloApiServlet.class);
				
	@Override
	public <T extends HavaloEntity> HavaloServletClosure<? extends HavaloEntity>
		trace(final AsyncContext context) {
		return new HavaloApiServletClosure<T>(logger__, context) {
			@Override
			public T doit() throws Exception {
				throw new MethodNotNotSupportedException();
			}
		};
	}
	
	@Override
	public <T extends HavaloEntity> HavaloServletClosure<? extends HavaloEntity>
		head(final AsyncContext context) {
		return new HavaloApiServletClosure<T>(logger__, context) {
			@Override
			public T doit() throws Exception {
				throw new MethodNotNotSupportedException();
			}
		};
	}
	
	@Override
	public <T extends HavaloEntity> HavaloServletClosure<? extends HavaloEntity>
		get(final AsyncContext context) {
		return new HavaloApiServletClosure<T>(logger__, context) {
			@Override
			public T doit() throws Exception {
				throw new MethodNotNotSupportedException();
			}
		};
	}
	
	@Override
	public <T extends HavaloEntity> HavaloServletClosure<? extends HavaloEntity>
		post(final AsyncContext context) {
		return new HavaloApiServletClosure<T>(logger__, context) {
			@Override
			public T doit() throws Exception {
				throw new MethodNotNotSupportedException();
			}
		};
	}
	
	@Override
	public <T extends HavaloEntity> HavaloServletClosure<? extends HavaloEntity>
		put(final AsyncContext context) {
		return new HavaloApiServletClosure<T>(logger__, context) {
			@Override
			public T doit() throws Exception {
				throw new MethodNotNotSupportedException();
			}
		};
	}
	
	@Override
	public <T extends HavaloEntity> HavaloServletClosure<? extends HavaloEntity>
		delete(final AsyncContext context) {
		return new HavaloApiServletClosure<T>(logger__, context) {
			@Override
			public T doit() throws Exception {
				throw new MethodNotNotSupportedException();
			}
		};
	}
	
}
