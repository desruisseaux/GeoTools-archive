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

import java.io.IOException;
import java.util.Iterator;

import org.geotools.data.FeatureReader;
import org.geotools.data.collection.DelegateFeatureReader;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.CollectionListener;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureList;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.visitor.FeatureVisitor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.sort.SortBy;
import org.geotools.util.ProgressListener;

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
 * @source $URL$
 */
public class SubFeatureCollection extends AbstractResourceCollection implements FeatureCollection {
	/** Filter */
    protected Filter filter;
    
    /** Origional Collection */
	protected FeatureCollection collection;    
    protected FeatureState state;
    protected FilterFactory ff = CommonFactoryFinder.getFilterFactory( null );
    
    public SubFeatureCollection(FeatureCollection collection ) {
        this( collection, null );
    }
	public SubFeatureCollection(FeatureCollection collection, Filter subfilter ){
		if (subfilter != null && subfilter.equals(Filter.EXCLUDE)) {
			throw new IllegalArgumentException("A subcollection with Filter.EXCLUDE is a null operation");
		}
		if (subfilter != null && subfilter.equals(Filter.INCLUDE)) {
			throw new IllegalArgumentException("A subcollection with Filter.INCLUDE should be a FeatureCollectionEmpty");
		}
        if( subfilter != null && (collection instanceof SubFeatureCollection)){
			SubFeatureCollection filtered = (SubFeatureCollection) collection;
			this.collection = filtered.collection;            
			this.filter = ff.and( filtered.filter(), subfilter );
		} else {
			this.collection = collection;
			this.filter = subfilter;
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
        return Filter.INCLUDE;
    }
    
	public FeatureType getFeatureType() {
		return state.getFeatureType();
	}
	
	public FeatureIterator features() {
		return new DelegateFeatureIterator( this, iterator() );		
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
		if (filter.equals(Filter.INCLUDE)) {
			return this;
		}
		if (filter.equals(Filter.EXCLUDE)) {
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

    /**
     * Accepts a visitor, which then visits each feature in the collection.
     * @throws IOException 
     */
    public void accepts(FeatureVisitor visitor, ProgressListener progress ) throws IOException {
        Iterator iterator = null;
        // if( progress == null ) progress = new NullProgressListener();
        try{
            float size = size();
            float position = 0;            
            progress.started();
            for( iterator = iterator(); !progress.isCanceled() && iterator.hasNext(); progress.progress( position++/size )){
                try {
                    Feature feature = (Feature) iterator.next();
                    visitor.visit(feature);
                }
                catch( Exception erp ){
                    progress.exceptionOccurred( erp );
                }
            }            
        }
        finally {
            progress.complete();            
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
