/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2008, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.arcsde.data;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.arcsde.data.versioning.ArcSdeVersionHandler;
import org.geotools.arcsde.pool.ArcSDEConnectionPool;
import org.geotools.arcsde.pool.ArcSDEPooledConnection;
import org.geotools.data.DataSourceException;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.logging.Logging;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;

import com.vividsolutions.jts.geom.Envelope;

public class ArcSdeFeatureSource implements FeatureSource<SimpleFeatureType, SimpleFeature> {

    private static final Logger LOGGER = Logging.getLogger("org.geotools.arcsde.data");

    protected FeatureTypeInfo typeInfo;

    protected ArcSDEDataStore dataStore;

    private ArcSdeResourceInfo resourceInfo;

    protected ArcSdeVersionHandler versionHandler;

    public ArcSdeFeatureSource(final FeatureTypeInfo typeInfo,
                               final ArcSDEDataStore dataStore,
                               final ArcSdeVersionHandler versionHandler) {
        this.typeInfo = typeInfo;
        this.dataStore = dataStore;
        this.versionHandler = versionHandler;
    }

    /**
     * Returns the same name than the feature type (ie, {@code getSchema().getName()} to honor the
     * simple feature land common practice of calling the same both the Features produces and their
     * types
     * 
     * @since 2.5
     * @see FeatureSource#getName()
     */
    public Name getName() {
        return getSchema().getName();
    }

    /**
     * @see FeatureSource#getInfo()
     */
    public synchronized ArcSdeResourceInfo getInfo() {
        if (this.resourceInfo == null) {
            this.resourceInfo = new ArcSdeResourceInfo(this.typeInfo, this);
        }
        return this.resourceInfo;
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
     * @return The bounding box of the query or null if unknown and too expensive for the method to
     *         calculate or any errors occur.
     * @see FeatureSource#getBounds(Query)
     */
    public final ReferencedEnvelope getBounds(final Query query) throws IOException {
        final Query namedQuery = namedQuery(query);
        final ArcSDEPooledConnection connection = getConnection();
        ReferencedEnvelope ev;
        try {
            ev = getBounds(namedQuery, connection);
        } finally {
            if (!connection.isTransactionActive()) {
                connection.close();
            }
        }
        return ev;
    }

    /**
     * @param namedQuery
     * @param connection
     * @return The bounding box of the query or null if unknown and too expensive for the method to
     *         calculate or any errors occur.
     * @throws DataSourceException
     * @throws IOException
     */
    protected ReferencedEnvelope getBounds(final Query namedQuery,
            final ArcSDEPooledConnection connection) throws DataSourceException, IOException {
        Envelope ev;
        ev = ArcSDEQuery.calculateQueryExtent(connection, typeInfo, namedQuery, versionHandler);

        if (ev != null) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("ArcSDE optimized getBounds call returned: " + ev);
            }
            final ReferencedEnvelope envelope;
            final GeometryDescriptor defaultGeometry = getSchema().getDefaultGeometry();
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
            count = getCount(namedQuery, connection);
        } finally {
            if (!connection.isTransactionActive()) {
                connection.close();
            }
        }
        return count;
    }

    /**
     * @see FeatureSource#getCount(Query)
     */
    protected int getCount(final Query namedQuery, final ArcSDEPooledConnection connection)
            throws IOException {
        final int count;
        count = ArcSDEQuery.calculateResultCount(connection, typeInfo, namedQuery, versionHandler);
        return count;
    }

    /**
     * Returns a connection from the datastore's connection pool.
     * <p>
     * This is convenient way to get a connection for {@link #getBounds()} and
     * {@link #getCount(Query)}. {@link ArcSdeFeatureStore} overrides to get the connection from
     * the transaction instead of the pool.
     * </p>
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
        final String localName = typeInfo.getFeatureTypeName();
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
    public final ArcSDEDataStore getDataStore() {
        return dataStore;
    }

    /**
     * @see FeatureSource#getFeatures(Query)
     */
    public final FeatureCollection<SimpleFeatureType, SimpleFeature> getFeatures(final Query query)
            throws IOException {
        final Query namedQuery = namedQuery(query);
        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = new ArcSdeFeatureCollection(
                this, namedQuery);
        return collection;
    }

    /**
     * @see FeatureSource#getFeatures(Filter)
     */
    public final FeatureCollection<SimpleFeatureType, SimpleFeature> getFeatures(final Filter filter)
            throws IOException {
        DefaultQuery query = new DefaultQuery(typeInfo.getFeatureTypeName(), filter);
        return getFeatures(query);
    }

    /**
     * @see FeatureSource#getFeatures()
     */
    public final FeatureCollection<SimpleFeatureType, SimpleFeature> getFeatures()
            throws IOException {
        return getFeatures(Filter.INCLUDE);
    }

    /**
     * @see FeatureSource#getSchema();
     */
    public final SimpleFeatureType getSchema() {
        return typeInfo.getFeatureType();
    }

    /**
     * @return empty set
     * @see FeatureSource#getSupportedHints()
     */
    public final Set getSupportedHints() {
        return Collections.EMPTY_SET;
    }

    public ArcSdeVersionHandler getVersionHandler() {
        return versionHandler;
    }
}
