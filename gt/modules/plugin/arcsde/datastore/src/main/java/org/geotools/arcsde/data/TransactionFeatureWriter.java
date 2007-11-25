package org.geotools.arcsde.data;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.geotools.arcsde.pool.ArcSDEConnectionPool;
import org.geotools.arcsde.pool.ArcSDEPooledConnection;
import org.geotools.arcsde.pool.UnavailableArcSDEConnectionException;
import org.geotools.data.DataSourceException;
import org.geotools.data.FeatureReader;
import org.geotools.data.Transaction;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * A FeatureWriter aware of transactions.
 * 
 * @author Gabriel Roldan (TOPP)
 * @version $Id$
 * @since 2.5
 * @URL $URL$
 */
class TransactionFeatureWriter extends AutoCommitFeatureWriter {

    private Transaction transaction;

    public TransactionFeatureWriter(final SimpleFeatureType featureType,
            final FeatureReader filteredContent, final ArcSDEConnectionPool pool,
            final Transaction transaction) throws NoSuchElementException, IOException {

        super(featureType, filteredContent, pool);
        this.transaction = transaction;
    }

    @Override
    protected ArcSDEPooledConnection getConnection() throws DataSourceException {
        ArcTransactionState state = getTransactionState();
        ArcSDEPooledConnection connection;
        try {
            connection = state.getConnection();
        } catch (UnavailableArcSDEConnectionException e) {
            throw new DataSourceException("No connection available to initiate transaction", e);
        }
        return connection;
    }

    private ArcTransactionState getTransactionState() {
        final ArcSDEConnectionPool pool = getPool();
        ArcTransactionState state = (ArcTransactionState) transaction.getState(pool);
        if (state == null) {
            state = new ArcTransactionState(pool);
            transaction.putState(pool, state);
        }
        return state;
    }
}
