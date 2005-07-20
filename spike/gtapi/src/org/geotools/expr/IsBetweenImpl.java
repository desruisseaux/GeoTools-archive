package org.geotools.expr;

import org.geotools.api.filter.PropertyIsBetween;
import org.geotools.api.filter.expression.Expression;
import org.geotools.feature.Feature;

public class IsBetweenImpl extends FilterAbstract implements PropertyIsBetween {

	private Expression upperBoundary;
	private Expression lowerBoundary;
	private Expression expression;

	protected IsBetweenImpl(Expr factory, Expression lower, Expression expression, Expression upper ){
		super( factory );
		this.expression = expression;
		this.lowerBoundary = lower;
		this.upperBoundary = upper;
	}
	public Expression getExpression() {
		return expression;
	}
	public Expression getLowerBoundary() {
		return lowerBoundary;
	}
	public Expression getUpperBoundary() {
		return upperBoundary;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Boolean evaluate(Feature feature) {
		Comparable lower = compareable( lowerBoundary, feature );
		Comparable value = compareable( expression, feature );
		Comparable upper = compareable( upperBoundary, feature );

		return lower.compareTo( value ) == -1 &&
		       upper.compareTo( upper ) == 1;
	}
	private Comparable compareable( Expression expr, Feature feature ){
		Object value = expr.evaluate( feature );
		if( value instanceof Comparable ){
			return (Comparable) value;
		}
		else {
			return String.valueOf( value );
		}
	}
}
