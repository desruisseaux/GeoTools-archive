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

import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;


public interface FeatureIndex extends FeatureSource {
    public abstract void add(Feature f);

    public abstract Feature get(String featureID);

    public abstract void remove(String featureID);

    public abstract void flush();

    public abstract void clear();

    public abstract FeatureCollection getFeatures(Query q);

    public abstract FeatureSource getView(Query q);
}
