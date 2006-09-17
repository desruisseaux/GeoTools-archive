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
import org.geotools.filter.Expression;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FunctionExpression;
import org.geotools.filter.FunctionExpressionImpl;
import org.geotools.filter.LiteralExpression;


public class FilterFunction_min_2 extends FunctionExpressionImpl
    implements FunctionExpression {
    private Expression[] args; // list of args that this functions needs

    public FilterFunction_min_2() {
    }

    public String getName() {
        return "min_2";
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
        String result = "min_2(";

        for (int t = 0; t < args.length; t++) {
            result += (args[t] + ",");
        }

        result += ")";

        return result;
    }

    public Object evaluate(Feature feature) {
        float arg0;
        float arg1;

        try { //attempt to get value and perform conversion
            arg0 = ((Number) args[0].getValue(feature)).floatValue();
        } catch (Exception e) // probably a type error
         {
            throw new IllegalArgumentException(
                "Filter Function problem for function min argument #0 - expected type float");
        }

        try { //attempt to get value and perform conversion
            arg1 = ((Number) args[1].getValue(feature)).floatValue();
        } catch (Exception e) // probably a type error
         {
            throw new IllegalArgumentException(
                "Filter Function problem for function min argument #1 - expected type float");
        }

        return new Float(Math.min(arg0, arg1));
    }
}
