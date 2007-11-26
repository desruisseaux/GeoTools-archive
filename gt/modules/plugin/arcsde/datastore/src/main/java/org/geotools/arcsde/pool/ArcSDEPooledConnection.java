/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.arcsde.pool;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.pool.ObjectPool;
import org.geotools.data.DataSourceException;

import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeLayer;
import com.esri.sde.sdk.client.SeTable;

/**
 * An SeConnection that returns itself to the connection pool instead of closing
 * on each call to close().
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 * @since 2.3.x
 * 
 */
public class ArcSDEPooledConnection extends SeConnection {

    private static final Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger("org.geotools.arcsde.pool");

    private ObjectPool pool;

    private ArcSDEConnectionConfig config;

    private static int connectionCounter;

    private int connectionId;

    private boolean transactionInProgress;

    private Map<String, SeLayer> cachedLayers = new HashMap<String, SeLayer>();

    public ArcSDEPooledConnection(ObjectPool pool, ArcSDEConnectionConfig config)
            throws SeException {
        super(config.getServerName(), config.getPortNumber().intValue(), config.getDatabaseName(),
                config.getUserName(), config.getUserPassword());
        this.config = config;
        this.pool = pool;
        this.setConcurrency(SeConnection.SE_UNPROTECTED_POLICY);
        synchronized (ArcSDEPooledConnection.class) {
            connectionCounter++;
            connectionId = connectionCounter;
        }
    }

    public synchronized SeLayer getLayer(final String layerName) throws DataSourceException {
        if (!cachedLayers.containsKey(layerName)) {
            try {
                cacheLayers();
            } catch (SeException e) {
                throw new DataSourceException("Can't obtain layer " + layerName, e);
            }
        }
        return cachedLayers.get(layerName);
    }

    public synchronized SeTable getTable(final String tableName) throws DataSourceException {
        try {
            return new SeTable(this, tableName);
        } catch (SeException e) {
            throw new DataSourceException("Can't access table " + tableName, e);
        }
    }

    @SuppressWarnings("unchecked")
    private void cacheLayers() throws SeException {
        Vector<SeLayer> layers = this.getLayers();
        cachedLayers.clear();
        for (SeLayer layer : layers) {
            cachedLayers.put(layer.getQualifiedName(), layer);
        }
    }

    public void startTransaction() throws SeException {
        super.startTransaction();
        transactionInProgress = true;
    }

    public void commitTransaction() throws SeException {
        super.commitTransaction();
        transactionInProgress = false;
    }

    public boolean isTransactionActive() {
        return transactionInProgress;
    }

    public void rollbackTransaction() throws SeException {
        super.rollbackTransaction();
        transactionInProgress = false;
    }

    /**
     * Doesn't close the connection, but returns itself to the connection pool.
     * 
     * @throws IllegalStateException
     *             if close() is called while a transaction is in progress
     * @see #destroy()
     */
    public void close() throws IllegalStateException {
        if (transactionInProgress) {
            throw new IllegalStateException(
                    "Transaction is in progress, should commit or rollback before closing");
        }

        try {
            if (LOGGER.isLoggable(Level.FINER)) {
                System.err.println("<- returning " + toString() + " to pool");
            }
            this.pool.returnObject(this);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public String toString() {
        return "ArcSDEPooledConnection[" + connectionId + "]";
    }

    /**
     * Actually closes the connection
     */
    void destroy() {
        try {
            super.close();
        } catch (SeException e) {
            LOGGER.info("closing connection: " + e.getMessage());
        }
    }

    /**
     * Compares for reference equality
     */
    public boolean equals(Object other) {
        return other == this;
    }

    public int hashCode() {
        return 17 ^ this.config.hashCode();
    }

}
