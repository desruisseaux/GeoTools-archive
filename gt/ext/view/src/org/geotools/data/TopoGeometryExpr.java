package org.geotools.data;

import java.io.IOException;

import org.geotools.feature.FeatureType;
import org.geotools.filter.Expression;
import org.geotools.filter.Filter;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.IllegalFilterException;

class TopoGeometryExpr extends AbstractGeometryExpr {
	GeometryExpr expr1,expr2;	
	short op;	
	TopoGeometryExpr( GeometryExpr expr1, short op, GeometryExpr expr2){
		this.expr1 = expr1;
		this.expr2 = expr2;
		this.op = op;
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