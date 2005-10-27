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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.visitor.UniqueVisitor;
import org.geotools.filter.Expression;
import org.geotools.filter.IllegalFilterException;


/**
 * Clone of EqualIntervalFunction for unique values
 *
 * @author Cory Horner
 */
public class UniqueIntervalFunction extends ClassificationFunction {
    Object unique = null;

    /**
     * Creates a new instance of UniqueRanges ClassificationFunction
     */
    public UniqueIntervalFunction() {
    }

    public String getName() {
        return "UniqueInterval";
    }

    private void calculateValues()
        throws IllegalFilterException, IOException {
        UniqueVisitor uniqueVisit = new UniqueVisitor(expr);
        fc.accepts(uniqueVisit);
        unique = uniqueVisit.getResult().getValue();
    }

    private int calculateSlot(Object val) {
    	if (unique instanceof HashSet) {
    		Object[] values = ((HashSet) unique).toArray();
    		for (int i = 0; i < values.length; i++) {
    			if (values[i].equals(val)) return i;
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
		if (!fcNew.equals(fc)) {
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
     * Determines the value for the indexed slot/bin/bucket.
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

        if (unique instanceof HashMap) { 
        	HashMap uniqueMap = (HashMap) unique;
        	Object[] keys = uniqueMap.keySet().toArray();
        	return uniqueMap.get(keys[index]);
        } else if (unique instanceof HashSet) {
        	HashSet uniqueSet = (HashSet) unique;
        	Object[] uniqueArray = uniqueSet.toArray();
        	return uniqueArray[index];
        }
        return unique;
    }
}
