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
package org.geotools.data.postgis;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultQuery;
import org.geotools.data.EmptyFeatureReader;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.data.InProcessLockingManager;
import org.geotools.data.LockingManager;
import org.geotools.data.Query;
import org.geotools.data.ReTypeFeatureReader;
import org.geotools.data.Transaction;
import org.geotools.data.jdbc.ConnectionPool;
import org.geotools.data.jdbc.FeatureTypeInfo;
import org.geotools.data.jdbc.JDBCDataStore;
import org.geotools.data.jdbc.JDBCDataStoreConfig;
import org.geotools.data.jdbc.JDBCFeatureLocking;
import org.geotools.data.jdbc.JDBCFeatureSource;
import org.geotools.data.jdbc.JDBCFeatureStore;
import org.geotools.data.jdbc.JDBCFeatureWriter;
import org.geotools.data.jdbc.JDBCUtils;
import org.geotools.data.jdbc.QueryData;
import org.geotools.data.jdbc.SQLBuilder;
import org.geotools.data.jdbc.attributeio.AttributeIO;
import org.geotools.data.jdbc.attributeio.WKTAttributeIO;
import org.geotools.data.jdbc.fidmapper.FIDMapper;
import org.geotools.data.jdbc.fidmapper.FIDMapperFactory;
import org.geotools.data.postgis.attributeio.PgWKBAttributeIO;
import org.geotools.data.postgis.fidmapper.PostgisFIDMapperFactory;
import org.geotools.data.postgis.referencing.PostgisAuthorityFactory;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.FeatureType;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.filter.Filter;
import org.geotools.filter.SQLEncoderPostgis;
import org.geotools.filter.SQLEncoderPostgisGeos;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;


/**
 * Postgis DataStore implementation.
 * 
 * <p>
 * This datastore by default will read/write geometries in WKT format.<br>
 * Optionally use of WKB can be turned on, in which case you may want to turn
 * on also the use of the bytea function, that fasten the data trasfer, but
 * that it's available only from version 0.7.2 onwards.
 * </p>
 *
 * @author Chris Holmes, TOPP
 * @author Andrea Aime
 * @author Paolo Rizzi
 * @version $Id: PostgisDataStore.java,v 1.18.2.3 2004/05/02 15:31:43 aaime Exp $
 * @task REVISIT: So Paolo Rizzi has a number of improvements in 
 * http://jira.codehuas.org/browse/GEOT-379  I rolled in a few of them, but 
 * some beg more fundamental questions - like the use of primary keys - in the
 * geotools model.  See the issue for a bit more discussion, and I will attempt
 * to write my thoughts up on wiki soon.  -ch
 */
public class PostgisDataStore extends JDBCDataStore implements DataStore {
    /** The logger for the postgis module. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.data.postgis");

    /** Map of postgis geometries to jts geometries */
    private static Map GEOM_TYPE_MAP = new HashMap();

    static {
        GEOM_TYPE_MAP.put("GEOMETRY", Geometry.class);
        GEOM_TYPE_MAP.put("POINT", Point.class);
        GEOM_TYPE_MAP.put("LINESTRING", LineString.class);
        GEOM_TYPE_MAP.put("POLYGON", Polygon.class);
        GEOM_TYPE_MAP.put("MULTIPOINT", MultiPoint.class);
        GEOM_TYPE_MAP.put("MULTILINESTRING", MultiLineString.class);
        GEOM_TYPE_MAP.put("MULTIPOLYGON", MultiPolygon.class);
        GEOM_TYPE_MAP.put("GEOMETRYCOLLECTION", GeometryCollection.class);
    }

    private static Map CLASS_MAPPINGS = new HashMap();

    static {
        CLASS_MAPPINGS.put(String.class, "VARCHAR");

        CLASS_MAPPINGS.put(Boolean.class, "BOOLEAN");

        CLASS_MAPPINGS.put(Integer.class, "INTEGER");

        CLASS_MAPPINGS.put(Float.class, "REAL");
        CLASS_MAPPINGS.put(Double.class, "DOUBLE PRECISION");

        CLASS_MAPPINGS.put(BigDecimal.class, "DECIMAL");

        CLASS_MAPPINGS.put(java.sql.Date.class, "DATE");
        CLASS_MAPPINGS.put(java.util.Date.class, "DATE");
        CLASS_MAPPINGS.put(java.sql.Time.class, "TIME");
        CLASS_MAPPINGS.put(java.sql.Timestamp.class, "TIMESTAMP");
    }

    private static Map GEOM_CLASS_MAPPINGS = new HashMap();
    //why don't we just stick this in with the non-geom class mappings?
    static {
        // init the inverse map
        Set keys = GEOM_TYPE_MAP.keySet();

        for (Iterator it = keys.iterator(); it.hasNext();) {
            String name = (String) it.next();
            Class geomClass = (Class) GEOM_TYPE_MAP.get(name);
            GEOM_CLASS_MAPPINGS.put(geomClass, name);
        }
    }

    /** OPTIMIZE constants */
    public static final int OPTIMIZE_SAFE = 0;
    public static final int OPTIMIZE_SQL = 1;

    /** The lock manager */
    private LockingManager lockingManager = createLockingManager();

    /** Enables the use of geos operators */
    protected boolean useGeos;

    /** Current optimize mode */
    public final int OPTIMIZE_MODE;

    /**
     * If true, WKB format is used instead of WKT
     * 
     * @uml.property name="WKBEnabled"
     */
    protected boolean WKBEnabled = false;

    /**
     * If true, the bytea function will be used to optimize even further data
     * loading when using WKB format
     * 
     * @uml.property name="byteaEnabled"
     */
    protected boolean byteaEnabled = false;

    /** 
     * If true then the bounding box filters will use the && postgis operator,
     * which uses the spatial index and performs against the envelope of the
     * geom, leading to greater speed and slightly less accuracy.
     */ 
    protected boolean looseBbox;

    protected PostgisDataStore(ConnectionPool connPool)
        throws IOException {
        this(connPool, (String) null);
    }

    protected PostgisDataStore(ConnectionPool connPool, String namespace)
        throws IOException {
        this(connPool, null, namespace);
    }

    protected PostgisDataStore(ConnectionPool connPool, String schema,
        String namespace) throws IOException {
        this(connPool,
            new JDBCDataStoreConfig(namespace, schema, new HashMap(),
                new HashMap()), OPTIMIZE_SQL);
    }

    protected PostgisDataStore(ConnectionPool connPool, String schema,
        String namespace, int optimizeMode) throws IOException {
        this(connPool,
            new JDBCDataStoreConfig(namespace, schema, new HashMap(),
                new HashMap()), OPTIMIZE_SQL);
    }

    public PostgisDataStore(ConnectionPool connectionPool,
        JDBCDataStoreConfig config, int optimizeMode) throws IOException {
        super(connectionPool, config);

        guessDataStoreOptions();
        OPTIMIZE_MODE = optimizeMode;

        // use the specific postgis fid mapper factory
        setFIDMapperFactory(new PostgisFIDMapperFactory());
    }

    /**
     * Allows subclass to create LockingManager to support their needs.
     *
     * @return
     */
    protected LockingManager createLockingManager() {
        return new InProcessLockingManager();
    }


    /**
     * Attempts to figure out some optimization options, based on some
     * postgis metadata.  If the version is later than 0.7.2 then bytea
     * will be used to read geometries if WKB is enabled.  And it will
     * read if GEOS is enabled from the version string as well.
     */
    protected void guessDataStoreOptions() throws IOException {
        Connection dbConnection = null;

        try {
            String sqlStatement = "SELECT  postgis_version();";
            dbConnection = getConnection(Transaction.AUTO_COMMIT);

            Statement statement = dbConnection.createStatement();
            ResultSet result = statement.executeQuery(sqlStatement);
            boolean retValue = false;

            if (result.next()) {
                String version = result.getString(1);
                LOGGER.fine("version is " + version);

                int[] versionNumbers;

                try {
                    String[] values = version.trim().split(" ");
                    String[] versionNumbersStr = values[0].trim().split("\\.");
                    versionNumbers = new int[versionNumbersStr.length];

                    for (int i = 0; i < versionNumbers.length; i++) {
                        versionNumbers[i] = Integer.parseInt(versionNumbersStr[i]);
                    }

                    // bytea function has been introduced in 0.7.2
                    if ((versionNumbers[0] > 0) || (versionNumbers[1] > 7)
                            || ((versionNumbers.length > 2)
                            && (versionNumbers[2] >= 2))) {
                         byteaEnabled = true;
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING,
                        "Exception occurred while parsing the version number.",
                        e);
                }

                if (version.indexOf("USE_GEOS=1") != -1) {
                    this.useGeos = true;
                }
            }
        } catch (SQLException sqle) {
            String message = sqle.getMessage();
            LOGGER.warning(message);
            throw new DataSourceException(message, sqle);
        } finally {
            JDBCUtils.close(dbConnection, Transaction.AUTO_COMMIT, null);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.geotools.data.DataStore#getTypeNames()
     */
    public String[] getTypeNames() throws IOException {
        final int TABLE_NAME_COL = 3;
        Connection conn = null;
        List list = new ArrayList();

        try {
            conn = getConnection(Transaction.AUTO_COMMIT);

            DatabaseMetaData meta = conn.getMetaData();
            String[] tableType = { "TABLE" };
            ResultSet tables = meta.getTables(null,
                    config.getDatabaseSchemaName(), "%", tableType);

            while (tables.next()) {
                String tableName = tables.getString(TABLE_NAME_COL);

                if (allowTable(tableName)) {
                    list.add(tableName);
                }
            }

            return (String[]) list.toArray(new String[list.size()]);
        } catch (SQLException sqlException) {
            JDBCUtils.close(conn, Transaction.AUTO_COMMIT, sqlException);
            conn = null;

            String message = "Error querying database for list of tables:"
                + sqlException.getMessage();
            throw new DataSourceException(message, sqlException);
        } finally {
            JDBCUtils.close(conn, Transaction.AUTO_COMMIT, null);
        }
    }

    protected boolean allowTable(String tablename) {
        if (tablename.equals("geometry_columns")) {
            return false;
        } else if (tablename.startsWith("spatial_ref_sys")) {
            return false;
        }

        //others?
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.geotools.data.DataStore#getFeatureReader(org.geotools.data.Query,
     *      org.geotools.data.Transaction)
     */

    /**
     * This is a public entry point to the DataStore.
     * 
     * <p>
     * We have given some though to changing this api to be based on query.
     * </p>
     * 
     * <p>
     * Currently the is is the only way to retype your features to different
     * name spaces.
     * </p>
     * (non-Javadoc)
     *
     * @see org.geotools.data.DataStore#getFeatureReader(org.geotools.feature.FeatureType,
     *      org.geotools.filter.Filter, org.geotools.data.Transaction)
     */
    public FeatureReader getFeatureReader(final FeatureType requestType,
        final Filter filter, final Transaction transaction)
        throws IOException {
        String typeName = requestType.getTypeName();
        FeatureType schemaType = getSchema(typeName);

        int compare = DataUtilities.compare(requestType, schemaType);

        Query query;

        if (compare == 0) {
            // they are the same type
            //
            query = new DefaultQuery(typeName, filter);
        } else if (compare == 1) {
            // featureType is a proper subset and will require reTyping
            //
            String[] names = attributeNames(requestType, filter);
            query = new DefaultQuery(typeName, filter, Query.DEFAULT_MAX,
                    names, "getFeatureReader");
        } else {
            // featureType is not compatiable
            //
            throw new IOException("Type " + typeName + " does match request");
        }

        if ((filter == Filter.ALL) || filter.equals(Filter.ALL)) {
            return new EmptyFeatureReader(requestType);
        }

        FeatureReader reader = getFeatureReader(query, transaction);

        if (compare == 1) {
            reader = new ReTypeFeatureReader(reader, requestType);
        }

        return reader;
    }

    /**
     * DOCUMENT ME!
     *
     * @param featureType
     * @param filter
     *
     * @return
     *
     * @throws IOException DOCUMENT ME!
     */
    /**
     * Gets the list of attribute names required for both featureType and
     * filter
     *
     * @param featureType The FeatureType to get attribute names for.
     * @param filter The filter which needs attributes to filter.
     *
     * @return The list of attribute names required by a filter.
     *
     * @throws IOException If we can't get the schema.
     */
    protected String[] attributeNames(FeatureType featureType, Filter filter)
        throws IOException {
        String typeName = featureType.getTypeName();
        FeatureType origional = getSchema(typeName);
        SQLBuilder sqlBuilder = getSqlBuilder(typeName);

        if (featureType.getAttributeCount() == origional.getAttributeCount()) {
            // featureType is complete (so filter must require subset
            return DataUtilities.attributeNames(featureType);
        }

        String[] typeAttributes = DataUtilities.attributeNames(featureType);
        String[] filterAttributes = DataUtilities.attributeNames(sqlBuilder
                .getPostQueryFilter(filter));

        if ((filterAttributes == null) || (filterAttributes.length == 0)) {
            // no filter attributes required
            return typeAttributes;
        }

        Set set = new HashSet();
        set.addAll(Arrays.asList(typeAttributes));
        set.addAll(Arrays.asList(filterAttributes));

        if (set.size() == typeAttributes.length) {
            // filter required a subset of featureType attributes
            return typeAttributes;
        } else {
            return (String[]) set.toArray(new String[set.size()]);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param typeName
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public SQLBuilder getSqlBuilder(String typeName) throws IOException {
        FeatureTypeInfo info = typeHandler.getFeatureTypeInfo(typeName);
        int srid = -1;

        // HACK: geos should be integrated with the sql encoder, not a
        // seperate class.
        SQLEncoderPostgis encoder = useGeos ? new SQLEncoderPostgisGeos()
                                            : new SQLEncoderPostgis();
        encoder.setFIDMapper(typeHandler.getFIDMapper(typeName));
        if (info.getSchema().getDefaultGeometry() != null) {
            String geom = info.getSchema().getDefaultGeometry().getName();
            srid = info.getSRID(geom);
            encoder.setDefaultGeometry(geom);
        }

        encoder.setSRID(srid);
        encoder.setLooseBbox(looseBbox);
        PostgisSQLBuilder builder = new PostgisSQLBuilder(encoder);
        builder.setWKBEnabled(WKBEnabled);
        builder.setByteaEnabled(byteaEnabled);

        return builder;
    }

    /**
     * DOCUMENT ME!
     *
     * @param tableName
     * @param geometryColumnName
     *
     * @return
     *
     * @throws IOException DOCUMENT ME!
     * @throws DataSourceException DOCUMENT ME!
     */
    protected int determineSRID(String tableName, String geometryColumnName)
        throws IOException {
        Connection dbConnection = null;

        try {
            String sqlStatement = "SELECT * FROM GEOMETRY_COLUMNS WHERE "
                + "f_table_name='" + tableName + "' AND f_geometry_column='"
                + geometryColumnName + "';";
            dbConnection = getConnection(Transaction.AUTO_COMMIT);

            Statement statement = dbConnection.createStatement();
            ResultSet result = statement.executeQuery(sqlStatement);

            if (result.next()) {
                int retSrid = result.getInt("srid");
                JDBCUtils.close(statement);

                return retSrid;
            } else {
                String mesg = "No geometry column row for srid in table: "
                    + tableName + ", geometry column " + geometryColumnName;
                throw new DataSourceException(mesg);
            }
        } catch (SQLException sqle) {
            String message = sqle.getMessage();

            throw new DataSourceException(message, sqle);
        } finally {
            JDBCUtils.close(dbConnection, Transaction.AUTO_COMMIT, null);
        }
    }

    /**
     * Provides the default implementation of determining the FID column.
     * 
     * <p>
     * The default implementation of determining the FID column name is to use
     * the primary key as the FID column. If no primary key is present, null
     * will be returned. Sub classes can override this behaviour to define
     * primary keys for vendor specific cases.
     * </p>
     * 
     * <p>
     * There is an unresolved issue as to what to do when there are multiple
     * primary keys. Maybe a restriction that table much have a single column
     * primary key is appropriate.
     * </p>
     * 
     * <p>
     * This should not be called by subclasses to retreive the FID column name.
     * Instead, subclasses should call getFeatureTypeInfo(String) to get the
     * FeatureTypeInfo for a feature type and get the fidColumn name from the
     * fidColumn name memeber.
     * </p>
     *
     * @return The name of the primay key column or null if one does not exist.
     */

    //    protected String determineFidColumnName(String typeName)
    //        throws IOException {
    //        String fidColumn = super.determineFidColumnName(typeName);
    //        
    //        if(fidColumn == null)
    //        	fidColumn = DEFAULT_FID_COLUMN;
    //        	
    //        return fidColumn;
    //    }

    private static boolean isPresent(String[] array, String value) {
        if (array != null) {
            for (int i = 0; i < array.length; i++) {
                if ((array[i] != null) && (array[i].equals(value))) {
                    return (true);
                }
            }
        }

        return (false);
    }

    /**
     * Constructs an AttributeType from a row in a ResultSet. The ResultSet
     * contains the information retrieved by a call to getColumns() on the
     * DatabaseMetaData object. This information can be used to construct an
     * Attribute Type.
     * 
     * <p>
     * This implementation construct an AttributeType using the default JDBC
     * type mappings defined in JDBCDataStore. These type mappings only handle
     * native Java classes and SQL standard column types. If a geometry type
     * is found then getGeometryAttribute is called.
     * </p>
     * 
     * <p>
     * Note: Overriding methods must never move the current row pointer in the
     * result set.
     * </p>
     *
     * @param metadataRs The ResultSet containing the result of a
     *        DatabaseMetaData.getColumns call.
     *
     * @return The AttributeType built from the ResultSet.
     *
     * @throws IOException If an error occurs processing the ResultSet.
     */
    protected AttributeType buildAttributeType(ResultSet metadataRs)
        throws IOException {
        try {
            final int TABLE_NAME = 3;
            final int COLUMN_NAME = 4;
            final int TYPE_NAME = 6;
            String typeName = metadataRs.getString(TYPE_NAME);

            if (typeName.equals("geometry")) {
                String tableName = metadataRs.getString(TABLE_NAME);
                String columnName = metadataRs.getString(COLUMN_NAME);

                return getGeometryAttribute(tableName, columnName);
            } else {
                return super.buildAttributeType(metadataRs);
            }
        } catch (SQLException e) {
            throw new IOException("Sql error occurred: " + e.getMessage());
        }
    }

    /**
     * @see org.geotools.data.jdbc.JDBCDataStore#buildFIDMapperFactory(org.geotools.data.jdbc.JDBCDataStoreConfig)
     */
    protected FIDMapperFactory buildFIDMapperFactory( JDBCDataStoreConfig config ) {
        return new PostgisFIDMapperFactory();
    }
    
    /**
     * Returns an attribute type for a geometry column in a feature table.
     *
     * @param tableName The feature table name.
     * @param columnName The geometry column name.
     *
     * @return Geometric attribute.
     *
     * @throws IOException DOCUMENT ME!
     *
     * @task REVISIT: combine with querySRID, as they use the same select
     *       statement.
     * @task This should probably take a Transaction, so if things mess up then
     *       we can rollback.
     */
    AttributeType getGeometryAttribute(String tableName, String columnName)
        throws IOException {
        Connection dbConnection = null;

        try {
            dbConnection = getConnection(Transaction.AUTO_COMMIT);

            String sqlStatement = "SELECT type FROM GEOMETRY_COLUMNS WHERE "
                + "f_table_name='" + tableName + "' AND f_geometry_column='"
                + columnName + "';";
            LOGGER.fine("geometry sql statement is " + sqlStatement);

            String geometryType = null;
            
            // retrieve the result set from the JDBC driver
            Statement statement = dbConnection.createStatement();
            ResultSet result = statement.executeQuery(sqlStatement);

            if (result.next()) {
                geometryType = result.getString("type");
                LOGGER.fine("geometry type is: " + geometryType);
            }
            
            

            if (geometryType == null) {
                String msg = " no geometry found in the GEOMETRY_COLUMNS table "
                    + " for " + tableName + " of the postgis install.  A row "
                    + "for " + columnName + " is required  "
                    + " for geotools to work correctly";
                throw new DataSourceException(msg);
            }

            statement.close();

            Class type = (Class) GEOM_TYPE_MAP.get(geometryType);

            CoordinateReferenceSystem crs = null;
            try{
                crs = getPostgisAuthorityFactory().createCRS(determineSRID(tableName,columnName));
            }catch(FactoryException e){
                crs = null;
            }
            return AttributeTypeFactory.newAttributeType(columnName, type,true,0,null,crs);
        } catch (SQLException sqe) {
            throw new IOException("An SQL exception occurred: "
                + sqe.getMessage());
        } finally {
            JDBCUtils.close(dbConnection, Transaction.AUTO_COMMIT, null);
        }
    }
    
    private static PostgisAuthorityFactory paf = null;
    private PostgisAuthorityFactory getPostgisAuthorityFactory(){
        if(paf == null){
            paf = new PostgisAuthorityFactory(connectionPool);
        }
        return paf;
    }

    /**
     * Gets the sql geometry column name for this type.
     * @task TODO: test this, I can just make sure it compiles.
     */
    private String getGeometrySQLTypeName(Class type) {
        String res = (String) GEOM_CLASS_MAPPINGS.get(type);

        if (res == null) {
            throw new RuntimeException("Uknown type name for class "
                + type + " please update GEOMETRY_MAPPINGS");
        }

        return res;
    }


    /**
     *
     * @see org.geotools.data.DataStore#createSchema(org.geotools.feature.FeatureType)
     */
    public void createSchema(FeatureType featureType) throws IOException {
        String tableName = featureType.getTypeName();
        AttributeType[] attributeType = featureType.getAttributeTypes();

        FIDMapper fidMapper = typeHandler.getFIDMapper(tableName);

        Connection con = this.getConnection(Transaction.AUTO_COMMIT);

        Statement st = null;

        //fix from Paolo Rizzi, to print sql used to create table if it was
        //already present.
        boolean shouldExecute = !tablePresent(tableName, con);

            try {
                con.setAutoCommit(false);
                st = con.createStatement();

                StringBuffer statementSQL = new StringBuffer("CREATE TABLE \""
                        + tableName + "\" (");

                if (!fidMapper.returnFIDColumnsAsAttributes()) {
                    for (int i = 0; i < fidMapper.getColumnCount(); i++) {
             
                        //fix from PR, to ignore the auto oid columns
                      if ("oid".equalsIgnoreCase(fidMapper.getColumnName(i))) {
                            continue;
                      }


                        int val = fidMapper.getColumnType(i);
                        String typeName = getSQLTypeName(fidMapper
                                .getColumnType(i));

                        if (typeName.equals("VARCHAR")) {
                            typeName = typeName + "("
                                + fidMapper.getColumnSize(i) + ")";
                        }

                        statementSQL.append("\"" + fidMapper.getColumnName(i) 
                            + "\" " + typeName + ",");
                    }
                }

                statementSQL.append(makeSqlCreate(attributeType));
                //fix from PR, constraint name must be unique, so add tablename
                statementSQL.append(" CONSTRAINT PK_" + tableName + 
                                    " PRIMARY KEY (");

                for (int i = 0; i < fidMapper.getColumnCount(); i++) {
                    statementSQL.append("\"" + fidMapper.getColumnName(i) 
                                        + "\",");
                }

                statementSQL.setCharAt(statementSQL.length() - 1, ')');
                statementSQL.append(")");

                System.out.println(statementSQL.toString());
                
                if (shouldExecute) {  
                    st.execute(statementSQL.toString());
                }

            //fix from pr: it may be that table existed and then was dropped
            //without removing its geometry info from GEOMETRY_COLUMNS.
            //To support this, try to delete before inserting.
            ////Preserving case for table names gives problems, 
            //so convert to lower case
            statementSQL = new StringBuffer(
                    "DELETE FROM GEOMETRY_COLUMNS WHERE f_table_catalog=''"
                    + " AND f_table_schema = 'public'"
                    + 
                ////" AND f_table_name = '" + tableName.toLowerCase() + "'");
                " AND f_table_name = '" + tableName + "';");

            //SISfixed - prints statement for later reuse
            String s = statementSQL.toString();
            System.out.println(s);

            if (shouldExecute) {
                st.execute(s);
            }

	    //Ok, so Paolo Rizzi suggested that we get rid of our hand-adding
            //of geometry column information and use AddGeometryColumn instead
	    //as it is better (this is in GEOT-379, he attached an extended
            //datastore that does postgis fixes).  But I am pretty positive 
            //the reason we are doing things this way is to preserve the order
            //of FeatureTypes.  I know this is fairly silly, from most 
            //information perspectives, but from another perspective it seems
            //to make sense - if you were transfering a featureType from one
            //data store to another then it should have the same order, right?
            //And order is important in WFS.  There are a few caveats though
            //for one I don't even know if things work right.  I imagine the
            //proper constraints that a AddGeometryColumn operation does are 
            //not set in our hand version, for one.  I would feel better about
            //ignoring the order and just doing things as we like if we had 
            //views in place, if users could add the schema, and then be able
            //to get it back in exactly the order they wanted.  So for now 
            //let's leave things as is, and maybe talk about it in an irc. -ch 

                for (int i = 0; i < attributeType.length; i++) {
                    if (!attributeType[i].isGeometry()) {
			continue;
		    }
		    GeometryAttributeType geomAttribute = (GeometryAttributeType) attributeType[i];
                    //This needs to be improved - I believe we now have
                    //code for a PostGIS authority factory, so we need to
                    //look up from the CRS (maybe its WKT?) to the postgis
                    //spatial_ref_sys table, which will tell us the SRID
                    //for it.  I'm not sure about the exact mechanism for
                    //this, as I don't know how the authorities and crs's
                    //work, but it should be possible -ch
                    CoordinateReferenceSystem refSys = geomAttribute
                        .getCoordinateSystem();
                    int SRID = -1;
                     //so for now we just use -1
                     if (refSys != null) {
                         SRID = -1;
                     } else {
                        SRID = -1;
                     }

                     DatabaseMetaData metaData = con.getMetaData();
                     ResultSet rs = metaData.getCatalogs();
                     rs.next();

                     String dbName = rs.getString(1);
                     rs.close();
		     String typeName = null;
		     //this construct seems unneccesary, since we already would
		     //pass over if this wasn't a geometry...
		     Class type = geomAttribute.getType();
		     if (geomAttribute.isGeometry()) {
			 typeName = getGeometrySQLTypeName(type);
		     } else {
                         typeName = (String) CLASS_MAPPINGS.get(type);
		     }
                     if( typeName != null ) {
                         //SISfixed- dbName was used for schema name, force 
                         //it to 'public' Preserving case for table names 
                         //gives problems, so convert to lower case
                            //statementSQL = new StringBuffer(
                            //"INSERT INTO GEOMETRY_COLUMNS VALUES (" + "'',"
                            //+ "'" + dbName + "'," + "'" + tableName + "',"
                            //+ "'" + attributeType[i].getName() + "',"
                            //+ "2," + SRID + "," + "'"
                            //+ typeName + "')");
                            statementSQL = new StringBuffer(
                            "INSERT INTO GEOMETRY_COLUMNS VALUES (" + "'',"
                             ////+ "'" + "public" + "'," + "'" + tableName.toLowerCase() + "',"
                             + "'" + "public" + "'," + "'" + tableName + "',"
                             + "'" + attributeType[i].getName() + "',"
                             + "2," + SRID + "," + "'"
                             + typeName + "')");
                        System.out.println(statementSQL);
                             if (shouldExecute) {
                                 st.execute(statementSQL.toString());
                             }
			} else {
			    System.out.println("Error: " + geomAttribute
					       .getName()+ " unknown type!!!");
			}

 
                //also build a spatial index on each geometry column.
                //TODO review!!! Should this be parameterized???
                //SISfixed - put tablename and fieldname between quotes to preserve case					
                statementSQL.append("\nCREATE INDEX spatial_"
                    + tableName.toLowerCase() + "_"
                    + attributeType[i].getName().toLowerCase() + " ON \""
                    + tableName + "\" USING GIST (\""
                    + attributeType[i].getName() + "\");");

                //SISfixed - prints statement for later reuse
                s = statementSQL.toString();
                System.out.println(s);

                if (shouldExecute) {
                    st.execute(s);
                }
		}   
                con.commit();

		//Paolo Rizzi had a VACUUM ANALYZE here, but I'm not sure that
                //it's needed, since the table is empty.  Waiting for feedback
                //from dblasby -ch

            } catch (SQLException e) {
                try {
                    if (con != null) {
                        con.rollback();
                    }
                } catch (SQLException sqle) {
                    throw new IOException(sqle.getMessage());
                }

                throw new IOException(e.getMessage());
            } finally {
                try {
                    if (st != null) {
                        st.close();
                    }
                } catch (SQLException e) {
                    throw new IOException(e.getMessage());
                } finally {
                    try {
                        if (con != null) {
                            con.setAutoCommit(true);
                            con.close();
                        }
                    } catch (SQLException e) {
                        throw new IOException(e.getMessage());
                    }
                }
            }
	    if (!shouldExecute) {
		throw new IOException("The table " + tableName + " already exist.");
	    }
    }

    /**
     * Returns the sql type name given the SQL type code
     *
     * @param typeCode
     *
     * @return
     *
     * @throws RuntimeException DOCUMENT ME!
     */
    private String getSQLTypeName(int typeCode) {
        Class typeClass = (Class) TYPE_MAPPINGS.get(new Integer(typeCode));

        if (typeClass == null) {
            throw new RuntimeException("Uknown type " + typeCode
                + " please update TYPE_MAPPINGS");
        }

        String typeName = (String) CLASS_MAPPINGS.get(typeClass);

        if (typeName == null) {
            throw new RuntimeException("Uknown type name for class "
                + typeClass.getName() + " please update CLASS_MAPPINGS");
        }

        return typeName;
    }

    private StringBuffer makeSqlCreate(AttributeType[] attributeType)
        throws IOException {
        StringBuffer buf = new StringBuffer("");

        for (int i = 0; i < attributeType.length; i++) {
            String typeName = null;

            if ((typeName = (String) CLASS_MAPPINGS.get(
                            attributeType[i].getType())) != null) {
                if (attributeType[i].isGeometry()) {
                    typeName = "GEOMETRY";
                } else if (typeName.equals("VARCHAR")) {
                    typeName = typeName + "("
                        + attributeType[i].getFieldLength() + ")";
                }

                if (!attributeType[i].isNillable()) {
                    typeName = typeName + " NOT NULL";
                }

                                //SISfixed - added support for default values
                //TODO review!!! Is toString() always OK???
                //TODO review!!! Brute-force quoting should work for numbers also,
                //but not sure!!!
                Object defaultValue = attributeType[i].createDefaultValue();

                if (defaultValue != null) {
                    typeName = typeName + " DEFAULT '"
                        + defaultValue.toString() + "'";
                }

                //SISfixed - put fieldname between quotes to preserve case
                buf.append("\"" + attributeType[i].getName() + "\" " + typeName
                    + ",\n");

                buf.append(attributeType[i].getName() + " " + typeName + ",");
                //System.out.println(buf);
            } else {
                throw (new IOException("Type not supported!"));
            }
        }

        return buf;
    }

    /**
     * DOCUMENT ME!
     *
     * @param table
     * @param con
     *
     * @return
     *
     * @throws IOException DOCUMENT ME!
     * @throws DataSourceException DOCUMENT ME!
     */
    private boolean tablePresent(String table, Connection con)
        throws IOException {
        final int TABLE_NAME_COL = 3;
        Connection conn = null;
        List list = new ArrayList();

        try {
            conn = getConnection(Transaction.AUTO_COMMIT);

            DatabaseMetaData meta = conn.getMetaData();
            String[] tableType = { "TABLE" };
            ResultSet tables = meta.getTables(null,
                    config.getDatabaseSchemaName(), "%", tableType);

            while (tables.next()) {
                String tableName = tables.getString(TABLE_NAME_COL);

                if (allowTable(tableName) && (tableName != null)
                        && (tableName.equalsIgnoreCase(table))) {
                    return (true);
                }
            }

            return false;
        } catch (SQLException sqlException) {
            JDBCUtils.close(conn, Transaction.AUTO_COMMIT, sqlException);
            conn = null;

            String message = "Error querying database for list of tables:"
                + sqlException.getMessage();
            throw new DataSourceException(message, sqlException);
        } finally {
            JDBCUtils.close(conn, Transaction.AUTO_COMMIT, null);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param query
     *
     * @return
     *
     * @throws IOException DOCUMENT ME!
     */
    /**
     * Get propertyNames in a safe manner.
     * 
     * <p>
     * Method wil figure out names from the schema for query.getTypeName(), if
     * query getPropertyNames() is <code>null</code>, or
     * query.retrieveAllProperties is <code>true</code>.
     * </p>
     *
     * @param query
     *
     * @return
     *
     * @throws IOException
     */
    private String[] propertyNames(Query query) throws IOException {
        String[] names = query.getPropertyNames();

        if ((names == null) || query.retrieveAllProperties()) {
            String typeName = query.getTypeName();
            FeatureType schema = getSchema(typeName);

            names = new String[schema.getAttributeCount()];

            for (int i = 0; i < schema.getAttributeCount(); i++) {
                names[i] = schema.getAttributeType(i).getName();
            }
        }

        return names;
    }

    /**
     * 
     * @see org.geotools.data.DataStore#updateSchema(java.lang.String,
     *      org.geotools.feature.FeatureType)
     */
    public void updateSchema(String typeName, FeatureType featureType)
        throws IOException {
        // TODO Auto-generated method stub
    }

    /**
     * Default implementation based on getFeatureReader and getFeatureWriter.
     * 
     * <p>
     * We should be able to optimize this to only get the RowSet once
     * </p>
     *
     * @see org.geotools.data.DataStore#getFeatureSource(java.lang.String)
     */
    public FeatureSource getFeatureSource(String typeName)
        throws IOException {
        if (typeHandler.getFIDMapper(typeName).isVolatile() || allowWriteOnVolatileFIDs) {
            LOGGER.fine("get Feature source called on " + typeName);

            if (OPTIMIZE_MODE == OPTIMIZE_SQL) {
                LOGGER.fine("returning pg feature locking");

                return new PostgisFeatureLocking(this, getSchema(typeName));
            }

            // default
            if (getLockingManager() != null) {
                // Use default JDBCFeatureLocking that delegates all locking
                // the getLockingManager
                LOGGER.fine("returning jdbc feature locking");

                return new JDBCFeatureLocking(this, getSchema(typeName));
            } else {
                LOGGER
                        .fine("returning jdbc feature store (lock manager is null)");

                // subclass should provide a FeatureLocking implementation
                // but for now we will simply forgo all locking
                return new JDBCFeatureStore(this, getSchema(typeName));
            }
        } else {
            return new JDBCFeatureSource(this, getSchema(typeName));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param fReader
     * @param queryData
     *
     * @return
     *
     * @throws IOException DOCUMENT ME!
     */
    protected JDBCFeatureWriter createFeatureWriter(FeatureReader fReader,
        QueryData queryData) throws IOException {
        return new PostgisFeatureWriter(fReader, queryData, WKBEnabled);
    }

    /**
     * Retrieve a FeatureWriter over entire dataset.
     * 
     * <p>
     * Quick notes: This FeatureWriter is often used to add new content, or
     * perform summary calculations over the entire dataset.
     * </p>
     * 
     * <p>
     * Subclass may wish to implement an optimized featureWriter for these
     * operations.
     * </p>
     * 
     * <p>
     * It should provide Feature for next() even when hasNext() is
     * <code>false</code>.
     * </p>
     * 
     * <p>
     * Subclasses are responsible for checking with the lockingManger unless
     * they are providing their own locking support.
     * </p>
     *
     * @param typeName
     * @param transaction
     *
     * @return
     *
     * @throws IOException
     *
     * @see org.geotools.data.DataStore#getFeatureWriter(java.lang.String,
     *      boolean, org.geotools.data.Transaction)
     */
    public FeatureWriter getFeatureWriter(String typeName,
        Transaction transaction) throws IOException {
        return getFeatureWriter(typeName, Filter.NONE, transaction);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.geotools.data.DataStore#getFeatureWriterAppend(java.lang.String,
     *      org.geotools.data.Transaction)
     */

    /**
     * Retrieve a FeatureWriter for creating new content.
     * 
     * <p>
     * Subclass may wish to implement an optimized featureWriter for this
     * operation. One based on prepaired statemnts is a possibility, as we do
     * not require a ResultSet.
     * </p>
     * 
     * <p>
     * To allow new content the FeatureWriter should provide Feature for next()
     * even when hasNext() is <code>false</code>.
     * </p>
     * 
     * <p>
     * Subclasses are responsible for checking with the lockingManger unless
     * they are providing their own locking support.
     * </p>
     *
     * @param typeName
     * @param transaction
     *
     * @return
     *
     * @throws IOException
     *
     * @see org.geotools.data.DataStore#getFeatureWriter(java.lang.String,
     *      boolean, org.geotools.data.Transaction)
     */
    public FeatureWriter getFeatureWriterAppend(String typeName,
        Transaction transaction) throws IOException {
        FeatureWriter writer = getFeatureWriter(typeName, Filter.ALL,
                transaction);

        while (writer.hasNext()) {
            writer.next(); // this would be a use for skip then :-)
        }

        return writer;
    }

    int getSRID(String typeName, String geomColName) throws IOException {
        return typeHandler.getFeatureTypeInfo(typeName).getSRID(geomColName);
    }

    /**
     * @see org.geotools.data.jdbc.JDBCDataStore#getGeometryAttributeIO(org.geotools.feature.AttributeType)
     */
    protected AttributeIO getGeometryAttributeIO(AttributeType type,
        QueryData queryData) {
        if (WKBEnabled) {
            return new PgWKBAttributeIO(isByteaEnabled());
        } else {
            return new WKTAttributeIO();
        }
    }

    protected int getResultSetType(boolean forWrite) {
        return ResultSet.TYPE_FORWARD_ONLY;
    }

    protected int getConcurrency(boolean forWrite) {
        return ResultSet.CONCUR_READ_ONLY;
    }

    /**
     * Returns true if the WKB format is used to transfer geometries, false
     * otherwise
     * 
     * @return
     * 
     * @uml.property name="WKBEnabled"
     */
    public boolean isWKBEnabled() {
        return WKBEnabled;
    }

    /**
     * If turned on, WKB will be used to transfer geometry data instead of  WKT
     * 
     * @param enabled
     * 
     * @uml.property name="WKBEnabled"
     */
    public void setWKBEnabled(boolean enabled) {
        WKBEnabled = enabled;
    }

    /**
     * Sets this postgis instance to use a less strict but faster bounding
     * box query.  Setting this to <tt>true</tt> will have PostGIS issue
     * bounding box queries against the envelope of the geometry, so some
     * may be <i>slighty</i> wrong, but will perform much faster.  The 
     * intersects function can still be used to obtain the exact query.
     * 
     * @param isLooseBbox <tt>true</tt> if this should have a loose Bbox.
     */
    public void setLooseBbox(boolean isLooseBbox) {
	this.looseBbox = isLooseBbox;
    }

    /**
     * Whether the bounding boxes issued against this postgis datastore are
     * on the envelope of the geometry or the actual geometry.
     *
     * @return <tt>true</tt> if the bounding box is 'loose', against the 
     *         envelope instead of the actual geometry.
     */
    public boolean isLooseBbox() {
	return looseBbox;
    }

    /**
     * Returns true if the data store is using the bytea function to fasten WKB
     * data transfer, false otherwise
     * 
     * @return
     * 
     * @uml.property name="byteaEnabled"
     */
    public boolean isByteaEnabled() {
        return byteaEnabled;
    }

    /**
     * Enables the use of bytea function for WKB data transfer (will improve
     * performance).  Note this function need not be set by the programmer,
     * as the datastore will use it to optimize performance whenever it can
     * (when postGIS is 0.7.2 or later)
     * 
     * @param byteaEnabled
     * 
     * @uml.property name="byteaEnabled"
     */
    public void setByteaEnabled(boolean byteaEnabled) {
        this.byteaEnabled = byteaEnabled;
    }

}
