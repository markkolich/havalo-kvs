package com.kolich.havalo.servlets.api.handlers;

import javax.servlet.AsyncContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kolich.havalo.entities.types.HavaloUUID;
import com.kolich.havalo.entities.types.KeyPair;
import com.kolich.havalo.entities.types.Repository;
import com.kolich.havalo.servlets.api.HavaloApiServlet;
import com.kolich.havalo.servlets.api.HavaloApiServletClosure;

public final class AuthenticateApi extends HavaloApiServlet {
	
	private static final Logger logger__ =
		LoggerFactory.getLogger(AuthenticateApi.class);

	private static final long serialVersionUID = 1087288709731427991L;
	
	@Override
	public final HavaloApiServletClosure<KeyPair> post(final AsyncContext context) {
		return new HavaloApiServletClosure<KeyPair>(logger__, context) {
			@Override
			public KeyPair execute(final HavaloUUID userId) throws Exception {
				final Repository repo = getRepository(userId);
				return repo.getKeyPair();
			}
		};
	}
	
}
