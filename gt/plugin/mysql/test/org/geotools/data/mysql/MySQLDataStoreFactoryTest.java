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
package org.geotools.data.mysql;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi.Param;

/**
 * Test Params used by PostgisDataStoreFactory.
 * 
 * @author jgarnett, Refractions Research, Inc.
 * @author $Author: aaime $ (last modification)
 * @version $Id: MySQLDataStoreFactoryTest.java,v 1.1.2.1 2004/05/09 15:35:34 aaime Exp $
 */
public class MySQLDataStoreFactoryTest extends TestCase {
    static MySQLDataStoreFactory factory
        = new MySQLDataStoreFactory();
    
    Map remote;
    Map local;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(MySQLDataStoreFactoryTest.class);
        return suite;
    }    

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
         
        remote = new HashMap();
        remote.put("dbtype","postgis");        
        remote.put("host","localhost");
        remote.put("port", new Integer(5432));
        remote.put("database", "testdb");
        remote.put("user", "postgres");
        remote.put("passwd", "postgres");
        remote.put("namesapce", "topp");
        //remote.put( remote, "topp");
        
        local = new HashMap();
        local.put("dbtype","postgis");        
        local.put("host","hydra");
        local.put("port", new Integer(5432));
        local.put("database", "cite");
        local.put("user", "cite");
        local.put("passwd", "cite");
        //local.put("charset", "");
        //local.put("namesapce", "");
        
        super.setUp();
    }
    
    public void testLocal() throws Exception {
        Map map = local;
        System.out.println( "local:"+map );
        assertEquals( "cite", MySQLDataStoreFactory.DATABASE.lookUp(map) );        
        assertEquals( "postgis", MySQLDataStoreFactory.DBTYPE.lookUp(map) );
        assertEquals( "hydra", MySQLDataStoreFactory.HOST.lookUp(map) );
        assertEquals( null, MySQLDataStoreFactory.NAMESPACE.lookUp(map) );
        assertEquals( "cite", MySQLDataStoreFactory.PASSWD.lookUp(map) );
        assertEquals( new Integer(5432), MySQLDataStoreFactory.PORT.lookUp(map) );
        assertEquals( "cite", MySQLDataStoreFactory.USER.lookUp(map) );
        
        assertTrue( "canProcess", factory.canProcess(map));
        try {
            DataStore temp = factory.createDataStore(map);
            assertNotNull( "created", temp );
        }
        catch( DataSourceException expected){
            assertEquals("Could not get connection",expected.getMessage());
        }                        
    }
    public void testRemote() throws Exception {
        Map map = remote;
        System.out.println( "local:"+map );
        assertEquals( "testdb", MySQLDataStoreFactory.DATABASE.lookUp(map) );        
        assertEquals( "postgis", MySQLDataStoreFactory.DBTYPE.lookUp(map) );
        assertEquals( "localhost", MySQLDataStoreFactory.HOST.lookUp(map) );
        assertEquals( null, MySQLDataStoreFactory.NAMESPACE.lookUp(map) );
        assertEquals( "postgres", MySQLDataStoreFactory.PASSWD.lookUp(map) );
        assertEquals( new Integer(5432), MySQLDataStoreFactory.PORT.lookUp(map) );
        assertEquals( "postgres", MySQLDataStoreFactory.USER.lookUp(map) );
        
        assertTrue( "canProcess", factory.canProcess(map));
        try {
            DataStore temp = factory.createDataStore(map);
            assertNotNull( "created", temp );
        }
        catch( DataSourceException expected){
            assertEquals("Could not get connection",expected.getMessage());
        }               
    }    
}
