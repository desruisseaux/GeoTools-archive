package org.geotools.feature;

import org.geotools.feature.collection.AbstractResourceCollection;
import org.geotools.feature.collection.DelegateFeatureIterator;

/**
 * Helper methods to get us started on the implementation road
 * for FeatureCollections.
 * <p>
 * Most of the origional content of this class has moved to AbstractResourceCollection and/or
 * FeatureDelegate.
 * </p>
 * @deprecated Unused, moved to org.geotools.feature.collection
 * @author Jody Garnett, Refractions Research, Inc.
 * @since 2.1.RC0
 */
public abstract class AbstractFeatureCollection extends AbstractResourceCollection implements FeatureCollection {
     
    /** Default implementation based on DelegateFeatureIterator */
    public FeatureIterator features() {
        return new DelegateFeatureIterator(this, iterator() );
    }
    /** will close() the provided FeatureIterator */
    public void close( FeatureIterator close ) {
        if( close == null ) return;
        close.close();
    }    
}
