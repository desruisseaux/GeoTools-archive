package org.geotools.expr;

import java.io.IOException;

import org.geotools.feature.FeatureType;
import org.geotools.filter.BetweenFilter;
import org.geotools.filter.Expression;
import org.geotools.filter.Filter;
import org.geotools.filter.IllegalFilterException;

class BetweenExpr extends AbstractExpr {
	Expr expr, min, max;	
	BetweenExpr( Expr min, Expr expr, Expr max ){
		this.expr = expr;
		this.min = min;
		this.max = max;
	}
	static final Comparable compare( ResolvedExpr expr ){
		Object value = expr.getValue();
		if( value instanceof Comparable ){
			return (Comparable) value;
		}
		return null;
	}
	
	public Expr eval() {
		Expr evalMin = min.eval();
		Expr eval = expr.eval();
		Expr evalMax = max.eval();
		
		if( evalMin instanceof ResolvedExpr &&
		    eval instanceof ResolvedExpr &&
			evalMax instanceof ResolvedExpr){
			Comparable lower = compare( (ResolvedExpr) evalMin );
			Comparable value = compare( (ResolvedExpr) eval );
			Comparable upper = compare( (ResolvedExpr) evalMax );
			
			if( value == null ){
				return new LiteralExpr( false );
			}
			boolean gt = lower == null ? true : value.compareTo( lower ) >= 0;
			boolean lt = upper == null ? true : value.compareTo( upper ) <= 0;
			return new LiteralExpr( gt && lt );						 
		}		
		if( evalMin == min && eval == expr && evalMax == max ){
			return this;
		}
		return new BetweenExpr( evalMin, eval, evalMax );		
	}	
	public Filter filter(FeatureType schema) throws IOException {
		try {
			BetweenFilter between = factory.createBetweenFilter();
			Expression expression = expr.expression( schema );
			Expression left = min.expression( schema );
			Expression right = max.expression( schema );
			between.addMiddleValue( expression );
			between.addLeftValue( left );
			between.addRightValue( right );
			return between;
		} catch (IllegalFilterException e) {
			return null;
		}				
	}
}