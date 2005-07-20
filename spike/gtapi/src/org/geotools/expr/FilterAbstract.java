package org.geotools.expr;

import org.geotools.api.filter.Filter;
import org.geotools.api.filter.expression.Expression;
import org.geotools.feature.Feature;

/**
 * Shared implementation for Filter.
 * <p>
 * Please treat Filter as a Expression that happens to result
 * in boolean.
 * </p>
 * @author Jody Garnett
 */
public class FilterAbstract extends ExpressionAbstract implements Filter {
	protected FilterAbstract(Expr factory) {
		super(factory);
	}
	/**
	 * Subclass should overrride.
	 * 
	 * Default value is Boolean.FALSE
	 */
	@Override
	public Boolean evaluate(Feature feature) {
		return Boolean.FALSE;
	}
	/**
	 * Straight call throught to: evaulate( feature )
	 */
	public boolean accepts(Feature feature) {
		return evaluate( feature );
	}
	/**
	 * Return this, because we are already a Filter.
	 * <p>
	 * This method is used to allow the chaining methods in ExpressionAbstract
	 * to function smoothly.
	 * </p>
	 */
	@Override
	public Filter filter() {
		return this;
	}
	
}
