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
package org.geotools.filter.function;

import java.io.IOException;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.visitor.AverageVisitor;
import org.geotools.feature.visitor.CalcResult;
import org.geotools.feature.visitor.StandardDeviationVisitor;
import org.geotools.filter.Expression;
import org.geotools.filter.IllegalFilterException;
import org.geotools.util.NullProgressListener;

/**
 * Breaks a FeatureCollection into classes using the standard deviation classification method.
 * 
 * @author Cory Horner, Refractions Research Inc.
 * @source $URL: http://svn.geotools.org/geotools/branches/2.2.x/module/main/src/org/geotools/filter/function/QuantileFunction.java $
 */
public class StandardDeviationFunction extends RangedClassificationFunction {
	double standardDeviation;
	double average;
	
	boolean isValid = false; // we have valid data

	public StandardDeviationFunction() {
	}

	public String getName() {
		return "StandardDeviation";
	}

	private void calculate() throws IllegalFilterException, IOException {
		// find the average
		AverageVisitor averageVisit = new AverageVisitor(expr);
		if (progress == null) progress = new NullProgressListener();
		fc.accepts(averageVisit, progress);
		if (progress.isCanceled()) return;
		CalcResult calcResult = averageVisit.getResult();
		if (calcResult == null) return;
		average = calcResult.toDouble();
		// find the standard deviation
		StandardDeviationVisitor sdVisit = new StandardDeviationVisitor(expr, average);
		fc.accepts(sdVisit, progress);
		if (progress.isCanceled()) return;
		calcResult = sdVisit.getResult();
		if (calcResult == null) return;
		standardDeviation = calcResult.toDouble();
		isValid = true;
	}

	private int calculateSlot(Object val) {
		if (val == null)
			return -1;
		double value = Double.parseDouble(val.toString());
		if (isValid) { // we have data!
			//calculate upper bound of first class
			double firstBoundary = average - (((classNum / 2.0) - 1) * standardDeviation);
			for (int i = 0; i < classNum - 1; i++) {
				if (value < (firstBoundary + (i * standardDeviation))) {
					return i;
				}
			}
			return classNum - 1; //must be in uppermost class
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
	
	public Object getMin(int index) {
		if (index <= 0 || index >= classNum)
			return null;
		return new Double(average - (((classNum / 2.0) - index) * standardDeviation));
	}
	
	public Object getMax(int index) {
		if (index < 0 || index >= classNum - 1)
			return null;
		return new Double(average - (((classNum / 2.0) - 1 - index) * standardDeviation));
	}
}
