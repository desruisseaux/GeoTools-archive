package org.geotools.data;

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
public class LiteralExpr extends AbstractExpr {
	Object literal;
	public LiteralExpr( int i ){
		this( new Integer( i ) );		
	}
	public LiteralExpr( double d ){
		this( new Double( d ) );		
	}
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
}