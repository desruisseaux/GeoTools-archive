package org.geotools.filter;

import org.geotools.feature.Feature;
import org.opengis.filter.PropertyIsNull;
import org.opengis.filter.expression.Expression;

public class IsNullImpl extends FilterAbstract implements
		PropertyIsNull {

	private org.opengis.filter.expression.Expression expression;

	public IsNullImpl(FilterFactory factory, org.opengis.filter.expression.Expression expression) {
		super(factory);
		this.expression = expression;
	}
	
	public Expression getExpression() {
		return expression;
	}
	
	public void setExpression(Expression expression) {
		this.expression = expression;
	}
	
	//@Override
	public boolean evaluate(Feature feature) {
		return expression == null ||
	       expression.evaluate( feature ) == null;
	}	
}
