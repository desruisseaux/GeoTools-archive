package org.geotools.data;

import java.io.IOException;

import org.geotools.factory.Hints;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;

/**
 * An extension of FeatureCollection which provides additional api for 
 * ContentDataStore.
 * <p>
 * This class is temporary while the feature collection cleanup takes place.
 * </p>
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public interface ContentFeatureCollection extends FeatureCollection {

    void setHints( Hints hints );
    
    FeatureIterator writer() throws IOException;
    
    FeatureIterator inserter() throws IOException;
}
