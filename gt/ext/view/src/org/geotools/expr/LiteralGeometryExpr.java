package org.geotools.expr;

import org.geotools.feature.FeatureType;
import org.geotools.filter.Expression;
import org.geotools.filter.IllegalFilterException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;

public class LiteralGeometryExpr extends AbstractGeometryExpr implements ResolvedExpr {
	static final GeometryFactory geomFactory = new GeometryFactory();
	Geometry geom;
	
	public LiteralGeometryExpr( Envelope extent ){
		Coordinate points[] = new Coordinate[5];
		points[0] = new Coordinate( extent.getMinX(), extent.getMinY() );
		points[1] = new Coordinate( extent.getMinX(), extent.getMaxY() );
		points[2] = new Coordinate( extent.getMaxX(), extent.getMaxY() );
		points[3] = new Coordinate( extent.getMaxX(), extent.getMinY() );
		points[4] = points[0];
		LinearRing ring = geomFactory.createLinearRing( points );
		geom = geomFactory.createPolygon( ring, new LinearRing[0] );		
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
	/**
	 * Value of this LiteralGeometryExpr.
	 * <p>
	 * Expr is doing its best to be immutable, Please 
	 * don't duck around this idea.
	 * </p>
	 * @return geom
	 */
	public Object getValue(){
		return geom;
	}	
	public Geometry getGeometry(){
		return geom;
	}
}