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
package src.org.geotools.data;

import com.vividsolutions.jts.geom.Envelope;

import org.geotools.data.Query;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import java.io.IOException;

public interface FeatureSource {
    
    FeatureIterator iterator(Query query) throws IOException;
    
    Feature get(String fid);

    FeatureType getSchema();

    Envelope getBounds(Query query) throws IOException;

    int getCount(Query query) throws IOException;
}
