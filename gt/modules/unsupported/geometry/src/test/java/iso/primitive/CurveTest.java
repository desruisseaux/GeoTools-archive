package iso.primitive;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.geotools.geometry.iso.FeatGeomFactoryImpl;
import org.geotools.geometry.iso.coordinate.CoordinateFactoryImpl;
import org.geotools.geometry.iso.coordinate.LineStringImpl;
import org.geotools.geometry.iso.coordinate.PositionImpl;
import org.geotools.geometry.iso.primitive.CurveImpl;
import org.geotools.geometry.iso.primitive.PrimitiveFactoryImpl;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.complex.Complex;
import org.opengis.spatialschema.geometry.complex.CompositeCurve;
import org.opengis.spatialschema.geometry.geometry.Position;
import org.opengis.spatialschema.geometry.primitive.CurveBoundary;
import org.opengis.spatialschema.geometry.primitive.CurveSegment;

/**
 * @author sanjay
 *
 */
public class CurveTest extends TestCase {
	
	public void testMain() {
		
		FeatGeomFactoryImpl tGeomFactory = FeatGeomFactoryImpl.getDefault2D();
		
		this._testCurve(tGeomFactory);
		
	}
	
	
	private void _testCurve(FeatGeomFactoryImpl aGeomFactory) {
		
		CoordinateFactoryImpl tCoordFactory = aGeomFactory.getCoordinateFactory();
		PrimitiveFactoryImpl tPrimFactory = aGeomFactory.getPrimitiveFactory();
		
		PositionImpl p1 = new PositionImpl(tCoordFactory.createDirectPosition(new double[]{-50,  0}));
		PositionImpl p2 = new PositionImpl(tCoordFactory.createDirectPosition(new double[]{-30,  30}));
		PositionImpl p3 = new PositionImpl(tCoordFactory.createDirectPosition(new double[]{0,  50}));
		PositionImpl p4 = new PositionImpl(tCoordFactory.createDirectPosition(new double[]{30,  30}));
		PositionImpl p5 = new PositionImpl(tCoordFactory.createDirectPosition(new double[]{50,  0}));

		LineStringImpl line1 = null;
		
		ArrayList<Position> positionList = new ArrayList<Position>();
		positionList .add(p1);
		positionList.add(p2);
		positionList.add(p3);
		positionList.add(p4);
		positionList.add(p5);
		line1 = tCoordFactory.createLineString(positionList); 

		/* Set parent curve for LineString */
		ArrayList<CurveSegment> tLineList = new ArrayList<CurveSegment>();
		tLineList.add(line1);

		// PrimitiveFactory.createCurve(List<CurveSegment>)
		CurveImpl curve1 = tPrimFactory.createCurve(tLineList);
		System.out.println("\nCurve1: " + curve1);
		
		// Set curve for further LineString tests
		line1.setCurve(curve1);

		System.out.println("\n*** TEST: Curve\n" + curve1);
		
		// ***** getStartPoint()
		System.out.println("\n*** TEST: .getStartPoint()\n" + curve1.getStartPoint());
		assertTrue(curve1.getStartPoint().getOrdinate(0) == -50);
		assertTrue(curve1.getStartPoint().getOrdinate(1) == 0);
		
		// ***** getEndPoint()
		System.out.println("\n*** TEST: .getEndPoint()\n" + curve1.getEndPoint());
		assertTrue(curve1.getEndPoint().getOrdinate(0) == 50);
		assertTrue(curve1.getEndPoint().getOrdinate(1) == 0);

		// ***** getStartParam()
		System.out.println("\n*** TEST: .getStartParam()\n" + curve1.getStartParam());
		assertTrue(curve1.getStartParam() == 0.0);

		// ***** getEndParam()
		System.out.println("\n*** TEST: .getEndParam()\n" + curve1.getEndParam());
		assertTrue(Math.round(line1.getEndParam()) == 144.0);

		// ***** getStartConstructiveParam()
		System.out.println("\n*** TEST: .getStartConstructiveParam()\n" + curve1.getStartConstructiveParam());
		assertTrue(curve1.getStartConstructiveParam() == 0.0);

		// ***** getEndConstructiveParam()
		System.out.println("\n*** TEST: .getEndConstructiveParam()\n" + curve1.getEndConstructiveParam());
		assertTrue(curve1.getEndConstructiveParam() == 1.0);

		// ***** getBoundary()
		System.out.println("\n*** TEST: .getBoundary()\n" + curve1.getBoundary());
		CurveBoundary cb = curve1.getBoundary();
		assertTrue(cb != null);
		double[] dp = cb.getStartPoint().getPosition().getCoordinates();
		assertTrue(dp[0] == -50);
		assertTrue(dp[1] == 0);
		dp = cb.getEndPoint().getPosition().getCoordinates();
		assertTrue(dp[0] == 50);
		assertTrue(dp[1] == 0);
		
		// ***** getEnvelope()
		System.out.println("\n*** TEST: .getEnvelope()\n" + curve1.getEnvelope());
		assertTrue(curve1.getEnvelope() != null);
		dp = curve1.getEnvelope().getLowerCorner().getCoordinates();
		assertTrue(dp[0] == -50);
		assertTrue(dp[1] == 0);
		dp = curve1.getEnvelope().getUpperCorner().getCoordinates();
		assertTrue(dp[0] == 50);
		assertTrue(dp[1] == 50);
		
		// ***** forParam(double distance) : DirectPosition
		dp = curve1.forParam(0).getCoordinates();
		assertTrue(dp[0] == -50);
		assertTrue(dp[1] == 0.0);
		
		dp = curve1.forParam(curve1.length()).getCoordinates();
		assertTrue(dp[0] == 50);
		assertTrue(dp[1] == 0.0);
		
		dp = curve1.forParam(50).getCoordinates();
		//System.out.println("forParam: " + dp[0] + "," + dp[1]);
		assertTrue(Math.round(dp[0]*1000) == -18397);
		assertTrue(Math.round(dp[1]*1000) == 37735);

		// ***** forConstructiveParam(double distance)
		dp = curve1.forConstructiveParam(0.0).getCoordinates();
		assertTrue(dp[0] == -50);
		assertTrue(dp[1] == 0.0);

		dp = curve1.forConstructiveParam(1.0).getCoordinates();
		assertTrue(dp[0] == 50);
		assertTrue(dp[1] == 0.0);
		
		dp = curve1.forConstructiveParam(50 / curve1.length()).getCoordinates();
		assertTrue(Math.round(dp[0]*1000) == -18397);
		assertTrue(Math.round(dp[1]*1000) == 37735);
		
		// ***** getTangent(double distance)
		dp = curve1.getTangent(0);
		//System.out.println("tangent: " + dp[0] + "," + dp[1]);
		assertTrue(Math.round(dp[0]*1000) == -49445);
		assertTrue(Math.round(dp[1]*1000) == 832);

		dp = curve1.getTangent(40);
		assertTrue(Math.round(dp[0]*100) == -2589);
		assertTrue(Math.round(dp[1]*100) == 3274);

		dp = curve1.getTangent(curve1.getEndParam());
		System.out.println("tangent: " + dp[0] + "," + dp[1]);
		assertTrue(Math.round(dp[0]*100) == 5055);
		assertTrue(Math.round(dp[1]*100) == -83);
		
		// ***** getRepresentativePoint()
		dp = curve1.getRepresentativePoint().getCoordinates();
		assertTrue(dp[0] == -50);
		assertTrue(dp[1] == 0.0);
		
		
		// ***** Curve.Merge(Curve)
		
		DirectPosition p6 = tCoordFactory.createDirectPosition(new double[]{80,  20});
		DirectPosition p7 = tCoordFactory.createDirectPosition(new double[]{130,  60});
		
		List<DirectPosition> directPositions = new ArrayList<DirectPosition>();
		
		directPositions.add(p5.getPosition());
		directPositions.add(p6);
		directPositions.add(p7);
		CurveImpl curve2 = (CurveImpl) tPrimFactory.createCurveByDirectPositions(directPositions);
		
		CurveImpl curve3 = curve1.merge(curve2);
		System.out.println("Curve1: " + curve1);
		System.out.println("Curve2: " + curve2);
		System.out.println("Merge: " + curve3);
		// Lists of line1 and line2 are not modified
		assertTrue(curve1.asDirectPositions().size() == 5);
		assertTrue(curve2.asDirectPositions().size() == 3);
		// New LineString has combined positions
		assertTrue(curve3.asDirectPositions().size() == 7);

		curve3 = curve2.merge(curve1);
		System.out.println("Curve1: " + curve1);
		System.out.println("Curve2: " + curve2);
		System.out.println("Merge: " + curve3);
		// Lists of line1 and line2 are not modified
		assertTrue(curve1.asDirectPositions().size() == 5);
		assertTrue(curve2.asDirectPositions().size() == 3);
		// New LineString has combined positions
		assertTrue(curve3.asDirectPositions().size() == 7);
		
		directPositions.remove(0);
		curve2 = (CurveImpl) tPrimFactory.createCurveByDirectPositions(directPositions);
		curve3 =  null;
		try {
			curve3 = curve2.merge(curve1);
		} catch (IllegalArgumentException e){
			//
		}
		// Merge of two not touching linestrings does not work
		assertTrue(curve3==null);
		
		Complex cc1 = curve1.getClosure();
		System.out.println(cc1);
		assertTrue(cc1 instanceof CompositeCurve);

		
	}

}
