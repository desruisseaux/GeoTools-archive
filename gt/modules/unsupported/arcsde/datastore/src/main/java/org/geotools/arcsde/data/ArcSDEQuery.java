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
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.jsqlparser.statement.select.PlainSelect;

import org.geotools.arcsde.filter.FilterToSQLSDE;
import org.geotools.arcsde.filter.GeometryEncoderException;
import org.geotools.arcsde.filter.GeometryEncoderSDE;
import org.geotools.arcsde.pool.ArcSDEConnectionPool;
import org.geotools.arcsde.pool.ArcSDEPooledConnection;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataUtilities;
import org.geotools.data.Query;
import org.geotools.data.jdbc.FilterToSQLException;
import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureType;
import org.geotools.feature.SchemaException;
import org.geotools.filter.visitor.PostPreProcessFilterSplittingVisitor;
import org.opengis.filter.Filter;

import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeExtent;
import com.esri.sde.sdk.client.SeFilter;
import com.esri.sde.sdk.client.SeLayer;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeQueryInfo;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeSqlConstruct;
import com.esri.sde.sdk.client.SeTable;
import com.vividsolutions.jts.geom.Envelope;


/**
 * Wrapper class for SeQuery to hold a SeConnection until close() is called and
 * provide utility methods.
 *
 * @author Gabriel Roldan, Axios Engineering
 * @source $URL$
 * @version $Id$
 */
class ArcSDEQuery {
    /** Shared package's logger */
    private static final Logger LOGGER = Logger.getLogger(ArcSDEQuery.class.getPackage()
                                                                           .getName());

    /**
     * The connection to the ArcSDE server obtained when first created the
     * SeQuery in <code>getSeQuery</code>. It is retained until
     * <code>close()</code> is called. Do not use it directly, but through
     * <code>getConnection()</code>.
     * <p>
     * NOTE: this member is package visible only for unit test pourposes
     * </p>
     */
    ArcSDEPooledConnection connection = null;

    /**
     * The exact feature type this query is about to request from the arcsde
     * server. It could have less attributes than the ones of the actual table
     * schema, in which case only those attributes will be requested.
     */
    private FeatureType schema;

    /**
     * The query built using the constraints given by the geotools Query. It
     * must not be accessed directly, but through <code>getSeQuery()</code>,
     * since it is lazyly created
     */
    private SeQuery query;

    /**
     * Holds the geotools Filter that originated this query from which can
     * parse the sql where clause and the set of spatial filters for the
     * ArcSDE Java API
     */
    private ArcSDEQuery.FilterSet filters;

    /** The lazyly calculated result count */
    private int resultCount = -1;

    /** DOCUMENT ME!  */
    private FIDReader fidReader;
    
    private Object []previousRowValues;

    /**
     * Creates a new SDEQuery object.
     *
     * @param connection the connection attached to the life cycle of this query
     * @param schema the schema with all the attributes as expected.
     * @param filterSet DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     *
     * @see prepareQuery
     */
    private ArcSDEQuery(ArcSDEPooledConnection connection, FeatureType schema,
        FilterSet filterSet, FIDReader fidReader) throws DataSourceException {
        this.connection = connection;
        this.schema = schema;
        this.filters = filterSet;
        this.fidReader = fidReader;
    }

    /**
     * DOCUMENT ME!
     *
     * @param store DOCUMENT ME!
     * @param query DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public static ArcSDEQuery createQuery(ArcSDEDataStore store, Query query)
        throws IOException {
        String typeName = query.getTypeName();
        FeatureType schema = store.getSchema(typeName);
        return createQuery(store, schema, query);
    }

    /**
     * DOCUMENT ME!
     *
     * @param store DOCUMENT ME!
     * @param schema DOCUMENT ME!
     * @param query DOCUMENT ME!
     *
     * @return the newly created ArcSDEQuery or null if <code>Filter.EXCLUDE ==
     *         query.getFilter()</code>.
     *
     * @throws IOException see <i>throws DataSourceException</i> bellow.
     * @throws NullPointerException if some of the arguments is null.
     * @throws DataSourceException DOCUMENT ME!
     */
    public static ArcSDEQuery createQuery(ArcSDEDataStore store,
        FeatureType schema, Query query) throws IOException {
        if ((store == null) || (schema == null) || (query == null)) {
            throw new NullPointerException("store=" + store + ", schema="
                + schema + ", query=" + query);
        }

        Filter filter = query.getFilter();

        if (filter == Filter.EXCLUDE) {
            return null;
        }

        LOGGER.fine("Creating new ArcSDEQuery");

        final ArcSDEConnectionPool pool = store.getConnectionPool();

        final ArcSDEQuery sdeQuery;
        final String typeName = schema.getTypeName();
        final ArcSDEPooledConnection conn = pool.getConnection();
        final boolean isInprocessView = store.isView(typeName);
        final FIDReader fidReader;
        final SeLayer sdeLayer;
        final SeQueryInfo definitionQuery;

        if (isInprocessView) {
            fidReader = FIDReader.NULL_READER;
            definitionQuery = store.getViewQueryInfo(typeName);
            //the first table has to be the main layer
            String layerName;
            try{
                layerName = definitionQuery.getConstruct().getTables()[0];
                //@REVISIT: HACK HERE!, look how to get rid of alias in query info, or
                //better stop using queryinfo as definition query and use the PlainSelect,
                //then construct the query info dynamically when needed?
                if(layerName.indexOf(" AS") > 0){
                    layerName = layerName.substring(0, layerName.indexOf(" AS"));
                }
            }catch(SeException e){
                throw new DataSourceException("shouldn't happen: " + e.getMessage(), e);
            }
            sdeLayer = pool.getSdeLayer(conn, layerName);
        } else {
            definitionQuery = null;
            sdeLayer = pool.getSdeLayer(conn, typeName);
            fidReader = FIDReader.getFidReader(conn, sdeLayer);
        }

        //query can establish a subset of properties to retrieve, or do not
        //specify which properties.
        String[] queryProperies = query.getPropertyNames();

        //guess which properties needs actually be retrieved.
        List queryColumns = getQueryColumns(queryProperies, schema);

        /*Simple*/FeatureType querySchema = null;

        //TODO: create attributes with namespace when switching to GeoAPI FM
//        String ns = store.getNamespace() == null? null : store.getNamespace().toString();
//        AttributeName[] attNames = new AttributeName[queryColumns.size()];
//
//        for (int i = 0; i < queryColumns.size(); i++) {
//            String colName = (String) queryColumns.get(i);
//            attNames[i] = new org.geotools.util.AttributeName(ns, colName);
//        }

        String[] attNames = new String[queryColumns.size()];

        for (int i = 0; i < queryColumns.size(); i++) {
            String colName = (String) queryColumns.get(i);
            attNames[i] = colName;
        }

        try {
            //create the resulting feature type for the real attributes to retrieve
            querySchema = DataUtilities.createSubType(schema, attNames);
        } catch (SchemaException ex) {
            throw new DataSourceException(
                "Some requested attributes do not match the table schema: "
                + ex.getMessage(), ex);
        }

        //create the set of filters to work over
        ArcSDEQuery.FilterSet filterSet = createFilters(sdeLayer, querySchema, 
                filter, definitionQuery, store.getViewSelectStatement(typeName), fidReader);

        sdeQuery = new ArcSDEQuery(conn, querySchema, filterSet, fidReader);

        return sdeQuery;
    }

    /**
     * Returns the FID strategy used
     *
     * @return DOCUMENT ME!
     */
    public FIDReader getFidReader() {
        return this.fidReader;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param queryColumns DOCUMENT ME!
     * @param schema DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    private static List /*<String>*/ getQueryColumns(String[] queryColumns,
        FeatureType schema) throws DataSourceException {
        List columNames;

        if ((queryColumns == null) || (queryColumns.length == 0)) {
            List attNames = Arrays.asList(schema.getAttributeTypes());

            columNames = new ArrayList(attNames.size());

            for (Iterator it = attNames.iterator(); it.hasNext();) {
                AttributeType att = (AttributeType) it.next();
                String attName = att.getName();
                columNames.add(attName);
            }
        } else {
            columNames = Arrays.asList(queryColumns);
        }

        return columNames;
    }

    /**
     * DOCUMENT ME!
     *
     * @param store DOCUMENT ME!
     * @param typeName DOCUMENT ME!
     * @param filter DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws NoSuchElementException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     */
    public static ArcSDEQuery.FilterSet createFilters(SeLayer layer, FeatureType schema,
        Filter filter, SeQueryInfo qInfo, PlainSelect viewSelect, FIDReader fidReader)
        throws NoSuchElementException, IOException {
        
        ArcSDEQuery.FilterSet filters = new ArcSDEQuery.FilterSet(layer,
                filter, schema, qInfo, viewSelect, fidReader);

        return filters;
    }
    
    /**
     * Returns the stream used to fetch rows, creating it if it was not yet
     * created.
     *
     *
     * @throws SeException
     * @throws IOException
     */
    private SeQuery getSeQuery() throws SeException, IOException {
        if (this.query == null) {
            try {
                String[] propsToQuery = fidReader.getPropertiesToFetch(this.schema);
                this.query = createSeQueryForFetch(connection, propsToQuery);
            } catch (DataSourceException e) {
                throw e;
            } catch (IOException e) {
                throw e;
            } catch (SeException e) {
                e.printStackTrace();
                throw e;
            }
        }
        return this.query;
    }

    /**
     * creates an SeQuery with the filters provided to the constructor and
     * returns it.  Queries created with this method can be used to execute and
     * fetch results.  They cannot be used for other operations, such as
     * calculating layer extents, or result count.
     * <p> 
     * Difference with {@link #createSeQueryForFetch(ArcSDEPooledConnection, String[])}
     * is tha this function tells <code>SeQuery.setSpatialConstraints</code> to 
     * NOT return geometry based bitmasks, which are needed for calculating the
     * query extent and result count, but not for fetching SeRows.
     * </p>
     *
     * @param connection DOCUMENT ME!
     * @param propertyNames names of attributes to build the query for,
     *        respecting order
     *
     * @return DOCUMENT ME!
     *
     * @throws SeException if the ArcSDE Java API throws it while creating the
     *         SeQuery or setting it the spatial constraints.
     * @throws DataSourceException DOCUMENT ME!
     */
    private SeQuery createSeQueryForFetch(ArcSDEPooledConnection connection,
        String[] propertyNames)
        throws SeException, DataSourceException {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("constructing new sql query with connection: "
                + connection + ", propnames: "
                + java.util.Arrays.asList(propertyNames) + " sqlConstruct where clause: '"
                + this.filters.getSeSqlConstruct().getWhere() + "'");
        }

        SeQuery query = new SeQuery(connection);

        SeQueryInfo qInfo = filters.getQueryInfo(propertyNames);
        if(LOGGER.isLoggable(Level.FINER)){
            String msg = "ArcSDE query is: " + toString(qInfo);
            LOGGER.finer(msg);
        }
        try{
            query.prepareQueryInfo(qInfo);
        }catch(SeException e){
            // HACK: a DATABASE LEVEL ERROR (code -51) occurs when using
            // prepareQueryInfo but the geometry att is not required in the list
            // of properties to retrieve, and thus propertyNames contains
            // SHAPE.fid as a last resort to get a fid
            if (-51 == e.getSeError().getSdeError()) {
                query.close();
                query = new SeQuery(connection, propertyNames, filters.getSeSqlConstruct());
                query.prepareQuery();
            } else {
                throw e;
            }
        }

        SeFilter[] spatialConstraints = this.filters.getSpatialFilters();
        if (spatialConstraints.length > 0) {
            final boolean setReturnGeometryMasks = false;
            query.setSpatialConstraints(SeQuery.SE_OPTIMIZE,
                setReturnGeometryMasks, spatialConstraints);
        }

        return query;
    }
    
    private String toString(SeQueryInfo qInfo){
        StringBuffer sb = new StringBuffer("SeQueryInfo[\n\tcolumns=");
        try{
            SeSqlConstruct sql = qInfo.getConstruct();
            String [] tables = sql.getTables();
            String []cols = qInfo.getColumns();
            String by = null;
            try{
                by = qInfo.getByClause();
            }catch(NullPointerException npe){
                //no-op
            }
            String where =sql.getWhere();
            for(int i = 0; cols != null && i < cols.length; i++){
                sb.append(cols[i]);
                if(i < cols.length - 1)
                    sb.append(", ");
            }
            sb.append("\n\tTables=");
            for(int i = 0; i < tables.length; i++){
                sb.append(tables[i]);
                if(i < tables.length - 1)
                    sb.append(", ");
            }
            sb.append("\n\tWhere=");
            sb.append(where);
            sb.append("\n\tOrderBy=");
            sb.append(by);
        }catch(SeException e){
            sb.append("Exception retrieving query info properties: " + e.getMessage());
        }
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * creates an SeQuery with the filters provided to the constructor and
     * returns it.  Queries created with this method are to be used for
     * calculating layer extents and result counts.  These queries cannot
     * be executed or used to fetch results.
     * <p> 
     * Difference with {@link #createSeQueryForFetch(ArcSDEPooledConnection, String[])}
     * is tha this function tells <code>SeQuery.setSpatialConstraints</code> to 
     * return geometry based bitmasks, which are needed for calculating the
     * query extent and result count, but not for fetching SeRows.
     * </p>
     * 
     *
     * @param connection DOCUMENT ME!
     * @param propertyNames names of attributes to build the query for,
     *        respecting order
     *        
     * @return DOCUMENT ME!
     *
     * @throws SeException if the ArcSDE Java API throws it while creating the
     *         SeQuery or setting it the spatial constraints.
     * @throws DataSourceException DOCUMENT ME!
     */
    private SeQuery createSeQueryForQueryInfo(ArcSDEPooledConnection connection)
        throws SeException, DataSourceException {

        SeQuery query = new SeQuery(connection);

        SeFilter[] spatialConstraints = this.filters.getSpatialFilters();
        
        if (spatialConstraints.length > 0) {
            final boolean setReturnGeometryMasks = true;
            query.setSpatialConstraints(SeQuery.SE_OPTIMIZE,
                setReturnGeometryMasks, spatialConstraints);
        }

        return query;
    }

    /**
     * Returns the schema of the originating Query
     *
     * @return the schema of the originating Query
     */
    public FeatureType getSchema() {
        return this.schema;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public ArcSDEQuery.FilterSet getFilters() {
        return this.filters;
    }

    /**
     * Convenient method to just calculate the result count of a given query.
     *
     * @param ds
     * @param query
     *
     *
     * @throws IOException
     */
    public static int calculateResultCount(ArcSDEDataStore ds, Query query)
        throws IOException {
        ArcSDEQuery countQuery = createQuery(ds, query);
        int count;
        try{
            count = countQuery.calculateResultCount();
        }finally{
            countQuery.close();
        }
        return count;
    }

    /**
     * Convenient method to just calculate the resulting bound box of a given
     * query.
     *
     * @param ds DOCUMENT ME!
     * @param query DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public static Envelope calculateQueryExtent(ArcSDEDataStore ds, Query query)
        throws IOException {
        ArcSDEQuery boundsQuery = createQuery(ds, query);
        Envelope queryExtent;
        try{
            queryExtent = boundsQuery.calculateQueryExtent();
        }finally{
            boundsQuery.close();
        }
        return queryExtent;
    }

    /**
     * if the query has been parsed as just a where clause filter, or has no
     * filter at all, the result count calculation is optimized by selecting a
     * <code>count()</code> single row. If the filter involves any kind of
     * spatial filter, such as BBOX, the calculation can't be optimized by
     * this way, because the ArcSDE Java API throws a <code>"DATABASE LEVEL
     * ERROR OCURRED"</code> exception. So, in this case, a query over the
     * shape field is made and the result is traversed counting the number of
     * rows inside a while loop
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws DataSourceException DOCUMENT ME!
     */
    public int calculateResultCount() throws IOException {
        LOGGER.fine("about to calculate result count");

        if (this.resultCount == -1) {
            String aFieldName = "*";
            String[] columns = { aFieldName };

            SeQuery countQuery = null;

            try {
                countQuery = createSeQueryForQueryInfo(connection);
                SeQueryInfo qInfo = filters.getQueryInfo(columns);

                SeTable.SeTableStats tableStats = countQuery
                    .calculateTableStatistics(aFieldName,
                        SeTable.SeTableStats.SE_COUNT_STATS, qInfo, 0);

                this.resultCount = tableStats.getCount();
            } catch (SeException e) {
                LOGGER.severe("Error calculating result cout with SQL where clause: " + this.filters.getSeSqlConstruct().getWhere());
                throw new DataSourceException("Calculating result count: "
                    + e.getSeError().getErrDesc(), e);
            } finally {
                close(countQuery);
            }
        }

        return this.resultCount;
    }

    /**
     * Returns the envelope for all features within the layer that pass any SQL
     * construct, state, or spatial constraints for the stream.
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws DataSourceException DOCUMENT ME!
     */
    public Envelope calculateQueryExtent() throws IOException {
        Envelope envelope = null;
        SeQuery extentQuery = null;

        LOGGER.fine("Building a new SeQuery to consult it's resulting envelope");

        try {
            SeExtent extent = null;
            
            String[] spatialCol = { schema.getDefaultGeometry().getName() };

            extentQuery = createSeQueryForQueryInfo(connection);

            SeQueryInfo sdeQueryInfo = filters.getQueryInfo(spatialCol);

            extent = extentQuery.calculateLayerExtent(sdeQueryInfo);

            envelope = new Envelope(extent.getMinX(), extent.getMaxX(),
                    extent.getMinY(), extent.getMaxY());
            LOGGER.fine("got extent: " + extent + ", built envelope: "
                + envelope);
        } catch (SeException ex) {
            // //////////////////////
            SeSqlConstruct sqlCons = this.filters.getSeSqlConstruct();
            String sql = (sqlCons == null) ? null : sqlCons.getWhere();
            LOGGER.log(Level.SEVERE,
                "***********************\n" + ex.getSeError().getErrDesc()
                + "\nfilter: " + this.filters.getGeometryFilter() + "\nSQL: "
                + sql, ex);

            // ///////////////////////
            ex.printStackTrace();
        /*
         * temporary work around until we found the source of the problem
         * for which Brock is getting a DATABASE LEVEL ERROR OCCURED
            throw new DataSourceException("Can't consult the query extent: "
                + ex.getSeError().getErrDesc(), ex);
        */
        } finally {
            close(extentQuery);
        }

        return envelope;
    }

    /**
     * Silently closes this query.
     *
     * @param query
     */
    private void close(SeQuery query) {
        if (query == null) {
            return;
        }

        try {
            query.close();
        } catch (SeException e) {
            LOGGER.warning("Closing query: " + e.getSeError().getErrDesc());
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // //////////// RELEVANT METHODS WRAPPED FROM SeStreamOp ////////////////
    // //////////////////////////////////////////////////////////////////////

    /**
     * Closes the query and releases the holded connection back to the
     * connection pool. If reset is TRUE, the query status is set to INACTIVE;
     * also releases the SeConnection back to the SeConnectionPool
     */
    public void close() {
        close(this.query);
        this.query = null;    
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }

    /**
     * Tells the server to execute a stream operation.
     *
     * @throws IOException DOCUMENT ME!
     * @throws DataSourceException DOCUMENT ME!
     */
    public void execute() throws IOException {
        try {
            getSeQuery().execute();
        } catch (SeException e) {
            throw new DataSourceException(e.getSeError().getErrDesc(), e);
        }
    }

    /**
     * Flushes any outstanding insert/update buffers.
     *
     * @throws IOException DOCUMENT ME!
     * @throws DataSourceException DOCUMENT ME!
     */
    public void flushBufferedWrites() throws IOException {
        try {
            getSeQuery().flushBufferedWrites();
        } catch (SeException e) {
            throw new DataSourceException(e.getSeError().getErrDesc(), e);
        }
    }

    /**
     * Cancels the current operation on the stream. If <code>reset</code> is
     * TRUE, the query status is set to INACTIVE. If reset is FALSE the query
     * status is set to CLOSED.
     *
     * @param reset if true the Query is closed, else it is resetted to be
     *        reused
     *
     * @throws IOException DOCUMENT ME!
     * @throws DataSourceException DOCUMENT ME!
     */
    public void cancel(boolean reset) throws IOException {
        try {
            getSeQuery().cancel(reset);
        } catch (SeException e) {
            throw new DataSourceException(e.getSeError().getErrDesc(), e);
        }
    }

    /**
     * Sets state constraints for input and output stream operations. If a
     * differenct type is specified, then only features different in the way
     * supplied are returned.
     * 
     * <p>
     * differencesType:
     * 
     * <ul>
     * <li>
     * SeState.SE_STATE_DIFF_NOCHECK Returns all features in the source state.
     * It doesn't check the differences between source state and differences
     * state.
     * </li>
     * <li>
     * SeState.SE_STATE_DIFF_NOCHANGE_UPDATE Returns all features that haven't
     * changed in the source state, but have been updated in the differences
     * state.
     * </li>
     * <li>
     * SeState.SE_STATE_DIFF_NOCHANGE_DELETE Returns all features that haven't
     * changed in the source state, but have been deleted in the differences
     * state.
     * </li>
     * <li>
     * SeState.SE_STATE_DIFF_UPDATE_NOCHANGE Returns all features that have
     * been updated in the source state, but unchanged in the differences
     * state.
     * </li>
     * <li>
     * SeState.SE_STATE_DIFF_UPDATE_UPDATE Returns all features that have been
     * updated in both the source and difference states.
     * </li>
     * <li>
     * SeState.SE_STATE_DIFF_UPDATE_DELETE Returns all features that have been
     * updated in the source state but deleted in the difference states.
     * </li>
     * <li>
     * SeState.SE_STATE_DIFF_INSERT Returns all features that were inserted
     * into the source state and that never existed in the differences state.
     * </li>
     * </ul>
     * </p>
     *
     * @param lockActions The id of the state to direct input into and take
     *        output from
     *
     * @throws IOException DOCUMENT ME!
     * @throws DataSourceException DOCUMENT ME!
     */

    /*
     * public void setState(SeObjectId sourceId, SeObjectId differencesId, int
     * differencesType) throws SeException { getSeQuery().setState(sourceId,
     * differencesId, differencesType); }
     */

    /**
     * Sets the row locking environment for a stream.
     * 
     * <p>
     * The row locking environment remains in effect until the stream is closed
     * with reset TRUE or the stream is freed. The row lock types are:
     * 
     * <ul>
     * <li>
     * SE_ROWLOCKING_LOCK_ON_QUERY - Rows selected by a query are locked.
     * </li>
     * <li>
     * SE_ROWLOCKING_LOCK_ON_INSERT - New rows are locked when inserted.
     * </li>
     * <li>
     * SE_ROWLOCKING_LOCK_ON_UPDATE - Updated rows are locked.
     * </li>
     * <li>
     * SE_ROWLOCKING_UNLOCK_ON_QUERY - Locks are removed upon query.
     * </li>
     * <li>
     * SE_ROWLOCKING_UNLOCK_ON_UPDATE - Modified rows are unlocked.
     * </li>
     * <li>
     * SE_ROWLOCKING_FILTER_MY_LOCKS - Only rows locked by the user are
     * returned on query.
     * </li>
     * <li>
     * SE_ROWLOCKING_FILTER_OTHER_LOCKS - Only rows locked by other users are
     * returned on query.
     * </li>
     * <li>
     * SE_ROWLOCKING_FILTER_UNLOCKED - Only unlocked rows are returned.
     * </li>
     * <li>
     * SE_ROWLOCKING_LOCK_ONLY - Query operations lock but don't return rows.
     * </li>
     * </ul>
     * </p>
     *
     * @param lockActions DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws DataSourceException DOCUMENT ME!
     */
    public void setRowLocking(int lockActions) throws IOException {
        try {
            getSeQuery().setRowLocking(lockActions);
        } catch (SeException e) {
            throw new DataSourceException(e.getSeError().getErrDesc(), e);
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // /////////////// METHODS WRAPPED FROM SeQuery /////////////////////
    // //////////////////////////////////////////////////////////////////////

    /**
     * Initializes a stream with a query using a selected set of columns and an
     * SeSqlConstruct object for the where clause. The where clause can?t
     * contain any ORDER BY or GROUP BY clauses.
     *
     * @throws IOException DOCUMENT ME!
     * @throws DataSourceException DOCUMENT ME!
     */
    public void prepareQuery() throws IOException {
        try {
            getSeQuery().prepareQuery();
        } catch (SeException e) {
            throw new DataSourceException(e.getSeError().getErrDesc(), e);
        }
    }

    /**
     * Fetches an SeRow of data.
     * 
     * @return DOCUMENT ME!
     * 
     * @throws IOException
     *             (DataSourceException) if the fetching fails
     * @throws IllegalStateException
     *             if the query was already closed or {@link #execute()} hastn't
     *             been called yet
     */
    public SdeRow fetch() throws IOException, IllegalStateException {
        if (this.query == null) {
            throw new IllegalStateException("query closed or not yet executed");
        }

        try {
            SeQuery seQuery = getSeQuery();
            SeRow row = seQuery.fetch();
            SdeRow currentRow = (row == null) ? null : new SdeRow(row, previousRowValues);
            previousRowValues = currentRow == null? null : currentRow.getAll();
            return currentRow;
        } catch (SeException e) {
            close();
            throw new DataSourceException(e.getSeError().getErrDesc(), e);
        }catch(Exception e){
            close();
            LOGGER.log(Level.SEVERE, "fetching row: " + e.getMessage(), e);
            throw new DataSourceException("fetching row: " + e.getMessage(), e);
        }
    }

    /**
     * Sets the spatial filters on the query using SE_OPTIMIZE as the policy
     * for spatial index search
     *
     * @param filters a set of spatial constraints to filter upon
     *
     * @throws IOException DOCUMENT ME!
     * @throws DataSourceException DOCUMENT ME!
     */
    public void setSpatialConstraints(SeFilter[] filters)
        throws IOException {
        try {
            getSeQuery().setSpatialConstraints(SeQuery.SE_OPTIMIZE, false,
                filters);
        } catch (SeException e) {
            throw new DataSourceException(e.getSeError().getErrDesc(), e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String toString() {
        return "Schema: " + this.schema.getTypeName() + ", query: "
        + this.query;
    }

    /**
     * DOCUMENT ME!
     *
     * @author $author$
     * @version $Revision: 1.9 $
     */
    public static class FilterSet {
        /** DOCUMENT ME! */
        private SeQueryInfo definitionQuery;
        
        private PlainSelect layerSelectStatement;

        private FIDReader fidReader;

        /** DOCUMENT ME! */
        private final SeLayer sdeLayer;

        /** DOCUMENT ME! */
        private final Filter sourceFilter;

        /** DOCUMENT ME! */
        private Filter sqlFilter;

        /** DOCUMENT ME! */
        private Filter geometryFilter;

        /** DOCUMENT ME! */
        private Filter unsupportedFilter;

        private FilterToSQLSDE sqlEncoder;
        
        /**
         * Holds the ArcSDE Java API definition of the geometry related filters
         * this datastore implementation supports natively.
         */
        private SeFilter[] sdeSpatialFilters;

        /**
         * Holds the ArcSDE Java API definition of the <strong>non</strong>
         * geometry related filters this datastore implementation supports
         * natively.
         */
        private SeSqlConstruct sdeSqlConstruct;
        
        private FeatureType featureType;

        /**
         * Creates a new FilterSet object.
         *
         * @param sdeLayer DOCUMENT ME!
         * @param sourceFilter DOCUMENT ME!
         */
        public FilterSet(SeLayer sdeLayer, Filter sourceFilter, FeatureType ft,
                SeQueryInfo definitionQuery, PlainSelect layerSelectStatement, FIDReader fidReader) {
            assert sdeLayer != null;
            assert sourceFilter != null;
            assert ft != null;
            
            this.sdeLayer = sdeLayer;
            this.sourceFilter = sourceFilter;
            this.featureType = ft;
            this.definitionQuery = definitionQuery;
            this.layerSelectStatement = layerSelectStatement;
            this.fidReader = fidReader;
            createGeotoolsFilters();
        }

        /**
         * Given the <code>Filter</code> passed to the constructor, unpacks it
         * to three different filters, one for the supported SQL based filter,
         * another for the supported Geometry based filter, and the last one
         * for the unsupported filter. All of them can be retrieved from its
         * corresponding getter.
         */
        private void createGeotoolsFilters() {
            FilterToSQLSDE sqlEncoder = getSqlEncoder();
            
            PostPreProcessFilterSplittingVisitor unpacker = new PostPreProcessFilterSplittingVisitor(sqlEncoder.getCapabilities(), featureType, null);
            sourceFilter.accept(unpacker, null);

            this.sqlFilter = unpacker.getFilterPre();
            
            if (LOGGER.isLoggable(Level.FINE) && sqlFilter != null)
                LOGGER.fine("SQL portion of SDE Query: '" + sqlFilter + "'");

            Filter remainingFilter = unpacker.getFilterPost();

            unpacker = new PostPreProcessFilterSplittingVisitor(GeometryEncoderSDE.getCapabilities(), featureType, null);
            remainingFilter.accept(unpacker, null);

            this.geometryFilter = unpacker.getFilterPre();
            if (LOGGER.isLoggable(Level.FINE) && geometryFilter != null)
                LOGGER.fine("Spatial-Filter portion of SDE Query: '" + geometryFilter + "'");
            
            this.unsupportedFilter = unpacker.getFilterPost();
            if (LOGGER.isLoggable(Level.FINE) && unsupportedFilter != null)
                LOGGER.fine("Unsupported (and therefore ignored) portion of SDE Query: '" + unsupportedFilter + "'");
        }

        /**
         * Returns an SeQueryInfo that can be used to retrieve a set of SeRows from
         * an ArcSDE layer or a layer with joins. The SeQueryInfo object lacks the set
         * of column names to fetch. It is the responsibility of the calling code to
         * call setColumns(String []) on the returned object to specify which properties
         * to fetch.
         * 
         * @param unqualifiedPropertyNames
         * @return
         * @throws SeException
         * @throws DataSourceException
         */
        public SeQueryInfo getQueryInfo(String []unqualifiedPropertyNames)throws SeException, DataSourceException{
            String []tables;
            String byClause = null;
            
            final SeSqlConstruct plainSqlConstruct = getSeSqlConstruct();
            
            String where = plainSqlConstruct.getWhere();
            
            if(definitionQuery == null){
                tables = new String[]{this.sdeLayer.getQualifiedName()};
            }else{
                tables = definitionQuery.getConstruct().getTables();
                String joinWhere = definitionQuery.getConstruct().getWhere();
                if(where == null){
                    where = joinWhere;
                }else{
                    where = joinWhere == null? where : (joinWhere + " AND " + where);
                }
                try{
                    byClause = definitionQuery.getByClause();
                }catch(NullPointerException e){
                    //no-op
                }
            }
            
            final SeQueryInfo qInfo = new SeQueryInfo();
            final SeSqlConstruct sqlConstruct = new SeSqlConstruct();
            sqlConstruct.setTables(tables);
            if(where != null && where.length() > 0){
                sqlConstruct.setWhere(where);
            }
            
            final int queriedAttCount = unqualifiedPropertyNames == null ? 0
                    : unqualifiedPropertyNames.length;
            
            if(queriedAttCount > 0){
                String []sdeAttNames = new String[queriedAttCount];
                FilterToSQLSDE sqlEncoder = getSqlEncoder();
                
                for(int i = 0; i < queriedAttCount; i++){
                    String attName = unqualifiedPropertyNames[i];
                    String coldef = sqlEncoder.getColumnDefinition(attName);
                    sdeAttNames[i] = coldef;
                }
                qInfo.setColumns(sdeAttNames);
            }
            
            qInfo.setConstruct(sqlConstruct);
            if(byClause != null){
                qInfo.setByClause(byClause);
            }
            return qInfo;
        }        
        
        /**
         * DOCUMENT ME!
         *
         * @return the SeSqlConstruct corresponding to the given SeLayer and
         *         SQL based filter. Should never return null.
         *
         * @throws DataSourceException if an error occurs encoding the sql
         *         filter to a SQL where clause, or creating the
         *         SeSqlConstruct for the given layer and where clause.
         */
        public SeSqlConstruct getSeSqlConstruct() throws DataSourceException {
            if (this.sdeSqlConstruct == null) {
                final String layerName;
                try {
                    layerName = this.sdeLayer.getQualifiedName();
                    this.sdeSqlConstruct = new SeSqlConstruct(layerName);
                } catch (SeException e) {
                    throw new DataSourceException(
                        "Can't create SQL construct: "
                        + e.getSeError().getErrDesc(), e);
                }

                Filter sqlFilter = getSqlFilter();

                if (!Filter.INCLUDE.equals(sqlFilter)) {
                    String whereClause = null;
                    FilterToSQLSDE sqlEncoder = getSqlEncoder();

                    try {
                        whereClause = sqlEncoder.encodeToString(sqlFilter);
                    } catch (FilterToSQLException sqle) {
                        String message = "Geometry encoder error: "
                            + sqle.getMessage();
                        throw new DataSourceException(message, sqle);
                    }
                    LOGGER.fine("ArcSDE where clause '" + whereClause + "'");

                    this.sdeSqlConstruct.setWhere(whereClause);
                }
            }

            return this.sdeSqlConstruct;
        }

        /**
         * Lazily creates the array of <code>SeShapeFilter</code> objects that
         * map the corresponding geometry related filters included in the
         * original  <code>org.geotools.data.Query</code> passed to the
         * constructor.
         *
         * @return an array with the spatial filters to be applied to the
         *         SeQuery, or null if none.
         *
         * @throws DataSourceException DOCUMENT ME!
         */
        public SeFilter[] getSpatialFilters() throws DataSourceException {
            if (this.sdeSpatialFilters == null) {
                GeometryEncoderSDE geometryEncoder = new GeometryEncoderSDE(this.sdeLayer);

                try {
                    geometryEncoder.encode(getGeometryFilter());
                } catch (GeometryEncoderException e) {
                    throw new DataSourceException(
                        "Error parsing geometry filters: " + e.getMessage(), e);
                }

                this.sdeSpatialFilters = geometryEncoder.getSpatialFilters();
            }

            return this.sdeSpatialFilters;
        }

        /**
         * DOCUMENT ME!
         *
         * @return the subset, non geometry related, of the original filter
         *         this datastore implementation supports natively, or
         *         <code>Filter.INCLUDE</code> if the original Query does not
         *         contains non spatial filters that we can deal with at the
         *         ArcSDE Java API side.
         */
        public Filter getSqlFilter() {
            return (this.sqlFilter == null) ? Filter.INCLUDE : this.sqlFilter;
        }

        /**
         * DOCUMENT ME!
         *
         * @return the geometry related subset of the original filter this
         *         datastore implementation supports natively, or
         *         <code>Filter.INCLUDE</code> if the original Query does not
         *         contains spatial filters that we can deal with at the
         *         ArcSDE Java API side.
         */
        public Filter getGeometryFilter() {
            return (this.geometryFilter == null) ? Filter.INCLUDE
                                                 : this.geometryFilter;
        }

        /**
         * DOCUMENT ME!
         *
         * @return the part of the original filter this datastore
         *         implementation does not supports natively, or
         *         <code>Filter.INCLUDE</code> if we support the whole Query
         *         filter.
         */
        public Filter getUnsupportedFilter() {
            return (this.unsupportedFilter == null) ? Filter.INCLUDE
                                                    : this.unsupportedFilter;
        }
        
        private FilterToSQLSDE getSqlEncoder(){
            if (sqlEncoder == null) {
                final String layerName;
                try {
                    layerName = sdeLayer.getQualifiedName();
                } catch (SeException e) {
                    throw (RuntimeException) new RuntimeException(
                            "error getting layer's qualified name").initCause(e);
                }
                String fidColumn = fidReader.getFidColumn();
                sqlEncoder = new FilterToSQLSDE(layerName, fidColumn, featureType,
                        layerSelectStatement);
            }
            return sqlEncoder;
        }   
    }
}
