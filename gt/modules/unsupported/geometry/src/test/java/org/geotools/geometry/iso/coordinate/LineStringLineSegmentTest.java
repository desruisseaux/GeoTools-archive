package org.geotools.geometry.iso.coordinate;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.geotools.geometry.iso.FeatGeomFactoryImpl;
import org.geotools.geometry.iso.coordinate.CoordinateFactoryImpl;
import org.geotools.geometry.iso.coordinate.LineSegmentImpl;
import org.geotools.geometry.iso.coordinate.LineStringImpl;
import org.geotools.geometry.iso.coordinate.PositionImpl;
import org.geotools.geometry.iso.primitive.CurveImpl;
import org.geotools.geometry.iso.primitive.PrimitiveFactoryImpl;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.geometry.LineSegment;
import org.opengis.spatialschema.geometry.geometry.PointArray;
import org.opengis.spatialschema.geometry.geometry.Position;
import org.opengis.spatialschema.geometry.primitive.CurveSegment;

/**
 * Test case for LineString and LineSegment
 * 
 * @author Sanjay Jena
 *
 */
public class LineStringLineSegmentTest extends TestCase {
	
	public void testMain() {
		
		FeatGeomFactoryImpl tGeomFactory = FeatGeomFactoryImpl.getDefault2D();
		
		this._testLineString1(tGeomFactory);
		
	}
	
	
	private void _testLineString1(FeatGeomFactoryImpl aGeomFactory) {
		
		CoordinateFactoryImpl tCoordFactory = aGeomFactory.getCoordinateFactory();
		PrimitiveFactoryImpl tPrimFactory = aGeomFactory.getPrimitiveFactory();
		
		PositionImpl p1 = new PositionImpl(tCoordFactory.createDirectPosition(new double[]{-50,  0}));
		PositionImpl p2 = new PositionImpl(tCoordFactory.createDirectPosition(new double[]{-30,  30}));
		PositionImpl p3 = new PositionImpl(tCoordFactory.createDirectPosition(new double[]{0,  50}));
		PositionImpl p4 = new PositionImpl(tCoordFactory.createDirectPosition(new double[]{30,  30}));
		PositionImpl p5 = new PositionImpl(tCoordFactory.createDirectPosition(new double[]{50,  0}));

		LineStringImpl line1 = null;
		
		/* Testing constructor of LineString with Array with size of 0 */
		
		System.out.println("\n***** TEST: Constructors");
		//PositionImpl arrayOfPoints[] = new PositionImpl[0];
		ArrayList<Position> positionList = new ArrayList<Position>();
		try {
			line1 = tCoordFactory.createLineString(positionList); 
		} catch (IllegalArgumentException e) {
			System.out.println("LineStringImpl - Number of Positions in array: 0 - Not accepted");
		}
		assertTrue(line1 == null);

		/* Testing constructor of LineString with Array with size of 1 */

		positionList.add(p1);
		try {
			line1 = tCoordFactory.createLineString(positionList); 
		} catch (IllegalArgumentException e) {
			System.out.println("LineStringImpl - Number of Positions in array: 1 - Not accepted");
		}
		assertTrue(line1 == null);

		/* Testing constructor of LineString with Array with size of 2 */

		positionList.add(p2);
		try {
			line1 = tCoordFactory.createLineString(positionList); 
			System.out.println("LineStringImpl - Number of Positions in array: 2 - accepted");
		} catch (IllegalArgumentException e) {
			System.out.println("LineStringImpl - Number of Positions in array: 2 - Not accepted");
		}
		assertTrue(line1 != null);
		
		/* Testing constructor of LineString with Array with size of 5 */

		positionList.add(p3);
		positionList.add(p4);
		positionList.add(p5);
		try {
			line1 = tCoordFactory.createLineString(positionList); 
			System.out.println("LineStringImpl - Number of Positions in array: 5 - accepted");
			System.out.println("\n" + line1);

		} catch (IllegalArgumentException e) {
			System.out.println("\nLineStringImpl - Number of Positions in array: 5 - Not accepted");
		}
		assertTrue(line1 != null);

		// ***** getEnvelope()
		System.out.println("\n***** TEST: .envelope()");
		System.out.println("Envelope of the LineString is " +  line1.getEnvelope());

		// ***** getStartPoint();
		System.out.println("\n***** TEST: .startPoint()");
		System.out.println("StartPoint: " + line1.getStartPoint());
		assertTrue(line1.getStartPoint().getOrdinate(0) == -50);
		assertTrue(line1.getStartPoint().getOrdinate(1) == 0);

		// ***** getEndPoint();
		System.out.println("\n***** TEST: .endPoint()");
		System.out.println("EndPoint: " + line1.getEndPoint());
		assertTrue(line1.getEndPoint().getOrdinate(0) == 50);
		assertTrue(line1.getEndPoint().getOrdinate(1) == 0);
		
		// Set curve for further LineString tests
		ArrayList<CurveSegment> tLineList = new ArrayList<CurveSegment>();
		tLineList.add(line1);
		
		CurveImpl curve1 = tPrimFactory.createCurve(tLineList);
		line1.setCurve(curve1);
		
		// ***** length()
		System.out.println("\n***** TEST: .length()");
		System.out.println("Length of LineString is " + line1.length());
		assertTrue(Math.round(line1.length() * 100) == 14422.0);

		// ***** getStartParam();
		System.out.println("\n***** TEST: .startParam()");
		System.out.println("StartParam: " + line1.getStartParam());
		assertTrue(line1.getStartParam() == 0.0);

		// ***** getEndParam();
		System.out.println("\n***** TEST: .endParam()");
		System.out.println("EndParam: " + line1.getEndParam());
		assertTrue(Math.round(line1.getEndParam() * 100) == 14422.0);

		// ***** getStartConstructiveParam();
		System.out.println("\n***** TEST: .startConstrParam()");
		System.out.println("ConstrStartParam: " + line1.getStartConstructiveParam());
		assertTrue(line1.getStartConstructiveParam() == 0.0);
		
		// ***** getEndConstructiveParam();
		System.out.println("\n***** TEST: .endConstrParam()");
		System.out.println("ConstrEndParam: " + line1.getEndConstructiveParam());
		assertTrue(line1.getEndConstructiveParam() == 1.0);

		
		
		// Receive LineSegments from LineString
		List<LineSegment> segments = line1.asLineSegments();
		assertTrue(segments.size() == 4);

		LineSegment seg1 = segments.get(0);
		LineSegment seg2 = segments.get(1);
		LineSegment seg3 = segments.get(2);
		LineSegment seg4 = segments.get(3);

		System.out.println("LineSegment: " + seg1);	
		System.out.println("LineSegment: " + seg2);	

		// ***** LineSegment.getStartParam()
		System.out.println(seg1.getStartParam());
		assertTrue(seg1.getStartParam() == 0.0);
		
		// ***** LineSegment.getEndParam()
		System.out.println(seg1.getEndParam());
		assertTrue(Math.round(seg1.getEndParam()) == 36.0);

		System.out.println(seg2.getStartParam());
		assertTrue(Math.round(seg2.getStartParam()) == 36.0);
		
		System.out.println(seg2.getEndParam());
		assertTrue(Math.round(seg2.getEndParam()) == 72.0);
		
		// ***** LineSegment.getStartConstructiveParam()
		// ***** LineSegment.getEndConstructiveParam()
		System.out.println(seg1.getStartConstructiveParam());
		assertTrue(seg1.getStartConstructiveParam() == 0.0);
		System.out.println(seg1.getEndConstructiveParam());
		assertTrue(seg1.getEndConstructiveParam() == 0.25);
		assertTrue(segments.get(1).getStartConstructiveParam() == 0.25);
		assertTrue(segments.get(1).getEndConstructiveParam() == 0.50);
		assertTrue(segments.get(2).getStartConstructiveParam() == 0.50);
		assertTrue(segments.get(2).getEndConstructiveParam() == 0.75);
		assertTrue(segments.get(3).getStartConstructiveParam() == 0.75);
		assertTrue(segments.get(3).getEndConstructiveParam() == 1.0);


		// ***** LineSegment.forParam(double)
		// Parameter for forParam() is 0.0 (startparam)
		DirectPosition resultPos = seg1.forParam(0.0);
		System.out.println(resultPos);
		assertTrue(resultPos.getOrdinate(0) == -50.0);
		assertTrue(resultPos.getOrdinate(1) == 0.0);

		// Parameter for forParam() is endparam
		resultPos = seg1.forParam(seg1.getEndParam());
		System.out.println(resultPos);
		assertTrue(resultPos.getOrdinate(0) == -30.0);
		assertTrue(resultPos.getOrdinate(1) == 30.0);

		// Parameter for startParam out of param range
		resultPos = null;
		try {
			resultPos = seg1.forParam(180);
		} catch(IllegalArgumentException e) {
			// Shall throw exception
		}
		System.out.println(resultPos);
		assertTrue(resultPos == null);

		resultPos = seg1.forParam(30);
		System.out.println(resultPos);
		
		// ***** LineSegment.getControlPoints()
		assertTrue(seg1.getControlPoints().length() == 2);
		
		// ***** LineSegment.asLineSegments()
		assertTrue(seg2.asLineSegments().size() == 1);
		
		// ***** forParam(double distance)
		double[] dp = line1.forParam(0).getCoordinates();
		assertTrue(dp[0] == -50);
		assertTrue(dp[1] == 0.0);
		
		dp = line1.forParam(line1.length()).getCoordinates();
		assertTrue(dp[0] == 50);
		assertTrue(dp[1] == 0.0);
		
		dp = line1.forParam(seg1.getEndParam()).getCoordinates();
		assertTrue(dp[0] == -30);
		assertTrue(dp[1] == 30.0);

		dp = line1.forParam(50).getCoordinates();
		//System.out.println("forParam: " + dp[0] + "," + dp[1]);
		assertTrue(Math.round(dp[0]*1000) == -18397);
		assertTrue(Math.round(dp[1]*1000) == 37735);

		// ***** forConstructiveParam(double distance)
		dp = line1.forConstructiveParam(0.0).getCoordinates();
		assertTrue(dp[0] == -50);
		assertTrue(dp[1] == 0.0);

		dp = line1.forConstructiveParam(1.0).getCoordinates();
		assertTrue(dp[0] == 50);
		assertTrue(dp[1] == 0.0);
		
		dp = line1.forConstructiveParam(50 / line1.length()).getCoordinates();
		assertTrue(Math.round(dp[0]*1000) == -18397);
		assertTrue(Math.round(dp[1]*1000) == 37735);

		// ***** getTangent(double distance)
		dp = line1.getTangent(0);
		//System.out.println("tangent: " + dp[0] + "," + dp[1]);
		assertTrue(Math.round(dp[0]*1000) == -49445);
		assertTrue(Math.round(dp[1]*1000) == 832);

		dp = line1.getTangent(40);
		assertTrue(Math.round(dp[0]*100) == -2589);
		assertTrue(Math.round(dp[1]*100) == 3274);

		dp = line1.getTangent(line1.getEndParam());
		System.out.println("tangent: " + dp[0] + "," + dp[1]);
		assertTrue(Math.round(dp[0]*100) == 5055);
		assertTrue(Math.round(dp[1]*100) == -83);
		
		
		
		
		// ***** merge(LineString)
		PositionImpl p6 = new PositionImpl(tCoordFactory.createDirectPosition(new double[]{80,  40}));
		PositionImpl p7 = new PositionImpl(tCoordFactory.createDirectPosition(new double[]{130,  60}));
		ArrayList<Position> positionList2 = new ArrayList<Position>();
		positionList2.add(p5);
		positionList2.add(p6);
		positionList2.add(p7);
		LineStringImpl line2 = tCoordFactory.createLineString(positionList2);
		
		LineStringImpl line3 = line1.merge(line2);
		System.out.println("Line1: " + line1);
		System.out.println("Line2: " + line2);
		System.out.println("MergedLine: " + line3);
		// Lists of line1 and line2 are not modified
		assertTrue(line1.getControlPoints().positions().size() == 5);
		assertTrue(line2.getControlPoints().positions().size() == 3);
		// New LineString has combined positions
		assertTrue(line3.getControlPoints().positions().size() == 7);
		
		line3 = line2.merge(line1);
		System.out.println("MergedLine: " + line3);
		// Lists of line1 and line2 are not modified
		assertTrue(line1.getControlPoints().positions().size() == 5);
		assertTrue(line2.getControlPoints().positions().size() == 3);
		// New LineString has combined positions
		assertTrue(line3.getControlPoints().positions().size() == 7);

		positionList2.remove(0);
		line3 =  null;
		try {
			line3 = line2.merge(line1);
		} catch (IllegalArgumentException e){
			// the exception shall be thrown, hence do nothing
		}
		// Merge of two not touching linestrings does not work
		assertTrue(line3==null);
		
		// ***** getNumDerivatesAtStart()
		assertTrue(line1.getNumDerivativesAtStart() == 0);
		// ***** getNumDerivativesInterior()
		assertTrue(line1.getNumDerivativesInterior() == 0);
		// ***** getNumDerivativesAtEnd()
		assertTrue(line1.getNumDerivativesAtEnd() == 0);
		
		// ***** reverse()
		line1.reverse();
		// number of control points is unchanged
		PointArray controlPoints = line1.getControlPoints();
		assertTrue(controlPoints.length() == 5);
		// control points are in opposite order
		assertTrue(controlPoints.get(0, null).equals(p5.getPosition()));
		assertTrue(controlPoints.get(1, null).equals(p4.getPosition()));
		assertTrue(controlPoints.get(2, null).equals(p3.getPosition()));
		assertTrue(controlPoints.get(3, null).equals(p2.getPosition()));
		assertTrue(controlPoints.get(4, null).equals(p1.getPosition()));
		System.out.println("Reversed. Line1: " + line1);

	}

}
