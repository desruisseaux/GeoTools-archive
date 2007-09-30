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
import org.opengis.feature.simple.SimpleFeatureType;


public class FeatureTypeCache {
    HashMap<String,SimpleFeatureType> map =
        new HashMap<String,SimpleFeatureType>();

    public SimpleFeatureType get(String name) {
        synchronized (this) {
            return (SimpleFeatureType) map.get(name);
        }
    }

    public void put(SimpleFeatureType type) {
        if (type.getTypeName() == null) {
            throw new IllegalArgumentException("Type name must be non null");
        }

        synchronized (this) {
            if (map.get(type.getTypeName()) != null) {
                SimpleFeatureType other = map.get(type.getTypeName());
                
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
