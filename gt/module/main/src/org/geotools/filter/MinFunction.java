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
/*
 * MinFunction.java
 *
 * Created on 28 July 2002, 16:03
 */
package org.geotools.filter;

import org.geotools.feature.Feature;
import org.geotools.filter.expression.Expression;
import org.geotools.filter.expression.FunctionExpression;


/**
 * A function that returns the minimum of two arguments.
 *
 * @author James
 * @source $URL$
 * @version $Id$
 * @deprecated - use org.geotools.filter.function.math.MinFunction instead
 */
public class MinFunction extends FunctionExpressionImpl
    implements FunctionExpression {
    /** The first expression to evaluate */
    private Expression expA;

    /** The first expression to evaluate */
    private Expression expB;

    /** The array of expressions to evaluate */
    private Expression[] args;

    /**
     * Creates a new instance of MinFunction
     */
    public MinFunction() {
    }

    /**
     * Returns a value for this expression.
     *
     * @param feature Specified feature to use when returning value.
     *
     * @return Value of the feature object.
     */
    public Object evaluate(Feature feature) {
        double first = ((Number) expA.getValue(feature)).doubleValue();
        double second = ((Number) expB.getValue(feature)).doubleValue();

        return new Double(Math.min(first, second));
    }

    /**
     * Gets the number of arguments that are set.
     *
     * @return the number of args.
     */
    public int getArgCount() {
        return 2;
    }

    /**
     * Gets the name of this function.
     *
     * @return the name of the function.
     */
    public String getName() {
        return "Min";
    }

    /**
     * Sets the arguments to be evaluated by this function.
     *
     * @param args an array of expressions to be evaluated.
     */
    public void setArgs(Expression[] args) {
        expA = args[0];
        expB = args[1];
        this.args = args;
    }

    /**
     * Gets the arguments to be evaluated by this function.
     *
     * @return an array of the args to be evaluated.
     */
    public Expression[] getArgs() {
        return args;
    }

    /**
     * Return this function as a string.
     *
     * @return String representation of this min function.
     */
    public String toString() {
        return "Min( " + expA + ", " + expB + ")";
    }
}
