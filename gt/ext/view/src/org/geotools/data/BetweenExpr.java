package org.geotools.data;

import java.io.IOException;

import org.geotools.feature.FeatureType;
import org.geotools.filter.BetweenFilter;
import org.geotools.filter.Expression;
import org.geotools.filter.Filter;
import org.geotools.filter.IllegalFilterException;

class BetweenExpr extends AbstractFilterExpr {
	Expr expr, min, max;	
	BetweenExpr( Expr min, Expr expr, Expr max ){
		this.expr = expr;
		this.min = min;
		this.max = max;
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