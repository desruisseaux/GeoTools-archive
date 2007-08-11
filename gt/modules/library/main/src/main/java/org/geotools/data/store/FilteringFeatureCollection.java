package org.geotools.data.store;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureReader;
import org.geotools.data.collection.DelegateFeatureReader;
import org.geotools.feature.CollectionListener;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.collection.DecoratingFeatureCollection;
import org.geotools.feature.collection.DelegateFeatureIterator;
import org.geotools.feature.visitor.FeatureVisitor;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.ProgressListener;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Decorates a feature collection with one that filters content.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class FilteringFeatureCollection extends DecoratingFeatureCollection implements FeatureCollection {

	/**
	 * The original feature collection.
	 */
	FeatureCollection delegate;
	/**
	 * the filter
	 */
	Filter filter;
	
	public FilteringFeatureCollection( FeatureCollection delegate, Filter filter ) {
		super(delegate);
		this.delegate = delegate;
		this.filter = filter;
	}
	
	public FeatureIterator features() {
		return new DelegateFeatureIterator( this, iterator() );
	}

	public void close(FeatureIterator close) {
		close.close();
	}

	public Iterator iterator() {
		return new FilteringIterator( delegate.iterator(), filter );
	}
	
	public void close(Iterator close) {
		FilteringIterator filtering = (FilteringIterator) close;
		delegate.close( filtering.getDelegate() );
	}

	public FeatureCollection subCollection(Filter filter) {
		throw new UnsupportedOperationException();
	}

	public FeatureCollection sort(SortBy order) {
		throw new UnsupportedOperationException();
	}

	public int size() {
		int count = 0;
		Iterator i = iterator();
		try {
			while( i.hasNext() ) {
				count++; i.next();
			}
			
			return count;
		}
		finally {
			close( i );
		}
	}

	public boolean isEmpty() {
		return size() == 0;
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
		if ( !filter.evaluate( o ) ) {
			return false;
		}
		
		return delegate.add( o );
	}

	public boolean contains(Object o) {
		return delegate.contains( o ) && filter.evaluate( o );
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

	public FeatureReader reader() throws IOException {
		return new DelegateFeatureReader( getSchema(), features() );
	}

	public ReferencedEnvelope getBounds() {
		//calculate manually
		return ReferencedEnvelope.reference( DataUtilities.bounds( this ) );
	}

}
