/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.data.oracle;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import oracle.jdbc.OracleConnection;
import oracle.sdoapi.OraSpatialManager;
import oracle.sdoapi.adapter.AdapterSDO;
import oracle.sdoapi.adapter.GeometryInputTypeNotSupportedException;
import oracle.sdoapi.adapter.GeometryOutputTypeNotSupportedException;
import oracle.sdoapi.geom.GeometryFactory;
import oracle.sdoapi.geom.InvalidGeometryException;
import oracle.sdoapi.sref.SRException;
import oracle.sdoapi.sref.SRManager;
import oracle.sdoapi.sref.SpatialReference;
import oracle.sdoapi.util.GeometryMetaData;
import oracle.sql.STRUCT;
import org.geotools.data.AbstractDataSource;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataSourceMetaData;
import org.geotools.data.Query;
import org.geotools.data.jdbc.ConnectionPool;
import org.geotools.factory.FactoryConfigurationError;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.filter.Filter;
import org.geotools.filter.SQLEncoderException;
import org.geotools.filter.SQLEncoderOracle;
import org.geotools.filter.SQLUnpacker;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.data.shapefile.shp.JTSUtilities;

/**
 * Provides a DataSource implementation for Oracle Spatial Database.
 *
 * <p>This class is not suitable for subclassing.
 * 
 * @author Sean Geoghegan, Defence Science and Technology Organisation
 * @author $Author: seangeo $
 * @version $Id: OracleDataSource.java,v 1.13 2003/12/02 01:24:00 seangeo Exp $
 */
public final class OracleDataSource extends AbstractDataSource {
    /** The default column to use as the Feature ID */
    private final String DEFAULT_FID_COLUMN;
    /** A logger for logging */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.data.oracle");

    /** MetaData ResultSet column index for the column name */
    private static final int NAME_COLUMN = 4;

    /** MetaData ResultSet column index for the column type */
    private static final int TYPE_STRING_COLUMN = 6;

    /** Maps SQL types to Java classes */
    private static final Map TYPE_MAPPINGS = new HashMap();

    static {
        TYPE_MAPPINGS.put("NUMBER", Double.class);
        TYPE_MAPPINGS.put("VARCHAR", String.class);
        TYPE_MAPPINGS.put("VARCHAR2", String.class);
        TYPE_MAPPINGS.put("INT4", Integer.class);
        TYPE_MAPPINGS.put("FLOAT4", Float.class);
        TYPE_MAPPINGS.put("FLOAT8", Double.class);
    }

    /**
     * Adapter for converting between oracle.sdoapi.geom.Geometry and JTS geometries for use in
     * Geotools.
     */
    private AdapterJTS adapterJTS;

    /** The Connection to the Database */
    private OracleConnection transactionConnection = null;

    /** The connection pool */
    private ConnectionPool connectionPool;

    // End Oracle SDOAPI objects.

    /** The FID column in the feature table. Will be the primary key or DEFAULT_FID_COLUMN */
    private String fidColumn;

    /** Sequence for generating new FIDs */
    private FIDSequence fidSequence;

    /** The Spatial Reference ID of the geometry */
    private int srid = -1;

    /** Stores the schema name that may be ripped out of the table name */
    private String oraSchemaName = null;

    /** Schema of the features in the feature table */
    private FeatureType schema = null;

    /** The name of the feature table */
    private String tableName = null;

    /** Unpacks filters into those supported by the SQL encoded and those not supported. */
    private SQLUnpacker unpacker;
    /** Encodes SQL statements */
    private SqlStatementEncoder sqlEncoder;

    /**
     * Creates an OracleDataSource object for a specified tableName.
     *
     * @param connectionPool The database connection
     * @param tableName The feature table name
     *
     * @throws DataSourceException If an error occurs creating the data source.
     * @throws SQLException Occurs if there is an error getting a connection from
     * the connection pool.
     */
    public OracleDataSource(ConnectionPool connectionPool, String tableName, String fidDefault) throws DataSourceException, SQLException {
        DEFAULT_FID_COLUMN = fidDefault;
        int dotIndex = -1;

        this.connectionPool = connectionPool;

        if ((dotIndex = tableName.indexOf(".")) > -1) {
            LOGGER.finer("Splitting table name on " + dotIndex);
            this.oraSchemaName = tableName.substring(0, dotIndex);
            this.tableName = tableName.substring(dotIndex + 1);
            LOGGER.finer("oraSchemaName = " + oraSchemaName + ", tableName = " + this.tableName);
        } else {
            this.tableName = tableName;
        }

        OracleConnection conn = getConnection();

        try {
            this.fidColumn = getFidColumn(conn, tableName);
            this.schema = makeSchema(conn);
            this.fidSequence = new FIDSequence(conn, tableName, fidColumn);
        } finally {
            conn.close();
        }

        SQLEncoderOracle encoder = new SQLEncoderOracle(fidColumn, srid);

        this.unpacker = new SQLUnpacker(encoder.getCapabilities());
        this.sqlEncoder = new SqlStatementEncoder(encoder, this.tableName, fidColumn);
    }

    /**
     * Adds a FeatureCollection to the data source.  All the features in the FeatureCollection are
     * added to the data source. If an error occurs the state of the datasource before this method
     * call is restored.
     *
     * @param fc The collection of features to add to the datasource.
     *
     * @return The fids of all the added features.
     *
     * @throws DataSourceException If error occurs when adding features. If this is thrown A
     *         rollback has occurred.
     */
    public Set addFeatures(FeatureCollection fc) throws DataSourceException {
        boolean previousAutoCommit = getAutoCommit();

        setAutoCommit(false);

        boolean fail = false;
        Set fids = new TreeSet();
        String sql = sqlEncoder.makeInsertSQL(schema);
        OracleConnection conn = null;
        PreparedStatement statement = null;

        try {
            conn = getTransactionConnection();
            statement = conn.prepareStatement(sql);

            // TODO: Use FeatureIterator when its done.
            for (Iterator iter = fc.iterator(); iter.hasNext();) {
                Feature feature = (Feature) iter.next();

                if (!tableName.equals(feature.getFeatureType().getTypeName())) {
                    LOGGER.warning("Got a feature that is not of the correct type");
                    continue;
                }

                int fidInt = fidSequence.getNext();
                String fid = tableName + "." + fidInt;
                AttributeType[] attributeTypes = schema.getAttributeTypes();

                // add the fid first
                statement.setInt(1, fidInt);

                for (int j = 0; j < attributeTypes.length; j++) {
                    if (attributeTypes[j].isGeometry()) {
                        // handle the geometry
                        oracle.sdoapi.geom.Geometry geometry = adapterJTS.importGeometry(feature.getAttribute(j));
                        AdapterSDO adaptersdo = getSDOAdapter(conn, attributeTypes[j].getName());
                        Object exportedStruct = adaptersdo.exportGeometry(STRUCT.class, geometry);
                        statement.setObject(j + 2, exportedStruct);
                    } else {
                        statement.setObject(j + 2, feature.getAttribute(j));
                    }
                }

                statement.executeUpdate();
                fids.add(fid);
            }
            // J-
        } catch (SQLException e) {
            fail = true;
            String message = "Database error when adding features: " + e.getMessage();
            LOGGER.warning(message);
            throw new DataSourceException(message, e);
        } catch (InvalidGeometryException e) {
            fail = true;
            String message = "Geometry Conversion error when adding features: " + e.getMessage();
            LOGGER.warning(message);
            throw new DataSourceException(message, e);
        } catch (GeometryInputTypeNotSupportedException e) {
            fail = true;
            String message = "Geometry input type error when adding features: " + e.getMessage();
            LOGGER.warning(message);
            throw new DataSourceException(message, e);
        } catch (GeometryOutputTypeNotSupportedException e) {
            fail = true;
            String message = "Geometry output type error when adding features: " + e.getMessage();
            LOGGER.warning(message);
            throw new DataSourceException(message, e);
        } finally {
            close(statement);
            finalizeTransactionMethod(previousAutoCommit, fail);
        }
        // J+
        return fids;
    }

    /**
     * Closes a result set and catches any errors.
     *
     * @param rs The result set to close.
     */
    private void close(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            LOGGER.warning("Failed to close result set - " + e.getMessage());
        }
    }

    /**
     * Closes a statement and catches any errors.
     *
     * @param s The statement to close.
     */
    private void close(Statement s) {
        try {
            if (s != null) {
                s.close();
            }
        } catch (SQLException e) {
            LOGGER.warning("Failed to close PreparedStatement - " + e.getMessage());
        }
    }

    /**
     * Makes all transactions made since the previous commit/rollback permanent.  This method
     * should be used only when auto-commit mode has been disabled.   If autoCommit is true then
     * this method does nothing.
     *
     * @throws DataSourceException if there are any datasource errors.
     *
     * @see #setAutoCommit(boolean)
     */
    public void commit() throws DataSourceException {
        try {
            getTransactionConnection().commit();
            closeTransactionConnection();
        } catch (SQLException e) {
            String message = "problem committing";

            LOGGER.warning(message + ": " + e.getMessage());
            throw new DataSourceException(message, e);
        }
    }

    /**
     * Creates the feature from a result and a schema.
     *
     * @param result The result set to load the data from. The data is loaded from the current row
     *        in the result set.
     * @param localSchema The schema to use for the Feature.
     *
     * @return The constructed Feature.
     *
     * @throws SQLException If an error occurs reading in the data.
     * @throws DataSourceException If an error occurs converting the data into a feature. This will
     *         then be thrown by getFeature, so if somethig goes wrong loading one feature all
     *         feature loading stops.
     */
    private Feature createFeature(OracleConnection conn, ResultSet result, FeatureType localSchema)
        throws SQLException, DataSourceException {
        try {
            AttributeType[] attrTypes = localSchema.getAttributeTypes();
            Object[] attributes = new Object[localSchema.getAttributeCount()];

            // Feature ID always appears in row 1 because of the way the sql is created
            String fid = getTypeName() + "." + result.getString(1);

            // loop through the attributes starting at 0, but
            // we have to add 2 to i because of the fid column being 1
            for (int i = 0; i < attributes.length; i++) {
                if (attrTypes[i].isGeometry()) {
                    LOGGER.finest("getting Adapter");
                    AdapterSDO adaptersdo = getSDOAdapter(conn, attrTypes[i].getName());
                    LOGGER.finest("about to get geom");
                    attributes[i] =
                        adapterJTS.exportGeometry(Geometry.class, adaptersdo.importGeometry(result.getObject(i + 2)));
                    LOGGER.finest("get geom "+attributes[i]);
                } else {
                    attributes[i] = result.getObject(i + 2);
                }
            }

            return localSchema.create(attributes, fid);
            // J-    
        } catch (InvalidGeometryException e) {
            String message = "Error parsing geometry: " + e.toString();
            LOGGER.warning(message);
            throw new DataSourceException(message, e);
        } catch (GeometryOutputTypeNotSupportedException e) {
            String message = "Geometry Conversion type error: " + e.toString();
            LOGGER.warning(message);
            throw new DataSourceException(message, e);
        } catch (GeometryInputTypeNotSupportedException e) {
            String message = "Geometry Conversion type error: " + e.toString();
            LOGGER.warning(message);
            throw new DataSourceException(message, e);
        } catch (IllegalAttributeException e) {
            String message = "Error instantiating feature: " + e.toString();
            LOGGER.warning(message);
            throw new DataSourceException(message, e);
        }
        // J+
    }

    /**
     * Creates the Metadata describing the capabilities of this data source.
     *
     * @return The DataSourceMetaData that describes the capabilities of the OracleDataSource. This
     *         data source supports rollbacks, adding, modifying, removing, setting and getting
     *         features and getting the bounding box.
     *
     * @see org.geotools.data.AbstractDataSource#createMetaData()
     */
    protected DataSourceMetaData createMetaData() {
        MetaDataSupport support = new MetaDataSupport();

        support.setFastBbox(true);
        support.setSupportsRollbacks(true);
        support.setSupportsAdd(true);
        support.setSupportsModify(true);
        support.setSupportsRemove(true);
        support.setSupportsSetFeatures(true);
        support.setSupportsGetBbox(true);

        return support;
    }

    /**
     * strips the tableName from the fid for those in the format featureName.3534 should maybe just
     * strip out all alpha-numeric characters.
     *
     * @param feature The feature format the fid for.
     *
     * @return The formated feature id.
     */
    private String formatFid(Feature feature) {
        String fid = feature.getID();

        if (fid.startsWith(tableName)) {
            //take out the tableName and the .
            fid = fid.substring(tableName.length() + 1);
        }

        return fid;
    }

    private AttributeType[] getAttTypes(Query query) throws DataSourceException {
        AttributeType[] schemaTypes = schema.getAttributeTypes();

        if (query.retrieveAllProperties()) {
            return schemaTypes;
        } else {
            List attNames = new ArrayList();

            attNames.addAll(Arrays.asList(query.getPropertyNames()));

            AttributeType[] retAttTypes = new AttributeType[attNames.size()];
            int j = 0;

            for (int i = 0, n = schemaTypes.length; i < n; i++) {
                String schemaTypeName = schemaTypes[i].getName();

                if (attNames.contains(schemaTypeName)) {
                    retAttTypes[j] = schemaTypes[i];
                    j++;
                    attNames.remove(schemaTypeName);
                }
            }

            //TODO: better error reporting, and completely test this method.
            if (attNames.size() > 0) {
                String msg = "Attempted to request a property, " + attNames.get(0) + " that is not part of the schema";

                throw new DataSourceException(msg);
            }

            return retAttTypes;
        }
    }

    /**
     * Retrieves the current autoCommit mode for the current DataSource.  If the datasource does
     * not implement setAutoCommit, then this method should always return true.
     *
     * @return the current state of this datasource's autoCommit mode.
     *
     * @throws DataSourceException if a datasource access error occurs.
     *
     * @see #setAutoCommit(boolean)
     */
    public boolean getAutoCommit() throws DataSourceException {
        try {
            // If there is no transaction connection we are in autocommit.
            // otherwise we check the transaction connections status.
            if (transactionConnection == null) {
                return true;
            } else {
                return getTransactionConnection().getAutoCommit();
            }
        } catch (SQLException e) {
            String message = "problem getting auto commit";

            LOGGER.warning(message + ": " + e.getMessage());
            throw new DataSourceException(message, e);
        }
    }

    /**
     * Gets the bounding box of this Data source.
     *
     * @return The bounding box of the data source.  This is extracted from the geometry metadata.
     *
     * @see org.geotools.data.DataSource#getBbox()
     */
    public Envelope getBounds() {
        try {
            Envelope bbox = null;
            OracleConnection conn = getConnection();
            GeometryMetaData metaData =
                OraSpatialManager.getGeometryMetaData(conn, tableName, schema.getDefaultGeometry().getName());
            if (metaData != null) {
                oracle.sdoapi.geom.Envelope oraEnv = metaData.getExtent();

                bbox = new Envelope(oraEnv.getMinX(), oraEnv.getMaxX(), oraEnv.getMinY(), oraEnv.getMaxY());
            }
            LOGGER.fine("Got bounds "+bbox);
            return bbox;
        } catch (DataSourceException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /* The public instance methods of DataSource */

    /**
     * Places the features from this data source, filtered by the filter, into the feature
     * collection.
     *
     * @param collection The collection in which to place the features.
     * @param query The query to execute.
     *
     * @throws DataSourceException If error occurs when getting the features.
     */
    public void getFeatures(FeatureCollection collection, Query query) throws DataSourceException {
        LOGGER.finest("Entering getFeatures");

        OracleConnection conn = null;
        int maxFeatures = query.getMaxFeatures();
        Filter filter = query.getFilter();
        ArrayList features = new ArrayList();
        ResultSet result = null;
        Statement statement = null;

        try {
            Filter manualFilter = null;
            Filter supportedFilters = null;

            conn = getConnection();
            statement = conn.createStatement();

            if (filter != null) {
                LOGGER.finer("Unpacking filter");
                unpacker.unPackAND(filter);
                manualFilter = unpacker.getUnSupported();
                supportedFilters = unpacker.getSupported();
            }

            AttributeType[] attrTypes = getAttTypes(query);
            FeatureType localSchema = FeatureTypeFactory.newFeatureType(attrTypes, tableName);

            // we use the max features limit if there are no filters or
            // no filters we have to manually apply to the result.
            boolean useMax = ((filter == Filter.NONE) || (manualFilter == null));
            String sqlQuery = sqlEncoder.makeSelectSQL(attrTypes, supportedFilters, maxFeatures, useMax);
            LOGGER.fine("SQLQuery: " + sqlQuery);
            result = statement.executeQuery(sqlQuery);

            while (result.next()) {
                Feature newFeature = createFeature(conn, result, localSchema);

                if (newFeature == null) {
                    LOGGER.finer("Null feature return, trying the next one.");
                } else if (manualFilter == null) {
                    LOGGER.fine("Adding feature: " + newFeature.getID());
                    features.add(newFeature);
                } else if (manualFilter.contains(newFeature)) {
                    LOGGER.fine("Adding Manually filtered features: "+newFeature.getID());
                    features.add(newFeature);
                }
            }

            collection.addAll(features);
        } catch (SQLException e) {
            String message = "SQL Error when loading features: " + e.toString();

            LOGGER.warning(message);
            throw new DataSourceException(message, e);
        } catch (FactoryConfigurationError e) {
            String message = "Error instantiating feature factory: " + e.toString();

            LOGGER.warning(message);
            throw new DataSourceException(message, e);
        } catch (SchemaException e) {
            String message = "Error creating schema: " + e.toString();

            LOGGER.warning(message);
            throw new DataSourceException(message, e);
        } catch (SQLEncoderException e) {
            String message = "Error creating sql statement: " + e.toString();

            LOGGER.warning(message);
            throw new DataSourceException(message, e);
        } finally {
            close(result);
            close(statement);
            close(conn);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param conn
     */
    private void close(OracleConnection conn) {
        try {
            conn.close();
        } catch (SQLException e) {
            LOGGER.warning("Error closing connection: " + e);
        }
    }

    /**
     * Gets the FID column name.  Determines the FID column name which is either the primary key or
     * the default - DEFAULT_FID_COLUMN.
     *
     * @param conn The Oracle database connection.
     * @param tableName The table name to get the fid for.
     *
     * @return The fid column name.
     */
    private String getFidColumn(OracleConnection conn, String tableName) {
        String pkString = DEFAULT_FID_COLUMN;
        ResultSet rs = null;

        try {
            DatabaseMetaData dbMetadata = conn.getMetaData();

            rs = dbMetadata.getPrimaryKeys(null, null, tableName);

            if (rs.next()) {
                // @task REVIST: Need to work out what to do when there is more than 1 PK
                pkString = rs.getString(NAME_COLUMN);
            }
        } catch (SQLException e) {
            LOGGER.warning("Could not find the primary key - using the default");
        } finally {
            close(rs);
        }

        LOGGER.finest("FID=" + pkString);

        return pkString;
    }

    /**
     * Constructs the AttributeType for a geometry column.
     *
     * @param conn The database connection
     * @param tableName The table name.
     * @param columnName The geometry column name.
     *
     * @return The AttributeType for the geometry column.
     *
     * @throws DataSourceException
     */
    private AttributeType getGeometryAttribute(OracleConnection conn, String tableName, String columnName)
        throws DataSourceException {
        AttributeType attributeType = null;
        ResultSet rs = null;
        Statement statement = null;
        try {
            StringBuffer queryBuffer = new StringBuffer("SELECT ");

            queryBuffer.append(columnName);
            queryBuffer.append(" FROM ");
            queryBuffer.append(tableName);
            queryBuffer.append(" WHERE ROWNUM = 1");

            String query = queryBuffer.toString();

            LOGGER.finer("Checking geometry using: " + query);
            statement = conn.createStatement();
            rs = statement.executeQuery(query);

            if (rs.next()) {
                try {
                    Object geomObject = rs.getObject(columnName);
                    AdapterSDO adaptersdo = getSDOAdapter(conn, columnName);
                    adapterJTS = getJTSAdapter(conn, columnName);
                    oracle.sdoapi.geom.Geometry sdoGeom = adaptersdo.importGeometry(geomObject);
                    Geometry geometry = (Geometry) adapterJTS.exportGeometry(Geometry.class, sdoGeom);                    
                    Class geomClass = JTSUtilities.findBestGeometryClass(JTSUtilities.findBestGeometryType(geometry));                    
                    attributeType = AttributeTypeFactory.newAttributeType(columnName, geomClass);
                } catch (Exception e) {
                    String message = "Could not import geometry";

                    LOGGER.log(Level.SEVERE, message, e);
                    throw new DataSourceException(message, e);
                }
            } else {
                throw new DataSourceException("Could not get any features to determine geometry type");
            }
        } catch (SQLException e) {
            String message = "Database error when getting geometry attribute";

            LOGGER.log(Level.SEVERE, message, e);
            throw new DataSourceException(message, e);
        } finally {
            close(rs);
            close(statement);
        }

        return attributeType;
    }

    /**
     * Gets the schema of the features in the DataSource
     *
     * @return The schema of the Datasource.
     *
     * @see org.geotools.data.DataSource#getSchema()
     */
    public FeatureType getSchema() {
        return schema;
    }

    private String getTypeName() {
        String typeName = "";

        if (oraSchemaName != null) {
            typeName += (oraSchemaName + ".");
        }

        typeName += tableName;

        return typeName;
    }

    private AdapterSDO getSDOAdapter(OracleConnection conn, String geometryColumnName) throws DataSourceException {
        try {
            GeometryMetaData metaData = OraSpatialManager.getGeometryMetaData(conn, tableName, geometryColumnName);
            
            if (metaData == null) {
                throw new DataSourceException("No geometry metadata found for " + tableName + "," 
                            + geometryColumnName + ". Every Geometry must have an entry in" +
                                    "the USER__SDO_GEOM_METADATA table.");
            }
            
            SRManager srManager = OraSpatialManager.getSpatialReferenceManager(conn);
            if (srid == -1) {
                srid = metaData.getSpatialReferenceID();
            }
            SpatialReference sr = srManager.retrieve(srid);
            GeometryFactory gFact = OraSpatialManager.getGeometryFactory(sr);
            return new AdapterSDO(gFact, conn);
        } catch (SRException e) {
            String message = e.getMessage();
            LOGGER.warning(message);
            throw new DataSourceException(message, e);
        } catch (SQLException e) {
            String message = e.getMessage();
            LOGGER.warning(message);
            throw new DataSourceException(message, e);
        }
    }

    private AdapterJTS getJTSAdapter(OracleConnection conn, String geometryColumnName) 
            throws DataSourceException {
        try {
            GeometryMetaData metaData = OraSpatialManager.getGeometryMetaData(conn, tableName, geometryColumnName);
            SRManager srManager = OraSpatialManager.getSpatialReferenceManager(conn);
            if (srid == -1) {
                srid = metaData.getSpatialReferenceID();
            }
            SpatialReference sr = srManager.retrieve(srid);
            GeometryFactory gFact = OraSpatialManager.getGeometryFactory(sr);
            return new AdapterJTS(gFact);
        } catch (SRException e) {
            String message = e.getMessage();
            LOGGER.warning(message);
            throw new DataSourceException(message, e);
        }
    }

    //J-
    /**
     * Constructs the schema of this feature type.
     *
     * @return The schema of the features in the database table.
     *
     * @throws DataSourceException
     */
    private FeatureType makeSchema(OracleConnection conn) throws DataSourceException {
        FeatureType featureType = null;
        ResultSet tableInfo = null;
        try {
            DatabaseMetaData dbMetaData = conn.getMetaData();
            tableInfo = dbMetaData.getColumns(null, oraSchemaName, tableName, "%");
            List attributeTypes = new ArrayList();
            while (tableInfo.next()) {
                LOGGER.fine(
                    "Column Name = "
                        + tableInfo.getObject(NAME_COLUMN)
                        + "; Column Type = "
                        + tableInfo.getObject(TYPE_STRING_COLUMN));
                String attributeName = tableInfo.getString(NAME_COLUMN);
                if (fidColumn.equals(attributeName)) {
                    continue;
                } else if ("SDO_GEOMETRY".equals(tableInfo.getString(TYPE_STRING_COLUMN))) {
                    AttributeType geomType = getGeometryAttribute(conn, tableName, attributeName);
                    attributeTypes.add(geomType);
                } else {
                    Class type = (Class) TYPE_MAPPINGS.get(tableInfo.getString(TYPE_STRING_COLUMN));
                    if (type != null) {
                        AttributeType attributeType = AttributeTypeFactory.newAttributeType(attributeName, type);
                        attributeTypes.add(attributeType);
                    } else {
                        LOGGER.fine(
                            "Ignoring an SQL type that is not known: " + tableInfo.getString(TYPE_STRING_COLUMN));
                    }
                }
            }
            AttributeType[] types = (AttributeType[]) attributeTypes.toArray(new AttributeType[0]);
            featureType = FeatureTypeFactory.newFeatureType(types, tableName);
        } catch (SQLException e) {
            String error = "Database error in schema construction: " + e;
            LOGGER.warning(error);
            throw new DataSourceException(error, e);
        } catch (SchemaException e) {
            String error = "Error in schema construction: " + e;
            LOGGER.warning(error);
            throw new DataSourceException(error, e);
        } finally {
            close(tableInfo);
        }
        return featureType;
    }
    //J+

    /**
     * Modifies the passed attribute types with the passed objects in all features that correspond
     * to the passed OGS filter.
     * 
     * <p>
     * If an error occurs in this operation, the state of the data source will be rolled back to
     * the previous state.
     * </p>
     * 
     * <p>
     * The types array and the values array have a 1-1 relationship, meaning that the ith value in
     * values is used to set the ith attribute in types.
     * </p>
     *
     * @param types The Attributes of the Feature to modify.
     * @param values The values to set the attributes in types to.
     * @param filter The filter to determine which features need to be modified.
     *
     * @throws DataSourceException If an error occured when modifying the features. If this is
     *         thrown then a rollback occurs.
     *
     * @see org.geotools.data.DataSource#modifyFeatures(org.geotools.feature.AttributeType[],
     *      java.lang.Object[], org.geotools.filter.Filter)
     */
    public void modifyFeatures(AttributeType[] types, Object[] values, Filter filter) throws DataSourceException {
        boolean previousAutoCommit = getAutoCommit();

        setAutoCommit(false);

        boolean fail = false;
        OracleConnection conn = null;
        PreparedStatement pStatement = null;
        Statement statement = null;

        // Just some constraint checking first
        if (types.length != values.length) {
            throw new DataSourceException("The number of AttributeTypes must match the number of values");
        }

        // Should probably check that the attributes are valid in the schema
        // Unpack on OR conditions so we can treat them separately.
        unpacker.unPackOR(filter);

        Filter supported = unpacker.getSupported();
        Filter unsupported = unpacker.getUnSupported();

        try {
            conn = getTransactionConnection();

            // update all the features that are found in the supported filter
            if (supported != null) {
                String update = sqlEncoder.makeModifyTemplate(types, supported);

                //String update = sqlTemplate + encoder.encode(supported);
                LOGGER.finer("Update STMT: " + update);
                pStatement = conn.prepareStatement(update);

                for (int i = 0; i < values.length; i++) {
                    if (types[i].isGeometry()) {
                        oracle.sdoapi.geom.Geometry geometry = adapterJTS.importGeometry(values[i]);
                        AdapterSDO adaptersdo = getSDOAdapter(conn, types[i].getName());
                        pStatement.setObject(i + 1, adaptersdo.exportGeometry(STRUCT.class, geometry));
                    } else {
                        pStatement.setObject(i + 1, values[i]);
                    }
                }

                pStatement.executeUpdate();
            }

            // now do the ones that are in the unsupported filters.
            if (unsupported != null) {
                String sqlTemplate = sqlEncoder.makeModifyTemplate(types);

                // we need to retreive these manually
                FeatureCollection features = getFeatures(unsupported);

                /* Then update them manually, using their fid
                 * as the where clause.
                 */
                if (features.size() > 0) {
                    int i = 0;
                    StringBuffer updateBuffer = new StringBuffer(sqlTemplate + " WHERE ");

                    // Make the WHERE clause
                    for (Iterator iter = features.iterator(); iter.hasNext(); i++) {
                        Feature feature = (Feature) iter.next();

                        updateBuffer.append(fidColumn);
                        updateBuffer.append(" = ");
                        updateBuffer.append(formatFid(feature));
                        if (i < (features.size() - 1)) {
                            updateBuffer.append(" OR ");
                        } else {
                            updateBuffer.append(" ");
                        }
                    }

                    String update = updateBuffer.toString();

                    LOGGER.fine("Manual Update STMT: " + update);
                    pStatement = conn.prepareStatement(update);
                    fillModifyValues(types, values, pStatement, conn);
                    pStatement.executeUpdate();
                }
            }
            // J-    
        } catch (SQLEncoderException e) {
            fail = true;
            String message = "Failed to encode filter: " + e;
            LOGGER.warning(message);
            throw new DataSourceException(message, e);
        } catch (SQLException e) {
            fail = true;
            String message = "An SQL Error occured: " + e;
            LOGGER.warning(message);
            throw new DataSourceException(message, e);
        } catch (GeometryInputTypeNotSupportedException e) {
            fail = true;
            String message = "Geometry input type error when updating features: " + e;
            LOGGER.warning(message);
            throw new DataSourceException(message, e);
        } catch (GeometryOutputTypeNotSupportedException e) {
            fail = true;
            String message = "Geometry output type error when updating features: " + e;
            LOGGER.warning(message);
            throw new DataSourceException(message, e);
        } catch (InvalidGeometryException e) {
            fail = true;
            String message = "Geometry Conversion error when updating features: " + e;
            LOGGER.warning(message);
        } finally {
            close(pStatement);
            close(statement);
            finalizeTransactionMethod(previousAutoCommit, fail);
        }
        // J+
    }

    /**
     * Fills a prepared statement with the new values for each attribute.
     *
     * @param types The Attribute types contained in the prepared statement
     * @param values The new values.
     * @param pStatement The statement to fill.
     *
     * @throws InvalidGeometryException If an error occurs when coverting a geometry.
     * @throws GeometryInputTypeNotSupportedException This should not happen.
     * @throws SQLException If an error occurs setting the objects in the statement.
     * @throws GeometryOutputTypeNotSupportedException This shoudl not happen.
     */
    private void fillModifyValues(
        AttributeType[] types,
        Object[] values,
        PreparedStatement pStatement,
        OracleConnection conn)
        throws
            InvalidGeometryException,
            GeometryInputTypeNotSupportedException,
            SQLException,
            GeometryOutputTypeNotSupportedException,
            DataSourceException {
        // Fill in the prepared statement
        for (int i = 0; i < values.length; i++) {
            if (types[i].isGeometry()) {
                oracle.sdoapi.geom.Geometry geometry = adapterJTS.importGeometry(values[i]);
                AdapterSDO adaptersdo = getSDOAdapter(conn, types[i].getName());
                Object geomStruct = adaptersdo.exportGeometry(STRUCT.class, geometry);

                pStatement.setObject(i + 1, geomStruct);
            } else {
                pStatement.setObject(i + 1, values[i]);
            }
        }
    }

    /**
     * Removes all of the features specificed by the passed filter from the data source.
     * 
     * <p>
     * If an error occurs in the execution of this method, the state of the data source is restored
     * to the previous state.
     * </p>
     *
     * @param filter The filter of features to remove.
     *
     * @throws DataSourceException If an error occurs when removing features.
     *
     * @see org.geotools.data.DataSource#removeFeatures(org.geotools.filter.Filter)
     */
    public void removeFeatures(Filter filter) throws DataSourceException {
        boolean previousAutoCommit = getAutoCommit();

        setAutoCommit(false);

        boolean fail = false;

        unpacker.unPackOR(filter);

        Filter supported = unpacker.getSupported();
        Filter unsupported = unpacker.getUnSupported();
        OracleConnection conn = null;
        Statement statement = null;

        try {
            conn = getTransactionConnection();
            statement = conn.createStatement();

            if (supported != null) {
                String sql = sqlEncoder.makeDeleteSQL(filter);

                LOGGER.finer("delete sql is " + sql);
                statement.executeUpdate(sql);
            }

            // Now see if there is anything that needs to be done manually
            if (unsupported != null) {
                FeatureCollection features = getFeatures(unsupported);

                if (features.size() > 0) {
                    int i = 0;
                    StringBuffer manualDelete = new StringBuffer("DELETE FROM ");
                    manualDelete.append(tableName);
                    manualDelete.append(" WHERE ");

                    for (Iterator iter = features.iterator(); iter.hasNext(); i++) {
                        Feature feature = (Feature) iter.next();

                        manualDelete.append(fidColumn);
                        manualDelete.append(" = ");
                        manualDelete.append(formatFid(feature));

                        if (i < (features.size() - 1)) {
                            manualDelete.append(" OR ");
                        }
                    }

                    String manualDeleteStmt = manualDelete.toString();
                    LOGGER.finer("Manual Delete is: " + manualDeleteStmt);
                    statement.executeUpdate(manualDeleteStmt);
                }
            }
        } catch (SQLException e) {
            fail = true;

            String message = "An SQL Error occured: " + e.getMessage();

            LOGGER.warning(message);
            throw new DataSourceException(message, e);
        } catch (SQLEncoderException e) {
            fail = true;

            String message = "Failed to encode filter: " + e.getMessage();

            LOGGER.warning(message);
            throw new DataSourceException(message, e);
        } finally {
            close(statement);
            finalizeTransactionMethod(previousAutoCommit, fail);
        }
    }

    /**
     * This is called my any transaction method in its finally block. If the transaction failed it
     * is rolled back, if it succeeded and we are previously set to autocommit, it is committed
     * and if  it succeed and we are set to manual commit, no action is taken.
     * 
     * <p>
     * In all cases the autocommit status of the data source is set to previousAutoCommit and the
     * closeTransactionConnection is called.
     * </p>
     *
     * @param previousAutoCommit The status of autoCommit prior to the  beginning of a transaction
     *        method.  This tells us whether we should commit or wiat for the user to perform the
     *        commit.
     * @param fail The fail status of the transaction.  If true, the transaction is rolled back.
     *
     * @throws DataSourceException If errors occur performing any of the actions.
     */
    private void finalizeTransactionMethod(boolean previousAutoCommit, boolean fail) throws DataSourceException {
        if (fail) {
            rollback();
        } else {
            // only commit if this transaction was atomic
            // ie if the user had previously set autoCommit to false
            // we leave commiting up to them.
            if (previousAutoCommit) {
                commit();
            }
        }

        setAutoCommit(previousAutoCommit);
        closeTransactionConnection();
    }

    /**
     * Undoes all transactions made since the last commit or rollback. This method should be used
     * only when auto-commit mode has been disabled. This method should only be implemented if
     * <tt>setAutoCommit(boolean)</tt>  is also implemented.
     *
     * @throws DataSourceException if there are problems with the datasource.
     *
     * @see #setAutoCommit(boolean)
     */
    public void rollback() throws DataSourceException {
        try {
            getTransactionConnection().rollback();
            closeTransactionConnection();
        } catch (SQLException e) {
            String message = "problem with rollbacks";

            LOGGER.warning(message + ": " + e.getMessage());
            throw new DataSourceException(message, e);
        }
    }

    /**
     * Starts a MultiTransaction.
     * 
     * <p>
     * Begins a transaction(add, remove or modify) that does not commit as each modification call
     * is made.  If an error occurs during a transaction after this method has been called then
     * the datasource should rollback: none of the transactions performed after this method was
     * called should go through.
     * </p>
     * 
     * <p>
     * This transaction state will persist until endMultiTransaction or abortMultiTransaction is
     * called.
     * </p>
     *
     * @see org.geotools.data.DataSource#startMultiTransaction()
     */
    public void setAutoCommit(boolean b) throws DataSourceException {
        try {
            OracleConnection conn = getTransactionConnection();

            conn.setAutoCommit(b);
            closeTransactionConnection();
        } catch (SQLException e) {
            String message = "Error beginning Multi Transaction: " + e.getMessage();

            LOGGER.warning(message);
            throw new DataSourceException(message, e);
        }
    }

    /**
     * Sets the feature table of this data source to the features contained in the feature
     * collection.  All features are removed and replaced with the contents of the
     * FeatureCollection.
     *
     * @param features The FeatureCollection to set as the contents of this DataSource.
     *
     * @throws DataSourceException If an error occured when setting the features. If this is thrown
     *         then a rollback occurs.
     *
     * @see org.geotools.data.DataSource#setFeatures(org.geotools.feature.FeatureCollection)
     */
    public void setFeatures(FeatureCollection features) throws DataSourceException {
        boolean originalAutoCommit = getAutoCommit();

        setAutoCommit(false);
        removeFeatures(null);
        addFeatures(features);
        commit();
        setAutoCommit(originalAutoCommit);
    }

    /** Gets a connection.
     * 
     *  <p>The connection returned by this method is suitable for a single
     * use.  Once a method has finish with the connection it should call the connections
     * close method.
     * 
     * <p>Methods wishing to use a connection for transactions or methods who use
     * of the connection involves commits or rollbacks should use getTransactionConnection
     * instead of this method.
     *  
     * @return  A single use connection.
     * @throws DataSourceException If the connection is not an OracleConnection. 
     * @throws SQLException If there is a problem with the connection.
     */
    private OracleConnection getConnection() throws DataSourceException, SQLException {
        Connection conn = connectionPool.getConnection();

        if (conn instanceof OracleConnection) {
            return (OracleConnection) conn;
        } else {
            throw new DataSourceException(
                "Connection Pool did not return a connection to" + "an Oracle Spatial Database.");
        }
    }

    /** This method should be called when a connection is required for transactions.
     *  After completion of the use of the connection the caller should call 
     *  closeTransactionConnection which will either close the conn if we are in auto 
     *  commit, or maintain the connection if we are in manual commit.  Successive calls
     *  to this method after setting autoCommit to false will return the same connection
     *  object.
     *  
     * @return A connection object suitable for multiple transactional calls.
     * @throws DataSourceException IF an error occurs getting the connection.
     * @throws SQLException If there is something wrong with the connection.
     */
    private OracleConnection getTransactionConnection() throws DataSourceException, SQLException {
        if (transactionConnection == null) {
            transactionConnection = getConnection();
        }

        return transactionConnection;
    }

    /** This method should be called when a connection retrieved using
     *  getTransactionConnection in to be closed. 
     * 
     *  <p>This method only closes the connection if it is set to
     *  auto commit.  Otherwise the connection is kept open and held in the
     *  transactionConnection instance variable.
     */
    private void closeTransactionConnection() {
        try {
            // we only close if the transaction is set to auto commit
            // otherwise we wait until auto commit is turned off before closing.
            if ((transactionConnection != null) && transactionConnection.getAutoCommit()) {
                LOGGER.fine("Closing Transaction Connection");
                transactionConnection.close();
                transactionConnection = null;
            } else {
                LOGGER.fine("Transaction connection not open or set to manual commit");
            }
        } catch (SQLException e) {
            LOGGER.warning("Error closing transaction connection: " + e);
        }
    }
}
