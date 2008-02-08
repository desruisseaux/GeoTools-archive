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

import java.io.IOException;
import java.util.Set;

import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import org.geotools.feature.FeatureCollection;


/**
 * Provides storage of data for Features.
 *
 * <p>
 * Individual shapefiles, database tables, etc. are modified through this
 * interface.
 * </p>
 *
 * <p>
 * This is a prototype DataSource replacement please see FeatureSource for more
 * information.
 * </p>
 *
 * @author Jody Garnett
 * @author Ray Gallagher
 * @author Rob Hranac, TOPP
 * @author Chris Holmes, TOPP
 * @source $URL$
 * @version $Id$
 */
public interface FeatureStore extends Store<SimpleFeatureType, SimpleFeature>, FeatureSource {
    /**
     * Adds all features from the passed feature collection to the datasource.
     *
     * @param collection The collection of features to add.
     * @return the FeatureIds of the newly added features.
     *
     * @throws IOException if anything goes wrong.
     */
    Set addFeatures(SimpleFeatureCollection collection) throws IOException;

    /**
     * Deletes the all the current Features of this datasource and adds the new
     * collection.  Primarily used as a convenience method for file
     * datasources.
     *
     * @param reader - the collection to be written
     *
     * @throws IOException if there are any datasource errors.
     */
    void setFeatures(FeatureReader reader) throws IOException;
}
