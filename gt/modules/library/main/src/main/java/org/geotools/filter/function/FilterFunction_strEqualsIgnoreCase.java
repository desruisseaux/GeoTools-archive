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

public class FilterFunction_strEqualsIgnoreCase extends FunctionExpressionImpl
        implements FunctionExpression {

    public FilterFunction_strEqualsIgnoreCase() {
        super("strEqualsIgnoreCase");
    }

    public int getArgCount() {
        return 2;
    }

    public Object evaluate(Object feature) {
        String arg0;
        String arg1;

        try { // attempt to get value and perform conversion
            arg0 = (getExpression(0).evaluate(feature)).toString(); // extra
                                                                    // protection
                                                                    // for
                                                                    // strings
        } catch (Exception e) // probably a type error
        {
            throw new IllegalArgumentException(
                    "Filter Function problem for function strEqualsIgnoreCase argument #0 - expected type String");
        }

        try { // attempt to get value and perform conversion
            arg1 = (getExpression(1).evaluate(feature)).toString(); // extra
                                                                    // protection
                                                                    // for
                                                                    // strings
        } catch (Exception e) // probably a type error
        {
            throw new IllegalArgumentException(
                    "Filter Function problem for function strEqualsIgnoreCase argument #1 - expected type String");
        }

        return new Boolean(StaticGeometry.strEqualsIgnoreCase(arg0, arg1));
    }
}
