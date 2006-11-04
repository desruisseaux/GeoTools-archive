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
package org.geotools.data.arcsde;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.DataSourceException;
import org.geotools.data.DataUtilities;
import org.geotools.data.Query;
import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureType;
import org.geotools.feature.SchemaException;
import org.opengis.filter.Filter;
import org.geotools.filter.GeometryEncoderException;
import org.geotools.filter.GeometryEncoderSDE;
import org.geotools.filter.SQLEncoderException;
import org.geotools.filter.SQLEncoderSDE;
import org.geotools.filter.SQLUnpacker;

import com.esri.sde.sdk.client.SeColumnDefinition;
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
     * the pool from where to take a connection to pass to the new stream (the
     * stream is the SeQuery object, not the connection itself)
     * <p>
     * NOTE: this member is package visible only for unit test pourposes
     * </p>
     */
    ArcSDEConnectionPool connectionPool;

    /**
     * The connection to the ArcSDE server obtained when first created the
     * SeQuery in <code>getSeQuery</code>. It is retained until
     * <code>close()</code> is called. Do not use it directly, but through
     * <code>getConnection()</code>.
     * <p>
     * NOTE: this member is package visible only for unit test pourposes
     * </p>
     */
    PooledConnection connection = null;

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

    /** The lazyly calculated result bounds */
    private Envelope resultEnvelope;

    /**
     * Creates a new SDEQuery object.
     *
     * @param pool DOCUMENT ME!
     * @param schema the schema with all the attributes as expected.
     * @param filterSet DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     *
     * @see prepareQuery
     * @deprecated will be removed as soon as only the factory method is used
     */
    private ArcSDEQuery(ArcSDEConnectionPool pool, FeatureType schema,
        FilterSet filterSet) throws DataSourceException {
        this.schema = schema;
        this.connectionPool = pool;
        this.filters = filterSet;
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
        return createQuery(store, store.getSchema(query.getTypeName()), query);
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

        ArcSDEQuery sdeQuery = null;
        String typeName = schema.getTypeName();

        ArcSDEConnectionPool pool = store.getConnectionPool();

        //query can establish a subset of properties to retrieve, or do not
        //specify which properties.
        String[] queryColumns = query.getPropertyNames();

        //guess which properties needs actually be retrieved.
        queryColumns = getQueryColumns(pool, typeName, queryColumns, schema);

        FeatureType querySchema = null;

        try {
            //create the resulting feature type for the real attributes to retrieve
            querySchema = DataUtilities.createSubType(schema, queryColumns);
        } catch (SchemaException ex) {
            throw new DataSourceException(
                "Some requested attributes do not match the table schema: "
                + ex.getMessage(), ex);
        }

        //create the set of filters to work over
        ArcSDEQuery.FilterSet filterSet = createFilters(store, typeName, filter);

        sdeQuery = new ArcSDEQuery(pool, querySchema, filterSet);

        return sdeQuery;
    }

    /**
     * DOCUMENT ME!
     *
     * @param pool DOCUMENT ME!
     * @param typeName DOCUMENT ME!
     * @param queryColumns DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    private static String[] getQueryColumns(ArcSDEConnectionPool pool,
        String typeName, String[] queryColumns, FeatureType schema) throws DataSourceException {
        if ((queryColumns == null) || (queryColumns.length == 0)) {
            SeTable table = pool.getSdeTable(typeName);
            SeColumnDefinition[] sdeCols = null;

            try {
                sdeCols = table.describe();
            } catch (SeException ex) {
                throw new DataSourceException(ex.getMessage(), ex);
            }

            queryColumns = new String[sdeCols.length];

            for (int i = 0; i < sdeCols.length; i++) {
                queryColumns[i] = sdeCols[i].getName();
            }
        }
        
        boolean hasFIDColumn = false;
        for (int i = 0; i < queryColumns.length; i++) {
        	AttributeType type = schema.getAttributeType(schema.find(queryColumns[i]));
        	if (type instanceof ArcSDEAttributeType) {
        		if  (((ArcSDEAttributeType)type).isFeatureIDAttribute()) {
        			hasFIDColumn = true;
        			break;
        		}
        	}
        }
        
        if (!hasFIDColumn) {
        	LOGGER.warning("No FID attribute was contained in your query.  Appending the discovered one to the list of columns to be fetched.");
        	for (int i = 0; i < schema.getAttributeCount(); i++) {
        		AttributeType type = schema.getAttributeType(i);
            	if (type instanceof ArcSDEAttributeType) {
            		if  (((ArcSDEAttributeType)type).isFeatureIDAttribute()) {
            			String[] newQCols = new String[queryColumns.length + 1];
            			System.arraycopy(queryColumns, 0, newQCols, 0, queryColumns.length);
            			newQCols[queryColumns.length] = type.getName();
            			LOGGER.warning("Appendend " + newQCols[queryColumns.length] + " to column list.");
                    	queryColumns = newQCols;
            			break;
            		}
            	}
        	}
        	
        }

        return queryColumns;
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
    public static ArcSDEQuery.FilterSet createFilters(ArcSDEDataStore store,
        String typeName, Filter filter)
        throws NoSuchElementException, IOException {
        SeLayer sdeLayer = store.getConnectionPool().getSdeLayer(typeName);
        ArcSDEQuery.FilterSet filters = new ArcSDEQuery.FilterSet(sdeLayer,
                filter);

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
            PooledConnection conn = getConnection();
            try {
				String[] propsToQuery = getPropertiesToFetch();
				this.query = createSeQueryForFetch(conn, propsToQuery, true);
			} catch (DataSourceException e) {
				throw e;
			} catch (IOException e) {
				throw e;
			} catch (SeException e) {
				throw e;
			}finally{
				//as for GEOT-765, we no longer release the
				//connection here
				//releaseConnection();
			}
        }

        return this.query;
    }

    /**
     * creates an SeQuery with the filters provided to the constructor and
     * returns it.  Queries created with this method can be used to execute and
     * fetch results.  They cannot be used for other operations, such as
     * calculating layer extents, or result count.
     * 
     *
     * @param connection DOCUMENT ME!
     * @param propertyNames names of attributes to build the query for,
     *        respecting order
     * @param setReturnGeometryMasks tells
     *        <code>SeQuery.setSpatialConstraints</code> wether to return
     *        geometry based bitmasks, which are needed for calculating the
     *        query extent and result count, but not for fetching SeRows
     *
     * @return DOCUMENT ME!
     *
     * @throws SeException if the ArcSDE Java API throws it while creating the
     *         SeQuery or setting it the spatial constraints.
     * @throws DataSourceException DOCUMENT ME!
     */
    private SeQuery createSeQueryForFetch(PooledConnection connection,
        String[] propertyNames, boolean setReturnGeometryMasks)
        throws SeException, DataSourceException {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("constructing new sql query with connection: "
                + connection + ", propnames: "
                + java.util.Arrays.asList(propertyNames) + " sqlConstruct: "
                + this.filters.getSeSqlConstruct());
        }

        SeQuery query = new SeQuery(connection, propertyNames,
                this.filters.getSeSqlConstruct());
        SeFilter[] spatialConstraints = this.filters.getSpatialFilters();

        query.prepareQuery();
        
        if (spatialConstraints.length > 0) {
            query.setSpatialConstraints(SeQuery.SE_OPTIMIZE,
                setReturnGeometryMasks, spatialConstraints);
        }

        return query;
    }
    
    /**
     * creates an SeQuery with the filters provided to the constructor and
     * returns it.  Queries created with this method are to be used for
     * calculating layer extents and result counts.  These queries cannot
     * be executed or used to fetch results.
     * 
     *
     * @param connection DOCUMENT ME!
     * @param propertyNames names of attributes to build the query for,
     *        respecting order
     * @param setReturnGeometryMasks tells
     *        <code>SeQuery.setSpatialConstraints</code> wether to return
     *        geometry based bitmasks, which are needed for calculating the
     *        query extent and result count, but not for fetching SeRows
     *
     * @return DOCUMENT ME!
     *
     * @throws SeException if the ArcSDE Java API throws it while creating the
     *         SeQuery or setting it the spatial constraints.
     * @throws DataSourceException DOCUMENT ME!
     */
    private SeQuery createSeQueryForQueryInfo(PooledConnection connection,
        String[] propertyNames, boolean setReturnGeometryMasks)
        throws SeException, DataSourceException {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("constructing new sql query with connection: "
                + connection + ", propnames: "
                + java.util.Arrays.asList(propertyNames) + " sqlConstruct: "
                + this.filters.getSeSqlConstruct());
        }

        SeQuery query = new SeQuery(connection, propertyNames,
                this.filters.getSeSqlConstruct());
        SeFilter[] spatialConstraints = this.filters.getSpatialFilters();
        
        if (spatialConstraints.length > 0) {
            query.setSpatialConstraints(SeQuery.SE_OPTIMIZE,
                setReturnGeometryMasks, spatialConstraints);
        }

        return query;
    }

    /**
     * Returns the attribute names of the FeatureType passed to the
     * constructor.
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException if the SeLayer can't be obtained (only if the
     *         geomety attribute was not included in the request).
     */
    private String[] getPropertiesToFetch() throws IOException {
        String[] attNames = new String[this.schema.getAttributeCount()];

        for (int i = 0; i < this.schema.getAttributeCount(); i++) {
            attNames[i] = this.schema.getAttributeType(i).getName();
        }

        return attNames;
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
        return createQuery(ds, query).calculateResultCount();
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
        return createQuery(ds, query).calculateQueryExtent();
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
                countQuery = createSeQueryForQueryInfo(getConnection(), columns, true);

                SeQueryInfo qInfo = new SeQueryInfo();
                qInfo.setConstruct(this.filters.getSeSqlConstruct());

                SeTable.SeTableStats tableStats = countQuery
                    .calculateTableStatistics(aFieldName,
                        SeTable.SeTableStats.SE_COUNT_STATS, qInfo, 0);

                this.resultCount = tableStats.getCount();
            } catch (SeException e) {
                throw new DataSourceException("Calculating result count: "
                    + e.getSeError().getErrDesc(), e);
            } finally {
                close(countQuery);
                releaseConnection();
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
            final SeLayer layer = this.connectionPool.getSdeLayer(this.schema
                    .getTypeName());
            String[] spatialCol = { layer.getSpatialColumn() };

            extentQuery = createSeQueryForQueryInfo(getConnection(), spatialCol, true);

            SeQueryInfo sdeQueryInfo = new SeQueryInfo();
            sdeQueryInfo.setColumns(spatialCol);
            sdeQueryInfo.setConstruct(this.filters.getSeSqlConstruct());

            extent = extentQuery.calculateLayerExtent(sdeQueryInfo);

            envelope = new Envelope(extent.getMinX(), extent.getMaxX(),
                    extent.getMinY(), extent.getMaxY());
            LOGGER.info("got extent: " + extent + ", built envelope: "
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
            releaseConnection();
        }

        return envelope;
    }

    /**
     * DOCUMENT ME!
     */
    private void releaseConnection() {
        if (this.connectionPool != null && this.connection != null) {
            this.connection.close();
            this.connection = null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    private PooledConnection getConnection() throws DataSourceException {
        if (this.connection == null) {
            try {
            	if(this.connectionPool == null){
            		throw new IllegalStateException("query is closed");
            	}
                this.connection = this.connectionPool.getConnection();
            } catch (UnavailableConnectionException e) {
                throw new DataSourceException("Can't obtain a connection: "
                    + e.getMessage(), e);
            }
        }

        return this.connection;
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
        releaseConnection();

        if (this.connectionPool != null) {
            this.connectionPool = null;
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
     * @throws IOException DOCUMENT ME!
     * @throws DataSourceException DOCUMENT ME!
     */
    public SeRow fetch() throws IOException {
        try {
            return getSeQuery().fetch();
        } catch (SeException e) {
            throw new DataSourceException(e.getSeError().getErrDesc(), e);
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
        private SeLayer sdeLayer;

        /** DOCUMENT ME! */
        private Filter sourceFilter;

        /** DOCUMENT ME! */
        private Filter sqlFilter;

        /** DOCUMENT ME! */
        private Filter geometryFilter;

        /** DOCUMENT ME! */
        private Filter unsupportedFilter;

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

        /**
         * Creates a new FilterSet object.
         *
         * @param sdeLayer DOCUMENT ME!
         * @param sourceFilter DOCUMENT ME!
         */
        public FilterSet(SeLayer sdeLayer, Filter sourceFilter) {
            this.sdeLayer = sdeLayer;
            this.sourceFilter = sourceFilter;
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
            /** DOCUMENT ME! */
            SQLEncoderSDE sqlEncoder = new SQLEncoderSDE(this.sdeLayer);

            SQLUnpacker unpacker = new SQLUnpacker(sqlEncoder.getCapabilities());

            unpacker.unPackAND(this.sourceFilter);

            this.sqlFilter = unpacker.getSupported();

            Filter remainingFilter = unpacker.getUnSupported();

            unpacker = new SQLUnpacker(GeometryEncoderSDE.getCapabilities());
            unpacker.unPackAND(remainingFilter);

            this.geometryFilter = unpacker.getSupported();
            this.unsupportedFilter = unpacker.getUnSupported();
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
                try {
                    String layerName = this.sdeLayer.getQualifiedName();
                    this.sdeSqlConstruct = new SeSqlConstruct(layerName);
                } catch (SeException e) {
                    throw new DataSourceException(
                        "Can't create SQL construct: "
                        + e.getSeError().getErrDesc(), e);
                }

                Filter sqlFilter = getSqlFilter();

                if (!Filter.INCLUDE.equals(sqlFilter)) {
                    String whereClause = null;
                    SQLEncoderSDE sqlEncoder = new SQLEncoderSDE(this.sdeLayer);

                    try {
                        whereClause = sqlEncoder.encode(sqlFilter);
                    } catch (SQLEncoderException sqle) {
                        String message = "Geometry encoder error: "
                            + sqle.getMessage();
                        throw new DataSourceException(message, sqle);
                    }

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
    }
}
