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

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;

/**
 * Test Params used by PostgisDataStoreFactory.
 * 
 * @author jgarnett, Refractions Research, Inc.
 * @author $Author: jive $ (last modification)
 * @version $Id: PostgisDataStoreFactoryTest.java,v 1.3 2004/01/14 10:24:59 jive Exp $
 */
public class PostgisDataStoreFactoryTest extends TestCase {
    static PostgisDataStoreFactory factory
        = new PostgisDataStoreFactory();
    
    Map remote;
    Map local;
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
        remote.put("charset", "");
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
        assertEquals( "cite", factory.DATABASE.lookUp(map) );        
        assertEquals( "postgis", factory.DBTYPE.lookUp(map) );
        assertEquals( "hydra", factory.HOST.lookUp(map) );
        assertEquals( null, factory.NAMESPACE.lookUp(map) );
        assertEquals( "cite", factory.PASSWD.lookUp(map) );
        assertEquals( new Integer(5432), factory.PORT.lookUp(map) );
        assertEquals( "cite", factory.USER.lookUp(map) );
        
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
        assertEquals( "testdb", factory.DATABASE.lookUp(map) );        
        assertEquals( "postgis", factory.DBTYPE.lookUp(map) );
        assertEquals( "localhost", factory.HOST.lookUp(map) );
        assertEquals( null, factory.NAMESPACE.lookUp(map) );
        assertEquals( "postgres", factory.PASSWD.lookUp(map) );
        assertEquals( new Integer(5432), factory.PORT.lookUp(map) );
        assertEquals( "postgres", factory.USER.lookUp(map) );
        
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
