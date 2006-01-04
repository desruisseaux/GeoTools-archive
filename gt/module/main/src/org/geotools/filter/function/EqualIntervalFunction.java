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
import org.geotools.util.NullProgressListener;


/**
 * Classification function for breaking a feature collection into edible chunks
 * of "equal" size.
 * 
 * @author James Macgill
 * @author Cory Horner, Refractions Research Inc.
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
			if (progress == null) progress = new NullProgressListener();
			fc.accepts(minVisit, progress);
			if (progress.isCanceled()) return;
			globalMin = (Comparable) minVisit.getResult().getValue();

			MaxVisitor maxVisit = new MaxVisitor(expr);
			fc.accepts(maxVisit, progress);
			if (progress.isCanceled()) return;
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
        		
        		if (i == 0) {
    				//ensure first min is less than or equal to globalMin
        			if (localMin[i].compareTo(new Double(((Number) globalMin).doubleValue())) < 0)
        				localMin[i] = new Double(fixRound(((Number) localMin[i]).doubleValue(), decPlaces, false));
        		} else if (i == classNum - 1) { 
        			//ensure last max is greater than or equal to globalMax
        			if (localMax[i].compareTo(new Double(((Number) globalMax).doubleValue())) > 0)
        				localMax[i] = new Double(fixRound(((Number) localMax[i]).doubleValue(), decPlaces, true));
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
    		int lastBigBin = values.length % classNum;
    		if (lastBigBin == 0) lastBigBin = classNum;
    		else lastBigBin--;

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
