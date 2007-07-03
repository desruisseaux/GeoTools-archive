package org.geotools.geometry.iso.operations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geotools.geometry.iso.PositionFactoryImpl;
import org.geotools.geometry.iso.PrecisionModel;
import org.geotools.geometry.iso.coordinate.GeometryFactoryImpl;
import org.geotools.geometry.iso.primitive.CurveImpl;
import org.geotools.geometry.iso.primitive.PointImpl;
import org.geotools.geometry.iso.primitive.PrimitiveFactoryImpl;
import org.geotools.geometry.iso.primitive.RingImpl;
import org.geotools.geometry.iso.primitive.SurfaceImpl;
import org.geotools.referencing.CRS;
import org.opengis.geometry.PositionFactory;
import org.opengis.geometry.coordinate.GeometryFactory;
import org.opengis.geometry.coordinate.LineString;
import org.opengis.geometry.coordinate.Position;
import org.opengis.geometry.coordinate.Triangle;
import org.opengis.geometry.primitive.OrientableCurve;
import org.opengis.geometry.primitive.PrimitiveFactory;
import org.opengis.geometry.primitive.Ring;
import org.opengis.geometry.primitive.Surface;
import org.opengis.geometry.primitive.SurfaceBoundary;
import org.opengis.geometry.primitive.SurfacePatch;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import junit.framework.TestCase;

public class TransformTest extends TestCase {
	
	private CoordinateReferenceSystem crs1;
	private CoordinateReferenceSystem crs2;
	
	public void setUp() throws Exception {
		this.crs1 = CRS.decode("EPSG:4326");
		this.crs2 = CRS.decode("EPSG:3005");
	}

	public void testPoint() throws Exception {

		PositionFactory positionFactory = new PositionFactoryImpl(crs1, new PrecisionModel());
		PrimitiveFactory primitiveFactory = new PrimitiveFactoryImpl(crs1, positionFactory);
		
		PointImpl point1 = (PointImpl) primitiveFactory.createPoint( new double[]{-123.47009555832284, 48.543261561072285} );
		PointImpl point2 = (PointImpl) point1.transform(crs2);
		
		// create expected result
		PositionFactory expectedPosF2 = new PositionFactoryImpl(crs2, new PrecisionModel());
		PrimitiveFactory expectedPrimF2 = new PrimitiveFactoryImpl(crs2, expectedPosF2);
		
		PointImpl expectedPoint2 = (PointImpl) expectedPrimF2.createPoint( new double[]{1187128.000000001, 395268.0000000004} );
		
		//System.out.println(point1);
		//System.out.println(point2);
		//System.out.println(expectedPoint2);
		
		assertTrue(point2.equals(expectedPoint2));
		
	}
	
	public void testCurve() throws Exception {

		PositionFactory positionFactory = new PositionFactoryImpl(crs1, new PrecisionModel());
		PrimitiveFactory primitiveFactory = new PrimitiveFactoryImpl(crs1, positionFactory);
		GeometryFactory geometryFactory = new GeometryFactoryImpl(crs1, positionFactory);
		
		List<Position> points = new ArrayList<Position>();
		points.add(primitiveFactory.createPoint( new double[]{-123.47009555832284,48.543261561072285} ));
		points.add(primitiveFactory.createPoint( new double[]{-123.46972894676578,48.55009592117936} ));
		points.add(primitiveFactory.createPoint( new double[]{-123.45463828850829,48.54973520267305} ));
		points.add(primitiveFactory.createPoint( new double[]{-123.4550070827961,48.54290089070186} ));
        LineString lineString = geometryFactory.createLineString(points);
        List curveSegmentList = Collections.singletonList(lineString);
        
        CurveImpl curve1 = (CurveImpl) primitiveFactory.createCurve(curveSegmentList);
        CurveImpl curve2 = (CurveImpl) curve1.transform(crs2);
        
		// create expected result
		PositionFactory expectedPosF2 = new PositionFactoryImpl(crs2, new PrecisionModel());
		PrimitiveFactory expectedPrimF2 = new PrimitiveFactoryImpl(crs2, expectedPosF2);
		GeometryFactory ExpectedGeomF2 = new GeometryFactoryImpl(crs2, expectedPosF2);
		
		List<Position> expectedPoints = new ArrayList<Position>();
		expectedPoints.add(expectedPrimF2.createPoint( new double[]{1187128.000000001, 395268.0000000004} ));
		expectedPoints.add(expectedPrimF2.createPoint( new double[]{1187127.9999999998, 396026.99999999825} ));
		expectedPoints.add(expectedPrimF2.createPoint( new double[]{1188245.0000000007, 396027.0000000039} ));
		expectedPoints.add(expectedPrimF2.createPoint( new double[]{1188245.0000000005, 395268.0000000018} ));
        LineString expectedLineString = ExpectedGeomF2.createLineString(expectedPoints);
        List expectedCurveSegmentList = Collections.singletonList(expectedLineString);
        
        CurveImpl expectedCurve = (CurveImpl) expectedPrimF2.createCurve(expectedCurveSegmentList);
		
//		System.out.println(curve1);
//		System.out.println(curve2);
//		System.out.println(expectedCurve);
		
		assertTrue(curve2.equals(expectedCurve));
	}
	
	public void testRing() throws Exception {

		PositionFactory positionFactory = new PositionFactoryImpl(crs1, new PrecisionModel());
		PrimitiveFactory primitiveFactory = new PrimitiveFactoryImpl(crs1, positionFactory);
		GeometryFactory geometryFactory = new GeometryFactoryImpl(crs1, positionFactory);
		
		List<Position> points1 = new ArrayList<Position>();
		points1.add(primitiveFactory.createPoint( new double[]{-123.47009555832284,48.543261561072285} ));
		points1.add(primitiveFactory.createPoint( new double[]{-123.46972894676578,48.55009592117936} ));
		points1.add(primitiveFactory.createPoint( new double[]{-123.45463828850829,48.54973520267305} ));
		points1.add(primitiveFactory.createPoint( new double[]{-123.4550070827961,48.54290089070186} ));
		points1.add(primitiveFactory.createPoint( new double[]{-123.47009555832284,48.543261561072285} ));
		LineString lineString1 = geometryFactory.createLineString(points1);
        List curveSegmentList1 = Collections.singletonList(lineString1);
        
        CurveImpl curve1 = (CurveImpl) primitiveFactory.createCurve(curveSegmentList1);
        
		/* Build Ring from Curve */
		ArrayList<OrientableCurve> curveList = new ArrayList<OrientableCurve>();
		curveList.add(curve1);
		
		RingImpl ring1 = (RingImpl) primitiveFactory.createRing(curveList);
		RingImpl ring2 = (RingImpl) ring1.transform(crs2);
		
		// create expected result
		PositionFactory expectedPosF2 = new PositionFactoryImpl(crs2, new PrecisionModel());
		PrimitiveFactory expectedPrimF2 = new PrimitiveFactoryImpl(crs2, expectedPosF2);
		GeometryFactory ExpectedGeomF2 = new GeometryFactoryImpl(crs2, expectedPosF2);
		
		List<Position> expectedPoints = new ArrayList<Position>();
		expectedPoints.add(expectedPrimF2.createPoint( new double[]{1187128.000000001, 395268.0000000004} ));
		expectedPoints.add(expectedPrimF2.createPoint( new double[]{1187127.9999999998, 396026.99999999825} ));
		expectedPoints.add(expectedPrimF2.createPoint( new double[]{1188245.0000000007, 396027.0000000039} ));
		expectedPoints.add(expectedPrimF2.createPoint( new double[]{1188245.0000000005, 395268.0000000018} ));
		expectedPoints.add(expectedPrimF2.createPoint( new double[]{1187128.000000001, 395268.0000000004} ));
        LineString expectedLineString = ExpectedGeomF2.createLineString(expectedPoints);
        List expectedCurveSegmentList = Collections.singletonList(expectedLineString);
        
        CurveImpl expectedCurve = (CurveImpl) expectedPrimF2.createCurve(expectedCurveSegmentList);
        
		/* Build Ring from Curve */
		ArrayList<OrientableCurve> expectedCurveList = new ArrayList<OrientableCurve>();
		expectedCurveList.add(expectedCurve);
		
		RingImpl expectedRing = (RingImpl) expectedPrimF2.createRing(expectedCurveList);
        
//		System.out.println(ring1);
//		System.out.println(ring2);
//		System.out.println(expectedRing);
		
		assertTrue(ring2.equals(expectedRing));
	}
	
	public void testSurface() throws Exception {

		PositionFactory positionFactory = new PositionFactoryImpl(crs1, new PrecisionModel());
		PrimitiveFactory primitiveFactory = new PrimitiveFactoryImpl(crs1, positionFactory);
		GeometryFactory geometryFactory = new GeometryFactoryImpl(crs1, positionFactory);
		
		List<Position> points1 = new ArrayList<Position>();
		points1.add(primitiveFactory.createPoint( new double[]{-123.47009555832284,48.543261561072285} ));
		points1.add(primitiveFactory.createPoint( new double[]{-123.46972894676578,48.55009592117936} ));
		points1.add(primitiveFactory.createPoint( new double[]{-123.45463828850829,48.54973520267305} ));
		points1.add(primitiveFactory.createPoint( new double[]{-123.4550070827961,48.54290089070186} ));
		points1.add(primitiveFactory.createPoint( new double[]{-123.47009555832284,48.543261561072285} ));
		LineString lineString1 = geometryFactory.createLineString(points1);
        List curveSegmentList1 = Collections.singletonList(lineString1);
        
        CurveImpl curve1 = (CurveImpl) primitiveFactory.createCurve(curveSegmentList1);
        
		/* Build Ring from Curve */
		ArrayList<OrientableCurve> curveList = new ArrayList<OrientableCurve>();
		curveList.add(curve1);
		
		// Build Ring then SurfaceBoundary then Surface
		RingImpl exteriors = (RingImpl) primitiveFactory.createRing(curveList);
		List<Ring> interiors = new ArrayList<Ring>();
		SurfaceBoundary sboundary = primitiveFactory.createSurfaceBoundary(exteriors, interiors);
		Surface surface1 = primitiveFactory.createSurface(sboundary);
		Surface surface2 = (SurfaceImpl) surface1.transform(crs2);
		
		// create expected result
		PositionFactory expectedPosF2 = new PositionFactoryImpl(crs2, new PrecisionModel());
		PrimitiveFactory expectedPrimF2 = new PrimitiveFactoryImpl(crs2, expectedPosF2);
		GeometryFactory ExpectedGeomF2 = new GeometryFactoryImpl(crs2, expectedPosF2);
		
		List<Position> expectedPoints = new ArrayList<Position>();
		expectedPoints.add(expectedPrimF2.createPoint( new double[]{1187128.000000001, 395268.0000000004} ));
		expectedPoints.add(expectedPrimF2.createPoint( new double[]{1187127.9999999998, 396026.99999999825} ));
		expectedPoints.add(expectedPrimF2.createPoint( new double[]{1188245.0000000007, 396027.0000000039} ));
		expectedPoints.add(expectedPrimF2.createPoint( new double[]{1188245.0000000005, 395268.0000000018} ));
		expectedPoints.add(expectedPrimF2.createPoint( new double[]{1187128.000000001, 395268.0000000004} ));
        LineString expectedLineString = ExpectedGeomF2.createLineString(expectedPoints);
        List expectedCurveSegmentList = Collections.singletonList(expectedLineString);
        
        CurveImpl expectedCurve = (CurveImpl) expectedPrimF2.createCurve(expectedCurveSegmentList);
        
		/* Build Ring from Curve */
		ArrayList<OrientableCurve> expectedCurveList = new ArrayList<OrientableCurve>();
		expectedCurveList.add(expectedCurve);
		
		// Build Ring then SurfaceBoundary then Surface
		RingImpl exteriors2 = (RingImpl) expectedPrimF2.createRing(expectedCurveList);
		List<Ring> interiors2 = new ArrayList<Ring>();
		SurfaceBoundary sboundary2 = expectedPrimF2.createSurfaceBoundary(exteriors2, interiors2);
		Surface expectedSurface = expectedPrimF2.createSurface(sboundary2);
        
		//System.out.println(surface1);
		//System.out.println(surface2);
		//System.out.println(expectedSurface);
		
		assertTrue(surface2.equals(expectedSurface));
	}
}
