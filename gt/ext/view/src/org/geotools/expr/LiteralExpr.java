package org.geotools.expr;

import org.geotools.feature.FeatureType;
import org.geotools.filter.Expression;
import org.geotools.filter.IllegalFilterException;

/**
 * Provides a literal for use with Expr.
 * <p>
 * This is a start of an Expr chain - I would rather setup
 * Expr.literal( value ) if I can figure out how.
 * Maybe Chain.literal( value ), or Statement.literal( value )
 * </p>
 * <p>
 * Note many expression have constructors that take values directly
 * allowing us to cut down the use of this class and increase
 * readability.
 * </p>
 */
public class LiteralExpr extends AbstractExpr implements ResolvedExpr {
	Object literal;	
	public LiteralExpr( boolean b ){
		this( b ? Boolean.TRUE : Boolean.FALSE );		
	}
	public LiteralExpr( Object value ){
		literal = value;
	}
	public Expression expression(FeatureType schema) {
		try {
			return factory.createLiteralExpression( literal );		
		} catch (IllegalFilterException e) {
			return null;
		}
	}
	/**
	 * Value of this LiteralExpr.
	 * <p>
	 * Expr is doing its best to be immutable, Please 
	 * don't duck around this idea.
	 * </p>
	 * @return value of literal Expr
	 */
	public Object getValue(){
		return literal;
	}
}