package org.geotools.expr;

import org.geotools.feature.FeatureType;
import org.geotools.filter.Expression;
import org.geotools.filter.IllegalFilterException;

public class LiteralMathExpr extends AbstractMathExpr {
	Object number;
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
}