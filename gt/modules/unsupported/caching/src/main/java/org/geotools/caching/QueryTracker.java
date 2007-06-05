package org.geotools.caching;

import org.geotools.data.Query;


public interface QueryTracker {

	public abstract void register(Query q) ;
	
	public abstract Query match(Query q) ;
	
	public abstract void unregister(Query q) ;
	
	public abstract void clear() ;
	
}
