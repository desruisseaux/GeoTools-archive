package org.geotools.data.mysql;

import java.util.HashMap;

import org.geotools.jdbc.JDBCDataStoreFactory;

import junit.framework.TestCase;

public class MySQLDataStoreFactoryTest extends TestCase {

    MySQLDataStoreFactory factory;
    
    protected void setUp() throws Exception {
        factory = new MySQLDataStoreFactory();
    }
    
    public void testCanProcess() throws Exception {
        HashMap params = new HashMap();
        assertFalse( factory.canProcess(params));
        
        params.put( JDBCDataStoreFactory.NAMESPACE.key, "http://www.geotools.org/test" );
        params.put( JDBCDataStoreFactory.DATABASE.key, "geotools" );
        params.put( JDBCDataStoreFactory.DBTYPE.key, "mysql" );
        
        params.put( JDBCDataStoreFactory.HOST.key, "localhost");
        params.put( JDBCDataStoreFactory.PORT.key, "3306");
        params.put( JDBCDataStoreFactory.USER.key, "mysqluser");
        assertTrue( factory.canProcess(params));
    }
}
