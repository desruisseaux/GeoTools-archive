package org.geotools.data.sample;

import java.io.IOException;
import java.util.Set;

import org.geotools.data.DataStore;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureLock;
import org.geotools.data.FeatureLocking;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.Reader;
import org.geotools.data.ResourceInfo;
import org.geotools.data.SimpleFeatureCollection;
import org.geotools.data.Transaction;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.identity.FeatureId;
import org.opengis.util.ProgressListener;

public class SampleFeatureLocking implements FeatureLocking {

    public Set addFeatures(SimpleFeatureCollection collection) throws IOException {
        return null;
    }

    public void setFeatures(FeatureReader reader) throws IOException {
    }

    public Set<FeatureId> addFeatures(FeatureCollection<SimpleFeatureType, SimpleFeature> collection)
            throws IOException {
        return null;
    }

    public Transaction getTransaction() {
        return null;
    }

    public void modifyFeatures(AttributeDescriptor[] type, Object[] value, Filter filter)
            throws IOException {
    }

    public void modifyFeatures(AttributeDescriptor type, Object value, Filter filter)
            throws IOException {
    }

    public void removeFeatures(Filter filter) throws IOException {
    }

    public void setFeatures(Reader<SimpleFeatureType, SimpleFeature> reader) throws IOException {
    }

    public void setTransaction(Transaction transaction) {
    }

    public void accept(FeatureVisitor visitor, ProgressListener listener) throws IOException {
    }

    public void accept(Filter filter, FeatureVisitor visitor, ProgressListener listener)
            throws IOException {
    }

    public void addFeatureListener(FeatureListener listener) {
    }

    public ReferencedEnvelope getBounds() throws IOException {
        return null;
    }

    public ReferencedEnvelope getBounds(Query query) throws IOException {
        return null;
    }

    public int getCount(Query query) throws IOException {
        return 0;
    }

    public DataStore getDataStore() {
        return null;
    }

    public SimpleFeatureCollection getFeatures(Query query)
            throws IOException {
        return null;
    }

    public SimpleFeatureCollection getFeatures(Filter filter)
            throws IOException {
        return null;
    }

    public SimpleFeatureCollection getFeatures() throws IOException {
        return null;
    }

    public ResourceInfo getInfo() {
        return null;
    }

    public SimpleFeatureType getSchema() {
        return null;
    }

    public Set getSupportedHints() {
        return null;
    }

    public void removeFeatureListener(FeatureListener listener) {
    }

    public int lockFeatures(Query query) throws IOException {
        return 0;
    }

    public int lockFeatures(Filter filter) throws IOException {
        return 0;
    }

    public int lockFeatures() throws IOException {
        return 0;
    }

    public void setFeatureLock(FeatureLock lock) {
    }

    public void unLockFeatures() throws IOException {
    }

    public void unLockFeatures(Filter filter) throws IOException {
    }

    public void unLockFeatures(Query query) throws IOException {
    }

}
