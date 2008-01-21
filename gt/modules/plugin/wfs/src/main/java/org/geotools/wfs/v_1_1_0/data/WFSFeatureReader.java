package org.geotools.wfs.v_1_1_0.data;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.geotools.data.FeatureReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class WFSFeatureReader implements FeatureReader {

    private SimpleFeature next;

    private GetFeatureParser parser;

    private SimpleFeatureType featureType;

    public WFSFeatureReader(final SimpleFeatureType featureType, final GetFeatureParser parser)
            throws IOException {
        this.featureType = featureType;
        this.parser = parser;
        this.next = parser.parse();
    }

    /**
     * @see FeatureReader#close()
     */
    public void close() throws IOException {
        final GetFeatureParser parser = this.parser;
        this.parser = null;
        this.next = null;
        if (parser != null) {
            parser.close();
        }
    }

    /**
     * @see FeatureReader#getFeatureType()
     */
    public SimpleFeatureType getFeatureType() {
        return featureType;
    }

    /**
     * @see FeatureReader#hasNext()
     */
    public boolean hasNext() throws IOException {
        return next != null;
    }

    /**
     * @see FeatureReader#next()
     */
    public SimpleFeature next() throws IOException, NoSuchElementException {
        if (this.next == null) {
            throw new NoSuchElementException();
        }
        SimpleFeature current = this.next;
        this.next = parser.parse();
        return current;
    }

}
