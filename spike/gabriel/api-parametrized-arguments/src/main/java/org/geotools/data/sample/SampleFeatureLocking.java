package org.geotools.data.sample;

import java.awt.RenderingHints.Key;
import java.io.IOException;
import java.util.Set;

import org.geotools.data.DataStore;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureLock;
import org.geotools.data.FeatureLocking;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
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

/**
 * This fake implementation illustrates the actual problem with this approach,
 * that you cannot as easily narrow the parameters when they're genericized. See
 * {@link #setFeatures(FeatureReader)} and {@link #setFeatures(Reader)}, as
 * well as {@link #addFeatures(FeatureCollection)} and
 * {@link #addFeatures(SimpleFeatureCollection)}, they end up being overloaded
 * versions of the originals and go against the intent of the proposal that is
 * to keep the DataStore based API as close as possible as it was. See the third
 * spike apprach to find out how it would be to approach it in a way to only
 * parametrize the argument types strictly needed to cope with that goal.
 * 
 * @author Gabriel Roldan (TOPP)
 * @version $Id$
 * @since 2.5.x
 * @source $URL$
 */
public class SampleFeatureLocking implements FeatureLocking {

    // ///////////////////////////////////////////////
    // the following are the problematic methods and illustrate why this
    // approach goes only half way////
    // ///////////////////////////////////////////////

    public void setFeatures(FeatureReader reader) throws IOException {
    }

    public void setFeatures(Reader<SimpleFeatureType, SimpleFeature> reader) throws IOException {
    }

    public Set<FeatureId> addFeatures(FeatureCollection<SimpleFeatureType, SimpleFeature> collection)
            throws IOException {
        return null;
    }

    public Set<FeatureId> addFeatures(SimpleFeatureCollection collection) throws IOException {
        return null;
    }

    // /////////

    public FeatureWriter getFeatureWriter(Query query) {
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

    public void setTransaction(Transaction transaction) {
    }

    public void accept(FeatureVisitor<SimpleFeature> visitor, ProgressListener listener)
            throws IOException {
    }

    public void accept(Filter filter, FeatureVisitor<SimpleFeature> visitor,
            ProgressListener listener) throws IOException {
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

    public FeatureReader getFeatureReader(Query qury) {
        return null;
    }

    public SimpleFeatureCollection getFeatures(Query query) throws IOException {
        return null;
    }

    public SimpleFeatureCollection getFeatures(Filter filter) throws IOException {
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

    public Set<Key> getSupportedHints() {
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