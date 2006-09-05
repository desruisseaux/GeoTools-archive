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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.pool.ObjectPool;

import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeException;

/**
 * An SeConnection that returns itself to the connection pool instead of
 * closing on each call to close().
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 * @since 2.3.x
 *
 */
class PooledConnection extends SeConnection {

	private static final Logger LOGGER = Logger.getLogger(PooledConnection.class.getPackage().getName());
	
	private ObjectPool pool;
	
	private ConnectionConfig config;
	
	public PooledConnection(ObjectPool pool, ConnectionConfig config) throws SeException {
		super(	config.getServerName(),
				config.getPortNumber().intValue(),
				config.getDatabaseName(),
				config.getUserName(),
				config.getUserPassword());
		this.config = config;
		this.pool = pool;
	}
	
	/**
	 * Doesn't close the connection, but returns itself to the
	 * connection pool.
	 * @see #destroy()
	 */
	public void close(){
        try {
            this.pool.returnObject(this);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
	}

	/**
	 * Actually closes the connection
	 */
	void destroy(){
		try{
			super.close();
		}catch(SeException e){
			LOGGER.info("closing connection: " + e.getMessage());
		}
	}

	/**
	 * Compares for reference equality
	 */
	public boolean equals(Object other){
		return other == this;
	}
	
	public int hashCode(){
		return 17 ^ this.config.hashCode();
	}
}
