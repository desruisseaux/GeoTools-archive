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


/*
 * this code is hand modified from autogenerated code
 * generator can be found in
 * http://svn.geotools.org/geotools/trunk/spike/dblasby/FunctionFilterWriter/
 * Modifications made to correct autogeneration that made it so the
 * min numbered args were different than the max numbered args
 * min_2 was a double, max_2 a float, for example.
 */
import org.geotools.feature.Feature;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FunctionExpressionImpl;
import org.geotools.filter.expression.Expression;
import org.geotools.filter.expression.FunctionExpression;
import org.geotools.filter.expression.LiteralExpression;


public class FilterFunction_min_3 extends FunctionExpressionImpl
    implements FunctionExpression {
    private Expression[] args; // list of args that this functions needs

    public FilterFunction_min_3() {
    }

    public String getName() {
        return "min_3";
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
        String result = "min_3(";

        for (int t = 0; t < args.length; t++) {
            result += (args[t] + ",");
        }

        result += ")";

        return result;
    }

    public Object evaluate(Feature feature) {
        int arg0;
        int arg1;

        try { //attempt to get value and perform conversion
            arg0 = ((Number) args[0].getValue(feature)).intValue();
        } catch (Exception e) // probably a type error
         {
            throw new IllegalArgumentException(
                "Filter Function problem for function min argument #0 - expected type int");
        }

        try { //attempt to get value and perform conversion
            arg1 = ((Number) args[1].getValue(feature)).intValue();
        } catch (Exception e) // probably a type error
         {
            throw new IllegalArgumentException(
                "Filter Function problem for function min argument #1 - expected type int");
        }

        return new Integer(Math.min(arg0, arg1));
    }
}
