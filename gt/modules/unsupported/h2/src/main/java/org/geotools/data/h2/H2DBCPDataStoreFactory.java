package org.geotools.data.h2;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.pool.KeyedObjectPoolFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPoolFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.geotools.data.AbstractDataStoreFactory;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.jdbc.ConnectionManager;
import org.geotools.data.jdbc.DataSourceManager;
import org.geotools.data.jdbc.JDBCDataStore;

/**
 * Factory will connect to the H2 database, internally this class makes use of a DBCP
 * dataSource supporting Pooling (please see H2DirectStoreFactory if you want to use
 * your own DataSource).
 * <p>
 * Please see the public static Params for a description of what is needed to connect to this
 * beast. The information is available in a dynamic fashion via getPrametersInfo() if you
 * are building a user interface on the fly.
 * </ul>
 * <p>
 * Notes and examples:
 * <ul>
 * <li>H2 configuration is in your local home directory - .h2.server.properties
 * <br>C:\Documents and Settings\[username]\.h2.server.properties
 * <li>url:
 * <br>"jdbc:h2:test"
 * <br>"jdbc:h2:test;IFEXISTS=TRUE"
 * <br>"jdbc:h2:file:/data/sample;IFEXISTS=TRUE"
 * <br>"jdbc:h2:mem:test_mem"
 * <li>user: "sa" (for system administrator)
 * <li>passowrd: ""
 * </ul>
 * <p>
 * Implementation note: This implementation <b>does not use a DBTYPE</b> parameter, instead the URL_PARAM
 * is checked to ensure it starts with "jdbc:h2". 
 * 
 * @author Jody Garnett
 */
public class H2DBCPDataStoreFactory extends AbstractDataStoreFactory {
    
    private static final String H2_DRIVER_CLASSNAME = "org.h2.Driver";
    public static final Param URL_PARAM = new Param("url",String.class,"Example: jdbc:h2:file:/data/sample", true, "jdbc:h2:mem:test_mem" );
    public static final Param USERNAME_PARAM =  new Param("user",String.class,"Username", true, "sa" );
    public static final Param PASSWORD_PARAM =  new Param("user",String.class,"Password", true, "" );
    
    public DataStore createDataStore( Map params ) throws IOException {
        String url = (String) URL_PARAM.lookUp( params );
        String user = (String) USERNAME_PARAM.lookUp( params );
        String password = (String) PASSWORD_PARAM.lookUp( params );
        
        url += ";IFEXISTS=TRUE"; // do not create if it does not exist
        DataSource dataSource = aquireDataSource( url, user, password );
        
        // ConnectionManager if needed to bridge to old JDBCDataStore code
        // ConnectionManager connectionManager = new DataSourceManager( dataSource );
        JDBCDataStore dataStore = new JDBCDataStore();
        dataStore.setDataSource( dataSource );
        
        return dataStore;
    }

    public DataStore createNewDataStore( Map params ) throws IOException {
        String url = (String) URL_PARAM.lookUp( params );
        String user = (String) USERNAME_PARAM.lookUp( params );
        String password = (String) PASSWORD_PARAM.lookUp( params );
        
        DataSource dataSource = aquireDataSource( url, user, password );
        
        JDBCDataStore dataStore = new JDBCDataStore();
        dataStore.setDataSource( dataSource );
        
        return dataStore;
    }
    
    private DataSource aquireDataSource( String url, String user, String password ) {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName( H2_DRIVER_CLASSNAME );
        dataSource.setUrl( url );
        dataSource.setUsername( user );
        dataSource.setPassword( password );
        dataSource.setAccessToUnderlyingConnectionAllowed( true );
        
        final int CONNECTIONS = 10;
        dataSource.setMaxActive( CONNECTIONS );
        dataSource.setMaxIdle( CONNECTIONS / 2 );
        dataSource.setMinIdle( CONNECTIONS / 4 );
        
        final int PREPAIRED_STATEMENTS = 5; // should aquire from Hint
        dataSource.setPoolPreparedStatements( PREPAIRED_STATEMENTS != 0 );
        if( PREPAIRED_STATEMENTS != 0 ){
            dataSource.setMaxOpenPreparedStatements( PREPAIRED_STATEMENTS );            
        }
        return dataSource;
    }

    /** This is good example code - but not sure how to hook it up? */
    private PoolableConnectionFactory createConnectionFactory( String url, String user, String password ) {
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory( url, user, password );
        
        GenericObjectPool connectionPool = new GenericObjectPool();
        
        KeyedObjectPoolFactory statementPool = new GenericKeyedObjectPoolFactory(null);
        final boolean defaultReadOnly = false;
        final boolean defaultAutoCommit = true;
        final String validationQuery = null;
        
       return new PoolableConnectionFactory( connectionFactory, connectionPool, statementPool, validationQuery, defaultReadOnly, defaultAutoCommit );
    }

    public String getDescription() {
        return "The H2 Database is Java and can be used for temporary, or persistent storage.";
    }

    public String getDisplayName() {
        return "H2";
    }

    public Param[] getParametersInfo() {
        return new Param[]{
                URL_PARAM, USERNAME_PARAM, PASSWORD_PARAM                
        };
    }

    public boolean isAvailable() {
         try {
            Class.forName(H2_DRIVER_CLASSNAME);            
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }

}
