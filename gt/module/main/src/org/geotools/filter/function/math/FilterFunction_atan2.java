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
package org.geotools.filter.function.math;


//this code is autogenerated - you shouldnt be modifying it!
import org.geotools.feature.Feature;
import org.geotools.filter.Expression;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FunctionExpression;
import org.geotools.filter.FunctionExpressionImpl;
import org.geotools.filter.LiteralExpression;


public class FilterFunction_atan2 extends FunctionExpressionImpl
    implements FunctionExpression {
    private Expression[] args; // list of args that this functions needs

    public FilterFunction_atan2() {
    }

    public String getName() {
        return "atan2";
    }

    public int getArgCount() {
        return 2;
    }

    public void setArgs(Expression[] args) {
        this.args = args;
    }

    public Expression[] getArgs() {
        return args;
    }

    public String toString() {
        String result = "atan2(";

        for (int t = 0; t < args.length; t++) {
            result += (args[t] + ",");
        }

        result += ")";

        return result;
    }

    public Object evaluate(Feature feature) {
        double arg0;
        double arg1;

        try { //attempt to get value and perform conversion
            arg0 = ((Number) args[0].getValue(feature)).doubleValue();
        } catch (Exception e) // probably a type error
         {
            throw new IllegalArgumentException(
                "Filter Function problem for function atan2 argument #0 - expected type double");
        }

        try { //attempt to get value and perform conversion
            arg1 = ((Number) args[1].getValue(feature)).doubleValue();
        } catch (Exception e) // probably a type error
         {
            throw new IllegalArgumentException(
                "Filter Function problem for function atan2 argument #1 - expected type double");
        }

        return new Double(Math.atan2(arg0, arg1));
    }
}
