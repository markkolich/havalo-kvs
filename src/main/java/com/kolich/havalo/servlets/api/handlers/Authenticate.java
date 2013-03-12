package com.kolich.havalo.servlets.api.handlers;

import javax.servlet.AsyncContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kolich.havalo.entities.types.KeyPair;
import com.kolich.havalo.entities.types.Repository;
import com.kolich.havalo.servlets.api.HavaloApiServlet;
import com.kolich.havalo.servlets.api.HavaloApiServletClosure;

public final class Authenticate extends HavaloApiServlet {
	
	private static final Logger logger__ =
		LoggerFactory.getLogger(Authenticate.class);

	private static final long serialVersionUID = 1087288709731427991L;
	
	@Override
	public final HavaloApiServletClosure<KeyPair> post(final AsyncContext context) {
		return new HavaloApiServletClosure<KeyPair>(logger__, context) {
			@Override
			public KeyPair doit(final KeyPair userKp) throws Exception {
				final Repository repo = getRepository(userKp.getIdKey());
				return repo.getKeyPair();
			}
		};
	}
	
}
