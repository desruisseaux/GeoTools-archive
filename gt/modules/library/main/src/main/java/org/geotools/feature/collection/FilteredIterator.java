/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
 *    
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.feature.collection;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;

/**
 * Provides an implementation of Iterator that will filter
 * contents using the provided filter.
 * <p>
 * This is a *Generic* iterator not limited to Feature, this
 * will become more interesting as Filter is able to evaulate
 * itself with more things then just Features.
 * </p>
 * <p>
 * This also explains the use of Collection (where you may
 * have expected a FeatureCollection). However
 * <code>FeatureCollectoin.close( iterator )</code> will be
 * called on the internal delgate.
 * </p>
 *  
 * @author Jody Garnett, Refractions Research, Inc.
 * @source $URL$
 */
public class FilteredIterator implements Iterator {
	/** Used to close the delgate, or null */
	private FeatureCollection collection;
	private Iterator delegate;
	private Filter filter;

	private Object next;
	
	public FilteredIterator(Iterator iterator, Filter filter) {
		this.collection = null;
		this.delegate = iterator;
		this.filter = filter;
	}
	public FilteredIterator(FeatureCollection collection, Filter filter) {
		this.collection = collection;
		this.delegate = collection.iterator();
		this.filter = filter;
		next = getNext();
	}
	
	/** Package protected, please use SubFeatureCollection.close( iterator ) */
	void close(){
		if( collection != null ){
			collection.close( delegate );
		}
		collection = null;
		delegate = null;
		filter = null;
		next = null;
	}
	
	private Object getNext() {
		Object item = null;
		while (delegate.hasNext()) {
			item = (SimpleFeature) delegate.next();
			if (filter.evaluate( item )){
				return item;
			}
		}
		return null;
	}

	public boolean hasNext() {
		return next != null;
	}

	public Object next() {
		if(next == null){
			throw new NoSuchElementException();
		}
		Object current = next;
		next = getNext();
		return current;
	}

	public void remove() {
		if( delegate == null ) throw new IllegalStateException();
		
	    delegate.remove();
	}
}
