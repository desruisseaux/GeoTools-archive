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
import java.sql.SQLException;
import org.geotools.data.Transaction;
import org.geotools.data.Transaction.State;


public final class JDBCTransactionState implements State {
    /**
     * the transaction
     */
    Transaction tx;

    /**
     * the datastore
     *
     */
    JDBCFeatureStore featureSource;

    public JDBCTransactionState(JDBCFeatureStore featureSource) {
        this.featureSource = featureSource;
    }

    public void setTransaction(Transaction tx) {
        if ((tx == null) && (this.tx != null)) {
            //            //close the connection
            //            try {
            //                synchronized ( featureSource ) {
            //                    featureSource.setTransaction( this.tx );
            //                    if ( featureSource.getState().getConnection() != null ) {
            //                        featureSource.getState().getConnection().close();
            //                    }
            //                }
            //            } 
            //            catch (SQLException e) {
            //                throw new RuntimeException( e );
            //            }
        }

        this.tx = tx;
    }

    public void addAuthorization(String AuthID) throws IOException {
    }

    public void commit() throws IOException {
        synchronized (featureSource) {
            featureSource.setTransaction(tx);

            try {
                featureSource.getState().getConnection().commit();
            } catch (SQLException e) {
                String msg = "Error occured on commit";
                throw (IOException) new IOException(msg).initCause(e);
            }
        }
    }

    public void rollback() throws IOException {
        synchronized (featureSource) {
            featureSource.setTransaction(tx);

            try {
                featureSource.getState().getConnection().rollback();
            } catch (SQLException e) {
                String msg = "Error occured on rollback";
                throw (IOException) new IOException(msg).initCause(e);
            }
        }
    }
}
