package org.geotools.expr;

import org.geotools.feature.FeatureType;
import org.geotools.filter.Expression;
import org.geotools.filter.IllegalFilterException;

public class LiteralMathExpr extends AbstractMathExpr implements ResolvedExpr {
	Number number;
	public LiteralMathExpr( int number ){
		this.number = new Integer( number );
	}
	public LiteralMathExpr( double number ){
		this.number = new Double( number );
	}	
	public LiteralMathExpr( Number number ){
		this.number = number;
	}
	public Expression expression(FeatureType schema) {
		try {
			return factory.createLiteralExpression( number );			
		} catch (IllegalFilterException e) {
			return null;
		}
	}
	/**
	 * Value of this LiteralMathExpr.
	 * <p>
	 * Expr is doing its best to be immutable, Please 
	 * don't duck around this idea.
	 * </p>
	 * @return number
	 */
	public Object getValue(){
		return number;
	}
	public Number getNumber(){
		return number;
	}
	public double toDouble(){
		if( number == null ){
			return Double.NaN;
		}
		return number.doubleValue();
	}
}