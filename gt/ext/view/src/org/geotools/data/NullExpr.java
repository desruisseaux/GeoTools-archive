package org.geotools.data;

import java.io.IOException;

import org.geotools.feature.FeatureType;
import org.geotools.filter.Filter;
import org.geotools.filter.IllegalFilterException;
import org.geotools.filter.NullFilter;

class NullExpr extends AbstractExpr {
	Expr expr;
	NullExpr( Expr expr ){
		this.expr = expr;
	}
	public Filter filter(FeatureType schema) throws IOException {
		try {
			NullFilter nullFilter = factory.createNullFilter();
			nullFilter.nullCheckValue( expr.expression( schema ) );
			return nullFilter;
		} catch (IllegalFilterException e) {
			return null;
		}
	}
}