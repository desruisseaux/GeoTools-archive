package org.geotools.filter.function;

/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
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

//this code is autogenerated - you shouldnt be modifying it!
import org.geotools.filter.FunctionExpression;
import org.geotools.filter.FunctionExpressionImpl;

import com.vividsolutions.jts.geom.Geometry;

public class FilterFunction_isClosed extends FunctionExpressionImpl implements
        FunctionExpression {

    public FilterFunction_isClosed() {
        super("isClosed");
    }

    public int getArgCount() {
        return 1;
    }

    public Object evaluate(Object feature) {
        Geometry arg0;

        try { // attempt to get value and perform conversion
            arg0 = (Geometry) getExpression(0).evaluate(feature);
        } catch (Exception e) // probably a type error
        {
            throw new IllegalArgumentException(
                    "Filter Function problem for function isClosed argument #0 - expected type Geometry");
        }

        return new Boolean(StaticGeometry.isClosed(arg0));
    }
}
