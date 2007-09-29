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
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;

/**
 * A feature iterator that completely delegates to a normal
 * Iterator, simply allowing Java 1.4 code to escape the caste (sic)
 * system.
 * <p>
 * This implementation is not suitable for use with collections
 * that make use of system resources. As an alterantive please
 * see ResourceFetaureIterator.
 * </p>
 * @author Jody Garnett, Refractions Research, Inc.
 * @source $URL$
 */
public class DelegateFeatureIterator implements FeatureIterator {
	Iterator delegate;
	private FeatureCollection collection;
	/**
	 * Wrap the provided iterator up as a FeatureIterator.
	 * 
	 * @param iterator Iterator to be used as a delegate.
	 */
	public DelegateFeatureIterator( FeatureCollection collection, Iterator iterator ){
		delegate = iterator;
		this.collection=collection;
	}
	public boolean hasNext() {
		return delegate != null && delegate.hasNext();
	}
	public SimpleFeature next() throws NoSuchElementException {
		if( delegate == null ) throw new NoSuchElementException();
		return (SimpleFeature) delegate.next();
	}
	public void close() {
		if( collection!=null && delegate!=null)
			collection.close(delegate);
		collection =null;
		delegate = null;
		
	}
}
