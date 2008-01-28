package org.geotools.wfs.v_1_1_0.data;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import org.geotools.data.DataStore;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.ResourceInfo;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

public class WFSFeatureSource implements FeatureSource, org.geotools.data.wfs.WFSFeatureSource {

    private String typeName;

    private WFS110ProtocolHandler protocolHandler;

    private WFS_1_1_0_DataStore dataStore;

    private SimpleFeatureType featureType;

    public WFSFeatureSource(final WFS_1_1_0_DataStore dataStore, final String typeName,
            final WFS110ProtocolHandler protocolHandler) throws IOException {
        this.typeName = typeName;
        this.dataStore = dataStore;
        this.protocolHandler = protocolHandler;
        this.featureType = dataStore.getSchema(typeName);
    }

    /**
     * @see FeatureSource#getDataStore()
     */
    public DataStore getDataStore() {
        return dataStore;
    }

    /**
     * @see FeatureSource#getSchema()
     */
    public SimpleFeatureType getSchema() {
        try {
            return dataStore.getSchema(typeName);
        } catch (IOException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    /**
     * Returns available metadata for this resource
     * 
     * @return
     */
    public ResourceInfo getInfo() {
        return new CapabilitiesResourceInfo(typeName, protocolHandler);
    }

    /**
     * @see FeatureSource#addFeatureListener(FeatureListener)
     */
    public void addFeatureListener(FeatureListener listener) {

    }

    /**
     * @see FeatureSource#removeFeatureListener(FeatureListener)
     */
    public void removeFeatureListener(FeatureListener listener) {
    }

    /**
     * @see FeatureSource#getBounds()
     */
    public ReferencedEnvelope getBounds() throws IOException {
        return getInfo().getBounds();
    }

    /**
     * @see FeatureSource#getBounds(Query)
     */
    public ReferencedEnvelope getBounds(Query query) throws IOException {
        Query namedQuery = namedQuery(typeName, query);
        return protocolHandler.getBounds(namedQuery);
    }

    /**
     * @see FeatureSource#getCount(Query)
     */
    public int getCount(Query query) throws IOException {
        Query namedQuery = namedQuery(typeName, query);
        return protocolHandler.getCount(namedQuery);
    }

    /**
     * @see FeatureSource#getFeatures(Filter)
     */
    public WFSFeatureCollection getFeatures(Filter filter) throws IOException {
        return getFeatures(new DefaultQuery(typeName, filter));
    }

    /**
     * @see FeatureSource#getFeatures()
     */
    public WFSFeatureCollection getFeatures() throws IOException {
        return getFeatures(new DefaultQuery(typeName));
    }

    /**
     * @see FeatureSource#getFeatures(Query)
     */
    public WFSFeatureCollection getFeatures(final Query query) throws IOException {
        Query namedQuery = namedQuery(typeName, query);
        return new WFSFeatureCollection(protocolHandler, namedQuery);
    }

    /**
     * @see FeatureSource#getSupportedHints()
     */
    @SuppressWarnings("unchecked")
    public Set getSupportedHints() {
        return Collections.EMPTY_SET;
    }

    private Query namedQuery(final String typeName, final Query query) {
        if (query.getTypeName() != null && !query.getTypeName().equals(typeName)) {
            throw new IllegalArgumentException("Wrong query type name: " + query.getTypeName()
                    + ". It should be " + typeName);
        }
        DefaultQuery named = new DefaultQuery(query);
        named.setTypeName(typeName);
        return named;
    }

}
