package org.geotools.data.postgis;

import java.io.IOException;
import java.util.Set;

import org.geotools.data.DataStore;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.opengis.filter.Filter;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Really delegates everything to a wrapped feature source, but allows to
 * advertise a data store other than the original one
 * 
 * @author aaime
 * @since 2.4
 * 
 */
public class WrappingPostgisFeatureSource implements FeatureSource {
    FeatureSource wrapped;

    VersionedPostgisDataStore store;

    public WrappingPostgisFeatureSource(FeatureSource wrapped,
            VersionedPostgisDataStore store) {
        this.wrapped = wrapped;
        this.store = store;
    }

    public DataStore getDataStore() {
        return store;
    }

    public void addFeatureListener(FeatureListener listener) {
        wrapped.addFeatureListener(listener);
    }

    public void removeFeatureListener(FeatureListener listener) {
        wrapped.removeFeatureListener(listener);
    }

    public Envelope getBounds() throws IOException {
        return wrapped.getBounds();
    }

    public Envelope getBounds(Query query) throws IOException {
        return wrapped.getBounds(query);
    }

    public int getCount(Query query) throws IOException {
        return wrapped.getCount(query);
    }

    public FeatureCollection getFeatures() throws IOException {
        return wrapped.getFeatures();
    }

    public FeatureCollection getFeatures(Filter filter) throws IOException {
        return wrapped.getFeatures(filter);
    }

    public FeatureCollection getFeatures(Query query) throws IOException {
        return wrapped.getFeatures(query);
    }

    public FeatureType getSchema() {
        return wrapped.getSchema();
    }
    
    public Set getSupportedHints() {
        return wrapped.getSupportedHints();
    }

}
