package org.geotools.feature.collection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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
 * Note: to implementors, this is not optimal, please do your own
 * thing - your users will thank you.
 * </p>
 * 
 * @author Jody Garnett, Refractions Research, Inc.
 *
 */
public class SubFeatureCollection extends AbstractResourceCollection implements FeatureCollection {
	/** Filter */
    protected Filter filter;
    
    /** Origional Collection */
	protected FeatureCollection collection;
    
    protected FeatureState state;
    
    public SubFeatureCollection(FeatureCollection collection ) {
        this( collection, null );
    }
	public SubFeatureCollection(FeatureCollection collection, Filter subfilter ){
		if (subfilter.equals(Filter.ALL)) {
			throw new IllegalArgumentException("A subcollection with Filter.ALL is a null operation");
		}
		if (subfilter.equals(Filter.NONE)) {
			throw new IllegalArgumentException("A subcollection with Filter.NONE should be a FeatureCollectionEmpty");
		}
        if( subfilter != null ){
    		if (collection instanceof SubFeatureCollection) {
    			SubFeatureCollection filtered = (SubFeatureCollection) collection;
    			collection = filtered.collection;            
    			this.filter = filtered.filter().and(subfilter);
    		} else {
    			this.collection = collection;
    			this.filter = subfilter;
    		}
        }
        state = new SubFeatureState( this.collection, this );
	}
    
	protected Filter filter(){
	    if( filter == null ){
            filter = createFilter();
        }
        return filter;
    }
    /** Override to implement subsetting */
    protected Filter createFilter(){
        return Filter.NONE;
    }
    
	public FeatureType getFeatureType() {
		return state.getFeatureType();
	}
	
	public FeatureIterator features() {
		return new DelegateFeatureIterator( iterator() );		
	}	
	
	public void closeIterator(Iterator iterator) {
		if( iterator == null ) return;
		
		if( iterator instanceof FilteredIterator){
			FilteredIterator filtered = (FilteredIterator) iterator;			
			filtered.close();
		}
	}
	public void close(FeatureIterator close) {
		if( close != null ) close.close();
	}

    //
    // Feature methods
    //
	public String getID() {
		return state.getId();
	}

	public Envelope getBounds(){
        return state.getBounds();        
	}
	
	public Geometry getDefaultGeometry() {
		return state.getDefaultGeometry();
	}

	public void setDefaultGeometry(Geometry g) throws IllegalAttributeException {
		state.setDefaultGeometry( g );
	}
    public void addListener(CollectionListener listener) throws NullPointerException {
        state.addListener( listener );
    }

    public void removeListener(CollectionListener listener) throws NullPointerException {
        state.removeListener( listener );
    }
    
    public FeatureCollection getParent() {
        return state.getParent(); 
    }

    public void setParent(FeatureCollection collection) {
        state.setParent( collection );
    }

    public Object[] getAttributes(Object[] attributes) {
        return state.getAttributes( attributes );
    }

    public Object getAttribute(String xPath) {
        return state.getAttribute( xPath );
    }

    public Object getAttribute(int index) {
        return state.getAttribute( index );
    }

    public void setAttribute(int position, Object val) throws IllegalAttributeException, ArrayIndexOutOfBoundsException {
        state.setAttribute( position, val  );
    }
    public int getNumberOfAttributes() {
        return state.getNumberOfAttributes();
    }

    public void setAttribute(String xPath, Object attribute) throws IllegalAttributeException {
        state.setAttribute( xPath, attribute );
    }
    
    //
    //
    //
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
	
	public Iterator openIterator() {
		return new FilteredIterator( collection, filter() );
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

	public FeatureList sort(SortBy order) {
		return null;
	}

	public void purge() {
		collection.purge();
	}	
}