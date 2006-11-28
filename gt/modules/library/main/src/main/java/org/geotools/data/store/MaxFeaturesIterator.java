package org.geotools.data.store;

import java.util.Iterator;

/**
 * Iterator wrapper which caps the number of returned features;
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class MaxFeaturesIterator implements Iterator {

	Iterator delegate;
	long max;
	long counter;
	
	public MaxFeaturesIterator( Iterator delegate, long max ) {
		this.delegate = delegate;
		this.max = max;
		counter = 0;
	}
	
	public Iterator getDelegate() {
		return delegate;
	}
	
	public void remove() {
		delegate.remove();
	}

	public boolean hasNext() {
		return delegate.hasNext() && counter < max; 
	}

	public Object next() {
		if ( counter++ <= max ) {
			return delegate.next();
		}
		
		return null;
	}

}
