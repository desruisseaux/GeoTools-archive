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
package org.geotools.feature.type;

import org.geotools.feature.Name;


/**
 * Simple implementation of TypeName.
 *
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public class TypeName extends Name implements org.opengis.feature.type.TypeName {
    public TypeName(String namespace, String local) {
        super(namespace, local);
    }

    public TypeName(String local) {
        super(local);
    }

    public boolean equals(Object obj) {
        if (obj instanceof TypeName) {
            return super.equals(obj);
        }

        return false;
    }
}
