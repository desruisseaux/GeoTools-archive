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

import junit.framework.TestCase;

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
    
    public void testParamCHARSET() throws Throwable {
        Param p = MySQLDataStoreFactory.CHARSET;
        try {
            p.parse(null);
            fail("expected error for parse null");
        }
        catch( Exception e){}
        
        try {
            p.parse("");
            fail("expected error for parse empty");
        }
        catch( Exception e){}
        assertNotNull( "parse ISO-8859-1", p.parse("ISO-8859-1"));
        
        assertNull("handle null", p.handle(null) );
        assertNull("handle empty", p.handle("") );
        assertNotNull( "handle ISO-8859-1", p.handle("ISO-8859-1"));
        
        Map map = new HashMap();
        Charset latin1=Charset.forName("ISO-8859-1");
        map.put("charset", latin1 );
        assertEquals( latin1, p.lookUp( map ));
        
        try {
            assertNotNull( "handle ISO-LATIN-1", p.handle("ISO-LATIN-1"));            
        } catch (IOException expected){            
        }
        System.out.println( latin1.toString() );
        System.out.println( latin1.name() );
        System.out.println( p.text( latin1 ));
        assertEquals("ISO-8859-1", p.text(latin1) );
        try {
            assertEquals("ISO-8859-1", p.text("ISO-8859-1") );
            fail("Should not handle bare text");
        }
        catch( ClassCastException expected ){            
        }
    }
    public void testLocal() throws Exception {
        Map map = local;
        System.out.println( "local:"+map );
        assertEquals( null, MySQLDataStoreFactory.CHARSET.lookUp(map) );
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
        assertEquals( null, MySQLDataStoreFactory.CHARSET.lookUp(map) );
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
