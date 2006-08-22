package org.geotools.gml3.bindings;

import java.net.URI;
import java.util.List;

import org.geotools.referencing.CRS;
import org.geotools.xml.Node;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.DirectPosition;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;

/**
 * Utility class for gml3 parsing.
 * 
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public class GML3ParsingUtils {

	static CoordinateReferenceSystem crs( Node node ) {
		if ( node.getAttribute( "srsName" ) != null ) {
			URI srs = (URI) node.getAttributeValue( "srsName" );
			try {
				return CRS.decode( srs.toString() );
			} 
			catch (Exception e) {
				//TODO: log this
				return null;
			} 
			
		}
		
		return null;
	}
	
	static LineString lineString( Node node, GeometryFactory gf, CoordinateSequenceFactory csf ) {
		return line( node, gf, csf, false );
	}
	
	static LinearRing linearRing( Node node, GeometryFactory gf, CoordinateSequenceFactory csf ) {
		return (LinearRing) line( node, gf, csf, true );
	}
	
	static LineString line( Node node, GeometryFactory gf, CoordinateSequenceFactory csf, boolean ring ) {
		if ( node.hasChild( DirectPosition.class ) ) {
			List dps = node.getChildValues( DirectPosition.class );
			DirectPosition dp = (DirectPosition) dps.get( 0 );
			
			CoordinateSequence seq = csf.create( dps.size(), dp.getDimension() );
			for ( int i = 0; i < dps.size(); i++ ) {
				dp = (DirectPosition) dps.get( i );
				for ( int j = 0; j < dp.getDimension(); j++ ) {
					seq.setOrdinate( i, j, dp.getOrdinate( j ) );
				}
			}
			
			return ring ? gf.createLinearRing( seq ) : gf.createLineString( seq );
		}
		
		if ( node.hasChild( Point.class ) ) {
			List points = node.getChildValues( Point.class );
			Coordinate[] coordinates = new Coordinate[ points.size() ];
			
			for ( int i = 0; i < points.size(); i++ ) {
				coordinates[ i ] = ((Point)points.get( 0 )).getCoordinate();  
			}
			
			return ring ? gf.createLinearRing( coordinates ) : gf.createLineString( coordinates );
		}
		
		if ( node.hasChild( Coordinate.class ) ) {
			List list = node.getChildValues( Coordinate.class );
			Coordinate[] coordinates = (Coordinate[]) list.toArray( new Coordinate[list.size()] );
			
			return ring ? gf.createLinearRing( coordinates ) : gf.createLineString( coordinates );
		}
		
		if ( node.hasChild( DirectPosition[].class ) ) {
			DirectPosition[] dps = (DirectPosition[]) node.getChildValue( DirectPosition[].class );
			
			CoordinateSequence seq = null;
			if ( dps.length == 0 ) {
				seq = csf.create( 0, 0 );
			}
			else {
				seq = csf.create( dps.length, dps[0].getDimension() );
				for ( int i = 0; i < dps.length; i++ ) {
					DirectPosition dp = (DirectPosition) dps[ i ];
					for ( int j = 0; j < dp.getDimension(); j++ ) {
						seq.setOrdinate( i, j, dp.getOrdinate( j ) );
					}
				}
			}
			
			return ring ? gf.createLinearRing( seq ) : gf.createLineString( seq );
		}
		
		if ( node.hasChild( CoordinateSequence.class ) ) {
			CoordinateSequence seq = (CoordinateSequence) node.getChildValue( CoordinateSequence.class );
			return ring ? gf.createLinearRing( seq ) : gf.createLineString( seq );
		}
		
		return null;
	}
}
