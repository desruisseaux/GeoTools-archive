/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.data.feature;

import com.vividsolutions.jts.geom.Envelope;

import java.io.IOException;

import org.geotools.data.Query;

public interface FeatureIndexed extends FeatureStore {

    boolean recompute(FeatureIndexStrategy fis) throws IOException;
    
    FeatureIterator iterator(Query query, Envelope minFeature) throws IOException;
    
    static interface FeatureIndexStrategy {
        // no sure what goes here ... but it's a strategy object
    }
}
