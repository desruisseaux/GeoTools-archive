package org.geotools.data;

import org.geotools.filter.Filter;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;


/**
 * Default implementation of GeometryExpr.
 * <p>
 * Provides chaining access to appropriate Expr constructors. Allows
 * use of literal Geometry objects via Exprs.geom( literal ).
 * </p>
 */
abstract public class AbstractGeometryExpr extends AbstractExpr implements GeometryExpr {
	public GeometryExpr beyond( GeometryExpr expr, double distance ){
 		return new TopoDistanceGeometryExpr( this, Filter.GEOMETRY_BEYOND, expr, distance );
 	}
 	/** Convience method allowing literal geometry */
 	public GeometryExpr beyond( Geometry geometry, double distance ){
 		return beyond( Exprs.geom( geometry ), distance ); 		
 	}
 	/** Convience method allowing literal extent */
 	public GeometryExpr beyond( Envelope extent, double distance ){
 		return beyond( Exprs.geom( extent ), distance ); 		
 	}
 	
 	public GeometryExpr contains( GeometryExpr expr ){
 		return new TopoGeometryExpr( this, Filter.GEOMETRY_CONTAINS, expr );
 	}
 	/** Convience method allowing literal geometry */
 	public GeometryExpr contains( Geometry geometry ){
 		return contains( Exprs.geom( geometry )); 		
 	}
 	/** Convience method allowing literal extent */
 	public GeometryExpr contains( Envelope extent ){
 		return contains( Exprs.geom( extent ) ); 		
 	}
 	 	
 	public GeometryExpr crosses( GeometryExpr expr ){
 		return new TopoGeometryExpr( this, Filter.GEOMETRY_CROSSES, expr );
 	}
 	/** Convience method allowing literal geometry */
 	public GeometryExpr crosses( Geometry geometry ){
 		return crosses( Exprs.geom( geometry )); 		
 	}
 	/** Convience method allowing literal extent */
 	public GeometryExpr crosses( Envelope extent ){
 		return crosses( Exprs.geom( extent ) ); 		
 	}
 	
 	public GeometryExpr disjoint( GeometryExpr expr ){
 		return new TopoGeometryExpr( this, Filter.GEOMETRY_DISJOINT, expr );
 	}
 	/** Convience method allowing literal geometry */
 	public GeometryExpr disjoint( Geometry geometry ){
 		return disjoint( Exprs.geom( geometry )); 		
 	}
 	/** Convience method allowing literal extent */
 	public GeometryExpr disjoint( Envelope extent ){
 		return disjoint( Exprs.geom( extent ) ); 		
 	}
 	
 	public GeometryExpr dwithin( GeometryExpr expr, double distance ){
 		return new TopoDistanceGeometryExpr( this, Filter.GEOMETRY_DWITHIN, expr, distance );
 	}
 	/** Convience method allowing literal geometry */
 	public GeometryExpr dwithin( Geometry geometry, double distance ){
 		return dwithin( Exprs.geom( geometry ), distance ); 		
 	}
 	/** Convience method allowing literal extent */
 	public GeometryExpr dwithin( Envelope extent, double distance ){
 		return dwithin( Exprs.geom( extent ), distance ); 		
 	}
 	
 	public GeometryExpr equal( GeometryExpr expr ){
 		return new TopoGeometryExpr( this, Filter.GEOMETRY_EQUALS, expr );
 	}
 	/** Convience method allowing literal geometry */
 	public GeometryExpr equal( Geometry geometry ){
 		return equal( Exprs.geom( geometry )); 		
 	}
 	/** Convience method allowing literal extent */
 	public GeometryExpr equal( Envelope extent ){
 		return equal( Exprs.geom( extent ) ); 		
 	}
 	
 	public GeometryExpr intersects( GeometryExpr expr ){
 		return new TopoGeometryExpr( this, Filter.GEOMETRY_INTERSECTS, expr );
 	}
 	/** Convience method allowing literal geometry */
 	public GeometryExpr intersects( Geometry geometry ){
 		return intersects( Exprs.geom( geometry )); 		
 	}
 	/** Convience method allowing literal extent */
 	public GeometryExpr intersects( Envelope extent ){
 		return intersects( Exprs.geom( extent ) ); 		
 	}
 	
 	public GeometryExpr overlaps( GeometryExpr expr ){
 		return new TopoGeometryExpr( this, Filter.GEOMETRY_OVERLAPS, expr );
 	}
 	/** Convience method allowing literal geometry */
 	public GeometryExpr overlaps( Geometry geometry ){
 		return overlaps( Exprs.geom( geometry )); 		
 	}
 	/** Convience method allowing literal extent */
 	public GeometryExpr overlaps( Envelope extent ){
 		return overlaps( Exprs.geom( extent ) ); 		
 	}
 	
 	public GeometryExpr touches( GeometryExpr expr ){
 		return new TopoGeometryExpr( this, Filter.GEOMETRY_TOUCHES, expr );
 	}
 	/** Convience method allowing literal geometry */
 	public GeometryExpr touches( Geometry geometry ){
 		return touches( Exprs.geom( geometry )); 		
 	}
 	/** Convience method allowing literal extent */
 	public GeometryExpr touches( Envelope extent ){
 		return touches( Exprs.geom( extent ) ); 		
 	}
 	
 	public GeometryExpr within( GeometryExpr expr ){
 		return new TopoGeometryExpr( this, Filter.GEOMETRY_WITHIN, expr ); 
 	}
 	/** Convience method allowing literal geometry */
 	public GeometryExpr within( Geometry geometry ){
 		return within( Exprs.geom( geometry )); 		
 	}
 	/** Convience method allowing literal extent */
 	public GeometryExpr within( Envelope extent ){
 		return within( Exprs.geom( extent ) ); 		
 	}
}
