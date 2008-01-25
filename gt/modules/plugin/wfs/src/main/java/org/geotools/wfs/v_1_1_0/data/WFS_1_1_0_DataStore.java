/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2008, GeoTools Project Managment Committee (PMC)
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

package org.geotools.wfs.v_1_1_0.data;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.data.LockingManager;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.view.DefaultView;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.data.wfs.WFSServiceInfo;
import org.geotools.feature.SchemaException;
import org.geotools.util.logging.Logging;
import org.geotools.wfs.WFSConfiguration;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

/**
 * A WFS 1.1 DataStore implementation.
 * 
 * @author Gabriel Roldan
 * @version $Id$
 * @since 2.5.x
 * @URL $URL:
 *      http://svn.geotools.org/geotools/trunk/gt/modules/plugin/wfs/src/main/java/org/geotools/wfs/v_1_1_0/data/WFSDataStore.java $
 */
public final class WFS_1_1_0_DataStore implements WFSDataStore {
    private static final Logger LOGGER = Logging.getLogger("org.geotools.data.wfs");

    private static WFSConfiguration configuration;

    private final Map<String, SimpleFeatureType> featureTypeCache;

    private WFS110ProtocolHandler protocolHandler;

    /**
     * The WFS capabilities document.
     * 
     * @param capabilities
     */
    @SuppressWarnings("unchecked")
    public WFS_1_1_0_DataStore(final WFS110ProtocolHandler connectionFactory) {
        this.protocolHandler = connectionFactory;
        this.featureTypeCache = new HashMap<String, SimpleFeatureType>();
        synchronized (WFS_1_1_0_DataStore.class) {
            if (configuration == null) {
                configuration = new WFSConfiguration();
            }
        }
    }

    public WFSServiceInfo getInfo() {
        return new CapabilitiesServiceInfo(protocolHandler);
    }

    /**
     * @param
     * @return the GeoTools FeatureType for the {@code typeName} as stated on
     *         the capabilities document.
     * @see org.geotools.data.DataStore#getSchema(java.lang.String)
     */
    public SimpleFeatureType getSchema(final String typeName) throws IOException {
        if (featureTypeCache.containsKey(typeName)) {
            return featureTypeCache.get(typeName);
        }

        SimpleFeatureType ftype = protocolHandler.parseDescribeFeatureType(typeName);
        synchronized (featureTypeCache) {
            featureTypeCache.put(typeName, ftype);
        }
        return ftype;

    }

    /**
     * @see org.geotools.data.DataStore#getTypeNames()
     */
    public String[] getTypeNames() throws IOException {
        return protocolHandler.getCapabilitiesTypeNames();
    }

    /**
     * @see org.geotools.data.DataStore#dispose()
     */
    public void dispose() {
        if (protocolHandler != null) {
            protocolHandler = null;
        }
    }

    /**
     * @see org.geotools.data.DataStore#getFeatureReader(org.geotools.data.Query,
     *      org.geotools.data.Transaction)
     */
    public FeatureReader getFeatureReader(Query query, Transaction transaction) throws IOException {
        String typeName = query.getTypeName();
        SimpleFeatureType featureType = getSchema(typeName);
        return protocolHandler.getFeatureReader(featureType, query, transaction);
    }

    /**
     * @see org.geotools.data.DataStore#getFeatureSource(java.lang.String)
     */
    public WFSFeatureSource getFeatureSource(final String typeName)
            throws IOException {
        return new WFSFeatureSource(this, typeName, protocolHandler);
    }

    /**
     * @return {@code null}, no lock support so far
     * @see org.geotools.data.DataStore#getLockingManager()
     */
    public LockingManager getLockingManager() {
        return null;
    }

    /**
     * @see org.geotools.data.DataStore#getView(org.geotools.data.Query)
     * @see DefaultView
     */
    public FeatureSource getView(final Query query) throws IOException, SchemaException {
        final String typeName = query.getTypeName();
        final FeatureSource featureSource = this.getFeatureSource(typeName);
        return new DefaultView(featureSource, query);
    }

    /**
     * Not supported.
     * 
     * @see org.geotools.data.DataStore#getFeatureWriter(java.lang.String,
     *      org.opengis.filter.Filter, org.geotools.data.Transaction)
     * @throws UnsupportedOperationException
     *             always since this operation does not apply to a WFS backend
     */
    public FeatureWriter getFeatureWriter(String typeName, Filter filter, Transaction transaction)
            throws IOException {
        throw new UnsupportedOperationException("This is a read only DataStore");
    }

    /**
     * Not supported.
     * 
     * @see org.geotools.data.DataStore#getFeatureWriter(java.lang.String,
     *      org.geotools.data.Transaction)
     * @throws UnsupportedOperationException
     *             always since this operation does not apply to a WFS backend
     */
    public FeatureWriter getFeatureWriter(String typeName, Transaction transaction)
            throws IOException {
        throw new UnsupportedOperationException("This is a read only DataStore");
    }

    /**
     * Not supported.
     * 
     * @see org.geotools.data.DataStore#getFeatureWriterAppend(java.lang.String,
     *      org.geotools.data.Transaction)
     * @throws UnsupportedOperationException
     *             always since this operation does not apply to a WFS backend
     */
    public FeatureWriter getFeatureWriterAppend(String typeName, Transaction transaction)
            throws IOException {
        throw new UnsupportedOperationException("This is a read only DataStore");
    }

    /**
     * @see org.geotools.data.DataStore#updateSchema(java.lang.String,
     *      org.opengis.feature.simple.SimpleFeatureType)
     * @throws UnsupportedOperationException
     *             always since this operation does not apply to a WFS backend
     */
    public void updateSchema(String typeName, SimpleFeatureType featureType) throws IOException {
        throw new UnsupportedOperationException("WFS DataStore does not supports updateSchema");
    }

    /**
     * @see org.geotools.data.DataStore#createSchema(org.opengis.feature.simple.SimpleFeatureType)
     * @throws UnsupportedOperationException
     *             always since this operation does not apply to a WFS backend
     */
    public void createSchema(SimpleFeatureType featureType) throws IOException {
        throw new UnsupportedOperationException("WFS DataStore does not support createSchema");
    }
}
