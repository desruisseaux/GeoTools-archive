package iso.primitive;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.geotools.geometry.iso.FeatGeomFactoryImpl;
import org.geotools.geometry.iso.coordinate.CoordinateFactoryImpl;
import org.geotools.geometry.iso.coordinate.DirectPositionImpl;
import org.geotools.geometry.iso.coordinate.PositionImpl;
import org.geotools.geometry.iso.primitive.CurveImpl;
import org.geotools.geometry.iso.primitive.PrimitiveFactoryImpl;
import org.geotools.geometry.iso.primitive.RingImpl;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.geometry.Position;
import org.opengis.spatialschema.geometry.primitive.CurveBoundary;
import org.opengis.spatialschema.geometry.primitive.CurveSegment;
import org.opengis.spatialschema.geometry.primitive.OrientableCurve;
import org.opengis.spatialschema.geometry.primitive.Ring;
import org.opengis.spatialschema.geometry.primitive.SurfaceBoundary;

/**
 * @author sanjay
 *
 */
public class BoundaryTest extends TestCase {
	
	public void testMain() {
		
		FeatGeomFactoryImpl tGeomFactory = FeatGeomFactoryImpl.getDefault2D();
		
		this._testCurveBoundary1(tGeomFactory);
		this._testSurfaceBoundary1(tGeomFactory);
		
	}

	private void _testCurveBoundary1(FeatGeomFactoryImpl aGeomFactory) {

		CoordinateFactoryImpl tCoordFactory = aGeomFactory.getCoordinateFactory();
		PrimitiveFactoryImpl tPrimitiveFactory = aGeomFactory.getPrimitiveFactory();
		
		DirectPositionImpl dp1 = tCoordFactory.createDirectPosition(new double[] {0, 0});
		DirectPositionImpl dp2 = tCoordFactory.createDirectPosition(new double[] {100, 100});
		
		CurveBoundary curveBoundary1 = tPrimitiveFactory.createCurveBoundary(dp1, dp2);
		
		System.out.println(curveBoundary1);

		// RepresentativePoint()
		DirectPosition dp = curveBoundary1.getRepresentativePoint();
		assertTrue(dp.getOrdinate(0) == 0);
		assertTrue(dp.getOrdinate(1) == 0);

	}

	private void _testSurfaceBoundary1(FeatGeomFactoryImpl aGeomFactory) {

		CoordinateFactoryImpl tCoordFactory = aGeomFactory.getCoordinateFactory();
		PrimitiveFactoryImpl tPrimFactory = aGeomFactory.getPrimitiveFactory();

		/* Defining Positions for LineStrings */
		ArrayList<Position> line1 = new ArrayList<Position>();
		line1.add(new PositionImpl(tCoordFactory.createDirectPosition(new double[]{50, 20})));
		line1.add(new PositionImpl(tCoordFactory.createDirectPosition(new double[]{30, 30})));
		line1.add(new PositionImpl(tCoordFactory.createDirectPosition(new double[]{20, 50})));
		line1.add(new PositionImpl(tCoordFactory.createDirectPosition(new double[]{20, 70})));

		ArrayList<Position> line2 = new ArrayList<Position>();
		line2.add(new PositionImpl(tCoordFactory.createDirectPosition(new double[]{20, 70})));
		line2.add(new PositionImpl(tCoordFactory.createDirectPosition(new double[]{40, 80})));
		line2.add(new PositionImpl(tCoordFactory.createDirectPosition(new double[]{70, 80})));

		ArrayList<Position> line3 = new ArrayList<Position>();
		line3.add(new PositionImpl(tCoordFactory.createDirectPosition(new double[]{70, 80})));
		line3.add(new PositionImpl(tCoordFactory.createDirectPosition(new double[]{90, 70})));
		line3.add(new PositionImpl(tCoordFactory.createDirectPosition(new double[]{100, 60})));
		line3.add(new PositionImpl(tCoordFactory.createDirectPosition(new double[]{100, 40})));

		ArrayList<Position> line4 = new ArrayList<Position>();
		line4.add(new PositionImpl(tCoordFactory.createDirectPosition(new double[]{100, 40})));
		line4.add(new PositionImpl(tCoordFactory.createDirectPosition(new double[]{80, 30})));
		line4.add(new PositionImpl(tCoordFactory.createDirectPosition(new double[]{50, 20})));
		
		/* Setting up Array of these LineStrings */
		ArrayList<CurveSegment> tLineList1 = new ArrayList<CurveSegment>();
		tLineList1.add(tCoordFactory.createLineString(line1));
		tLineList1.add(tCoordFactory.createLineString(line2)); 

		ArrayList<CurveSegment> tLineList2 = new ArrayList<CurveSegment>();
		tLineList2.add(tCoordFactory.createLineString(line3)); 
		tLineList2.add(tCoordFactory.createLineString(line4)); 

		/* Build Curve */
		CurveImpl curve1 = tPrimFactory.createCurve(tLineList1);
		CurveImpl curve2 = tPrimFactory.createCurve(tLineList2);

		
		/* Build Ring */
		ArrayList<OrientableCurve> curveList = new ArrayList<OrientableCurve>();
		curveList.add(curve1);
		curveList.add(curve2);
		
		RingImpl exteriorring1 = tPrimFactory.createRing(curveList);

		System.out.println(exteriorring1);
		
		List<Ring> interiors = new ArrayList<Ring>();
		
		SurfaceBoundary surfaceBoundary1 = tPrimFactory.createSurfaceBoundary(exteriorring1, interiors);
		
		System.out.println(surfaceBoundary1);
		
		
		// clone()
		SurfaceBoundary surfaceBoundary2 = null;
		try {
			surfaceBoundary2 = (SurfaceBoundary) surfaceBoundary1.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		assertTrue(surfaceBoundary1 != surfaceBoundary2);
		assertTrue(surfaceBoundary1.getExterior() != surfaceBoundary2.getExterior());
		if (surfaceBoundary1.getInteriors().size() > 0) {
			assertTrue(surfaceBoundary1.getInteriors().get(0) != surfaceBoundary2.getInteriors().get(0));
		}
		
		// RepresentativePoint()
		DirectPosition dp = surfaceBoundary1.getRepresentativePoint();
		assertTrue(dp.getOrdinate(0) == 50);
		assertTrue(dp.getOrdinate(1) == 20);

		
	}

		

	
	
}
