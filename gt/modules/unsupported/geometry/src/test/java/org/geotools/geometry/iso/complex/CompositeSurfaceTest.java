package org.geotools.geometry.iso.complex;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.geotools.geometry.iso.FeatGeomFactoryImpl;
import org.geotools.geometry.iso.complex.ComplexFactoryImpl;
import org.geotools.geometry.iso.coordinate.CoordinateFactoryImpl;
import org.geotools.geometry.iso.primitive.PrimitiveFactoryImpl;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.complex.CompositeSurface;
import org.opengis.spatialschema.geometry.primitive.OrientableSurface;
import org.opengis.spatialschema.geometry.primitive.Surface;

public class CompositeSurfaceTest extends TestCase {

	
	public void testMain() {
		
		FeatGeomFactoryImpl tGeomFactory = FeatGeomFactoryImpl.getDefault2D();
		
		this._testCompositeSurface(tGeomFactory);
		
	}
	
	
	private void _testCompositeSurface(FeatGeomFactoryImpl aGeomFactory) {
		
		ComplexFactoryImpl complf = aGeomFactory.getComplexFactory();
		PrimitiveFactoryImpl pf = aGeomFactory.getPrimitiveFactory();
		CoordinateFactoryImpl cf = aGeomFactory.getCoordinateFactory();

		List<DirectPosition> directPositionList = new ArrayList<DirectPosition>();
		directPositionList.add(cf.createDirectPosition(new double[] {20, 10}));
		directPositionList.add(cf.createDirectPosition(new double[] {40, 10}));
		directPositionList.add(cf.createDirectPosition(new double[] {50, 40}));
		directPositionList.add(cf.createDirectPosition(new double[] {30, 50}));
		directPositionList.add(cf.createDirectPosition(new double[] {10, 30}));
		directPositionList.add(cf.createDirectPosition(new double[] {20, 10}));

		
		Surface s1 = pf.createSurfaceByDirectPositions(directPositionList);
		
		List<OrientableSurface> surfaceList = new ArrayList<OrientableSurface>();
		surfaceList.add(s1);
		
		CompositeSurface comps1 = complf.createCompositeSurface(surfaceList);
		
		//System.out.println(comps1.getEnvelope());
		double[] dp = comps1.getEnvelope().getLowerCorner().getCoordinates();
		assertTrue(dp[0] == 10);
		assertTrue(dp[1] == 10);
		dp = comps1.getEnvelope().getUpperCorner().getCoordinates();
		assertTrue(dp[0] == 50);
		assertTrue(dp[1] == 50);

		
		// ***** getRepresentativePoint()
		dp = comps1.getRepresentativePoint().getCoordinates();
		assertTrue(dp[0] == 20);
		assertTrue(dp[1] == 10);
		
		// Boundary operation of CompositeSurface not implemented yet. Hence isCycle doesn´t work yet.
		//assertTrue(comps1.isCycle() == false);

		
	}
	
}
