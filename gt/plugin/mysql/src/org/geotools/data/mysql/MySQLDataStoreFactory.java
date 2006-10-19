/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.data.mysql;

import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.Map;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.jdbc.ConnectionPool;


/**
 * Creates a MySQLDataStoreFactory based on the correct params.
 * 
 * <p>
 * This factory should be registered in the META-INF/ folder, under services/
 * in the DataStoreFactorySpi file.
 * </p>
 *
 * @author Andrea Aime, University of Modena and Reggio Emilia
 * @source $URL$
 */
public class MySQLDataStoreFactory
    implements org.geotools.data.DataStoreFactorySpi {

    private static final Logger LOGGER = Logger.getLogger(MySQLDataStoreFactory.class.getName());
        
    /** Creates MySQL JDBC driver class. */
    private static final String DRIVER_CLASS = "com.mysql.jdbc.Driver";

    /** Param, package visibiity for JUnit tests */
    static final Param DBTYPE = new Param("dbtype", String.class,
            "must be 'mysql'", true, "mysql");

    /** Param, package visibiity for JUnit tests */
    static final Param HOST = new Param("host", String.class,
            "mysql host machine", true, "localhost");

    /** Param, package visibiity for JUnit tests */
    static final Param PORT = new Param("port", String.class,
            "mysql connection port", true, "3306");

    /** Param, package visibiity for JUnit tests */
    static final Param DATABASE = new Param("database", String.class,
            "msyql database");

    /** Param, package visibiity for JUnit tests */
    static final Param USER = new Param("user", String.class,
            "user name to login as", false);

    /** Param, package visibiity for JUnit tests */
    static final Param PASSWD = new Param("passwd", String.class,
            "password used to login", false);

    /** Param, package visibiity for JUnit tests */
    static final Param NAMESPACE = new Param("namespace", String.class,
            "namespace prefix used", false);

    /** Array with all of the params */
    static final Param[] arrayParameters = {
        DBTYPE, HOST, PORT, DATABASE, USER, PASSWD, NAMESPACE
    };

    /**
     * Creates a new instance of PostgisDataStoreFactory
     */
    public MySQLDataStoreFactory() {
    }

    /**
     * Checks to see if all the postgis params are there.
     * 
     * <p>
     * Should have:
     * </p>
     * 
     * <ul>
     * <li>
     * dbtype: equal to postgis
     * </li>
     * <li>
     * host
     * </li>
     * <li>
     * user
     * </li>
     * <li>
     * passwd
     * </li>
     * <li>
     * database
     * </li>
     * </ul>
     * 
     *
     * @param params Set of parameters needed for a postgis data store.
     *
     * @return <code>true</code> if dbtype equals postgis, and contains keys
     *         for host, user, passwd, and database.
     */
    public boolean canProcess(Map params) {
        Object value;

        if (params != null) {
            for (int i = 0; i < arrayParameters.length; i++) {
                if (!(((value = params.get(arrayParameters[i].key)) != null)
                        && (arrayParameters[i].type.isInstance(value)))) {
                    if (arrayParameters[i].required) {
                        if (LOGGER.isLoggable(Level.FINER)) {
                            LOGGER.finer("Failed on : " 
                                         + arrayParameters[i].key + ", full " +
                                         "params= " + params.toString()); 
                        }
                        return (false);
                    }
                }
            }
        } else {
            return (false);
        }

        if ((((String) params.get("dbtype")).equalsIgnoreCase("mysql"))) {
            return (true);
        } else {
            return (false);
        }
    }

    /**
     * Construct a postgis data store using the params.
     *
     * @param params The full set of information needed to construct a live
     *        data source.  Should have  dbtype equal to postgis, as well as
     *        host, user, passwd, database, and table.
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
        // lookup will throw nice exceptions back to the client code
        String host = (String) HOST.lookUp(params);
        String user = (String) USER.lookUp(params);
        String passwd = (String) PASSWD.lookUp(params);
        String port = (String) PORT.lookUp(params);
        String database = (String) DATABASE.lookUp(params);
        String namespace = (String) NAMESPACE.lookUp(params);

        if (!canProcess(params)) {
            // Do this as a last sanity check.
            LOGGER.warning("Can not process : " + params);
            throw new IOException("The parameteres map isn't correct!!");
        }
        
        MySQLConnectionFactory connFact = new MySQLConnectionFactory(host,
                new Integer(port).intValue(), database);

        connFact.setLogin(user, passwd);

        ConnectionPool pool;

        try {
            pool = connFact.getConnectionPool();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Could not create connection to MySQL database.", e);
            throw new DataSourceException("Could not create connection", e);
        }

        if (namespace != null) {
            return new MySQLDataStore(pool, namespace);
        } else {
            return new MySQLDataStore(pool);
        }
    }

    /**
     * Postgis cannot create a new database.
     *
     * @param params
     *
     *
     * @throws IOException See UnsupportedOperationException
     * @throws UnsupportedOperationException Cannot create new database
     */
    public DataStore createNewDataStore(Map params) throws IOException {
        throw new UnsupportedOperationException(
            "MySQL cannot create a new Database");
    }

    /**
     * @return "MySQL"
     */
    public String getDisplayName() {
        return "MySQL";
    }
    
    /**
     * Describe the nature of the datasource constructed by this factory.
     *
     * @return A human readable description that is suitable for inclusion in a
     *         list of available datasources.  Currently uses the string "MySQL Database"
     */
    public String getDescription() {
        return "MySQL Database";
    }

//    /**
//     *
//     */
//    public DataSourceMetadataEnity createMetadata( Map params ) throws IOException {
//        String host = (String) HOST.lookUp(params);
//        String user = (String) USER.lookUp(params);
//        String port = (String) PORT.lookUp(params);
//        String database = (String) DATABASE.lookUp(params);
//        return new DataSourceMetadataEnity( host+"port", database, "MySQL connection to "+host+" as "+user );
//    }

    /**
     * Test to see if this datastore is available, if it has all the
     * appropriate libraries to construct a datastore.  This datastore just
     * returns true for now.  This method is used for gui apps, so as to not
     * advertise data store capabilities they don't actually have.
     *
     * @return <tt>true</tt> if and only if this factory is available to create
     *         DataStores.
     */
    public boolean isAvailable() {
        try {
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException cnfe) {
            LOGGER.warning("MySQL data sources are not available: " + cnfe.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Describe parameters.
     *
     *
     * @see org.geotools.data.DataStoreFactorySpi#getParametersInfo()
     */
    public Param[] getParametersInfo() {
        return new Param[] {
            DBTYPE, HOST, PORT, DATABASE, USER, PASSWD, NAMESPACE
        };
    }

    /**
     * Returns the implementation hints. The default implementation returns en empty map.
     */
    public Map getImplementationHints() {
        return Collections.EMPTY_MAP;
    }
}
