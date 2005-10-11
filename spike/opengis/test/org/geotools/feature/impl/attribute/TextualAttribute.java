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
package org.geotools.feature.impl.attribute;

import org.geotools.feature.impl.AttributeImpl;
import org.opengis.feature.type.AttributeType;


public class TextualAttribute extends AttributeImpl{
	
    public TextualAttribute(AttributeType type) {
        super(type);
    }

    public TextualAttribute(AttributeType type, Object value) {
        super(null, type, value);
    }

    public Object parse(Object value) throws IllegalArgumentException {
        if (value == null) {
            return value;
        }

        // string is immutable, so lets keep it
        if (value instanceof String) {
            return value;
        }

        // other char sequences are not mutable, create a String from it.
        // this also covers any other cases...
        return value.toString();
    }

}
