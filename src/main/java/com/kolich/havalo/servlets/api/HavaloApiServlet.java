package com.kolich.havalo.servlets.api;

import static com.kolich.havalo.HavaloServletContextBootstrap.HAVALO_REPO_MANAGER_ATTRIBUTE;

import javax.servlet.AsyncContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kolich.havalo.entities.HavaloEntity;
import com.kolich.havalo.entities.types.DiskObject;
import com.kolich.havalo.entities.types.HashedFileObject;
import com.kolich.havalo.entities.types.HavaloUUID;
import com.kolich.havalo.entities.types.KeyPair;
import com.kolich.havalo.entities.types.Repository;
import com.kolich.havalo.exceptions.MethodNotNotSupportedException;
import com.kolich.havalo.io.managers.RepositoryManager;
import com.kolich.havalo.servlets.HavaloServlet;
import com.kolich.havalo.servlets.HavaloServletClosure;

public abstract class HavaloApiServlet extends HavaloServlet {

	private static final long serialVersionUID = -7154044213558472481L;
	
	private static final Logger logger__ =
		LoggerFactory.getLogger(HavaloApiServlet.class);
	
	protected RepositoryManager repositoryManager_;
	
	@Override
	public void init(final ServletConfig config) throws ServletException {
		super.init(config);
		repositoryManager_ = (RepositoryManager)config.getServletContext()
			.getAttribute(HAVALO_REPO_MANAGER_ATTRIBUTE);
	}
					
	@Override
	public <T extends HavaloEntity> HavaloServletClosure<? extends HavaloEntity>
		trace(final AsyncContext context) {
		return new HavaloApiServletClosure<T>(logger__, context) {
			@Override
			public T doit(final KeyPair userKp) throws Exception {
				throw new MethodNotNotSupportedException();
			}
		};
	}
	
	@Override
	public <T extends HavaloEntity> HavaloServletClosure<? extends HavaloEntity>
		head(final AsyncContext context) {
		return new HavaloApiServletClosure<T>(logger__, context) {
			@Override
			public T doit(final KeyPair userKp) throws Exception {
				throw new MethodNotNotSupportedException();
			}
		};
	}
	
	@Override
	public <T extends HavaloEntity> HavaloServletClosure<? extends HavaloEntity>
		get(final AsyncContext context) {
		return new HavaloApiServletClosure<T>(logger__, context) {
			@Override
			public T doit(final KeyPair userKp) throws Exception {
				throw new MethodNotNotSupportedException();
			}
		};
	}
	
	@Override
	public <T extends HavaloEntity> HavaloServletClosure<? extends HavaloEntity>
		post(final AsyncContext context) {
		return new HavaloApiServletClosure<T>(logger__, context) {
			@Override
			public T doit(final KeyPair userKp) throws Exception {
				throw new MethodNotNotSupportedException();
			}
		};
	}
	
	@Override
	public <T extends HavaloEntity> HavaloServletClosure<? extends HavaloEntity>
		put(final AsyncContext context) {
		return new HavaloApiServletClosure<T>(logger__, context) {
			@Override
			public T doit(final KeyPair userKp) throws Exception {
				throw new MethodNotNotSupportedException();
			}
		};
	}
	
	@Override
	public <T extends HavaloEntity> HavaloServletClosure<? extends HavaloEntity>
		delete(final AsyncContext context) {
		return new HavaloApiServletClosure<T>(logger__, context) {
			@Override
			public T doit(final KeyPair userKp) throws Exception {
				throw new MethodNotNotSupportedException();
			}
		};
	}
	
	protected final Repository createRepository(final HavaloUUID id,
		final KeyPair keyPair) {
		return repositoryManager_.createRepository(id, keyPair);
	}
	
	protected final void deleteRepository(final HavaloUUID id) {
		repositoryManager_.deleteRepository(id);
	}
	
	protected final Repository getRepository(final HavaloUUID userId) {
		return repositoryManager_.getRepository(userId);
	}
	
	protected final HashedFileObject getHashedFileObject(final Repository repo,
		final String key, final boolean failIfNotFound) {
		return repositoryManager_.getHashedFileObject(repo, key, failIfNotFound);
	}
	
	protected final HashedFileObject getHashedFileObject(final Repository repo,
		final String key) {
		return getHashedFileObject(repo, key, false);
	}
	
	protected final DiskObject getCanonicalObject(final Repository repo,
		final HashedFileObject hfo, final boolean makeParentDirs) {
		return repositoryManager_.getCanonicalObject(repo, hfo, makeParentDirs);
	}
	
	protected final DiskObject getCanonicalObject(final Repository repo,
		final HashedFileObject hfo) {
		return getCanonicalObject(repo, hfo, false);
	}
	
	protected final HashedFileObject deleteHashedFileObject(final Repository repo,
		final String key, final String ifMatch) {
		return repositoryManager_.deleteHashedFileObject(repo, key, ifMatch);
	}
	
	protected final HashedFileObject deleteHashedFileObject(final HavaloUUID id,
		final String key, final String ifMatch) {
		return repositoryManager_.deleteHashedFileObject(id, key, ifMatch);
	}
	
	protected final void flushRepository(final Repository repo) {
		repositoryManager_.flushRepository(repo);
	}
	
}
