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
import org.geotools.filter.expression.Expression;


/**
 * DOCUMENT ME!
 *
 * @author James
 * @source $URL$
 */
public interface EnvironmentVariable extends Expression {
    /**
     * Gets the attribute value at the path held by this expression from the
     * feature.
     *
     * @param feature the feature to get this attribute from.
     *
     * @return the value of the attribute found by this expression.
     *
     * @deprecated use {@link org.opengis.filter.expression.Expression#evaluate(Object)}
     */
    Object getValue(Feature feature);
}
