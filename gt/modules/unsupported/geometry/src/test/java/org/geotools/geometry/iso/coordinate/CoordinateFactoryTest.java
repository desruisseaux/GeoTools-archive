package org.geotools.geometry.iso.coordinate;

import junit.framework.TestCase;

import org.geotools.geometry.iso.FeatGeomFactoryImpl;
import org.geotools.geometry.iso.coordinate.CoordinateFactoryImpl;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.spatialschema.geometry.geometry.LineSegment;
import org.opengis.spatialschema.geometry.geometry.Position;

public class CoordinateFactoryTest extends TestCase {
	
	public void testMain() {
		
		FeatGeomFactoryImpl tGeomFactory = FeatGeomFactoryImpl.getDefault3D();
		
		this._testCoordinateObjects(tGeomFactory);
		
	}	

	private void _testCoordinateObjects(FeatGeomFactoryImpl aFactory) {
		
		CoordinateFactoryImpl cf = aFactory.getCoordinateFactory();
		
		// public DirectPositionImpl createDirectPosition();
		DirectPosition dp1 = cf.createDirectPosition();
		assertTrue(dp1.getOrdinate(0) == 0.0);
		assertTrue(dp1.getOrdinate(1) == 0.0);
		assertTrue(dp1.getOrdinate(2) == 0.0);
		
		// public DirectPositionImpl createDirectPosition(double[] coord);
		double[] da = new double[3];
		da[0] = 10.0;
		da[1] = -115000.0;
		da[2] = 0.0000000125;
		DirectPosition dp2 = cf.createDirectPosition(da);
		assertTrue(dp2.getOrdinate(0) == 10.0);
		assertTrue(dp2.getOrdinate(1) == -115000.0);
		assertTrue(dp2.getOrdinate(2) == 0.0000000125);

		// public Envelope createEnvelope(
		//			DirectPosition lowerCorner,
		//			DirectPosition upperCorner)
		Envelope env1 = cf.createEnvelope(dp1, dp2);
		DirectPosition lc = env1.getLowerCorner();
		assertTrue(lc.getOrdinate(0) == 0.0);
		assertTrue(lc.getOrdinate(1) == -115000.0);
		assertTrue(lc.getOrdinate(2) == 0.0);
		DirectPosition uc = env1.getUpperCorner();
		assertTrue(uc.getOrdinate(0) == 10.0);
		assertTrue(uc.getOrdinate(1) == 0.0);
		assertTrue(uc.getOrdinate(2) == 0.0000000125);
		env1 = cf.createEnvelope(dp2, dp1);
		lc = env1.getLowerCorner();
		assertTrue(lc.getOrdinate(0) == 0.0);
		assertTrue(lc.getOrdinate(1) == -115000.0);
		assertTrue(lc.getOrdinate(2) == 0.0);
		uc = env1.getUpperCorner();
		assertTrue(uc.getOrdinate(0) == 10.0);
		assertTrue(uc.getOrdinate(1) == 0.0);
		assertTrue(uc.getOrdinate(2) == 0.0000000125);
		
		// public Position createPosition(DirectPosition dp);
		Position pos1 = cf.createPosition(dp2);
		assertTrue(pos1.getPosition().getOrdinate(0) == 10.0);
		assertTrue(pos1.getPosition().getOrdinate(1) == -115000.0);
		assertTrue(pos1.getPosition().getOrdinate(2) == 0.0000000125);

		// public LineSegment createLineSegment(Position startPoint, Position endPoint);
		Position pos2 = cf.createPosition(dp1);
		LineSegment seg1 = cf.createLineSegment(pos1, pos2);
		assertTrue(seg1.getEndPoint().getOrdinate(0) == 0.0);
		assertTrue(seg1.getEndPoint().getOrdinate(1) == 0.0);
		assertTrue(seg1.getEndPoint().getOrdinate(2) == 0.0);
		assertTrue(seg1.getStartPoint().getOrdinate(0) == 10.0);
		assertTrue(seg1.getStartPoint().getOrdinate(1) == -115000.0);
		assertTrue(seg1.getStartPoint().getOrdinate(2) == 0.0000000125);
		

		
		
	}

}
