package org.geotools.feature;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Helper methods to get us started on the implementation road
 * for FeatureCollections.
 *  
 * @author jgarnett
 * @since 2.1.RC0
 */
public abstract class AbstractFeatureCollection implements FeatureCollection {

    /** 
     * Get the set of fids for the provided collection.
     * <p>
     * By doing a quick pass through the collection we can  do
     * comparisons based on Feature ID (rather then collection
     * membership).
     * </p>
     * <p>
     * A subclass that tracks its FID information may wish to override
     * this method.
     * </p>
     */
    protected Set fids( Collection collection ){
        if( collection instanceof DefaultFeatureCollection ){
            DefaultFeatureCollection features = (DefaultFeatureCollection) collection;
            return features.fids();
        }
        
        Iterator iterator = collection.iterator();
        Set fids = new HashSet();
        try {
            while( iterator.hasNext() ){
                Feature feature = (Feature) iterator.next();
                fids.add( feature.getID() );
            }
        }
        finally {
            if( collection instanceof FeatureCollection){
                ((FeatureCollection) collection).close( iterator );
            }
        }
        return fids;
    }
    
    protected boolean isFeatures( Collection collection ){
        if( collection instanceof FeatureCollection ) return true;
        
        for( Iterator i = collection.iterator(); i.hasNext(); ){
            if(!(i.next() instanceof Feature))
                return false;
        }
        return true;
    }
}
