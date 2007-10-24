package org.geotools.data.store;

import java.io.IOException;

import org.geotools.feature.FeatureIterator;

/**
 * Temporary measure while feature collection cleanup proposal is in progress.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public interface ContentFeatureIterator extends FeatureIterator {

    void remove() throws IOException;
    
    void write() throws IOException;
}
