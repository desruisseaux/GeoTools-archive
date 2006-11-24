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
import com.vividsolutions.jts.geom.*; //generic JTS support
import org.geotools.filter.function.StaticGeometry; //make sure you include this or you will not be able to call the functions!!

import org.geotools.feature.Feature;
import org.geotools.filter.Expression;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FunctionExpression;
import org.geotools.filter.FunctionExpressionImpl;
import org.geotools.filter.LiteralExpression;

public class FilterFunction_equalsExact extends FunctionExpressionImpl
        implements FunctionExpression {

    public FilterFunction_equalsExact() {
        super("equalsExact");
    }

    public int getArgCount() {
        return 2;
    }

    public Object evaluate(Feature feature) {
        Geometry arg0;
        Geometry arg1;

        try { // attempt to get value and perform conversion
            arg0 = (Geometry) getExpression(0).evaluate(feature);
        } catch (Exception e) // probably a type error
        {
            throw new IllegalArgumentException(
                    "Filter Function problem for function equalsExact argument #0 - expected type Geometry");
        }

        try { // attempt to get value and perform conversion
            arg1 = (Geometry) getExpression(1).evaluate(feature);
        } catch (Exception e) // probably a type error
        {
            throw new IllegalArgumentException(
                    "Filter Function problem for function equalsExact argument #1 - expected type Geometry");
        }

        return new Boolean(StaticGeometry.equalsExact(arg0, arg1));
    }
}
