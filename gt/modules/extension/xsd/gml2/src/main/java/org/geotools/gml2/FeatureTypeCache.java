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
package org.geotools.gml2;

import java.util.HashMap;
import org.geotools.feature.FeatureType;


public class FeatureTypeCache {
    HashMap map = new HashMap();

    public FeatureType get(String name) {
        synchronized (this) {
            return (FeatureType) map.get(name);
        }
    }

    public void put(FeatureType type) {
        if (type.getTypeName() == null) {
            throw new IllegalArgumentException("Type name must be non null");
        }

        synchronized (this) {
            if (map.get(type.getTypeName()) != null) {
                FeatureType other = (FeatureType) map.get(type.getTypeName());

                if (!other.equals(type)) {
                    String msg = "Type with same name already exists in cache.";
                    throw new IllegalArgumentException(msg);
                }

                return;
            }

            map.put(type.getTypeName(), type);
        }
    }
}
