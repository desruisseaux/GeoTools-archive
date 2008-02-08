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

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * Provides Feature based locking.
 * 
 * <p>
 * Features from individual shapefiles, database tables, etc. can be protected
 * or reserved from modification through this interface.
 * </p>
 * <p>
 * To use please cast your FeatureSource to this interface.
 * 
 * <pre><code>
 * FeatureSource source = dataStore.getFeatureSource(&quot;roads&quot;);
 * if( source instanceof FeatureLocking ) {
 *     FeatureLocking locking = (FeatureLocking) source;
 *     ...
 * }
 * &#064;author Jody Garnett, Refractions Research, Inc.
 * &#064;author Ray Gallagher
 * &#064;author Rob Hranac, TOPP
 * &#064;author Chris Holmes, TOPP
 * &#064;source $URL$
 * &#064;version $Id$
 * 
 */
public interface FeatureLocking extends FeatureStore, Locking<SimpleFeatureType, SimpleFeature> {

}
