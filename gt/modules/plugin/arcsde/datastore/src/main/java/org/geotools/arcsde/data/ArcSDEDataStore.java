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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.jsqlparser.statement.select.PlainSelect;

import org.geotools.arcsde.data.view.QueryInfoParser;
import org.geotools.arcsde.data.view.SelectQualifier;
import org.geotools.arcsde.pool.ArcSDEConnectionPool;
import org.geotools.arcsde.pool.ArcSDEPooledConnection;
import org.geotools.data.AttributeReader;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultFeatureReader;
import org.geotools.data.DefaultQuery;
import org.geotools.data.Diff;
import org.geotools.data.DiffFeatureReader;
import org.geotools.data.EmptyFeatureReader;
import org.geotools.data.FeatureListenerManager;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.data.FilteringFeatureReader;
import org.geotools.data.LockingManager;
import org.geotools.data.MaxFeatureReader;
import org.geotools.data.Query;
import org.geotools.data.ReTypeFeatureReader;
import org.geotools.data.Transaction;
import org.geotools.data.TransactionStateDiff;
import org.geotools.data.view.DefaultView;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;

import sun.nio.cs.ext.ISCII91;

import com.esri.sde.sdk.client.SeDefs;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeExtent;
import com.esri.sde.sdk.client.SeLayer;
import com.esri.sde.sdk.client.SeQueryInfo;
import com.esri.sde.sdk.client.SeTable;
import com.vividsolutions.jts.geom.Envelope;

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
 * @author Gabriel Roldan, The Open Planning Project
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/unsupported/arcsde/datastore/src/main/java/org/geotools/arcsde/data/ArcSDEDataStore.java $
 * @version $Id$
 */
public class ArcSDEDataStore implements DataStore {

    private static final Logger LOGGER = Logger.getLogger("org.geotools.arcsde.data");

    /** Manages listener lists for FeatureSource implementation */
    FeatureListenerManager listenerManager = new FeatureListenerManager();

    private ArcSDEConnectionPool connectionPool;

    /**
     * <code>Map&lt;typeName/FeatureType&gt;</code> of inprocess views feature
     * type schemas registered through
     * {@link #registerView(String, PlainSelect)}
     */
    private Map<String, FeatureType> viewSchemasCache = new HashMap<String, FeatureType>();

    private Map<String, PlainSelect> viewSelectStatements = new HashMap<String, PlainSelect>();

    /**
     * In process view definitions
     */
    private Map<String, SeQueryInfo> viewQueryInfos = new HashMap<String, SeQueryInfo>();

    /** Cached feature types */
    private Map<String, FeatureType> schemasCache = new HashMap<String, FeatureType>();

    /**
     * Namespace URI to construct FeatureTypes and AttributeTypes with
     */
    private String namespace;

    public ArcSDEDataStore(final ArcSDEConnectionPool connPool) {
        this(connPool, "http://www.geotools.org");
    }

    public ArcSDEDataStore(final ArcSDEConnectionPool connPool, final String namespaceUri) {
        this.connectionPool = connPool;
        this.namespace = namespaceUri;
    }

    public void createSchema(final SimpleFeatureType featureType) throws IOException {
        createSchema(featureType, null);
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
    public synchronized SimpleFeatureType getSchema(final String typeName)
            throws java.io.IOException {
        // connection used to retrieve the user name if a non qualified type
        // name was passed in
        final ArcSDEPooledConnection conn = getConnectionPool().getConnection();
        SimpleFeatureType schema;
        try {
            schema = getSchema(typeName, conn);
        } finally {
            conn.close();
        }
        return schema;
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
        List<String> layerNames = new ArrayList<String>(connectionPool.getAvailableLayerNames());
        layerNames.addAll(viewSchemasCache.keySet());
        return layerNames.toArray(new String[layerNames.size()]);
    }

    public void dispose() {
        LOGGER.info("dispose not yet implemented for ArcSDE, don't forget to do that!");
    }

    public FeatureReader getFeatureReader(final Query query, final Transaction transaction)
            throws IOException {
        Filter filter = query.getFilter();
        String typeName = query.getTypeName();
        String propertyNames[] = query.getPropertyNames();

        if (filter == null) {
            throw new NullPointerException("getFeatureReader requires Filter: "
                + "did you mean Filter.INCLUDE?");
        }
        if( typeName == null ){
            throw new NullPointerException(
                "getFeatureReader requires typeName: "
                + "use getTypeNames() for a list of available types");
        }
        if (transaction == null) {
            throw new NullPointerException(
                "getFeatureReader requires Transaction: "
                + "did you mean to use Transaction.AUTO_COMMIT?");
        }
        SimpleFeatureType featureType = getSchema( query.getTypeName() );

        if( propertyNames != null || query.getCoordinateSystem()!=null ){
            try {
                featureType = DataUtilities.createSubType( featureType, propertyNames, query.getCoordinateSystem() );
            } catch (SchemaException e) {
                LOGGER.log( Level.FINEST, e.getMessage(), e);
                throw new DataSourceException( "Could not create Feature Type for query", e );

            }
        }
        if ( filter == Filter.EXCLUDE || filter.equals( Filter.EXCLUDE )) {
            return new EmptyFeatureReader(featureType);
        }
        //GR: allow subclases to implement as much filtering as they can,
        //by returning just it's unsupperted filter
        filter = getUnsupportedFilter(typeName, filter);
        if(filter == null){
            throw new NullPointerException("getUnsupportedFilter shouldn't return null. Do you mean Filter.INCLUDE?");
        }

        // There are cases where the readers have to lock.  Take shapefile for example.  Getting a Reader causes
        // the file to be locked.  However on a commit TransactionStateDiff locks before a writer is obtained.  In order to 
        // prevent deadlocks either the diff has to obtained first or the reader has to be obtained first.
        // Because shapefile writes to a buffer first the actual write lock is not flipped until the transaction has most of the work
        // done.  As a result I suggest getting the diff first then getting the reader.
        // JE
//        Diff diff=null;
//        if (transaction != Transaction.AUTO_COMMIT) {
//            TransactionStateDiff state = state(transaction);
//            if( state != null ){
//                diff = state.diff(typeName);
//            }
//        }
        
        // This calls our subclass "simple" implementation
        // All other functionality will be built as a reader around
        // this class
        //
        FeatureReader reader = getFeatureReader(typeName, query);

//        if( diff!=null ){
//            reader = new DiffFeatureReader(reader, diff, query.getFilter());
//        }

        if (!filter.equals( Filter.INCLUDE ) ) {
            reader = new FilteringFeatureReader(reader, filter);
        }

        if (!featureType.equals(reader.getFeatureType())) {
            LOGGER.fine("Recasting feature type to subtype by using a ReTypeFeatureReader");
            reader = new ReTypeFeatureReader(reader, featureType, false);
        }

        if (query.getMaxFeatures() != Query.DEFAULT_MAX) {
                reader = new MaxFeatureReader(reader, query.getMaxFeatures());
        }

        return reader;
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
                    return SimpleFeatureBuilder.build(resultingSchema, this.attributes, sdeAtts.readFID());
                }
            };
        } catch (SchemaException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            if (sdeQuery != null) {
                sdeQuery.close();
            }
            throw new DataSourceException("Types do not match: " + ex.getMessage(), ex);
        } catch (IOException e) {
            if (sdeQuery != null) {
                sdeQuery.close();
            }
            throw e;
        } catch (Exception t) {
            LOGGER.log(Level.SEVERE, t.getMessage(), t);
            if (sdeQuery != null) {
                sdeQuery.close();
            }
            throw new DataSourceException("Problem with feature reader: " + t.getMessage(), t);
        }
        return reader;
    }

    public FeatureSource getFeatureSource(final String typeName) throws IOException {
        FeatureSource fsource;
        final SimpleFeatureType featureType = getSchema(typeName);
        if (isView(typeName)) {
            fsource = new ArcSdeFeatureSource(featureType, this);
        } else if (canWrite(typeName)) {
            fsource = new ArcSdeFeatureStore(featureType, this);
        } else {
            fsource = new ArcSdeFeatureSource(featureType, this);
        }
        return fsource;
    }

    public FeatureWriter getFeatureWriter(final String typeName, final Transaction transaction)
            throws IOException {
        return getFeatureWriter(typeName, Filter.INCLUDE, transaction);
    }

    public FeatureWriter getFeatureWriter(final String typeName, final Filter filter,
            final Transaction transaction) throws IOException {
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

    public FeatureWriter getFeatureWriterAppend(final String typeName, final Transaction transaction)
            throws IOException {
        final ArcTransactionState state = getArcTransactionState(transaction);

        SeLayer layer;
        FIDReader fidStrategy;
        // use the same connection than for the transaction to
        // retrieve the fid strategy, don't close it as its in
        // the ArcTransactionState domain to do that
        ArcSDEPooledConnection conn;
        if (state == null) {
            conn = getConnectionPool().getConnection();
        } else {
            conn = state.getConnection();
        }
        layer = connectionPool.getSdeLayer(conn, typeName);
        fidStrategy = FIDReader.getFidReader(conn, layer);
        if (state == null) {
            conn.close();
        }

        FeatureWriter writer = new ArcSDEFeatureWriter(this, fidStrategy, state, layer);

        return writer;
    }

    /**
     * @return <code>null</code>, no locking yet
     * @see DataStore#getLockingManager()
     */
    public LockingManager getLockingManager() {
        return null;
    }

    public FeatureSource getView(final Query query) throws IOException, SchemaException {
        return new DefaultView(this.getFeatureSource(query.getTypeName()), query);
    }

    public void updateSchema(final String typeName, final SimpleFeatureType featureType)
            throws IOException {
        throw new UnsupportedOperationException("Schema modification not supported");
    }

    // ////// NON API Methods /////////

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
    private org.opengis.filter.Filter getUnsupportedFilter(String typeName, Filter filter) {
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
     * Connection pool as provided during construction.
     * 
     * @return Connection Pool (as provided during construction)
     */
    public ArcSDEConnectionPool getConnectionPool() {
        return this.connectionPool;
    }

    /**
     * Checks whether typeName is writable by the connection's user.
     * <p>
     * Will only return true if the user has both update and insert priviledges.
     * </p>
     * 
     * @param typeName
     * @return
     * @throws DataSourceException
     */
    private boolean canWrite(final String typeName) throws DataSourceException {
        final SeTable sdeTable = connectionPool.getSdeTable(typeName);
        final int permissions;
        try {
            permissions = sdeTable.getPermissions();
        } catch (SeException e) {
            throw new DataSourceException(e);
        }
        final int insertMask = SeDefs.SE_INSERT_PRIVILEGE;
        final int updateMask = SeDefs.SE_UPDATE_PRIVILEGE;
        boolean canWrite = false;
        if (((insertMask & permissions) == insertMask)
                && ((updateMask & permissions) == updateMask)) {
            canWrite = true;
        }
        return canWrite;
    }

    /**
     * Grab the ArcTransactionState (when not using AUTO_COMMIT).
     * <p>
     * As of GeoTools 2.5 we store the TransactionState using the connection
     * pool as a key.
     * 
     * @param transaction
     * @return
     */
    public ArcTransactionState getArcTransactionState(Transaction transaction) {
        ArcTransactionState state = null;

        if (Transaction.AUTO_COMMIT != transaction) {
            synchronized (this) {
                state = (ArcTransactionState) transaction.getState(connectionPool);

                if (state == null) {
                    // start a transaction
                    state = new ArcTransactionState(this);
                    transaction.putState(connectionPool, state);
                }
            }
        }
        return state;
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

    SimpleFeatureType getSchema(final String typeName, final ArcSDEPooledConnection conn)
            throws java.io.IOException {
        assert typeName != null;
        assert conn != null;

        SimpleFeatureType schema = (SimpleFeatureType) viewSchemasCache.get(typeName);

        if (schema == null) {
            // // check if it is not qualified and prepend it with
            // "instance.user."
            // if (typeName.indexOf('.') == -1) {
            // try {
            // LOGGER.warning("A non qualified type name was given, qualifying
            // it...");
            // if (conn.getDatabaseName() != null &&
            // conn.getDatabaseName().length() != 0) {
            // typeName = conn.getDatabaseName() + "." + conn.getUser() + "." +
            // typeName;
            // } else {
            // typeName = conn.getUser() + "." + typeName;
            // }
            // LOGGER.info("full qualified name is " + typeName);
            // } catch (SeException e) {
            // throw new DataSourceException(
            // "error obtaining the user name from a connection", e);
            // }
            // }

            schema = (SimpleFeatureType) schemasCache.get(typeName);

            if (schema == null) {
                ArcSDEConnectionPool connectionPool = getConnectionPool();
                SeLayer layer = connectionPool.getSdeLayer(conn, typeName);
                SeTable table = connectionPool.getSdeTable(conn, typeName);
                schema = ArcSDEAdapter.fetchSchema(layer, table, this.namespace);
                schemasCache.put(typeName, schema);
            }
        }

        return schema;
    }

    public void createSchema(SimpleFeatureType featureType, Map hints) throws IOException,
            IllegalArgumentException {
        ArcSDEAdapter.createSchema(featureType, hints, connectionPool);
    }

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

        SimpleFeatureType viewSchema = ArcSDEAdapter.fetchSchema(connectionPool, typeName,
                namespace, queryInfo);
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
        List<Object> errors = new LinkedList<Object>();
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
    private void verifyUnsupportedSqlConstruct(List<Object> errors, Object construct) {
        if (construct instanceof List) {
            List constructsList = (List) construct;
            if (constructsList.size() > 0) {
                errors.add(constructsList);
            }
        } else if (construct != null) {
            errors.add(construct);
        }
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
    int getCount(Query query) throws IOException {
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
    ReferencedEnvelope getBounds(Query query) throws IOException {
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
}
