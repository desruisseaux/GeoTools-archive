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

import java.util.Collection;
import java.util.Iterator;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;

/**
 * Implement a feature collection just based on provision of iterator.
 * <p>
 * Your subclass will need to provide an internal "state" stratagy object
 * used to access collection attributes - see the two protected constructors
 * for details.
 * </p>
 * @author Jody Garnett, Refractions Research Inc.
 */
public abstract class AbstractFeatureCollection extends BaseFeatureCollection /*extends AbstractResourceCollection*/ implements FeatureCollection {
    
	AbstractResourceCollection rc;

	protected AbstractFeatureCollection( SimpleFeatureType memberType ) {
		super(null,memberType);
	}
	
	protected AbstractFeatureCollection( SimpleFeatureType memberType, AbstractResourceCollection rc ) {
		super(null,memberType);
		this.rc = rc;
	}
	
	protected void setResourceCollection( AbstractResourceCollection rc ) {
		this.rc = rc;
	}
	
    //
    // FeatureCollection - Feature Access
    // 
    public FeatureIterator features() {
        FeatureIterator iter = new DelegateFeatureIterator( this, rc.openIterator() );
        rc.getOpenIterators().add( iter );
        return iter; 
    }
    public void close( FeatureIterator close ) {     
        closeIterator( close );
        rc.getOpenIterators().remove( close );
    }
    public void closeIterator( FeatureIterator close ) {
        DelegateFeatureIterator iter = (DelegateFeatureIterator) close;
        rc.closeIterator( iter.delegate );
        iter.close(); 
    }
    public void purge() {
        for( Iterator i = rc.getOpenIterators().iterator(); i.hasNext(); ){
            Object resource = i.next();
            if( resource instanceof FeatureIterator ){
                FeatureIterator resourceIterator = (FeatureIterator) resource;
                try {
                    closeIterator( resourceIterator );
                }
                catch( Throwable e){
                    // TODO: Log e = ln
                }
                finally {
                    i.remove();
                }
            }
        }        

        rc.purge();
    }
    
    final public int size() {
    	return rc.size();
    }
    
    final public Iterator iterator() {
    	return rc.iterator();
    }
    
    final public void close(Iterator close) {
    	rc.close(close);
    };
    
    final public boolean add(Object o) {
		return rc.add(o);
	}

    final public boolean addAll(Collection c) {
		return rc.addAll(c);
	}

    final public void clear() {
    	rc.clear();
	}

    final public boolean contains(Object o) {
    	return rc.contains(o);
	}

    final public boolean containsAll(Collection c) {
		return rc.containsAll(c);
	}

    final public boolean isEmpty() {
		return rc.isEmpty();
	}

    final public boolean remove(Object o) {
		return rc.remove(o);
	}

    final public boolean removeAll(Collection c) {
		return rc.removeAll(c);
	}

    final public boolean retainAll(Collection c) {
		return rc.retainAll(c);
	}

    final public Object[] toArray() {
		return rc.toArray();
	}

    final public Object[] toArray(Object[] a) {
		return rc.toArray(a);
	}

	public void accepts(org.opengis.feature.FeatureVisitor visitor, org.opengis.util.ProgressListener progress) {
    	Iterator iterator = null;
        // if( progress == null ) progress = new NullProgressListener();
        try{
            float size = size();
            float position = 0;            
            progress.started();
            for( iterator = iterator(); !progress.isCanceled() && iterator.hasNext();){
                if (size > 0) progress.progress( position++/size );
                try {
                    SimpleFeature feature = (SimpleFeature) iterator.next();
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
    
    //
    // Feature Collections API
    //
    public FeatureCollection subList( Filter filter ) {
        return new SubFeatureList(this, filter );
    }
    
    public FeatureCollection subCollection( Filter filter ) {
        if( filter == Filter.INCLUDE ){
            return this;
        }        
        return new SubFeatureCollection( this, filter );
    }

    public FeatureCollection sort( SortBy order ) {
        return new SubFeatureList(this, order );
    }

    //
    // FeatureCollection - Legacy
    //
    /*
    public FeatureReader reader() throws IOException {
        return new DelegateFeatureReader( getSchema(), features() );
    }
    public int getCount() throws IOException {
        return size();
    }
    public FeatureCollection collection() throws IOException {
        return this;
    }
    */
    
    
}