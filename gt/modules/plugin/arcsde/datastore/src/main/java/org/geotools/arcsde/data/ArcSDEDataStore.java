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
import org.geotools.arcsde.pool.UnavailableArcSDEConnectionException;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultQuery;
import org.geotools.data.EmptyFeatureReader;
import org.geotools.data.FeatureListenerManager;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.FeatureWriter;
import org.geotools.data.FilteringFeatureReader;
import org.geotools.data.LockingManager;
import org.geotools.data.MaxFeatureReader;
import org.geotools.data.Query;
import org.geotools.data.ReTypeFeatureReader;
import org.geotools.data.Transaction;
import org.geotools.data.view.DefaultView;
import org.geotools.feature.SchemaException;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;

import com.esri.sde.sdk.client.SeDefs;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeLayer;
import com.esri.sde.sdk.client.SeQueryInfo;
import com.esri.sde.sdk.client.SeTable;

/**
 * DataStore implementation to work upon an ArcSDE spatial database gateway.
 * 
 * @author Gabriel Roldan (TOPP)
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

    /**
     * Per inprocess view typeName SQL query definitions
     */
    private Map<String, PlainSelect> viewSelectStatements = new HashMap<String, PlainSelect>();

    /**
     * In process view definitions in ArcSDE Java API terms created from their
     * SQL definitions
     */
    private Map<String, SeQueryInfo> viewQueryInfos = new HashMap<String, SeQueryInfo>();

    /** Cached feature types */
    private Map<String, FeatureType> schemasCache = new HashMap<String, FeatureType>();

    /**
     * Namespace URI to construct FeatureTypes and AttributeTypes with
     */
    private String namespace;

    /**
     * Creates a new ArcSDE DataStore working over the given connection pool
     * 
     * @param connPool
     *            pool of {@link ArcSDEPooledConnection} this datastore works
     *            upon.
     */
    public ArcSDEDataStore(final ArcSDEConnectionPool connPool) {
        this(connPool, null);
    }

    /**
     * Creates a new ArcSDE DataStore working over the given connection pool
     * 
     * @param connPool
     *            pool of {@link ArcSDEPooledConnection} this datastore works
     *            upon.
     * @param namespaceUri
     *            namespace URI for the {@link SimpleFeatureType}s,
     *            {@link AttributeType}s, and {@link AttributeDescriptor}s
     *            created by this datastore. May be <code>null</code>.
     */
    public ArcSDEDataStore(final ArcSDEConnectionPool connPool, final String namespaceUri) {
        this.connectionPool = connPool;
        this.namespace = namespaceUri;
    }

    /**
     * @see DataStore#createSchema(SimpleFeatureType)
     * @see #createSchema(SimpleFeatureType, Map)
     */
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
     * @see DataStore#getSchema(String)
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

    /**
     * TODO: implement dispose()!
     */
    public void dispose() {
        LOGGER.info("dispose not yet implemented for ArcSDE, don't forget to do that!");
    }

    /**
     * Returns an {@link ArcSDEFeatureReader}
     * <p>
     * Preconditions:
     * <ul>
     * <li><code>query != null</code>
     * <li><code>query.getTypeName() != null</code>
     * <li><code>query.getFilter != null</code>
     * <li><code>transaction != null</code>
     * </ul>
     * </p>
     * 
     * @see DataStore#getFeatureReader(Query, Transaction)
     * @return {@link ArcSDEFeatureReader} aware of the transaction state
     */
    public FeatureReader getFeatureReader(final Query query, final Transaction transaction)
            throws IOException {
        assert query != null;
        assert query.getTypeName() != null;
        assert query.getFilter() != null;
        assert transaction != null;

        ArcSDEPooledConnection connection;
        {
            if (Transaction.AUTO_COMMIT == transaction) {
                connection = connectionPool.getConnection();
            } else {
                ArcTransactionState state = ArcTransactionState.getState(transaction,
                        connectionPool);
                connection = state.getConnection();
            }
        }

        // indicates the feature reader should close the connection when done
        // if it's not inside a transaction.
        final boolean handleConnection = true;
        FeatureReader reader = getFeatureReader(query, connection, handleConnection);
        return reader;
    }

    /**
     * Returns an {@link ArcSDEFeatureReader} for the given query that works
     * against the given connection.
     * <p>
     * Explicitly stating the connection to use allows for the feature reader to
     * fetch the differencies (additions/modifications/deletions) made while a
     * transaction is in progress.
     * </p>
     * 
     * @param query
     *            the Query containing the request criteria
     * @param connection
     *            the connection to use to retrieve content
     * @param readerClosesConnection
     *            flag indicating whether the reader should auto-close the
     *            connection when exhausted or when close() is called.
     * @return
     * @throws IOException
     */
    private FeatureReader getFeatureReader(final Query query,
            final ArcSDEPooledConnection connection, final boolean readerClosesConnection)
            throws IOException {
        final String typeName = query.getTypeName();
        final String propertyNames[] = query.getPropertyNames();

        final SimpleFeatureType completeSchema = getSchema(typeName, connection);
        final ArcSDEQuery sdeQuery;

        Filter filter = query.getFilter();
        SimpleFeatureType featureType = completeSchema;

        if (propertyNames != null || query.getCoordinateSystem() != null) {
            try {
                featureType = DataUtilities.createSubType(featureType, propertyNames, query
                        .getCoordinateSystem());
            } catch (SchemaException e) {
                LOGGER.log(Level.FINEST, e.getMessage(), e);
                throw new DataSourceException("Could not create Feature Type for query", e);

            }
        }
        if (filter == Filter.EXCLUDE || filter.equals(Filter.EXCLUDE)) {
            return new EmptyFeatureReader(featureType);
        }

        if (isView(typeName)) {
            SeQueryInfo definitionQuery = getViewQueryInfo(typeName);
            PlainSelect viewSelectStatement = getViewSelectStatement(typeName);
            sdeQuery = ArcSDEQuery.createQuery(connection, completeSchema, query, definitionQuery,
                    viewSelectStatement);
        } else {
            sdeQuery = ArcSDEQuery.createQuery(connection, completeSchema, query);
        }

        sdeQuery.execute();

        final ArcSDEAttributeReader attReader = new ArcSDEAttributeReader(sdeQuery, connection,
                readerClosesConnection);
        FeatureReader reader;
        try {
            reader = new ArcSDEFeatureReader(attReader);
        } catch (SchemaException e) {
            throw new RuntimeException("Schema missmatch, should never happen!: " + e.getMessage(),
                    e);
        }

        filter = getUnsupportedFilter(typeName, filter, connection);
        if (!filter.equals(Filter.INCLUDE)) {
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
     * @see DataStore#getFeatureSource(String)
     * @return {@link FeatureSource} or {@link FeatureStore} depending on if the
     *         user has write permissions over <code>typeName</code>
     */
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

    /**
     * Delegates to
     * {@link #getFeatureWriter(String, Filter, Transaction) getFeatureWriter(typeName, Filter.INCLUDE, transaction)}
     * 
     * @see DataStore#getFeatureWriter(String, Transaction)
     */
    public FeatureWriter getFeatureWriter(final String typeName, final Transaction transaction)
            throws IOException {
        return getFeatureWriter(typeName, Filter.INCLUDE, transaction);
    }

    /**
     * 
     * @see DataStore#getFeatureWriter(String, Filter, Transaction)
     */
    public FeatureWriter getFeatureWriter(final String typeName, final Filter filter,
            final Transaction transaction) throws IOException {
        if (!canWrite(typeName)) {
            throw new DataSourceException("No write permissions over " + typeName);
        }
        // get the connection the streamed writer content has to work over
        // so the reader and writer share it
        final ArcSDEPooledConnection connection;
        final ArcTransactionState state;
        {
            if (Transaction.AUTO_COMMIT == transaction) {
                connection = connectionPool.getConnection();
                state = null;
            } else {
                state = ArcTransactionState.getState(transaction, connectionPool);
                connection = state.getConnection();
            }
        }

        try {
            final SimpleFeatureType featureType = getSchema(typeName, connection);
            final DefaultQuery query = new DefaultQuery(typeName, filter);

            // get a reader over the requested content. It'll be used to stream
            // out the writer's features and will share a connection and
            // transaction. handleConnection in false means the reader shall not
            // try to close the connection, the writer will take care of it if
            // need be.
            final boolean handleConnection = false;
            final FeatureReader reader = getFeatureReader(query, connection, handleConnection);

            FeatureWriter writer;

            if (Transaction.AUTO_COMMIT == transaction) {
                writer = new AutoCommitFeatureWriter(featureType, reader, connection);
            } else {
                // if there's a transaction, the reader and the writer will
                // share
                // the connection
                // held in the transaction state
                writer = new TransactionFeatureWriter(featureType, reader, state);
            }
            return writer;
        } catch (IOException e) {
            try {
                connection.rollbackTransaction();
            } catch (SeException e1) {
                LOGGER.log(Level.SEVERE, "Error rolling back transaction on " + connection, e);
            }
            connection.close();
            throw e;
        } catch (RuntimeException e) {
            try {
                connection.rollbackTransaction();
            } catch (SeException e1) {
                LOGGER.log(Level.SEVERE, "Error rolling back transaction on " + connection, e);
            }
            connection.close();
            throw e;
        }
    }

    /**
     * Delegates to
     * {@link #getFeatureWriter(String, Filter, Transaction) getFeatureWriter(typeName, Filter.EXCLUDE, transaction)}
     * 
     * @see DataStore#getFeatureWriterAppend(String, Transaction)
     */
    public FeatureWriter getFeatureWriterAppend(final String typeName, final Transaction transaction)
            throws IOException {
        return getFeatureWriter(typeName, Filter.EXCLUDE, transaction);
    }

    /**
     * @return <code>null</code>, no locking yet
     * @see DataStore#getLockingManager()
     */
    public LockingManager getLockingManager() {
        return null;
    }

    /**
     * @see DataStore#getView(Query)
     */
    public FeatureSource getView(final Query query) throws IOException, SchemaException {
        return new DefaultView(this.getFeatureSource(query.getTypeName()), query);
    }

    /**
     * This operation is not supported at this version of the GeoTools ArcSDE
     * plugin.
     * 
     * @see DataStore#updateSchema(String, SimpleFeatureType)
     */
    public void updateSchema(final String typeName, final SimpleFeatureType featureType)
            throws IOException {
        throw new UnsupportedOperationException("Schema modification not supported");
    }

    // ////// NON API Methods /////////

    /**
     * Returns the unsupported part of the passed filter, so a
     * FilteringFeatureReader will be constructed upon it. Otherwise it will
     * just return the same filter.
     * 
     * <p>
     * If the complete filter is supported, returns <code>Filter.INCLUDE</code>
     * </p>
     * 
     * @param typeName
     *            DOCUMENT ME!
     * @param filter
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    private org.opengis.filter.Filter getUnsupportedFilter(final String typeName,
            final Filter filter, final ArcSDEPooledConnection conn) {
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
                layer = conn.getLayer(mainLayerName);
            } else {
                layer = conn.getLayer(typeName);
                qInfo = null;
            }

            FIDReader fidReader = FIDReader.getFidReader(conn, layer);

            SimpleFeatureType schema = getSchema(typeName, conn);
            PlainSelect viewSelectStatement = getViewSelectStatement(typeName);
            ArcSDEQuery.FilterSet filters = ArcSDEQuery.createFilters(layer, schema, filter, qInfo,
                    viewSelectStatement, fidReader);

            Filter result = filters.getUnsupportedFilter();

            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Supported filters: " + filters.getSqlFilter() + " --- "
                        + filters.getGeometryFilter());
                LOGGER.fine("Unsupported filter: " + result.toString());
            }

            return result;
        } catch (Exception ex) {
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
        final SeTable sdeTable;
        {
            ArcSDEPooledConnection conn = null;
            try {
                conn = connectionPool.getConnection();
                sdeTable = conn.getTable(typeName);
            } catch (UnavailableArcSDEConnectionException e) {
                throw new DataSourceException(e);
            } finally {
                if (conn != null) {
                    conn.close();
                }
            }
        }
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
                SeLayer layer = conn.getLayer(typeName);
                SeTable table = conn.getTable(typeName);
                schema = ArcSDEAdapter.fetchSchema(layer, table, this.namespace);
                schemasCache.put(typeName, schema);
            }
        }

        return schema;
    }

    public void createSchema(SimpleFeatureType featureType, Map hints) throws IOException,
            IllegalArgumentException {
        final ArcSDEPooledConnection connection = connectionPool.getConnection();
        try {
            ArcSDEAdapter.createSchema(featureType, hints, connection);
        } finally {
            connection.close();
        }
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

        final ArcSDEPooledConnection conn = connectionPool.getConnection();

        try {
            final PlainSelect qualifiedSelect = SelectQualifier.qualify(conn, select);
            // System.out.println(qualifiedSelect);

            final SeQueryInfo queryInfo;
            try {
                LOGGER.fine("creating definition query info");
                queryInfo = QueryInfoParser.parse(conn, qualifiedSelect);
            } catch (SeException e) {
                throw new DataSourceException("Parsing select: " + e.getMessage(), e);
            }
            SimpleFeatureType viewSchema = ArcSDEAdapter.fetchSchema(conn, typeName, namespace,
                    queryInfo);
            LOGGER.fine("view schema: " + viewSchema);

            this.viewQueryInfos.put(typeName, queryInfo);
            this.viewSchemasCache.put(typeName, viewSchema);
            this.viewSelectStatements.put(typeName, qualifiedSelect);
        } finally {
            conn.close();
        }
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
}
