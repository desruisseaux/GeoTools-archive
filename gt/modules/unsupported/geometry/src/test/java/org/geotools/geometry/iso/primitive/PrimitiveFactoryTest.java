package org.geotools.geometry.iso.primitive;

import junit.framework.TestCase;

import org.geotools.geometry.iso.FeatGeomFactoryImpl;
import org.geotools.geometry.iso.primitive.PrimitiveImpl;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.spatialschema.geometry.geometry.GeometryFactory;
import org.opengis.spatialschema.geometry.geometry.Position;
import org.opengis.spatialschema.geometry.primitive.Point;
import org.opengis.spatialschema.geometry.primitive.PrimitiveFactory;

public class PrimitiveFactoryTest extends TestCase {
	
	public void testMain() {
		
		FeatGeomFactoryImpl tGeomFactory2o5D = FeatGeomFactoryImpl.getDefault2o5D();
		FeatGeomFactoryImpl tGeomFactory3D = FeatGeomFactoryImpl.getDefault3D();
		
		this._testPrimitiveObjects2D(tGeomFactory2o5D);
		this._testPrimitiveObjects3D(tGeomFactory3D);
		
	}
	
	private void _testPrimitiveObjects2D(FeatGeomFactoryImpl aFactory) {
		
		PrimitiveFactory pf = aFactory.getPrimitiveFactory();
		GeometryFactory cf = aFactory.getCoordinateFactory();
		
		// public PrimitiveImpl createPrimitive(Envelope envelope);
		// indirect: public SurfaceImpl createSurface(SurfaceBoundary boundary);
		// indirect: public SurfaceImpl createSurfaceByDirectPositions(List<DirectPosition> positions);
		// indirect: public SurfaceBoundaryImpl createSurfaceBoundary(Ring exterior, List<Ring> interiors);
		// indirect: public Ring createRingByDirectPositions(List<DirectPosition> directPositions);
		DirectPosition dp1 = cf.createDirectPosition(new double[]{10, 10, 10});
		DirectPosition dp2 = cf.createDirectPosition(new double[]{70, 30, 90});
		Envelope env1 = cf.createEnvelope(dp1, dp2);
		PrimitiveImpl prim1 = (PrimitiveImpl) pf.createPrimitive(env1);
		System.out.println(prim1);
		
	}


	private void _testPrimitiveObjects3D(FeatGeomFactoryImpl aFactory) {
		
		PrimitiveFactory pf = aFactory.getPrimitiveFactory();
		GeometryFactory cf = aFactory.getCoordinateFactory();
		
		// public PointImpl createPoint(double[] coord);
		double[] da = new double[3];
		da[0] = 10.0;
		da[1] = -115000.0;
		da[2] = 0.0000000125;
		Point p1 = pf.createPoint(da);
		assertTrue(p1.getPosition().getOrdinate(0) == 10.0);
		assertTrue(p1.getPosition().getOrdinate(1) == -115000.0);
		assertTrue(p1.getPosition().getOrdinate(2) == 0.0000000125);

		// public PointImpl createPoint(Position position);
		// public PointImpl createPoint(DirectPositionImpl dp);
		da[0] = 999999999.0;
		da[1] = 100.0;
		da[2] = -0.00000565;
		Position pos1 = cf.createPosition(cf.createDirectPosition(da));
		Point p2 = pf.createPoint(pos1);
		assertTrue(p2.getPosition().getOrdinate(0) == 999999999.0);
		assertTrue(p2.getPosition().getOrdinate(1) == 100.0);
		assertTrue(p2.getPosition().getOrdinate(2) == -0.00000565);

		


		
	}

}
