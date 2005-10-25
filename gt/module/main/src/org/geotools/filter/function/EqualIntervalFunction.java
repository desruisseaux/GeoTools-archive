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
package org.geotools.filter.function;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.visitor.MaxVisitor;
import org.geotools.feature.visitor.MinVisitor;
import org.geotools.filter.Expression;
import org.geotools.filter.IllegalFilterException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;


/**
 * DOCUMENT ME!
 *
 * @author jfc173
 */
public class EqualIntervalFunction extends ClassificationFunction {
    double min = 0;
    double max = 0;

    /**
     * Creates a new instance of EqualRangesClassificationFunction
     */
    public EqualIntervalFunction() {
    }

    public String getName() {
        return "EqualInterval";
    }

    private void calculateMinAndMax()
        throws IllegalFilterException, IOException {
        //TODO: don't assume double
        MinVisitor minVisit = new MinVisitor(expr);
        fc.accepts(minVisit);
        min = minVisit.getResult().toDouble();

        MaxVisitor maxVisit = new MaxVisitor(expr);
        fc.accepts(maxVisit);
        max = maxVisit.getResult().toDouble();

        //    	FeatureIterator it = fc.features();
        //        min = Double.POSITIVE_INFINITY;
        //        max = Double.NEGATIVE_INFINITY;
        //        while (it.hasNext()){
        //            Feature f = it.next();
        //            double value = ((Number) expr.getValue(f)).doubleValue();
        //            if (value > max){
        //                max = value;
        //            }
        //            if (value < min){
        //                min = value;
        //            }            
        //        }
    }

    private double calculateSlotWidth() {
        return (max - min) / classNum;
    }

    protected int calculateSlot(double val) {
        if (val >= max) {
            return classNum - 1;
        }

        double slotWidth = calculateSlotWidth();

        return (int) Math.floor((val - min) / slotWidth);
    }

    public Object getValue(Feature feature) {
        FeatureCollection coll = feature.getParent();

        if (coll == null) {
            return null;
        }

        if (!(coll.equals(fc))) {
            fc = coll;

            try {
                calculateMinAndMax();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        int slot = calculateSlot(((Number) expr.getValue(feature)).doubleValue());

        return new Integer(slot);
    }

    public void setExpression(Expression e) {
        super.setExpression(e);

        if (fc != null) {
            try {
                calculateMinAndMax();
            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }

    /**
     * Determines the range (min and max values) for the indexed slot/bin/bucket.
     * 
     * @return a 2 element set containing the min and max values
     */
    public Object getRange(int index) {
        if (fc == null) {
            return null;
        }

        if (min == max) {
            try {
                calculateMinAndMax();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        Set minmax = new HashSet();
        double slotWidth = calculateSlotWidth();
        Double localMin = new Double((index * slotWidth) + min);
        Double localMax = new Double(max - ((classNum - index - 1) * slotWidth));

        if (index != classNum) { //trim the upper range for all except last
            localMax = new Double(localMax.doubleValue() - 0.00000000000001);

            //TODO: fix the upper bound of the interval (don't use -0.000...1)
        }

        minmax.add(localMin);
        minmax.add(localMax);

        return minmax;
    }
}
