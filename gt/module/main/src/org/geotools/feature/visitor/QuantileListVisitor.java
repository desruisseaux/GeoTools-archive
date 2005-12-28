package org.geotools.feature.visitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geotools.feature.Feature;
import org.geotools.filter.Expression;

public class QuantileListVisitor implements FeatureCalc {
	private Expression expr;
	private int count = 0;
	private int bins;
	private List items = new ArrayList();
	private List[] bin;

    boolean visited = false;
    int countNull = 0;
    int countNaN = 0;
	
	public QuantileListVisitor(Expression expr, int bins) {
		this.expr = expr;
		this.bins = bins;
		this.bin = new ArrayList[bins];
	}

	public CalcResult getResult() {
		// sort the list
		Collections.sort(items);

		// calculate number of items to put into each of the larger bins
		int binPop = new Double(Math.ceil((double) count / bins)).intValue();
		// determine index of bin where the next bin has one less item
		int lastBigBin = bins - (count % binPop) - 1;

		// put the items into their respective bins
		int item = 0;
		for (int binIndex = 0; binIndex < bins; binIndex++) {
			bin[binIndex] = new ArrayList();
			for (int binMember = 0; binMember < binPop; binMember++) {
				if (item < items.size())
					bin[binIndex].add(items.get(item++));
				else
					System.out.println("exceeded list bounds:"+item+"/"+items.size());
			}
			if (lastBigBin == binIndex)
				binPop--; // decrease the number of items in a bin for the next item
		}
		return new AbstractCalcResult() {
			public Object getValue() {
				return bin;
			}
		};
	}

	public void visit(Feature feature) {
        Object value = expr.getValue(feature);

        if (value == null) {
        	countNull++; //increment the null count, but don't store its value            
        	return;
        }

        if (value instanceof Double) {
        	double doubleVal = ((Number) value).doubleValue();
        	if ((doubleVal == Double.NaN) || Double.isInfinite(doubleVal)) {
        		countNaN++; //increment the NaN count, but don't store NaN as the max
        		return;
        	}
        }
		
		count++;
		items.add(value);
	}
	
	public void reset(int bins) {
		this.bins = bins;
		this.count = 0;
		this.items = new ArrayList();
		this.bin = new ArrayList[bins];
	    this.countNull = 0;
	    this.countNaN = 0;
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
