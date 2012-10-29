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

package com.kolich.havalo.entities;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.gson.annotations.SerializedName;
import com.kolich.bolt.LockableEntity;

public abstract class HavaloFileEntity extends HavaloEntity
	implements LockableEntity, Comparable<HavaloFileEntity> {
	
	protected transient final ReadWriteLock lock_;
		
	@SerializedName("name")
	protected String name_;
		
	public HavaloFileEntity(final String name) {
		name_ = name;
		// Non-fair mode (default)
		// When constructed as non-fair (the default), the order of entry
		// to the read and write lock is unspecified, subject to reentrancy
		// constraints. A nonfair lock that is continously contended may
		// indefinitely postpone one or more reader or writer threads, but
		// will normally have higher throughput than a fair lock.
		lock_ = new ReentrantReadWriteLock();
	}
	
	public HavaloFileEntity() {
		this(null);
	}
		
	@Override
	public ReadWriteLock getLock() {
		return lock_;
	}
	
	public String getName() {
		return name_;
	}
	
	public HavaloFileEntity setName(String name) {
		name_ = name;
		return this;
	}
	
	@Override
	public int compareTo(HavaloFileEntity o) {
		return name_.compareTo(o.getName());
	}
	
}
