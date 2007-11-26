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
     * Transactional connection this state works upon, held until commit(),
     * rollback() or close() is called.
     */
    private ArcSDEPooledConnection connection;

    /**
     * Creates a new ArcTransactionState object.
     * 
     * @param pool
     *            connection pool where to grab a connection and hold it while
     *            there's a transaction open (signaled by any use of
     *            {@link #getConnection()}
     */
    private ArcTransactionState(ArcSDEPooledConnection connection) {
        this.connection = connection;
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
            try {
                connection.rollbackTransaction();
            } catch (SeException e) {
                LOGGER.log(Level.WARNING, se.getMessage(), se);
            }
            LOGGER.log(Level.WARNING, se.getMessage(), se);
            throw new IOException(se.getMessage());
        }finally{
            close();
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
        }finally{
            close();
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
        if (connection == null) {
            throw new IllegalStateException("This transaction state has already been closed");
        }
    }

    /**
     * Releases resources and invalidates this state (signaled by setting pool
     * to null)
     */
    private void close() {
        // can't even try to use this state in any way from now on
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
     * 
     * @return connection
     * @throws UnavailableArcSDEConnectionException
     * @throws DataSourceException
     * @throws SeException
     */
    ArcSDEPooledConnection getConnection() throws DataSourceException,
            UnavailableArcSDEConnectionException {
        failIfClosed();
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
            final ArcSDEConnectionPool connectionPool) throws IOException {
        ArcTransactionState state;

        synchronized (ArcTransactionState.class) {
            state = (ArcTransactionState) transaction.getState(connectionPool);

            if (state == null) {
                // start a transaction
                final ArcSDEPooledConnection connection = connectionPool.getConnection();
                try {
                    // do not auto commit
                    connection.setTransactionAutoCommit(0);
                    // and start a transaction
                    connection.startTransaction();
                } catch (SeException e) {
                    try {
                        connection.rollbackTransaction();
                    } catch (SeException ignorableException) {
                        // bah, we're already failing
                    }
                    connection.close();
                    throw new DataSourceException("Exception initiating transaction on "
                            + connection, e);
                }

                state = new ArcTransactionState(connection);
                transaction.putState(connectionPool, state);
            }
        }
        return state;
    }
}
