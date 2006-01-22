/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) Copyright IBM Corporation, 2005. All rights reserved.
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
package org.geotools.data.db2;

import com.ibm.db2.jcc.DB2ConnectionPoolDataSource;
import org.geotools.data.jdbc.ConnectionPool;
import org.geotools.data.jdbc.ConnectionPoolManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.sql.ConnectionPoolDataSource;


/**
 * A factory to create a DB2 Connection based on the needed parameters.
 *
 * @author David Adler - IBM Corporation
 * @source $URL$
 */
public class DB2ConnectionFactory {
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.data.db2");
    private static final String DB2_URL_PREFIX = "jdbc:db2://";
    private static Map DATA_SOURCES = new HashMap();
    private String dbURL = null;
    private String dbname = null;
    private String user = null;
    private String pw = null;

    /**
     * Creates a new DB2ConnectionFactory from a host name, port number, and
     * database name.
     *
     * @param host the DB2 host name
     * @param portnum the TCP/IP port number for the DB2 client connection
     * @param dbname the DB2 database name
     */
    public DB2ConnectionFactory(String host, String portnum, String dbname) {
        super();
        this.dbURL = DB2_URL_PREFIX + host + ":" + portnum + "/" + dbname;
        this.dbname = dbname;
    }

    /**
     * Sets database login information.
     *
     * @param user the database user name
     * @param pw the user's database password
     */
    public void setLogin(String user, String pw) {
        this.user = user;
        this.pw = pw;
    }

    /**
     * Returns the database URL string for this connection.
     *
     * @return dbURL
     */
    public String getDbURL() {
        return this.dbURL;
    }

    /**
     * Returns a ConnectionPool.  Assumes that the required connection
     * information: database name, user name and password have already been
     * set.
     *
     * @return a ConnectionPool object
     *
     * @throws SQLException if we fail to get a database connection
     */
    public ConnectionPool getConnectionPool() throws SQLException {
        String key = this.dbURL + this.user + this.pw;
        DB2ConnectionPoolDataSource poolDataSource = (DB2ConnectionPoolDataSource) DATA_SOURCES
            .get(key);

        // Create a new pool data source if one doesn't already exist.
        if (poolDataSource == null) {
            poolDataSource = new DB2ConnectionPoolDataSource();
            poolDataSource.setDatabaseName(this.dbname);
            poolDataSource.setUser(this.user);
            poolDataSource.setPassword(this.pw);
            DATA_SOURCES.put(key, poolDataSource);
            LOGGER.info("Created new DB2ConnectionPoolDataSource for dbUrl "
                + this.dbURL);
        }

        ConnectionPoolManager manager = ConnectionPoolManager.getInstance();
        ConnectionPool connectionPool = manager.getConnectionPool((ConnectionPoolDataSource) poolDataSource);

        LOGGER.info("Successfully obtained DB2 ConnectionPool");

        return connectionPool;
    }
}
