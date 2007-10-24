package org.geotools.data;

import java.io.IOException;

import org.geotools.data.store.ContentFeatureIterator;
import org.geotools.feature.FeatureIterator;
import org.opengis.filter.Filter;

/**
 * Extension of {@link FilteringFeatureIterator} which implements 
 * {@link ContentFeatureIterator}.
 * 
 * TODO: merge with FilteringFeatureIteartor from main when FC proposal is figured
 * out.
 *
 */
public class FilteringFeatureIterator extends org.geotools.data.store.FilteringFeatureIterator
    implements ContentFeatureIterator {

    public FilteringFeatureIterator( FeatureIterator delegate, Filter filter ) {
        super(delegate,filter);
    }

    public void remove() throws IOException {
        if ( delegate instanceof ContentFeatureIterator ) {
            ((ContentFeatureIterator)delegate).remove();
        }
    }

    public void write() throws IOException {
        if ( delegate instanceof ContentFeatureIterator ) {
            ((ContentFeatureIterator)delegate).write();
        }
    }
    
}
