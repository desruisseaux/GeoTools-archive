/* $Id$
 *
 * Created on 4/08/2003
 */
package org.geotools.data.oracle;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;

import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.jdbc.ConnectionPool;
import org.geotools.data.jdbc.ConnectionPoolManager;

/**
 * Test the datastore factories
 * 
 * @author Andrea Aime
 */
public class OracleDataStoreFactoryTest extends TestCase {
    /** The Oracle driver class name */
    private static final String JDBC_DRIVER = "oracle.jdbc.driver.OracleDriver";
    private Properties properties;
    boolean GO = true;
    /**
     * Creates a new OracleDataStoreFactoryTest Test.
     * 
     * @throws ClassNotFoundException If the driver cannot be found
     */
    public OracleDataStoreFactoryTest() throws ClassNotFoundException {
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
     * Creates a new OracleDataStoreFactoryTest Test.
     * 
     * @param arg0 name of the test
     * @throws ClassNotFoundException If the Oracle Driver cannot be found
     */
    public OracleDataStoreFactoryTest( String arg0 ) throws ClassNotFoundException {
        super(arg0);
        try {
        	Class.forName(JDBC_DRIVER);
        }
        catch( Throwable t ){
            GO = false;
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
        properties.load( this.getClass().getResourceAsStream("test.properties") );
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
    
    public void testDataStoreFactory() throws Exception {
        if( !GO ) return;
        OracleDataStoreFactory factory = new OracleDataStoreFactory();
        checkFactoryNamespace(factory);
    }
    
    public void testOciDataStoreFactory() throws Exception {
        if( !GO ) return;
        OracleOCIDataStoreFactory factory = new OracleOCIDataStoreFactory();
        checkFactoryNamespace(factory);
    }

    public void checkFactoryNamespace(DataStoreFactorySpi factory) throws Exception {
    	Map map = new HashMap();
        map.put("host", properties.getProperty("host"));
        map.put("port", properties.getProperty("port"));
        map.put("instance", properties.getProperty("instance"));
        map.put("user", properties.getProperty("user"));
        map.put("passwd", properties.getProperty("passwd"));
        map.put("dbtype", "oracle");
        map.put("alias", properties.getProperty("instance"));
        map.put("namespace", null);
        
        assertTrue(factory.canProcess(map));
        OracleDataStore store = (OracleDataStore) factory.createDataStore(map); 
        assertNull(store.getNameSpace());
        
        map.put("schema", properties.getProperty("user").toUpperCase());
        store = (OracleDataStore) factory.createDataStore(map); 
        assertNull(store.getNameSpace());
        
        map.put("namespace", "topp");
        store = (OracleDataStore) factory.createDataStore(map); 
        assertEquals(new URI("topp"), store.getNameSpace());
    }
}
