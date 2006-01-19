package org.geotools.feature.collection;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;

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
 */
public class DelegateFeatureIterator implements FeatureIterator {
	Iterator delegate;
	/**
	 * Wrap the provided iterator up as a FeatureIterator.
	 * 
	 * @param iterator Iterator to be used as a delegate.
	 */
	public DelegateFeatureIterator( FeatureCollection collection, Iterator iterator ){
		delegate = iterator;
	}
	public boolean hasNext() {
		return delegate != null && delegate.hasNext();
	}
	public Feature next() throws NoSuchElementException {
		if( delegate == null ) throw new NoSuchElementException();
		return (Feature) delegate.next();
	}
	public void close() {
		delegate = null;
		
	}
}
