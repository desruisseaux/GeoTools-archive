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

import org.geotools.data.DataSourceException;

import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeInstance;
import com.esri.sde.sdk.client.SeLayer;
import com.esri.sde.sdk.client.SeRelease;
import com.esri.sde.sdk.client.SeTable;

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
 * passed to establish the pool policy. To pass parameters to the connection
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
 * <li>pool.minConnections Integer, tells the minimun number of open
 * connections the pool will maintain opened</li>
 * <li>pool.maxConnections Integer, tells the maximun number of open
 * connections the pool will create and maintain opened</li>
 * <li>pool.increment Integer, tells how many connections will be created at
 * once every time an available connection is not present and the maximin number
 * of allowed connections has not been reached</li>
 * <li>pool.timeOut Integer, tells how many milliseconds a calling thread is
 * guaranteed to wait before getConnection() throws an
 * UnavailableConnectionException</li>
 * </ul>
 * </p>
 * 
 * @author Gabriel Rold?n
 * @version $Id: ArcSDEConnectionPool.java,v 1.1 2004/06/21 15:00:33 cdillard
 *          Exp $
 */
public class ArcSDEConnectionPool {
	/** package's logger */
	protected static final Logger LOGGER = Logger
			.getLogger(ArcSDEConnectionPool.class.getPackage().getName());

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

	// START ADDED BY BROCK

	/** DOCUMENT ME! */
	private HashMap cachedLayers;

	// END ADDED BY BROCK

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
	protected ArcSDEConnectionPool(ConnectionConfig config)
			throws DataSourceException {
		if (config == null) {
			throw new NullPointerException("parameter config can't be null");
		}

		// START ADDED BY BROCK
		this.cachedLayers = new HashMap();

		// END ADDED BY BROCK
		this.config = config;
		LOGGER.fine("populating ArcSDE connection pool");

		synchronized (this.mutex) {
			populate();
		}

		LOGGER.fine("connection pool populated, added "
				+ this.availableConnections.size() + " connections");
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @throws DataSourceException
	 *             DOCUMENT ME!
	 */
	private void populate() throws DataSourceException {
		synchronized (this.mutex) {
			int minConnections = this.config.getMinConnections().intValue();
			final int actualCount = getPoolSize();

			if (actualCount == 0) {
				SeConfigReport.reportConfiguration(this.config);
			}

			int increment = (actualCount == 0) ? minConnections : this.config
					.getIncrement().intValue();
			int actual = 0;

			while ((actual++ < increment)
					&& (getPoolSize() < this.config.getMaxConnections()
							.intValue())) {
				try {
					SeConnection conn = newConnection();
					this.availableConnections.add(conn);
					LOGGER.fine("added connection to pool: " + conn);
				} catch (SeException ex) {
					throw new DataSourceException("Can't create connection to "
							+ this.config.getServerName() + ": "
							+ ex.getMessage(), ex);
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
		synchronized (this.mutex) {
			return this.usedConnections.size()
					+ this.availableConnections.size();
		}
	}

	/**
	 * This method does not just release a connection, it 'recycles' it, to make
	 * a completely new connection. This is due to a nasty problem with <i>some
	 * </i> arcsde instances on <i>some </i> datastores, only when spatial
	 * constraints are used. They seem to poison the connection. So this is a
	 * half decent work around, which probably slows things a bit, but also
	 * makes it work.
	 * 
	 * @param seConnection
	 *            DOCUMENT ME!
	 * 
	 * @throws DataSourceException
	 *             DOCUMENT ME!
	 */
	public void recycle(SeConnection seConnection) throws DataSourceException {
		if (seConnection == null) {
			LOGGER.fine("trying to recycle a null connection");

			return;
		}

		synchronized (this.mutex) {
			LOGGER.finer("trying to recycle seconnection: " + seConnection);
			LOGGER.finer("used is: " + this.usedConnections + "\navailable is "
					+ this.availableConnections);

			// added to force close
			try {
				seConnection.close();
			} catch (SeException sex) {
				LOGGER
						.fine("trouble closing seconnection: "
								+ sex.getMessage());
				sex.printStackTrace();
			}

			if (this.usedConnections.contains(seConnection)) {
				this.usedConnections.remove(seConnection);
			}

			if (this.availableConnections.contains(seConnection)) {
				LOGGER.fine("trying to recycle an already freed connection, "
						+ "getting rid of the free one...");
				this.availableConnections.remove(seConnection);
			}

			SeConnection newConnection = null;

			try {
				newConnection = newConnection();
				this.availableConnections.add(newConnection);
				LOGGER
						.fine("recycled new connection to pool: "
								+ newConnection);
			} catch (SeException ex) {
				throw new DataSourceException("Can't create connection to "
						+ this.config.getServerName() + ": " + ex.getMessage(),
						ex);
			}

			LOGGER.fine(seConnection + " freed" + ", added " + newConnection);
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param seConnection
	 *            DOCUMENT ME!
	 */
	public void release(SeConnection seConnection) {
		if (seConnection == null) {
			LOGGER.fine("trying to release a null connection");

			return;
		}

		synchronized (this.mutex) {
			LOGGER.fine("trying to release a seconnection: " + seConnection);
			LOGGER.finer("used is: " + this.usedConnections + "\navailable is "
					+ this.availableConnections);
			this.usedConnections.remove(seConnection);

			if (this.availableConnections.contains(seConnection)) {
				LOGGER.fine("trying to free an already freed connection...");
			} else {
				this.availableConnections.add(seConnection);
			}

			LOGGER.fine(seConnection + "freed, after release used is: "
					+ this.usedConnections + "\navailable is "
					+ this.availableConnections);
		}
	}

	/**
	 * closes all connections in this pool
	 */
	public void close() {
		synchronized (this.mutex) {
			int used = this.usedConnections.size();
			int available = this.availableConnections.size();

			for (int i = 0; i < used; i++) {
				SeConnection mPool = (SeConnection) this.usedConnections
						.removeFirst();

				try {
					mPool.close();
				} catch (SeException e) {
					LOGGER.warning("Failed to close in use PooledConnection: "
							+ e);
				}
			}

			for (int i = 0; i < available; i++) {
				SeConnection mPool = (SeConnection) this.availableConnections
						.removeFirst();

				try {
					mPool.close();
				} catch (SeException e) {
					LOGGER.warning("Failed to close free PooledConnection: "
							+ e);
				}
			}

			this.closed = true;
			LOGGER.fine("SDE connection pool closed. " + (used + available)
					+ " connections freed");
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 * 
	 * @throws DataSourceException
	 *             DOCUMENT ME!
	 * @throws UnavailableConnectionException
	 *             DOCUMENT ME!
	 */
	public SeConnection getConnection() throws DataSourceException,
			UnavailableConnectionException {
		return getConnection(true);
	}

	public int getAvailableCount(){
		return this.availableConnections.size();
	}
	
	public int getInUseCount(){
		return this.usedConnections.size();
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param waitIfNoneFree
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 * 
	 * @throws DataSourceException
	 *             DOCUMENT ME!
	 * @throws UnavailableConnectionException
	 * @throws IllegalStateException
	 *             DOCUMENT ME!
	 */
	public SeConnection getConnection(boolean waitIfNoneFree)
			throws DataSourceException, UnavailableConnectionException {
		if (this.closed) {
			throw new IllegalStateException(
					"The ConnectionPool has been closed.");
		}

		long timeWaited = 0;
		long timeOut = this.config.getConnTimeOut().intValue();

		synchronized (this.mutex) {
			try {
				if (this.availableConnections.size() == 0) {
					populate();
				}

				if (waitIfNoneFree) {
					while ((this.availableConnections.size() == 0)
							&& (timeWaited < timeOut)) {
						LOGGER.finer("waiting for connection...");
						this.mutex.wait(this.waitTime);
						timeWaited += this.waitTime;
					}

					if (timeWaited > 0) {
						LOGGER.fine("waited for connection for " + timeWaited
								+ "ms");
					}
				}

				if (this.availableConnections.size() > 0) {
					return getAvailable();
				}
				UnavailableConnectionException uce = new UnavailableConnectionException(
						this.usedConnections.size(), getConfig());
				Throwable trace = uce.fillInStackTrace();
				uce.setStackTrace(trace.getStackTrace());
				throw uce;

			} catch (InterruptedException ex) {
				throw new DataSourceException(
						"Interrupted while waiting for an available connection");
			} finally {
				this.mutex.notifyAll();
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

		SeConnection conn = (SeConnection) this.availableConnections
				.removeFirst();
		this.usedConnections.add(conn);
		LOGGER.fine(conn + " now in use");

		return conn;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 * 
	 * @throws DataSourceException
	 *             DOCUMENT ME!
	 */
	private SeConnection newPooledConnection() throws DataSourceException {
		int existents = this.availableConnections.size()
				+ this.usedConnections.size();

		// one never knows...
		if (existents >= this.config.getMaxConnections().intValue()) {
			throw new DataSourceException(
					"Maximun number of connections reached");
		}

		SeConnection connection = null;

		try {
			connection = newConnection();
			this.usedConnections.add(connection);
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
	 * @throws SeException
	 *             DOCUMENT ME!
	 */
	private SeConnection newConnection() throws SeException {
		SeConnection seConn = new SeConnection(this.config.getServerName(),
				this.config.getPortNumber().intValue(), this.config
						.getDatabaseName(), this.config.getUserName(),
				this.config.getUserPassword());
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
	 * @param tableName
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 * 
	 * @throws DataSourceException
	 *             DOCUMENT ME!
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
	 * @param typeName
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 * 
	 * @throws NoSuchElementException
	 *             DOCUMENT ME!
	 * @throws IOException
	 *             DOCUMENT ME!
	 */
	public SeLayer getSdeLayer(String typeName) throws NoSuchElementException,
			IOException {
		// attempt to get the SeLayer object from cache before creating a new
		// one, because creation is costly for SeLayer objects.
		SeLayer layer = (SeLayer) this.cachedLayers.get(typeName);

		// if the layer was not cached, create it
		if (layer == null) {
			synchronized (this.mutex) {
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
			throw new DataSourceException(
					"No free connection found to query the layers list", ex);
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
	 * Inner utility class to report the configuration of the ArcSDE service and
	 * the underlying RDBMS pointed by a <code>ConnectionConfig</code> object.
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
		 * @param config
		 *            DOCUMENT ME!
		 * 
		 * @throws DataSourceException
		 *             if a SeException is thrown by the ArcSDE Java API while
		 *             trying to fetch the server information.
		 */
		static void reportConfiguration(ConnectionConfig config)
				throws DataSourceException {
			try {
				SeInstance instanceInfo = new SeInstance(
						config.getServerName(), config.getPortNumber()
								.intValue());

				if (!LOGGER.isLoggable(INFO_LOG_LEVEL)) {
					return;
				}

				StringBuffer sb = new StringBuffer(
						"***\nArcSDE configuration info:\n");

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

				SeInstance.SeInstanceConfiguration iconf = instanceInfo
						.getConfiguration();
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
				sb
						.append(iconf.getMaxStreams()
								+ " (maximum number of streams allowed by the ArcSde instance)");
				sb.append("\nStream pool size: ");
				sb
						.append(iconf.getStreamPoolSize()
								+ " (maximum number of streams allowed in a native pool.)");

				sb.append("\n**************************");
				LOGGER.log(INFO_LOG_LEVEL, sb.toString());
			} catch (SeException e) {
				throw new DataSourceException(
						"Error fetching information from " + " the server "
								+ config.getServerName() + ":"
								+ config.getPortNumber());
			}
		}
	}
}
