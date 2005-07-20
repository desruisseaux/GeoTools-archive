package org.geotools.expr;

import org.geotools.api.filter.PropertyIsEqualTo;
import org.geotools.api.filter.expression.Expression;
import org.geotools.feature.Feature;

public class IsEqualsToImpl extends BinaryComparisonAbstract implements PropertyIsEqualTo {

	protected IsEqualsToImpl(Expr factory, Expression expression1, Expression expression2) {
		super(factory, expression1, expression2);
	}

	@Override
	public Boolean evaluate(Feature feature) {
		Object value1 = eval( expression1, feature );	
		Object value2 = eval( expression2, feature );	
		return (value1 == null && value2 == null) ||
		        value1 != null && value1.equals( value2 );
	}
}
