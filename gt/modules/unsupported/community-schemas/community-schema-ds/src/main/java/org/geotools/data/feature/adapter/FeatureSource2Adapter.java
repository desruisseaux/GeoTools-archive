package org.geotools.data.feature.adapter;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;

import org.geotools.catalog.GeoResourceInfo;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.feature.FeatureSource2;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.iso.type.TypeFactoryImpl;
import org.opengis.feature.simple.SimpleFeatureFactory;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.TypeFactory;
import org.opengis.filter.Filter;
import org.opengis.filter.capability.FilterCapabilities;

import com.vividsolutions.jts.geom.Envelope;

public class FeatureSource2Adapter implements FeatureSource2 {

    private FeatureSource source;

    private FeatureAccessAdapter dataStore;

    private AttributeDescriptor descriptor;

    private SimpleFeatureType isoFeatureType;

    private SimpleFeatureFactory attributeFactory;
    
    public FeatureSource2Adapter(FeatureAccessAdapter dataStore,
            FeatureSource featureSource, SimpleFeatureFactory attributeFactory) {
        this.dataStore = dataStore;
        this.source = featureSource;
        this.attributeFactory = attributeFactory;
        
        isoFeatureType = new ISOFeatureTypeAdapter(source.getSchema());
        TypeFactory tf = new TypeFactoryImpl();
        descriptor = tf.createAttributeDescriptor(isoFeatureType,
                isoFeatureType.getName(), 0, Integer.MAX_VALUE, true);
    }

    public void addFeatureListener(FeatureListener listener) {
        source.addFeatureListener(listener);
    }

    public Envelope getBounds() throws IOException {
        return source.getBounds();
    }

    public Envelope getBounds(Query query) throws IOException {
        return source.getBounds(query);
    }

    public int getCount(Query query) throws IOException {
        return source.getCount(query);
    }

    public DataStore getDataStore() {
        return this.dataStore;
    }

    public FeatureCollection getFeatures(Query query) throws IOException {
        return source.getFeatures(query);
    }

    public FeatureCollection getFeatures(Filter filter) throws IOException {
        return source.getFeatures(filter);
    }

    public FeatureCollection getFeatures() throws IOException {
        return source.getFeatures();
    }

    public FeatureType getSchema() {
        return source.getSchema();
    }

    public void removeFeatureListener(FeatureListener listener) {
        source.removeFeatureListener(listener);
    }

    // ////////////////////////////////////////

    public Collection content() {
        return content(Filter.INCLUDE);
    }

    public Collection content(String query, String queryLanguage) {
        throw new UnsupportedOperationException();
    }

    public Collection content(Filter filter) {
        FeatureCollection features;
        try {
            features = source.getFeatures(filter);
        } catch (IOException e) {
            throw (RuntimeException) new RuntimeException().initCause(e);
        }
        Collection isoFeatures = new FeatureCollectionAdapter(isoFeatureType,
                features, attributeFactory);
        return isoFeatures;
    }

    public Object describe() {
        return descriptor;
    }

    public void dispose() {
        // TODO: what to do?
    }

    public FilterCapabilities getFilterCapabilities() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public GeoResourceInfo getInfo() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public Name getName() {
        FeatureType gtType = source.getSchema();
        URI namespace = gtType.getNamespace();
        String nsUri = namespace == null ? null : namespace.toString();
        String name = gtType.getTypeName();

        Name typeName = new org.geotools.feature.Name(nsUri, name);
        return typeName;
    }

    public void setTransaction(Transaction t) {
        // DO NOTHING BY NOW
    }

}
