package org.geotools.expr;

import java.io.IOException;

import org.geotools.feature.FeatureType;
import org.geotools.filter.Expression;
import org.geotools.filter.Filter;
import org.geotools.filter.GeometryDistanceFilter;
import org.geotools.filter.IllegalFilterException;

import com.vividsolutions.jts.geom.Geometry;

class TopoDistanceGeometryExpr extends AbstractGeometryExpr {
	GeometryExpr expr1,expr2;	
	short op;
	double distance;	
	TopoDistanceGeometryExpr( GeometryExpr expr1, short op, GeometryExpr expr2, double distance ){
		this.expr1 = expr1;
		this.expr2 = expr2;
		this.op = op;
		this.distance = distance;
	}
	public Expr eval() {
		GeometryExpr eval1 = (GeometryExpr) expr1.eval();
		GeometryExpr eval2 = (GeometryExpr) expr2.eval();
		
		if( eval1 instanceof LiteralGeometryExpr &&
		    eval2 instanceof LiteralGeometryExpr ){
			Geometry geom1 = ((LiteralGeometryExpr)eval1).getGeometry();
			Geometry geom2 = ((LiteralGeometryExpr)eval1).getGeometry();
			switch( op ){
				case Filter.GEOMETRY_DWITHIN:
					return new LiteralExpr(geom1.isWithinDistance( geom2, distance ) );					
				case Filter.GEOMETRY_BEYOND:
					return new LiteralExpr(!geom1.isWithinDistance( geom2, distance ) );
				default:
					return new LiteralExpr( false );
			}			 
		}		
		if( eval1 == expr1 && eval2 == expr2 ){
			return this;
		}
		return new TopoDistanceGeometryExpr( eval1, op, eval2, distance );		
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