package org.geotools.caching;

import java.util.Collection;

import org.geotools.feature.Feature;

public interface InternalStore {

	public abstract boolean contains(Feature f) ;
	
	public abstract void put(Feature f) ;
	
	public abstract Feature get(String featureId) ;
	
	public abstract Collection getAll() ;
	
	public abstract void clear() ;
	
	public abstract void remove(String featureId) ;
	
}
