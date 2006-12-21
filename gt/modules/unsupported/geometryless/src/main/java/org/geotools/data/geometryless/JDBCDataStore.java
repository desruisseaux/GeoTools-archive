package org.geotools.data.geometryless;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.logging.Logger;

import org.geotools.data.DataSourceException;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.jdbc.ConnectionPool;
import org.geotools.data.jdbc.JDBCDataStoreConfig;
import org.geotools.data.jdbc.JDBCFeatureWriter;
import org.geotools.data.jdbc.QueryData;
import org.geotools.data.jdbc.SQLBuilder;
import org.geotools.data.jdbc.attributeio.AttributeIO;
import org.geotools.data.jdbc.attributeio.WKTAttributeIO;
import org.geotools.feature.AttributeType;
 import org.opengis.filter.Filter;
import org.geotools.filter.SQLEncoder;

/**
 * An implementation of the GeoTools Data Store API for a generic non-spatial database platform.
 * The plan is to support traditional jdbc datatypes, and support geometry held within such types (eg, x,y columns, or possibly WKT strings)<br>
 * <br>
 * Please see {@link org.geotools.data.jdbc.JDBCDataStore class JDBCDataStore} and
 * {@link org.geotools.data.DataStore interface DataStore} for DataStore usage details.
 * @author Rob Atkinson rob@socialchange.net.au
 * @source $URL$
 */

public class JDBCDataStore extends org.geotools.data.jdbc.JDBCDataStore {
    /** The logger for the mysql module. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.data.geometryless");

    /**
     * Basic constructor for JDBCDataStore.  Requires creation of a
     * {@link org.geotools.data.jdbc.ConnectionPool ConnectionPool}, which could
     * be done similar to the following:<br>
     * <br>
     * <code>MySQLConnectionFactory connectionFactory = new MySQLConnectionFactory("mysqldb.geotools.org", "3306", "myCoolSchema");</code><br>
     * <code>ConnectionPool connectionPool = connectionFactory.getConnectionPool("omcnoleg", "myTrickyPassword123");</code><br>
     * <code>DataStore dataStore = new JDBCDataStore(connectionPool);</code><br>
     * @param connectionPool a MySQL {@link org.geotools.data.jdbc.ConnectionPool ConnectionPool}
     * @throws IOException if the database cannot be properly accessed
     * @see org.geotools.data.jdbc.ConnectionPool
     * @see org.geotools.data.mysql.MySQLConnectionFactory
     */
    public JDBCDataStore(ConnectionPool connectionPool) throws IOException {
        super(connectionPool, new JDBCDataStoreConfig() );
    }

    /** <code>DEFAULT_NAMESPACE</code> field */
    public static String DEFAULT_NAMESPACE = "http://geotools.org/jdbc";
    
    /**
     * Constructor for JDBCDataStore where the database schema name is provided.
     * @param connectionPool a MySQL {@link org.geotools.data.jdbc.ConnectionPool ConnectionPool}
     * @param databaseSchemaName the database schema.  Can be null.  See the comments for the parameter schemaPattern in {@link java.sql.DatabaseMetaData#getTables(String, String, String, String[]) DatabaseMetaData.getTables}, because databaseSchemaName behaves in the same way.
     * @throws IOException if the database cannot be properly accessed
     */
    public JDBCDataStore(ConnectionPool connectionPool, String databaseSchemaName)        
        throws IOException {
        this( connectionPool, databaseSchemaName, DEFAULT_NAMESPACE );        
    }

    /**
     * Constructor for JDBCDataStore where the database schema name is provided.
     * @param connectionPool a MySQL {@link org.geotools.data.jdbc.ConnectionPool ConnectionPool}
     * @param databaseSchemaName the database schema.  Can be null.  See the comments for the parameter schemaPattern in {@link java.sql.DatabaseMetaData#getTables(String, String, String, String[]) DatabaseMetaData.getTables}, because databaseSchemaName behaves in the same way.
     * @param namespace the namespace for this data store.  Can be null, in which case the namespace will simply be the schema name.
     * @throws IOException if the database cannot be properly accessed
     */
    public JDBCDataStore(
        ConnectionPool connectionPool,
        String databaseSchemaName,
        String namespace)
        throws IOException {
        super(
            connectionPool,
            JDBCDataStoreConfig.createWithNameSpaceAndSchemaName(namespace, databaseSchemaName));
    }

    /**
     * A utility method for creating a JDBCDataStore from database connection parameters,
     * using the default port (3306) for MySQL.
     * @param host the host name or IP address of the database server
     * @param schema the name of the database instance
     * @param username the database username
     * @param password the password corresponding to <code>username</code>
     * @return a JDBCDataStore for the specified parameters

    public static JDBCDataStore getInstance(
        String host,
        String schema,
        String username,
        String password)
        throws IOException, SQLException {
        return getInstance(host, 3306, schema, username, password);
    }
    */
    /**
     * Utility method for creating a JDBCDataStore from database connection parameters.
     * @param host the host name or IP address of the database server
     * @param port the port number of the database
     * @param schema the name of the database instance
     * @param username the database username
     * @param password the password corresponding to <code>username</code>
     * @throws IOException if the JDBCDataStore cannot be created because the database cannot be properly accessed
     * @throws SQLException if a MySQL connection pool cannot be established
 
    public static JDBCDataStore getInstance(
        String host,
        int port,
        String schema,
        String username,
        String password)
        throws IOException, SQLException {
        return new JDBCDataStore(
            new MySQLConnectionFactory(host, port, schema).getConnectionPool(username, password));
    }
    */
    /**
     * Utility method for getting a FeatureWriter for modifying existing features,
     * using no feature filtering and auto-committing.  Not used for adding new
     * features.
     * @param typeName the feature type name (the table name)
     * @return a FeatureWriter for modifying existing features
     * @throws IOException if the database cannot be properly accessed
     */
    public FeatureWriter getFeatureWriter(String typeName) throws IOException {
        return getFeatureWriter(typeName, Filter.INCLUDE, Transaction.AUTO_COMMIT);
    }

    /**
     * Utility method for getting a FeatureWriter for adding new features, using
     * auto-committing.  Not used for modifying existing features.
     * @param typeName the feature type name (the table name)
     * @return a FeatureWriter for adding new features
     * @throws IOException if the database cannot be properly accessed
     */
    public FeatureWriter getFeatureWriterAppend(String typeName) throws IOException {
        return getFeatureWriterAppend(typeName, Transaction.AUTO_COMMIT);
    }

    /**
     * Constructs an AttributeType from a row in a ResultSet. The ResultSet
     * contains the information retrieved by a call to  getColumns() on the
     * DatabaseMetaData object.  This information  can be used to construct an
     * Attribute Type.
     * 
     * <p>
     * In addition to standard SQL types, this method identifies MySQL 4.1's geometric
     * datatypes and creates attribute types accordingly.  This happens when the
     * datatype, identified by column 5 of the ResultSet parameter, is equal to
     * java.sql.Types.OTHER.  If a Types.OTHER ends up not being geometric, this
     * method simply calls the parent class's buildAttributeType method to do something
     * with it.
     * </p>
     * 
     * <p>
     * Note: Overriding methods must never move the current row pointer in the
     * result set.
     * </p>
     *
     * @param rs The ResultSet containing the result of a
     *        DatabaseMetaData.getColumns call.
     *
     * @return The AttributeType built from the ResultSet.
     *
     * @throws SQLException If an error occurs processing the ResultSet.
     * @throws DataSourceException Provided for overriding classes to wrap
     *         exceptions caused by other operations they may perform to
     *         determine additional types.  This will only be thrown by the
     *         default implementation if a type is present that is not present
     *         in the TYPE_MAPPINGS.
     */
    protected AttributeType buildAttributeType(ResultSet rs) throws IOException {
        final int COLUMN_NAME = 4;
        final int DATA_TYPE = 5;
        final int TYPE_NAME = 6;

        try {
            int dataType = rs.getInt(DATA_TYPE);
             LOGGER.fine("dataType: " + dataType + " " + rs.getString(TYPE_NAME) + " " + rs.getString(COLUMN_NAME) );
   
            if (dataType == Types.OTHER) {
                //this is MySQL-specific; handle it
                String typeName = rs.getString(TYPE_NAME);
                String typeNameLower = typeName.toLowerCase();
	return super.buildAttributeType(rs);
            } else {
                return super.buildAttributeType(rs);
            }
        } catch (SQLException e) {
            throw new IOException("SQL exception occurred: " + e.getMessage());
        }
    }

    public SQLBuilder getSqlBuilder(String typeName) throws IOException {
    	
    
        SQLEncoder encoder = new SQLEncoder();
        encoder.setFIDMapper(getFIDMapper(typeName));
        return new GeometrylessSQLBuilder(encoder);
    }

    /**
     * @see org.geotools.data.jdbc.JDBCDataStore#getGeometryAttributeIO(org.geotools.feature.AttributeType)
     */
    protected AttributeIO getGeometryAttributeIO(AttributeType type, QueryData queryData) {
        return new WKTAttributeIO();
    }

    protected JDBCFeatureWriter createFeatureWriter(FeatureReader reader, QueryData queryData)
        throws IOException {
        LOGGER.fine("returning jdbc feature writer");

        return new GeometrylessFeatureWriter(reader, queryData);
    }

}
