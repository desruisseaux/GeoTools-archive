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
package org.geotools.data.oracle;

import java.util.PropertyResourceBundle;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.geotools.data.DataTestCase;
import org.geotools.data.jdbc.ConnectionPool;
import org.geotools.data.jdbc.JDBCDataStoreConfig;

/**
 * This class provides a quick test of OracleDataStore.
 * <p>
 * A simple properties file has been constructed,
 * <code>fixture.properties</code>, which you may direct to your own
 * database installation.
 * </p>
 *
 * @author Jody Garnett, Refractions Research
 */
public class QuickOracleTest extends DataTestCase {
    
    /** The logger for the filter module. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.data.postgis");
    
    OracleDataStore data;
    ConnectionPool pool;    

    /**
     * Constructor for MemoryDataStoreTest.
     *
     * @param test
     *
     * @throws AssertionError DOCUMENT ME!
     */
    public QuickOracleTest(String test) {
        super(test);
    }

    /**
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        PropertyResourceBundle resource;
        resource =
            new PropertyResourceBundle(this.getClass().getResourceAsStream("fixture.properties"));

        String namespace = resource.getString("namespace");
        String host = resource.getString("host");
        String port = resource.getString("port");        
        String instance = resource.getString("instance");        

        String user = resource.getString("user");
        String password = resource.getString("password");

        if (namespace.equals("http://www.geotools.org/data/postgis")) {
            throw new IllegalStateException(
                "The fixture.properties file needs to be configured for your own database");
        }

         
        OracleConnectionFactory factory1 = new OracleConnectionFactory(host, port, instance);
        pool = factory1.getConnectionPool(user, password);
        
        JDBCDataStoreConfig config =
        	JDBCDataStoreConfig.createWithNameSpaceAndSchemaName( namespace, instance );        
        
        data = new OracleDataStore(pool, config );
        //BasicFIDMapper basic = new BasicFIDMapper("tid", 255, false);
        //TypedFIDMapper typed = new TypedFIDMapper( basic, "trim_utm10");
        //data.setFIDMapper("trim_utm10", typed );        
    }
    public void testMe() throws Exception {
    	String typeNames[] = data.getTypeNames();
    	assertNotNull( typeNames );
    	assertEquals( 1, typeNames.length );
    }    
}
