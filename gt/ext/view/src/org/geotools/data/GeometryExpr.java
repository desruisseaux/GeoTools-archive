package org.geotools.data;

import java.io.IOException;

import org.geotools.feature.FeatureType;
import org.geotools.filter.Expression;
import org.geotools.filter.Filter;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.IllegalFilterException;

class GeometryExpr extends AbstractFilterExpr {
	Expr expr1,expr2;	
	short op;
	double distance;
	GeometryExpr( Expr expr1, short op, Expr expr2){
		this( expr1, op, expr2, Double.NaN );
	}
	GeometryExpr( Expr expr1, short op, Expr expr2, double distance ){
		this.expr1 = expr1;
		this.expr2 = expr2;
		this.op = op;
		this.distance = distance;
	}
	public Filter filter(FeatureType schema) throws IOException {
		try {
			GeometryFilter filter = factory.createGeometryFilter( op );
			Expression left = expr1.expression( schema );
			Expression right = expr2.expression( schema );
			filter.addLeftGeometry( left );
			filter.addRightGeometry( right );			
			return filter;
		} catch (IllegalFilterException e) {
			return null;
		}				
	}
}