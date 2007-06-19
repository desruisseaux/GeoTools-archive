package org.geotools.data.oracle.referencing;

import java.sql.Connection;
import java.util.PropertyResourceBundle;

import junit.framework.TestCase;

import org.geotools.data.jdbc.ConnectionPool;
import org.geotools.data.oracle.OracleConnectionFactory;
import org.geotools.metadata.iso.IdentifierImpl;
import org.geotools.referencing.CRS;
import org.geotools.referencing.NamedIdentifier;
import org.opengis.metadata.Identifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class OracleAuthorityFactoryTest extends TestCase {
    
    private ConnectionPool pool;
    private Connection conn;

    protected void setUp() throws Exception {
        super.setUp();

        PropertyResourceBundle resource;
        resource =
            new PropertyResourceBundle(this.getClass().getResourceAsStream("/org/geotools/data/oracle/fixture.properties"));

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
         
        try {
            OracleConnectionFactory factory1 = new OracleConnectionFactory(host, port, instance);
            pool = factory1.getConnectionPool(user, password);          
            conn = pool.getConnection();
        }
        catch( Throwable t ){
                t.printStackTrace();
            System.out.println("Could not load test fixture, configure "+getClass().getResource("fixture.properties"));
            t.printStackTrace();
            return;
        }

    }
    
    public void testSRIDLookup() throws Exception {
        if(conn == null)
            return;
        
        OracleAuthorityFactory af = new OracleAuthorityFactory(pool);
        CoordinateReferenceSystem crs27700 = af.createCRS(27700);
        assertNotNull(crs27700);
        
        CoordinateReferenceSystem crs81989 = af.createCRS(81989);
        assertNotNull(crs81989);
    }
}
