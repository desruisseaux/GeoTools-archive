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

import org.geotools.feature.FeatureType;
import org.geotools.filter.Filter;
import java.io.IOException;
import java.util.Map;
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
public abstract class AbstractDataStore implements DataStore {
    /** The logger for the filter module. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.data");

    /** Manages listener lists for FeatureSource implementation */
    protected FeatureListenerManager listenerManager = new FeatureListenerManager();

    protected final boolean isWriteable;
    /**
     * Manages InProcess locks for FeatureLocking implementations.
     * 
     * <p>
     * May be null if subclass is providing real locking.
     * </p>
     */
    private InProcessLockingManager lockingManager;

    public AbstractDataStore() {
        this( true );    
    }
    public AbstractDataStore( boolean isWriteable){
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

    public abstract String[] getTypeNames();

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
     * @return FeatureWriter over contents of typeName
     */
    protected FeatureWriter getFeatureWriter(String typeName) throws IOException {
        throw new UnsupportedOperationException("Writing not supported");
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
        if( isWriteable ){
            if (lockingManager != null) {
                return new AbstractFeatureLocking() {
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
            } else {
                return new AbstractFeatureStore() {
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
        else {
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

    /**
     * @see org.geotools.data.DataStore#getFeatureReader(org.geotools.feature.FeatureType,
     *      org.geotools.filter.Filter, org.geotools.data.Transaction)
     */
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
    public FeatureWriter getFeatureWriter(String typeName, Transaction transaction) throws IOException {
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
    public FeatureWriter getFeatureWriterAppend( String typeName, Transaction transaction ) throws IOException {
        FeatureWriter writer = getFeatureWriter( typeName, transaction );        
        
        while (writer.hasNext()){
            writer.next();// Hmmm this would be a use for skip() then?
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
}
