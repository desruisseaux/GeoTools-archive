package org.geotools.data.jdbc;

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
    
    public JDBCTransactionState( JDBCFeatureStore featureSource ) {
        this.featureSource = featureSource;
    }
    
    public void setTransaction(Transaction tx) {
        if ( tx == null && this.tx != null ) {
            //close the connection
            try {
                synchronized ( featureSource ) {
                    featureSource.setTransaction( this.tx );
                    if ( featureSource.getState().getConnection() != null ) {
                        featureSource.getState().getConnection().close();
                    }
                }
            } 
            catch (SQLException e) {
                throw new RuntimeException( e );
            }
        }
        this.tx = tx;
    }
    
    public void addAuthorization(String AuthID) throws IOException {
    }

    public void commit() throws IOException {
        synchronized ( featureSource ) {
            featureSource.setTransaction(tx);
            try {
                featureSource.getState().getConnection().commit();
            } 
            catch (SQLException e) {
                String msg = "Error occured on commit";
                throw (IOException)new IOException(msg).initCause( e );
            }
        }
    }

    public void rollback() throws IOException {
        synchronized ( featureSource ) {
            featureSource.setTransaction(tx);
            try {
                featureSource.getState().getConnection().rollback();
            } 
            catch (SQLException e) {
                String msg = "Error occured on rollback";
                throw (IOException)new IOException(msg).initCause( e );
            }
        }
    }
}
