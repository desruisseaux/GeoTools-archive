package org.geotools.expr;

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
	public MathExpr add( Number number ){
		return add( Exprs.literal( number ));
	}
	public MathExpr add( double number ){
		return add( Exprs.literal( number ));
	}
	
 	public MathExpr subtract( MathExpr expr ){
 		return new ArithmaticMathExpr( this, Expression.MATH_SUBTRACT, expr );
 	}
 	public MathExpr subtract( Number number ){
		return subtract( Exprs.literal( number ));
	}
	public MathExpr subtract( double number ){
		return subtract( Exprs.literal( number ));
	}
	
 	public MathExpr divide( MathExpr expr ){
 		return new ArithmaticMathExpr( this, Expression.MATH_DIVIDE, expr );
 	}
 	public MathExpr divide( Number number ){
		return divide( Exprs.literal( number ));
	}
	public MathExpr divide( double number ){
		return divide( Exprs.literal( number ));
	}
 	
 	public MathExpr multiply( MathExpr expr ){
 		return new ArithmaticMathExpr( this, Expression.MATH_MULTIPLY, expr );
 	}
 	public MathExpr multiply( Number number ){
		return multiply( Exprs.literal( number ));
	}
	public MathExpr multiply( double number ){
		return multiply( Exprs.literal( number ));
	}
	
	public Expr eq( Number number ){
 		return eq( Exprs.literal( number ) );
 	}
	public Expr eq( double number ){
 		return eq( Exprs.literal( number ) );
 	}
	
 	public Expr gt( Number number ){
 		return gt( Exprs.literal( number ) );
 	}
	public Expr gt( double number ){
 		return gt( Exprs.literal( number ) );
 	}
	
	public Expr gte( Number number ){
 		return gte( Exprs.literal( number ) );
 	}
	public Expr gte( double number ){
 		return gte( Exprs.literal( number ) );
 	}
	
	public Expr lt( Number number ){
 		return lt( Exprs.literal( number ) );
 	}
	public Expr lt( double number ){
 		return lt( Exprs.literal( number ) );
 	}
 	
	public Expr lte( Number number ){
 		return lte( Exprs.literal( number ) );
 	}
	public Expr lte( double number ){
 		return lte( Exprs.literal( number ) );
 	}
	
	public Expr ne( Number number ){
 		return ne( Exprs.literal( number ) );
 	}
	public Expr ne( double number ){
 		return ne( Exprs.literal( number ) );
 	}
	
 	public Expr between( Number min, Number max ){
 		return between( Exprs.literal( min ), Exprs.literal( max ) );
 	}
 	public Expr between( double min, double max ){
 		return between( Exprs.literal( min ), Exprs.literal( max ) );
 	}
}
