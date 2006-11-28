package org.geotools.data.gml;

import org.geotools.geometry.jts.GeometryCoordinateSequenceTransformer;
import org.geotools.gml3.Curve;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

public class GMLGeometryCoordinateSequenceTransformer extends
		GeometryCoordinateSequenceTransformer {

	public Geometry transform(Geometry g) throws TransformException {
		if ( g instanceof Curve ) {
			Curve curve = (Curve) g;
			LineString[] reprojected = new LineString[ curve.getNumGeometries() ];
			for ( int i = 0; i < reprojected.length; i++) {
				reprojected[ i ] =  (LineString) super.transform( (LineString) curve.getGeometryN( i ) );
			}
			
			return new Curve( reprojected, g.getFactory() );
		}
		
		return super.transform( g );
	}
	
}
