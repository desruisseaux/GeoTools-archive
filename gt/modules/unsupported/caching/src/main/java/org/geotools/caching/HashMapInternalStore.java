package org.geotools.caching;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import org.geotools.feature.Feature;

public class HashMapInternalStore implements InternalStore {
	
	private final InternalStore overflow ;
	private final int capacity ;
	private final HashMap buffer ;
	private int count = 0 ;
	private final Random rand = new Random() ;
	
	public HashMapInternalStore(int capacity, InternalStore overflow) {
		this.overflow = overflow ;
		this.capacity = capacity ;
		this.buffer = new HashMap() ;
	}

	public void clear() {
		buffer.clear() ;
		count = 0 ;
	}

	public boolean contains(final Feature f) {
		return buffer.containsKey(f.getID()) ;
	}

	public Feature get(final String featureId) {
		// TODO Auto-generated method stub
		Feature ret = null;
		if (buffer.containsKey(featureId)) {
			ret = (Feature) buffer.get(featureId) ;
		} else {
			if (overflow != null) {
				ret = overflow.get(featureId) ;
				if (ret != null) {
					put(ret) ;
				}
			}
		}
		return ret ;
	}
	
	public Collection getAll() {
		return buffer.values() ;
	}

	public void put(final Feature f) {
		//assert capacity > count ;
		if (count == capacity) {
			evict() ;
		}
		buffer.put(f.getID(), f) ;
		count++ ;
	}
	
	public void remove(final String featureId) {
		buffer.remove(featureId) ;
		count-- ;
	}
	
	protected void evict() {
		int entry = rand.nextInt(buffer.size()) ;
		Iterator it = buffer.keySet().iterator() ;
		for (int i = 0; i < (entry-1) ; i++) {
			it.next() ;
		}
		String id = (String) it.next() ;
		if (overflow != null) {
			overflow.put(get(id)) ;
		}
		remove(id) ;
	}
	
	class Entry {
		
		Feature f ;
		short state = 1 ;
		static final short DIRTY = 0 ;
		static final short FROM_SOURCE = 1 ;
		static final short FROM_CACHE = 2 ;
		
		public Entry(Feature f) {
			this.f = f ;
		}
		
	}

}
