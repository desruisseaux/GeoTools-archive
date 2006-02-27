package org.geotools.filter.function;

import org.geotools.feature.Feature;

public abstract class RangedClassificationFunction extends
		ClassificationFunction {

	abstract public String getName();

	abstract public Object evaluate(Feature feature);

	/**
	 * Returns the lower bound value for the bin.
	 * @param index
	 * @return
	 */
	abstract public Object getMin(int index);
	
	/**
	 * Returns the upper bound value for the bin.
	 * @param index
	 * @return
	 */
	abstract public Object getMax(int index);
	
}
