package org.geotools.expr;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Expr known to be a Geometry type.
 * <p>
 * Allows us to issolate all Geometry opperations against a single\
 * Expr subclass.
 * </p>
 */
public interface GeometryExpr extends Expr {
	
	public GeometryExpr beyond( GeometryExpr expr, double distance );
	public GeometryExpr beyond( Geometry geom, double distance );
	public GeometryExpr beyond( Envelope extent, double distance );
	
	public GeometryExpr contains( GeometryExpr expr );
	public GeometryExpr contains( Geometry geom );
	public GeometryExpr contains( Envelope extent );
	
	public GeometryExpr crosses( GeometryExpr expr );
	public GeometryExpr crosses( Geometry geom );
	public GeometryExpr crosses( Envelope extent );
	
	public GeometryExpr disjoint( GeometryExpr expr );
	public GeometryExpr disjoint( Geometry geom );
	public GeometryExpr disjoint( Envelope extent );
	
	public GeometryExpr dwithin( GeometryExpr expr, double distance );
	public GeometryExpr dwithin( Geometry geom, double distance );
	public GeometryExpr dwithin( Envelope extent, double distance );
	
	public GeometryExpr equal( GeometryExpr expr );
	public GeometryExpr equal( Geometry geom );
	public GeometryExpr equal( Envelope extent );
	
	public GeometryExpr intersects( GeometryExpr expr );
	public GeometryExpr intersects( Geometry geom );
	public GeometryExpr intersects( Envelope extent );
	
	public GeometryExpr overlaps( GeometryExpr expr );
	public GeometryExpr overlaps( Geometry geom );
	public GeometryExpr overlaps( Envelope extent );
	
	public GeometryExpr touches( GeometryExpr expr );
	public GeometryExpr touches( Geometry geom );
	public GeometryExpr touches( Envelope extent );
	
	public GeometryExpr within( GeometryExpr expr );
	public GeometryExpr within( Geometry geom );
	public GeometryExpr within( Envelope extent );		
}

