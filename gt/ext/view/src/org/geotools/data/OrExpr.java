package org.geotools.data;

import java.io.IOException;

import org.geotools.feature.FeatureType;
import org.geotools.filter.Filter;

class OrExpr extends AbstractExpr {
	Expr expr1,expr2;
	OrExpr( Expr expr1, Expr expr2 ){
		this.expr1 = expr1;
		this.expr2 = expr2;
	}
	public Filter filter(FeatureType schema) throws IOException {
		Filter filter1 = expr1.filter( schema );
		Filter filter2 = expr2.filter( schema );
		
		return filter1.or( filter2 );		
	}
}