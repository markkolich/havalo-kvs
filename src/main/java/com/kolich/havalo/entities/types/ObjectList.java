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

package com.kolich.havalo.entities.types;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

import com.google.gson.annotations.SerializedName;
import com.kolich.havalo.entities.HavaloEntity;
import com.kolich.havalo.entities.HavaloFileEntity;

public final class ObjectList extends HavaloEntity implements Serializable {

	private static final long serialVersionUID = 1941408068733295138L;
	
	@SerializedName("objects")
	private Set<HavaloFileEntity> objects_;
	
	public ObjectList(Set<HavaloFileEntity> objects) {
		objects_ = objects;
	}
	
	// For GSON
	public ObjectList() {
		this(new TreeSet<HavaloFileEntity>());
	}
		
	public ObjectList addObject(HavaloFileEntity object) {
		synchronized(objects_) {
			objects_.add(object);
		}
		return this;
	}

	// Straight from Eclipse
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((objects_ == null) ? 0 : objects_.hashCode());
		return result;
	}

	// Straight from Eclipse
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ObjectList other = (ObjectList) obj;
		if (objects_ == null) {
			if (other.objects_ != null)
				return false;
		} else if (!objects_.equals(other.objects_))
			return false;
		return true;
	}

}
