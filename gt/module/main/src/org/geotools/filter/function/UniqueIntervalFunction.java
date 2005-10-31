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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.visitor.CalcResult;
import org.geotools.feature.visitor.UniqueVisitor;
import org.geotools.filter.Expression;
import org.geotools.filter.IllegalFilterException;


/**
 * Clone of EqualIntervalFunction for unique values
 *
 * @author Cory Horner
 */
public class UniqueIntervalFunction extends ClassificationFunction {
    Object unique = null; //sorted list of unique values
    Set[] values = null; //the contents of each bin (set of objects)
    boolean isValid = false; //we have valid data
    
    public UniqueIntervalFunction() {
    }

    public String getName() {
        return "UniqueInterval";
    }

    private void calculateValues()
        throws IllegalFilterException, IOException {
    	//use a visitor to grab the unique values
        UniqueVisitor uniqueVisit = new UniqueVisitor(expr);
        fc.accepts(uniqueVisit);
        CalcResult calcResult = uniqueVisit.getResult();
        if (calcResult == null) return;
        List result = calcResult.toList();
        //sort the results and put them in an array
        Collections.sort(result);
        Object[] results = result.toArray();
        //put the results into their respective slots/bins/buckets
        if (classNum < results.length) { //put more than one item in each class 
        	//resize values array
        	values = new Set[classNum];
        	//calculate number of items to put in each of the larger bins
        	int binPop = new Double(Math.ceil((double) results.length / classNum)).intValue();
        	//determine index of bin where the next bin has one less item
        	int lastBigBin = classNum - (results.length % binPop) - 1;
        	int itemIndex = 0;
        	//for each bin
        	for (int binIndex = 0; binIndex < classNum; binIndex++) {
        		HashSet val = new HashSet();
        		//add the items
        		for (int binItem = 0; binItem < binPop; binItem++) 
        			val.add(results[itemIndex++]);
        		if (lastBigBin == binIndex)
					binPop--; // decrease the number of items in a bin for the
								// next iteration
        		//store the bin
        		values[binIndex] = val;
        	}
        } else {
        	if (classNum > results.length) {
        		classNum = results.length; //chop off a few classes
        	}
        	//resize values array
        	values = new Set[classNum];
        	//assign straight-across (1 item per class)
        	for (int i = 0; i < classNum; i++) {
        		HashSet val = new HashSet();
        		val.add(results[i]);
        		values[i] = val;
        	}
        }
        //save the result (list), finally
        unique = result;
        isValid = true;
    }

    private int calculateSlot(Object val) {
    	if (val == null) return -1;
    	if (isValid) { //we have data! 
    		//for each bin/slot/bucket
    		for (int i = 0; i < values.length; i++) {
    			if (values[i].contains(val)) return i;
    		}
    	}
    	return -1;
    }

    public Object getValue(Feature feature) {
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
				calculateValues();
			} catch (IllegalFilterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		int slot = calculateSlot(expr.getValue(feature)); //feature, not featureCollection!
        return new Integer(slot);
    }

    public void setExpression(Expression e) {
        super.setExpression(e);

        if (fc != null) {
            try {
                calculateValues();
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

        if (unique == null) {
            try {
                calculateValues();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        //return the set
        return values[index]; 
    }
}
