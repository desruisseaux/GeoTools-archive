package org.geotools.referencing.factory.epsg.oracle;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.geotools.factory.JNDI;
import org.geotools.test.OnlineTestCase;

public class OracleOnlineTest extends OnlineTestCase {
    DataSource datasource;
    Connection connection;
    
    protected String getFixtureId() {
        return "epsg.oracle";
    }

    protected void connect() throws Exception {
        Context context = JNDI.getInitialContext( null );
        BasicDataSourceFactory factory = new BasicDataSourceFactory();
        datasource = factory.createDataSource( fixture );
        
        connection = datasource.getConnection();
    }   

    protected void disconnect() throws Exception {
        connection.close();
        connection = null;
        datasource = null;
    }
    
    public void testConnection() throws Exception{
        assertNotNull( connection );
        DatabaseMetaData metaData = connection.getMetaData();
        System.out.println( "username:"+ metaData.getUserName() );
        ResultSet catalogs = metaData.getCatalogs();
        ResultSetMetaData catalogsMetaData = catalogs.getMetaData();
        for( catalogs.first(); !catalogs.isAfterLast(); catalogs.next() ){
            for( int col=1; col <= catalogsMetaData.getColumnCount(); col++ ){
                System.out.print( catalogsMetaData.getCatalogName( col ) );
                System.out.print( "=" );
                System.out.print( catalogs.getObject( col ) );
            }
        }
    }
}
