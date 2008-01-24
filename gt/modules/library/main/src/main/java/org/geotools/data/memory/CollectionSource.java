package org.geotools.data.memory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.geotools.data.Transaction;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.capability.FilterCapabilities;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Used to quickly adapt a collection for APIs expecting to
 * be able to query generic content.
 * <p>
 * Please note that this is read-only access.
 * 
 * @author Jody Garnett
 */
public final class CollectionSource {

    private Collection collection;
    private CoordinateReferenceSystem crs;
    
    public CollectionSource( Collection collection ){
        this( collection, null );
    }
    
    public CollectionSource( Collection collection, CoordinateReferenceSystem crs ){
        this.collection = Collections.unmodifiableCollection( collection );
        this.crs = crs;
    }
    
    public Collection content() {
        return collection;
    }

    public Collection content( String query, String queryLanguage ) {
        throw new UnsupportedOperationException("Please help me hook up the parser!");
    }

    public Collection content( Filter filter ) {
        return content(filter, Integer.MAX_VALUE);
    }
    
    public Collection content( Filter filter, int countLimit ) {
        List list = new ArrayList();
        int count = 0;
        for( Iterator i = collection.iterator(); i.hasNext() && count < countLimit;){
            Object obj = i.next();
            if( filter.evaluate( obj )){
                list.add( obj );
                count++;
            }
        }
        return Collections.unmodifiableList( list );
    }

    public Object describe() {
        return Object.class; // TODO: be more specific
    }

    public void dispose() {
        collection = null;
    }

    public FilterCapabilities getFilterCapabilities() {
        return null;
    }

    public Name getName() {
        return new org.geotools.feature.Name("localhost/memory");
    }

    public void setTransaction( Transaction t ) {
        // ignored
    }
    
    public CoordinateReferenceSystem getCRS(){
        return crs;
    }

}