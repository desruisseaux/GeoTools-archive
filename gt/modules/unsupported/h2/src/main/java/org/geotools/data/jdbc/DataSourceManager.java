/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, GeoTools Project Managment Committee (PMC)
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
 */
package org.geotools.data.jdbc;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.NamingException;
import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.PooledConnection;

import org.geotools.factory.GeoTools;
import org.geotools.factory.JNDI;


/**
 * A copy of our origional ConnectionPool class in order to back in onto
 * a java.sql.DataSource.
 * <p>
 * This class is method compatible - if you like it we can do the following:
 * <ol>
 * <li>Introduce an interface ConnectionManager
 * <li>Mark the origional ConnectionPool with the ConnectionManager interface
 * <li>Make this DataSourceManager implement ConnectionManager
 * <li>Change the constructor of JDBCDataStore to take a ConnectionManager
 * <li>Update the factories to use this implementation (with an off the shelf DataSource implementation)
 * <li>Create new factories that will accept an application supplied DataSource (or String for JNDI lookup)
 * </ul>
 * This implementation is "naked" - it depends on the dataSource provided to handle the whole pooling idea.
 * It *does* do something - it will close all outstanding connections (leased through this interface) when
 * the close method is called.'
 * 
 * @author Jody Garnett
 * @version 2.4
 */
public final class DataSourceManager implements ConnectionManager {
    /** A logger */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.data.jdbc");

    /** This is our data source - we are expecting it to handle pooling. */
    private DataSource dataSource;

    /** This of connections that are in use */
    private List usedConnections = Collections.synchronizedList( new LinkedList() ); 

    /**
     * Uses JNDI InitialContext to lookup a DataSource with the provided name.
     * <p>
     * This solution is mostly intended for use with Java EE Applications, or desktop
     * applications running in an evironment support JNDI. This class makes use
     * of the global GeoTools.getDefaultHints() in order to aquire the the corrent
     * InitialContext. The out of the box implementation makes use of the JRE's
     * JNDI configuration (which is probably already set up by your application server).
     * <p>
     * @param dataSourceName Name to lookup in InitialContext.
     * @throws NamingException If there is nothing located in the indicated location
     * @throws ClassCastException If the name does not refer to a DataSource 
     */
    public DataSourceManager(String dataSourceName ) throws NamingException, ClassCastException {
        this( (DataSource) JNDI.getInitialContext(GeoTools.getDefaultHints()).lookup( dataSourceName ));
    }
    /**
     * Creates a new DataSourceManager using the provided DataSource.
     * <p>
     * Please note we are depending on the dataSource to provide conneciton pooling.
     * @param dataSource Used to create connections as needed
     */
    public DataSourceManager(DataSource dataSource ) {
        this.dataSource = dataSource;
    }

    /**
     * Gets a Connection from the Connection Pool.
     * 
     * <p>
     * If no available connections exist a new connection will be created and added to the pool.
     * When the returned connection is closed it will be added to the connection pool for other
     * requests to this method.
     * </p>
     *
     * @return A Connection from the ConnectionPool.
     *
     * @throws SQLException If an error occurs getting the connection or if the 
     * connection pool has been closed by a previous call to close().
     */
    public synchronized Connection getConnection() throws SQLException {
        if( usedConnections == null ){
            throw new SQLException("DataSourceManager is closed");
        }
        Connection connection = dataSource.getConnection();
        this.usedConnections.add( connection );
        
        return new ConnectionDecorator( connection ){
            public void close() throws SQLException {
                usedConnections.remove(connection);
                connection.close();
            }
        };
    }

    /** Closes all the PooledConnections in the the ConnectionPool.
     *  The current behaviour is to first close all the used connections,
     *  then close all the available connections.  This method will also set
     *  the state of the ConnectionPool to closed, caused any future calls
     *  to getConnection to throw an SQLException.
     */
    public synchronized void close() {
        for( Iterator i = usedConnections.iterator(); i.hasNext(); ){
            Connection connection = (Connection) i.next();
            try {
                if( !connection.isClosed() ){
                    connection.close();  // return to dataSource
                }
            } catch (SQLException eek) {                
                LOGGER.log( Level.WARNING, "Closing Connection: "+eek, eek );
            }
            usedConnections.clear();
            usedConnections = null;
            dataSource = null;
        }
    }
    public synchronized boolean isClosed() {
        return this.usedConnections == null;
    }    
}
