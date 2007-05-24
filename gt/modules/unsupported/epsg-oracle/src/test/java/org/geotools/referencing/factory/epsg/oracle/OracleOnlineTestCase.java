package org.geotools.referencing.factory.epsg.oracle;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import oracle.jdbc.pool.OracleDataSource;
//import oracle.jdbc.pool.OracleDataSourceFactory;

import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.geotools.factory.GeoTools;
import org.geotools.factory.JNDI;
import org.geotools.referencing.factory.epsg.DefaultFactory;
import org.geotools.test.OnlineTestCase;

/**
 * This represents an online test case.
 * <p>
 * To run this test you
 * will need to supply a "fixture" in "%USERHOME%/.geotools/epsg/oracle.properties"
 * The contents of this file are as follows:
 * <pre><code>
 * user=****
 * password=****
 * url=jdbc:oracle:thin:@ant:1521:orcl
 * </code></pre>
 * <p>
 * This test requires ojdbc14.jar in order to run.
 * <p>
 * <b>Instructions for Maven:</b> set the environmental variable
 * "oracle.jdbc" to true (this will enable the oracle.jdbc-true profile and disable
 * the oracle.jdbc-false profile) ... basically sticking a real ojdbc14.jar in your
 * path so we have a real driver to work with.
 * <p>
 * @author Jody
 *
 */
public class OracleOnlineTestCase extends OnlineTestCase {
    DataSource datasource;
    
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
        //BasicDataSourceFactory factory = new BasicDataSourceFactory();
        final int TECHNIQUE = DATASOURCE;
        OracleDataSource source;
        source = new OracleDataSource();

        source.setUser( fixture.getProperty("user"));
        source.setPassword( fixture.getProperty("password"));
        source.setURL( fixture.getProperty("url"));
        
        datasource = source;

        Connection connection = source.getConnection();        
        try {
            DatabaseMetaData metaData = connection.getMetaData();        
            String user = fixture.getProperty("user").toUpperCase();
            ResultSet epsgTables = metaData.getTables( null, user, "EPSG%", null );
            List list = new ArrayList();
            while( epsgTables.next() ){
                list.add( epsgTables.getObject( 3 ));
            }
            if( list.isEmpty() ){
                throw new SQLException("Could not find EPSG tables");
            }
        }
        finally {
            connection.close();
        }
        
        //System.out.println( list );
        Hashtable env = new Hashtable();
        env.put( Context.INITIAL_CONTEXT_FACTORY, "org.osjava.sj.memory.MemoryContextFactory" );
        
        InitialContext context = new InitialContext(env);
        String name = context.composeName("jdbc/EPSG", ""); // jdbc/EPSG
        //System.out.println( name );
        context.bind( name, source );
        
        JNDI.init( context );
    }

    protected void disconnect() throws Exception {
        datasource = null;
    }
    /*
    public void XtestConnection() throws Exception{
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
    */
}
