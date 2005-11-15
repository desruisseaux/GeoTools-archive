/*
 * Created on 15-Mar-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data;

import java.io.IOException;
import java.util.Set;

/**
 * This is used to represent the absense of a Transaction and the use of
 * AutoCommit.
 * 
 * <p>
 * This class serves as the implementation of the constant Transaction.NONE.
 * </p>
 *
 * @author jgarnett
 * @author dzwiers -- just split it into it's own file
 */

class AutoCommitTransaction implements Transaction {
    /**
     * Authorization IDs are not stored by AutoCommit.
     * 
     * <p>
     * Authorization IDs are only stored for the duration of a Transaction.
     * </p>
     *
     * @return Set of authorizations
     *
     * @throws UnsupportedOperationException AUTO_COMMIT does not support this
     */
    public Set getAuthorizations() {
        throw new UnsupportedOperationException(
            "Authorization IDs are not valid for AutoCommit Transaction");
    }

    /**
     * AutoCommit does not save State.
     * 
     * <p>
     * While symetry would be good, state should be commited not stored for
     * later.
     * </p>
     *
     * @param key Key that is not used to Store State
     * @param state State we are not going to externalize
     *
     * @throws UnsupportedOperationException AutoCommit does not support State
     */
    public void putState(Object key, State state) {
        throw new UnsupportedOperationException(
            "AutoCommit does not support the putState opperations");
    }

    /**
     * AutoCommit does not save State.
     * 
     * <p>
     * While symetry would be good, state should be commited not stored for
     * later.
     * </p>
     *
     * @param key Key that is not used to Store State
     *
     * @throws UnsupportedOperationException AutoCommit does not support State
     */
    public void removeState(Object key) {
        throw new UnsupportedOperationException(
            "AutoCommit does not support the removeState opperations");
    }

    /**
     * I am not sure should AutoCommit be able to save sate?
     * 
     * <p>
     * While symetry would be good, state should be commited not stored for
     * later.
     * </p>
     *
     * @param key Key used to retrieve State
     *
     * @return State earlier provided with putState
     *
     * @throws UnsupportedOperationException As Autocommit does not support
     *         State
     */
    public State getState(Object key) {
        throw new UnsupportedOperationException(
            "AutoCommit does not support the getState opperations");
    }

    /**
     * Implemented as a NOP since this Transaction always commits.
     * 
     * <p>
     * This allows the following workflow:
     * </p>
     * <pre>
     * <code>
     * Transaction t = roads.getTransaction();
     * try{
     *     roads.addFeatures( features );
     *     roads.getTransaction().commit();
     * }
     * catch( IOException erp ){
     *     //something went wrong;
     *     roads.getTransaction().rollback();
     * }
     * </code>
     * </pre>
     *
     * @throws IOException If commit fails
     */
    public void commit(){
        // implement a NOP
    }

    /**
     * Implements a NOP since AUTO_COMMIT does not maintain State.
     */
    public void close(){
        // no state to clean up after
    }
    /**
     * Auto commit mode cannot support the rollback opperation.
     *
     * @throws IOException if Rollback fails
     */
    public void rollback() throws IOException {
        throw new IOException(
            "AutoCommit cannot support the rollback opperation");
    }

    /**
     * Authorization IDs are not stored by AutoCommit.
     * 
     * <p>
     * Authorization IDs are only stored for the duration of a Transaction.
     * </p>
     *
     * @param authID Authorization ID
     *
     * @throws IOException If set authorization fails
     */
    public void addAuthorization(String authID) throws IOException {
        throw new IOException(
            "Authorization IDs are not valid for AutoCommit Transaction");
    }

    /**
     * AutoCommit does not save State.
     * 
     * <p>
     * While symetry would be good, state should be commited not stored for
     * later.
     * </p>
     *
     * @param key Key that is not used to Store Property
     * @throws UnsupportedOperationException AutoCommit does not support State
     */
     public Object getProperty(Object key) {
         throw new UnsupportedOperationException(
             "AutoCommit does not support the getProperty opperations");
    }

    /**
     * Implementation of addProperty.
     * 
     * @see org.geotools.data.Transaction#addProperty(java.lang.Object, java.lang.Object)
     * 
     * @param key
     * @param value
     * @throws IOException
     */
    public void putProperty(Object key, Object value) {
        throw new UnsupportedOperationException(
            "AutoCommit does not support the addProperty opperations");        
    }
}
