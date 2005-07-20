package org.geotools.expr;

import org.geotools.api.filter.PropertyIsNull;
import org.geotools.api.filter.expression.Expression;
import org.geotools.feature.Feature;

public class IsNullImpl extends FilterAbstract implements
		PropertyIsNull {

	private Expression expression;

	protected IsNullImpl(Expr factory, Expression expression) {
		super(factory);
		this.expression = expression;
	}
	public Expression getExpression() {
		return expression;
	}
	@Override
	public Boolean evaluate(Feature feature) {
		return expression == null ||
	       expression.evaluate( feature ) == null;
	}	
}
