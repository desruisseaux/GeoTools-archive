package org.geotools.data;

/**
 * Expr known to be a Geometry type.
 * <p>
 * Allows us to issolate all Geometry opperations against a single\
 * Expr subclass.
 * </p>
 */
public interface GeometryExpr extends Expr {	
	public GeometryExpr beyond( GeometryExpr expr, double distance );
	public GeometryExpr contains( GeometryExpr expr );
	public GeometryExpr crosses( GeometryExpr expr );
	public GeometryExpr disjoint( GeometryExpr expr );	
	public GeometryExpr dwithin( GeometryExpr expr, double distance );
	public GeometryExpr equal( GeometryExpr expr );
	public GeometryExpr intersects( GeometryExpr expr );
	public GeometryExpr overlaps( GeometryExpr expr );
	public GeometryExpr touches( GeometryExpr expr );
	public GeometryExpr within( GeometryExpr expr );
}
