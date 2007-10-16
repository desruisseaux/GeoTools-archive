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
    private final FeatureTranslator translator;
    private final GpxDataStore dataStore;

    private Iterator it;

    GpxFeatureReader(GpxDataStore dataStore, String featureName) {
        this.dataStore = dataStore;
        featureType = dataStore.getSchema(featureName);
        
        dataStore.getMemoryLock().acquireReadLock();

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
    
    public synchronized void close() throws IOException {
        if(it == null) {
            // this indicates, that we were already closed
            GpxDataStore.LOGGER.fine("GpxFeatureWriter.close(): called second time.");
            return;
        }
        
        dataStore.getMemoryLock().releaseReadLock();
        it = null;
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
            return translator.convertFeature((WptType) element);
        } else if(element instanceof TrkType) {
            return translator.convertFeature((TrkType) element);
        } else if(element instanceof RteType) {
            return translator.convertFeature((RteType) element);
        } else {
            throw new RuntimeException("Illegal object class: " + element.getClass().getName());
        }
    }

}
