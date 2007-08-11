package org.geotools.data.store;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureReader;
import org.geotools.data.collection.DelegateFeatureReader;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.collection.DecoratingFeatureCollection;
import org.geotools.feature.collection.DelegateFeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;

/**
 * FeatureCollection wrapper which limits the number of features returned.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class MaxFeaturesFeatureCollection extends DecoratingFeatureCollection 
	implements FeatureCollection {

	FeatureCollection delegate;
	long max;
	
	public MaxFeaturesFeatureCollection( FeatureCollection delegate, long max ) {
		super(delegate);
		this.delegate = delegate;
		this.max = max;
	}
	
	public FeatureReader reader() throws IOException {
		return new DelegateFeatureReader( getSchema(), features() );
	}
	
	public FeatureIterator features() {
		return new DelegateFeatureIterator( this, iterator() );
	}

	public void close(FeatureIterator close) {
		close.close();
	}

	public Iterator iterator() {
		return new MaxFeaturesIterator( delegate.iterator(), max );
	}
	
	public void close(Iterator close) {
		Iterator iterator = ((MaxFeaturesIterator)close).getDelegate();
		delegate.close( iterator );
	}

	public FeatureCollection subCollection(Filter filter) {
		throw new UnsupportedOperationException();
	}

	public FeatureCollection sort(SortBy order) {
		throw new UnsupportedOperationException();
	}

	public int size() {
		return (int) Math.min( delegate.size(), max );
	}

	public boolean isEmpty() {
		return delegate.isEmpty() || max == 0;
	}

	public Object[] toArray() {
		return toArray( new Object[ size() ] );
	}

	public Object[] toArray(Object[] a) {
		List list = new ArrayList();
		Iterator i = iterator();
		try {
			while( i.hasNext() ) {
				list.add( i.next() );
			}
			
			return list.toArray( a );
		}
		finally {
			close( i );
		}
	}
	
	public boolean add(Object o) {
		long size = delegate.size();
		if ( size < max ) {
			return delegate.add( o );	
		}
		
		return false;
	}

	public boolean addAll(Collection c) {
		boolean changed = false;
		
		for ( Iterator i = c.iterator(); i.hasNext(); ) {
			changed = changed | add( i.next() );
		}
		
		return changed;
	}

	public boolean containsAll(Collection c) {
		for ( Iterator i = c.iterator(); i.hasNext(); ) {
			if ( !contains( i.next() ) ) {
				return false;
			}
		}
		
		return true;
	}

	public ReferencedEnvelope getBounds() {
		//calculate manually
		return ReferencedEnvelope.reference( DataUtilities.bounds( this ) );
	}
}
