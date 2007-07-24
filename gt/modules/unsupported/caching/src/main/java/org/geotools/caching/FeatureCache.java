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
package org.geotools.caching;

import com.vividsolutions.jts.geom.Envelope;
import org.geotools.data.FeatureStore;
import org.geotools.feature.FeatureCollection;


public interface FeatureCache extends FeatureStore {
    public void clear();

    public void put(FeatureCollection fc, Envelope e);

    public FeatureCollection get(Envelope e);

    public FeatureCollection peek(Envelope e);

    public void remove(Envelope e);
}
