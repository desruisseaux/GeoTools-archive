package org.geotools.referencing.factory.epsg.oracle;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.sql.DataSource;

import oracle.jdbc.pool.OracleDataSource;
import oracle.jdbc.pool.OracleDataSourceFactory;

import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.geotools.factory.JNDI;
import org.geotools.test.OnlineTestCase;

public class OracleOnlineTest extends OnlineTestCase {
    DataSource datasource;
    Connection connection;

    /** Creates PostGIS-specific JDBC driver class. */
    private static final String DRIVER_CLASS = "oracle.jdbc.driver.OracleDriver";

    /** Creates PostGIS-specific JDBC driver path. */
    private static final String JDBC_PATH = "jdbc:oracle:thin:@";
    
    protected String getFixtureId() {
        return "epsg.oracle";
    }

    static final int DRIVER = 0;
    static final int DATASOURCE= 1;
    
    protected void connect() throws Exception {
        Context context = JNDI.getInitialContext( null );
                
        //BasicDataSourceFactory factory = new BasicDataSourceFactory();
        final int TECHNIQUE = DATASOURCE;
        if( TECHNIQUE == DATASOURCE ){
            OracleDataSource source = new OracleDataSource();
            //source.setDriverType( fixture.getProperty("driverClassName"));
            
            //source.setPortNumber( Integer.parseInt( fixture.getProperty("port")) );
            //source.setServerName( fixture.getProperty("host"));
            source.setUser( fixture.getProperty("user"));
            source.setPassword( fixture.getProperty("password"));
            source.setURL( fixture.getProperty("url"));
            
            connection = source.getConnection();
        }
        else if (TECHNIQUE == DRIVER ){
            String host = fixture.getProperty("host");
            String port = fixture.getProperty("port");
            String database = fixture.getProperty("database");
            String url = JDBC_PATH + host + ":" + port + ":" + database;
            
            String user = fixture.getProperty("user");
            String password = fixture.getProperty("password");
            connection = DriverManager.getConnection(url, user, password);
        }
        
        DatabaseMetaData metaData = connection.getMetaData();        
        
        //dump( "schemas", metaData.getSchemas() );
        String user = fixture.getProperty("user").toUpperCase();
        ResultSet epsgTables = metaData.getTables( null, user, "EPSG%", null );
        List list = new ArrayList();
        while( epsgTables.next() ){
            list.add( epsgTables.getObject( 3 ));
        }
        if( list.isEmpty() ){
            throw new SQLException("Could not find EPSG tables");
        }
        System.out.println( list );
    }   

    protected void disconnect() throws Exception {
        connection.close();
        connection = null;
        datasource = null;
    }
    
    public void testConnection() throws Exception{
        assertNotNull( connection );
        
        DatabaseMetaData metaData = connection.getMetaData();     
        assertEquals( fixture.getProperty("user").toUpperCase(), metaData.getUserName() );
        
        //dump( "schemas", metaData.getSchemas() );
        //dump( "tables", metaData.getTables( null, "JODY_BRUNSWICK", "EPSG%", null ) );        
    }
    void dump( String handle, ResultSet results ) throws Exception {
        ResultSetMetaData metaData = results.getMetaData();        
        for( int col=1; col <= metaData.getColumnCount(); col++ ){
            System.out.print( metaData.getColumnName( col ) );
            System.out.print( "\t" );            
        }        
        for(  results.next(); !results.isAfterLast(); results.next() ){            
            for( int col=1; col <= metaData.getColumnCount(); col++ ){
                System.out.print( results.getObject( col ) );
                System.out.print( "\t" );                
            }
            System.out.println();
        }       
    }
}
