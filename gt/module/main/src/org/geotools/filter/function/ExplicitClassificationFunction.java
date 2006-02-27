package org.geotools.filter.function;

import org.geotools.feature.Feature;

public abstract class ExplicitClassificationFunction extends
		ClassificationFunction {

	abstract public String getName();

	abstract public Object evaluate(Feature feature);

	/**
	 * Returns the value(s) that was put into the bin. 
	 */
	abstract public Object getValue(int index);
}
