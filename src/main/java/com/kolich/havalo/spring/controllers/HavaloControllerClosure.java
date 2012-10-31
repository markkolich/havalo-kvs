package com.kolich.havalo.spring.controllers;

import org.slf4j.Logger;

import com.kolich.bolt.exceptions.LockConflictException;
import com.kolich.havalo.exceptions.BadHavaloUUIDException;
import com.kolich.havalo.exceptions.HavaloException;
import com.kolich.havalo.exceptions.InvalidResourceException;
import com.kolich.havalo.exceptions.objects.ObjectConflictException;
import com.kolich.havalo.exceptions.objects.ObjectDeletionException;
import com.kolich.havalo.exceptions.objects.ObjectFlushException;
import com.kolich.havalo.exceptions.objects.ObjectLoadException;
import com.kolich.havalo.exceptions.objects.ObjectNotFoundException;
import com.kolich.havalo.exceptions.repositories.DuplicateRepositoryException;
import com.kolich.havalo.exceptions.repositories.RepositoryCreationException;
import com.kolich.havalo.exceptions.repositories.RepositoryFlushException;
import com.kolich.havalo.exceptions.repositories.RepositoryLoadException;
import com.kolich.havalo.exceptions.repositories.RepositoryNotFoundException;
import com.kolich.spring.controllers.KolichControllerClosure;

public abstract class HavaloControllerClosure<T> extends KolichControllerClosure<T> {

	public HavaloControllerClosure(String comment, Logger logger) {
		super(comment, logger);
	}
	
	@Override
	public T execute() {
		try {
			return doit();
		} catch (IllegalArgumentException e) {
			logger_.debug(comment_, e);
			throw e;
		} catch (DuplicateRepositoryException e) {
			logger_.debug(comment_, e);
			throw e;
		} catch (RepositoryCreationException e) {
			logger_.debug(comment_, e);
			throw e;
		} catch (RepositoryFlushException e) {
			logger_.debug(comment_, e);
			throw e;
		} catch (RepositoryLoadException e) {
			logger_.debug(comment_, e);
			throw e;
		} catch (RepositoryNotFoundException e) {
			logger_.debug(comment_, e);
			throw e;
		} catch (ObjectConflictException e) {
			logger_.debug(comment_, e);
			throw e;
		} catch (ObjectDeletionException e) {
			logger_.debug(comment_, e);
			throw e;
		} catch (ObjectFlushException e) {
			logger_.debug(comment_, e);
			throw e;
		} catch (ObjectLoadException e) {
			logger_.debug(comment_, e);
			throw e;
		} catch (ObjectNotFoundException e) {
			logger_.debug(comment_, e);
			throw e;
		} catch (LockConflictException e) {
			logger_.debug(comment_, e);
			throw e;
		} catch (BadHavaloUUIDException e) {
			logger_.debug(comment_, e);
			throw e;
		} catch (InvalidResourceException e) {
			logger_.debug(comment_, e);
			throw e;
		} catch (Exception e) {
			logger_.debug(comment_, e);
			throw new HavaloException(e);
		}
	}

}
