package org.geotools.data;

import org.geotools.filter.Expression;

/**
 * @author Jody Garnett
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AbstractMathExpr extends AbstractExpr implements MathExpr {
	public MathExpr add( MathExpr expr ){
 		return new ArithmaticMathExpr( this, Expression.MATH_ADD, expr );
 	}
 	public MathExpr subtract( MathExpr expr ){
 		return new ArithmaticMathExpr( this, Expression.MATH_SUBTRACT, expr );
 	}
 	public MathExpr divide( MathExpr expr ){
 		return new ArithmaticMathExpr( this, Expression.MATH_DIVIDE, expr );
 	}
 	public MathExpr multiply( MathExpr expr ){
 		return new ArithmaticMathExpr( this, Expression.MATH_MULTIPLY, expr );
 	}
}
