package org.geotools.data.postgis.table;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.geotools.data.DataStore;
import org.geotools.test.OnlineTestCase;

public class TableTest extends OnlineTestCase {

    protected DataStore dataStore;

    protected final String DRIVER_CLASS = "org.postgresql.Driver";
    protected final String DRIVER_PATH = "jdbc:postgresql";

    /** Connection that can be used to test interaction */
    private Connection connection;

    protected void connect() throws Exception {
        Map params = new HashMap();
        String host = fixture.getProperty("host");
        String port = fixture.getProperty("port");
        String schema = schema = fixture.getProperty("schema");
        String database = fixture.getProperty("database");
        String user = fixture.getProperty("user");
        String password = fixture.getProperty("password");
        String connPath = DRIVER_PATH + "://" + host + ":" + port + "/" + database;
        
        // Instantiate the driver classes
        try {
            Class.forName(DRIVER_CLASS);
            Properties info = new Properties();
            info.putAll( fixture );
            connection = DriverManager.getConnection(connPath, info);            
        } catch (ClassNotFoundException cnfe) {
            throw new SQLException("Postgis driver was not found.");
        }
        
        connection.setAutoCommit(false);
    }

    protected void disconnect() throws Exception {
        if( connection != null ){
            connection.rollback();
            connection.close();
        }
    }

    protected String getFixtureId() {
        return null;
    }

    public void testTest(){
        
    }
}
