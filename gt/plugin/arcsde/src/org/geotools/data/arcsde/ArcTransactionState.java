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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.Transaction;

import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeException;

/**
 * Externalizes transactional state for <code>ArcSDEFeatureWriter</code>
 * instances.
 * 
 * @author Jake Fear
 * @source $URL$
 * @version $Id$
 */
class ArcTransactionState implements Transaction.State {
	private static final Logger LOGGER = Logger
			.getLogger(ArcTransactionState.class.getPackage().getName());

	private SeConnection connection;

	private ArcSDEDataStore dataStore;

	/**
	 * Creates a new ArcTransactionState object.
	 * 
	 * @param store
	 *            DOCUMENT ME!
	 */
	public ArcTransactionState(ArcSDEDataStore store) {
		this.dataStore = store;
	}

	/**
	 * 
	 */
	public void commit() throws IOException {
		try {
			if (connection != null) {
				connection.commitTransaction();
			}
		} catch (SeException se) {
			LOGGER.log(Level.WARNING, se.getMessage(), se);
			throw new IOException(se.getMessage());
		}
	}

	/**
	 * 
	 */
	public void rollback() throws IOException {
		try {
			if (connection != null) {
				connection.rollbackTransaction();
			}
		} catch (SeException se) {
			LOGGER.log(Level.WARNING, se.getMessage(), se);
			throw new IOException(se.getMessage());
		}
	}

	/**
	 * 
	 */
	public void addAuthorization(String authId) {
		// intentionally blank
	}

	/**
	 * 
	 */
	public void setTransaction(Transaction transaction) {
		if ((transaction == null) && (this.connection != null)) {
			this.dataStore.getConnectionPool().release(this.connection);
		} else if (transaction != null) {
			try {
				connection = dataStore.getConnectionPool().getConnection();
				connection.setTransactionAutoCommit(0);
				connection.startTransaction();
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, e.getMessage(), e);
				throw new RuntimeException(e.getMessage());
			}
		} else {
			throw new RuntimeException("Unexpected state.");
		}
	}

	/**
	 * Used only within the package to provide access to a single connection on
	 * which this transaction is being conducted.
	 * 
	 * @return DOCUMENT ME!
	 */
	SeConnection getConnection() {
		return connection;
	}
}
