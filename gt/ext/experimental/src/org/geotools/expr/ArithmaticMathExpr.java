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
		this.op = op;
	}
	public Expr eval() {
		MathExpr eval1 = (MathExpr) expr1.eval();
		MathExpr eval2 = (MathExpr) expr2.eval();
		
		if( eval1 instanceof LiteralMathExpr &&
		    eval2 instanceof LiteralMathExpr ){
			double number1 = ((LiteralMathExpr)eval1).toDouble();
			double number2 = ((LiteralMathExpr)eval2).toDouble();
			switch( op ){
				case Expression.MATH_ADD:
					return new LiteralMathExpr( number1 + number2 );
				case Expression.MATH_SUBTRACT:
					return new LiteralMathExpr( number1 - number2 );
				case Expression.MATH_MULTIPLY:
					return new LiteralMathExpr( number1 * number2 );
				case Expression.MATH_DIVIDE:
					return new LiteralMathExpr( number1 / number2 );					
			}			 
		}		
		if( eval1 == expr1 && eval2 == expr2 ){
			return this;
		}
		return new ArithmaticMathExpr( eval1, op, eval2 );		
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