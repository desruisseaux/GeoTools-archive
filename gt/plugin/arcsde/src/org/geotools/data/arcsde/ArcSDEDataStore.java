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
package org.geotools.data.arcsde;

import com.esri.sde.sdk.client.SeColumnDefinition;
import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeCoordinateReference;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeExtent;
import com.esri.sde.sdk.client.SeLayer;
import com.esri.sde.sdk.client.SeTable;
import com.vividsolutions.jts.geom.Envelope;
import org.geotools.data.AbstractDataStore;
import org.geotools.data.AttributeReader;
import org.geotools.data.DataSourceException;
import org.geotools.data.DefaultFeatureReader;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.filter.Filter;
import org.geotools.metadata.extent.GeographicExtent;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Implements a DataStore to work upon an ArcSDE spatial database gateway.
 * String[] getTypeNames() FeatureType getSchema(String typeName)
 * FeatureReader getFeatureReader( typeName ) FeatureWriter getFeatureWriter(
 * typeName ) Filter getUnsupportedFilter(String typeName, Filter filter)
 * FeatureReader getFeatureReader(String typeName, Query query)
 * 
 * <p>
 * All remaining functionality is implemented against these methods, including
 * Transaction and Locking Support. These implementations will not be optimal
 * but they will work.
 * </p>
 *
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id: ArcSDEDataStore.java,v 1.8 2004/06/28 10:24:32 jfear Exp $
 */
class ArcSDEDataStore extends AbstractDataStore {
    /** DOCUMENT ME! */
    private static final Logger LOGGER = Logger.getLogger(ArcSDEDataStore.class.getPackage()
                                                                               .getName());

    /** DOCUMENT ME! */
    private ArcSDEConnectionPool connectionPool;

    /** <code>Map&lt;typeName/FeatureType&gt;</code> of feature type schemas */
    private Map schemasCache = new HashMap();

    /** A mutex for synchronizing */
    private Object mutex = new Object();

    /**
     * Creates a new ArcSDEDataStore object.
     *
     * @param connectionPool DOCUMENT ME!
     */
    public ArcSDEDataStore(ArcSDEConnectionPool connectionPool) {
        super(true);
        this.connectionPool = connectionPool;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public ArcSDEConnectionPool getConnectionPool() {
        return this.connectionPool;
    }

    /**
     * DOCUMENT ME!
     *
     * @return the list of full qualified feature class names on the ArcSDE
     *         database this DataStore works on. An ArcSDE full qualified
     *         class name is composed of three dot separated strings:
     *         "DATABASE.USER.CLASSNAME", wich is usefull enough to use it as
     *         namespace
     *
     * @throws RuntimeException if an exception occurs while retrieving the
     *         list of registeres feature classes on the backend, or while
     *         obtaining the full qualified name of one of them
     */
    public String[] getTypeNames() {
        String[] featureTypesNames = null;

        try {
            List sdeLayers = connectionPool.getAvailableSdeLayers();
            featureTypesNames = new String[sdeLayers.size()];

            String typeName;
            int i = 0;

            for (Iterator it = sdeLayers.iterator(); it.hasNext(); i++) {
                typeName = ((SeLayer) it.next()).getQualifiedName();
                featureTypesNames[i] = typeName;
            }
        } catch (SeException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            throw new RuntimeException("Exception while fetching layer name: "
                + ex.getMessage(), ex);
        } catch (DataSourceException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            throw new RuntimeException("Exception while getting layers list: "
                + ex.getMessage(), ex);
        }

        return featureTypesNames;
    }

    /**
     * Obtains the schema for the given featuretype name.
     * 
     * <p>
     * Just for convenience, if the type name is not full qualified, it will be
     * prepended by the "&lt;DATABASE_NAME&gt;.&lt;USER_NAME&gt;." string.
     * Anyway, it is strongly recommended that you use <b>only</b> full
     * qualified type names. The rational for this is that the actual ArcSDE
     * name of a featuretype is full qualified,  and more than a single type
     * can exist with the same non qualified name, if they pertein to
     * different database users. So, if a non qualified name is passed, the
     * user name which will be prepended to it is the user used to create the
     * connections (i.e., the one you specified with the "user" parameter to
     * create the datastore.
     * </p>
     *
     * @param typeName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws java.io.IOException DOCUMENT ME!
     * @throws NullPointerException DOCUMENT ME!
     * @throws DataSourceException DOCUMENT ME!
     */
    public synchronized FeatureType getSchema(String typeName)
        throws java.io.IOException {
        if (typeName == null) {
            throw new NullPointerException("typeName is null");
        }

        //connection used to retrieve the user name if a non qualified type name was passed in
        SeConnection conn = null;

        //check if it is not qualified and prepend it with "instance.user." 
        if (typeName.indexOf('.') == -1) {
            try {
                conn = getConnectionPool().getConnection();
                LOGGER.warning(
                    "A non qualified type name was given, qualifying it...");
                typeName = conn.getDatabaseName() + "." + conn.getUser() + "."
                    + typeName;
                LOGGER.info("full qualified name is " + typeName);
            } catch (DataSourceException e) {
                throw e;
            } catch (UnavailableConnectionException e) {
                throw new DataSourceException("A non qualified type name ("
                    + typeName
                    + ") was passed and a connection to retrieve the user name "
                    + " is not available.", e);
            } catch (SeException e) {
                throw new DataSourceException("error obtaining the user name from a connection",
                    e);
            } finally {
                getConnectionPool().release(conn);
            }
        }

        FeatureType schema = (FeatureType) schemasCache.get(typeName);

        if (schema == null) {
            schema = ArcSDEAdapter.fetchSchema(getConnectionPool(), typeName);
            schemasCache.put(typeName, schema);
        }

        return schema;
    }

    /**
     * Creates the given featuretype in the underlying ArcSDE database.
     * 
     * <p>
     * The common use case to create an ArcSDE layer is to setup the SeTable
     * object with all the non-geometry attributes first, then create the
     * SeLayer and set the geometry column name and its properties. This
     * approach brings a nice problem, since we need to create the attributes
     * in exactly the same order as specified in the passed FeatureType, which
     * means that the geometry attribute needs not to be the last one.
     * </p>
     * 
     * <p>
     * To avoid this, the following workaround is performed: instead of
     * creating the schema as described above, we will first create the
     * SeTable with a single, temporary column, since it is not possible to
     * create a table without columns. The, we will iterate over the
     * AttributeTypes and add them as they appear using
     * <code>SeTable.addColumn(SeColumnDefinition)</code>. But if we found
     * that the current AttributeType is geometric, instead of adding the
     * column we just create the SeLayer object. This way, the geometric
     * attribute is inserted at the end, and then we keep iterating and adding
     * the rest of the columns. Finally, the first column is removed, since it
     * was temporal (note that I advertise it, it is a _workaround_).
     * </p>
     *
     * @param featureType the feature type containing the name, attributes and
     *        coordinate reference system of the new ArcSDE layer.
     *
     * @throws IOException see <code>throws DataSourceException</code> bellow
     * @throws IllegalArgumentException if the passed feature type does not
     *         contains at least one geometric attribute, or if the type name
     *         contains '.' (dots).
     * @throws NullPointerException if <code>featureType</code> is
     *         <code>null</code>
     * @throws DataSourceException if there is <b>not an available (free)
     *         connection</b> to the ArcSDE instance(in that case maybe you
     *         need to increase the maximun number of connections for the
     *         connection pool), or an SeException exception is catched while
     *         creating the feature type at the ArcSDE instance (e.g. a table
     *         with that name already exists).
     */
    public void createSchema(FeatureType featureType)
        throws IOException, IllegalArgumentException {
        if (featureType == null) {
            throw new NullPointerException(
                "You have to provide a FeatureType instance");
        }

        if (featureType.getDefaultGeometry() == null) {
            throw new IllegalArgumentException(
                "FeatureType must have at least a geometry attribute");
        }

        final String nonQualifiedTypeName = featureType.getTypeName();

        /*
           if (nonQualifiedTypeName.indexOf('.') != -1) {
               throw new IllegalArgumentException(
                   "Please do not use type names that contains '.' (dots)");
           }
         */

        // Create a new SeTable/SeLayer with the specified attributes....
        SeConnection connection = null;
        SeTable table = null;
        SeLayer layer = null;

        //flag to know if the table was created by us when catching an exception.
        boolean tableCreated = false;

        //placeholder to a catched exception to know in the finally block
        //if we should cleanup the crap we left in the database
        Exception error = null;

        try {
            connection = connectionPool.getConnection();

            //create a table with provided username
            String qualifiedName = null;

            if (nonQualifiedTypeName.indexOf('.') == -1) {
                qualifiedName = connection.getUser() + "."
                    + featureType.getTypeName();
                LOGGER.info("new full qualified type name: " + qualifiedName);
            } else {
                qualifiedName = nonQualifiedTypeName;
                LOGGER.info("full qualified type name provided by user: "
                    + qualifiedName);
            }

            layer = new SeLayer(connection);
            layer.setTableName(qualifiedName);

            final String HACK_COL_NAME = "gt_workaround_col_";
            table = createSeTable(connection, qualifiedName, HACK_COL_NAME);
            tableCreated = true;

            AttributeType[] atts = featureType.getAttributeTypes();
            AttributeType currAtt;

            for (int currAttIndex = 0; currAttIndex < atts.length;
                    currAttIndex++) {
                currAtt = atts[currAttIndex];

                if (currAtt instanceof GeometryAttributeType) {
                    GeometryAttributeType geometryAtt = (GeometryAttributeType) currAtt;
                    createSeLayer(layer, qualifiedName, geometryAtt);
                } else {
                    LOGGER.info("Creating column definition for " + currAtt);

                    SeColumnDefinition newCol = ArcSDEAdapter
                        .createSeColumnDefinition(currAtt);

                    ///////////////////////////////////////////////////////////////
                    //HACK!!!!: this hack is just to avoid the error that occurs //
                    //when adding a column wich is not nillable. Need to fix this//
                    //but by now it conflicts with the requirement of creating   //
                    //the schema with the correct attribute order.               //
                    ///////////////////////////////////////////////////////////////
                    newCol = new SeColumnDefinition(newCol.getName(),
                            newCol.getType(), newCol.getSize(),
                            newCol.getScale(), true);

                    ///////////////////////////////////////////////////////////////
                    //END of horrible HACK                                       //
                    ///////////////////////////////////////////////////////////////
                    LOGGER.info("Adding column " + newCol.getName()
                        + " to the actual table.");
                    table.addColumn(newCol);
                }
            }

            LOGGER.info("deleting the 'workaround' column...");
            table.dropColumn(HACK_COL_NAME);
            LOGGER.info("Schema correctly created: " + featureType);
        } catch (SeException e) {
            LOGGER.log(Level.WARNING, e.getSeError().getErrDesc(), e);
            throw new DataSourceException(e.getMessage(), e);
        } catch (DataSourceException dse) {
            LOGGER.log(Level.WARNING, dse.getMessage(), dse);
            throw dse;
        } catch (UnavailableConnectionException uce) {
            LOGGER.log(Level.WARNING, uce.getMessage(), uce);
            throw new DataSourceException(uce.getMessage(), uce);
        } finally {
            if ((error != null) && tableCreated) {
            }

            connectionPool.release(connection);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param connection
     * @param qualifiedName
     * @param hackColName DOCUMENT ME!
     *
     * @return
     *
     * @throws SeException
     */
    private SeTable createSeTable(SeConnection connection,
        String qualifiedName, String hackColName) throws SeException {
        SeTable table;
        final SeColumnDefinition[] tmpCol = {
                new SeColumnDefinition(hackColName,
                    SeColumnDefinition.TYPE_SMALLINT, 4, 0, true)
            };
        table = new SeTable(connection, qualifiedName);

        try {
            LOGGER.warning(
                "Remove the line 'table.delete()' for production use!!!");
            table.delete();
        } catch (SeException e) {
            //intentionally do nothing
        }

        LOGGER.info("creating table " + qualifiedName);

        //create the table using DBMS default configuration keyword.
        //valid keywords are defined in the dbtune table.
        table.create(tmpCol, "DEFAULTS");
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
        GeometryAttributeType geometryAtt) throws SeException {
        String spatialColName = geometryAtt.getName();
        LOGGER.info("setting spatial column name: " + spatialColName);
        layer.setSpatialColumnName(spatialColName);

        //Set the shape types that can be inserted into this layer
        int seShapeTypes = ArcSDEAdapter.guessShapeTypes(geometryAtt);
        layer.setShapeTypes(seShapeTypes);
        layer.setGridSizes(1100, 0, 0);
        layer.setDescription("Created with GeoTools");

        //Define the layer's Coordinate Reference
        CoordinateReferenceSystem crs = geometryAtt.getCoordinateSystem();
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

        ///////////////////////////
        //this param is used by ArcSDE for database initialization purposes
        int estInitFeatCount = 4;

        //this param is used by ArcSDE as an estimation of the average number
        //of points the layer's geometries will have, one never will know what for
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
     * @throws SeException DOCUMENT ME!
     */
    private static SeCoordinateReference getGenericCoordRef()
        throws SeException {
        //create a sde CRS with a huge value range and 5 digits of presission
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
     * @param typeName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws java.io.IOException DOCUMENT ME!
     */
    protected FeatureReader getFeatureReader(String typeName)
        throws java.io.IOException {
        return getFeatureReader(typeName, Query.ALL);
    }

    /**
     * GR: this method is called from inside getFeatureReader(Query
     * ,Transaction ) to allow subclasses return an optimized FeatureReader
     * wich supports the filter and attributes truncation specified in
     * <code>query</code>
     * 
     * <p>
     * A subclass that supports the creation of such an optimized FeatureReader
     * shold override this method. Otherwise, it just returns
     * <code>getFeatureReader(typeName)</code>
     * </p>
     * 
     * <p></p>
     *
     * @param typeName DOCUMENT ME!
     * @param query DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws DataSourceException DOCUMENT ME!
     */
    protected FeatureReader getFeatureReader(String typeName, Query query)
        throws IOException {
        ArcSDEQuery sdeQuery = null;
        FeatureReader reader = null;

        try {
            FeatureType schema = getSchema(typeName);
            sdeQuery = ArcSDEAdapter.createSeQuery(this, schema, query);
            sdeQuery.prepareQuery();
            sdeQuery.execute();

            AttributeReader attReader = new ArcSDEAttributeReader(sdeQuery);
            final FeatureType resultingSchema = sdeQuery.getSchema();
            reader = new DefaultFeatureReader(attReader, resultingSchema) {
                        protected Feature readFeature(AttributeReader atts)
                            throws IllegalAttributeException, IOException {
                        	ArcSDEAttributeReader sdeAtts = (ArcSDEAttributeReader) atts;
                        	Object [] currAtts = sdeAtts.readAll();
                        	System.arraycopy(currAtts, 0, attributes, 0, currAtts.length);
                        	/*
                            for (int i = 0, ii = atts.getAttributeCount();
                                    i < ii; i++) {
                                attributes[i] = atts.read(i);
                            }*/

                            return resultingSchema.create(attributes,
                                sdeAtts.readFID());
                        }
                    };
        } catch (SchemaException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new DataSourceException("Types do not match: "
                + ex.getMessage(), ex);
        } catch (Throwable t) {
            LOGGER.log(Level.SEVERE, t.getMessage(), t);

            if (sdeQuery != null) {
                sdeQuery.close();
            }

            throw new DataSourceException("Problem with feature reader: "
                + t.getMessage(), t);
        }

        return reader;
    }

    /**
     *
     */
    public FeatureReader getFeatureReader(Query query, Transaction transaction)
        throws IOException {
        String typeName = query.getTypeName();

        return getFeatureReader(typeName, query);
    }

    /**
     * GR: if a subclass supports filtering, it should override this method to
     * return the unsupported part of the passed filter, so a
     * FilteringFeatureReader will be constructed upon it. Otherwise it will
     * just return the same filter.
     * 
     * <p>
     * If the complete filter is supported, the subclass must return
     * <code>Filter.NONE</code>
     * </p>
     *
     * @param typeName DOCUMENT ME!
     * @param filter DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected Filter getUnsupportedFilter(String typeName, Filter filter) {
        try {
            ArcSDEAdapter.FilterSet filters = ArcSDEAdapter.computeFilters(this,
                    typeName, filter);

            Filter result = filters.getUnsupportedFilter();

            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Unsupported filter: " + result.toString());
            }

            return result;
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }

        return filter;
    }

    /**
     * DOCUMENT ME!
     *
     * @param typeName
     *
     * @return FeatureWriter over contents of typeName
     *
     * @throws IOException Subclass may throw IOException
     */
    protected FeatureWriter getFeatureWriter(String typeName)
        throws IOException {
        SeLayer layer = connectionPool.getSdeLayer(typeName);

        return new ArcSDEFeatureWriter(this, null, layer);
    }

    /**
     * Provides a writer that iterates over all of the features.
     *
     * @param typeName
     * @param transaction
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public FeatureWriter getFeatureWriter(String typeName,
        Transaction transaction) throws IOException {
        FeatureWriter featureWriter = super.getFeatureWriter(typeName,
                transaction);

        return featureWriter;
    }

    /**
     * DOCUMENT ME!
     *
     * @param typeName
     * @param filter
     * @param transaction
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public FeatureWriter getFeatureWriter(String typeName, Filter filter,
        Transaction transaction) throws IOException {
        FeatureType featureType = getSchema(typeName);
        AttributeType[] attributes = featureType.getAttributeTypes();
        String[] names = new String[attributes.length];

        // Extract the attribute names for the query, we want them all...
        for (int i = 0; i < names.length; i++) {
            names[i] = attributes[i].getName();
        }

        DefaultQuery query = new DefaultQuery(typeName, filter, 100, names,
                "handle");
        ArrayList list = new ArrayList();

        // We really don't need any transaction handling here, just keep it simple as
        // we are going to exhaust this feature reader immediately.  Really, this could
        // consume a great deal of memory based on the query.  
        // PENDING Jake Fear: Optimize this operation, exhausting the reader in this
        // case could be a cause of real trouble later on.  I need to think through 
        // the consequences of all of this.  Really the feature writer should 
        // delegate to a FeatureReader for the features that are queried.  That way
        // we can stream all of these goodies instead of having big fat chunks...
        //
        // All that said, this works until I get everything else completed....
        FeatureReader featureReader = getFeatureReader(query,
                Transaction.AUTO_COMMIT);

        while (featureReader.hasNext()) {
            try {
                list.add(featureReader.next());
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, ex.getMessage(), ex);

                break;
            }
        }

        // Well, this seems to come prepopulated with a state object, 
        // but I can't seem to figure out why.  As such we check for
        // and existing state, and check that states class as well. If
        // it is a state we already provided (or at least of a workable
        // type) then we will proceed with it.  Otherwise, we must remove
        // the state and replace it with an appropriate transaction
        // state object that we understand.  This should not present any
        // danger as the default state could not possibly have come from
        // us, and as such, no uncommitted changes could be lost.
        // Jake Fear 6/25/2004
        ArcTransactionState state = null;

        if (Transaction.AUTO_COMMIT != transaction) {
            synchronized (this) {
                Transaction.State s = transaction.getState(this);

                if (!(s instanceof ArcTransactionState)) {
                    if (s != null) {
                        transaction.removeState(this);
                    }

                    state = new ArcTransactionState(this);
                    transaction.putState(this, state);
                } else {
                    state = (ArcTransactionState) s;
                }
            }
        }

        SeLayer layer = connectionPool.getSdeLayer(typeName);
        FeatureWriter writer = new ArcSDEFeatureWriter(this, state, layer, list);

        return writer;
    }

    /**
     * Provides a <code>FeatureWriter</code> in an appropriate state for
     * immediately adding new <code>Feature</code> instances to  the specified
     * layer.
     *
     * @param typeName
     * @param transaction
     *
     * @return FeatureWriter whose hasNext() call will return false.
     *
     * @throws IOException DOCUMENT ME!
     */
    public FeatureWriter getFeatureWriterAppend(String typeName,
        Transaction transaction) throws IOException {
        ArcTransactionState state = null;

        if (Transaction.AUTO_COMMIT != transaction) {
            synchronized (this) {
                state = (ArcTransactionState) transaction.getState(this);

                if (state == null) {
                    state = new ArcTransactionState(this);
                    transaction.putState(this, state);
                }
            }
        }

        SeLayer layer = connectionPool.getSdeLayer(typeName);
        FeatureWriter writer = new ArcSDEFeatureWriter(this, state, layer);

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
     * @param query Contains the Filter and MaxFeatures to find the bounds for.
     *
     * @return The number of Features provided by the Query or <code>-1</code>
     *         if count is too expensive to calculate or any errors or occur.
     *
     * @throws IOException if there are errors getting the count
     */
    protected int getCount(Query query) throws IOException {
        LOGGER.info("getCount");

        ArcSDEQuery sdeQuery = null;

        try {
            sdeQuery = ArcSDEAdapter.createSeQuery(this, query);
            sdeQuery.prepareQuery();

            int count = sdeQuery.calculateResultCount();
            LOGGER.info("count: " + count);

            return count;
        } catch (IOException ex) {
        	throw ex;
        } finally {
            if (sdeQuery != null) {
                sdeQuery.close();
            }
        }
    }

    /**
     * Computes the bounds of the features for the specified feature type that
     * satisfy the query provided that there is a fast way to get that result.
     * 
     * <p>
     * Will return null if there is not fast way to compute the bounds. Since
     * it's based on some kind of header/cached information, it's not
     * guaranteed to be real bound of the features
     * </p>
     *
     * @param query
     *
     * @return the bounds, or null if too expensive
     *
     * @throws IOException
     * @throws DataSourceException DOCUMENT ME!
     */
    protected Envelope getBounds(Query query) throws IOException {
        LOGGER.info("getBounds");

        ArcSDEQuery sdeQuery = null;

        try {
            sdeQuery = ArcSDEAdapter.createSeQuery(this, query);
            sdeQuery.prepareQuery();

            Envelope ev = sdeQuery.calculateQueryExtent();
            LOGGER.info("bounds: " + ev);

            return ev;
        } catch (Exception ex) {
            if (ex instanceof IOException) {
                throw (IOException) ex;
            } else {
                throw new DataSourceException("unable to get bounds: "
                    + ex.getMessage(), ex);
            }
        } finally {
            LOGGER.info("closing query for bounds");

            if (sdeQuery != null) {
                sdeQuery.close();
            }
        }
    }
}
