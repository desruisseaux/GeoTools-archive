package org.geotools.expr;

import java.io.IOException;

import org.geotools.feature.FeatureType;
import org.geotools.filter.Expression;
import org.geotools.filter.IllegalFilterException;
import org.geotools.filter.MathExpression;

class ArithmaticMathExpr extends AbstractMathExpr {
	MathExpr expr1,expr2;
	short op;
	ArithmaticMathExpr( MathExpr expr1, short op, MathExpr expr2 ){
		this.expr1 = expr1;
		this.expr2 = expr2;
	}
	public Expression expression(FeatureType schema) throws IOException {
		try {
			MathExpression math = factory.createMathExpression( op );
			Expression left = expr1.expression( schema );
			Expression right = expr2.expression( schema );
			math.addLeftValue( left );
			math.addRightValue( right );
			return math;
		} catch (IllegalFilterException e) {
			return null;
		}
	}
}