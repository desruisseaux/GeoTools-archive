/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.referencing.crs;

import java.util.Arrays;

import junit.framework.TestCase;

import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.referencing.CRS;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.operation.MathTransformFactory;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.spatialschema.geometry.DirectPosition;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

/**
 * @author Jody Garnett
 */
public class CRSTest extends TestCase {
	
    public void testEPSG42102() throws Exception {
	    CoordinateReferenceSystem bc = CRS.decode("EPSG:42102");
		assertNotNull( "bc", bc );
	}
    
	public void testAUTO4200() throws Exception {	
	    CoordinateReferenceSystem utm = CRS.decode("AUTO:42001,0.0,0.0");
		assertNotNull( "auto-utm", utm );		
	}

    public void test4269() throws Exception {
        CoordinateReferenceSystem latlong = CRS.decode("EPSG:4269");        
        assertNotNull( "latlong", latlong );
        try {
            latlong = CRS.decode("4269");
            fail( "Shoudl not be able to decode 4269 without EPSG authority");
        } catch (NoSuchAuthorityCodeException e) {
            // expected
        }
        assertNotNull( "latlong", latlong );
    }
    
    public void testManditoryTranform() throws Exception {                
        CoordinateReferenceSystem WGS84 = (CoordinateReferenceSystem) CRS.decode("EPSG:4326"); // latlong
        CoordinateReferenceSystem NAD83 = (CoordinateReferenceSystem) CRS.decode("EPSG:4269");
        CoordinateReferenceSystem NAD83_UTM10 = (CoordinateReferenceSystem) CRS.decode("EPSG:26910");
        CoordinateReferenceSystem BC_ALBERS = (CoordinateReferenceSystem) CRS.decode("EPSG:42102");
                
        CoordinateOperation op = FactoryFinder.getCoordinateOperationFactory(null).createOperation( WGS84, WGS84 );
        MathTransform math = op.getMathTransform();
                
        DirectPosition pt1 = new GeneralDirectPosition(0.0,0.0);        
        DirectPosition pt2 = math.transform( pt1, null );
        assertNotNull( pt2 );
          
        double pts[] = new double[] {
                1187128,395268, 1187128,396027,
                1188245,396027, 1188245,395268,
                1187128,395268};
        double tst[] = new double[ pts.length ];                        
        math.transform( pts, 0, new double[ pts.length ], 0, pts.length/2 );
        for( int i=0; i<pts.length;i++)
            assertTrue( "pts["+i+"]", pts[i] != tst[i] );
        
        /*
        MathTransform transform = CRSService.reproject( bc, latlong, true );
        
        // origional bc alberts
        Polygon poly1 = poly( new double[] {
                1187128,395268, 1187128,396027,
                1188245,396027, 1188245,395268,
                1187128,395268} );

        // transformed
        Polygon poly2 = poly( new double[] {
                -123.470095558323,48.5432615620081, -123.469728946766,48.5500959221152,
                -123.454638288508,48.5497352036088, -123.455007082796,48.5429008916377,
                -123.470095558323,48.5432615620081} );        
        
        Polygon polyAfter = CRSService.transform( poly1, transform );
        System.out.println( "  actual:"+ polyAfter );
        System.out.println( "expected:"+ poly2 );        
        //assertEquals( poly2, polyAfter );
        
        Envelope before = poly1.getEnvelopeInternal();
        Envelope expected = poly2.getEnvelopeInternal();
        Envelope after = CRSService.transform( before, transform );
        
        System.out.println( "  actual:"+ after );
        System.out.println( "expected:"+ expected );                
        //assertEquals( expected, after );        
         */
    }
    
    public static GeometryFactory factory = new GeometryFactory();
    
    public static Polygon poly( double coords[] ) {
        return factory.createPolygon( ring( coords ), null );
    }
    public static LinearRing ring( double coords[] ) {
        return factory.createLinearRing( coords( coords ) );
    }
    public static CoordinateSequence coords( double coords[] ) {
        Coordinate array[] = new Coordinate[ coords.length/2 ];
        for( int i=0; i<array.length; i++ ) {
            array[i] = new Coordinate( coords[i*2], coords[i*2+1] );
        }
        return factory.getCoordinateSequenceFactory().create( array );
    }
    
}
