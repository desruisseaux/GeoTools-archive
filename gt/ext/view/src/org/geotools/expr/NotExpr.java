package org.geotools.expr;

import java.io.IOException;

import org.geotools.feature.FeatureType;
import org.geotools.filter.Filter;

class NotExpr extends AbstractExpr {
	Expr expr;
	NotExpr( Expr expr ){
		this.expr = expr;
	}
	public Filter filter(FeatureType schema) throws IOException {
		return expr.filter( schema ).not();
	}
}
