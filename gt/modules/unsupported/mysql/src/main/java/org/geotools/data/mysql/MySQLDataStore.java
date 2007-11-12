/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.data.mysql;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.logging.Logger;
import javax.sql.DataSource;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import org.opengis.feature.type.AttributeDescriptor;
import org.geotools.data.DataSourceException;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.jdbc.JDBCDataStore;
import org.geotools.data.jdbc.JDBCDataStoreConfig;
import org.geotools.data.jdbc.JDBCFeatureWriter;
import org.geotools.data.jdbc.QueryData;
import org.geotools.data.jdbc.SQLBuilder;
import org.geotools.data.jdbc.attributeio.AttributeIO;
import org.geotools.data.jdbc.attributeio.WKTAttributeIO;
import org.geotools.data.jdbc.datasource.DataSourceUtil;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.filter.Filter;
import org.geotools.filter.SQLEncoderMySQL;


/**
 * An implementation of the GeoTools Data Store API for the MySQL database platform.
 * The plan is to support traditional MySQL datatypes, as well as the new geometric
 * datatypes provided with MySQL 4.1 and higher.<br>
 * <br>
 * TODO:  MySQLDataStore is not yet tested for MySQL 4.1's geometric datatypes.<br>
 * <br>
 * Please see {@link org.geotools.data.jdbc.JDBCDataStore class JDBCDataStore} and
 * {@link org.geotools.data.DataStore interface DataStore} for DataStore usage details.
 * @author Gary Sheppard garysheppard@psu.edu
 * @author Andrea Aime aaime@users.sourceforge.net
 * @author Debasish Sahu debasish.sahu@rmsi.com
 * @source $URL$
 */
public class MySQLDataStore extends JDBCDataStore {
    /** The logger for the mysql module. */
    private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geotools.data.mysql");

    /**
     * Basic constructor for MySQLDataStore.
     * be done similar to the following:<br>
     * @param dataSource A source of connections for this datastore
     * @throws IOException if the database cannot be properly accessed
     * @see javax.sql.DataSource
     * @see org.geotools.data.mysql.MySQLConnectionFactory
     */
    public MySQLDataStore(DataSource dataSource) throws IOException {
        this(dataSource, null);
    }

    /**
     * Constructor for MySQLDataStore where the database schema name is provided.
     * @param dataSource A source of connections for this datastore
     * @param databaseSchemaName the database schema.  Can be null.  See the comments for the parameter schemaPattern in {@link java.sql.DatabaseMetaData#getTables(String, String, String, String[]) DatabaseMetaData.getTables}, because databaseSchemaName behaves in the same way.
     * @throws IOException if the database cannot be properly accessed
     */
    public MySQLDataStore(DataSource dataSource, String databaseSchemaName)
        throws IOException {
        this(dataSource, databaseSchemaName, null);
    }

    /**
     * Constructor for MySQLDataStore where the database schema name is provided.
     * @param dataSource A source of connections for this datastore
     * @param databaseSchemaName the database schema.  Can be null.  See the comments for the parameter schemaPattern in {@link java.sql.DatabaseMetaData#getTables(String, String, String, String[]) DatabaseMetaData.getTables}, because databaseSchemaName behaves in the same way.
     * @param namespace the namespace for this data store.  Can be null, in which case the namespace will simply be the schema name.
     * @throws IOException if the database cannot be properly accessed
     */
    public MySQLDataStore(DataSource dataSource, String databaseSchemaName, String namespace)
        throws IOException {
        super(dataSource,
            JDBCDataStoreConfig.createWithNameSpaceAndSchemaName(namespace, databaseSchemaName));
    }

    /**
     * A utility method for creating a MySQLDataStore from database connection parameters,
     * using the default port (3306) for MySQL.
     * @param host the host name or IP address of the database server
     * @param schema the name of the database instance
     * @param username the database username
     * @param password the password corresponding to <code>username</code>
     * @return a MySQLDataStore for the specified parameters
     */
    public static MySQLDataStore getInstance(String host, String schema, String username,
        String password) throws IOException, SQLException {
        return getInstance(host, 3306, schema, username, password);
    }

    /**
     * Utility method for creating a MySQLDataStore from database connection parameters.
     * @param host the host name or IP address of the database server
     * @param port the port number of the database
     * @param schema the name of the database instance
     * @param username the database username
     * @param password the password corresponding to <code>username</code>
     * @throws IOException if the MySQLDataStore cannot be created because the database cannot be properly accessed
     * @throws SQLException if a MySQL connection pool cannot be established
     */
    public static MySQLDataStore getInstance(String host, int port, String schema, String username,
        String password) throws IOException, SQLException {
        String url = "jdbc:mysql://" + host + ":" + port + "/" + schema;
        String driver = "com.mysql.jdbc.Driver";

        return new MySQLDataStore(DataSourceUtil.buildDefaultDataSource(url, driver, username,
                password, "select version()"));
    }

    /**
     * Utility method for getting a FeatureWriter for modifying existing features,
     * using no feature filtering and auto-committing.  Not used for adding new
     * features.
     * @param typeName the feature type name (the table name)
     * @return a FeatureWriter for modifying existing features
     * @throws IOException if the database cannot be properly accessed
     */
    public FeatureWriter getFeatureWriter(String typeName)
        throws IOException {
        return getFeatureWriter(typeName, Filter.INCLUDE, Transaction.AUTO_COMMIT);
    }

    /**
     * Utility method for getting a FeatureWriter for adding new features, using
     * auto-committing.  Not used for modifying existing features.
     * @param typeName the feature type name (the table name)
     * @return a FeatureWriter for adding new features
     * @throws IOException if the database cannot be properly accessed
     */
    public FeatureWriter getFeatureWriterAppend(String typeName)
        throws IOException {
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
    protected AttributeDescriptor buildAttributeType(ResultSet rs)
        throws IOException {
        final int COLUMN_NAME = 4;
        final int DATA_TYPE = 5;
        final int TYPE_NAME = 6;

        try {
            int dataType = rs.getInt(DATA_TYPE);

            if (dataType == Types.OTHER) {
                //this is MySQL-specific; handle it
                String typeName = rs.getString(TYPE_NAME);
                String typeNameLower = typeName.toLowerCase();
                AttributeTypeBuilder builder = new AttributeTypeBuilder();

                //TODO: Get at CRS info, put geometry stuff in its own method
                if ("geometry".equals(typeNameLower)) {
                    builder.setBinding(Geometry.class);
                } else if ("point".equals(typeNameLower)) {
                    builder.setBinding(Point.class);
                } else if ("linestring".equals(typeNameLower)) {
                    builder.setBinding(LineString.class);
                } else if ("polygon".equals(typeNameLower)) {
                    builder.setBinding(Polygon.class);
                } else if ("multipoint".equals(typeNameLower)) {
                    builder.setBinding(MultiPoint.class);
                } else if ("multilinestring".equals(typeNameLower)) {
                    builder.setBinding(MultiLineString.class);
                } else if ("multipolygon".equals(typeNameLower)) {
                    builder.setBinding(MultiPolygon.class);
                } else if ("geometrycollection".equals(typeNameLower)) {
                    builder.setBinding(GeometryCollection.class);
                } else {
                    //nothing else we can do
                    return super.buildAttributeType(rs);
                }
                builder.setNillable(true);
                builder.setName(rs.getString(COLUMN_NAME));
                return builder.buildDescriptor(rs.getString(COLUMN_NAME));
            } else {
                return super.buildAttributeType(rs);
            }
        } catch (SQLException e) {
            throw new IOException("SQL exception occurred: " + e.getMessage());
        }
    }

    /**
     * @see org.geotools.data.jdbc.JDBCDataStore#getSqlBuilder(java.lang.String)
     */
    public SQLBuilder getSqlBuilder(String typeName) throws IOException {
        //SQLEncoder encoder = new SQLEncoderMySQL(); replace with this once
        //it is fully tested, the test cases work, but I don't have a live
        //mysql database. -ch
        //SQLEncoder encoder = new SQLEncoder();
        SQLEncoderMySQL encoder = new SQLEncoderMySQL();
        encoder.setFIDMapper(getFIDMapper(typeName));

        return new MySQLSQLBuilder(encoder, getSchema(typeName));
    }

    /**
     * @see org.geotools.data.jdbc.JDBCDataStore#getGeometryAttributeIO(org.geotools.feature.AttributeType)
     */
    protected AttributeIO getGeometryAttributeIO(AttributeDescriptor type, QueryData queryData) {
        return new WKTAttributeIO();
    }

    protected JDBCFeatureWriter createFeatureWriter(FeatureReader reader, QueryData queryData)
        throws IOException {
        LOGGER.fine("returning jdbc feature writer");

        return new MySQLFeatureWriter(reader, queryData);
    }
}
