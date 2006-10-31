/*
 *    GeoTools - OpenSource mapping toolkit
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

import org.geotools.data.jdbc.ConnectionPool;
import java.sql.Connection;
import java.sql.SQLException;


/**
 * Exercise DB2ConnectionFactory.
 *
 * @author David Adler - IBM Corporation
 * @source $URL$
 */
public class DB2ConnectionFactoryTest extends DB2TestCase {
    // Test connection factory with 3 parameter constructor
    public void testConnectionFactory3() {
        Connection conn = null;
        DB2ConnectionFactory connFact = null;
        ConnectionPool pool = null;
        String dbUrl = null;

/* Test connection factory using type 4 connection - port number is non-zero */        
        connFact = new DB2ConnectionFactory(host, String.valueOf(portnum),
                dbname);
        connFact.setLogin(user, pw);

        try {
            pool = connFact.getConnectionPool();
            conn = pool.getConnection();
            conn.close();
        } catch (SQLException e) {
            fail("Get connection with valid parameters failed: " + e);
        }

        dbUrl = connFact.getDbURL();
        assertEquals("Check returned dbUrl", dbUrl,
            "jdbc:db2://localhost:50000/geotools");
        
/* Test connection factory using type 2 connection - port number is zero */        
        connFact = new DB2ConnectionFactory(host, String.valueOf(0),
                dbname);
        connFact.setLogin(user, pw);

        try {
            pool = connFact.getConnectionPool();
            conn = pool.getConnection();
            conn.close();
        } catch (SQLException e) {
            fail("Get connection with valid parameters failed: " + e);
        }

        dbUrl = connFact.getDbURL();
        assertEquals("Check returned dbUrl", dbUrl,
            "jdbc:db2://localhost:0/geotools");
        
        connFact.setLogin("nouser", "nopw");

        try {
            pool = connFact.getConnectionPool();
            conn = pool.getConnection();
            fail("Get connection with invalid parameters didn't fail");
        } catch (SQLException e) {
            // Should get here if test is successful
        }

        connFact = new DB2ConnectionFactory(host, String.valueOf(portnum),
                "nodbname");
        connFact.setLogin(user, pw);

        try {
            pool = connFact.getConnectionPool();
            conn = pool.getConnection();
            fail("Get connection with invalid parameters didn't fail");
        } catch (SQLException e) {
            // Should get here if test is successful
        }
    }
}
