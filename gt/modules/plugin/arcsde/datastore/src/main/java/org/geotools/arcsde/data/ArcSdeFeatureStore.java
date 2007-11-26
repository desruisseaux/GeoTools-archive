package org.geotools.arcsde.data;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.geotools.arcsde.pool.ArcSDEConnectionPool;
import org.geotools.arcsde.pool.ArcSDEPooledConnection;
import org.geotools.arcsde.pool.UnavailableArcSDEConnectionException;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultFeatureResults;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureStore;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;

import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeTable;

public class ArcSdeFeatureStore extends ArcSdeFeatureSource implements FeatureStore {

    public ArcSdeFeatureStore(SimpleFeatureType featureType, ArcSDEDataStore arcSDEDataStore) {
        super(featureType, arcSDEDataStore);
    }

    /**
     * @see FeatureStore#getTransaction()
     */
    public Transaction getTransaction() {
        return transaction;
    }

    /**
     * @see FeatureStore#setTransaction(Transaction)
     */
    public void setTransaction(final Transaction transaction) {
        if (transaction == null) {
            throw new NullPointerException("mean Transaction.AUTO_COMMIT?");
        }
        super.transaction = transaction;
    }

    /**
     * Overrides {@link ArcSdeFeatureSource#getFeatures(Query)} to return a
     * transaction aware collection.
     * 
     * @see ArcSDEDataStore#getFeatureReader(Query, Transaction)
     */
    @Override
    public FeatureCollection getFeatures(Query query) throws IOException {
        final ArcSDEDataStore ds = (ArcSDEDataStore) getDataStore();
        final Transaction transaction = getTransaction();
        final FeatureReader featureReader = ds.getFeatureReader(query, transaction);
        final FeatureCollection collection = DataUtilities.collection(featureReader);
        return collection;
    }

    /**
     * @see FeatureStore#addFeatures(FeatureCollection)
     */
    public Set<String> addFeatures(final FeatureCollection collection) throws IOException {
        final String typeName = featureType.getTypeName();
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
        } finally {
            iterator.close();
            writer.close();
        }
        return featureIds;
    }

    /**
     * @see FeatureStore#modifyFeatures(AttributeDescriptor[], Object[], Filter)
     */
    public void modifyFeatures(final AttributeDescriptor[] attributes, final Object[] values,
            final Filter filter) throws IOException {
        final String typeName = featureType.getTypeName();
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
    }

    /**
     * @see FeatureStore#modifyFeatures(AttributeDescriptor, Object, Filter)
     */
    public void modifyFeatures(final AttributeDescriptor type, final Object value,
            final Filter filter) throws IOException {
        modifyFeatures(new AttributeDescriptor[] { type, }, new Object[] { value, }, filter);
    }

    /**
     * @see FeatureStore#removeFeatures(Filter)
     */
    public void removeFeatures(final Filter filter) throws IOException {
        final ArcSDEPooledConnection connection = getConnection();
        final String typeName = featureType.getTypeName();
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
    }

    /**
     * 
     * @see FeatureStore#setFeatures(FeatureReader)
     */
    public void setFeatures(final FeatureReader reader) throws IOException {
        final String typeName = featureType.getTypeName();
        final ArcSDEPooledConnection connection = getConnection();
        final boolean transactionInProgress = transactionInProgress();
        try {
            // truncate using this connection to apply or not depending on
            // whether a transaction is in progress
            truncate(typeName, connection);
        } finally {
            if (!transactionInProgress) {
                connection.close();
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
            throws DataSourceException {
        try {
            // whether this takes effect immediatelly or not depends
            // on the connection being in autocommit mode or not
            final SeTable table = new SeTable(connection, typeName);
            table.truncate();
        } catch (SeException e) {
            throw new DataSourceException("Cannot truncate table " + featureType, e);
        }
    }

    /**
     * Whether the feature store has a non auto commit transaction set
     */
    private boolean transactionInProgress() {
        return Transaction.AUTO_COMMIT != getTransaction();
    }

    private ArcSDEPooledConnection getConnection() throws IOException,
            UnavailableArcSDEConnectionException {
        final Transaction transaction = getTransaction();
        final ArcSDEConnectionPool connectionPool = dataStore.getConnectionPool();
        ArcSDEPooledConnection connection;
        if (Transaction.AUTO_COMMIT == transaction) {
            connection = connectionPool.getConnection();
        } else {
            final ArcTransactionState state;
            state = ArcTransactionState.getState(transaction, connectionPool);
            connection = state.getConnection();
        }
        return connection;
    }
}
