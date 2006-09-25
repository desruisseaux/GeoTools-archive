package org.geotools.data.postgis;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import junit.framework.TestCase;

import org.geotools.data.DataStoreFinder;
import org.geotools.data.postgis.PostgisTests.Fixture;

public class PostgisDBInfoOnlineTest extends TestCase {
    
    PostgisDataStore ds;
    Fixture fixture;
    PostgisDBInfo dbInfo;
    
    protected void setUp() throws Exception {
        super.setUp();
        //connect
        fixture = PostgisTests.newFixture("fixture.properties");
        Map params = PostgisTests.getParams(fixture);
        ds = (PostgisDataStore) DataStoreFinder.getDataStore(params);
        dbInfo = ds.getDBInfo();
    }
    
    public Connection getConnection() throws SQLException {
        return ds.getConnectionPool().getConnection();
    }
    
    protected void tearDown() throws Exception {
        ds.getConnectionPool().close();
        ds = null;
        super.tearDown();
    }

    public void testVersions() {
        assertNotNull(dbInfo);
        
        //postgis version should be 0.* or 1.*
        assertTrue(dbInfo.getVersion().length() > 2);
        assertTrue(dbInfo.getMajorVersion() == 0 || dbInfo.getMajorVersion() == 1);
        
        //postgres version should be 7.* or 8.*
        assertTrue(dbInfo.getPostgresVersion().length() > 2);
        assertTrue(dbInfo.getPostgresMajorVersion() == 7 || dbInfo.getPostgresMajorVersion() == 8);
    }
    

}
