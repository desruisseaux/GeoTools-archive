package org.geotools.data;

import org.geotools.feature.FeatureType;
import org.geotools.filter.Expression;
import org.geotools.filter.IllegalFilterException;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public class LiteralGeometryExpr extends AbstractGeometryExpr {
	Object geom;
	
	public LiteralGeometryExpr( Envelope extent ){
		geom = extent;
	}
	public LiteralGeometryExpr( Geometry geom ){
		this.geom = geom;
	}
	public Expression expression(FeatureType schema) {
		try {
			return factory.createLiteralExpression( geom );			
		} catch (IllegalFilterException e) {
			return null;
		}
	}
}