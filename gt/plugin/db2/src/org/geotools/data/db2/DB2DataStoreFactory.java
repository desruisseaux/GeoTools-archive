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

import org.geotools.data.AbstractDataStoreFactory;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.jdbc.ConnectionPool;
import org.geotools.data.jdbc.JDBCDataStoreConfig;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Logger;


/**
 * Implements the DataStoreFactorySpi interface to create an instance of a
 * DB2DataStore.
 *
 * @author David Adler - IBM Corporation
 * @source $URL$
 */
public class DB2DataStoreFactory extends AbstractDataStoreFactory
    implements DataStoreFactorySpi {
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.data.db2");

    // The DB2 JDBC type 4 driver class.  isAvailable() uses this to
    // check whether the DB2 JDBC library is in the classpath
    private static final String DRIVER_CLASS = "com.ibm.db2.jcc.DB2Driver";
    private static boolean isAvailable = false;
    private static final Param DBTYPE = new Param("dbtype", String.class,
            "must be 'DB2'", true, "DB2");
    private static final Param HOST = new Param("host", String.class,
            "DB2 host machine", true, "localhost");
    private static final Param PORT = new Param("port", String.class,
            "DB2 connection port", true, "50000");
    private static final Param DATABASE = new Param("database", String.class,
            "database name", true);
    private static final Param USER = new Param("user", String.class,
            "user name to login as", false);
    private static final Param PASSWD = new Param("passwd", String.class,
            "password used to login", false);
    private static final Param TABSCHEMA = new Param("tabschema", String.class,
            "default table schema", false);
    static final Param[] DB2PARMS = {
            DBTYPE, HOST, PORT, DATABASE, USER, PASSWD, TABSCHEMA
        };

    /**
     * canProcess and lastParams are used to cut out processing when
     * 'canProcess' is called successively.
     */
    private boolean canProcess = false;
    private Map lastParams = null;

    /**
     * Constructs a DB2 data store using the params.
     *
     * @param params The full set of information needed to construct a live
     *        data source.  Should have  dbtype equal to DB2, as well as host,
     *        user, passwd, database, and table schema.
     *
     * @return The created DataSource, this may be null if the required
     *         resource was not found or if insufficent parameters were given.
     *         Note that canProcess() should have returned false if the
     *         problem is to do with insuficent parameters.
     *
     * @throws IOException See DataSourceException
     * @throws DataSourceException Thrown if there were any problems creating
     *         or connecting the datasource.
     */
    public DataStore createDataStore(Map params) throws IOException {
        if (!canProcess(params)) {
            throw new IOException("Invalid parameters");
        }

        String host = (String) HOST.lookUp(params);
        String user = (String) USER.lookUp(params);
        String passwd = (String) PASSWD.lookUp(params);
        String port = (String) PORT.lookUp(params);
        String database = (String) DATABASE.lookUp(params);
        String tabschema = (String) TABSCHEMA.lookUp(params);

        DB2ConnectionFactory connFact = new DB2ConnectionFactory(host, port,
                database);

        connFact.setLogin(user, passwd);

        ConnectionPool pool;

        try {
            pool = connFact.getConnectionPool();
        } catch (SQLException e) {
            throw new DataSourceException("Could not create connection", e);
        }

        // Set the namespace and databaseSchemaName both to the table schema name
        // Set the timeout value to 100 seconds to force FeatureTypeHandler caching
        JDBCDataStoreConfig config = new JDBCDataStoreConfig(tabschema,
                tabschema, 100000);
        DB2DataStore ds;

        try {
            ds = new DB2DataStore(pool, config, connFact.getDbURL());
        } catch (IOException e) {
            LOGGER.info("Create DB2Datastore failed: "
                    + e);
            return null;
        }

        LOGGER.info("Successfully created DB2Datastore for: "
            + connFact.getDbURL());

        return ds;
    }

    /**
     * Creating a new DB2 database is not supported.
     *
     * @param params Doesn't much matter what this contains.
     *
     * @return DataStore But will always throw an exception
     *
     * @throws UnsupportedOperationException Cannot create new database
     */
    public DataStore createNewDataStore(Map params)
        throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
            "Creating a new DB2 database is not supported");
    }

    /**
     * Provide a String description of this data store.
     *
     * @return the data store description.
     */
    public String getDescription() {
        return "DB2 Data Store";
    }

    /**
     * Name suitable for display to end user.
     * 
     * <p>
     * A non localized display name for this data store type.
     * </p>
     *
     * @return A short name suitable for display in a user interface.
     */
    public String getDisplayName() {
        return "DB2";
    }

    /**
     * Returns the array of parameters used by DB2.
     *
     * @return Param[] Array of parameters.
     */
    public Param[] getParametersInfo() {
        return DB2PARMS;
    }

    /**
     * Check whether the parameter list passed identifies it as a request for a
     * DB2DataStore.
     * 
     * <p>
     * Most critical is the 'dbtype' parameter which must have the value 'DB2'.
     * If it is, then the remaining parameter values can be checked.
     * </p>
     *
     * @param params Key/Value parameter list containing values required to
     *        identify a request for a DB2DataStore and remaining values to
     *        identify the database to be connected to.
     *
     * @return true if dbtype equals DB2, and contains keys for host, user,
     *         passwd, and database.
     */
    public boolean canProcess(Map params) {
        String logInfo = "";

        // Hopefully we won't be called with a null parameter list.		
        if (params == null) {
            return false;
        }

        // Can't do anything if no dbtype or the dbtype is not DB2	
        String dbtype = (String) params.get("dbtype");

        if (dbtype == null) {
            return (false);
        }

        if (!(dbtype.equalsIgnoreCase("DB2"))) {
            return (false);
        }

        // If the parameters are the same as last time and it was ok last time
        // it should still be ok.
        if (this.canProcess && (this.lastParams == params)) {
            return true;
        }

        if (!super.canProcess(params)) {
            return false;
        }

        this.lastParams = params;
        this.canProcess = true;

        return true;
    }

    /**
     * Check whether the DB2 JDBC type 4 driver is found in the classpath.
     * 
     * <p>
     * If it isn't, there is a problem since the FactoryFinder found the
     * DB2DataStoreFactory but there is no driver to connect to a DB2
     * database.
     * </p>
     * 
     * <p>
     * The classpath should have db2jcc.jar and db2jcc_license_cu.jar
     * </p>
     *
     * @return true if a DB2 driver is available for the DB2DataStore to
     *         connect to a DB2 database.
     */
    public boolean isAvailable() {
        if (isAvailable) {
            return isAvailable;
        }

        try {
            Class.forName(DRIVER_CLASS);
            isAvailable = true;
        } catch (ClassNotFoundException e) {
            isAvailable = false;
        }

        LOGGER.info("DB2 driver found: " + isAvailable);

        return isAvailable;
    }
}
