package org.geotools.expr;

import java.io.IOException;

import org.geotools.feature.FeatureType;
import org.geotools.filter.Filter;

class OrExpr extends AbstractExpr {
	Expr expr1,expr2;
	OrExpr( Expr expr1, Expr expr2 ){
		this.expr1 = expr1;
		this.expr2 = expr2;
	}
	public Expr eval() {
		Expr eval1 = expr1.eval();
		Expr eval2 = expr2.eval();
		
		if( eval1 instanceof ResolvedExpr &&
		    eval2 instanceof ResolvedExpr ){
			Object value1 = ((ResolvedExpr)eval1).getValue();
			Object value2 = ((ResolvedExpr)eval1).getValue();
			boolean or = Exprs.truth( value1 ) || Exprs.truth( value2 );
			return new LiteralExpr( or ); 
		}		
		if( eval1 == expr1 && eval2 == expr2 ){
			return this;
		}
		return new OrExpr( eval1, eval2 );		
	}	
	public Filter filter(FeatureType schema) throws IOException {
		Filter filter1 = expr1.filter( schema );
		Filter filter2 = expr2.filter( schema );
		
		return filter1.or( filter2 );		
	}
}