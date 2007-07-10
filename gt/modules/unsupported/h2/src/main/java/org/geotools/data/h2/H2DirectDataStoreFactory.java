package org.geotools.data.h2;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import javax.naming.NamingException;
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
import org.geotools.factory.GeoTools;

/**
 * Factory will connect to the H2 database using a DataSource you provide.
 * <p>
 * If you don't know what that means please use H2DBCPDataStoreFactory (it will
 * make use of the popular commons dbcp library as an implementation of DataSource).
 * <p>
 * Please see the public static Params for a description of what is needed to connect to this
 * beast. The information is available in a dynamic fashion via getPrametersInfo() if you
 * are building a user interface on the fly.
 * <p>
 * Please note that a data source name is required (even if you are not using JNDI) -
 * it is good Java EE Manners to register your DataSource and this gives us a chance to
 * check up on you, the name will also be used in log messages.
 * </p>
 * @author Jody Garnett
 */
public class H2DirectDataStoreFactory extends AbstractDataStoreFactory {
    
    private static final String H2_DRIVER_CLASSNAME = "org.h2.Driver";
    public static final Param DATASOURCE_NAME_PARAM = new Param("dataSourceName", String.class,"Name to look up via JNDI",true );
    public static final Param DATASOURCE_PARAM = new Param("dataSource", DataSource.class,"DataSource to use",false );
    
    public DataStore createDataStore( Map params ) throws IOException {
        String name = (String) DATASOURCE_NAME_PARAM.lookUp( params );
        DataSource dataSource = (DataSource) DATASOURCE_PARAM.lookUp( params );
        
        if( name == null ){
            throw new IOException("DataSource name required");
        }
        if( dataSource == null ){
            try {
                dataSource = (DataSource) GeoTools.getInitialContext( GeoTools.getDefaultHints() ).lookup( name );
            } catch (NamingException e) {
                throw (IOException) new IOException("Could not locate DataSource:"+e).initCause(e);
            }            
        }
        else {
            try {
                GeoTools.getInitialContext( GeoTools.getDefaultHints() ).bind( name, dataSource );
            } catch (NamingException e) {
                // log warning
            }
        }
        // ConnectionManager if needed to bridge to old JDBCDataStore code
        // ConnectionManager connectionManager = new DataSourceManager( dataSource );
        JDBCDataStore dataStore = new JDBCDataStore();
        dataStore.setDataSource( dataSource );
        
        return dataStore;
    }

    public DataStore createNewDataStore( Map params ) throws IOException {
        throw new UnsupportedOperationException("You must provide an existing DataSource");
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
        return "Access to an registered H2 Database.";
    }

    public String getDisplayName() {
        return "H2 DataSource";
    }

    public Param[] getParametersInfo() {
        return new Param[]{
                DATASOURCE_NAME_PARAM, DATASOURCE_PARAM                
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
