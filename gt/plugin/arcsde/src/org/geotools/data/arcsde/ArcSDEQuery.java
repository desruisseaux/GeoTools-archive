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
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeExtent;
import com.esri.sde.sdk.client.SeFilter;
import com.esri.sde.sdk.client.SeLayer;
import com.esri.sde.sdk.client.SeLog;
import com.esri.sde.sdk.client.SeObjectId;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeQueryInfo;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeSqlConstruct;
import com.esri.sde.sdk.client.SeTable;
import com.vividsolutions.jts.geom.Envelope;
import org.geotools.data.DataSourceException;
import org.geotools.feature.FeatureType;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterTransformer;
import org.geotools.filter.GeometryEncoderException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.TransformerException;


/**
 * Wrapper class for SeQuery to hold a SeConnection until close() is called and
 * provide utility methods.
 *
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id: ArcSDEQuery.java,v 1.1 2004/06/21 15:00:33 cdillard Exp $
 */
class ArcSDEQuery {
    /** DOCUMENT ME! */
    private static final Logger LOGGER = Logger.getLogger(ArcSDEQuery.class.getPackage()
                                                                           .getName());

    /** DOCUMENT ME! */
    private ArcSDEConnectionPool connectionPool;

    /** DOCUMENT ME! */
    private FeatureType schema;

    /**
     * The query built using the constraints given by the geotools Query. It
     * must not be accessed directly, but through <code>getSeQuery()</code>,
     * since it is lazyly created
     */
    private SeQuery query;

    /** DOCUMENT ME! */
    private SeSqlConstruct sqlConstruct;

    /** DOCUMENT ME! */
    private ArcSDEAdapter.FilterSet filters;

    /** DOCUMENT ME! */
    private SeConnection connection = null;

    /** the lazyly calculated result count */
    private int resultCount = -1;

    /**
     * Creates a new SDEQuery object.
     *
     * @param pool DOCUMENT ME!
     * @param schema the schema with all the attributes as expected.
     * @param sqlConstruct DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     *
     * @see prepareQuery
     */
    public ArcSDEQuery(ArcSDEConnectionPool pool, FeatureType schema,
        SeSqlConstruct sqlConstruct) throws DataSourceException {
        this.schema = schema;
        this.connectionPool = pool;
        this.sqlConstruct = sqlConstruct;
    }

    /**
     * Lazyly creates and returns the SeQuery
     *
     * @return DOCUMENT ME!
     *
     * @throws SeException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     * @throws UnavailableConnectionException DOCUMENT ME!
     */
    private SeQuery getSeQuery()
        throws SeException, IOException, UnavailableConnectionException {
        if (this.query == null) {
            connection = this.connectionPool.getConnection();
            LOGGER.fine("constructing new sql query with connection: "
                + connection + ", propnames: "
                + java.util.Arrays.asList(getQueryPropertyNames())
                + " sqlConstruct: " + sqlConstruct);
            this.query = new SeQuery(connection, getQueryPropertyNames(),
                    sqlConstruct);
        }

        return query;
    }

    /**
     * Returns the attribute names of the FeatureType passed to the
     * constructor, plust the geometry attribute name if it was not part of
     * the request, due to the need to grab the SeShape to obtain its feature
     * id.
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException if the SeLayer can't be obtained (only if the
     *         geomety attribute was not included in the request).
     */
    private String[] getQueryPropertyNames() throws IOException {
        String[] attNames = new String[schema.getAttributeCount()];

        for (int i = 0; i < schema.getAttributeCount(); i++) {
            attNames[i] = schema.getAttributeType(i).getName();
        }

        if (schema.getDefaultGeometry() == null) {
            LOGGER.info("geometry att not included in query. Adding it "
                + " to be able of fetching feature ids, but will not appear "
                + "in results");

            String[] atts = new String[1 + attNames.length];
            System.arraycopy(attNames, 0, atts, 0, attNames.length);

            SeLayer layer = connectionPool.getSdeLayer(schema.getTypeName());
            String spatialCol = layer.getSpatialColumn();
            atts[attNames.length] = spatialCol;
            attNames = atts;
            LOGGER.info("Added spatial column " + spatialCol);
        }

        return attNames;
    }

    /**
     * Returns the schema of the originating Query
     *
     * @return the schema of the originating Query
     */
    public FeatureType getSchema() {
        return schema;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public SeSqlConstruct getSeSqlConstruct() {
        return this.sqlConstruct;
    }

    /**
     * DOCUMENT ME!
     *
     * @param filters DOCUMENT ME!
     */
    public void setFilterSet(ArcSDEAdapter.FilterSet filters) {
        this.filters = filters;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public ArcSDEAdapter.FilterSet getFilters() {
        return filters;
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
            String typeName = getSchema().getTypeName();
            String aFieldName = "*";
            String[] columns = { aFieldName };

            SeQuery countQuery = null;

            try {
                countQuery = new SeQuery(connection, columns, sqlConstruct);

                SeFilter[] geometryFilters = null;

                if (filters.getGeometryFilter() != Filter.NONE) {
                    try {
                        geometryFilters = filters.createSpatialFilters();
                    } catch (GeometryEncoderException ex) {
                        throw new DataSourceException(
                            "Can't create the spatial filter: "
                            + ex.getMessage(), ex);
                    }
                }

                if ((geometryFilters != null) && (geometryFilters.length > 0)) {
                    final boolean RETURN_GEOMETRY_MASKS = true;
                    countQuery.setSpatialConstraints(SeQuery.SE_OPTIMIZE,
                        RETURN_GEOMETRY_MASKS, geometryFilters);
                }

                SeQueryInfo qInfo = new SeQueryInfo();
                qInfo.setConstruct(sqlConstruct);

                SeTable.SeTableStats tableStats = countQuery
                    .calculateTableStatistics(aFieldName,
                        SeTable.SeTableStats.SE_COUNT_STATS, qInfo, 0);

                resultCount = tableStats.getCount();
            } catch (SeException e) {
                throw new DataSourceException("Calculating result count: "
                    + e.getSeError().getErrDesc(), e);
            } finally {
                close(countQuery);
            }
        }

        return resultCount;
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
            final SeLayer layer = connectionPool.getSdeLayer(schema.getTypeName());
            String[] spatialCol = { layer.getSpatialColumn() };

            extentQuery = new SeQuery(connection, spatialCol, sqlConstruct);

            if (!Filter.NONE.equals(filters.getGeometryFilter())) {
                SeFilter[] geometryFilters = null;

                try {
                    geometryFilters = filters.createSpatialFilters();
                } catch (GeometryEncoderException e) {
                    throw new DataSourceException(
                        "Error creating the spatial filters: " + e.getMessage(),
                        e);
                }

                if ((geometryFilters != null) && (geometryFilters.length > 0)) {
                    extentQuery.setSpatialConstraints(SeQuery.SE_OPTIMIZE,
                        true, geometryFilters);
                }
            }

            SeQueryInfo sdeQueryInfo = new SeQueryInfo();
            sdeQueryInfo.setColumns(spatialCol);
            sdeQueryInfo.setConstruct(sqlConstruct);
            extent = extentQuery.calculateLayerExtent(sdeQueryInfo);
            envelope = new Envelope(extent.getMinX(), extent.getMaxX(),
                    extent.getMinY(), extent.getMaxY());
            LOGGER.info("got extent: " + extent + ", built envelope: "
                + envelope);
        } catch (SeException ex) {
            ////////////////////////
            try {
				String filter = new FilterTransformer().transform(filters
				        .getGeometryFilter());
				String sql = (sqlConstruct == null) ? null : sqlConstruct.getWhere();
				LOGGER.log(Level.SEVERE, "***********************\n" +
						ex.getSeError().getErrDesc() + 
						"\nfilter: " + filter +
						"\nSQL: " + sql, ex);
				
			} catch (TransformerException e) {
				e.printStackTrace();
			}

            /////////////////////////
            ex.printStackTrace();
            throw new DataSourceException("Can't consult the query extent: "
                + ex.getSeError().getErrDesc(), ex);
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

    ////////////////////////////////////////////////////////////////////////
    ////////////// RELEVANT METHODS WRAPPED FROM SeStreamOp ////////////////
    ////////////////////////////////////////////////////////////////////////

    /**
     * Closes the query and releases the holded connection back to the
     * connection pool. If reset is TRUE, the query status is set to INACTIVE;
     * also releases the SeConnection back to the SeConnectionPool
     */
    public void close() {
        close(query);

        if ((connectionPool != null) && (connection != null)) {
            LOGGER.finer("releasing connection: " + connection);
            connectionPool.release(connection);
            connection = null;
            connectionPool = null;
        }
    }

    /**
     * Determines if the stream operation is in use
     *
     * @return true if the stream operation is in use
     *
     * @throws SeException DOCUMENT ME!
     */
    public boolean inProgress() throws SeException {
        return query.inProgress();
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
        } catch (UnavailableConnectionException e) {
            throw new DataSourceException(e.getMessage(), e);
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
        } catch (UnavailableConnectionException e) {
            throw new DataSourceException(e.getMessage(), e);
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
        } catch (UnavailableConnectionException e) {
            throw new DataSourceException(e.getMessage(), e);
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
       public void setState(SeObjectId sourceId, SeObjectId differencesId,
           int differencesType) throws SeException {
           getSeQuery().setState(sourceId, differencesId, differencesType);
       }
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
        } catch (UnavailableConnectionException e) {
            throw new DataSourceException(e.getMessage(), e);
        }
    }

    ////////////////////////////////////////////////////////////////////////
    ///////////////// METHODS WRAPPED FROM SeQuery /////////////////////
    ////////////////////////////////////////////////////////////////////////

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
        } catch (UnavailableConnectionException e) {
            throw new DataSourceException(e.getMessage(), e);
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
        } catch (UnavailableConnectionException e) {
            throw new DataSourceException(e.getMessage(), e);
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
        } catch (UnavailableConnectionException e) {
            throw new DataSourceException(e.getMessage(), e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String toString() {
        return "Schema: " + schema.getTypeName() + ", query: " + query;
    }
}
