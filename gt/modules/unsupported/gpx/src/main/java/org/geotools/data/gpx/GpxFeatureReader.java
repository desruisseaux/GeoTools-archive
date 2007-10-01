package org.geotools.data.gpx;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.geotools.data.FeatureReader;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.gpx.bean.RteType;
import org.geotools.gpx.bean.TrkType;
import org.geotools.gpx.bean.WptType;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class GpxFeatureReader implements FeatureReader {

    private final SimpleFeatureType featureType;
    private final Iterator it;
    private final FeatureTranslator translator;

    GpxFeatureReader(GpxDataStore dataStore, String featureName) {
        featureType = dataStore.getSchema(featureName);
    

    	if(GpxDataStore.TYPE_NAME_POINT.equals(featureName)) {
            it = dataStore.getGpxData().getWpt().iterator();
        } else if(GpxDataStore.TYPE_NAME_TRACK.equals(featureName)) {
            it = dataStore.getGpxData().getTrk().iterator();
        } else if(GpxDataStore.TYPE_NAME_ROUTE.equals(featureName)) {
            it = dataStore.getGpxData().getRte().iterator();
        } else {
            throw new IllegalArgumentException("Unknown featureType: " + featureName);
        }
        
        translator = new FeatureTranslator(featureType);
    }
    
    public void close() throws IOException {
        // reading from memory, nothing to do.
        // TODO: maybe we should do locking???
    }

    public SimpleFeatureType getFeatureType() {
        return featureType;
    }

    public boolean hasNext() throws IOException {
        return it.hasNext();
    }

    public SimpleFeature next() throws IOException, IllegalAttributeException, NoSuchElementException {
        Object element = it.next();
        
        if(element instanceof WptType) {
            return (SimpleFeature) translator.convertFeature((WptType) element);
        } else if(element instanceof TrkType) {
            return (SimpleFeature) translator.convertFeature((TrkType) element);
        } else if(element instanceof RteType) {
            return (SimpleFeature) translator.convertFeature((RteType) element);
        } else {
            throw new RuntimeException("Illegal object class: " + element.getClass().getName());
        }
    }

}
