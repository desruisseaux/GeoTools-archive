/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
 */
package org.geotools.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.geotools.data.Transaction;
import org.geotools.data.Transaction.State;


public final class JDBCTransactionState implements State {
    /**
     * the current transaction
     */
    Transaction tx;
    /**
     * The current connection
     */
    Connection cx;
    
    public JDBCTransactionState(Connection cx) {
        this.cx = cx;
    }

    public void setTransaction(Transaction tx) {
        if ( tx != null && this.tx != null ) {
            throw new IllegalStateException( "New transaction set without " +
                "closing old transaction first.");
        }
            
        if ( tx == null ) {
            if ( cx != null ) {
                try {
                    cx.close();
                }
                catch( SQLException e ) {
                    //TODO: perhaps we should log this at the finest level
                }
            }
            else {
                JDBCDataStore.LOGGER.warning("Transaction is attempting to " +
                    "close an already closed connection");
            }
            cx = null;
        }
        
        this.tx = tx;
    }

    public void addAuthorization(String AuthID) throws IOException {
    }

    public void commit() throws IOException {
        try {
            cx.commit();
        } catch (SQLException e) {
            String msg = "Error occured on commit";
            throw (IOException) new IOException(msg).initCause(e);
        }
    }

    public void rollback() throws IOException {
        try {
            cx.rollback();
        } catch (SQLException e) {
            String msg = "Error occured on rollback";
            throw (IOException) new IOException(msg).initCause(e);
        }
    }
}
