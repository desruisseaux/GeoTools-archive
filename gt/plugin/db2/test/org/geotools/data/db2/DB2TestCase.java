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

import com.ibm.db2.jcc.DB2ConnectionPoolDataSource;
import junit.framework.TestCase;
import org.geotools.data.db2.DB2ConnectionFactory;
import org.geotools.data.jdbc.ConnectionPool;
import org.geotools.data.jdbc.JDBCDataStoreConfig;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.Set;
import javax.sql.PooledConnection;


/**
 * Provide common functionality for DB2 plug-in testcases.
 *
 * @author David Adler - IBM Corporation
 * @source $URL$
 */
public class DB2TestCase extends TestCase {
    protected static final String DB2_URL_PREFIX = "jdbc:db2://";
    protected Map allParams = null;
    protected String dbname = null;
    protected String user = null;
    protected String pw = null;
    protected String host = null;
    protected int portnum = 50000;
    protected String tabSchema = null;
    protected String dbURL = null;
    protected boolean mock = false;
    protected ConnectionPool pool = null;

    protected void setUp() throws Exception {
        super.setUp();
        setPropertyValues();
        setAllParamValues();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    protected String getDbURL() {
        return DB2_URL_PREFIX + host + ":" + portnum + "/" + dbname;
    }

    /**
     * Sets local values used in testing from a properties file
     *
     * @throws IOException
     * @throws SQLException
     */
    protected void setPropertyValues() throws IOException, SQLException {
        PropertyResourceBundle resource = new PropertyResourceBundle(this.getClass()
                                                                         .getResourceAsStream("db2test.properties"));
        host = resource.getString("host");
        portnum = Integer.parseInt(resource.getString("portnum"));
        dbname = resource.getString("dbname");
        user = resource.getString("user");
        pw = resource.getString("password");
        tabSchema = resource.getString("tabschema");

        if (resource.getString("mock").equals("1")) {
            mock = true;
        } else {
            mock = false;
        }
    }

    protected void setAllParamValues() {
        allParams = new HashMap();
        allParams.put("database", dbname);
        allParams.put("dbtype", "db2");
        allParams.put("host", host);
        allParams.put("port", String.valueOf(portnum));
        allParams.put("user", user);
        allParams.put("passwd", pw);
        allParams.put("tabschema", tabSchema);
    }

    /**
     * Common local method to get a Connection for testing
     *
     *
     * @throws Exception
     */
    protected Connection getLocalConnection() throws Exception {
        DB2ConnectionPoolDataSource poolDataSource;
        poolDataSource = new DB2ConnectionPoolDataSource();
        poolDataSource.setDatabaseName(dbname);
        poolDataSource.setUser(user);
        poolDataSource.setPassword(pw);
        poolDataSource.setPortNumber(portnum);
        poolDataSource.setServerName(host);
        poolDataSource.setDriverType(4);

        PooledConnection pc = poolDataSource.getPooledConnection();
        Connection conn = pc.getConnection();

        return conn;
    }

    /**
     * Common local method to get a ConnectionPool for testing
     *
     *
     * @throws Exception
     */
    protected ConnectionPool getLocalConnectionPool() throws Exception {
        DB2ConnectionFactory connFact = new DB2ConnectionFactory(host,
                String.valueOf(portnum), dbname);
        connFact.setLogin(user, pw);

        ConnectionPool pool = connFact.getConnectionPool();

        return pool;
    }

    /**
     * Local utility method to make a copy of a Map object.
     * 
     * <p>
     * Used to make copies of a HashMap of Param objects but should work for
     * any Map.
     * </p>
     *
     * @param params an arbitrary Map object
     *
     * @return a copy of the input Map object
     */
    protected Map copyParams(Map params) {
        Map p2 = new HashMap();
        Set keys = params.keySet();
        Iterator it = keys.iterator();

        while (it.hasNext()) {
            String key = (String) it.next();
            p2.put(key, params.get(key));
        }

        return p2;
    }

    protected DB2DataStore getDataStore() throws Exception {
        JDBCDataStoreConfig config = new JDBCDataStoreConfig(tabSchema,
                tabSchema, 100000);

        if (pool == null) {
            pool = getLocalConnectionPool();
        }

        return new DB2DataStore(pool, config, getDbURL());
    }
}
