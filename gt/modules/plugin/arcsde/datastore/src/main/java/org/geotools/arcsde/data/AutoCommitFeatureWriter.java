package org.geotools.arcsde.data;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.geotools.arcsde.pool.ArcSDEPooledConnection;
import org.geotools.data.FeatureReader;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * A FeatureWriter for auto commit mode.
 * 
 * @author Gabriel Roldan (TOPP)
 * @version $Id$
 * @since 2.5
 * @source $URL:
 *      http://svn.geotools.org/geotools/trunk/gt/modules/plugin/arcsde/datastore/src/main/java/org/geotools/arcsde/data/AutoCommitFeatureWriter.java $
 */
class AutoCommitFeatureWriter extends ArcSdeFeatureWriter {

    public AutoCommitFeatureWriter(final FIDReader fidReader, final SimpleFeatureType featureType,
            final FeatureReader filteredContent, final ArcSDEPooledConnection connection)
            throws NoSuchElementException, IOException {

        super(fidReader, featureType, filteredContent, connection);
    }
}
