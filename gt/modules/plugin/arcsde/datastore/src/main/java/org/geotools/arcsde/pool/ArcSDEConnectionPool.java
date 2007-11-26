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

import java.util.ArrayList;
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

/**
 * Maintains <code>SeConnection</code>'s for a single set of connection
 * properties (for instance: by server, port, user and password) in a pooled way
 * 
 * <p>
 * Since sde connections are not jdbc connections, I can't use Sean's excellent
 * connection pool. So I'll borrow most of it.
 * </p>
 * 
 * <p>
 * This connection pool is configurable in the sense that some parameters can be
 * passed to establish the pooling policy. To pass parameters to the connection
 * pool, you should set some properties in the parameters Map passed to
 * SdeDataStoreFactory.createDataStore, wich will invoke
 * SdeConnectionPoolFactory to get the SDE instance's pool singleton. That
 * instance singleton will be created with the preferences passed the first time
 * createDataStore is called for a given SDE instance/user, if subsecuent calls
 * change that preferences, they will be ignored.
 * </p>
 * 
 * <p>
 * The expected optional parameters that you can set up in the argument Map for
 * createDataStore are:
 * 
 * <ul>
 * <li> pool.minConnections Integer, tells the minimun number of open
 * connections the pool will maintain opened </li>
 * <li> pool.maxConnections Integer, tells the maximun number of open
 * connections the pool will create and maintain opened </li>
 * <li> pool.timeOut Integer, tells how many milliseconds a calling thread is
 * guaranteed to wait before getConnection() throws an
 * UnavailableArcSDEConnectionException </li>
 * </ul>
 * </p>
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 */
public class ArcSDEConnectionPool {
    /** package's logger */
    private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geotools.arcsde.pool");

    /** DOCUMENT ME! */
    protected static final Level INFO_LOG_LEVEL = Level.WARNING;

    /** default number of connections a pool creates at first population */
    public static final int DEFAULT_CONNECTIONS = 2;

    /** default number of maximun allowable connections a pool can hold */
    public static final int DEFAULT_MAX_CONNECTIONS = 2;

    public static final int DEFAULT_MAX_WAIT_TIME = 1000;

    /** DOCUMENT ME! */
    private SeConnectionFactory seConnectionFactory;

    /** this connection pool connection's parameters */
    private ArcSDEConnectionConfig config;

    /** Apache commons-pool used to pool arcsde connections*/
    private ObjectPool pool;

    /**
     * Creates a new SdeConnectionPool object with the connection parameters
     * holded by <code>config</code>
     * 
     * @param config
     *            holds connection options such as server, user and password, as
     *            well as tuning options as maximun number of connections
     *            allowed
     * 
     * @throws DataSourceException
     *             DOCUMENT ME!
     * @throws NullPointerException
     *             DOCUMENT ME!
     */
    protected ArcSDEConnectionPool(ArcSDEConnectionConfig config) throws DataSourceException {
        if (config == null) {
            throw new NullPointerException("parameter config can't be null");
        }

        this.config = config;
        LOGGER.fine("populating ArcSDE connection pool");

        this.seConnectionFactory = new SeConnectionFactory(this.config);

        int minConnections = config.getMinConnections().intValue();
        int maxConnections = config.getMaxConnections().intValue();
        // byte exhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_GROW;
        byte exhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_BLOCK;
        long maxWait = config.getConnTimeOut().longValue();

        this.pool = new GenericObjectPool(seConnectionFactory, maxConnections, exhaustedAction,
                maxWait, true, true);
        LOGGER.info("Created ArcSDE connection pool for " + config);

        ArcSDEPooledConnection[] preload = new ArcSDEPooledConnection[minConnections];

        try {
            for (int i = 0; i < minConnections; i++) {
                preload[i] = (ArcSDEPooledConnection) this.pool.borrowObject();
            }

            for (int i = 0; i < minConnections; i++) {
                this.pool.returnObject(preload[i]);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "can't connect to " + config, e);
            throw new DataSourceException(e);
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
        if (pool != null) {
            try {
                this.pool.close();
                pool = null;
                LOGGER.fine("SDE connection pool closed. ");
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Closing pool: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Ensures proper closure of connection pool at this object's finalization
     * stage.
     */
    protected void finalize() {
        close();
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
     * @throws DataSourceException
     *             DOCUMENT ME!
     * @throws UnavailableArcSDEConnectionException
     * @throws IllegalStateException
     *             DOCUMENT ME!
     */
    public ArcSDEPooledConnection getConnection() throws DataSourceException,
            UnavailableArcSDEConnectionException {
        if (pool == null) {
            throw new IllegalStateException("The ConnectionPool has been closed.");
        }

        try {
//            String caller = null;
//            if(LOGGER.isLoggable(Level.FINER)){
//                StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
//                caller = stackTrace[3].getClassName() + "." + stackTrace[3].getMethodName();
//                System.err.print("-> " + caller);
//            }
            
            ArcSDEPooledConnection ret = (ArcSDEPooledConnection) this.pool.borrowObject();
            
            if(LOGGER.isLoggable(Level.FINER)){
                //System.err.println(" got " + ret);
                LOGGER.finer(ret + " out of connection pool");
            }

            return ret;
        } catch (NoSuchElementException e) {
            LOGGER.log(Level.WARNING, "Out of connections: " + e.getMessage(), e);
            throw new UnavailableArcSDEConnectionException(this.pool.getNumActive(), this.config);
        } catch (SeException se) {
            LOGGER.log(Level.WARNING, "ArcSDE error getting connection: "
                    + se.getSeError().getErrDesc(), se);
            throw new DataSourceException("ArcSDE Error Message: " + se.getSeError().getErrDesc(),
                    se);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Unknown problem getting connection: " + e.getMessage(), e);
            throw new DataSourceException(
                    "Unknown problem fetching connection from connection pool", e);
        }
    }
       
    /**
     * Sometimes (and largely without reason) ArcSDEPooledConnections (really their underlying SeConnection objects)
     * just poop out.  They start behaving strangely, or not behaving at all.  You can tell the pool that a particular
     * SeConnection has 'Failed' using this method, and it will do its best to get it out of the pool as soon as you
     * release your hold on it.
     * 
     * @param conn
     */
    public synchronized void markConnectionAsFailed(ArcSDEPooledConnection conn) {
        LOGGER.warning("ArcSDE connection '" + conn + "' has been marked as failed.  Current pool state is " + getAvailableCount() + " avail/" + this.getPoolSize() + " total");
        seConnectionFactory.markObjectInvalid(conn);
    }

 
    /**
     * Gets the list of available layer names on the database
     * 
     * @return a <code>List&lt;String&gt;</code> with the registered
     *         featureclasses on the ArcSDE database
     * 
     * @throws DataSourceException
     */
    @SuppressWarnings("unchecked")
    public List<String> getAvailableLayerNames() throws DataSourceException {
        ArcSDEPooledConnection conn = null;

        List<String> layerNames = new LinkedList<String>();
        try {
            conn = getConnection();
            for (Iterator<SeLayer> it = conn.getLayers().iterator(); it.hasNext();) {
                layerNames.add(it.next().getQualifiedName());
            }
        } catch (SeException ex) {
            throw new DataSourceException("Error querying the layers list"
                    + ex.getSeError().getSdeError() + " (" + ex.getSeError().getErrDesc() + ") ",
                    ex);
        } catch (UnavailableArcSDEConnectionException ex) {
            throw new DataSourceException("No free connection found to query the layers list", ex);
        } finally {
            if (conn != null)
                conn.close();
        }
        return layerNames;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public ArcSDEConnectionConfig getConfig() {
        return this.config;
    }

    /**
     * Inner utility class to report the configuration of the ArcSDE service and
     * the underlying RDBMS pointed by a <code>ArcSDEConnectionConfig</code>
     * object.
     * 
     * @author Gabriel Roldan, Axios Engineering
     * @version $Id: ArcSDEConnectionPool.java 25767 2007-06-07 10:33:44Z
     *          groldan $
     */
    private static class SeConfigReport {
        /**
         * Reports the configuration of the ArcSDE version and DBMS information
         * to the logging system, at connection pool's startup, with INFO
         * logging level.
         * 
         * @param config
         *            DOCUMENT ME!
         * 
         * @throws DataSourceException
         *             if a SeException is thrown by the ArcSDE Java API while
         *             trying to fetch the server information.
         */
        static void reportConfiguration(ArcSDEConnectionConfig config) throws DataSourceException {
            try {
                SeInstance instanceInfo = new SeInstance(config.getServerName(), config
                        .getPortNumber().intValue());

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
                sb.append(iconf.getMaxStreams()
                        + " (maximum number of streams allowed by the ArcSde instance)");
                sb.append("\nStream pool size: ");
                sb.append(iconf.getStreamPoolSize()
                        + " (maximum number of streams allowed in a native pool.)");

                sb.append("\n**************************");
                LOGGER.log(INFO_LOG_LEVEL, sb.toString());
            } catch (SeException e) {
                throw new DataSourceException("Error fetching information from " + " the server "
                        + config.getServerName() + ":" + config.getPortNumber());
            }
        }
    }

    /**
     * PoolableObjectFactory intended to be used by a Jakarta's commons-pool
     * objects pool, that provides ArcSDE's SeConnections.
     * 
     * @author Gabriel Roldan, Axios Engineering
     * @version $Id: ArcSDEConnectionPool.java 25767 2007-06-07 10:33:44Z
     *          groldan $
     */
    class SeConnectionFactory extends BasePoolableObjectFactory {
        /** DOCUMENT ME! */
        private ArcSDEConnectionConfig config;
        
        private List<SeConnection> invalidConnections = new ArrayList<SeConnection>(2);

        /**
         * Creates a new SeConnectionFactory object.
         * 
         * @param config
         *            DOCUMENT ME!
         */
        public SeConnectionFactory(ArcSDEConnectionConfig config) {
            super();
            this.config = config;
        }
        
        public void markObjectInvalid(Object o) {
            invalidConnections.add((SeConnection) o);
        }
        /**
         * Called whenever a new instance is needed.
         * 
         * @return a newly created <code>SeConnection</code>
         * 
         * @throws SeException
         *             if the connection can't be created
         */
        public Object makeObject() throws SeException, DataSourceException {
            NegativeArraySizeException cause = null;
            for (int i = 0; i < 3; i++) {
                try {
                    ArcSDEPooledConnection seConn = new ArcSDEPooledConnection(
                            ArcSDEConnectionPool.this.pool, config);
                    return seConn;
                } catch (NegativeArraySizeException nase) {
                    LOGGER.warning("Strange failed ArcSDE connection error.  Trying again (try "
                            + (i + 1) + " of 3)");
                    cause = nase;
                }
            }
            throw new DataSourceException(
                    "Couldn't create ArcSDEPooledConnection because of strange SDE internal exception.  Tried 3 times, giving up.",
                    cause);
        }

        /**
         * is invoked on every instance before it is returned from the pool.
         * 
         * @param obj
         */
        public void activateObject(Object obj) {
            // no-op
            LOGGER.finest("activating connection " + obj);
        }

        /**
         * is invoked in an implementation-specific fashion to determine if an
         * instance is still valid to be returned by the pool. It will only be
         * invoked on an "activated" instance.
         * 
         * @param an instance of {@link ArcSDEPooledConnection} maintained by
         *            this pool.
         * 
         * @return <code>true</code> if the connection is still alive and
         *            operative (checked by asking its user name), <code>false</code>
         *            otherwise.
         */
        public boolean validateObject(Object obj) {
            ArcSDEPooledConnection conn = (ArcSDEPooledConnection) obj;
            boolean valid = !conn.isClosed();
            // MAKE PROPER VALIDITY CHECK HERE as for GEOT-1273
            if (valid) {
                if (invalidConnections.contains(obj))
                    valid = false;
                
                try {
                    LOGGER.finest("Validating SDE Connection");
                    String user = conn.getUser();
                    LOGGER.finer("Connection validated, returned user " + user);
                } catch (SeException e) {
                    LOGGER.info("Can't validate SeConnection, discarding it: " + conn);
                    valid = false;
                }
            }
            return valid;
        }

        /**
         * is invoked on every instance when it is being "dropped" from the pool
         * (whether due to the response from validateObject, or for reasons
         * specific to the pool implementation.)
         * 
         * @param obj
         *            an instance of {@link ArcSDEPooledConnection} maintained
         *            by this pool.
         */
        public void destroyObject(Object obj) {
            ArcSDEPooledConnection conn = (ArcSDEPooledConnection) obj;
            conn.destroy();
        }
    }
    
    public String toString() {
        StringBuffer ret = new StringBuffer();
        ret.append("[ACTIVE: ");
        ret.append(pool.getNumActive() + "/" + ((GenericObjectPool)pool).getMaxActive());
        ret.append("  INACTIVE: ");
        ret.append(pool.getNumIdle() + "/" + ((GenericObjectPool)pool).getMaxIdle() + "]");
        return ret.toString();
    }
}