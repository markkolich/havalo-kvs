package com.kolich.havalo.servlets.api;

import javax.servlet.AsyncContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kolich.common.either.Either;
import com.kolich.common.either.Left;
import com.kolich.havalo.entities.errors.HavaloError;
import com.kolich.havalo.entities.types.KeyPair;
import com.kolich.havalo.servlets.HavaloApiServlet;
import com.kolich.havalo.servlets.HavaloApiServletClosure;

public final class AuthenticateApi extends HavaloApiServlet {
	
	private static final Logger logger__ =
		LoggerFactory.getLogger(AuthenticateApi.class);

	private static final long serialVersionUID = 1087288709731427991L;
	
	@Override
	public final HavaloApiServletClosure<KeyPair,Either<HavaloError,KeyPair>> get(final AsyncContext context) {
		return new HavaloApiServletClosure<KeyPair,Either<HavaloError,KeyPair>>(logger__, context) {
			@Override
			public Either<HavaloError,KeyPair> doit() throws Exception {
				return Left.left(new HavaloError("Yay it kinda works!"));
			}
		};
	}
	
}
