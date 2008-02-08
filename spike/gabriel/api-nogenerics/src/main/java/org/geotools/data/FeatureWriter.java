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

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * Provides the ability to write Features information.
 * 
 * <p>
 * Capabilities:
 * </p>
 * 
 * <ul>
 * <li> Similar API to FeatureReader </li>
 * <li> After aquiring a feature using next() you may call remove() or after
 * modification write(). If you do not call one of these two methods before
 * calling hasNext(), or next() for that matter, the feature will be left
 * unmodified. </li>
 * <li> This API allows modification, and Filter based modification to be
 * written. Please see AbstractDataStore for examples of implementing common
 * opperations using this API. </li>
 * <li> In order to add new Features, FeatureWriters capable of accepting new
 * content allow next() to be called when hasNext() is <code>false</code> to
 * allow new feature creation. These changes </li>
 * </ul>
 * 
 * <p>
 * One thing that is really nice about the approach to adding content is that
 * the generation of FID is not left in the users control.
 * </p>
 * 
 * @author Ian Schneider
 * @author Jody Garnett, Refractions Research
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/spike/gabriel/api-parametrized-arguments/src/main/java/org/geotools/data/FeatureWriter.java $
 * @version $Id$
 */
public interface FeatureWriter extends Writer {

    SimpleFeatureType getFeatureType();

    SimpleFeature next() throws IOException;
}
