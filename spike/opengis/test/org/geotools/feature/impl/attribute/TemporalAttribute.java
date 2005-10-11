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

import java.util.Date;

import org.geotools.feature.impl.AttributeImpl;
import org.geotools.filter.Filter;
import org.opengis.feature.type.AttributeType;

/**
 * A Default class that represents a Temporal attribute.
 */
public class TemporalAttribute extends AttributeImpl {
    // this might be right, maybe not, but anyway, its a default formatting
    static java.text.DateFormat format = java.text.DateFormat.getInstance();

    public TemporalAttribute(AttributeType type) {
        super(type);
    }

    public TemporalAttribute(AttributeType type, Object value) {
        super(null, type, value);
    }

    public Object parse(Object value) throws IllegalArgumentException {
        if (value == null) {
            return value;
        }
        
        Class type = super.TYPE.getBinding();

        if (type.isAssignableFrom(value.getClass())) {
            return value;
        }

        if (value instanceof Number) {
            return new Date(((Number) value).longValue());
        }

        if (value instanceof java.util.Calendar) {
            return ((java.util.Calendar) value).getTime();
        }

        try {
            return format.parse(value.toString());
        } catch (java.text.ParseException pe) {
            throw new IllegalArgumentException("unable to parse " + value
                + " as Date");
        }
    }

}
