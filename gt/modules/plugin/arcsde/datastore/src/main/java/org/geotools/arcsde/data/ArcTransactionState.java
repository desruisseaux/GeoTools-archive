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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.arcsde.pool.ArcSDEConnectionPool;
import org.geotools.arcsde.pool.ArcSDEPooledConnection;
import org.geotools.arcsde.pool.UnavailableArcSDEConnectionException;
import org.geotools.data.DataSourceException;
import org.geotools.data.Transaction;

import com.esri.sde.sdk.client.SeException;

/**
 * Externalizes transactional state for <code>ArcSDEFeatureWriter</code>
 * instances.
 * 
 * @author Jake Fear
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/arcsde/datastore/src/main/java/org/geotools/arcsde/data/ArcTransactionState.java $
 * @version $Id$
 */
class ArcTransactionState implements Transaction.State {
    private static final Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger(ArcTransactionState.class.getPackage().getName());

    /**
     * Connection lazily grabbed from the pool, and held until commit() or
     * rollback() is called.
     */
    private ArcSDEPooledConnection connection;

    /**
     * Connection pool this state grabs connections from to be held during a
     * transaction lifetime. A value of <code>null</code> indicates close()
     * has been called and thus any other usage attempt shall fail.
     */
    private ArcSDEConnectionPool pool;

    /**
     * Creates a new ArcTransactionState object.
     * 
     * @param pool
     *            connection pool where to grab a connection and hold it while
     *            there's a transaction open (signaled by any use of
     *            {@link #getConnection()}
     */
    public ArcTransactionState(ArcSDEConnectionPool pool) {
        this.pool = pool;
    }

    /**
     * Commits the transaction and returns the connection to the pool. A new one
     * will be grabbed when needed.
     * <p>
     * Preconditions:
     * <ul>
     * <li>{@link #setTransaction(Transaction)} already called with non
     * <code>null</code> argument.
     * <li>
     * </ul>
     * </p>
     */
    public void commit() throws IOException {
        failIfClosed();
        try {
            connection.commitTransaction();
        } catch (SeException se) {
            LOGGER.log(Level.WARNING, se.getMessage(), se);
            throw new IOException(se.getMessage());
        } finally {
            connection.close();
            connection = null;
        }
    }

    /**
     * 
     */
    public void rollback() throws IOException {
        failIfClosed();
        try {
            connection.rollbackTransaction();
        } catch (SeException se) {
            LOGGER.log(Level.WARNING, se.getMessage(), se);
            throw new IOException(se.getMessage());
        } finally {
            connection.close();
            connection = null;
        }
    }

    /**
     * 
     */
    public void addAuthorization(String authId) {
        // intentionally blank
    }

    /**
     * @see Transaction.State#setTransaction(Transaction)
     * @param transaction
     *            transaction information, <code>null</code> signals this
     *            state lifecycle end.
     * @throws IllegalStateException
     *             if close() is called while a transaction is in progress
     */
    public void setTransaction(final Transaction transaction) {
        if (transaction == null) {
            // this is a call to free resources (ugly, but that's what the API
            // says)
            close();
        } else {
            try {
                getConnection();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, e.getMessage(), e);
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    /**
     * If this state has been closed throws an unchecked exception as its
     * clearly a broken workflow.
     * 
     * @throws IllegalStateException
     *             if the transaction state has been closed.
     */
    private void failIfClosed() throws IllegalStateException {
        if (pool == null) {
            throw new IllegalStateException("This transaction state has already been closed");
        }
    }

    /**
     * Releases resources and invalidates this state (signaled by setting pool
     * to null)
     */
    private void close() {
        // can't even try to use this state in any way from now on
        pool = null;
        if (connection != null) {
            // may throw ISE if transaction is still in progress
            try {
                connection.close();
            } catch (IllegalStateException workflowError) {
                // fail fast but put the connection in a healthy state first
                try {
                    connection.rollbackTransaction();
                } catch (SeException e) {
                    // well, it's totally messed up, just log though
                    LOGGER.log(Level.SEVERE, "rolling back connection " + connection, e);
                    connection.close();
                }
                throw workflowError;
            } finally {
                connection = null;
            }
        }
    }

    /**
     * Used only within the package to provide access to a single connection on
     * which this transaction is being conducted.
     * <p>
     * The very call to this method signals a transaction is in progress, and
     * thus if a connection has not been yet grabbed from the pool, one it taken
     * and a transaction is open on the connection.
     * </p>
     * 
     * @return connection
     * @throws UnavailableArcSDEConnectionException
     * @throws DataSourceException
     * @throws SeException
     */
    ArcSDEPooledConnection getConnection() throws DataSourceException,
            UnavailableArcSDEConnectionException {
        failIfClosed();
        if (connection == null) {
            connection = pool.getConnection();
            try {
                // tells ArcSDE to never to auto commit
                connection.setTransactionAutoCommit(0);
                // and start a transaction. Remember no connection can be
                // returned
                // to the pool if it has a transaction in progress
                connection.startTransaction();
            } catch (SeException e) {
                throw new DataSourceException("Cannot initiate transaction on " + connection);
            }
        }
        return connection;
    }

    /**
     * Grab the ArcTransactionState (when not using AUTO_COMMIT).
     * <p>
     * As of GeoTools 2.5 we store the TransactionState using the connection
     * pool as a key.
     * </p>
     * 
     * @param transaction
     *            non autocommit transaction
     * @return the ArcTransactionState stored in the transaction with
     *         <code>connectionPool</code> as key.
     */
    public static ArcTransactionState getState(final Transaction transaction,
            final ArcSDEConnectionPool connectionPool) {
        ArcTransactionState state;

        synchronized (ArcTransactionState.class) {
            state = (ArcTransactionState) transaction.getState(connectionPool);

            if (state == null) {
                // start a transaction
                state = new ArcTransactionState(connectionPool);
                transaction.putState(connectionPool, state);
            }
        }
        return state;
    }
}
