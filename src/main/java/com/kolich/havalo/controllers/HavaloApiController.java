/**
 * Copyright (c) 2013 Mark S. Kolich
 * http://mark.koli.ch
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

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
