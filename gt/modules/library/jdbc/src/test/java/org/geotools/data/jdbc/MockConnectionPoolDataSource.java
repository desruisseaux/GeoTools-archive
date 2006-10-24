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

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;


/** A Mock ConnectionPoolDataSource for testing the JDBCDataStore.
 * 
 * @author Sean Geoghegan, Defence Science and Technology Organisation.
 * @source $URL$
 */
public class MockConnectionPoolDataSource implements ConnectionPoolDataSource {
    private Connection conn;

    /**
     * 
     */
    public MockConnectionPoolDataSource(Connection conn) {
        this.conn = conn;
    }

    /* (non-Javadoc)
     * @see javax.sql.ConnectionPoolDataSource#getPooledConnection()
     */
    public PooledConnection getPooledConnection() throws SQLException {
        return new MockPooledConnection(conn);
    }

    /* (non-Javadoc)
     * @see javax.sql.ConnectionPoolDataSource#getPooledConnection(java.lang.String, java.lang.String)
     */
    public PooledConnection getPooledConnection(String user, String password) throws SQLException {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see javax.sql.ConnectionPoolDataSource#getLogWriter()
     */
    public PrintWriter getLogWriter() throws SQLException {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see javax.sql.ConnectionPoolDataSource#setLogWriter(java.io.PrintWriter)
     */
    public void setLogWriter(PrintWriter out) throws SQLException {        

    }

    /* (non-Javadoc)
     * @see javax.sql.ConnectionPoolDataSource#setLoginTimeout(int)
     */
    public void setLoginTimeout(int seconds) throws SQLException {
       
    }

    /* (non-Javadoc)
     * @see javax.sql.ConnectionPoolDataSource#getLoginTimeout()
     */
    public int getLoginTimeout() throws SQLException {       
        return 0;
    }
}
