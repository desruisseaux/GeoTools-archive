package org.geotools.filter.function.math;

/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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
import static org.geotools.filter.capability.FunctionNameImpl.parameter;

import org.geotools.filter.FunctionExpressionImpl;
import org.geotools.filter.capability.FunctionNameImpl;
import org.opengis.filter.capability.FunctionName;

public class FilterFunction_min_4 extends FunctionExpressionImpl {

    //public static FunctionName NAME = new FunctionNameImpl("min_4","int","int");
    public static FunctionName NAME = new FunctionNameImpl("min_4",
            parameter("minium", Integer.class),
            parameter("int",Number.class),
            parameter("int",Number.class));
    public FilterFunction_min_4() {
        super("min_4");
        functionName = NAME;
    }

    public int getArgCount() {
        return 2;
    }

    public Object evaluate(Object feature) {
        int arg0;
        int arg1;

        try { // attempt to get value and perform conversion
            arg0 = (getExpression(0).evaluate(feature,Integer.class)).intValue();
        } catch (Exception e) {
            // probably a type error
            throw new IllegalArgumentException(
                    "Filter Function problem for function min argument #0 - expected type double");
        }

        try { // attempt to get value and perform conversion
            arg1 = (getExpression(1).evaluate(feature, Integer.class)).intValue();
        } catch (Exception e) {
            // probably a type error
            throw new IllegalArgumentException(
                    "Filter Function problem for function min argument #1 - expected type double");
        }

        return new Integer(Math.min(arg0, arg1));
    }
}
