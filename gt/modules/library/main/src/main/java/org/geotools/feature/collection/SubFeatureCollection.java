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
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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
import org.geotools.feature.simple.SimpleFeatureCollectionImpl;
import org.geotools.feature.visitor.FeatureVisitor;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.type.AttributeType;
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
public class SubFeatureCollection extends BaseFeatureCollection implements FeatureCollection {
	/** Filter */
    protected Filter filter;
    
    /** Origional Collection */
	protected FeatureCollection collection;    
    //protected FeatureState state;
    protected FilterFactory ff = CommonFactoryFinder.getFilterFactory( null );
    
    protected AbstractResourceCollection rc; 
    
    public SubFeatureCollection(FeatureCollection collection ) {
        this( collection, null );
    }
	public SubFeatureCollection(FeatureCollection collection, Filter subfilter ){
		super(null,collection.getFeatureCollectionType());
		
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
        
        rc = createResourceCollection();
    }
	
	AbstractResourceCollection createResourceCollection() {
		return new AbstractResourceCollection() {
			public Iterator openIterator() {
    			return new FilteredIterator( collection, filter() );
    		}

    		public void closeIterator(Iterator iterator) {
    			if( iterator == null ) return;
    			
    			if( iterator instanceof FilteredIterator){
    				FilteredIterator filtered = (FilteredIterator) iterator;			
    				filtered.close();
    			}
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
			
		};
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
    
	public FeatureIterator features() {
		return new DelegateFeatureIterator( this, iterator() );		
	}	
	
	
	public void close(FeatureIterator close) {
		if( close != null ) close.close();
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

	
	public boolean isEmpty() {
		Iterator iterator = iterator();
		try {
			return !iterator.hasNext();
		}
		finally {
			close( iterator );
		}
	}
	

	public FeatureType getSchema() {
        return collection.getSchema();
	}

    /**
     * Accepts a visitor, which then visits each feature in the collection.
     * @throws IOException 
     */
    public final void accepts(FeatureVisitor visitor, ProgressListener progress ) throws IOException {
        accepts((org.opengis.feature.FeatureVisitor) visitor, (org.opengis.util.ProgressListener) progress);
    }
    
	public void accepts(org.opengis.feature.FeatureVisitor visitor, org.opengis.util.ProgressListener progress) {
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
	public void close(Iterator close) {
		rc.close(close);
	}
	public Iterator iterator() {
		return rc.iterator();
	}
	public boolean add(Object o) {
		return rc.add(o); 
	}
	public boolean addAll(Collection c) {
		return rc.addAll(c);
	}
	public void clear() {
		rc.clear();
	}
	public boolean contains(Object o) {
		return rc.contains(o);
	}
	public boolean containsAll(Collection c) {
		return rc.containsAll(c);
	}
	public boolean remove(Object o) {
		return rc.remove(o);
	}
	public boolean removeAll(Collection c) {
		return rc.removeAll(c);
	}
	public boolean retainAll(Collection c) {
		return rc.retainAll(c);
	}
	public int size() {
		return rc.size();
	}
	public Object[] toArray() {
		return rc.toArray();
	}
	public Object[] toArray(Object[] a) {
		return rc.toArray(a);
	}

}
