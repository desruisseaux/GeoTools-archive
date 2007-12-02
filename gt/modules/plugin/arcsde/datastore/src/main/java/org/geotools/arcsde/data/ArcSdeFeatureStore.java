package org.geotools.arcsde.data;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.arcsde.ArcSdeException;
import org.geotools.arcsde.pool.ArcSDEConnectionPool;
import org.geotools.arcsde.pool.ArcSDEPooledConnection;
import org.geotools.arcsde.pool.UnavailableArcSDEConnectionException;
import org.geotools.data.DataSourceException;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureStore;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.logging.Logging;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;

import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeTable;

public class ArcSdeFeatureStore extends ArcSdeFeatureSource implements FeatureStore {

    private static final Logger LOGGER = Logging.getLogger("org.geotools.arcsde.data");

    private Transaction transaction = Transaction.AUTO_COMMIT;

    public ArcSdeFeatureStore(final FeatureTypeInfo typeInfo, final ArcSDEDataStore arcSDEDataStore) {
        super(typeInfo, arcSDEDataStore);
    }

    /**
     * @see FeatureStore#getTransaction()
     */
    public synchronized Transaction getTransaction() {
        return transaction;
    }

    /**
     * Sets this FeatureStore transaction.
     * <p>
     * If transaction is not auto commit, initiates an
     * {@link ArcTransactionState} with the dataStore's connection pool as key.
     * </p>
     * 
     * @see FeatureStore#setTransaction(Transaction)
     */
    public synchronized void setTransaction(final Transaction transaction) {
        // System.err.println(">>setTransaction called at " +
        // Thread.currentThread().getName());
        if (transaction == null) {
            throw new NullPointerException("mean Transaction.AUTO_COMMIT?");
        }

        // this is hacky as a simple reference check should be enough but
        // there
        // seem to be some class loader issues with udig so it does not work
        boolean isAutoCommit = Transaction.AUTO_COMMIT.equals(transaction);
        if (!isAutoCommit) {
            try {
                transaction.getState(new Object());
            } catch (UnsupportedOperationException e) {
                isAutoCommit = true;
            }
        }

        if (!isAutoCommit) {
            final ArcSDEConnectionPool connectionPool = dataStore.getConnectionPool();
            try {
                // set the transaction state so it grabs a connection and
                // starts
                // a transaction on it
                ArcTransactionState state = ArcTransactionState.getState(transaction,
                        connectionPool, dataStore.listenerManager);
                LOGGER.finer("ArcSDE transaction initialized: " + state);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Can't initiate transaction: " + e.getMessage(), e);
            }
        }

        this.transaction = transaction;
    }

    //
    // /**
    // * Overrides {@link ArcSdeFeatureSource#getFeatures(Query)} to return a
    // * transaction aware collection.
    // *
    // * @see ArcSDEDataStore#getFeatureReader(Query, Transaction)
    // */
    // @Override
    // public FeatureCollection getFeatures(Query query) throws IOException {
    // FeatureCollection collection = new DefaultFeatureResults(this, query);
    // return collection;
    // // final ArcSDEDataStore ds = (ArcSDEDataStore) getDataStore();
    // // final Transaction transaction = getTransaction();
    // // final FeatureReader featureReader = ds.getFeatureReader(query,
    // transaction);
    // // final FeatureCollection collection =
    // DataUtilities.collection(featureReader);
    // // return collection;
    // }

    /**
     * @see FeatureStore#addFeatures(FeatureCollection)
     */
    public Set<String> addFeatures(final FeatureCollection collection) throws IOException {
        // System.err.println(">>addFeatures called at " +
        // Thread.currentThread().getName());
        final String typeName = typeInfo.getFeatureTypeName();
        final ArcSDEPooledConnection connection = getConnection();
        connection.getLock().lock();
        try {
            final FeatureWriter writer = dataStore.getFeatureWriterAppend(typeName, transaction);
            final FeatureIterator iterator = collection.features();
            Set<String> featureIds = new HashSet<String>();
            try {
                SimpleFeature toAdd;
                SimpleFeature newFeature;
                while (iterator.hasNext()) {
                    toAdd = iterator.next();
                    newFeature = writer.next();
                    newFeature.setAttributes(toAdd.getAttributes());
                    writer.write();
                    featureIds.add(newFeature.getID());
                }
            } catch (IOException e) {
                // it was all wrong, let's rollback the transaction as we don't
                // know
                // where things messed up
                if (Transaction.AUTO_COMMIT != transaction) {
                    transaction.rollback();
                }
                throw e;
            } finally {
                iterator.close();
                writer.close();
            }
            return featureIds;
        } finally {
            connection.getLock().unlock();
        }
    }

    @Override
    protected final ReferencedEnvelope getBounds(final Query namedQuery,
            final ArcSDEPooledConnection connection) throws DataSourceException, IOException {
        connection.getLock().lock();
        try {
            return super.getBounds(namedQuery, connection);
        } finally {
            connection.getLock().unlock();
        }
    }

    @Override
    protected int getCount(final Query namedQuery, final ArcSDEPooledConnection connection)
            throws IOException {
        connection.getLock().lock();
        try {
            final int count = super.getCount(namedQuery, connection);
            return count;
        } finally {
            connection.getLock().unlock();
        }
    }

    /**
     * @see FeatureStore#modifyFeatures(AttributeDescriptor[], Object[], Filter)
     */
    public void modifyFeatures(final AttributeDescriptor[] attributes, final Object[] values,
            final Filter filter) throws IOException {
        final ArcSDEPooledConnection connection = getConnection();
        connection.getLock().lock();
        try {
            final String typeName = typeInfo.getFeatureTypeName();
            final Transaction transaction = getTransaction();
            final FeatureWriter writer = dataStore.getFeatureWriter(typeName, filter, transaction);

            try {
                SimpleFeature feature;
                while (writer.hasNext()) {
                    feature = writer.next();
                    for (int i = 0; i < values.length; i++) {
                        feature.setAttribute(attributes[i].getLocalName(), values[i]);
                    }
                    writer.write();
                }
            } finally {
                writer.close();
            }
        } finally {
            connection.getLock().unlock();
        }
    }

    /**
     * @see FeatureStore#modifyFeatures(AttributeDescriptor, Object, Filter)
     */
    public final void modifyFeatures(final AttributeDescriptor type, final Object value,
            final Filter filter) throws IOException {
        modifyFeatures(new AttributeDescriptor[] { type, }, new Object[] { value, }, filter);
    }

    /**
     * @see FeatureStore#removeFeatures(Filter)
     */
    public void removeFeatures(final Filter filter) throws IOException {
        final ArcSDEPooledConnection connection = getConnection();
        connection.getLock().lock();
        try {
            final String typeName = typeInfo.getFeatureTypeName();
            // short circuit cut if needed to remove all features
            if (Filter.INCLUDE == filter) {
                truncate(typeName, connection);
                return;
            }
            // just remove some features, go the slow way
            final Transaction transaction = getTransaction();
            final FeatureWriter writer = dataStore.getFeatureWriter(typeName, filter, transaction);
            try {
                while (writer.hasNext()) {
                    writer.next();
                    writer.remove();
                }
            } finally {
                writer.close();
            }
        } finally {
            connection.getLock().unlock();
        }
    }

    /**
     * 
     * @see FeatureStore#setFeatures(FeatureReader)
     */
    public void setFeatures(final FeatureReader reader) throws IOException {
        final SimpleFeatureType readerType = reader.getFeatureType();
        if (!getSchema().equals(readerType)) {
            throw new IllegalArgumentException("Type mismatch: " + readerType);
        }

        final String typeName = typeInfo.getFeatureTypeName();
        final ArcSDEPooledConnection connection = getConnection();
        connection.getLock().lock();
        try {
            // truncate using this connection to apply or not depending on
            // whether a transaction is in progress
            truncate(typeName, connection);
            final FeatureWriter writer = dataStore.getFeatureWriterAppend(typeName, transaction);
            while (reader.hasNext()) {
                SimpleFeature feature = reader.next();
                SimpleFeature newFeature = writer.next();
                newFeature.setAttributes(feature.getAttributes());
                writer.write();
            }
            writer.close();
        } finally {
            try {
                if (!connection.isTransactionActive()) {
                    connection.close();
                }
            } finally {
                connection.getLock().unlock();
            }
        }
    }

    /**
     * Truncates (removes all the features in) the ArcSDE table named
     * <code>typeName</code> by using an SeTable with the provided
     * <code>connection</code>. This means if the connection has a
     * transaction in progress, the truncation takes effect upon commit,
     * otherwise it takes effect immediately.
     * 
     * @param typeName
     * @param connection
     * @throws DataSourceException
     */
    private void truncate(final String typeName, final ArcSDEPooledConnection connection)
            throws IOException {
        final boolean transactionInProgress = connection.isTransactionActive();
        final SeTable table = connection.getTable(typeName);
        if (transactionInProgress) {
            // need to do actual deletes, as SeTable.truncate does not respects
            // transactions and would delete all content
            LOGGER.fine("deleting all table records for " + typeName);
            final FeatureWriter writer = dataStore.getFeatureWriter(typeName, transaction);
            while (writer.hasNext()) {
                writer.next();
                writer.remove();
            }
        } else {
            // we're in auto commit mode, lets truncate the table the fast way
            try {
                LOGGER.fine("truncating table " + typeName);
                table.truncate();
            } catch (SeException e) {
                throw new ArcSdeException("Cannot truncate table " + typeInfo, e);
            }
        }
    }

    /**
     * If current transaction is not auto commit, grabs the connection from the
     * {@link ArcTransactionState#getConnection() transaction state} using the
     * datastore's connection pool as key. Otherwise asks the pool for a new
     * connection.
     */
    @Override
    protected ArcSDEPooledConnection getConnection() throws IOException,
            UnavailableArcSDEConnectionException {
        final Transaction transaction = getTransaction();
        final ArcSDEConnectionPool connectionPool = dataStore.getConnectionPool();
        ArcSDEPooledConnection connection;
        if (Transaction.AUTO_COMMIT.equals(transaction)) {
            connection = connectionPool.getConnection();
        } else {
            final ArcTransactionState state;
            state = (ArcTransactionState) transaction.getState(connectionPool);
            connection = state.getConnection();
        }
        return connection;
    }
}
