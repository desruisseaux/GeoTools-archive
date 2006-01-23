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
 * @source $URL$
 */
package org.geotools.feature.type;

import org.geotools.feature.DefaultAttributeType;
import org.geotools.feature.PrimativeAttributeType;
import org.geotools.filter.Filter;


public class TextualAttributeType extends DefaultAttributeType implements PrimativeAttributeType {
    public TextualAttributeType(String name, boolean nillable, int min,
        int max, Object defaultValue, Filter filter) {
        super(name, String.class, nillable, min, max, defaultValue);
        this.filter = filter;
    }
    private Filter filter;

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

    /**
     * Duplicate as a String
     *
     * @param o DOCUMENT ME!
     *
     * @return a String obtained by calling toString or null.
     */
    public Object duplicate(Object o) {
        if (o == null) {
            return null;
        }

        return o.toString();
    }

	/* (non-Javadoc)
	 * @see org.geotools.feature.PrimativeAttributeType#getRestriction()
	 */
	public Filter getRestriction() {
		return filter;
	}
}
