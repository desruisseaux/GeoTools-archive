package org.geotools.data.store;

import java.util.Iterator;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;

/**
 * Decorates a {@link org.geotools.feature.Feature} iterator with one that
 * filters content.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class FilteringIterator implements Iterator {

	/**
	 * Delegate iterator
	 */
	Iterator delegate;
	/**
	 * The Filter
	 */
	Filter filter;
	/**
	 * Next feature
	 */
	SimpleFeature next;
	
	public FilteringIterator( Iterator delegate, Filter filter ) {
		this.delegate = delegate;
		this.filter = filter;
	}
	
	public Iterator getDelegate() {
		return delegate;
	}
	
	public void remove() {
		delegate.remove();
	}

	public boolean hasNext() {
		if ( next != null ) {
			return true;
		}
		
		while( delegate.hasNext() ) {
			SimpleFeature peek = (SimpleFeature) delegate.next();
			if ( filter.evaluate( peek ) ) {
				next = peek;
				break;
			}
		}
		
		return next != null;
	}

	public Object next() {
		SimpleFeature f = next;
		next = null;
		return f;
	}

}
