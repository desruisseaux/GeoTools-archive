package org.geotools.data.h2;

import java.util.HashMap;

import org.geotools.jdbc.JDBCDataStoreFactory;

import junit.framework.TestCase;

public class H2DataStoreFactoryTest extends TestCase {

    H2DataStoreFactory factory;
    
    protected void setUp() throws Exception {
        factory = new H2DataStoreFactory();
    }
    
    public void testCanProcess() throws Exception {
        HashMap params = new HashMap();
        assertFalse( factory.canProcess(params));
        
        params.put( JDBCDataStoreFactory.NAMESPACE.key, "http://www.geotools.org/test" );
        params.put( JDBCDataStoreFactory.DATABASE.key, "geotools" );
        params.put( JDBCDataStoreFactory.DBTYPE.key, "h2" );
        
        assertTrue( factory.canProcess(params));
    }
}
