package com.kolich.havalo.servlets.api;

import javax.servlet.AsyncContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kolich.common.either.Either;
import com.kolich.havalo.entities.HavaloEntity;
import com.kolich.havalo.entities.errors.HavaloError;
import com.kolich.havalo.exceptions.MethodNotNotSupportedException;
import com.kolich.havalo.servlets.HavaloServlet;

public abstract class HavaloApiServlet extends HavaloServlet {

	private static final long serialVersionUID = -7154044213558472481L;
	
	private static final Logger logger__ =
		LoggerFactory.getLogger(HavaloApiServlet.class);
				
	@Override
	public <T extends HavaloEntity> HavaloApiServletClosure<? extends HavaloEntity,
		? extends Either<HavaloError,T>> trace(final AsyncContext context) {
		return new HavaloApiServletClosure<T, Either<HavaloError,T>>(logger__, context) {
			@Override
			public Either<HavaloError,T> doit() throws Exception {
				throw new MethodNotNotSupportedException();
			}
		};
	}
	
	@Override
	public <T extends HavaloEntity> HavaloApiServletClosure<? extends HavaloEntity,
		? extends Either<HavaloError,T>> head(final AsyncContext context) {
		return new HavaloApiServletClosure<T, Either<HavaloError,T>>(logger__, context) {
			@Override
			public Either<HavaloError,T> doit() throws Exception {
				throw new MethodNotNotSupportedException();
			}
		};
	}
	
	@Override
	public <T extends HavaloEntity> HavaloApiServletClosure<? extends HavaloEntity,
		? extends Either<HavaloError,T>> get(final AsyncContext context) {
		return new HavaloApiServletClosure<T, Either<HavaloError,T>>(logger__, context) {
			@Override
			public Either<HavaloError,T> doit() throws Exception {
				throw new MethodNotNotSupportedException();
			}
		};
	}
	
	@Override
	public <T extends HavaloEntity> HavaloApiServletClosure<? extends HavaloEntity,
		? extends Either<HavaloError,T>> post(final AsyncContext context) {
		return new HavaloApiServletClosure<T, Either<HavaloError,T>>(logger__, context) {
			@Override
			public Either<HavaloError,T> doit() throws Exception {
				throw new MethodNotNotSupportedException();
			}
		};
	}

	@Override
	public <T extends HavaloEntity> HavaloApiServletClosure<? extends HavaloEntity,
		? extends Either<HavaloError,T>> put(final AsyncContext context) {
		return new HavaloApiServletClosure<T, Either<HavaloError,T>>(logger__, context) {
			@Override
			public Either<HavaloError,T> doit() throws Exception {
				throw new MethodNotNotSupportedException();
			}
		};
	}
	
	@Override
	public <T extends HavaloEntity> HavaloApiServletClosure<? extends HavaloEntity,
		? extends Either<HavaloError,T>> delete(final AsyncContext context) {
		return new HavaloApiServletClosure<T, Either<HavaloError,T>>(logger__, context) {
			@Override
			public Either<HavaloError,T> doit() throws Exception {
				throw new MethodNotNotSupportedException();
			}
		};
	}
	
}
