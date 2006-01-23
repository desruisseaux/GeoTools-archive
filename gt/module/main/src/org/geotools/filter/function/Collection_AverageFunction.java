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
 * Collection_MinFunction.java
 *
 * Created on May 11, 2005, 6:21 PM
 */
package org.geotools.filter.function;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.visitor.AverageVisitor;
import org.geotools.feature.visitor.CalcResult;
import org.geotools.filter.AttributeExpression;
import org.geotools.filter.Expression;
import org.geotools.filter.FunctionExpression;
import org.geotools.filter.FunctionExpressionImpl;
import org.geotools.filter.IllegalFilterException;
import org.geotools.filter.visitor.AbstractFilterVisitor;


/**
 * Calculates the average value of an attribute for a given FeatureCollection
 * and Expression.
 * 
 * @author Cory Horner
 * @since 2.2M2
 * @source $URL$
 */
public class Collection_AverageFunction extends FunctionExpressionImpl
    implements FunctionExpression {
    /** The logger for the filter module. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.filter.function");
    FeatureCollection previousFeatureCollection = null;
    Object average = null;
    Expression expr;

    /**
     * Creates a new instance of Collection_AverageFunction
     */
    public Collection_AverageFunction() {
    }

    public String getName() {
        return "Collection_Average";
    }

    public int getArgCount() {
        return 1;
    }

    /**
     * Calculate average (using FeatureCalc) - only one parameter is used.
     *
     * @param collection collection to calculate the average
     * @param expression Single Expression argument
     *
     * @return An object containing the average value of the attributes
     *
     * @throws IllegalFilterException
     * @throws IOException 
     */
    public static CalcResult calculateAverage(FeatureCollection collection,
        Expression expression) throws IllegalFilterException, IOException {
        AverageVisitor averageVisitor = new AverageVisitor(expression);
        collection.accepts(averageVisitor, null);

        return averageVisitor.getResult();
    }

    /**
     * The provided arguments are evaulated with respect to the
     * FeatureCollection.
     * 
     * <p>
     * For an aggregate function (like average) please use the WFS mandated XPath
     * syntax to refer to featureMember content.
     * </p>
     * 
     * <p>
     * To refer to all 'X': <code>featureMember/asterisk/X</code>
     * </p>
     *
     * @param args DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public void setArgs(Expression[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException(
                "Require a single argument for average");
        }

        expr = args[0];

        // if we see "featureMembers/*/ATTRIBUTE" change to "ATTRIBUTE"
        expr.accept(new AbstractFilterVisitor() {
                public void visit(AttributeExpression expression) {
                    String xpath = expression.getAttributePath();

                    if (xpath.startsWith("featureMembers/*/")) {
                        xpath = xpath.substring(17);
                    } else if (xpath.startsWith("featureMember/*/")) {
                        xpath = xpath.substring(16);
                    }

                    try {
                        expression.setAttributePath(xpath);
                    } catch (IllegalFilterException e) {
                        // ignore
                    }
                }
            });
    }

    public Object getValue(Feature feature) {
		FeatureCollection featureCollection;

		if (feature instanceof FeatureCollection) {
			featureCollection = (FeatureCollection) feature;
		} else {
			featureCollection = feature.getParent();
		}
		if (featureCollection == null) {
			// we don't got no parent
			return new Integer(0); // no features were visited in the making of this answer
		}
		synchronized (featureCollection) {
			if (featureCollection != previousFeatureCollection) {
				previousFeatureCollection = featureCollection;
				average = null;
				try {
					CalcResult result = calculateAverage(featureCollection, expr);
					if (result != null) {
						average = result.getValue();
					}
				} catch (IllegalFilterException e) {
					LOGGER.log(Level.FINER, e.getLocalizedMessage(), e);
				} catch (IOException e) {
					LOGGER.log(Level.FINER, e.getLocalizedMessage(), e);
				}
			}
		}
		return average;
    }

    public void setExpression(Expression e) {
        expr = e;
    }

    /**
     * Should be an xPath of the form: featureMembers/asterisk/NAME
     *
     * @return
     */
    public Expression[] getArgs() {
        Expression[] ret = new Expression[1];
        ret[0] = expr;

        return ret;
    }

    /**
     * Return this function as a string.
     *
     * @return String representation of this average function.
     */
    public String toString() {
        return "Collection_Average(" + expr + ")";
    }
}
