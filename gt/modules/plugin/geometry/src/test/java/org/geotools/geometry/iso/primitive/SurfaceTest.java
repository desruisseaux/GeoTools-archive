package org.geotools.geometry.iso.primitive;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.geotools.geometry.GeometryBuilder;
import org.geotools.geometry.iso.PositionFactoryImpl;
import org.geotools.geometry.iso.coordinate.GeometryFactoryImpl;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.hsqldb.lib.StopWatch;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.PositionFactory;
import org.opengis.geometry.coordinate.GeometryFactory;
import org.opengis.geometry.coordinate.LineSegment;
import org.opengis.geometry.coordinate.PointArray;
import org.opengis.geometry.coordinate.LineString;
import org.opengis.geometry.coordinate.Triangle;
import org.opengis.geometry.primitive.Curve;
import org.opengis.geometry.primitive.CurveSegment;
import org.opengis.geometry.primitive.OrientableCurve;
import org.opengis.geometry.primitive.PrimitiveFactory;
import org.opengis.geometry.primitive.Curve;
import org.opengis.geometry.primitive.Point;
import org.opengis.geometry.primitive.Ring;
import org.opengis.geometry.primitive.Surface;
import org.opengis.geometry.primitive.SurfaceBoundary;
import org.opengis.geometry.primitive.SurfacePatch;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * @author sanjay
 *
 */
public class SurfaceTest extends TestCase {
    GeometryBuilder builder;
    
    protected void setUp() throws Exception {
        super.setUp();
        
        builder = new GeometryBuilder(DefaultGeographicCRS.WGS84);                
    }
    /** We need to create a large surface with 7000 points */
    public void testLargeSurfaceFactory(){
        DefaultGeographicCRS crs = DefaultGeographicCRS.WGS84;
        PositionFactory postitionFactory = new PositionFactoryImpl( crs );
        PrimitiveFactory primitiveFactory = new PrimitiveFactoryImpl( crs, postitionFactory );
        GeometryFactory geometryFactory = new GeometryFactoryImpl( crs, postitionFactory );
        
        int NUMBER = 100000;
         double delta = 360.0 / (double) NUMBER;
         PointArray points = postitionFactory.createPointArray();
         for( double angle = 0.0; angle < 360.0; angle += delta ){
             double ordinates[] = new double[]{
                     Math.sin( Math.toRadians(angle) ),
                     Math.cos( Math.toRadians(angle) )
             };
             DirectPosition point = postitionFactory.createDirectPosition( ordinates );
             points.add( point );
         }
         List<OrientableCurve> curves = new ArrayList<OrientableCurve>();        
         // A curve will be created
         // - The curve will be set as parent curves for the Curve segments
         // - Start and end params for the CurveSegments will be set
         List<CurveSegment> segmentList = new ArrayList<CurveSegment>();
         for( int i=0; i<points.length();i++){
             int start = i;
             int end = (i+1)%points.size();
             DirectPosition point1 = points.getDirectPosition( start, null );
             DirectPosition point2 = points.getDirectPosition( end, null );
             LineSegment segment = geometryFactory.createLineSegment( point1, point2 );
             segmentList.add( segment );
         }
         Curve curve = primitiveFactory.createCurve( segmentList );
         curves.add( curve);         
         Ring ring = primitiveFactory.createRing( curves );
         SurfaceBoundary boundary = primitiveFactory.createSurfaceBoundary(ring,new ArrayList());
         Surface surface = primitiveFactory.createSurface(boundary);         
    }
    
    /** We need to create a large surface with 7000 points */
    public void XtestLargeSurfaceBuilder(){
         int NUMBER = 100000;
         double delta = 360.0 / (double) NUMBER;
         PointArray points = builder.createPointArray();
         for( double angle = 0.0; angle < 360.0; angle += delta ){
             DirectPosition point = builder.createDirectPosition();
             point.setOrdinate( 0, Math.sin( Math.toRadians(angle) ) );
             point.setOrdinate( 1, Math.cos( Math.toRadians(angle) ) );   

             points.add( point );
         }
         List<OrientableCurve> curves = new ArrayList<OrientableCurve>();        
         // A curve will be created
         // - The curve will be set as parent curves for the Curve segments
         // - Start and end params for the CurveSegments will be set
         List<LineSegment> segmentList = new ArrayList<LineSegment>();
         for( int i=0; i<points.length();i++){
             int start = i;
             int end = (i+1)%points.size();
             DirectPosition point1 = points.getDirectPosition( start, null );
             DirectPosition point2 = points.getDirectPosition( end, null );
             LineSegment segment = builder.createLineSegment( point1, point2 );
             segmentList.add( segment );
         }
         Curve curve = builder.createCurve( segmentList );
         curves.add( curve);         
         Ring ring = builder.createRing( curves );
         SurfaceBoundary boundary = builder.createSurfaceBoundary(ring );
         Surface surface = builder.createSurface(boundary);         
    }
    
	private List<Triangle> createTestTriangle1(GeometryBuilder builder) {		
		GeometryFactoryImpl tCoordFactory = (GeometryFactoryImpl) builder.getGeometryFactory();
		PrimitiveFactoryImpl tPrimFactory = (PrimitiveFactoryImpl) builder.getPrimitiveFactory();

		ArrayList<double[][]> tDoubleList = new ArrayList<double[][]>();
		tDoubleList.add(new double[][]{{0,0},{100,100},{0, 100}});
		tDoubleList.add(new double[][]{{0,100},{100,100},{50,200}});
		tDoubleList.add(new double[][]{{50,200},{100,100},{150,200}});
		ArrayList<Triangle> triangleList = tCoordFactory.createTriangles(tDoubleList);
	    
		for (int i=0; i < triangleList.size(); i++) {
			Triangle triangle1 = triangleList.get(i);
			//System.out.println(triangle1);
		}
	    
	    //System.out.println(triangle1.get.getEnvelope());
	    
	    //System.out.println(triangle1.getBoundary());
	    
		return triangleList;
	    
	}

	/**
	 * Create a surface on basis of SurfacePatches (Triangles)
	 * @param aGeomFactory
	 */
	public void XtestSurface1() {
		PrimitiveFactoryImpl tPrimFactory = (PrimitiveFactoryImpl) builder.getPrimitiveFactory();
		
		List<? extends SurfacePatch> triangleList = createTestTriangle1(builder);
		
		List<SurfacePatch> surfacePatches1 = (List<SurfacePatch>)triangleList;

		Surface surface1 = tPrimFactory.createSurface(surfacePatches1);
		
		//System.out.print("\n******************* SURFACE GENERATED BY SURFACEPATCHES");
		this.XtestSurfaces((SurfaceImpl) surface1);
	}

	public Surface _testSurface2(GeometryBuilder builder) {
		
		GeometryFactoryImpl tCoordFactory = (GeometryFactoryImpl) builder.getGeometryFactory();
		PrimitiveFactoryImpl tPrimFactory = (PrimitiveFactoryImpl) builder.getPrimitiveFactory();

		List<DirectPosition> directPositionList = new ArrayList<DirectPosition>();
		directPositionList.add(tCoordFactory.createDirectPosition(new double[] {20, 10}));
		directPositionList.add(tCoordFactory.createDirectPosition(new double[] {40, 10}));
		directPositionList.add(tCoordFactory.createDirectPosition(new double[] {50, 40}));
		directPositionList.add(tCoordFactory.createDirectPosition(new double[] {30, 50}));
		directPositionList.add(tCoordFactory.createDirectPosition(new double[] {10, 30}));
		directPositionList.add(tCoordFactory.createDirectPosition(new double[] {20, 10}));

		RingImpl exteriorRing = (RingImpl) tPrimFactory.createRingByDirectPositions(directPositionList);
		List<Ring> interiors = new ArrayList<Ring>();
		
		SurfaceBoundaryImpl surfaceBoundary1 = tPrimFactory.createSurfaceBoundary(exteriorRing, interiors );
		
		Surface surface2 = tPrimFactory.createSurface(surfaceBoundary1);
		
		//System.out.print("\n******************* SURFACE GENERATED BY SURFACEBOUNDARY");

		
		this.XtestSurfaces((SurfaceImpl) surface2);
		
		// ***** clone()
		SurfaceImpl surface3 = null;
		try {
			surface3 = (SurfaceImpl) surface2.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		assertTrue(surface2 != surface3);
		this.XtestSurfaces((SurfaceImpl) surface3);
		
		// ***** getRepresentativePoint()
		double[] dp = surface2.getRepresentativePoint().getCoordinates();
		assertTrue(dp[0] == 20);
		assertTrue(dp[1] == 10);


		return surface2;

	}
	
	
	private void XtestSurfaces(SurfaceImpl surface) {

		try {
			//System.out.print("\nSurface: " + surface);
		} catch (NullPointerException e) {
		}
//		System.out.print("\ngetBoundary: " + surface.getBoundary());
        assertNotNull( surface.getBoundary() );
//		System.out.print("\ngetEnvelope: " + surface.getEnvelope());
        assertNotNull( surface.getEnvelope() );
//		System.out.print("\ngetCoordinateDimension: " + surface.getCoordinateDimension());\
        assertNotNull( surface.getCoordinateDimension() );
//		System.out.print("\ngetDimension: " + surface.getDimension(null));
		assertTrue(surface.isCycle() == false);
		
	}

//    public void testSlowGeometry() throws FileNotFoundException {
//        String fname = System.getenv("ESP_HOME") + "\\resources\\users\\petritis\\zzzPoints.bin";
//        CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
//        GeometryBuilder builder = new GeometryBuilder(crs);
//		GeometryFactoryImpl tGeomFactory = (GeometryFactoryImpl) builder.getGeometryFactory();
//		PrimitiveFactoryImpl tPrimFactory = (PrimitiveFactoryImpl) builder.getPrimitiveFactory();
//
//        double[] imagePoints = null;
//        RandomAccessFile in = new RandomAccessFile(fname, "r");
//
//        ArrayList<Double> list = new ArrayList<Double>();
//        double d;
//        while (true) {
//            try {
//                d = in.readDouble();
//            } catch (IOException e) {
//                break;
//            }
//            list.add(d);
//        }
//
//        imagePoints = new double[list.size()];
//        int at = 0;
//        for (Double dv : list) {
//            imagePoints[at] = dv.doubleValue();
//            at++;
//        }
//
//        int numPoints = imagePoints.length / 2;
//        Point[] points = new Point[numPoints + 1];
//        for (int i = 0; i < numPoints; i++) {
//            double[] coord = new double[2];
//            coord[0] = imagePoints[i * 2];
//            coord[1] = imagePoints[i * 2 + 1];
//            points[i] = tPrimFactory.createPoint(coord);
//        }
//        points[numPoints] = points[0];
//
//        //StopWatch timer = new StopWatch();
//        
//        LineString lineString = tGeomFactory.createLineString(new ArrayList(Arrays.asList(points)));        
//        List curveSegmentList = Collections.singletonList(lineString);
//        List curveList = Collections.singletonList(tPrimFactory.createCurve(curveSegmentList));
//        Ring exteriorRing = tPrimFactory.createRing(curveList);
//        SurfaceBoundary surfaceBoundary = tPrimFactory
//                .createSurfaceBoundary(exteriorRing, Collections.EMPTY_LIST);
//        Surface polygon = tPrimFactory.createSurface(surfaceBoundary);
//        //timer.stop();
//
//        System.out.println("numpoints=" + numPoints
//                + "  time to create polygon: " );
//
//        assertNotNull(polygon);
//    }
	

}
