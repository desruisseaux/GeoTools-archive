/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004, Geotools Project Managment Committee (PMC)
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

package org.geotools.data.vpf;

import java.util.HashMap;
import java.util.Vector;

import org.geotools.data.DataSourceException;
import org.geotools.feature.FeatureType;
import org.geotools.feature.SchemaException;

/**
 * VPFSchemaCreator.java
 *
 * Created on 13. april 2004, 14:26
 * @author  <a href="mailto:knuterik@onemap.org">Knut-Erik Johnsen</a>, Project OneMap
 * @deprecated
 */
public class VPFSchemaCreator {
    private static HashMap featureTypes = new HashMap();

    public static FeatureType getSchema(String featuretype)
                                 throws DataSourceException {
        try {
            
            Object type = featureTypes.get(featuretype);

            if (type == null) {
                throw new SchemaException("Schema not found");
            }

            return (FeatureType) type;
        } catch (SchemaException e) {
            e.printStackTrace();
            throw new DataSourceException(featuretype + 
                                          " schema not available", e);
        }
    }

    public static String[] getTypeNames() {
        Vector v = new Vector(featureTypes.keySet());
        String[] tmp = new String[v.size()];

        for (int i = 0; i < tmp.length; i++) {
            tmp[i] = (String) v.elementAt(i);
        }

        return tmp;
    }

    public static void addSchema(FeatureType type, String featurename) {
        featureTypes.put(featurename, type);
    }
}