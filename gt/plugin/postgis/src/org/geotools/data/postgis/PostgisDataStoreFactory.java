/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
package org.geotools.data.postgis;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import org.geotools.data.AbstractDataStoreFactory;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.jdbc.ConnectionPool;
import org.geotools.data.jdbc.fidmapper.BasicFIDMapper;
import org.geotools.data.jdbc.fidmapper.TypedFIDMapper;


/**
 * Creates a PostgisDataStore baed on the correct params.
 * 
 * <p>
 * This factory should be registered in the META-INF/ folder, under services/
 * in the DataStoreFactorySpi file.
 * </p>
 *
 * @author Jody Garnett, Refractions Research
 */
public class PostgisDataStoreFactory extends AbstractDataStoreFactory
    implements org.geotools.data.DataStoreFactorySpi {
    /** Creates PostGIS-specific JDBC driver class. */
    private static final String DRIVER_CLASS = "org.postgresql.Driver";

    /** Param, package visibiity for JUnit tests */
    static final Param DBTYPE = new Param("dbtype", String.class,
            "must be 'postgis'", true, "postgis");

    /** Param, package visibiity for JUnit tests */
    static final Param HOST = new Param("host", String.class,
            "postgis host machine", true, "localhost");

    /** Param, package visibiity for JUnit tests */
    static final Param PORT = new Param("port", Integer.class,
            "postgis connection port (default is 5432)", true, new Integer(5432));

    /** Param, package visibiity for JUnit tests */
    static final Param DATABASE = new Param("database", String.class,
            "postgis database");

    /** Param, package visibiity for JUnit tests */
    static final Param USER = new Param("user", String.class,
            "user name to login as");

    /** Param, package visibiity for JUnit tests */
    static final Param PASSWD = new Param("passwd", String.class,
            "password used to login", false);


    /** Param, package visibiity for JUnit tests */
    static final Param NAMESPACE = new Param("namespace", String.class,
            "namespace prefix used", false);

    static final Param WKBENABLED = new Param("wkb enabled", Boolean.class,
            "set to true if Well Known Binary should be used to read PostGIS "
            + "data (experimental)", false, new Boolean(true));

    static final Param LOOSEBBOX = new Param("loose bbox", Boolean.class,
            "set to true if the Bounding Box should be 'loose', faster but "
            + "not as deadly accurate", false, new Boolean(true));



    /** Array with all of the params */
    static final Param[] arrayParameters = {
        DBTYPE, HOST, PORT, DATABASE, USER, PASSWD, WKBENABLED, LOOSEBBOX,
        NAMESPACE
    };

    /**
     * Creates a new instance of PostgisDataStoreFactory
     */
    public PostgisDataStoreFactory() {
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
     * <li>
     * charset
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
        if( !super.canProcess( params ) ){
            return false; // was not in agreement with getParametersInfo
        }
        if (!(((String) params.get("dbtype")).equalsIgnoreCase("postgis"))) {
            return (false);
        } else {
            return (true);
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
        // lookup will throw error message for
        // miscoversion or lack of required param
        //
        String host = (String) HOST.lookUp(params);
        String user = (String) USER.lookUp(params);
        String passwd = (String) PASSWD.lookUp(params);
        Integer port = (Integer) PORT.lookUp(params);
        String database = (String) DATABASE.lookUp(params);
        Boolean wkb_enabled = (Boolean) WKBENABLED.lookUp(params);
        Boolean is_loose_bbox = (Boolean) LOOSEBBOX.lookUp(params);
        String namespace = (String) NAMESPACE.lookUp(params);
        
        // Try processing params first so we can get real IO
        // error message back to the user
        //
        if (!canProcess(params)) {
            throw new IOException("The parameteres map isn't correct!!");
        }        
        PostgisConnectionFactory connFact = new PostgisConnectionFactory(host,
                port.toString(), database);

        connFact.setLogin(user, passwd);

        ConnectionPool pool;

        try {
            pool = connFact.getConnectionPool();
        } catch (SQLException e) {
            throw new DataSourceException("Could not create connection", e);
        }

        PostgisDataStore dataStore;

        if (namespace != null) {
            dataStore = new PostgisDataStore(pool, namespace);
        } else {
            dataStore = new PostgisDataStore(pool);
        }

        if (wkb_enabled != null) {
            dataStore.setWKBEnabled(wkb_enabled.booleanValue());
        }

        if (is_loose_bbox != null) {
            dataStore.setLooseBbox(is_loose_bbox.booleanValue());
        }
        
        return dataStore;
    }

    /**
     * Postgis cannot create a new database.
     *
     * @param params
     *
     * @return
     *
     * @throws IOException See UnsupportedOperationException
     * @throws UnsupportedOperationException Cannot create new database
     */
    public DataStore createNewDataStore(Map params) throws IOException {
        throw new UnsupportedOperationException(
            "Postgis cannot create a new Database");
    }
    
    public String getDisplayName() {
        return "Postgis";
    }
    /**
     * Describe the nature of the datasource constructed by this factory.
     *
     * @return A human readable description that is suitable for inclusion in a
     *         list of available datasources.
     */
    public String getDescription() {
        return "PostGIS spatial database";
    }

//      /**
//       * TODO: Probabaly need some metadata for loose bbox and wkbenabled?
//       */
//	public DataSourceMetadataEnity createMetadata( Map params ) throws IOException {
//	    String host = (String) HOST.lookUp(params);
//        String user = (String) USER.lookUp(params);
//        Integer port = (Integer) PORT.lookUp(params);
//        String database = (String) DATABASE.lookUp(params);
//        return new DataSourceMetadataEnity( host+":"+port, database, "Connection to "+getDisplayName()+" on "+host+" as "+user );
//	}
    /**
     * Determines if the appropriate libraries are present for this datastore
     * factory to successfully produce postgis datastores.  
     * 
     * @return <tt>true</tt> if the postgresql jar is on the classpath.
     */
    public boolean isAvailable() {
        try {
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException cnfe) {
            return false;
        }
        return true;
    }

    /**
     * Describe parameters.
     *
     * @return
     *
     * @see org.geotools.data.DataStoreFactorySpi#getParametersInfo()
     */
    public Param[] getParametersInfo() {
        return new Param[] {
            DBTYPE, HOST, PORT, DATABASE, USER, PASSWD, WKBENABLED, LOOSEBBOX, NAMESPACE
        };
    }
}
