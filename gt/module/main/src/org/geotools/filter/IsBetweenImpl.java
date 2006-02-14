package org.geotools.filter;

import org.geotools.feature.Feature;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.expression.Expression;

public class IsBetweenImpl extends FilterAbstract implements PropertyIsBetween {

	private Expression upperBoundary;
	private Expression lowerBoundary;
	private Expression expression;

	protected IsBetweenImpl(FilterFactory factory, Expression lower, Expression expression, Expression upper ){
		super( factory );
		this.expression = expression;
		this.lowerBoundary = lower;
		this.upperBoundary = upper;
	}
	
	public Expression getExpression() {
		return expression;
	}
	public void setExpression(Expression expression) {
		this.expression = expression;
	}
	public Expression getLowerBoundary() {
		return lowerBoundary;
	}
	public void setLowerBoundary(Expression lowerBounds) {
		this.lowerBoundary = lowerBounds;
	}
	public Expression getUpperBoundary() {
		return upperBoundary;
	}
	public void setUpperBoundary(Expression upperBounds) {
		this.upperBoundary = upperBounds;
	}
	
	//@Override
	public boolean evaluate(Feature feature) {
		Comparable lower = comparable( lowerBoundary, feature );
		Comparable value = comparable( expression, feature );
		Comparable upper = comparable( upperBoundary, feature );

		return lower.compareTo( value ) == -1 &&
		       upper.compareTo( upper ) == 1;
	}
	
	
}
