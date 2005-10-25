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
package org.geotools.feature.visitor;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.filter.Expression;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.IllegalFilterException;


/**
 * Calculates the minimum value of an attribute.
 *
 * @author Cory Horner, Refractions
 *
 * @since 2.2.M2
 */
public class MaxVisitor implements FeatureCalc {
    private Expression expr;
    Comparable maxvalue;
    Comparable curvalue;
    boolean visited = false;

    public MaxVisitor(int attributeTypeIndex, FeatureType type)
        throws IllegalFilterException {
        FilterFactory factory = FilterFactory.createFilterFactory();
        expr = factory.createAttributeExpression(type,
                type.getAttributeType(attributeTypeIndex).getName());
    }

    public MaxVisitor(String attrName, FeatureType type)
        throws IllegalFilterException {
        FilterFactory factory = FilterFactory.createFilterFactory();
        expr = factory.createAttributeExpression(type,
                type.getAttributeType(attrName).getName());
    }

    public MaxVisitor(Expression expr) throws IllegalFilterException {
        this.expr = expr;
    }

    /**
     * Visitor function, which looks at each feature and finds the maximum.
     *
     * @param feature the feature to be visited
     */
    public void visit(Feature feature) {
        Object attribValue = expr.getValue(feature);

        if (attribValue == null) {
            System.out.println("no one is home");

            return;
        }

        curvalue = (Comparable) attribValue;

        if ((!visited) || (curvalue.compareTo(maxvalue) > 0)) {
            maxvalue = curvalue;
            visited = true;
        }

        // throw new IllegalStateException("Expression is not comparable!");
    }

    /**
     * Get the max value.
     *
     * @return Max value
     *
     * @throws IllegalStateException DOCUMENT ME!
     */
    public Comparable getMax() {
        if (!visited) {
            throw new IllegalStateException(
                "Must visit before max value is ready!");
        }

        return maxvalue;
    }

    public void reset() {
        /**
         * Reset the count and current maximum
         */
        this.visited = false;
        this.maxvalue = new Integer(Integer.MIN_VALUE);
    }

    public Expression getExpression() {
        return expr;
    }

    public CalcResult getResult() {
        if (!visited) {
            throw new IllegalStateException(
                "Must visit before max value is ready!");
        }

        return new MaxResult(maxvalue);
    }

    /**
     * Overwrites the result stored by the visitor. This should only be used by
     * optimizations which will tell the visitor the answer rather than
     * visiting all features.
     * 
     * <p></p>
     * For 'max', the value stored is of type 'Comparable'.
     *
     * @param result
     */
    public void setValue(Object result) {
        visited = true;
        maxvalue = (Comparable) result;
    }

    public static class MaxResult extends AbstractCalcResult {
        private Comparable maxValue;

        public MaxResult(Comparable newMaxValue) {
            maxValue = newMaxValue;
        }

        public Object getValue() {
            Comparable max = (Comparable) maxValue;

            return max;
        }

        public boolean isCompatible(CalcResult targetResults) {
            //list each calculation result which can merge with this type of result
            if (targetResults instanceof MaxResult) {
                return true;
            }

            return false;
        }

        public CalcResult merge(CalcResult resultsToAdd) {
            if (!isCompatible(resultsToAdd)) {
                throw new IllegalArgumentException(
                    "Parameter is not a compatible type");
            }

            if (resultsToAdd instanceof MaxResult) {
                //take the smaller of the 2 values
                Comparable toAdd = (Comparable) resultsToAdd.getValue();
                Comparable newMax = maxValue;

                if (newMax.getClass() != toAdd.getClass()) { //2 different data types, therefore convert
                	Class bestClass = CalcUtil.bestClass(new Object[] {toAdd, newMax});
            		if (bestClass != toAdd.getClass())
            			toAdd = (Comparable) CalcUtil.convert(toAdd, bestClass);
            		if (bestClass != newMax.getClass())
            			newMax = (Comparable) CalcUtil.convert(newMax, bestClass);
                }
                if (newMax.compareTo(toAdd) < 0) {
                    newMax = toAdd;
                }

                return new MaxResult(newMax);
            } else {
                throw new IllegalArgumentException(
                    "The CalcResults claim to be compatible, but the appropriate merge method has not been implemented.");
            }
        }
    }

    //    private Comparable bumpType(Comparable var1, Comparable var2) {
    //    	Class class1 = var1.getClass();
    //		Class class2 = var2.getClass();
    //		if (class1 != class2) {
    //    		//find bigger class
    //    		if (class2 == Double.class) {
    //    			var1 = (Comparable) new Double(var1.toString());
    //    		} else if (class1 == Float.class) {
    //    			var2 = (Comparable) new Float(var2.toString());
    //    		} else if (class2 == Float.class) {
    //    			var1 = (Comparable) new Float(var1.toString());
    //    		} else if (class1 == Long.class) {
    //    			var2 = (Comparable) new Long(var2.toString());
    //    		} else if (class2 == Long.class) {
    //    			var1 = (Comparable) new Long(var1.toString());
    //    		} else if (class1 == Integer.class) {
    //    			var2 = (Comparable) new Integer(var2.toString());
    //    		} else if (class2 == Integer.class) {
    //    			var1 = (Comparable) new Integer(var1.toString());
    //    		}
    //    	}
    //		return var1;
    //    }
}
