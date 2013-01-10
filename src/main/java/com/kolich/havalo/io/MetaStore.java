package com.kolich.havalo.io;

import java.io.Reader;

import com.kolich.havalo.entities.StoreableEntity;

public interface MetaStore {
		
	public Reader getReader(final String index);
	
	public void save(final StoreableEntity entity);
	
	public void delete(final String index);

}
