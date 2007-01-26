package org.geotools.geometry.iso.primitive;

import junit.framework.TestCase;

import org.geotools.geometry.iso.FeatGeomFactoryImpl;
import org.geotools.geometry.iso.coordinate.CoordinateFactoryImpl;
import org.geotools.geometry.iso.coordinate.DirectPositionImpl;
import org.geotools.geometry.iso.primitive.PrimitiveFactoryImpl;
import org.opengis.spatialschema.geometry.complex.Complex;
import org.opengis.spatialschema.geometry.complex.CompositePoint;
import org.opengis.spatialschema.geometry.primitive.Point;

/**
 * @author sanjay
 *
 */
public class PointTest extends TestCase {
	
	public void testMain() {
		
		FeatGeomFactoryImpl tGeomFactory = FeatGeomFactoryImpl.getDefault2D();
		
		this._testPoint(tGeomFactory);
		
	}
	
	
	private void _testPoint(FeatGeomFactoryImpl aGeomFactory) {
		
		CoordinateFactoryImpl tCoordFactory = aGeomFactory.getCoordinateFactory();
		PrimitiveFactoryImpl tPrimFactory = aGeomFactory.getPrimitiveFactory();
		
		double[] coord = new double[]{10, 32000};
		Point p1 = tPrimFactory.createPoint(coord);

		// ***** getRepresentativePoint()
		double[] dp = p1.getRepresentativePoint().getCoordinates();
		assertTrue(dp[0] == 10);
		assertTrue(dp[1] == 32000);
		
		DirectPositionImpl dp1 = tCoordFactory.createDirectPosition(coord);
		
		Point p2 = tPrimFactory.createPoint(dp1);
		
		System.out.println("P1: " + p1);
		System.out.println("P2: " + p2);
		assertTrue(p1.equals(p2));
		
		System.out.println("Dimension is " + p1.getDimension(null));
		assertTrue(p1.getDimension(null) == 0);

		System.out.println("Coordinate dimension is " + p1.getCoordinateDimension());
		assertTrue(p1.getCoordinateDimension() == 2);
		
		Complex cp1 = p1.getClosure();
		System.out.println("Class of p1.closure() is " + cp1.getClass());
		assertTrue(cp1 instanceof CompositePoint);
		System.out.println("p1.closure() is " + cp1);
		
		assertTrue(p1.isCycle() == true);

		
		double[] coord2 = new double[]{5, 20};
		dp1 = tCoordFactory.createDirectPosition(coord2);
		p1.setPosition(dp1);
		System.out.println("P1: " + p1);
		System.out.println("P2: " + p2);
		assertTrue(!p1.equals(p2));
		

		
	}

}
