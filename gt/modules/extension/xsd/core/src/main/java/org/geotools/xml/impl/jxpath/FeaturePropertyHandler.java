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
package org.geotools.xml.impl.jxpath;

import org.apache.commons.jxpath.DynamicPropertyHandler;
import java.util.HashSet;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;


/**
 * JXPath property handler for features.
 * <p>
 *
 * </p>
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class FeaturePropertyHandler implements DynamicPropertyHandler {
    public String[] getPropertyNames(Object object) {
        Feature feature = (Feature) object;
        FeatureType featureType = feature.getFeatureType();

        //set is ok because jxpath ignores order
        String[] propertyNames = new String[featureType.getAttributeCount()];

        for (int i = 0; i < propertyNames.length; i++) {
            propertyNames[i] = featureType.getAttributeType(i).getLocalName();
        }

        return propertyNames;
    }

    public Object getProperty(Object object, String property) {
        Feature feature = (Feature) object;
        Object value = feature.getAttribute(property(property));

        if (value != null) {
            return value;
        }

        //check additional properties
        if ("fid".equals(property) || property.matches("(\\w+:)?id")) {
            return feature.getID();
        }

        return null;
    }

    public void setProperty(Object object, String property, Object value) {
        Feature feature = (Feature) object;

        try {
            feature.setAttribute(property(property), value);
        } catch (IllegalAttributeException e) {
            throw new RuntimeException(e);
        }
    }

    String property(String property) {
        //strip of namesapce prefix
        int i = property.indexOf(":");

        if (i != -1) {
            property = property.substring(i + 1);
        }

        return property;
    }
}
