package org.geotools.data.memory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.geotools.catalog.GeoResourceInfo;
import org.geotools.data.Source;
import org.geotools.data.Transaction;
import org.opengis.feature.type.TypeName;
import org.opengis.filter.Filter;
import org.opengis.filter.capability.FilterCapabilities;

/**
 * Used to quickly adapt a collection for APIs expecting to
 * be able to query generic content.
 * <p>
 * Please note that this is read-only access.
 * 
 * @author Jody Garnett
 */
public class CollectionSource implements Source {

    private Collection collection;

    public CollectionSource( Collection collection ){
        this.collection = Collections.unmodifiableCollection( collection );
    }
    
    public Collection content() {
        return collection;
    }

    public Collection content( String query, String queryLanguage ) {
        throw new UnsupportedOperationException("Please help me hook up the parser!");
    }

    public Collection content( Filter filter ) {
        List list = new ArrayList();
        for( Iterator i = collection.iterator(); i.hasNext();){
            Object obj = i.next();
            if( filter.evaluate( obj )){
                list.add( obj );
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

    public TypeName getName() {
        return new org.geotools.feature.type.TypeName("localhost/memory");
    }

    public void setTransaction( Transaction t ) {
        // ignored
    }

    public GeoResourceInfo getInfo() {
        return null; // TODO: info? at least scan through for bounds
    }

}