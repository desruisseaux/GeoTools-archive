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
package org.geotools.filter.function.math;


//this code is autogenerated - you shouldnt be modifying it!
import org.geotools.feature.Feature;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FunctionExpressionImpl;
import org.geotools.filter.expression.Expression;
import org.geotools.filter.expression.FunctionExpression;
import org.geotools.filter.expression.LiteralExpression;


public class FilterFunction_toDegrees extends FunctionExpressionImpl
    implements FunctionExpression {
    private Expression[] args; // list of args that this functions needs

    public FilterFunction_toDegrees() {
    }

    public String getName() {
        return "toDegrees";
    }

    public int getArgCount() {
        return 1;
    }

    public void setArgs(Expression[] args) {
        this.args = args;
    }

    public Expression[] getArgs() {
        return args;
    }

    public String toString() {
        String result = "toDegrees(";

        for (int t = 0; t < args.length; t++) {
            result += (args[t] + ",");
        }

        result += ")";

        return result;
    }

    public Object evaluate(Feature feature) {
        double arg0;

        try { //attempt to get value and perform conversion
            arg0 = ((Number) args[0].getValue(feature)).doubleValue();
        } catch (Exception e) // probably a type error
         {
            throw new IllegalArgumentException(
                "Filter Function problem for function toDegrees argument #0 - expected type double");
        }

        return new Double(Math.toDegrees(arg0));
    }
}
