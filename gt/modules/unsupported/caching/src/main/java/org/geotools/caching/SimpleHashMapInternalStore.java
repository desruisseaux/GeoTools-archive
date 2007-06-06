package org.geotools.caching;

import java.util.Collection;
import java.util.HashMap;

import org.geotools.feature.Feature;

public class SimpleHashMapInternalStore implements InternalStore {

	private final HashMap buffer = new HashMap() ;

	public void clear() {
		buffer.clear() ;
	}

	public boolean contains(final Feature f) {
		return contains(f.getID()) ;
	}

	public Feature get(final String featureId) {
		return (Feature) buffer.get(featureId) ;
	}

	public Collection getAll() {
		return buffer.values() ;
	}

	public void put(final Feature f) {
		buffer.put(f.getID(), f) ;
	}

	public void remove(final String featureId) {
		buffer.remove(featureId) ;
	}

    /* (non-Javadoc)
     * @see org.geotools.caching.InternalStore#contains(java.lang.String)
     */
    public boolean contains(String featureId) {
        return buffer.containsKey(featureId) ;
    }

}
