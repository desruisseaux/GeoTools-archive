/**
 * 
 * @source $URL$
 */
package org.geotools.data;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;

class TestReader implements FeatureReader{

    /**
	 * 
	 */
	private FeatureType type;
	private Feature feature;

    public TestReader(FeatureType type, Feature f) {
        this.type = type;
		this.feature=f;
    }
    
    public FeatureType getFeatureType() {
        return type;
    }

    public Feature next() throws IOException, IllegalAttributeException, NoSuchElementException {
        next=false;
        return feature;
    }

    boolean next=true;
    public boolean hasNext() throws IOException {
        return next;
    }

    public void close() throws IOException {
    }
    
}
