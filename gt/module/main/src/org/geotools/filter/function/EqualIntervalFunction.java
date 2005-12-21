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

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.visitor.MaxVisitor;
import org.geotools.feature.visitor.MinVisitor;
import org.geotools.feature.visitor.UniqueVisitor;
import org.geotools.filter.Expression;
import org.geotools.filter.IllegalFilterException;


/**
 * Classification function for breaking a feature collection into edible chunks
 * of "equal" size.
 * 
 * @author James Macgill
 * @author Cory Horner, Refractions Research
 */
public class EqualIntervalFunction extends ClassificationFunction {
	Comparable globalMin = null;
    Comparable globalMax = null;
    Comparable[] localMin = null;
    Comparable[] localMax = null;
    Object[] values = null;
    boolean isValid = false;
    boolean isNumber = false;
    
    public EqualIntervalFunction() {
    }

    public String getName() {
        return "EqualInterval";
    }

    private void calculateMinAndMax() {
        MinVisitor minVisit;
		try {
			minVisit = new MinVisitor(expr);
			fc.accepts(minVisit, null);
			globalMin = (Comparable) minVisit.getResult().getValue();

			MaxVisitor maxVisit = new MaxVisitor(expr);
			fc.accepts(maxVisit, null);
			globalMax = (Comparable) maxVisit.getResult().getValue();
			
			if (!((globalMin instanceof Number) && (globalMax instanceof Number))) {
				//obtain of list of unique values, so we can enumerate
				UniqueVisitor uniqueVisit = new UniqueVisitor(expr);
				fc.accepts(uniqueVisit, null);
		        List result = uniqueVisit.getResult().toList();
		        //sort the results and put them in an array
		        Collections.sort(result);
				values = result.toArray();
			}
		} catch (IllegalFilterException e) { //accepts exploded
			e.printStackTrace();
			isValid = false;
			return;
		} catch (IOException e) { //getResult().getValue() exploded
			e.printStackTrace();
			isValid = false;
			return;
		}
        
    	//resize arrays
        localMin = new Comparable[classNum];
    	localMax = new Comparable[classNum];

    	//calculate all the little min and max values
    	if ((globalMin instanceof Number) && (globalMax instanceof Number)) {
        	double slotWidth = calculateSlotWidth();
        	isNumber = true;
        	for (int i = 0; i < classNum; i++) {
        		//calculate the min + max values
        		localMin[i] = new Double(((Number) globalMin).doubleValue() + (i * slotWidth));
        		localMax[i] = new Double(((Number) globalMax).doubleValue() - ((classNum - i - 1) * slotWidth));
        		//determine number of decimal places to allow
        		int decPlaces = decimalPlaces(slotWidth);
        		//clean up truncation error
        		if (decPlaces > -1) {
        			localMin[i] = new Double(round(((Number) localMin[i]).doubleValue(), decPlaces));
        			localMax[i] = new Double(round(((Number) localMax[i]).doubleValue(), decPlaces));
        		}
        		//synchronize min with previous max
        		if ((i != 0) && (!localMin[i].equals(localMax[i-1]))) {
        			localMin[i] = localMax[i-1];
        		}
        	}
        } else {
        	isNumber = false;
        	//we have 2 options here:
        	//1. break apart by numeric value: (aaa, aab, aac, bbb) --> [aaa, aab, aac], [bbb]
        	//2. break apart by item count:                         --> [aaa, aab], [aac, bbb]

        	// this code currently implements option #2
        	
        	//calculate number of items to put in each of the larger bins
        	int binPop = new Double(Math.ceil((double) values.length / classNum)).intValue();
        	//determine index of bin where the next bin has one less item
        	int lastBigBin = classNum - (values.length % binPop) - 1;
        	int itemIndex = 0;
        	//for each bin
        	for (int binIndex = 0; binIndex < classNum; binIndex++) {
        		//store min
        		localMin[binIndex] = (Comparable) values[itemIndex];
        		itemIndex+=binPop;
        		//store max
        		if (binIndex == classNum - 1) {
        			localMax[binIndex] = (Comparable) values[itemIndex];
        		} else {
        			localMax[binIndex] = (Comparable) values[itemIndex+1];
        		}
        		if (lastBigBin == binIndex)
					binPop--; // decrease the number of items in a bin for the
								// next iteration
        	}
        }
        isValid = true;
    }

    /**
	 * Determines the number of decimal places to truncate the interval at
	 * (public for testing purposes only).
	 * 
	 * @param slotWidth
	 * @return
	 */
    public int decimalPlaces(double slotWidth) {
    	int val = (new Double(Math.log(1.0/slotWidth)/2.0)).intValue();
    	if (val < 0) return 0;
    	else return val+1;
    }
    
    /**
	 * Truncates a double to a certain number of decimals places (public for
	 * testing purposes only). Note: truncation at zero decimal places will
	 * still show up as x.0, since we're using the double type.
	 * 
	 * @param value
	 * @param decimalPlaces
	 * @return
	 */
    public double round(double value, int decimalPlaces) {
    	double divisor = Math.pow(10, decimalPlaces);
    	double newVal = value * divisor;
    	//newVal = (new Double(newVal).intValue())/divisor; 
    	newVal =  (new Long(Math.round(newVal)).intValue())/divisor; 
    	return newVal;
    }
    
    private double calculateSlotWidth() {
    	//this method assumes isNumber and isValid are asserted
    	return (((Number) globalMax).doubleValue() - ((Number) globalMin).doubleValue()) / classNum;
    }

    protected int calculateSlot(Object val) {
        if (!isValid) return -1;
    	if (globalMax.compareTo(val) < 1) { //if val => max, put it in the last slot
            return classNum - 1;
        }
    	Double doubleVal = new Double(((Number) val).doubleValue());
    	//check each slot and see if: min <= val < max
    	for (int i = 0; i < classNum; i++) {
    		if (localMin[i].compareTo(doubleVal) < 1 && localMax[i].compareTo(doubleVal) > 0) {
    			return i;
    		}
    	}
    	//we didn't find it
    	return -1;
    }

    public Object getValue(Feature feature) {
        FeatureCollection coll = feature.getParent();

        if (coll == null) {
            return null;
        }

        if (!(coll.equals(fc))) {
            fc = coll;
            calculateMinAndMax();
        }

        int slot = calculateSlot(expr.getValue(feature));

        return new Integer(slot);
    }

    public void setExpression(Expression e) {
        super.setExpression(e);
        isValid = false; // the expression has changed, so we set the flag
						 // which will cause it to be recalculated.
        if (fc != null) {
            calculateMinAndMax();
        }
    }
    
    public Object getMin(int index) {
        if (fc == null) 
            return null;

        if (!isValid) 
            calculateMinAndMax();

        return localMin[index];
    }

    public Object getMax(int index) {
		if (fc == null)
			return null;

		if (!isValid)
			calculateMinAndMax();

		return localMax[index];
	}
}
