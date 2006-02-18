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
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.geotools.data.DataSourceException;

import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeInstance;
import com.esri.sde.sdk.client.SeLayer;
import com.esri.sde.sdk.client.SeRelease;
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
 * be passed to establish the pooling policy. To pass parameters to the
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
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 */
public class ArcSDEConnectionPool {
    /** package's logger */
    protected static final Logger LOGGER = Logger.getLogger(ArcSDEConnectionPool.class.getPackage()
                                                                                      .getName()
                                                           );

    /** DOCUMENT ME! */
    protected static final Level INFO_LOG_LEVEL = Level.WARNING;

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

    /** DOCUMENT ME! */
    private SeConnectionFactory seConnectionFactory;

    /** this connection pool connection's parameters */
    private ConnectionConfig config;

    /** DOCUMENT ME! */
    private ObjectPool pool;

    /**
     * Holds a cache of SeLayers, which is cleared every time the connection
     * used to create the SeLayer objects is closed.
     */
    private HashMap cachedLayers;

    /**
     * The connection used to retrieve the list of SeLayers, holded to compare
     * it upon the one recieved in <code>destroyObject</code> and clear the
     * layers cache if we're going to close it.
     */
    private SeConnection layerCacheConnection;

    /**
     * Indicates that this Connection Pool is closed and it should not return
     * connections on calls to getConnection()
     */
    private boolean closed = false;

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

        this.config = config;
        this.cachedLayers = new HashMap();
        LOGGER.fine("populating ArcSDE connection pool");

        this.seConnectionFactory = new SeConnectionFactory(this.config);

        int minConnections = config.getMinConnections().intValue();
        int maxConnections = config.getMaxConnections().intValue();
        //byte exhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_GROW;
		byte exhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_BLOCK;
        long maxWait = config.getConnTimeOut().longValue();

        this.pool = new GenericObjectPool(seConnectionFactory, maxConnections,
                                          exhaustedAction, maxWait);
        LOGGER.info("Created pool " + pool);

        SeConnection[] preload = new SeConnection[minConnections];

        try {
            for (int i = 0; i < minConnections; i++) {
                preload[i] = (SeConnection) this.pool.borrowObject();
            }

            for (int i = 0; i < minConnections; i++) {
                this.pool.returnObject(preload[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * returns the number of actual connections holded by this connection pool.
     * In other words, the sum of used and available connections, regardless
     *
     * @return DOCUMENT ME!
     */
    public int getPoolSize() {
        synchronized (this.pool) {
            return this.pool.getNumActive() + this.pool.getNumIdle();
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

        try {
            this.pool.returnObject(seConnection);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /**
     * closes all connections in this pool
     */
    public void close() {
        try {
            this.pool.close();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Closing pool: " + e.getMessage(), e);
        }

        this.closed = true;
        LOGGER.fine("SDE connection pool closed. ");
    }

    /**
     * TODO: Document this method!
     *
     * @return DOCUMENT ME!
     */
    public synchronized int getAvailableCount() {
        return this.pool.getNumIdle();
    }

    /**
     * TODO: Document this method!
     *
     * @return DOCUMENT ME!
     */
    public synchronized int getInUseCount() {
        return this.pool.getNumActive();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     * @throws UnavailableConnectionException
     * @throws IllegalStateException DOCUMENT ME!
     */
    public SeConnection getConnection()
                               throws DataSourceException, 
                                      UnavailableConnectionException {
        if (this.closed) {
            throw new IllegalStateException("The ConnectionPool has been closed.");
        }

        try {
            return (SeConnection) this.pool.borrowObject();
        } catch (NoSuchElementException e) {
            LOGGER.log(Level.WARNING, "Getting connection: " + e.getMessage(), e);
            throw new UnavailableConnectionException(this.pool.getNumActive(),
                                                     this.config
                                                    );
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Getting connection: " + e.getMessage(), e);
            throw new DataSourceException(e.getMessage(), e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws SeException DOCUMENT ME!
     */
    private SeConnection newConnection() throws SeException {
        SeConnection seConn = new SeConnection(this.config.getServerName(),
                                               this.config.getPortNumber()
                                                          .intValue(),
                                               this.config.getDatabaseName(),
                                               this.config.getUserName(),
                                               this.config.getUserPassword()
                                              );
        LOGGER.fine("created new connection " + seConn);

        seConn.setConcurrency(SeConnection.SE_UNPROTECTED_POLICY);

        // SeConnection.SeStreamSpec stSpec = seConn.getStreamSpec();

        /*
         * System.out.println("getMinBufSize=" + stSpec.getMinBufSize());
         * System.out.println("getMaxBufSize=" + stSpec.getMaxBufSize());
         * System.out.println("getMaxArraySize=" + stSpec.getMaxArraySize());
         * System.out.println("getMinObjects=" + stSpec.getMinObjects());
         * System.out.println("getAttributeArraySize=" +
         * stSpec.getAttributeArraySize());
         * System.out.println("getShapePointArraySize=" +
         * stSpec.getShapePointArraySize());
         * System.out.println("getStreamPoolSize=" +
         * stSpec.getStreamPoolSize());
         */
        /*
         * stSpec.setMinBufSize(1024 * 1024); stSpec.setMaxBufSize(10 * 1024 *
         * 1024); stSpec.setMaxArraySize(10000); stSpec.setMinObjects(1024);
         * stSpec.setAttributeArraySize(1024 * 1024);
         * stSpec.setShapePointArraySize(1024 * 1024);
         * stSpec.setStreamPoolSize(10);
         */
        /*
         * System.out.println("********************************************");
         * System.out.println("getMinBufSize=" + stSpec.getMinBufSize());
         * System.out.println("getMaxBufSize=" + stSpec.getMaxBufSize());
         * System.out.println("getMaxArraySize=" + stSpec.getMaxArraySize());
         * System.out.println("getMinObjects=" + stSpec.getMinObjects());
         * System.out.println("getAttributeArraySize=" +
         * stSpec.getAttributeArraySize());
         * System.out.println("getShapePointArraySize=" +
         * stSpec.getShapePointArraySize());
         * System.out.println("getStreamPoolSize=" +
         * stSpec.getStreamPoolSize());
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
            throw new DataSourceException("Can't obtain the table " +
                                          tableName + ": " + ex.getMessage(), ex
                                         );
        } catch (UnavailableConnectionException ex) {
            throw new DataSourceException("Can't obtain the table " +
                                          tableName + ": " + ex.getMessage(), ex
                                         );
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
        // attempt to get the SeLayer object from cache before creating a new
        // one, because creation is costly for SeLayer objects.
        SeLayer layer = (SeLayer) this.cachedLayers.get(typeName);

        // if the layer was not cached, create it
        if (layer == null) {
            // check for race condition
            if ((layer = (SeLayer) this.cachedLayers.get(typeName)) != null) {
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

            // cache the layer.
            if (layer != null) {
                this.cachedLayers.put(typeName, layer);
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
            throw new DataSourceException("Error querying the layers list" +
                                          ex.getSeError().getSdeError() + " (" +
                                          ex.getSeError().getErrDesc() + ") ",
                                          ex
                                         );
        } catch (UnavailableConnectionException ex) {
            throw new DataSourceException("No free connection found to query the layers list",
                                          ex
                                         );
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
        return this.closed;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public ConnectionConfig getConfig() {
        return this.config;
    }

    /**
     * Inner utility class to report the configuration of the ArcSDE service
     * and the underlying RDBMS pointed by a <code>ConnectionConfig</code>
     * object.
     *
     * @author Gabriel Roldan, Axios Engineering
     * @version $Id$
     */
    private static class SeConfigReport {
        /**
         * Reports the configuration of the ArcSDE version and DBMS information
         * to the logging system, at connection pool's startup, with INFO
         * logging level.
         *
         * @param config DOCUMENT ME!
         *
         * @throws DataSourceException if a SeException is thrown by the ArcSDE
         *         Java API while trying to fetch the server information.
         */
        static void reportConfiguration(ConnectionConfig config)
                                 throws DataSourceException {
            try {
                SeInstance instanceInfo = new SeInstance(config.getServerName(),
                                                         config.getPortNumber()
                                                               .intValue()
                                                        );

                if (!LOGGER.isLoggable(INFO_LOG_LEVEL)) {
                    return;
                }

                StringBuffer sb = new StringBuffer("***\nArcSDE configuration info:\n");

                sb.append("*** ArcSDE Server info: ****");
                sb.append("Server name: " + instanceInfo.getServerName());

                SeInstance.SeInstanceStatus status = instanceInfo.getStatus();
                SeRelease sdeRelease = status.getSeRelease();

                sb.append("\n ArcSDE version: ");
                sb.append(sdeRelease.getMajor());
                sb.append('.');
                sb.append(sdeRelease.getMinor());
                sb.append('.');
                sb.append(sdeRelease.getBugFix());
                sb.append(" - ");
                sb.append(sdeRelease.getDesc());

                sb.append("\nAccepting connections: ");
                sb.append(status.isAccepting());
                sb.append("\nBlocking connections: ");
                sb.append(status.isBlocking());

                SeInstance.SeInstanceConfiguration iconf = instanceInfo.getConfiguration();
                sb.append("\n---- Instance configuration: ----");
                sb.append("\nInstance is read-only: ");
                sb.append(iconf.getReadOnlyInstance());
                sb.append("\nHome path: ");
                sb.append(iconf.getHomePath());
                sb.append("\nLog path: ");
                sb.append(iconf.getLogPath());
                sb.append("\nMax. connections: ");
                sb.append(iconf.getMaxConnections());
                sb.append("\nMax. layers: ");
                sb.append(iconf.getMaxLayers());
                sb.append("\nMax. streams: ");
                sb.append(iconf.getMaxStreams() +
                          " (maximum number of streams allowed by the ArcSde instance)"
                         );
                sb.append("\nStream pool size: ");
                sb.append(iconf.getStreamPoolSize() +
                          " (maximum number of streams allowed in a native pool.)"
                         );

                sb.append("\n**************************");
                LOGGER.log(INFO_LOG_LEVEL, sb.toString());
            } catch (SeException e) {
                throw new DataSourceException("Error fetching information from " +
                                              " the server " +
                                              config.getServerName() + ":" +
                                              config.getPortNumber()
                                             );
            }
        }
    }

    /**
     * PoolableObjectFactory intended to be used by a Jakarta's commons-pool
     * objects pool, that provides ArcSDE's SeConnections.
     *
     * @author Gabriel Roldan, Axios Engineering
     * @version $Id
     */
    private class SeConnectionFactory extends BasePoolableObjectFactory {
        /** DOCUMENT ME! */
        private ConnectionConfig config;

        /**
         * Creates a new SeConnectionFactory object.
         *
         * @param config DOCUMENT ME!
         */
        public SeConnectionFactory(ConnectionConfig config) {
            super();
            this.config = config;
        }

        /**
         * Called whenever a new instance is needed.
         *
         * @return a newly created <code>SeConnection</code>
         *
         * @throws SeException if the connection can't be created
         */
        public Object makeObject() throws SeException {
            String server = config.getServerName();
            int port = config.getPortNumber().intValue();
            String database = config.getDatabaseName();
            String user = config.getUserName();
            String pwd = config.getUserPassword();

            SeConnection seConn = new SeConnection(server, port, database,
                                                   user, pwd
                                                  );
            LOGGER.fine("created new connection " + seConn);

            return seConn;
        }

        /**
         * is invoked on every instance before it is returned from the pool.
         *
         * @param obj
         */
        public void activateObject(Object obj) {
        	//no-op
        	LOGGER.finest("activating connection " + obj);
        }

        /**
         * is invoked in an implementation-specific fashion to determine if an
         * instance is still valid to be returned by the pool. It will only be
         * invoked on an "activated"  instance.
         *
         * @param obj DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public boolean validateObject(Object obj) {
            return (obj instanceof SeConnection) &&
                   !((SeConnection) obj).isClosed();
        }

        /**
         * is invoked on every instance when it is being "dropped" from the
         * pool (whether due to the response from validateObject, or for
         * reasons specific to the pool implementation.)
         *
         * @param obj DOCUMENT ME!
         */
        public void destroyObject(Object obj) {
            SeConnection conn = (SeConnection) obj;

            if (layerCacheConnection == conn) {
                LOGGER.info("Clearing the cache of SeLayers since the connection they hold is going to be removed from the pool");
                cachedLayers.clear();
                layerCacheConnection = null;
            }

            if (!conn.isClosed()) {
                LOGGER.info("Closing connection " + conn);

                try {
                    conn.close();
                } catch (SeException e) {
                    LOGGER.log(Level.SEVERE, "Can't close connection " + conn, e);
                }
            }
        }
    }
}