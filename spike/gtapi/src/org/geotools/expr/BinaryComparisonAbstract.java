package org.geotools.expr;

import org.geotools.api.filter.BinaryComparisonOperator;
import org.geotools.api.filter.expression.Expression;

public class BinaryComparisonAbstract extends FilterAbstract implements BinaryComparisonOperator {

	protected Expression expression1;
	protected Expression expression2;

	protected BinaryComparisonAbstract(Expr factory, Expression expression1, Expression expression2 ) {
		super(factory);
		this.expression1 = expression1;
		this.expression2 = expression2;		
	}
	public Expression getExpression1() {
		return expression1;
	}

	public Expression getExpression2() {
		return expression2;
	}

}
