/*
 * ClassificationExpression.java
 *
 * Created on November 4, 2003, 2:34 PM
 */

package edu.psu.geovista.geotools.filter;

/**
 * 
 * @author jfc173
 */

import java.util.Random;

import org.geotools.feature.Feature;
import org.geotools.filter.DefaultExpression;
import org.geotools.filter.Filter;

public class ClassificationExpression extends DefaultExpression {

	private Filter filter;

	private Random r = new Random();

	/** Creates a new instance of ClassificationExpression */
	public ClassificationExpression() {
	}

	public short getType() {
		// Placeholder to be filled later.
		return (short) 59;
	}

	public void setFilter(Filter f) {
		filter = f;
		// stores the filter and then does absolutely nothing with it!
	}

	public Object evaluate(Feature feature) {
		return new Integer(r.nextInt(5));
	}

}
