package org.geotools.arcsde.data;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import org.geotools.data.DataStore;
import org.geotools.data.DefaultFeatureResults;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

public class ArcSdeFeatureSource implements FeatureSource {

    protected SimpleFeatureType featureType;
    protected ArcSDEDataStore dataStore;
    protected Transaction transaction = Transaction.AUTO_COMMIT;

    public ArcSdeFeatureSource(final SimpleFeatureType featureType, final ArcSDEDataStore dataStore) {
        this.featureType = featureType;
        this.dataStore = dataStore;
    }

    public void addFeatureListener(FeatureListener listener) {
        dataStore.listenerManager.addFeatureListener(this, listener);
    }

    public void removeFeatureListener(FeatureListener listener) {
        dataStore.listenerManager.removeFeatureListener(this, listener);
    }

    public ReferencedEnvelope getBounds() throws IOException {
        final String typeName = featureType.getName().getLocalPart();
        final DefaultQuery query = new DefaultQuery(typeName);
        return dataStore.getBounds(query);
    }

    public ReferencedEnvelope getBounds(Query query) throws IOException {
        Query namedQuery = namedQuery(query);
        return dataStore.getBounds(namedQuery);
    }

    public int getCount(final Query query) throws IOException {
        Query namedQuery = namedQuery(query);
        return dataStore.getCount(namedQuery);
    }

    private Query namedQuery(final Query query) {
        final String localName = featureType.getName().getLocalPart();
        final String typeName = query.getTypeName();
        if (typeName != null && !localName.equals(typeName)) {
            throw new IllegalArgumentException("Wrong type name: " + typeName + " (this is "
                    + localName + ")");
        }
        DefaultQuery namedQuery = new DefaultQuery(query);
        namedQuery.setTypeName(localName);
        return namedQuery;
    }

    public DataStore getDataStore() {
        return dataStore;
    }

    public FeatureCollection getFeatures(Query query) throws IOException {
        FeatureCollection collection = new DefaultFeatureResults(this, query);
        return collection;
    }

    public FeatureCollection getFeatures(Filter filter) throws IOException {
        DefaultQuery query = new DefaultQuery(featureType.getTypeName(), filter);
        return getFeatures(query);
    }

    public FeatureCollection getFeatures() throws IOException {
        return getFeatures(Filter.INCLUDE);
    }

    public SimpleFeatureType getSchema() {
        return featureType;
    }

    public Set getSupportedHints() {
        return Collections.EMPTY_SET;
    }

}
