package org.geotools.data.postgis;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.catalog.GeoResourceInfo;
import org.geotools.data.DataSourceException;
import org.geotools.data.Transaction;
import org.geotools.data.postgis.collection.TableFeatureCollection;
import org.geotools.data.postgis.table.NormalTable;
import org.geotools.data.store.Content;
import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentState;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureList;
import org.opengis.feature.type.TypeName;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;

/**
 * Access PostGIS 1.2 Content.
 * <ul>
 * <li><b>:</b></li>
 * <li><b>:</b></li>s
 * </ul>
 * Examples:<pre><code>
 * </code></pre>
 *
 * @author Jody Garnett, Refractions Research Inc.
 */
public class PostGISContent extends Content {
    /** The logger for the postgis module. */
    protected static final Logger LOGGER = Logger.getLogger(
            "org.geotools.data.postgis");

    protected final String DRIVER_CLASS = "org.postgresql.Driver";
    protected final String DRIVER_PATH = "jdbc:postgresql";
    private Connection connection;
    private PostGISConfig configuration;
    
    public PostGISContent( Properties fixture ) throws SQLException {
        this( new PostGISConfig( fixture ));
    }
    public PostGISContent( PostGISConfig config ) throws SQLException {
        this.configuration = config;
        String connPath = DRIVER_PATH + "://" + config.getHost() + ":" + config.getHost()+ "/" + config.getDatabase();
        
        // Instantiate the driver classes
        try {
            Class.forName(DRIVER_CLASS);
            
            Properties info = new Properties();
            info.put( "username", config.getUser() );
            info.put( "password", config.getPassword() );
            
            connection = DriverManager.getConnection(connPath, info);            
        } catch (ClassNotFoundException cnfe) {
            throw new SQLException("Postgis driver was not found.");
        }
        connection.setAutoCommit(false);
    }
    public Connection getConnection(Transaction transaction){
        return connection; //shared until we figure poolness
    }
    public ContentEntry entry(ContentDataStore dataStore, TypeName typeName) {
        return new NormalTable( (PostgisDataStore) dataStore, typeName );
    }
    
    public FeatureCollection all(ContentState state) {
        return new TableFeatureCollection( this, state );
    }

    public FeatureCollection filter(ContentState state, Filter filter) {
        return null;
    }

    public GeoResourceInfo info(ContentState state) {
        return null;
    }

    public FeatureCollection readonly(ContentState state, Filter filter) {
        return null;
    }

    public FeatureList sorted(ContentState state, Filter filter, SortBy sort) {
        return null;
    }

    public ContentState state(ContentEntry entry) {
        return null;
    }
    public List getTypeNames() throws IOException {        
        final int TABLE_NAME_COL = 3;
        Connection conn = null;
        List list = new ArrayList();

        try {
            conn = getConnection(Transaction.AUTO_COMMIT);
            
            // DB: shouldnt this be done by looking at geometry_columns?
            // or are you trying to allow non-spatial tables in as well?
            DatabaseMetaData meta = conn.getMetaData();
            String[] tableType = { "TABLE" , "VIEW"};
            ResultSet tables = meta.getTables(null,
                    configuration.getSchema(), "%", tableType);

            while (tables.next()) {
                String tableName = tables.getString(TABLE_NAME_COL);
                if (tableName.equals("geometry_columns") ) continue;
                if (tableName.startsWith("spatial_ref_sys")) continue;
                
                list.add( new org.geotools.feature.type.TypeName(configuration.getNamespace(),tableName));
            }            
            tables.close();

            return list;
        } catch (SQLException sqlException) {
            close(conn, Transaction.AUTO_COMMIT, sqlException);
            conn = null;

            String message = "Error querying database for list of tables:"
                + sqlException.getMessage();
            throw (IOException) new IOException(message).initCause(sqlException);
        } finally {
            close(conn, Transaction.AUTO_COMMIT, null);
        }
    }
    public void close(Connection conn, Transaction transaction, SQLException sqlException) {
            if (conn == null) {
                // Assume we have already closed the connection
                // (allows use of method in a finally block)
                return;
            }
        
            if (transaction != Transaction.AUTO_COMMIT) {
                // we should not close Transaction connections
                // they will do this themselves when they are finished
                // with the connection.
                if (sqlException != null) {
                    // we are closing due to an SQLException                
                    try {
                        transaction.rollback();
                    } catch (IOException e) {
                        String msg = "Error rolling back transaction in response"
                            + "to connection error. We are in an inconsistent state";
                        LOGGER.log(Level.SEVERE, msg, e);        
                        // TODO: this is a bad place to be should we completely gut the transaction 
                        // to prevent damage                                                            
                        // transaction.close();
                    }
                }        
                return;
            }        
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                String msg = "Error closing JDBC Connection";
                LOGGER.log(Level.WARNING, msg, e);
            }
        }
}
