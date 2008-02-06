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

import org.geotools.arcsde.ArcSdeException;
import org.geotools.arcsde.data.view.QueryInfoParser;
import org.geotools.arcsde.data.view.SelectQualifier;
import org.geotools.arcsde.pool.ArcSDEConnectionPool;
import org.geotools.arcsde.pool.ArcSDEPooledConnection;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultQuery;
import org.geotools.data.DefaultServiceInfo;
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
import org.geotools.data.ServiceInfo;
import org.geotools.data.Transaction;
import org.geotools.data.view.DefaultView;
import org.geotools.feature.FeatureTypes;
import org.geotools.feature.SchemaException;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.filter.Filter;

import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeLayer;
import com.esri.sde.sdk.client.SeQueryInfo;

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
    final FeatureListenerManager listenerManager = new FeatureListenerManager();

    private final ArcSDEConnectionPool connectionPool;

    /**
     * ArcSDE registered layers definitions
     */
    private final Map<String, FeatureTypeInfo> featureTypeInfos;

    /**
     * In process view definitions. This map is populated through
     * {@link #registerView(String, PlainSelect)}
     */
    private final Map<String, FeatureTypeInfo> inProcessFeatureTypeInfos;

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
        this.featureTypeInfos = new HashMap<String, FeatureTypeInfo>();
        this.inProcessFeatureTypeInfos = new HashMap<String, FeatureTypeInfo>();
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
     * @see DataStore#getSchema(String)
     */
    public synchronized SimpleFeatureType getSchema(final String typeName)
            throws java.io.IOException {
        FeatureTypeInfo typeInfo = getFeatureTypeInfo(typeName);
        SimpleFeatureType schema = typeInfo.getFeatureType();
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
        layerNames.addAll(inProcessFeatureTypeInfos.keySet());
        return layerNames.toArray(new String[layerNames.size()]);
    }

    public ServiceInfo getInfo() {
        DefaultServiceInfo info = new DefaultServiceInfo();
        info.setDescription("Features from ArcSDE" );
        info.setSchema( FeatureTypes.DEFAULT_NAMESPACE );        
        return info;
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
            if (Transaction.AUTO_COMMIT.equals(transaction)) {
                connection = connectionPool.getConnection();
            } else {
                ArcTransactionState state = ArcTransactionState.getState(transaction,
                        connectionPool, listenerManager);
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
     * fetch the differences (additions/modifications/deletions) made while a
     * transaction is in progress.
     * </p>
     * 
     * @param query
     *            the Query containing the request criteria
     * @param connection
     *            the connection to use to retrieve content. It'll be closed by
     *            the returned FeatureReader only if the connection does not has
     *            a
     *            {@link ArcSDEPooledConnection#isTransactionActive() transaction in progress}.
     * @param readerClosesConnection
     *            flag indicating whether the reader should auto-close the
     *            connection when exhausted/closed. <code>false</code>
     *            indicates never close it as its being used as the streamed
     *            content of a feature writer.
     * @return
     * @throws IOException
     */
    FeatureReader getFeatureReader(final Query query, final ArcSDEPooledConnection connection,
            final boolean readerClosesConnection) throws IOException {
        final String typeName = query.getTypeName();
        final String propertyNames[] = query.getPropertyNames();

        final FeatureTypeInfo typeInfo = getFeatureTypeInfo(typeName, connection);
        final SimpleFeatureType completeSchema = typeInfo.getFeatureType();
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

        if (typeInfo.isInProcessView()) {
            SeQueryInfo definitionQuery = typeInfo.getSdeDefinitionQuery();
            PlainSelect viewSelectStatement = typeInfo.getDefinitionQuery();
            sdeQuery = ArcSDEQuery.createInprocessViewQuery(connection, completeSchema, query,
                    definitionQuery, viewSelectStatement);
        } else {
            final FIDReader fidStrategy = typeInfo.getFidStrategy();
            sdeQuery = ArcSDEQuery.createQuery(connection, completeSchema, query, fidStrategy);
        }

        ///sdeQuery.execute();

        // this is the one which's gonna close the connection when done
        final ArcSDEAttributeReader attReader;
        attReader = new ArcSDEAttributeReader(sdeQuery, connection, readerClosesConnection);
        FeatureReader reader;
        try {
            reader = new ArcSDEFeatureReader(attReader);
        } catch (SchemaException e) {
            throw new RuntimeException("Schema missmatch, should never happen!: " + e.getMessage(),
                    e);
        }

        filter = getUnsupportedFilter(typeInfo, filter, connection);
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
        final FeatureTypeInfo typeInfo = getFeatureTypeInfo(typeName);

        FeatureSource fsource;
        if (typeInfo.isWritable()) {
            fsource = new ArcSdeFeatureStore(typeInfo, this);
        } else {
            fsource = new ArcSdeFeatureSource(typeInfo, this);
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
        // get the connection the streamed writer content has to work over
        // so the reader and writer share it
        final ArcSDEPooledConnection connection;
        final ArcTransactionState state;
        {
            if (Transaction.AUTO_COMMIT.equals(transaction)) {
                connection = connectionPool.getConnection();
                state = null;
            } else {
                state = ArcTransactionState.getState(transaction, connectionPool, listenerManager);
                connection = state.getConnection();
            }
        }

        try {
            final FeatureTypeInfo typeInfo = getFeatureTypeInfo(typeName, connection);
            if (!typeInfo.isWritable()) {
                throw new DataSourceException(typeName + " is not writable");
            }
            final SimpleFeatureType featureType = typeInfo.getFeatureType();

            final DefaultQuery query = new DefaultQuery(typeName, filter);
            // don't let the reader close the connection as the writer needs it
            final boolean closeConnection = false;
            final FeatureReader reader = getFeatureReader(query, connection, closeConnection);

            FeatureWriter writer;

            final FIDReader fidReader = typeInfo.getFidStrategy();

            final FeatureListenerManager listenerManager = this.listenerManager;
            if (Transaction.AUTO_COMMIT == transaction) {
                writer = new AutoCommitFeatureWriter(fidReader, featureType, reader, connection, listenerManager);
            } else {
                // if there's a transaction, the reader and the writer will
                // share the connection held in the transaction state
                writer = new TransactionFeatureWriter(fidReader, featureType, reader, state, listenerManager);
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
     */
    private org.opengis.filter.Filter getUnsupportedFilter(final FeatureTypeInfo typeInfo,
            final Filter filter, final ArcSDEPooledConnection conn) {
        try {
            SeLayer layer;
            SeQueryInfo qInfo;

            if (typeInfo.isInProcessView()) {
                qInfo = typeInfo.getSdeDefinitionQuery();
                String mainLayerName;
                try {
                    mainLayerName = qInfo.getConstruct().getTables()[0];
                } catch (SeException e) {
                    throw new ArcSdeException(e);
                }
                layer = conn.getLayer(mainLayerName);
            } else {
                layer = conn.getLayer(typeInfo.getFeatureTypeName());
                qInfo = null;
            }

            FIDReader fidReader = typeInfo.getFidStrategy();

            SimpleFeatureType schema = typeInfo.getFeatureType();
            PlainSelect viewSelectStatement = typeInfo.getDefinitionQuery();

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
    ArcSDEConnectionPool getConnectionPool() {
        return this.connectionPool;
    }

    FeatureTypeInfo getFeatureTypeInfo(final String typeName) throws java.io.IOException {
        assert typeName != null;

        FeatureTypeInfo typeInfo = inProcessFeatureTypeInfos.get(typeName);
        if (typeInfo != null) {
            return typeInfo;
        }
        typeInfo = featureTypeInfos.get(typeName);
        if (typeInfo == null) {
            // connection used to retrieve the user name if a non qualified type
            // name was passed in
            final ArcSDEPooledConnection conn = getConnectionPool().getConnection();
            try {
                typeInfo = getFeatureTypeInfo(typeName, conn);
            } finally {
                conn.close();
            }
        }
        return typeInfo;
    }

    synchronized FeatureTypeInfo getFeatureTypeInfo(final String typeName,
            final ArcSDEPooledConnection connection) throws IOException {

        FeatureTypeInfo ftInfo = inProcessFeatureTypeInfos.get(typeName);

        if (ftInfo == null) {
            synchronized (featureTypeInfos) {
                ftInfo = featureTypeInfos.get(typeName);
                if (ftInfo == null) {
                    ftInfo = ArcSDEAdapter.fetchSchema(typeName, this.namespace, connection);
                    featureTypeInfos.put(typeName, ftInfo);
                }
            }
        }
        return ftInfo;
    }

    /**
     * Creates a given FeatureType on the ArcSDE instance this DataStore is
     * running over.
     * <p>
     * This deviation from the {@link DataStore#createSchema(SimpleFeatureType)}
     * API is to allow the specification of ArcSDE specific hints for the
     * "Feature Class" to create:
     * <ul>
     * At this time the following hints may be passed:
     * <li><b>configuration.keywords</b>: database configuration keyword to
     * use for the newly create feature type. In not present,
     * <code>"DEFAULTS"</code> will be used.</li>
     * <li><b>rowid.column.name</b>: indicates the name of the table column to
     * set up as the unique identifier, and thus to be used as feature id.</li>
     * <li><b>rowid.column.type</b>: The row id column type. Must be one of
     * the following allowed values: <code>"NONE"</code>, <code>"USER"</code>,
     * <code>"SDE"</code> in order to set up the row id column name to not be
     * managed at all, to be user managed or to be managed by ArcSDE,
     * respectively. Refer to the ArcSDE documentation for an explanation of the
     * meanings of those terms.</li>
     * </ul>
     * </p>
     * 
     * @param featureType
     * @param hints
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public void createSchema(final SimpleFeatureType featureType, final Map<String, String> hints)
            throws IOException, IllegalArgumentException {
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
    void registerView(final String typeName, final PlainSelect select) throws IOException {
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
                throw new ArcSdeException("Error Parsing select: " + qualifiedSelect, e);
            }
            FeatureTypeInfo typeInfo = ArcSDEAdapter.fetchSchema(conn, typeName, namespace,
                    qualifiedSelect, queryInfo);

            inProcessFeatureTypeInfos.put(typeName, typeInfo);
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
