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

    public static final int DEFAULT_MAX_WAIT_TIME = 1000;
    
    /** DOCUMENT ME! */
    private SeConnectionFactory seConnectionFactory;

    /** this connection pool connection's parameters */
    private ConnectionConfig config;

    /** DOCUMENT ME! */
    private ObjectPool pool;

    /**
     * Holds a cache of tablename/shapeColumn, for all the layers
     * visible for this pool's connections.
     */
    private HashMap /*<String,String>*/ cachedLayers;

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

        PooledConnection[] preload = new PooledConnection[minConnections];

        try {
            for (int i = 0; i < minConnections; i++) {
                preload[i] = (PooledConnection) this.pool.borrowObject();
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
    public PooledConnection getConnection()
                               throws DataSourceException, 
                                      UnavailableConnectionException {
        if (this.closed) {
            throw new IllegalStateException("The ConnectionPool has been closed.");
        }

        try {
            return (PooledConnection) this.pool.borrowObject();
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

    public SeTable getSdeTable(String tableName) throws DataSourceException {
    	PooledConnection conn;
    	try{
    		conn = getConnection();
    	}catch(UnavailableConnectionException e){
    		throw new DataSourceException(e);
    	}
        try {
            SeTable table = new SeTable(conn, tableName);

            return table;
        } catch (SeException ex) {
            throw new DataSourceException("Can't obtain the table " +
                                          tableName + ": " + ex.getMessage(), ex
                                         );
        }finally{
        	conn.close();
        }
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
    public SeTable getSdeTable(SeConnection conn, String tableName) throws DataSourceException {
        try {
            SeTable table = new SeTable(conn, tableName);

            return table;
        } catch (SeException ex) {
            throw new DataSourceException("Can't obtain the table " +
                                          tableName + ": " + ex.getMessage(), ex
                                         );
        }
    }

    public synchronized SeLayer getSdeLayer(String typeName)
    throws NoSuchElementException, IOException {
    	PooledConnection conn;
    	SeLayer layer;
    	
    	try {
			conn = getConnection();
		} catch (UnavailableConnectionException e) {
			throw new DataSourceException(e);
		}
		try{
			layer = getSdeLayer(conn, typeName);
		}finally{
			conn.close();
		}
    	return layer;
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
    public synchronized SeLayer getSdeLayer(SeConnection conn, String typeName)
                        throws NoSuchElementException, IOException {

    	SeLayer layer = null;

    	if(cachedLayers.containsKey(typeName)){
    		
    		String shapeColumn = (String)cachedLayers.get(typeName);
    		try {
				layer = new SeLayer(conn, typeName, shapeColumn);
			} catch (SeException e) {
				throw new DataSourceException("Getting layer " +  typeName, e);
			}
    		
    	}else{
    		List layers;
            try {
				layers = conn.getLayers();
                for (Iterator it = layers.iterator(); it.hasNext();) {
                    layer = (SeLayer) it.next();

                    if (layer.getQualifiedName().equalsIgnoreCase(typeName)) {
                        break;
                    }

                    layer = null;
                }
            } catch (SeException e) {
				throw new DataSourceException("Getting layer list: " + e.getMessage(), e);
            }

            if (layer == null) {
                throw new NoSuchElementException(typeName);
            }
            // cache the layer.
            this.cachedLayers.put(typeName, layer.getSpatialColumn());
    	}

    	return layer;
    }

    /**
     * Gets the list of available layer names on the database
     *
     * @return a <code>List&lt;String&gt;</code> with the registered
     *         featureclasses on the ArcSDE database
     *
     * @throws DataSourceException
     */
    public List /*<String>*/ getAvailableLayerNames() throws DataSourceException {
        PooledConnection conn = null;

        List layerNames = new LinkedList();
        try {
            conn = getConnection();
            for(Iterator it =  conn.getLayers().iterator(); it.hasNext();){
            	layerNames.add(((SeLayer)it.next()).getQualifiedName());
            }
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
            conn.close();
        }
        return layerNames;
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
    class SeConnectionFactory extends BasePoolableObjectFactory {
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
            PooledConnection seConn = new PooledConnection(ArcSDEConnectionPool.this.pool, config);
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
            boolean valid = (obj instanceof PooledConnection);
            valid = valid && !((PooledConnection) obj).isClosed();
            return valid;
        }

        /**
         * is invoked on every instance when it is being "dropped" from the
         * pool (whether due to the response from validateObject, or for
         * reasons specific to the pool implementation.)
         *
         * @param obj DOCUMENT ME!
         */
        public void destroyObject(Object obj) {
            PooledConnection conn = (PooledConnection) obj;
            conn.destroy();
        }
    }
}