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
import java.util.List;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.visitor.CalcResult;
import org.geotools.feature.visitor.QuantileListVisitor;
import org.geotools.filter.IllegalFilterException;
import org.geotools.filter.expression.Expression;
import org.geotools.util.NullProgressListener;

/**
 * Breaks a FeatureCollection into classes with an equal number of items in each.
 * 
 * @author Cory Horner, Refractions Research Inc.
 * @source $URL$
 */
public class QuantileFunction extends RangedClassificationFunction {
	List[] bin;
	Comparable globalMin = null;
    Comparable globalMax = null;
    Comparable[] localMin = null;
    Comparable[] localMax = null;
    boolean isNumber = false;
	
	boolean isValid = false; // we have valid data

	public QuantileFunction() {
	}

	public String getName() {
		return "Quantile";
	}

	private void calculate() throws IllegalFilterException, IOException {
		// use a visitor to find the values in each bin
		QuantileListVisitor quantileVisit = new QuantileListVisitor(expr, classNum);
		if (progress == null) progress = new NullProgressListener();
		fc.accepts(quantileVisit, progress);
		if (progress.isCanceled()) return;
		CalcResult calcResult = quantileVisit.getResult();
		if (calcResult == null) return;
		Object result = calcResult.getValue();
		bin = (List[]) result;
		if (bin.length != classNum) {
			classNum = bin.length; //number of bins was reduced, therefore resize.
		}
		
		//generate the min and max values, and round off if applicable/necessary
		globalMin = (Comparable) bin[0].toArray()[0];
		Object lastBin[] = bin[bin.length-1].toArray(); 
		if (lastBin.length > 0)
			globalMax = (Comparable) lastBin[lastBin.length-1];
		else
			globalMax = null;
	
		if ((globalMin instanceof Number) && (globalMax instanceof Number)) {
			isNumber = true;
		} else {
			isNumber = false;
		}
        
    	//resize arrays
        localMin = new Comparable[classNum];
    	localMax = new Comparable[classNum];

    	//calculate all the little min and max values
    	if (isNumber) {
        	//globally consistent
    		//double slotWidth = (((Number) globalMax).doubleValue() - ((Number) globalMin).doubleValue()) / classNum;
        	for (int i = 0; i < classNum; i++) {
        		//copy the min + max values
        		List thisBin = bin[i];
        		localMin[i] = (Comparable) thisBin.get(0);
        		localMax[i] = (Comparable) thisBin.get(thisBin.size()-1);
        		//locally accurate
        		double slotWidth = (((Number) localMax[i]).doubleValue() - ((Number) localMin[i]).doubleValue()) / classNum;
        		if (slotWidth == 0.0) { //use global value, as there is only 1 value in this set
        			slotWidth = (((Number) globalMax).doubleValue() - ((Number) globalMin).doubleValue()) / classNum;
        		}
        		//determine number of decimal places to allow
        		int decPlaces = decimalPlaces(slotWidth);
        		//clean up truncation error
        		if (decPlaces > -1) {
        			localMin[i] = new Double(round(((Number) localMin[i]).doubleValue(), decPlaces));
        			localMax[i] = new Double(round(((Number) localMax[i]).doubleValue(), decPlaces));
        		}
        		
        		if (i == 0) {
    				//ensure first min is less than or equal to globalMin
        			if (localMin[i].compareTo(new Double(((Number) globalMin).doubleValue())) > 0)
        				localMin[i] = new Double(fixRound(((Number) localMin[i]).doubleValue(), decPlaces, false));
        		} else if (i == classNum - 1) { 
        			//ensure last max is greater than or equal to globalMax
        			if (localMax[i].compareTo(new Double(((Number) globalMax).doubleValue())) < 0)
        				localMax[i] = new Double(fixRound(((Number) localMax[i]).doubleValue(), decPlaces, true));
        		}
        		//synchronize min with previous max
        		if ((i != 0) && (!localMin[i].equals(localMax[i-1]))) {
        			if (!localMin[i].equals(localMax[i])) //only if the range contains more than 1 value
        				localMin[i] = localMax[i-1];
        		}
        	}
        } else {
        	//it's a string.. leave it be (just copy the values)
        	for (int i = 0; i < classNum; i++) {
        		List thisBin = bin[i];
        		localMin[i] = (Comparable) thisBin.get(0);
        		localMax[i] = (Comparable) thisBin.get(thisBin.size()-1);
        	}
        }
		isValid = true;
	}

	private int calculateSlot(Object val) {
		if (val == null)
			return -1;
		if (isValid) { // we have data!
			// for each bin/slot/bucket
			for (int i = 0; i < bin.length; i++) {
				if (bin[i].contains(val))
					return i;
			}
		}
		return -1;
	}

	public Object evaluate(Feature feature) {
		FeatureCollection fcNew;

		if (feature instanceof FeatureCollection) {
			fcNew = (FeatureCollection) feature;
		} else {
			fcNew = feature.getParent();
		}
		if (fcNew == null) {
			return new Integer(0);
		}
		if (!fcNew.equals(fc) || !isValid) {
			fc = fcNew;
			try {
				calculate();
			} catch (IllegalFilterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		int slot = calculateSlot(expr.getValue(feature)); // feature, not
															// featureCollection!
		return new Integer(slot);
	}

	public void setExpression(Expression e) {
		super.setExpression(e);

		if (fc != null) {
			try {
				calculate();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	/**
	 * Determines the value(s) for the indexed slot/bin/bucket.
	 * 
	 * @return the value
	 */
	public Object getValue(int index) {
		if (fc == null) {
			return null;
		}

		if (!isValid) {
			try {
				calculate();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// return the values
		return bin[index];
	}
	
	public Object getMin(int index) {
		return localMin[index];
	}
	
	public Object getMax(int index) {
		return localMax[index];
	}
}
