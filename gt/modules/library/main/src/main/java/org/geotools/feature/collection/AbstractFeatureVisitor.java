/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, GeoTools Project Managment Committee (PMC)
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
package org.geotools.feature.collection;

import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.type.FeatureCollectionType;

/**
 * An abstract class to reduce the amount of work needed when working with FeatureVisitor.
 * <p>
 * This class is best used when making anonymous inner classes:
 * 
 * <pre><code>
 * features.accepts(new AbstractFeatureVisitor(){
 *     public void visit( org.opengis.feature.Feature feature ) {
 *         bounds.include(feature.getBounds());
 *     }
 * }, null);
 * </code></pre>
 * 
 * @author Jody Garnett
 */
public abstract class AbstractFeatureVisitor implements FeatureVisitor {
    public void init( FeatureCollectionType collection ) {
    }
}
