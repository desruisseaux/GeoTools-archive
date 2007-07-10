package org.geotools.data.store;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.geotools.data.FeatureReader;
import org.geotools.data.collection.DelegateFeatureReader;
import org.geotools.feature.CollectionListener;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureList;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.collection.DelegateFeatureIterator;
import org.geotools.feature.visitor.FeatureVisitor;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.ProgressListener;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * FeatureCollection decorator which decorates a feature collection "re-typing" 
 * its schema based on attributes specified in a query.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class ReTypingFeatureCollection implements FeatureCollection {

	FeatureCollection delegate;
	FeatureType featureType;
    
	public ReTypingFeatureCollection ( FeatureCollection delegate, FeatureType featureType ) {
		this.delegate = delegate;
		this.featureType = featureType;
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
		return new ReTypingIterator( delegate.iterator(), delegate.getSchema(), featureType );
	}
	
	public void close(Iterator close) {
		ReTypingIterator reType = (ReTypingIterator) close;
		delegate.close( reType.getDelegate() );
	}

	public void addListener(CollectionListener listener) throws NullPointerException {
		delegate.addListener( listener );
	}

	public void removeListener(CollectionListener listener) throws NullPointerException {
		delegate.removeListener( listener );
	}

	public FeatureType getFeatureType() {
		return delegate.getFeatureType();
	}

	public FeatureType getSchema() {
		return delegate.getSchema();
	}

	public void accepts(FeatureVisitor visitor, ProgressListener progress) throws IOException {
		delegate.accepts( visitor, progress );
	}

	public FeatureCollection subCollection(Filter filter) {
		throw new UnsupportedOperationException();
	}

	public FeatureList sort(SortBy order) {
		throw new UnsupportedOperationException();
	}

	public void purge() {
		delegate.purge();
	}

	public int size() {
		return delegate.size();
	}

	public void clear() {
		delegate.clear();
	}

	public boolean isEmpty() {
		return delegate.isEmpty();
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
		return delegate.add( o );
	}

	public boolean contains(Object o) {
		return delegate.add( o );
	}

	public boolean remove(Object o) {
		return delegate.remove( o );
	}

	public boolean addAll(Collection c) {
		return delegate.addAll( c );
	}

	public boolean containsAll(Collection c) {
		return delegate.containsAll( c );
	}

	public boolean removeAll(Collection c) {
		return delegate.removeAll ( c );
	}

	public boolean retainAll(Collection c) {
		return delegate.retainAll( c );
	}

	public String getID() {
		return delegate.getID();
	}

	public Object[] getAttributes(Object[] attributes) {
		return delegate.getAttributes( attributes );
	}

	public Object getAttribute(String xPath) {
		return delegate.getAttribute( xPath );
	}

	public Object getAttribute(int index) {
		return delegate.getAttribute( index );
	}

	public void setAttribute(int position, Object val) throws IllegalAttributeException, ArrayIndexOutOfBoundsException {
		delegate.setAttribute( position, val );
	}

	public int getNumberOfAttributes() {
		return delegate.getNumberOfAttributes();
	}

	public void setAttribute(String xPath, Object attribute) throws IllegalAttributeException {
		delegate.setAttribute( xPath, attribute );
	}

	public Geometry getDefaultGeometry() {
		return delegate.getDefaultGeometry();
	}
	
	public Geometry getPrimaryGeometry() {
		return delegate.getPrimaryGeometry();
	}

	public void setDefaultGeometry(Geometry geometry) throws IllegalAttributeException {
		delegate.setDefaultGeometry( geometry );
	}
	
	public void setPrimaryGeometry(Geometry geometry) throws IllegalAttributeException {
		delegate.setPrimaryGeometry(geometry);
	}

	public ReferencedEnvelope getBounds() {
		return delegate.getBounds();
	}

}
