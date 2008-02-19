package org.geotools.arcsde.data;

import java.io.IOException;
import java.util.NoSuchElementException;

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

    public AutoCommitFeatureWriter(final FIDReader fidReader, final SimpleFeatureType featureType,
            final  FeatureReader<SimpleFeatureType, SimpleFeature> filteredContent, final ArcSDEPooledConnection connection,
            FeatureListenerManager listenerManager) throws NoSuchElementException, IOException {

        super(fidReader, featureType, filteredContent, connection, listenerManager);
    }

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
