package org.geotools.data.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.geotools.data.Transaction;
import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.data.store.ContentState;
import org.geotools.feature.FeatureType;
import org.opengis.feature.simple.SimpleTypeFactory;

public class JDBCDataStore extends ContentDataStore {
	/**
	 * logging instance
	 */
	static final Logger LOGGER = Logger.getLogger( "org.geotools.data.jdbc" );
	/**
	 * data source
	 */
	protected DataSource dataSource;
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

    public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
    
    public DataSource getDataSource() {
		return dataSource;
	}
    
    public Logger getLogger() {
    	return LOGGER;
    }
    
    public void createSchema(final FeatureType featureType) throws IOException {
  
        if (entry(name(featureType.getTypeName())) != null) {
            String msg = "Schema '" + featureType.getTypeName() + "' already exists";
            throw new IllegalArgumentException(msg);
        }

        //create a content entry for the type
        ContentEntry entry = new ContentEntry( this, name( featureType.getTypeName() ) );
        
        //cache the feature type
        entry.getState( Transaction.AUTO_COMMIT ).setMemberType( featureType );
        
        JDBCUtils.statement( this, new JDBCRunnable() {
        	public Object run(Statement st) throws IOException, SQLException {
					SQLBuilder sqlBuilder = new SQLBuilder( JDBCDataStore.this );
		        	st.execute( sqlBuilder.createTable( featureType ) );
		        	return null;
		        }
	    	}
		);
    }
    
    public PrimaryKey getPrimaryKey( ContentEntry entry ) throws IOException {
	    try {
	    	return JDBCUtils.primaryKey( entry.getName(), this );
	    } 
		catch (Exception e) {
			throw (IOException) new IOException().initCause( e );
		}
    }
    
    public PrimaryKey getPrimaryKey( FeatureType featureType ) throws IOException {
    	return getPrimaryKey( ensureEntry( ( name( featureType.getTypeName() ) ) ) );
	}
    
    /**
     * @return A type builder for building feature types.
     */
    protected JDBCTypeBuilder createTypeBuilder() {
        return new JDBCTypeBuilder( getTypeFactory() );
    }
    
    /**
     * @return A type builder for building feature types.
     */
    protected JDBCTypeBuilder createTypeBuilder( SimpleTypeFactory typeFactory ) {
        return new JDBCTypeBuilder( typeFactory );
    }
    
    /**
     * @return A sql builder for encoding sql statements.
     */
    protected SQLBuilder createSQLBuilder() {
    	return new SQLBuilder( this );
    }
    
    /**
     * Convenience method for grabbing a new connection.
     * <p>
     * Callers of this method should close the connection when done with it.
     * </p>.
     *
     */
    public final Connection connection() {
        try {
			return getDataSource().getConnection();
		} 
        catch (SQLException e) {
        	throw new RuntimeException( "Unable to obtain connection", e );
		}
    }
    
    protected ContentFeatureSource createFeatureSource(ContentEntry entry) throws IOException {
        return new JDBCFeatureSource( entry );
    }
    
    protected ContentState createContentState(ContentEntry entry) {
    	return new JDBCState( entry );
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
