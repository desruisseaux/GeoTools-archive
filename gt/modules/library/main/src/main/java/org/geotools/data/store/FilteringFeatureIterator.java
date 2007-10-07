package org.geotools.data.store;

import java.util.NoSuchElementException;

import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;

/**
 * Decorates a FeatureIterator  with one that filters content.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class FilteringFeatureIterator implements FeatureIterator {

    /**
     * delegate iterator
     */
    FeatureIterator delegate;
    /**
     * The Filter
     */
    Filter filter;
    /**
     * Next feature
     */
    SimpleFeature next;
    
    public FilteringFeatureIterator( FeatureIterator delegate, Filter filter ) {
        this.delegate = delegate;
        this.filter = filter;
    }
    
    public boolean hasNext() {
        if ( next != null ) {
            return true;
        }
        
        while( delegate.hasNext() ) {
            SimpleFeature peek = (SimpleFeature) delegate.next();
            if ( filter.evaluate( peek ) ) {
                next = peek;
                break;
            }
        }
        
        return next != null;
    }

    public SimpleFeature next() throws NoSuchElementException {
        SimpleFeature f = next;
        next = null;
        return f;
    }
    
    public void close() {
        delegate.close();
        delegate = null;
        next = null;
        filter = null;
    }


}
