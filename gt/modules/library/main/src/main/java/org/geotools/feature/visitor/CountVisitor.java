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
package org.geotools.feature.visitor;

import org.geotools.feature.Feature;
import org.geotools.feature.visitor.SumVisitor.SumResult;
import org.opengis.feature.type.FeatureCollectionType;


/**
 * Determines the number of features in the collection
 *
 * @author Cory Horner, Refractions
 *
 * @since 2.2.M2
 * @source $URL$
 */
public class CountVisitor implements FeatureCalc {
    int count = 0;

    public void init(FeatureCollectionType collection) {
    	//do nothing
    }
    public void visit(Feature feature) {
        visit((org.opengis.feature.Feature)feature);
    }
    
    public void visit(org.opengis.feature.Feature feature) {
    	count++;
    }

    public int getCount() {
        return count;
    }

    public void setValue(int count) {
        this.count = count;
    }
    
    public void reset() {
        this.count = 0;
    }

    public CalcResult getResult() {
        return new CountResult(count);
    }

    public static class CountResult extends AbstractCalcResult {
        private int count;

        public CountResult(int newcount) {
            count = newcount;
        }

        public Object getValue() {
            return new Integer(count);
        }

        public boolean isCompatible(CalcResult targetResults) {
        	if (targetResults instanceof CountResult) return true;
        	if (targetResults instanceof SumResult) return true;
        	return false;
        }

        public CalcResult merge(CalcResult resultsToAdd) {
            if (!isCompatible(resultsToAdd)) {
                throw new IllegalArgumentException(
                    "Parameter is not a compatible type");
            }

            if (resultsToAdd instanceof CountResult) {
            	//add the two counts
            	int toAdd = resultsToAdd.toInt();
            	return new CountResult(count + toAdd);
            } else if (resultsToAdd instanceof SumResult) {
            	// we don't want to implement this twice, so we'll call the
				// SumResult version of this function
            	return resultsToAdd.merge(this);
            } else {
            	throw new IllegalArgumentException(
				"The CalcResults claim to be compatible, but the appropriate merge method has not been implemented.");
            }
        }
    }
}
