package org.geotools.data;

import java.io.IOException;

import org.geotools.feature.FeatureType;
import org.geotools.filter.Expression;
import org.geotools.filter.Filter;
import org.geotools.filter.GeometryDistanceFilter;
import org.geotools.filter.IllegalFilterException;

class GeometryDistanceExpr extends AbstractFilterExpr {
	Expr expr1,expr2;	
	short op;
	double distance;	
	GeometryDistanceExpr( Expr expr1, short op, Expr expr2, double distance ){
		this.expr1 = expr1;
		this.expr2 = expr2;
		this.op = op;
		this.distance = distance;
	}
	public Filter filter(FeatureType schema) throws IOException {
		try {
			GeometryDistanceFilter filter = factory.createGeometryDistanceFilter( op );
			Expression left = expr1.expression( schema );
			Expression right = expr2.expression( schema );
			filter.addLeftGeometry( left );
			filter.addRightGeometry( right );
			filter.setDistance( distance );
			return filter;
		} catch (IllegalFilterException e) {
			return null;
		}				
	}
}