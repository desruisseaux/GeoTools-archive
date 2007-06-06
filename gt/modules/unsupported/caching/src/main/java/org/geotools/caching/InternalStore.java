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

import java.util.Collection;
import org.geotools.feature.Feature;


public interface InternalStore {
    public abstract boolean contains(Feature feature);

    public abstract boolean contains(String featureId);

    public abstract void put(Feature f);

    public abstract Feature get(String featureId);

    public abstract Collection getAll();

    public abstract void clear();

    public abstract void remove(String featureId);
}
