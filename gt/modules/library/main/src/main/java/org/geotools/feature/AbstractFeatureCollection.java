/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.feature;

import org.geotools.feature.collection.AbstractResourceCollection;
import org.geotools.feature.collection.DelegateFeatureIterator;

/**
 * Helper methods to get us started on the implementation road
 * for FeatureCollections.
 * <p>
 * Most of the origional content of this class has moved to AbstractResourceCollection and/or
 * FeatureDelegate.
 * </p>
 * @deprecated Unused, moved to org.geotools.feature.collection
 * @author Jody Garnett, Refractions Research, Inc.
 * @since 2.1.RC0
 * @source $URL$
 */
public abstract class AbstractFeatureCollection extends AbstractResourceCollection implements FeatureCollection {
     
    /** Default implementation based on DelegateFeatureIterator */
    public FeatureIterator features() {
        return new DelegateFeatureIterator(this, iterator() );
    }
    /** will close() the provided FeatureIterator */
    public void close( FeatureIterator close ) {
        if( close == null ) return;
        close.close();
    }    
}
