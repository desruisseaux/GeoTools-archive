/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
package org.geotools.data.arcsde;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import org.geotools.data.DataSourceException;

import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeLayer;
import com.esri.sde.sdk.client.SeTable;


/**
 * Maintains <code>SeConnection</code>'s for a single set of connection
 * properties (for instance: by server, port, user and password) in a pooled
 * way
 * 
 * <p>
 * Since sde connections are not jdbc connections, I can't use Sean's excellent
 * connection pool. So I'll borrow most of it.
 * </p>
 * 
 * <p>
 * This connection pool is configurable in the sense that some parameters can
 * be passed to establish the pool policy. To pass parameters to the
 * connection pool, you should set some properties in the parameters Map
 * passed to SdeDataStoreFactory.createDataStore, wich will invoke
 * SdeConnectionPoolFactory to get the SDE instance's pool singleton. That
 * instance singleton will be created with the preferences passed the first
 * time createDataStore is called for a given SDE instance/user, if subsecuent
 * calls change that preferences, they will be ignored.
 * </p>
 * 
 * <p>
 * The expected optional parameters that you can set up in the argument Map for
 * createDataStore are:
 * 
 * <ul>
 * <li>
 * pool.minConnections Integer, tells the minimun number of open connections
 * the pool will maintain opened
 * </li>
 * <li>
 * pool.maxConnections Integer, tells the maximun number of open connections
 * the pool will create and maintain opened
 * </li>
 * <li>
 * pool.increment Integer, tells how many connections will be created at once
 * every time an available connection is not present and the maximin number of
 * allowed connections has not been reached
 * </li>
 * <li>
 * pool.timeOut Integer, tells how many milliseconds a calling thread is
 * guaranteed to wait before getConnection() throws an
 * UnavailableConnectionException
 * </li>
 * </ul>
 * </p>
 *
 * @author Gabriel Rold?n
 * @version $Id: ArcSDEConnectionPool.java,v 1.1 2004/06/21 15:00:33 cdillard Exp $
 */
public class ArcSDEConnectionPool {
    /** package's logger */
    private static final Logger LOGGER = Logger.getLogger(ArcSDEConnectionPool.class.getPackage()
                                                                                    .getName());

    /** default number of connections a pool creates at first population */
    public static final int DEFAULT_CONNECTIONS = 2;

    /** default number of maximun allowable connections a pool can hold */
    public static final int DEFAULT_MAX_CONNECTIONS = 2;

    /** default number of connections a pool increments by */
    public static final int DEFAULT_INCREMENT = 1;

    /**
     * default interval in milliseconds a calling thread waits for an available
     * connection
     */
    private static final long DEFAULT_WAIT_TIME = 1000;

    /**
     * default number of milliseconds a calling thread waits before
     * <code>getConnection</code> throws an <code>UnavailableException</code>
     */
    public static final int DEFAULT_MAX_WAIT_TIME = 10000;

    /**
     * number of milliseconds to wait in the wait loop until reach the timeout
     * period
     */
    private long waitTime = DEFAULT_WAIT_TIME;

    /** this connection pool connection's parameters */
    private ConnectionConfig config;

    /** list of SDE connections ready to use */
    private LinkedList availableConnections = new LinkedList();

    /** list of SDE connections actually in use */
    private LinkedList usedConnections = new LinkedList();

    /** A mutex for synchronizing */
    private Object mutex = new Object();

    /**
     * Indicates that this Connection Pool is closed and it should not return
     * connections on calls to getConnection()
     */
    private boolean closed = false;

    //    START ADDED BY BROCK

    /** DOCUMENT ME!  */
    private HashMap cachedLayers;

    //    END ADDED BY BROCK

    /**
     * Creates a new SdeConnectionPool object with the connection parameters
     * holded by <code>config</code>
     *
     * @param config holds connection options such as server, user and
     *        password, as well as tuning options as maximun number of
     *        connections allowed
     *
     * @throws DataSourceException DOCUMENT ME!
     * @throws NullPointerException DOCUMENT ME!
     */
    protected ArcSDEConnectionPool(ConnectionConfig config)
        throws DataSourceException {
        if (config == null) {
            throw new NullPointerException("parameter config can't be null");
        }

        //      START ADDED BY BROCK
        cachedLayers = new HashMap();

        //      END ADDED BY BROCK        
        this.config = config;
        LOGGER.fine("populating ArcSDE connection pool");

        synchronized (mutex) {
            populate();
        }

        LOGGER.fine("connection pool populated, added "
            + availableConnections.size() + " connections");
    }

    /**
     * DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    private void populate() throws DataSourceException {
        synchronized (mutex) {
            int minConnections = config.getMinConnections().intValue();
            int actualCount = getPoolSize();
            int increment = (actualCount == 0) ? minConnections
                                               : config.getIncrement().intValue();
            int actual = 0;

            while ((actual++ < increment)
                    && (getPoolSize() < config.getMaxConnections().intValue())) {
                try {
                    SeConnection conn = newConnection();
                    availableConnections.add(conn);
                    LOGGER.fine("added connection to pool: " + conn);
                } catch (SeException ex) {
                    throw new DataSourceException("Can't create connection to "
                        + config.getServerName() + ": " + ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * returns the number of actual connections holded by this connection pool.
     * In other words, the sum of used and available connections, regardless
     *
     * @return DOCUMENT ME!
     */
    public int getPoolSize() {
        synchronized (mutex) {
            return usedConnections.size() + availableConnections.size();
        }
    }

    /**
     * This method does not just release a connection, it 'recycles' it,  to
     * make a completely new connection.  This is due to a nasty problem with
     * <i>some</i> arcsde instances on <i>some</i> datastores, only when
     * spatial constraints are used.  They seem to poison the connection. So
     * this is a half decent work around, which probably slows things a bit,
     * but also makes it work.
     *
     * @param seConnection DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    public void recycle(SeConnection seConnection) throws DataSourceException {
        if (seConnection == null) {
            LOGGER.fine("trying to recycle a null connection");

            return;
        }

        synchronized (mutex) {
            LOGGER.finer("trying to recycle seconnection: " + seConnection);
            LOGGER.finer("used is: " + usedConnections + "\navailable is "
                + availableConnections);

            //added to force close
            try {
                seConnection.close();
            } catch (SeException sex) {
                LOGGER.fine("trouble closing seconnection: " + sex.getMessage());
                sex.printStackTrace();
            }

            if (usedConnections.contains(seConnection)) {
                usedConnections.remove(seConnection);
            }

            if (availableConnections.contains(seConnection)) {
                LOGGER.fine("trying to recycle an already freed connection, "
                    + "getting rid of the free one...");
                availableConnections.remove(seConnection);
            }

            SeConnection newConnection = null;

            try {
                newConnection = newConnection();
                availableConnections.add(newConnection);
                LOGGER.fine("recycled new connection to pool: " + newConnection);
            } catch (SeException ex) {
                throw new DataSourceException("Can't create connection to "
                    + config.getServerName() + ": " + ex.getMessage(), ex);
            }

            LOGGER.fine(seConnection + " freed" + ", added " + newConnection);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param seConnection DOCUMENT ME!
     */
    public void release(SeConnection seConnection) {
        if (seConnection == null) {
            LOGGER.fine("trying to release a null connection");

            return;
        }

        synchronized (mutex) {
            LOGGER.fine("trying to release a seconnection: " + seConnection);
            LOGGER.finer("used is: " + usedConnections + "\navailable is "
                + availableConnections);
            usedConnections.remove(seConnection);

            if (availableConnections.contains(seConnection)) {
                LOGGER.fine("trying to free an already freed connection...");
            } else {
                availableConnections.add(seConnection);
            }

            LOGGER.fine(seConnection + "freed, after release used is: "
                + usedConnections + "\navailable is " + availableConnections);
        }
    }

    /**
     * closes all connections in this pool
     */
    public void close() {
        synchronized (mutex) {
            int used = usedConnections.size();
            int available = availableConnections.size();

            for (int i = 0; i < used; i++) {
                SeConnection mPool = (SeConnection) usedConnections.removeFirst();

                try {
                    mPool.close();
                } catch (SeException e) {
                    LOGGER.warning("Failed to close in use PooledConnection: "
                        + e);
                }
            }

            for (int i = 0; i < available; i++) {
                SeConnection mPool = (SeConnection) availableConnections
                    .removeFirst();

                try {
                    mPool.close();
                } catch (SeException e) {
                    LOGGER.warning("Failed to close free PooledConnection: "
                        + e);
                }
            }

            closed = true;
            LOGGER.fine("SDE connection pool closed. " + (used + available)
                + " connections freed");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     * @throws UnavailableConnectionException DOCUMENT ME!
     */
    public SeConnection getConnection()
        throws DataSourceException, UnavailableConnectionException {
        return getConnection(true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param waitIfNoneFree DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     * @throws UnavailableConnectionException
     * @throws IllegalStateException DOCUMENT ME!
     */
    public SeConnection getConnection(boolean waitIfNoneFree)
        throws DataSourceException, UnavailableConnectionException {
        if (closed) {
            throw new IllegalStateException(
                "The ConnectionPool has been closed.");
        }

        long timeWaited = 0;
        long timeOut = config.getConnTimeOut().intValue();

        synchronized (mutex) {
            try {
                if (availableConnections.size() == 0) {
                    populate();
                }

                if (waitIfNoneFree) {
                    while ((availableConnections.size() == 0)
                            && (timeWaited < timeOut)) {
                        LOGGER.finer("waiting for connection...");
                        mutex.wait(waitTime);
                        timeWaited += waitTime;
                    }

                    if (timeWaited > 0) {
                        LOGGER.fine("waited for connection for " + timeWaited
                            + "ms");
                    }
                }

                if (availableConnections.size() > 0) {
                    return getAvailable();
                } else {
                    UnavailableConnectionException uce = new UnavailableConnectionException(usedConnections
                            .size(), getConfig());
                    Throwable trace = uce.fillInStackTrace();
                    uce.setStackTrace(trace.getStackTrace());
                    throw uce;
                }
            } catch (InterruptedException ex) {
                throw new DataSourceException(
                    "Interrupted while waiting for an available connection");
            } finally {
                mutex.notifyAll();
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private SeConnection getAvailable() {
        LOGGER.finest("Getting available connection.");

        SeConnection conn = (SeConnection) availableConnections.removeFirst();
        usedConnections.add(conn);
        LOGGER.fine(conn + " now in use");

        return conn;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    private SeConnection newPooledConnection() throws DataSourceException {
        int existents = availableConnections.size() + usedConnections.size();

        //one never knows...
        if (existents >= config.getMaxConnections().intValue()) {
            throw new DataSourceException(
                "Maximun number of connections reached");
        }

        SeConnection connection = null;

        try {
            connection = newConnection();
            usedConnections.add(connection);
        } catch (SeException ex) {
            throw new DataSourceException(
                "can't create a sde pooled connection: "
                + ex.getSeError().getSdeErrMsg(), ex);
        }

        return connection;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws SeException DOCUMENT ME!
     */
    private SeConnection newConnection() throws SeException {
        SeConnection seConn = new SeConnection(config.getServerName(),
                config.getPortNumber().intValue(), config.getDatabaseName(),
                config.getUserName(), config.getUserPassword());
        LOGGER.fine("created new connection " + seConn);
        seConn.setConcurrency(SeConnection.SE_UNPROTECTED_POLICY);

        // SeConnection.SeStreamSpec stSpec = seConn.getStreamSpec();

        /*
           System.out.println("getMinBufSize=" + stSpec.getMinBufSize());
           System.out.println("getMaxBufSize=" + stSpec.getMaxBufSize());
           System.out.println("getMaxArraySize=" + stSpec.getMaxArraySize());
           System.out.println("getMinObjects=" + stSpec.getMinObjects());
           System.out.println("getAttributeArraySize=" + stSpec.getAttributeArraySize());
           System.out.println("getShapePointArraySize=" + stSpec.getShapePointArraySize());
           System.out.println("getStreamPoolSize=" + stSpec.getStreamPoolSize());
         */
        /*
           stSpec.setMinBufSize(1024 * 1024);
           stSpec.setMaxBufSize(10 * 1024 * 1024);
           stSpec.setMaxArraySize(10000);
           stSpec.setMinObjects(1024);
           stSpec.setAttributeArraySize(1024 * 1024);
           stSpec.setShapePointArraySize(1024 * 1024);
           stSpec.setStreamPoolSize(10);
         */
        /*
           System.out.println("********************************************");
           System.out.println("getMinBufSize=" + stSpec.getMinBufSize());
           System.out.println("getMaxBufSize=" + stSpec.getMaxBufSize());
           System.out.println("getMaxArraySize=" + stSpec.getMaxArraySize());
           System.out.println("getMinObjects=" + stSpec.getMinObjects());
           System.out.println("getAttributeArraySize=" + stSpec.getAttributeArraySize());
           System.out.println("getShapePointArraySize=" + stSpec.getShapePointArraySize());
           System.out.println("getStreamPoolSize=" + stSpec.getStreamPoolSize());
         */
        return seConn;
    }

    /**
     * DOCUMENT ME!
     *
     * @param tableName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    public SeTable getSdeTable(String tableName) throws DataSourceException {
        SeConnection conn = null;

        try {
            conn = getConnection();

            SeTable table = new SeTable(conn, tableName);

            return table;
        } catch (SeException ex) {
            throw new DataSourceException("Can't obtain the table " + tableName
                + ": " + ex.getMessage(), ex);
        } catch (UnavailableConnectionException ex) {
            throw new DataSourceException("Can't obtain the table " + tableName
                + ": " + ex.getMessage(), ex);
        } finally {
            release(conn);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param typeName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws NoSuchElementException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     */
    public SeLayer getSdeLayer(String typeName)
        throws NoSuchElementException, IOException {
        //attempt to get the SeLayer object from cache before creating a new
        //one, because creation is costly for SeLayer objects.
        SeLayer layer = (SeLayer) cachedLayers.get(typeName);

        //if the layer was not cached, create it
        if (layer == null) {
            synchronized (mutex) {
                //check for race condition
                if ((layer = (SeLayer) cachedLayers.get(typeName)) != null) {
                    return layer;
                }

                List layers = getAvailableSdeLayers();

                try {
                    for (Iterator it = layers.iterator(); it.hasNext();) {
                        layer = (SeLayer) it.next();

                        if (layer.getQualifiedName().equalsIgnoreCase(typeName)) {
                            break;
                        }

                        layer = null;
                    }
                } catch (SeException ex) {
                    throw new NoSuchElementException(ex.getMessage());
                }

                //cache the layer.
                if (layer != null) {
                    cachedLayers.put(typeName, layer);
                }
            }
        }

        return layer;
    }

    /**
     * gets the list of available SeLayers on the database
     *
     * @return a <code>List&lt;SeLayer&gt;</code> with the registered
     *         featureclasses on the ArcSDE database
     *
     * @throws DataSourceException
     */
    public List getAvailableSdeLayers() throws DataSourceException {
        SeConnection conn = null;

        try {
            conn = getConnection();

            return conn.getLayers();
        } catch (SeException ex) {
            throw new DataSourceException("Error querying the layers list"
                + ex.getSeError().getSdeError() + " ("
                + ex.getSeError().getErrDesc() + ") ", ex);
        } catch (UnavailableConnectionException ex) {
            throw new DataSourceException("No free connection found to query the layers list",
                ex);
        } finally {
            release(conn);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public ConnectionConfig getConfig() {
        return config;
    }
}
