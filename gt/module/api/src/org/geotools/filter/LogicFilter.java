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
package org.geotools.filter;

import org.geotools.feature.Feature;
import org.opengis.filter.BinaryLogicOperator;
import java.util.Iterator;


/**
 * Defines a logic filter (the only filter type that contains other filters).
 * This filter holds one or more filters together and relates them logically
 * with an internally defined type (AND, OR, NOT).
 *
 * @author Rob Hranac, TOPP
 * @author Chris Holmes, TOPP
 * @source $URL$
 * @version $Id$
 */
public interface LogicFilter extends Filter, BinaryLogicOperator {
    /**
     * Determines whether the feature matches the appropriate logic
     * relationships.
     *
     * @param feature Specified feature to examine.
     *
     * @return Flag confirming whether or not this feature is inside the
     *         filter.
     *
     * @deprecated use {@link Filter#evaluate(Feature)}.
     */
    boolean contains(Feature feature);

    /**
     * Gets an iterator for the filters held by this logic filter.
     *
     * @return the iterator of the filters.
     */
    Iterator getFilterIterator();

    /**
     * Adds a sub filter to this filter.
     *
     * @param filter Specified filter to add to the sub filter list.
     *
     * @throws IllegalFilterException Does not conform to logic filter
     *         structure
     *
     * @task REVISIT: make all filters immutable.  This should return a new
     *       filter.
     */
    void addFilter(Filter filter) throws IllegalFilterException;
}
