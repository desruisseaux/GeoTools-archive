package org.geotools.feature.collection;

import java.io.IOException;
import java.util.AbstractCollection;
import java.util.Iterator;

import org.geotools.data.FeatureReader;
import org.geotools.data.collection.DelegateFeatureReader;
import org.geotools.feature.CollectionListener;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureList;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.visitor.FeatureVisitor;
import org.geotools.filter.Filter;
import org.geotools.filter.SortBy;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Used as a reasonable default implementation for subCollection.
 * <p>
 * Note: to implementors, this is not optional, please do your own
 * thing - the users will thank you.
 * </p>
 * 
 * @author jgarnett
 *
 */
public class SubFeatureCollection extends AbstractCollection implements FeatureCollection {
	
	protected FeatureCollection collection;
	protected Filter filter;

	public SubFeatureCollection(FeatureCollection collection, Filter filter) {
		if (filter.equals(Filter.ALL)) {
			throw new IllegalArgumentException("A subcollection with Filter.ALL is a null operation");
		}
		if (filter.equals(Filter.NONE)) {
			throw new IllegalArgumentException("A subcollection with Filter.NONE should be a FeatureCollectionEmpty");
		}
		if (collection instanceof SubFeatureCollection) {
			SubFeatureCollection filtered = (SubFeatureCollection) collection;
			collection = filtered.collection;
			this.filter = filtered.filter.and(filter);
		} else {
			this.collection = collection;
			this.filter = filter;
		}
	}

	public FeatureType getFeatureType() {
		return collection.getFeatureType();
	}
	
	public FeatureIterator features() {
		return new DelegateFeatureIterator( iterator() );		
	}
	
	
	
	public void close(Iterator iterator) {
		if( iterator == null ) return;
		
		if( iterator instanceof FilteredIterator){
			FilteredIterator filtered = (FilteredIterator) iterator;			
			filtered.close();
		}
	}
	public void close(FeatureIterator close) {
		if( close != null ) close.close();
	}

	/**
	 * I am a little bit worried about this one!
	 * <p>
	 * Here is why: it a set threory we are defining <b>collection</b>
	 * as a subset of <b>all possible</b> features in the universe. Since
	 * this is a <i>subset</i> of collection it is a different (smaller)
	 * collection and should get a new ID.
	 * </p>
	 * <p>
	 * Since the above is clear cut why is this method returning
	 * the same ID as collection? Because this collection is really
	 * be used as a computer science hack to reduce the number
	 * of methods that are part of the FeatureCollection interface.
	 * </p>
	 * <p>
	 * This distinction brings clarity to this class - it really is
	 * <i>a view</i> used to allow a way to interact with the
	 * origional collection and is not really expected to venture out
	 * into the world on its own steam.
	 * </p>
	 */
	public String getID() {
		return collection.getID();
	}

	public Envelope getBounds(){
		Iterator i = null;
		Envelope bounds = new Envelope();
		try {
		    for( i=iterator(); i.hasNext();  ) {
				Feature feature = (Feature) i.next();
				bounds.expandToInclude(feature.getBounds());
		    }
		}
		finally {
			close( i );
		}
		return bounds;
	}
	
	public Geometry getDefaultGeometry() {
		return collection.getDefaultGeometry();
	}

	public void setDefaultGeometry(Geometry g) {
		throw new UnsupportedOperationException();
	}

	public FeatureCollection subCollection(Filter filter) {
		if (filter.equals(Filter.NONE)) {
			return this;
		}
		if (filter.equals(Filter.ALL)) {
			// TODO implement EmptyFeatureCollection( schema )
		}
		return new SubFeatureCollection(this, filter);
	}

	public int size() {
		int count = 0;
		Iterator i = null;		
		try {
			for( i = iterator(); i.hasNext(); count++) i.next();
		}
		finally {
			close( i );
		}
		return count;
	}

	public boolean isEmpty() {
		Iterator iterator = iterator();
		try {
			return iterator.hasNext();
		}
		finally {
			close( iterator );
		}
	}
	
	public Iterator iterator() {
		return new FilteredIterator( collection, filter );
	}

	public void addListener(CollectionListener listener) throws NullPointerException {
		collection.addListener( listener );
	}

	public void removeListener(CollectionListener listener) throws NullPointerException {
		collection.removeListener( listener );
	}

	public FeatureType getSchema() {
		return collection.getSchema();
	}

	public void accepts(FeatureVisitor visitor) throws IOException {
		Iterator iterator = null;
		try {
			for( iterator = iterator(); iterator.hasNext(); ){
				try {
					visitor.visit( (Feature) iterator.next() );
				}
				catch( Throwable t ){
					// WARNING!
				}
			}
		}
		finally {
			close( iterator );
		}
	}

	public FeatureReader reader() throws IOException {
		return new DelegateFeatureReader( getSchema(), features() );
	}

	public int getCount() throws IOException {
		return size();
	}

	public FeatureCollection collection() throws IOException {
		return this;
	}

	public FeatureCollection getParent() {
		return null; 
	}

	public void setParent(FeatureCollection collection) {
		collection.setParent( collection );
	}

	public Object[] getAttributes(Object[] attributes) {
		return collection.getAttributes( attributes );
	}

	public Object getAttribute(String xPath) {
		return collection.getAttribute( xPath );
	}

	public Object getAttribute(int index) {
		return collection.getAttribute( index );
	}

	public void setAttribute(int position, Object val) throws IllegalAttributeException, ArrayIndexOutOfBoundsException {
		collection.setAttribute( position, val  );
	}
	public int getNumberOfAttributes() {
		return collection.getNumberOfAttributes();
	}

	public void setAttribute(String xPath, Object attribute) throws IllegalAttributeException {
		collection.setAttribute( xPath, attribute );
	}

	public FeatureList sort(SortBy order) {
		return null;
	}

	public void purge() {
		collection.purge();
	}	
}
