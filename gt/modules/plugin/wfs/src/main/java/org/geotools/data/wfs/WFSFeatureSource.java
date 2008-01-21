package org.geotools.data.wfs;

import org.geotools.data.FeatureSource;
import org.geotools.data.ResourceInfo;

/**
 * Superinterface for FeatureSources returned by a WFSDataStore.
 * <p>
 * This interface is meant to be short-lived while waiting for the addition of a {@code getInfo()}
 * method to the core FeatureSource interface.
 * </p>
 * @author Gabriel Roldan
 * @version $Id$
 * @since 2.5.x
 * @URL $URL$
 */
public interface WFSFeatureSource extends FeatureSource {

    ResourceInfo getInfo();
}
