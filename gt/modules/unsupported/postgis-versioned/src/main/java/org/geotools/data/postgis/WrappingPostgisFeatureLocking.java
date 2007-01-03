package org.geotools.data.postgis;

import java.io.IOException;
import java.util.Set;

import org.geotools.data.DataStore;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureLock;
import org.geotools.data.FeatureLocking;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.opengis.filter.Filter;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Really delegates everything to a wrapped feature locking, but allows to advertise a data store
 * other than the original one
 * 
 * @author aaime
 * @since 2.4
 * 
 */
class WrappingPostgisFeatureLocking implements FeatureLocking {
    FeatureLocking wrapped;

    private VersionedPostgisDataStore store;

    public WrappingPostgisFeatureLocking(FeatureLocking wrapped, VersionedPostgisDataStore store) {
        this.wrapped = wrapped;
        this.store = store;
    }

    public DataStore getDataStore() {
        return store;
    }

    public void addFeatureListener(FeatureListener listener) {
        wrapped.addFeatureListener(listener);
    }

    public Set addFeatures(FeatureCollection collection) throws IOException {
        return wrapped.addFeatures(collection);
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

    public Transaction getTransaction() {
        return wrapped.getTransaction();
    }

    public int lockFeatures() throws IOException {
        return wrapped.lockFeatures();
    }

    public int lockFeatures(Filter filter) throws IOException {
        return wrapped.lockFeatures(filter);
    }

    public int lockFeatures(Query query) throws IOException {
        return wrapped.lockFeatures(query);
    }

    public void modifyFeatures(AttributeType type, Object value, Filter filter) throws IOException {
        wrapped.modifyFeatures(type, value, filter);
    }

    public void modifyFeatures(AttributeType[] type, Object[] value, Filter filter)
            throws IOException {
        wrapped.modifyFeatures(type, value, filter);
    }

    public void removeFeatureListener(FeatureListener listener) {
        wrapped.removeFeatureListener(listener);
    }

    public void removeFeatures(Filter filter) throws IOException {
        wrapped.removeFeatures(filter);
    }

    public void setFeatureLock(FeatureLock lock) {
        wrapped.setFeatureLock(lock);
    }

    public void setFeatures(FeatureReader reader) throws IOException {
        wrapped.setFeatures(reader);
    }

    public void setTransaction(Transaction transaction) {
        wrapped.setTransaction(transaction);
    }

    public void unLockFeatures() throws IOException {
        wrapped.unLockFeatures();
    }

    public void unLockFeatures(Filter filter) throws IOException {
        wrapped.unLockFeatures(filter);
    }

    public void unLockFeatures(Query query) throws IOException {
        wrapped.unLockFeatures(query);
    }

}
