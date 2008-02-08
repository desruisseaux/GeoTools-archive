package org.geotools.data;

import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * This is actually the old geotools FeatureCollection, just named it SimpleFeatureCollection
 * as it be cleaner and I don't actually want to introduce geoapi FeatureCollection as it kind
 * of not fit with the rest of our data api being home made. 
 */
public interface SimpleFeatureCollection extends FeatureCollection {

}
