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
 *    Created on 4/08/2003
 */
package org.geotools.data.oracle;

import java.util.Properties;

import junit.framework.TestCase;

import org.geotools.data.jdbc.ConnectionPool;
import org.geotools.data.jdbc.ConnectionPoolManager;

/**
 * Test the Connection poolings
 * 
 * @author Sean Geoghegan, Defence Science and Technology Organisation
 * @author $Author: seangeo $
 * @source $URL$
 * @version $Id$ Last
 *          Modified: $Date: 2003/08/15 00:42:02 $
 */
public class OracleConnectionFactoryTest extends TestCase {
    /** The Oracle driver class name */
    private static final String JDBC_DRIVER = "oracle.jdbc.driver.OracleDriver";
    private Properties properties;
    boolean GO = true;
    /**
     * Creates a new OracleConnectionFactory Test.
     * 
     * @throws ClassNotFoundException If the driver cannot be found
     */
    public OracleConnectionFactoryTest() throws ClassNotFoundException {
        super();
        try {
        	Class.forName(JDBC_DRIVER);
        }
        catch( Throwable t ){
        	GO = false;
        	// must be running off dummy jar!
        }
    }

    /**
     * Creates a new OracleConnectionFactory Test.
     * 
     * @param arg0 name of the test
     * @throws ClassNotFoundException If the Oracle Driver cannot be found
     */
    public OracleConnectionFactoryTest( String arg0 ) throws ClassNotFoundException {
        super(arg0);
        try {
        	Class.forName(JDBC_DRIVER);
        }
        catch( Throwable t ){
        	// must be running off dummy jar!
        }
    }

    /**
     * Loads the properties
     * 
     * @throws Exception
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        properties = new Properties();
        properties.load( this.getClass().getResourceAsStream("remote.properties") );
    }

    /**
     * Removes the properties
     * 
     * @throws Exception
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        ConnectionPoolManager manager = ConnectionPoolManager.getInstance();
        manager.closeAll();
        properties = null;
    }

    public void testGetConnection() throws Exception {
    	if( !GO ) return;
        OracleConnectionFactory cFact = new OracleConnectionFactory(properties.getProperty("host"),
                properties.getProperty("port"), properties.getProperty("instance"));
        cFact.setLogin(properties.getProperty("user"), properties.getProperty("passwd"));

        // check that two connection pools from the same fact are the same
        try {
        	ConnectionPool pool1 = cFact.getConnectionPool();
        	ConnectionPool pool2 = cFact.getConnectionPool();
        	assertTrue("Connection pool was not equal", pool1 == pool2);
	        // check that two connection pools using the same url,user,pass but
	        // from different factories are the same
	        OracleConnectionFactory cFact2 = new OracleConnectionFactory(
	                properties.getProperty("host"), properties.getProperty("port"), properties
	                        .getProperty("instance"));
	        cFact2.setLogin(properties.getProperty("user"), properties.getProperty("passwd"));
	        ConnectionPool pool3 = cFact2.getConnectionPool();
	        assertTrue("New factory returned different pool", pool1 == pool3);
        }
        catch (Throwable t ){
        	// must be using dummy spatial
        }        	        
    }
}
