package org.geotools.data;

import java.io.IOException;

import org.geotools.feature.FeatureType;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.Expression;
import org.geotools.filter.Filter;
import org.geotools.filter.IllegalFilterException;

class CompareExpr extends AbstractExpr {
	Expr expr1,expr2;
	short op;
	CompareExpr( Expr expr1, short op, Expr expr2 ){
		this.expr1 = expr1;
		this.expr2 = expr2;
	}
	public Filter filter(FeatureType schema) throws IOException {
		try {
			CompareFilter compare = factory.createCompareFilter( op );
			Expression left = expr1.expression( schema );
			Expression right = expr2.expression( schema );
			compare.addLeftValue( left );
			compare.addRightValue( right );
			return compare;
		} catch (IllegalFilterException e) {
			return null;
		}				
	}
}