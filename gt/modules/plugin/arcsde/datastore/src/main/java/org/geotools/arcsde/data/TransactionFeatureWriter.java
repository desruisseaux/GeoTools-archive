package org.geotools.arcsde.data;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.geotools.arcsde.pool.ArcSDEConnectionPool;
import org.geotools.arcsde.pool.ArcSDEPooledConnection;
import org.geotools.arcsde.pool.UnavailableArcSDEConnectionException;
import org.geotools.data.DataSourceException;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * A FeatureWriter aware of transactions.
 * 
 * @author Gabriel Roldan (TOPP)
 * @version $Id: TransactionFeatureWriter.java 28045 2007-11-25 22:56:34Z
 *          groldan $
 * @since 2.5
 * @URL $URL:
 *      http://svn.geotools.org/geotools/trunk/gt/modules/plugin/arcsde/datastore/src/main/java/org/geotools/arcsde/data/TransactionFeatureWriter.java $
 */
class TransactionFeatureWriter extends AutoCommitFeatureWriter {

    private ArcTransactionState state;

    /**
     * 
     * @param featureType
     * @param filteredContent
     * @param transactionalConnection
     *            the {@link ArcSDEPooledConnection} to work over, with a
     *            {@link ArcSDEPooledConnection#isTransactionActive() transaction active}
     * @param transaction
     *            a transaction <b>already configured</b> with the
     *            {@link ArcTransactionState} needed for this writer to work.
     * @throws NoSuchElementException
     * @throws IOException
     */
    public TransactionFeatureWriter(final SimpleFeatureType featureType,
            final FeatureReader filteredContent, final ArcTransactionState state)
            throws NoSuchElementException, IOException {

        super(featureType, filteredContent, state.getConnection());
        this.state = state;
        assert state.getConnection().isTransactionActive();
    }

    @Override
    protected ArcSDEPooledConnection getConnection() throws DataSourceException {
        ArcSDEPooledConnection connection;
        try {
            connection = state.getConnection();
        } catch (UnavailableArcSDEConnectionException e) {
            throw new DataSourceException("No connection available to initiate transaction", e);
        }
        return connection;
    }

    /**
     * Overrides to not close the connection as it's the transaction
     * responsibility.
     * 
     * @see FeatureWriter#close()
     */
    @Override
    public void close() throws IOException {
        if (filteredContent != null) {
            filteredContent.close();
            filteredContent = null;
        }
    }
}
