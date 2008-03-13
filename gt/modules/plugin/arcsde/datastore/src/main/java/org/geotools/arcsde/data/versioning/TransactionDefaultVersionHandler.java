/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2008, Geotools Project Managment Committee (PMC)
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
package org.geotools.arcsde.data.versioning;

import java.io.IOException;

import org.geotools.arcsde.ArcSdeException;
import org.geotools.arcsde.pool.ArcSDEPooledConnection;

import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeObjectId;
import com.esri.sde.sdk.client.SeState;
import com.esri.sde.sdk.client.SeStreamOp;
import com.esri.sde.sdk.client.SeVersion;

/**
 * Handles a versioned table when in transaction mode
 * 
 * @author Gabriel Roldan (TOPP)
 * @version $Id$
 * @since 2.5.x
 * @source $URL$
 */
public class TransactionDefaultVersionHandler implements ArcSdeVersionHandler {

    private final ArcSDEPooledConnection connection;

    private final SeVersion defaultVersion;

    //private SeObjectId initialStateId;

    // private SeVersion thisTransactionVersion;

    private SeState transactionState;

    public TransactionDefaultVersionHandler(final ArcSDEPooledConnection connection) throws IOException {
        this.connection = connection;
        try {
            defaultVersion = new SeVersion(connection, SeVersion.SE_QUALIFIED_DEFAULT_VERSION_NAME);
            defaultVersion.getInfo();
            //initialStateId = defaultVersion.getStateId();
        } catch (SeException e) {
            throw new ArcSdeException(e);
        }
    }

    // private void setUpNewVersionForThisTransaction() throws IOException {
    // try {
    // defaultVersion.getInfo();
    // // initialStateId = defaultVersion.getStateId();
    //
    // thisTransactionVersion = createNewVersionForThisTransaction();
    // // create a new state for this temporal version
    // final SeState parentState = new SeState(connection, thisTransactionVersion.getStateId());
    // if (parentState.isOpen()) {
    // parentState.close();
    // }
    // transactionState = new SeState(connection);
    // transactionState.create(parentState.getId());
    // } catch (SeException e) {
    // throw new ArcSdeException(e);
    // }
    // }
    //
    // /**
    // * Creates a new datbase version for the life-time of the transaction this handler works upon.
    // * This new version should be merged to the default one if necessary (commit), and shall
    // deleted
    // * at either {@link #rollbackEditState()} or {@link #commitEditState()}.
    // *
    // * @return
    // * @throws SeException
    // */
    // private SeVersion createNewVersionForThisTransaction() throws SeException {
    // final String parentVersionName = SeVersion.SE_QUALIFIED_DEFAULT_VERSION_NAME;
    // SeVersion newVersion = new SeVersion(connection, parentVersionName);
    // newVersion.setParentName(parentVersionName);
    // newVersion.setName("GeoTools Transaction");
    // newVersion
    // .setDescription("Temporal version created by GeoTools during a transaction lifetime");
    // final boolean dbCreatesUniqueName = true;
    // newVersion.create(dbCreatesUniqueName, defaultVersion);
    // return newVersion;
    // }

    /**
     * Called by ArcSdeFeatureWriter.createStream
     * 
     * @see ArcSdeVersionHandler#
     */
    public void setUpStream(final SeConnection connection, SeStreamOp streamOperation)
            throws IOException {
        if (transactionState == null) {
            try {
                defaultVersion.getInfo();
                SeState parentState = new SeState(connection, defaultVersion.getStateId());
                if (parentState.isOpen()) {
                    parentState.close();
                }
                transactionState = new SeState(connection);
                transactionState.create(parentState.getId());
            } catch (SeException e) {
                throw new ArcSdeException(e);
            }
        }
        final SeObjectId differencesId = new SeObjectId(SeState.SE_NULL_STATE_ID);
        final SeObjectId currentStateId = transactionState.getId();
        try {
            streamOperation.setState(currentStateId, differencesId, SeState.SE_STATE_DIFF_NOCHECK);
        } catch (SeException e) {
            throw new ArcSdeException(e);
        }
    }

    /**
     * Not called at all
     * 
     * @see ArcSdeVersionHandler#editOperationWritten(SeStreamOp)
     */
    public void editOperationWritten(SeStreamOp editOperation) throws IOException {
        // intentionally blank
    }

    /**
     * Not called at all
     * 
     * @see ArcSdeVersionHandler#editOperationFailed(SeStreamOp)
     */
    public void editOperationFailed(SeStreamOp editOperation) throws IOException {
        // intentionally blank
    }

    /**
     * Called by ArcTransactionState.commit()
     * 
     * @see ArcSdeVersionHandler#commitEditState()
     */
    public void commitEditState() throws IOException {
        if (transactionState == null) {
            return;
        }
        try {
            SeObjectId transactionStateId = transactionState.getId();
            defaultVersion.changeState(transactionStateId);
            //transactionState.trimTree(initialStateId, transactionStateId);
            transactionState = null;
        } catch (SeException e) {
            throw new ArcSdeException(e);
        }
    }

    /**
     * Called by ArcTransactionState.rollback()
     * 
     * @see ArcSdeVersionHandler#rollbackEditState()
     */
    public void rollbackEditState() throws IOException {
        if (transactionState == null) {
            return;
        }
        try {
            SeObjectId parentStateId = transactionState.getParentId();
            SeState parentState = new SeState(connection, parentStateId);
            transactionState.delete();
            transactionState = null;
            // restore parent state
            if (!parentState.isOpen()) {
                parentState.open();
            }
        } catch (SeException e) {
            throw new ArcSdeException(e);
        }
    }

}
