package com.kolich.havalo.controllers;

import com.kolich.havalo.entities.types.*;
import com.kolich.havalo.io.managers.RepositoryManager;

public abstract class HavaloApiController {

    protected final RepositoryManager repositoryManager_;

    public HavaloApiController(final RepositoryManager repositoryManager) {
        repositoryManager_ = repositoryManager;
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
