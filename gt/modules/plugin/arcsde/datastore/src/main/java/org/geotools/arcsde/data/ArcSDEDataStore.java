/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, Geotools Project Managment Committee (PMC)
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
package org.geotools.arcsde.data;

import com.esri.sde.sdk.client.*;
import com.vividsolutions.jts.geom.Envelope;
import net.sf.jsqlparser.statement.select.PlainSelect;
import org.geotools.arcsde.data.view.QueryInfoParser;
import org.geotools.arcsde.data.view.SelectQualifier;
import org.geotools.arcsde.pool.ArcSDEConnectionPool;
import org.geotools.arcsde.pool.ArcSDEPooledConnection;
import org.geotools.arcsde.pool.UnavailableArcSDEConnectionException;
import org.geotools.data.*;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.beans.FeatureDescriptor;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implements a DataStore to work upon an ArcSDE spatial database gateway.
 * String[] getTypeNames() FeatureType getSchema(String typeName) FeatureReader
 * getFeatureReader( typeName ) FeatureWriter getFeatureWriter( typeName )
 * Filter getUnsupportedFilter(String typeName, Filter filter) FeatureReader
 * getFeatureReader(String typeName, Query query)
 * 
 * <p>
 * All remaining functionality is implemented against these methods, including
 * Transaction and Locking Support. These implementations will not be optimal
 * but they will work.
 * </p>
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/unsupported/arcsde/datastore/src/main/java/org/geotools/arcsde/data/ArcSDEDataStore.java $
 * @version $Id$
 */
public class ArcSDEDataStore extends AbstractDataStore {
    /** DOCUMENT ME! */
    private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger(ArcSDEDataStore.class.getName());

    private static final String DEFAULT_NAMESPACE = "http://www.geotools.org/sde";

    /** DOCUMENT ME! */
    private ArcSDEConnectionPool connectionPool;

    private String namespace;

    /** <code>Map&lt;typeName/FeatureType&gt;</code> of feature type schemas */
    private Map schemasCache = new HashMap();

    /**
     * <code>Map&lt;typeName/FeatureType&gt;</code> of inprocess views feature
     * type schemas registered through
     * {@link #registerView(String, PlainSelect)}
     */
    private Map viewSchemasCache = new HashMap();

    private Map viewSelectStatements = new HashMap();

    /**
     * <code>Map&lt;typeName/SeQueryInfo&gt;</code> of inprocess views
     */
    private Map viewQueryInfos = new HashMap();

    /**
     * Creates a new ArcSDEDataStore object.
     * 
     * @param connectionPool
     *            DOCUMENT ME!
     */
    public ArcSDEDataStore(ArcSDEConnectionPool connectionPool) {
        this(connectionPool, DEFAULT_NAMESPACE);
    }

    /**
     * 
     * @param connectionPool
     *            datastore's connection pool. Not null.
     * @param nsUri
     *            datastore's namespace. May be null.
     */
    public ArcSDEDataStore(ArcSDEConnectionPool connectionPool, String nsUri) {
        super(true);
        if (connectionPool == null) {
            throw new NullPointerException("connectionPool");
        }
        if (nsUri == null) {
            throw new NullPointerException("namespace");
        }
        this.connectionPool = connectionPool;
        this.namespace = nsUri;
    }

    /**
     * Connection pool as provided during construction.
     * 
     * @return Connection Pool (as provided during construction)
     */
    public ArcSDEConnectionPool getConnectionPool() {
        return this.connectionPool;
    }

    public String getNamespace() {
        return this.namespace;
    }

    /**
     * List of type names; should be a list of all feature classes.
     * 
     * @return the list of full qualified feature class names on the ArcSDE
     *         database this DataStore works on. An ArcSDE full qualified class
     *         name is composed of three dot separated strings:
     *         "DATABASE.USER.CLASSNAME", wich is usefull enough to use it as
     *         namespace
     * 
     * @throws RuntimeException
     *             if an exception occurs while retrieving the list of
     *             registeres feature classes on the backend, or while obtaining
     *             the full qualified name of one of them
     */
    public String[] getTypeNames() throws IOException {
        List layerNames = new ArrayList(connectionPool.getAvailableLayerNames());
        layerNames.addAll(viewSchemasCache.keySet());
        return (String[]) layerNames.toArray(new String[layerNames.size()]);
    }

    /**
     * Obtains the schema for the given featuretype name.
     * 
     * <p>
     * Just for convenience, if the type name is not full qualified, it will be
     * prepended by the "&lt;DATABASE_NAME&gt;.&lt;USER_NAME&gt;." string.
     * Anyway, it is strongly recommended that you use <b>only </b> full
     * qualified type names. The rational for this is that the actual ArcSDE
     * name of a featuretype is full qualified, and more than a single type can
     * exist with the same non qualified name, if they pertein to different
     * database users. So, if a non qualified name is passed, the user name
     * which will be prepended to it is the user used to create the connections
     * (i.e., the one you specified with the "user" parameter to create the
     * datastore.
     * </p>
     * 
     * @param typeName
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * 
     * @throws java.io.IOException
     *             DOCUMENT ME!
     * @throws NullPointerException
     *             DOCUMENT ME!
     * @throws DataSourceException
     *             DOCUMENT ME!
     */
    public synchronized SimpleFeatureType getSchema(String typeName) throws java.io.IOException {
        if (typeName == null) {
            throw new NullPointerException("typeName is null");
        }

        SimpleFeatureType schema = (SimpleFeatureType) viewSchemasCache.get(typeName);

        if (schema == null) {
            // connection used to retrieve the user name if a non qualified type
            // name was passed in
            ArcSDEPooledConnection conn = null;

            // check if it is not qualified and prepend it with "instance.user."
            if (typeName.indexOf('.') == -1) {
                try {
                    conn = getConnectionPool().getConnection();
                    LOGGER.warning("A non qualified type name was given, qualifying it...");
                    if (conn.getDatabaseName() != null && conn.getDatabaseName().length() != 0) {
                        typeName = conn.getDatabaseName() + "." + conn.getUser() + "." + typeName;
                    } else {
                        typeName = conn.getUser() + "." + typeName;
                    }
                    LOGGER.info("full qualified name is " + typeName);
                } catch (DataSourceException e) {
                    throw e;
                } catch (UnavailableArcSDEConnectionException e) {
                    throw new DataSourceException("A non qualified type name (" + typeName
                            + ") was passed and a connection to retrieve the user name "
                            + " is not available.", e);
                } catch (SeException e) {
                    throw new DataSourceException(
                            "error obtaining the user name from a connection", e);
                } finally {
                    conn.close();
                }
            }

            schema = (SimpleFeatureType) schemasCache.get(typeName);

            if (schema == null) {
                schema = ArcSDEAdapter.fetchSchema(getConnectionPool(), typeName, this.namespace);
                schemasCache.put(typeName, schema);
            }
        }

        return schema;
    }

    /**
     * Pass-through to the createSchema method with a config keyword. This
     * method calls createSchema(schema, null);
     * 
     */
    public void createSchema(SimpleFeatureType schema) throws IOException, IllegalArgumentException {
        createSchema(schema, null);
    }

    /**
     * Creates the given featuretype in the underlying ArcSDE database.
     * 
     * <p>
     * The common use case to create an ArcSDE layer is to setup the SeTable
     * object with all the non-geometry attributes first, then create the
     * SeLayer and set the geometry column name and its properties. This
     * approach brings a nice problem, since we need to create the attributes in
     * exactly the same order as specified in the passed FeatureType, which
     * means that the geometry attribute needs not to be the last one.
     * </p>
     * 
     * <p>
     * To avoid this, the following workaround is performed: instead of creating
     * the schema as described above, we will first create the SeTable with a
     * single, temporary column, since it is not possible to create a table
     * without columns. The, we will iterate over the AttributeTypes and add
     * them as they appear using
     * <code>SeTable.addColumn(SeColumnDefinition)</code>. But if we found
     * that the current AttributeType is geometric, instead of adding the column
     * we just create the SeLayer object. This way, the geometric attribute is
     * inserted at the end, and then we keep iterating and adding the rest of
     * the columns. Finally, the first column is removed, since it was temporary
     * (note that I advertise it, it is a _workaround_).
     * </p>
     * 
     * <p>
     * Sometimes some 'extra' information is required to correctly create the
     * underlying ArcSDE SeLayer. For instance, a specific configuration keyword
     * might be required to be used (instead of "DEFAULTS"), or a particular
     * column might need to be marked as the rowid column for the featuretype.
     * 
     * A non-null <code>hints</code> parameter contains a mapping from a list
     * of well-known {@link java.lang.String} keys to values. The possible keys
     * are listed in the table below. keys with any other values are ignored.
     * 
     * <table>
     * <tr>
     * <td>key name</td>
     * <td>key value type</td>
     * <td>default value (if applicable)</td>
     * </tr>
     * 
     * <tr>
     * <td>configuration.keyword</td>
     * <td>{@link java.lang.String}</td>
     * <td>"DEFAULTS"</td>
     * </tr>
     * 
     * <tr>
     * <td>rowid.column.type</td>
     * <td>{@link java.lang.String} - "NONE", "USER" and "SDE" are the only
     * valid values</td>
     * <td>"NONE"</td>
     * </tr>
     * 
     * <tr>
     * <td>rowid.column.name</td>
     * <td>{@link java.lang.String}</td>
     * <td>null</td>
     * </tr>
     * 
     * </p>
     * 
     * @param featureType
     *            the feature type containing the name, attributes and
     *            coordinate reference system of the new ArcSDE layer.
     * 
     * @param hints
     *            A map containing extra ArcSDE-specific hints about how to
     *            create the underlying ArcSDE SeLayer and SeTable objects from
     *            this FeatureType.
     * 
     * @throws IOException
     *             see <code>throws DataSourceException</code> bellow
     * @throws IllegalArgumentException
     *             if the passed feature type does not contains at least one
     *             geometric attribute, or if the type name contains '.' (dots).
     * @throws NullPointerException
     *             if <code>featureType</code> is <code>null</code>
     * @throws DataSourceException
     *             if there is <b>not an available (free) connection </b> to the
     *             ArcSDE instance(in that case maybe you need to increase the
     *             maximun number of connections for the connection pool), or an
     *             SeException exception is catched while creating the feature
     *             type at the ArcSDE instance (e.g. a table with that name
     *             already exists).
     */
    public void createSchema(SimpleFeatureType featureType, Map hints) throws IOException,
            IllegalArgumentException {
        if (featureType == null) {
            throw new NullPointerException("You have to provide a FeatureType instance");
        }
        /*
         * if(!(featureType instanceof FeatureType)){ throw new
         * IllegalArgumentException("ArcSDE datastore supports only
         * SimpleFeatureType"); }
         */

        if (featureType.getDefaultGeometry() == null) {
            throw new IllegalArgumentException(
                    "FeatureType must have at least a geometry attribute");
        }

        final String nonQualifiedTypeName = featureType.getTypeName();

        if (nonQualifiedTypeName.indexOf('.') != -1) {
            throw new IllegalArgumentException(
                    "Please do not use type names that contains '.' (dots)");
        }

        // Create a new SeTable/SeLayer with the specified attributes....
        ArcSDEPooledConnection connection = null;
        SeTable table = null;
        SeLayer layer = null;

        // flag to know if the table was created by us when catching an
        // exception.
        boolean tableCreated = false;

        // table/layer creation hints information
        int rowIdType = SeRegistration.SE_REGISTRATION_ROW_ID_COLUMN_TYPE_NONE;
        String rowIdColumn = null;
        String configKeyword = "DEFAULTS";
        if (hints != null) {
            if (hints.get("configuration.keyword") instanceof String) {
                configKeyword = (String) hints.get("configuration.keyword");
            }
            if (hints.get("rowid.column.type") instanceof String) {
                String rowIdStr = (String) hints.get("rowid.column.type");
                if (rowIdStr.equalsIgnoreCase("NONE")) {
                    rowIdType = SeRegistration.SE_REGISTRATION_ROW_ID_COLUMN_TYPE_NONE;
                } else if (rowIdStr.equalsIgnoreCase("USER")) {
                    rowIdType = SeRegistration.SE_REGISTRATION_ROW_ID_COLUMN_TYPE_USER;
                } else if (rowIdStr.equalsIgnoreCase("SDE")) {
                    rowIdType = SeRegistration.SE_REGISTRATION_ROW_ID_COLUMN_TYPE_SDE;
                } else {
                    throw new DataSourceException(
                            "createSchema hint 'rowid.column.type' must be one of 'NONE', 'USER' or 'SDE'");
                }
            }
            if (hints.get("rowid.column.name") instanceof String) {
                rowIdColumn = (String) hints.get("rowid.column.name");
            }
        }

        // placeholder to a catched exception to know in the finally block
        // if we should cleanup the crap we left in the database
        Exception error = null;

        try {
            connection = connectionPool.getConnection();

            // create a table with provided username
            String qualifiedName = null;

            if (nonQualifiedTypeName.indexOf('.') == -1) {
                qualifiedName = connection.getUser() + "." + featureType.getTypeName();
                LOGGER.finer("new full qualified type name: " + qualifiedName);
            } else {
                qualifiedName = nonQualifiedTypeName;
                LOGGER.finer("full qualified type name provided by user: " + qualifiedName);
            }

            layer = new SeLayer(connection);
            layer.setTableName(qualifiedName);
            layer.setCreationKeyword(configKeyword);

            final String HACK_COL_NAME = "gt_workaround_col_";

            table = createSeTable(connection, qualifiedName, HACK_COL_NAME, configKeyword);
            tableCreated = true;

            List atts = Arrays.asList(featureType.getAttributes());
            AttributeDescriptor currAtt;

            for (Iterator it = atts.iterator(); it.hasNext();) {
                currAtt = (AttributeDescriptor) it.next();

                if (currAtt instanceof GeometryDescriptor) {
                    GeometryDescriptor geometryAtt = (GeometryDescriptor) currAtt;
                    createSeLayer(layer, qualifiedName, geometryAtt);
                } else {
                    LOGGER.fine("Creating column definition for " + currAtt);

                    SeColumnDefinition newCol = ArcSDEAdapter.createSeColumnDefinition(currAtt);

                    // /////////////////////////////////////////////////////////////
                    // HACK!!!!: this hack is just to avoid the error that
                    // occurs //
                    // when adding a column wich is not nillable. Need to fix
                    // this//
                    // but by now it conflicts with the requirement of creating
                    // //
                    // the schema with the correct attribute order. //
                    // /////////////////////////////////////////////////////////////
                    newCol = new SeColumnDefinition(newCol.getName(), newCol.getType(), newCol
                            .getSize(), newCol.getScale(), true);

                    // /////////////////////////////////////////////////////////////
                    // END of horrible HACK //
                    // /////////////////////////////////////////////////////////////
                    LOGGER.fine("Adding column " + newCol.getName() + " to the actual table.");
                    table.addColumn(newCol);
                }
            }

            LOGGER.fine("deleting the 'workaround' column...");
            table.dropColumn(HACK_COL_NAME);

            LOGGER.fine("setting up table registration with ArcSDE...");
            SeRegistration reg = new SeRegistration(connection, table.getName());
            if (rowIdColumn != null) {
                LOGGER.fine("setting rowIdColumnName to " + rowIdColumn + " in table "
                        + reg.getTableName());
                reg.setRowIdColumnName(rowIdColumn);
                reg.setRowIdColumnType(rowIdType);
                reg.alter();
                reg = null;
            }

            LOGGER.fine("Schema correctly created: " + featureType);

        } catch (SeException e) {
            LOGGER.log(Level.WARNING, e.getSeError().getErrDesc(), e);
            throw new DataSourceException(e.getMessage(), e);
        } catch (DataSourceException dse) {
            LOGGER.log(Level.WARNING, dse.getMessage(), dse);
            throw dse;
        } finally {
            if ((error != null) && tableCreated) {
                // TODO: remove table if created and then failed
            }
            connection.close();
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @param connection
     * @param qualifiedName
     * @param hackColName
     *            DOCUMENT ME!
     * 
     * 
     * @throws SeException
     */
    private SeTable createSeTable(ArcSDEPooledConnection connection, String qualifiedName,
            String hackColName, String configKeyword) throws SeException {
        SeTable table;
        final SeColumnDefinition[] tmpCol = { new SeColumnDefinition(hackColName,
                SeColumnDefinition.TYPE_STRING, 4, 0, true) };
        table = new SeTable(connection, qualifiedName);

        try {
            LOGGER.warning("Remove the line 'table.delete()' for production use!!!");
            table.delete();
        } catch (SeException e) {
            // intentionally do nothing
        }

        LOGGER.info("creating table " + qualifiedName);

        // create the table using DBMS default configuration keyword.
        // valid keywords are defined in the dbtune table.
        table.create(tmpCol, configKeyword);
        LOGGER.info("table " + qualifiedName + " created...");

        return table;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param layer
     * @param qualifiedName
     * @param geometryAtt
     * 
     * @throws SeException
     */
    private void createSeLayer(SeLayer layer, String qualifiedName,
            GeometryDescriptor geometryAtt) throws SeException {
        String spatialColName = geometryAtt.getLocalName();
        LOGGER.info("setting spatial column name: " + spatialColName);
        layer.setSpatialColumnName(spatialColName);

        // Set the shape types that can be inserted into this layer
        int seShapeTypes = ArcSDEAdapter.guessShapeTypes(geometryAtt);
        layer.setShapeTypes(seShapeTypes);
        layer.setGridSizes(1100, 0, 0);
        layer.setDescription("Created with GeoTools");

        // Define the layer's Coordinate Reference
        CoordinateReferenceSystem crs = geometryAtt.getCRS();
        SeCoordinateReference coordref = getGenericCoordRef();
        String WKT = null;

        if (crs == null) {
            LOGGER.warning("Creating feature type " + qualifiedName
                    + ": the geometry attribute does not supply a coordinate reference system");
        } else {
            LOGGER.info("Creating the SeCoordRef object for CRS " + crs);
            WKT = crs.toWKT();
            coordref.setCoordSysByDescription(WKT);
        }

        SeExtent validCoordRange = null;

        if ((WKT != null) && (WKT.indexOf("GEOGCS") != -1)) {
            validCoordRange = new SeExtent(-180, -90, 180, 90);
        } else {
            validCoordRange = coordref.getXYEnvelope();
        }

        layer.setExtent(validCoordRange);

        LOGGER.info("Applying CRS " + coordref.getCoordSysDescription());
        layer.setCoordRef(coordref);
        LOGGER.info("CRS applyed to the new layer.");

        // /////////////////////////
        // this param is used by ArcSDE for database initialization purposes
        int estInitFeatCount = 4;

        // this param is used by ArcSDE as an estimation of the average number
        // of points the layer's geometries will have, one never will know what
        // for
        int estAvgPointsPerFeature = 4;
        LOGGER.info("Creating the layer...");
        layer.create(estInitFeatCount, estAvgPointsPerFeature);
        LOGGER.info("ArcSDE layer created.");
    }

    /**
     * Creates and returns a <code>SeCoordinateReference</code> CRS, though
     * based on an UNKNOWN CRS, is inclusive enough (in terms of valid
     * coordinate range and presicion) to deal with most coordintates.
     * 
     * <p>
     * Actually tested to deal with coordinates with 0.0002 units of separation
     * as well as with large coordinates such as UTM (values greater than
     * 500,000.00)
     * </p>
     * 
     * <p>
     * This method is driven by the equally named method in TestData.java
     * </p>
     * 
     * @return DOCUMENT ME!
     * 
     * @throws SeException
     *             DOCUMENT ME!
     */
    private static SeCoordinateReference getGenericCoordRef() throws SeException {
        // create a sde CRS with a huge value range and 5 digits of presission
        SeCoordinateReference seCRS = new SeCoordinateReference();
        int shift = 600000;
        SeExtent validRange = new SeExtent(-shift, -shift, shift, shift);
        seCRS.setXYByEnvelope(validRange);
        LOGGER.info("CRS: " + seCRS.getXYEnvelope());

        return seCRS;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param typeName
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * 
     * @throws java.io.IOException
     *             DOCUMENT ME!
     */
    protected FeatureReader getFeatureReader(String typeName) throws java.io.IOException {
        return getFeatureReader(typeName, Query.ALL);
    }

    /**
     * GR: this method is called from inside getFeatureReader(Query ,Transaction )
     * to allow subclasses return an optimized FeatureReader which supports the
     * filter and attributes truncation specified in <code>query</code>
     * 
     * <p>
     * A subclass that supports the creation of such an optimized FeatureReader
     * should override this method. Otherwise, it just returns
     * <code>getFeatureReader(typeName)</code>
     * </p>
     * 
     * @param typeName
     *            DOCUMENT ME!
     * @param query
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * 
     * @throws IOException
     *             DOCUMENT ME!
     * @throws DataSourceException
     *             DOCUMENT ME!
     */
    protected FeatureReader getFeatureReader(String typeName, Query query) throws IOException {
        ArcSDEQuery sdeQuery = null;
        FeatureReader reader = null;

        try {
            SimpleFeatureType schema = getSchema(typeName);
            sdeQuery = ArcSDEQuery.createQuery(this, schema, query);

            sdeQuery.execute();

            AttributeReader attReader = new ArcSDEAttributeReader(sdeQuery);
            final SimpleFeatureType resultingSchema = sdeQuery.getSchema();
            reader = new DefaultFeatureReader(attReader, resultingSchema) {
                protected org.opengis.feature.simple.SimpleFeature readFeature(AttributeReader atts)
                        throws IllegalAttributeException, IOException {
                    ArcSDEAttributeReader sdeAtts = (ArcSDEAttributeReader) atts;
                    Object[] currAtts = sdeAtts.readAll();
                    System.arraycopy(currAtts, 0, this.attributes, 0, currAtts.length);

                    /*
                     * for (int i = 0, ii = atts.getAttributeCount(); i < ii;
                     * i++) { attributes[i] = atts.read(i); }
                     */
                    
                    return SimpleFeatureBuilder.build(resultingSchema, this.attributes, sdeAtts.readFID());
                }
            };
        } catch (SchemaException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new DataSourceException("Types do not match: " + ex.getMessage(), ex);
        } catch (IOException e) {
            throw e;
        } catch (Exception t) {
            LOGGER.log(Level.SEVERE, t.getMessage(), t);
            throw new DataSourceException("Problem with feature reader: " + t.getMessage(), t);
        }finally{
            if (sdeQuery != null) {
                sdeQuery.close();
            }
        }

        return reader;
    }

    /**
     * 
     */
    /*public FeatureReader getFeatureReader(Query query, Transaction transaction) throws IOException {
        String typeName = query.getTypeName();

        return getFeatureReader(typeName, query);
    }*/

    /**
     * GR: if a subclass supports filtering, it should override this method to
     * return the unsupported part of the passed filter, so a
     * FilteringFeatureReader will be constructed upon it. Otherwise it will
     * just return the same filter.
     * 
     * <p>
     * If the complete filter is supported, the subclass must return
     * <code>Filter.INCLUDE</code>
     * </p>
     * 
     * @param typeName
     *            DOCUMENT ME!
     * @param filter
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    protected org.opengis.filter.Filter getUnsupportedFilter(String typeName, Filter filter) {
        try {
            SeLayer layer;
            SeQueryInfo qInfo;

            if (isView(typeName)) {
                qInfo = getViewQueryInfo(typeName);
                String mainLayerName;
                try {
                    mainLayerName = qInfo.getConstruct().getTables()[0];
                } catch (SeException e) {
                    throw new RuntimeException(e.getMessage());
                }
                layer = connectionPool.getSdeLayer(mainLayerName);
            } else {
                layer = connectionPool.getSdeLayer(typeName);
                qInfo = null;
            }
            
            ArcSDEPooledConnection conn = null;
            FIDReader fidReader;
            try {
                conn = connectionPool.getConnection();
                fidReader = FIDReader.getFidReader(conn, layer);
            } finally {
                if (conn != null) conn.close();
            }

            SimpleFeatureType schema = getSchema(typeName);
            ArcSDEQuery.FilterSet filters = ArcSDEQuery.createFilters(layer, schema, filter, qInfo,
                    getViewSelectStatement(typeName), fidReader);

            Filter result = filters.getUnsupportedFilter();

            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Supported filters: " + filters.getSqlFilter() + " --- "
                        + filters.getGeometryFilter());
                LOGGER.fine("Unsupported filter: " + result.toString());
            }

            return result;
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }

        return filter;
    }

    /**
     * Used to modify features using Transaction AUTO_COMMIT.
     * <p>
     * It is recommended that you use a Transaction.
     * 
     * @param typeName
     * @return FeatureWriter over contents of typeName
     * 
     * @throws IOException
     *             Subclass may throw IOException
     */
    protected FeatureWriter getFeatureWriter(String typeName) throws IOException {
        ArcSDEPooledConnection conn;
        SeLayer layer;
        FIDReader fidStrategy;
        try {
            conn = connectionPool.getConnection();
        } catch (UnavailableArcSDEConnectionException e) {
            throw new DataSourceException(e);
        }
        try {
            layer = connectionPool.getSdeLayer(conn, typeName);
            fidStrategy = FIDReader.getFidReader(conn, layer);
        } finally {
            conn.close();
        }
        return new ArcSDEFeatureWriter(this, fidStrategy, null, layer);
    }

    /**
     * Provides a writer that iterates over all of the features.
     * 
     * @param typeName
     * @param transaction
     * 
     * @return DOCUMENT ME!
     * 
     * @throws IOException
     *             DOCUMENT ME!
     */
    public FeatureWriter getFeatureWriter(String typeName, Transaction transaction)
            throws IOException {
        FeatureWriter featureWriter = super.getFeatureWriter(typeName, transaction);

        return featureWriter;
    }

    /**
     * This method is a complete and utter override! As such the facilities
     * like TransactionStateDiff provided by the super class are not
     * hooked up!
     * 
     * @param typeName
     * @param filter
     * @param transaction
     * 
     * @return DOCUMENT ME!
     * 
     * @throws IOException
     *             DOCUMENT ME!
     */
    public FeatureWriter getFeatureWriter(String typeName, Filter filter, Transaction transaction)
            throws IOException {

        SimpleFeatureType featureType = getSchema(typeName);
        List<AttributeDescriptor> attributes = featureType.getAttributes();
        String[] names = new String[attributes.size()];

        // Extract the attribute names for the query, we want them all...
        for (int i = 0; i < names.length; i++) {
            names[i] = attributes.get(i).getLocalName();
        }

        DefaultQuery query = new DefaultQuery(typeName, filter, 100, names, "handle");
        ArrayList list = new ArrayList();

        // We really don't need any transaction handling here, just keep it
        // simple as
        // we are going to exhaust this feature reader immediately. Really, this
        // could
        // consume a great deal of memory based on the query.
        // PENDING Jake Fear: Optimize this operation, exhausting the reader in
        // this
        // case could be a cause of real trouble later on. I need to think
        // through
        // the consequences of all of this. Really the feature writer should
        // delegate to a FeatureReader for the features that are queried. That
        // way
        // we can stream all of these goodies instead of having big fat
        // chunks...
        //
        // All that said, this works until I get everything else completed....
        FeatureReader featureReader = getFeatureReader(query, Transaction.AUTO_COMMIT);

        while (featureReader.hasNext()) {
            try {
                list.add(featureReader.next());
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, ex.getMessage(), ex);
                break;
            }
        }
        featureReader.close();

        ArcTransactionState state = getArcTransactionState(transaction);

        ArcSDEPooledConnection connection = connectionPool.getConnection();
        SeLayer layer;
        FIDReader fidStrategy;
        try {
            layer = connectionPool.getSdeLayer(connection, typeName);
            fidStrategy = FIDReader.getFidReader(connection, layer);
        } finally {
            connection.close();
        }

        FeatureWriter writer = new ArcSDEFeatureWriter(this, fidStrategy, state, layer, list);

        return writer;
    }

    /**
     * Return null as we do not need to use memory based transaction state
     * to hold edits before a commit.
     * <p>
     * Saul can you confirm this is in fact the case? Some code in your superclass
     * was in trouble because it was trying to check this value.
     * <p>
     * @return null indicating memory based transaction isolation is not needed.
     */
    protected TransactionStateDiff state(Transaction transaction) {
        return null;
    }
    
    /**
     * Grab the ArcTransactionState (when not using AUTO_COMMIT).
     * <p>
     * As of GeoTools 2.5 we store the TransactionState using
     * the connection pool as a key.
     * 
     * @param transaction
     * @return
     */
    public ArcTransactionState getArcTransactionState(
            Transaction transaction) {
        // Well, this seems to come prepopulated with a state object,
        // but I can't seem to figure out why. As such we check for
        // and existing state, and check that states class as well. If
        // it is a state we already provided (or at least of a workable
        // type) then we will proceed with it. Otherwise, we must remove
        // the state and replace it with an appropriate transaction
        // state object that we understand. This should not present any
        // danger as the default state could not possibly have come from
        // us, and as such, no uncommitted changes could be lost.
        // Jake Fear 6/25/2004
        // That is because you are using *this* to look up stuff, *this*
        // is already used by your super class AbtractDataStore to hold the
        // TransactionStateDiff (so if you are doing your own thing don't
        // use AbstractDataStore?!?!?)
        //
        // Jody Garnett 11/12/2007
        ArcTransactionState state = null;

        if (Transaction.AUTO_COMMIT != transaction) {
            synchronized (this) {
                state = (ArcTransactionState) transaction.getState(connectionPool);

                                if( state == null ){
                                    // start a transaction
                  state = new ArcTransactionState(this);
                  transaction.putState(connectionPool, state);
                }
            }
        }
        return state;
    }

    /**
     * Provides a <code>FeatureWriter</code> in an appropriate state for
     * immediately adding new <code>Feature</code> instances to the specified
     * layer.
     * 
     * @param typeName
     * @param transaction
     * 
     * @return FeatureWriter whose hasNext() call will return false.
     * 
     * @throws IOException
     *             DOCUMENT ME!
     */
    public FeatureWriter getFeatureWriterAppend(String typeName, Transaction transaction)
            throws IOException {
        ArcTransactionState state = getArcTransactionState( transaction );

        SeLayer layer;
        FIDReader fidStrategy;
        ArcSDEPooledConnection conn = connectionPool.getConnection();
        try {
            layer = connectionPool.getSdeLayer(conn, typeName);
            fidStrategy = FIDReader.getFidReader(conn, layer);
        } finally {
            conn.close();
        }
        FeatureWriter writer = new ArcSDEFeatureWriter(this, fidStrategy, state, layer);

        return writer;
    }

    /**
     * Gets the number of the features that would be returned by this query for
     * the specified feature type.
     * 
     * <p>
     * If getBounds(Query) returns <code>-1</code> due to expense consider
     * using <code>getFeatures(Query).getCount()</code> as a an alternative.
     * </p>
     * 
     * @param query
     *            Contains the Filter and MaxFeatures to find the bounds for.
     * 
     * @return The number of Features provided by the Query or <code>-1</code>
     *         if count is too expensive to calculate or any errors or occur.
     * 
     * @throws IOException
     *             if there are errors getting the count
     */
    protected int getCount(Query query) throws IOException {
        LOGGER.fine("getCount");

        int count = ArcSDEQuery.calculateResultCount(this, query);
        LOGGER.fine("count: " + count);

        return count;
    }

    /**
     * Computes the bounds of the features for the specified feature type that
     * satisfy the query provided that there is a fast way to get that result.
     * 
     * <p>
     * Will return null if there is not fast way to compute the bounds. Since
     * it's based on some kind of header/cached information, it's not guaranteed
     * to be real bound of the features
     * </p>
     * 
     * @param query non null query and query.getTypeName()
     * 
     * @return the bounds, or null if too expensive
     * 
     * @throws IOException
     */
    protected ReferencedEnvelope getBounds(Query query) throws IOException {
        LOGGER.fine("getBounds");

        Envelope ev;
        if (query.getFilter().equals(Filter.INCLUDE)) {
            LOGGER.fine("getting bounds of entire layer.  Using optimized SDE call.");
            // we're really asking for a bounds of the WHOLE layer,
            // let's just ask SDE metadata for that, rather than doing an
            // expensive query
            SeLayer thisLayer = this.connectionPool.getSdeLayer(query.getTypeName());
            SeExtent extent = thisLayer.getExtent();
            ev = new Envelope(extent.getMinX(), extent.getMaxX(), extent.getMinY(), extent
                    .getMaxY());
        } else {
            ev = ArcSDEQuery.calculateQueryExtent(this, query);
        }

        if (LOGGER.isLoggable(Level.FINE)) {
            if (ev != null)
                LOGGER.fine("ArcSDE optimized getBounds call returned: " + ev);
            else
                LOGGER.fine("ArcSDE couldn't process all filters in this query, so optimized getBounds() returns null.");
        }

        return ReferencedEnvelope.reference( ev );
    }

    /**
     * Returns wether <code>typeName</code> refers to a FeatureType registered
     * as an in-process view through {@link #registerView(String, PlainSelect)}.
     * 
     * @param typeName
     * @return <code>true</code> if <code>typeName</code> is registered as a
     *         view given a SQL SELECT query, <code>false</code> otherwise.
     */
    public boolean isView(String typeName) {
        return viewSchemasCache.containsKey(typeName);
    }

    public SeQueryInfo getViewQueryInfo(String typeName) {
        SeQueryInfo qInfo = (SeQueryInfo) viewQueryInfos.get(typeName);
        return qInfo;
    }

    public PlainSelect getViewSelectStatement(String typeName) {
        PlainSelect select = (PlainSelect) viewSelectStatements.get(typeName);
        return select;
    }

    /**
     * Creates an in-process data view against one or more actual FeatureTypes
     * of this DataStore, which will be advertised as <code>typeName</code>
     * 
     * @param typeName
     *            the name of the view's FeatureType.
     * @param sqlQuery
     *            a full SQL query which will act as the view definition.
     * @throws IOException
     */
    // public void registerView(String typeName, String sqlQuery) throws
    // IOException {
    // LOGGER.fine("about to register view " + typeName + "=" + sqlQuery);
    // SelectBody select = parseSqlQuery(sqlQuery);
    // registerView(typeName, select);
    // }
    // private static SelectBody parseSqlQuery(String selectStatement) throws
    // IOException {
    // CCJSqlParserManager pm = new CCJSqlParserManager();
    // Reader reader = new StringReader(selectStatement);
    // Statement statement;
    // try {
    // statement = pm.parse(reader);
    // } catch (Exception e) {
    // throw new DataSourceException("parsing select statement: " +
    // e.getCause().getMessage(),
    // e);
    // }
    // if (!(statement instanceof Select)) { // either PlainSelect or Union
    // throw new IllegalArgumentException("expected select or union statement: "
    // + statement);
    // }
    // SelectBody selectBody = ((Select) statement).getSelectBody();
    // return selectBody;
    // }
    /**
     * 
     * @param typeName
     * @param select
     *            may be a {@link PlainSelect} or a {@link Union}. If it is a
     *            <code>Union</code> will simple throw an
     *            UnsupportedOperationException.
     * @throws IOException
     * @throws UnsupportedOperationException
     *             if <code>select</code> is a <code>Union</code> or a
     *             <code>PlainSelect</code> containing a construct not
     *             supported by ArcSDE
     */
    // public void registerView(String typeName, SelectBody select) throws
    // IOException,
    // UnsupportedOperationException {
    // if (!(select instanceof PlainSelect)) {
    // throw new UnsupportedOperationException("ArcSDE supports only a limited"
    // + " set of PlainSelect construct: " + select);
    // }
    // registerView(typeName, (PlainSelect) select);
    // }
    /**
     * Supported constructs:
     * <ul>
     * <li>FromItems
     * <li>SelectItems
     * <li>Top (as in SELECT TOP 10 * FROM...)
     * <li>Where
     * </ul>
     * 
     * @param typeName
     * @param select
     * @throws IOException
     */
    public void registerView(final String typeName, final PlainSelect select) throws IOException {

        if (typeName == null)
            throw new NullPointerException("typeName");
        if (select == null)
            throw new NullPointerException("select");
        if (Arrays.asList(getTypeNames()).contains(typeName)) {
            throw new IllegalArgumentException(typeName + " already exists as a FeatureType");
        }

        verifyQueryIsSupported(select);

        ArcSDEPooledConnection conn = connectionPool.getConnection();

        PlainSelect qualifiedSelect = SelectQualifier.qualify(conn, select);
        // System.out.println(qualifiedSelect);

        SeQueryInfo queryInfo;
        LOGGER.fine("creating definition query info");
        try {
            queryInfo = QueryInfoParser.parse(conn, qualifiedSelect);
        } catch (SeException e) {
            throw new DataSourceException("Parsing select: " + e.getMessage(), e);
        } finally {
            conn.close();
        }

        SimpleFeatureType viewSchema = ArcSDEAdapter.fetchSchema(connectionPool, typeName, namespace,
                queryInfo);
        LOGGER.fine("view schema: " + viewSchema);

        this.viewQueryInfos.put(typeName, queryInfo);
        this.viewSchemasCache.put(typeName, viewSchema);
        this.viewSelectStatements.put(typeName, qualifiedSelect);
    }

    /**
     * Unsupported constructs:
     * <ul>
     * <li>GroupByColumnReferences
     * <li>Joins
     * <li>Into
     * <li>Limit
     * </ul>
     * Not yet verified to work:
     * <ul>
     * <li>Distinct
     * <li>Having
     * <li>
     * </ul>
     * 
     * @param select
     * @throws UnsupportedOperationException
     *             if any of the unsupported constructs are found on
     *             <code>select</code>
     */
    private void verifyQueryIsSupported(PlainSelect select) throws UnsupportedOperationException {
        List errors = new LinkedList();
        // @TODO errors.add(select.getDistinct());
        // @TODO errors.add(select.getHaving());
        verifyUnsupportedSqlConstruct(errors, select.getGroupByColumnReferences());
        verifyUnsupportedSqlConstruct(errors, select.getInto());
        verifyUnsupportedSqlConstruct(errors, select.getJoins());
        verifyUnsupportedSqlConstruct(errors, select.getLimit());
        if (errors.size() > 0) {
            throw new UnsupportedOperationException("The following constructs are not supported: "
                    + errors);
        }
    }

    /**
     * If construct is not null or an empty list, adds it to the list of errors.
     * 
     * @param errors
     * @param construct
     */
    private void verifyUnsupportedSqlConstruct(List errors, Object construct) {
        if (construct instanceof List) {
            List constructsList = (List) construct;
            if (constructsList.size() > 0) {
                errors.add(constructsList);
            }
        } else if (construct != null) {
            errors.add(construct);
        }
    }

}
