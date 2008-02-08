package org.geotools.data.sample;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.geotools.data.FeatureReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class SampleFeatureReader implements FeatureReader {

    public SimpleFeatureType getFeatureType() {
        return null;
    }

    public SimpleFeature next() throws IOException, IllegalArgumentException,
            NoSuchElementException {
        return null;
    }

    public void close() throws IOException {
    }

    public boolean hasNext() throws IOException {
        return false;
    }

}
