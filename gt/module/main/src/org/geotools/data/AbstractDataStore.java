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
package org.geotools.data;

import org.geotools.catalog.AbstractMetadataEntity;
import org.geotools.catalog.DefaultQueryResult;
import org.geotools.cs.CoordinateSystem;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.filter.Filter;
import org.opengis.catalog.Catalog;
import org.opengis.catalog.CatalogEntry;
import org.opengis.catalog.MetadataEntity;
import org.opengis.catalog.QueryDefinition;
import org.opengis.catalog.QueryResult;

import com.vividsolutions.jts.geom.Envelope;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Represents a stating point for implementing your own DataStore.
 *
 * <p>
 * The goal is to have this class provide <b>everything</b> else if you can
 * only provide:
 * </p>
 *
 * <ul>
 * <li>
 * String[] getFeatureTypes()
 * </li>
 * <li>
 * FeatureType getSchema(String typeName)
 * </li>
 * <li>
 * FeatureReader getFeatureReader( typeName )
 * </li>
 * <li>
 * FeatureWriter getFeatureWriter( typeName )
 * </li>
 * </ul>
 *
 * and optionally this protected methods to allow custom query optimizations:
 *
 * <ul>
 * <li>
 * Filter getUnsupportedFilter(String typeName, Filter filter)
 * </li>
 * <li>
 * FeatureReader getFeatureReader(String typeName, Query query)
 * </li>
 * </ul>
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
 * @author jgarnett
 */
public abstract class AbstractDataStore implements DataStore, Catalog {
    /** The logger for the filter module. */
    protected static final Logger LOGGER = Logger.getLogger("org.geotools.data");

    /** Manages listener lists for FeatureSource implementation */
    public FeatureListenerManager listenerManager = new FeatureListenerManager();

    /**
     * Flags AbstractDataStore to allow Modification.
     * <p>
     * GetFeatureSource will return a FeatureStore is this is true.
     * </p>
     */
    protected final boolean isWriteable;

    /**
     * Manages InProcess locks for FeatureLocking implementations.
     *
     * <p>
     * May be null if subclass is providing real locking.
     * </p>
     */
    private InProcessLockingManager lockingManager;

    /** Default (Writeable) DataStore */
    public AbstractDataStore() {
        this(true);
    }

    /**
     * AbstractDataStore creation.
     * 
     * @param isWriteable true for writeable DataStore. 
     */
    public AbstractDataStore(boolean isWriteable) {
        this.isWriteable = isWriteable;
        lockingManager = createLockingManager();
    }

    /**
     * Currently returns an InProcessLockingManager.
     *
     * <p>
     * Subclasses that implement real locking may override this method to
     * return <code>null</code>.
     * </p>
     *
     * @return InProcessLockingManager or null.
     */
    protected InProcessLockingManager createLockingManager() {
        return new InProcessLockingManager();
    }

    public void fireAdded( Feature newFeature ){
        String typeName = newFeature.getFeatureType().getTypeName();
        listenerManager.fireFeaturesAdded( typeName, Transaction.AUTO_COMMIT, newFeature.getBounds() );
    }
    public void fireRemoved( Feature removedFeature ){
        String typeName = removedFeature.getFeatureType().getTypeName();
        listenerManager.fireFeaturesRemoved( typeName, Transaction.AUTO_COMMIT, removedFeature.getBounds() );
    }
    public void fireChanged( Feature before, Feature after ){
        String typeName = after.getFeatureType().getTypeName();
        Envelope bounds = new Envelope();
        bounds.expandToInclude( before.getBounds() );
        bounds.expandToInclude( after.getBounds() );
        listenerManager.fireFeaturesChanged( typeName, Transaction.AUTO_COMMIT, bounds );
    }

    /* (non-Javadoc)
     * @see org.opengis.catalog.Catalog#add(org.opengis.catalog.CatalogEntry)
     */
    public void add(CatalogEntry arg0) throws IllegalStateException {
        throw new UnsupportedOperationException("DataStore does not support the modification of catalog information" );
    }
    /* (non-Javadoc)
     * @see org.opengis.catalog.Catalog#remove(org.opengis.catalog.CatalogEntry)
     */
    public void remove(CatalogEntry arg0) throws IllegalStateException {
        throw new UnsupportedOperationException("DataStore does not support the modification of catalog information" );
    }
    /** Iterator of Catalog entries - one for each featureType provided by this Datastore */
    public Iterator iterator() {
        String typeNames[] = getTypeNames();
        List list = new ArrayList( typeNames.length );
        for( int i=0; i<typeNames.length; i++){
            list.add( catalogEntry( typeNames[i] ));
        }
        return list.iterator();
    }
    public QueryResult query(QueryDefinition arg0) {
        QueryResult result = new DefaultQueryResult();
        
        return null;
    }
    protected MetadataEntity metadata( final String typeName ){
        return new AbstractMetadataEntity(){
            String getName(){
                return typeName;
            }
        };
    }
    protected CatalogEntry catalogEntry( final String typeName ){
        return new CatalogEntry(){            
            public Object getResource() {
                try {
                    return getFeatureSource( typeName );
                } catch (IOException e) {
                    return null;
                }
            }
            public String getDataName() {
                return typeName;
            }
            public int getNumMetadata() {
                return 1;
            }

            public String[] getMetadataNames() {
                return new String[]{ "default", };
            }
            public MetadataEntity getMetadata(int index) {
                if( index == 1){
                    return metadata( typeName );
                }
                return null;
            }

            public MetadataEntity getMetadata(String metadataName ) {
                if( "default".equals( metadataName )){
                    return metadata( typeName );
                }
                return null;
            }

            public Iterator iterator() {
                return Collections.singleton( metadata( typeName) ).iterator();                
            }
            
        };
    }
    /** Convience method for retriving all the names from the Catalog Entires */
    public abstract String[] getTypeNames();

    /** Retrive schema information for typeName */
    public abstract FeatureType getSchema(String typeName)
        throws IOException;

    /**
     * Subclass must implement.
     *
     * @param typeName
     *
     * @return FeatureReader over contents of typeName
     */
    protected abstract FeatureReader getFeatureReader(String typeName)
        throws IOException;

    /**
     * Subclass should implement this to provide writing support.
     *
     * @param typeName
     *
     * @return FeatureWriter over contents of typeName
     *
     * @throws IOException Subclass may throw IOException
     * @throws UnsupportedOperationException Subclass may implement
     */
    protected FeatureWriter getFeatureWriter(String typeName)
        throws IOException {
        throw new UnsupportedOperationException("Writing not supported");
    }

    /**
     * Subclass should implement to provide writing support.
     *
     * @param featureType Requested FeatureType
     *
     * @throws IOException Subclass may throw IOException
     * @throws UnsupportedOperationException Subclass may implement
     */
    public void createSchema(FeatureType featureType) throws IOException {
        throw new UnsupportedOperationException("Schema creation not supported");
    }
    /* (non-Javadoc)
     * @see org.geotools.data.DataStore#updateSchema(java.lang.String, org.geotools.feature.FeatureType)
     */
    public void updateSchema(String typeName, FeatureType featureType)
        throws IOException {
        throw new UnsupportedOperationException("Schema modification not supported");
    }

    // Jody - This is my recomendation for DataStore
    // in order to support CS reprojection and override
    public FeatureSource getView(final Query query)
        throws IOException, SchemaException {
        String typeName = query.getTypeName();
        FeatureType origionalType = getSchema( typeName );
        //CoordinateSystem cs = query.getCoordinateSystem();
        //final FeatureType featureType = DataUtilities.createSubType( origionalType, query.getPropertyNames(), cs );
        final FeatureType featureType = DataUtilities.createSubType( origionalType, query.getPropertyNames() );

        return new AbstractFeatureSource() {
            public DataStore getDataStore() {
                return AbstractDataStore.this;
            }

            public void addFeatureListener(FeatureListener listener) {
                listenerManager.addFeatureListener(this, listener);
            }

            public void removeFeatureListener(FeatureListener listener) {
                listenerManager.removeFeatureListener(this, listener);
            }

            public FeatureType getSchema() {
                return featureType;
            }
        };
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
    public FeatureSource getFeatureSource(final String typeName)
        throws IOException {
        final FeatureType featureType = getSchema(typeName);

        if (isWriteable) {
            if (lockingManager != null) {
                return new AbstractFeatureLocking() {
                    public DataStore getDataStore() {
                        return AbstractDataStore.this;
                    }

                    public void addFeatureListener(FeatureListener listener) {
                        listenerManager.addFeatureListener(this, listener);
                    }

                    public void removeFeatureListener(
                        FeatureListener listener) {
                        listenerManager.removeFeatureListener(this, listener);
                    }

                    public FeatureType getSchema() {
                        return featureType;
                    }
                };
            } else {
                return new AbstractFeatureStore() {
                    public DataStore getDataStore() {
                        return AbstractDataStore.this;
                    }

                    public void addFeatureListener(FeatureListener listener) {
                        listenerManager.addFeatureListener(this, listener);
                    }

                    public void removeFeatureListener(
                        FeatureListener listener) {
                        listenerManager.removeFeatureListener(this, listener);
                    }

                    public FeatureType getSchema() {
                        return featureType;
                    }
                };
            }
        } else {
            return new AbstractFeatureSource() {
                public DataStore getDataStore() {
                    return AbstractDataStore.this;
                }

                public void addFeatureListener(FeatureListener listener) {
                    listenerManager.addFeatureListener(this, listener);
                }

                public void removeFeatureListener(FeatureListener listener) {
                    listenerManager.removeFeatureListener(this, listener);
                }

                public FeatureType getSchema() {
                    return featureType;
                }
            };
        }
    }


    // Jody - Recomend moving to the following
    // When we are ready for CoordinateSystem support
    public FeatureReader getFeatureReader(Query query,Transaction transaction) throws IOException {
        Filter filter = query.getFilter();
        String typeName = query.getTypeName();
        String propertyNames[] = query.getPropertyNames();
        CoordinateSystem cs = null;

        if (filter == null) {
            throw new NullPointerException("getFeatureReader requires Filter: "
                + "did you mean Filter.NONE?");
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
        FeatureType featureType = getSchema( query.getTypeName() );

        if( propertyNames != null || cs != null ){
            try {
                featureType = DataUtilities.createSubType( featureType, propertyNames, cs );
            } catch (SchemaException e) {
                LOGGER.log( Level.FINEST, e.getMessage(), e);
                throw new DataSourceException( "Could not create Feature Type for query", e );

            }
        }
        if ( filter == Filter.ALL || filter.equals( Filter.ALL )) {
            return new EmptyFeatureReader(featureType);
        }
        //GR: allow subclases to implement as much filtering as they can,
        //by returning just it's unsupperted filter
        filter = getUnsupportedFilter(typeName, filter);
        if(filter == null){
            throw new NullPointerException("getUnsupportedFilter shouldn't return null. Do you mean Filter.NONE?");
        }

        // This calls our subclass "simple" implementation
        // All other functionality will be built as a reader around
        // this class
        //
        FeatureReader reader = getFeatureReader(typeName, query);

        if (!filter.equals( Filter.NONE ) ) {
            reader = new FilteringFeatureReader(reader, filter);
        }

        if (transaction != Transaction.AUTO_COMMIT) {
            Map diff = state(transaction).diff(typeName);
            reader = new DiffFeatureReader(reader, diff);
        }

        if (!featureType.equals(reader.getFeatureType())) {
            LOGGER.fine("Recasting feature type to subtype by using a ReTypeFeatureReader");
            reader = new ReTypeFeatureReader(reader, featureType);
        }

        if (query.getMaxFeatures() != Query.DEFAULT_MAX) {
			    reader = new MaxFeatureReader(reader, query.getMaxFeatures());
        }

        return reader;
    }

    /**
     * GR: this method is called from inside getFeatureReader(Query ,Transaction )
     * to allow subclasses return an optimized FeatureReader wich supports the
     * filter and attributes truncation specified in <code>query</code>
     * <p>
     * A subclass that supports the creation of such an optimized FeatureReader
     * shold override this method. Otherwise, it just returns
     * <code>getFeatureReader(typeName)</code>
     * <p>
     */
    protected FeatureReader getFeatureReader(String typeName, Query query)
    throws IOException
    {
      return getFeatureReader(typeName);
    }
    /**
     * GR: if a subclass supports filtering, it should override this method
     * to return the unsupported part of the passed filter, so a
     * FilteringFeatureReader will be constructed upon it. Otherwise it will
     * just return the same filter.
     * <p>
     * If the complete filter is supported, the subclass must return <code>Filter.NONE</code>
     * </p>
     */
    protected Filter getUnsupportedFilter(String typeName, Filter filter)
    {
      return filter;
    }
    /*
     * @see org.geotools.data.DataStore#getFeatureReader(org.geotools.feature.FeatureType,
     *      org.geotools.filter.Filter, org.geotools.data.Transaction)
     *
    public FeatureReader getFeatureReader(FeatureType featureType,
        Filter filter, Transaction transaction) throws IOException {
        if (filter == null) {
            throw new NullPointerException("getFeatureReader requires Filter: "
                + "did you mean Filter.NONE?");
        }

        if (featureType == null) {
            throw new NullPointerException(
                "getFeatureReader requires FeatureType: "
                + "use getSchema( typeName ) to aquire a FeatureType");
        }

        if (transaction == null) {
            throw new NullPointerException(
                "getFeatureReader requires Transaction: "
                + "did you mean to use Transaction.AUTO_COMMIT?");
        }

        if (filter == Filter.ALL) {
            return new EmptyFeatureReader(featureType);
        }

        String typeName = featureType.getTypeName();

        FeatureReader reader = getFeatureReader(typeName);

        if (filter != Filter.NONE) {
            reader = new FilteringFeatureReader(reader, filter);
        }

        if (transaction != Transaction.AUTO_COMMIT) {
            Map diff = state(transaction).diff(typeName);
            reader = new DiffFeatureReader(reader, diff);
        }

        if (!featureType.equals(reader.getFeatureType())) {
            reader = new ReTypeFeatureReader(reader, featureType);
        }

        return reader;
    }
   */
    TransactionStateDiff state(Transaction transaction) {
        synchronized (transaction) {
            TransactionStateDiff state = (TransactionStateDiff) transaction
                .getState(this);

            if (state == null) {
                state = new TransactionStateDiff(this);
                transaction.putState(this, state);
            }

            return state;
        }
    }

    /* (non-Javadoc)
     * @see org.geotools.data.DataStore#getFeatureWriter(java.lang.String, org.geotools.filter.Filter, org.geotools.data.Transaction)
     */
    public FeatureWriter getFeatureWriter(String typeName, Filter filter,
        Transaction transaction) throws IOException {
        if (filter == null) {
            throw new NullPointerException("getFeatureReader requires Filter: "
                + "did you mean Filter.NONE?");
        }

        if (filter == Filter.ALL) {
            FeatureType featureType = getSchema(typeName);

            return new EmptyFeatureWriter(featureType);
        }

        FeatureWriter writer = getFeatureWriter(typeName, transaction);

        if (filter != Filter.NONE) {
            writer = new FilteringFeatureWriter(writer, filter);
        }

        return writer;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.DataStore#getFeatureWriter(java.lang.String, org.geotools.data.Transaction)
     */
    public FeatureWriter getFeatureWriter(String typeName,
        Transaction transaction) throws IOException {
        if (transaction == null) {
            throw new NullPointerException(
                "getFeatureWriter requires Transaction: "
                + "did you mean to use Transaction.AUTO_COMMIT?");
        }

        FeatureWriter writer;

        if (transaction == Transaction.AUTO_COMMIT) {
            writer = getFeatureWriter(typeName);
        } else {
            writer = state(transaction).writer(typeName);
        }

        if (lockingManager != null) {
            // subclass has not provided locking so we will
            // fake it with InProcess locks
            writer = lockingManager.checkedWriter(writer, transaction);
        }

        return writer;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.DataStore#getFeatureWriterAppend(java.lang.String, org.geotools.data.Transaction)
     *
     */
    public FeatureWriter getFeatureWriterAppend(String typeName,
        Transaction transaction) throws IOException {
        FeatureWriter writer = getFeatureWriter(typeName, transaction);

        while (writer.hasNext()) {
            writer.next(); // Hmmm this would be a use for skip() then?
        }

        return writer;
    }

    /**
     * Locking manager used for this DataStore.
     *
     * <p>
     * By default AbstractDataStore makes use of InProcessLockingManager.
     * </p>
     *
     * @return
     *
     * @see org.geotools.data.DataStore#getLockingManager()
     */
    public LockingManager getLockingManager() {
        return lockingManager;
    }

    /**
     * Computes the bounds of the features for the specified feature type that
     * satisfy the query provided that there is a fast way to get that result.
     * <p>
     * Will return null if there is not fast way to compute the bounds. Since
     * it's based on some kind of header/cached information, it's not guaranteed
     * to be real bound of the features
     * </p>
     * @param query
     * @return the bounds, or null if too expensive
     * @throws IOException
     */
    protected Envelope getBounds(Query query) throws IOException {
        return null; // too expensive
    }


    /**
     * Gets the number of the features that would be returned by this query for
     * the specified feature type.
     * <p>
     * If getBounds(Query) returns <code>-1</code> due to expense consider
     * using <code>getFeatures(Query).getCount()</code> as a an alternative.
     * </p>
     *
     * @param query Contains the Filter and MaxFeatures to find the bounds for.
     * @return The number of Features provided by the Query or <code>-1</code>
     *         if count is too expensive to calculate or any errors or occur.
     *
     * @throws IOException if there are errors getting the count
     */
    protected int getCount(Query query) throws IOException {
        return -1; // too expensive
    }
}
