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
package org.geotools.arcsde.data;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.geotools.arcsde.data.versioning.ArcSdeVersionHandler;
import org.geotools.arcsde.pool.ArcSDEPooledConnection;
import org.geotools.data.FeatureListenerManager;
import org.geotools.data.FeatureReader;
import org.geotools.data.Transaction;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * A FeatureWriter for auto commit mode.
 * 
 * @author Gabriel Roldan (TOPP)
 * @version $Id$
 * @since 2.5
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/arcsde/datastore/src/main/java/org/geotools/arcsde/data/AutoCommitFeatureWriter.java $
 */
class AutoCommitFeatureWriter extends ArcSdeFeatureWriter {

    public AutoCommitFeatureWriter(final FIDReader fidReader,
                                   final SimpleFeatureType featureType,
                                   final FeatureReader<SimpleFeatureType, SimpleFeature> filteredContent,
                                   final ArcSDEPooledConnection connection,
                                   final FeatureListenerManager listenerManager,
                                   final ArcSdeVersionHandler versionHandler) throws NoSuchElementException,
                                                                             IOException {

        super(fidReader, featureType, filteredContent, connection, listenerManager, versionHandler);
    }

    /**
     * Overrides createStream so if the table is versioned a new edit state is created and attached
     * to the stream object
     */
    // @Override
    // protected SeStreamOp createStream(Class<? extends SeStreamOp> streamType) throws SeException,
    // DataSourceException {
    // final SeStreamOp streamOp = super.createStream(streamType);
    //
    // if (defaultVersion != null) {
    // // the table is versioned
    // // edit default version
    // if (currentState.isOpen()) {
    // LOGGER.info("closing current state and creating new edit state");
    // System.out.println("closing current state and creating new edit state");
    // currentState.close();
    // }
    // final SeObjectId parentStateId = currentState.getId();
    // currentState = new SeState(connection);
    // currentState.create(parentStateId);
    //
    // // set the version state pointer to the laste edit state
    // SeObjectId currentStateId = currentState.getId();
    // System.out
    // .println("Changing default version state from "
    // + defaultVersion.getStateId().longValue() + " to "
    // + currentStateId.longValue());
    // defaultVersion.changeState(currentStateId);
    // ///System.out.println(defaultVersion.getStateId().longValue());
    //
    // SeObjectId differencesId = new SeObjectId(SeState.SE_NULL_STATE_ID);
    // streamOp.setState(currentStateId, differencesId, SeState.SE_STATE_DIFF_NOCHECK);
    // }
    //
    // return streamOp;
    // }
    @Override
    protected void doFireFeaturesAdded(String typeName, ReferencedEnvelope bounds) {
        listenerManager.fireFeaturesAdded(typeName, Transaction.AUTO_COMMIT, bounds, false);
    }

    @Override
    protected void doFireFeaturesChanged(String typeName, ReferencedEnvelope bounds) {
        listenerManager.fireFeaturesChanged(typeName, Transaction.AUTO_COMMIT, bounds, false);
    }

    @Override
    protected void doFireFeaturesRemoved(String typeName, ReferencedEnvelope bounds) {
        listenerManager.fireFeaturesRemoved(typeName, Transaction.AUTO_COMMIT, bounds, false);
    }

}
