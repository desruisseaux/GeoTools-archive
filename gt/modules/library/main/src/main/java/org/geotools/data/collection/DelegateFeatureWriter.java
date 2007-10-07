package org.geotools.data.collection;

import java.io.IOException;

import org.geotools.data.FeatureWriter;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * A feature writer which delegates to a FeatureIterator to do writing.
 * <p>
 * The {@link #write()} method of this class does nothing, writing occurs 
 * on calls to {@link #next()}.
 * </p>
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class DelegateFeatureWriter implements FeatureWriter {

    /**
     * the feature type
     */
    SimpleFeatureType featureType;
    /**
     * the delegate
     */
    FeatureIterator delegate;
    
    public DelegateFeatureWriter( SimpleFeatureType featureType, FeatureIterator delegate ) {
        this.featureType = featureType;
        this.delegate = delegate;
    }
    
    public SimpleFeatureType getFeatureType() {
        return featureType;
    }

    public boolean hasNext() throws IOException {
        return delegate.hasNext();
    }

    public SimpleFeature next() throws IOException {
        return delegate.next();
    }

    public void remove() throws IOException {
        //TODO: implement
    }

    public void write() throws IOException {
        //do nothing, handled by a call to next
    }

    public void close() throws IOException {
        delegate.close();
    }

}
