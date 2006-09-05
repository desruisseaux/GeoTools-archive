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
package org.geotools.data.arcsde;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import org.geotools.data.DataSourceException;

/**
 * DOCUMENT ME!
 * 
 * @author Gabriel Rold?n
 * @source $URL$
 * @version $Id$
 */
class ConnectionPoolFactory {
	/** DOCUMENT ME! */
	private static Logger LOGGER = Logger.getLogger(ConnectionPoolFactory.class
			.getPackage().getName());

	/** DOCUMENT ME! */
	private static final ConnectionPoolFactory singleton =  new ConnectionPoolFactory();

	/** DOCUMENT ME! */
	private final Map currentPools = new HashMap();

	/**
	 * Creates a new SdeConnectionPoolFactory object.
	 */
	private ConnectionPoolFactory() {
		// intentionally blank
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public synchronized static ConnectionPoolFactory getInstance() {
		return singleton;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param config
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 * 
	 * @throws DataSourceException
	 *             DOCUMENT ME!
	 */
	public synchronized ArcSDEConnectionPool createPool(ConnectionConfig config)
			throws DataSourceException {
		ArcSDEConnectionPool pool = (ArcSDEConnectionPool) this.currentPools
				.get(config);

		if (pool == null) {
			// the new pool will be populated with config.minConnections
			// connections
			pool = new ArcSDEConnectionPool(config);
			this.currentPools.put(config, pool);
		}

		return pool;
	}

	/**
	 * DOCUMENT ME!
	 */
	public void clear() {
		closeAll();
		this.currentPools.clear();
		LOGGER.fine("sde connection pools creared");
	}

	/**
	 * DOCUMENT ME!
	 */
	public void closeAll() {
		for (Iterator it = this.currentPools.values().iterator(); it.hasNext();) {
			((ArcSDEConnectionPool) it.next()).close();
		}
	}

	/**
	 * DOCUMENT ME!
	 */
	public void finalize() {
		closeAll();
	}
}
