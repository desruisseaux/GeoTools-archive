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
package org.geotools.feature.type;

import org.geotools.feature.DefaultAttributeType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.PrimativeAttributeType;
import org.geotools.filter.Filter;

import java.util.Date;

/**
 * A Default class that represents a Temporal attribute.
 * @source $URL$
 */
public class TemporalAttributeType extends DefaultAttributeType implements PrimativeAttributeType {
    // this might be right, maybe not, but anyway, its a default formatting
    static java.text.DateFormat format = java.text.DateFormat.getInstance();

    public TemporalAttributeType(String name, boolean nillable, int min,
        int max, Object defaultValue, Filter filter) {
        super(name, java.util.Date.class, nillable, min, max, defaultValue);
        this.filter = filter;
    }
    private Filter filter;

    public Object parse(Object value) throws IllegalArgumentException {
        if (value == null) {
            return value;
        }

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

    public Object duplicate(Object o) throws IllegalAttributeException {
        if (o == null) {
            return null;
        }

        if (o instanceof Date) {
            Date d = (Date) o;

            return new Date(d.getTime());
        }

        throw new IllegalAttributeException("Cannot duplicate "
            + o.getClass().getName());
    }

	/* (non-Javadoc)
	 * @see org.geotools.feature.PrimativeAttributeType#getRestriction()
	 */
	public Filter getRestriction() {
		return filter;
	}
}
