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
package org.geotools.data;

import java.awt.RenderingHints;
import java.io.IOException;
import java.util.Set;

import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.util.ProgressListener;


/**
 * Highlevel API for Features from a specific location.
 *
 * <p>
 * Individual Shapefiles, databases tables , etc. are referenced through this
 * interface. Compare and constrast with DataStore.
 * </p>
 *
 * <p>
 * Differences from DataStore:
 * </p>
 *
 * <ul>
 * <li>
 * This is a prototype DataSource replacement, the Transaction methods have
 * been moved to an external object, and the locking api has been intergrated.
 * </li>
 * <li>
 * FeatureCollection has been replaced with FeatureResult as we do not wish to
 * indicate that results can be stored in memory.
 * </li>
 * <li>
 * FeatureSource has been split into three interfaces, the intention is to use
 * the instanceof opperator to check capabilities rather than the previous
 * DataSourceMetaData.
 * </li>
 * </ul>
 *
 *
 * @author Jody Garnett
 * @author Ray Gallagher
 * @author Rob Hranac, TOPP
 * @author Chris Holmes, TOPP
 * @source $URL$
 * @version $Id$
 */
public interface FeatureSource extends Source<SimpleFeatureType, SimpleFeature> {

    DataStore getDataStore();

    SimpleFeatureCollection getFeatures(Query query) throws IOException;

    SimpleFeatureCollection getFeatures(Filter filter) throws IOException;

    SimpleFeatureCollection getFeatures() throws IOException;

    SimpleFeatureType getSchema();
}
