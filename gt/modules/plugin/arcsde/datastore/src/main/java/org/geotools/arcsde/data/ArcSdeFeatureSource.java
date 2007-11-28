package org.geotools.arcsde.data;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.arcsde.pool.ArcSDEConnectionPool;
import org.geotools.arcsde.pool.ArcSDEPooledConnection;
import org.geotools.data.DataStore;
import org.geotools.data.DefaultFeatureResults;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.logging.Logging;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;

import com.esri.sde.sdk.client.SeExtent;
import com.esri.sde.sdk.client.SeLayer;
import com.vividsolutions.jts.geom.Envelope;

public class ArcSdeFeatureSource implements FeatureSource {

    private static final Logger LOGGER = Logging.getLogger("org.geotools.arcsde.data");

    protected SimpleFeatureType featureType;
    protected ArcSDEDataStore dataStore;

    public ArcSdeFeatureSource(final SimpleFeatureType featureType, final ArcSDEDataStore dataStore) {
        this.featureType = featureType;
        this.dataStore = dataStore;
    }

    /**
     * @see FeatureSource#addFeatureListener(FeatureListener)
     */
    public final void addFeatureListener(final FeatureListener listener) {
        dataStore.listenerManager.addFeatureListener(this, listener);
    }

    /**
     * @see FeatureSource#removeFeatureListener(FeatureListener)
     */
    public final void removeFeatureListener(final FeatureListener listener) {
        dataStore.listenerManager.removeFeatureListener(this, listener);
    }

    /**
     * @see FeatureSource#getBounds()
     */
    public final ReferencedEnvelope getBounds() throws IOException {
        return getBounds(Query.ALL);
    }

    /**
     * @see FeatureSource#getBounds(Query)
     */
    public final ReferencedEnvelope getBounds(final Query query) throws IOException {
        Envelope ev;
        {
            final Query namedQuery = namedQuery(query);
            final String typeName = namedQuery.getTypeName();
            final ArcSDEPooledConnection connection = getConnection();
            try {
                if (query.getFilter().equals(Filter.INCLUDE)) {
                    LOGGER.finer("getting bounds of entire layer.  Using optimized SDE call.");
                    // we're really asking for a bounds of the WHOLE layer,
                    // let's just ask SDE metadata for that, rather than doing
                    // an
                    // expensive query
                    SeLayer thisLayer = connection.getLayer(typeName);
                    SeExtent extent = thisLayer.getExtent();
                    ev = new Envelope(extent.getMinX(), extent.getMaxX(), extent.getMinY(), extent
                            .getMaxY());
                } else {
                    ev = ArcSDEQuery.calculateQueryExtent(connection, featureType, namedQuery);
                }
            } finally {
                if (!connection.isTransactionActive()) {
                    connection.close();
                }
            }
        }
        if (ev != null) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("ArcSDE optimized getBounds call returned: " + ev);
            }
            final ReferencedEnvelope envelope;
            final GeometryDescriptor defaultGeometry = featureType.getDefaultGeometry();
            if (defaultGeometry == null) {
                envelope = ReferencedEnvelope.reference(ev);
            } else {
                envelope = new ReferencedEnvelope(ev, defaultGeometry.getCRS());
            }
            return envelope;
        }
        LOGGER.finer("ArcSDE couldn't process all filters in this query, "
                + "so optimized getBounds() returns null.");

        return null;
    }

    /**
     * @see FeatureSource#getCount(Query)
     */
    public final int getCount(final Query query) throws IOException {
        final Query namedQuery = namedQuery(query);
        final ArcSDEPooledConnection connection = getConnection();
        final int count;
        try {
            count = ArcSDEQuery.calculateResultCount(connection, featureType, namedQuery);
        } finally {
            if (!connection.isTransactionActive()) {
                connection.close();
            }
        }
        return count;
    }

    /**
     * convenient way to get a connection for {@link #getBounds()} and
     * {@link #getCount(Query)}. {@link TransactionFeatureWriter} overrides to
     * get the connection from the transaction instead of the pool.
     * 
     * @return
     * @throws IOException
     */
    protected ArcSDEPooledConnection getConnection() throws IOException {
        final ArcSDEConnectionPool connectionPool = dataStore.getConnectionPool();
        final ArcSDEPooledConnection connection = connectionPool.getConnection();
        return connection;
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

    /**
     * @see FeatureSource#getDataStore()
     */
    public final DataStore getDataStore() {
        return dataStore;
    }

    /**
     * @see FeatureSource#getFeatures(Query)
     */
    public FeatureCollection getFeatures(final Query query) throws IOException {
        FeatureCollection collection = new DefaultFeatureResults(this, query);
        return collection;
    }

    /**
     * @see FeatureSource#getFeatures(Filter)
     */
    public final FeatureCollection getFeatures(final Filter filter) throws IOException {
        DefaultQuery query = new DefaultQuery(featureType.getTypeName(), filter);
        return getFeatures(query);
    }

    /**
     * @see FeatureSource#getFeatures()
     */
    public final FeatureCollection getFeatures() throws IOException {
        return getFeatures(Filter.INCLUDE);
    }

    /**
     * @see FeatureSource#getSchema();
     */
    public final SimpleFeatureType getSchema() {
        return featureType;
    }

    /**
     * @return empty set
     * @see FeatureSource#getSupportedHints()
     */
    public final Set getSupportedHints() {
        return Collections.EMPTY_SET;
    }

}
