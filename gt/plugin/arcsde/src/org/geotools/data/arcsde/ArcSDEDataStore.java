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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.filter.Filter;

import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeLayer;
import com.vividsolutions.jts.geom.Envelope;


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
 * <p>
 * Pleae note that there may be a better place for you to start out from, (like
 * JDBCDataStore).
 * </p>
 *
 * @author Gabriel Rold?n
 * @version $Id: ArcSDEDataStore.java,v 1.8 2004/06/28 10:24:32 jfear Exp $
 */
public class ArcSDEDataStore extends AbstractDataStore {
    /** DOCUMENT ME! */
    private static final Logger LOGGER = Logger.getLogger(ArcSDEDataStore.class.getPackage()
                                                                               .getName());
    private ArcSDEConnectionPool connectionPool;

    /** <code>Map&lt;typeName/FeatureType&gt;</code> of feature type schemas */
    private Map schemasCache = new HashMap();

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
     * DOCUMENT ME!
     *
     * @param typeName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws java.io.IOException DOCUMENT ME!
     */
    public synchronized FeatureType getSchema(String typeName)
        throws java.io.IOException {
        FeatureType schema = (FeatureType) schemasCache.get(typeName);

        if (schema == null) {
            schema = ArcSDEAdapter.createSchema(getConnectionPool(), typeName);
            schemasCache.put(typeName, schema);
        }

        return schema;
    }

    /**
     *
     */
    public void createSchema(FeatureType featureType) throws IOException {
        SeConnection connection = null;

        // Create a new SeTable/SeLayer with the specified attributes....
        try {
            connection = connectionPool.getConnection();
        } catch (DataSourceException dse) {
            LOGGER.log(Level.WARNING, dse.getMessage(), dse);
        } catch (UnavailableConnectionException uce) {
            LOGGER.log(Level.WARNING, uce.getMessage(), uce);
        } finally {
            connectionPool.release(connection);
        }
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
                            for (int i = 0, ii = atts.getAttributeCount();
                                    i < ii; i++) {
                                attributes[i] = atts.read(i);
                            }

                            return resultingSchema.create(attributes,
                                ((ArcSDEAttributeReader) atts).readFID());
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

            throw new DataSourceException("Problem with feature reader: " +
                                          t.getMessage(), t);
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
            FilterSet filters = ArcSDEAdapter.computeFilters(this, typeName,
                    filter);

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

        synchronized (this) {
            state = (ArcTransactionState) transaction.getState(this);

            if (state == null) {
                state = new ArcTransactionState(this);
                transaction.putState(this, state);
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
        ArcSDEQuery sdeQuery = null;

        try {
            sdeQuery = ArcSDEAdapter.createSeQuery(this, query);

            return sdeQuery.calculateResultCount();
        } catch (DataSourceException ex) {
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
     */
    protected Envelope getBounds(Query query) throws IOException {
        ArcSDEQuery sdeQuery = null;

        try {
            sdeQuery = ArcSDEAdapter.createSeQuery(this, query);

            return sdeQuery.calculateQueryExtent();
        } catch (DataSourceException ex) {
            throw ex;
        } finally {
            if (sdeQuery != null) {
                sdeQuery.close();
            }
        }
    }
}
