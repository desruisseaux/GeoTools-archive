package org.geotools.feature.visitor;

import org.geotools.feature.Feature;
import org.geotools.filter.expression.Expression;

/**
 * Determines the standard deviation. 
 *
 *            ----------------------------
 *            |  1   ---
 * Std dev =  | ___  \   ( x - mean ) ^ 2
 *           \|  N   /__
 *           
 * aka std dev = sqrt((sum((x-mean)^2))/N) where N is the number of samples
 * 
 * @author Cory Horner, Refractions Research Inc.
 *
 * @source $URL: http://svn.geotools.org/geotools/branches/2.2.x/module/main/src/org/geotools/feature/visitor/QuantileListVisitor.java $
 */
public class StandardDeviationVisitor implements FeatureCalc {
	private Expression expr;
	private int count = 0;
    private double deviationSquaredSum = 0;
    private double average = 0;

    boolean visited = false;
    int countNull = 0;
    int countNaN = 0;
	
	public StandardDeviationVisitor(Expression expr, double average) {
		this.expr = expr;
		this.average = average;
		//at the moment we're assuming we won't know who the feature collection is, and need the average as input
	}

	public CalcResult getResult() {
		return new AbstractCalcResult() {
			public Object getValue() {
                if (count == 0) return null;
				return new Double(Math.sqrt(deviationSquaredSum / count));
			}
		};
	}

	public void visit(Feature feature) {
        Object value = expr.getValue(feature);

        if (value == null) {
			countNull++; // increment the null count
			return; // don't store this value
		}

		if (value instanceof Double) {
			double doubleVal = ((Double) value).doubleValue();
			if (Double.isNaN(doubleVal) || Double.isInfinite(doubleVal)) {
				countNaN++; // increment the NaN count
				return; // don't store NaN value
			}
		}
		
		count++;
		deviationSquaredSum += Math.pow(average - Double.parseDouble(value.toString()),2);
	}
	
	public void reset() {
		this.count = 0;
	    this.countNull = 0;
	    this.countNaN = 0;
	    this.deviationSquaredSum = 0;
	    this.average = 0;
	}

    /**
     * @return the number of features which returned a NaN
     */
    public int getNaNCount() {
    	return countNaN;
    }
    
    /**
     * @return the number of features which returned a null
     */
    public int getNullCount() {
    	return countNull;
    }
}
