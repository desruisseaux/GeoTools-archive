/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *    
 *    Created on 20/10/2003
 */
package org.geotools.data.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.ConnectionEventListener;
import javax.sql.PooledConnection;

/**
 * @author geoghegs
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 * @source $URL$
 */
public class MockPooledConnection implements PooledConnection {
    private Connection conn;

    /**
     * 
     */
    public MockPooledConnection(Connection conn) {
        this.conn = conn;
    }

    /* (non-Javadoc)
     * @see javax.sql.PooledConnection#getConnection()
     */
    public Connection getConnection() throws SQLException {
        return conn;
    }

    /* (non-Javadoc)
     * @see javax.sql.PooledConnection#close()
     */
    public void close() throws SQLException {
        conn.close();
    }

    /* (non-Javadoc)
     * @see javax.sql.PooledConnection#addConnectionEventListener(javax.sql.ConnectionEventListener)
     */
    public void addConnectionEventListener(ConnectionEventListener listener) {
        
    }

    /* (non-Javadoc)
     * @see javax.sql.PooledConnection#removeConnectionEventListener(javax.sql.ConnectionEventListener)
     */
    public void removeConnectionEventListener(ConnectionEventListener listener) {
        
    }

}
