package org.geotools.data.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.ConnectionPoolDataSource;

import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.opengis.feature.simple.SimpleTypeFactory;

public class JDBCDataStore extends ContentDataStore {

    /**
     * connection pool
     */
    protected ConnectionPoolDataSource connectionPoolDataSource;
    
    /**
     * The database schema.
     */
    protected String databaseSchema;

    public void setDatabaseSchema(String databaseSchema) {
        this.databaseSchema = databaseSchema;
    }
    
    public String getDatabaseSchema() {
        return databaseSchema;
    }
    
    public void setConnectionPoolDataSource(
            ConnectionPoolDataSource connectionPoolDataSource) {
        this.connectionPoolDataSource = connectionPoolDataSource;
    }
    
    public ConnectionPoolDataSource getConnectionPoolDataSource() {
        return connectionPoolDataSource;
    }
    
    /**
     * @return A type builder for building feature types.
     */
    protected JDBCTypeBuilder typeBuilder() {
        return new JDBCTypeBuilder( getTypeFactory() );
    }
    
    /**
     * @return A type builder for building feature types.
     */
    protected JDBCTypeBuilder typeBuilder( SimpleTypeFactory typeFactory ) {
        return new JDBCTypeBuilder( typeFactory );
    }
    
    /**
     * Convenience method for grabbing a new connection.
     * <p>
     * Callers of this method should close the connection when done with it.
     * </p>.
     *
     */
    protected Connection connection() throws SQLException {
        return getConnectionPoolDataSource().getPooledConnection().getConnection();
    }
    
    protected ContentFeatureSource createFeatureSource(ContentEntry entry) {
        return new JDBCFeatureSource( entry );
    }

    protected List createTypeNames() throws IOException {
        try {
            return JDBCUtils.typeNames( this );
        } 
        catch (Exception e) {
            throw (IOException) new IOException().initCause( e );
        }
    }

    
}
