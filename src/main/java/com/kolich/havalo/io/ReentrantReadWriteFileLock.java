/**
 * Copyright (c) 2012 Mark S. Kolich
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

package com.kolich.havalo.io;

import java.util.concurrent.locks.Lock;

import com.kolich.havalo.entities.HavaloEntity;
import com.kolich.havalo.entities.HavaloFileEntity;
import com.kolich.havalo.exceptions.locks.LockConflictException;

public abstract class ReentrantReadWriteFileLock<T extends HavaloEntity> {
			
	private final HavaloFileEntity fileEntity_;
	
	public ReentrantReadWriteFileLock(HavaloFileEntity fileEntity) {
		fileEntity_ = fileEntity;
	}
	
	public T read(final boolean wait) throws Exception {
		return lock(fileEntity_.getLock().readLock(), wait);
    }
	
	public T read() throws Exception {
		return read(true);
	}
	
	public T write(final boolean wait) throws Exception {
		return lock(fileEntity_.getLock().writeLock(), wait);
    }
	
	public T write() throws Exception {
		return write(false);
    }
	
	private T lock(final Lock lock, final boolean wait) throws Exception {
		// Acquires the lock only if it is free at the time of invocation.
		// Acquires the lock if it is available and returns immediately with
		// the value true. If the lock is not available then this method will
		// return immediately with the value false.
		if(!wait && lock.tryLock()) {
			return doit(lock);
		} else if(wait) {
			// If the lock is not available then the current thread
			// becomes disabled for thread scheduling purposes and lies
			// dormant until the lock has been acquired.
			lock.lock();
			return doit(lock);
		} else {
			throw new LockConflictException("Failed to obtain lock on file " +
				"entity (name=" + fileEntity_.getName() + ")");
		}
	}
	
	private T doit(final Lock lock) throws Exception {
		try {
        	final T t = transaction();
        	// Only called on success. If the transaction failed, the
        	// callback here is never fired given that an Exception is
        	// thrown.
        	success(t);
        	return t;
        } finally {
            lock.unlock();
        }
	}
	
	public abstract T transaction() throws Exception;
	
	/**
	 * Success callback, called only on a successful completion
	 * of a transaction.  Default success operation is do nothing.
	 * @param t
	 * @throws Exception
	 */
	public void success(final T t) throws Exception {
		// Default, nothing.
	}

}
