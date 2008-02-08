package org.geotools.data.sample;

import java.io.IOException;

import org.geotools.data.FeatureWriter;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class SampleFeatureWriter implements FeatureWriter {

    public SimpleFeatureType getFeatureType() {
        return null;
    }

    public SimpleFeature next() throws IOException {
        return null;
    }

    public void close() throws IOException {
    }

    public boolean hasNext() throws IOException {
        return false;
    }

    public void remove() throws IOException {
    }

    public void write() throws IOException {
    }

}
