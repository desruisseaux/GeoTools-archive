package org.geotools.geometry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.geotools.factory.GeoTools;
import org.geotools.factory.Hints;
import org.geotools.referencing.CRS;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.Envelope;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.geometry.MismatchedReferenceSystemException;
import org.opengis.geometry.PositionFactory;
import org.opengis.geometry.Precision;
import org.opengis.geometry.aggregate.AggregateFactory;
import org.opengis.geometry.aggregate.MultiCurve;
import org.opengis.geometry.aggregate.MultiPoint;
import org.opengis.geometry.aggregate.MultiPrimitive;
import org.opengis.geometry.aggregate.MultiSurface;
import org.opengis.geometry.complex.ComplexFactory;
import org.opengis.geometry.complex.CompositeCurve;
import org.opengis.geometry.complex.CompositePoint;
import org.opengis.geometry.complex.CompositeSurface;
import org.opengis.geometry.coordinate.GeometryFactory;
import org.opengis.geometry.coordinate.LineSegment;
import org.opengis.geometry.coordinate.LineString;
import org.opengis.geometry.coordinate.PointArray;
import org.opengis.geometry.coordinate.Polygon;
import org.opengis.geometry.coordinate.PolyhedralSurface;
import org.opengis.geometry.coordinate.Position;
import org.opengis.geometry.coordinate.Tin;
import org.opengis.geometry.primitive.Curve;
import org.opengis.geometry.primitive.OrientableCurve;
import org.opengis.geometry.primitive.Point;
import org.opengis.geometry.primitive.Primitive;
import org.opengis.geometry.primitive.PrimitiveFactory;
import org.opengis.geometry.primitive.Ring;
import org.opengis.geometry.primitive.Solid;
import org.opengis.geometry.primitive.SolidBoundary;
import org.opengis.geometry.primitive.Surface;
import org.opengis.geometry.primitive.SurfaceBoundary;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystemAxis;

/**
 * A Builder to help with Geometry creation.
 * <p>
 * The factory interfaces provided by GeoAPI are hard to use in isolation (they
 * are even hard to collect a matched set in order to work on the same problem).
 * The main advantage a "builder" has over a factory is that it does not have to
 * be thread safe and can hold state in order to make your job easier.
 * <p>
 *  
 * @author Jody Garnett
 *
 */
public class GeometryBuilder {
    /**
     * Hints used for the duration of this GeometryBuilder.
     */
    private Hints hints;
    
    /**
     * CoordinateReferenceSystem used to construct the next geometry artifact.
     * <p>
     * This forms the core state of our builds, all other factories are created
     * with this CoordinateReferenceSystem in mind.
     */
    private CoordinateReferenceSystem crs;
    
    /**
     * Precision used to construct the next direct position.
     * <p>
     * This forms the core state of our builds, all other factories are created
     * with this CoordinateReferenceSystem in mind.
     */
    private Precision precision;
    
    private PositionFactory positionFactory;
    private PrimitiveFactory primitiveFactory;
    private AggregateFactory aggregateFactory;
    private ComplexFactory complexFactory;
    private GeometryFactory geometryFactory;
    
    public GeometryBuilder( CoordinateReferenceSystem crs ){
        this.crs = crs;
        this.hints = GeoTools.getDefaultHints();
        hints.put( Hints.CRS, crs );
    }
    
    public GeometryBuilder( String code ) throws NoSuchAuthorityCodeException, FactoryException{
        this( CRS.decode( code ));
    }
    
    public GeometryBuilder( Hints hints ){
        this.crs = (CoordinateReferenceSystem) hints.get( Hints.CRS );
        this.hints = hints;
    }
    
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return crs;
    }

    public void setCoordianteReferenceSystem( CoordinateReferenceSystem crs ) {
        if( this.crs != crs ){
            positionFactory = null;
            primitiveFactory = null;
            aggregateFactory = null;
            complexFactory = null;
            geometryFactory = null;
        }
        this.crs = crs;      
        hints.remove(Hints.CRS);
        hints.put( Hints.CRS, crs );
    }

    public Precision getPrecision() {
        if( precision == null ){
            precision = GeometryFactoryFinder.getPrecision( hints );
        }
        return precision;        
    }
    
    public PositionFactory getPositionFactory() {
        if( positionFactory == null ){
            positionFactory = GeometryFactoryFinder.getPositionFactory( hints);
        }
        return positionFactory;
    }

    public PrimitiveFactory getPrimitiveFactory() {
        if( primitiveFactory == null ){
            primitiveFactory = GeometryFactoryFinder.getPrimitiveFactory(  hints);
        }
        return primitiveFactory;
    }
    
    public AggregateFactory getAggregateFactory() {
        if( aggregateFactory == null ){
        	aggregateFactory = GeometryFactoryFinder.getAggregateFactory(  hints);
        }
        return aggregateFactory;
    }
    
    public GeometryFactory getGeometryFactory() {
        if( geometryFactory == null ){
        	geometryFactory = GeometryFactoryFinder.getGeometryFactory(  hints);
        }
        return geometryFactory;
    }
    
    public ComplexFactory getComplexFactory() {
        if( complexFactory == null ){
        	complexFactory = GeometryFactoryFinder.getComplexFactory(  hints);
        }
        return complexFactory;
    }    
    
    public DirectPosition createDirectPosition( double[] ordinates ) {
        return getPositionFactory().createDirectPosition( ordinates );
    }

    public Position createPosition( Position position ) {
        return getPositionFactory().createPosition( position );
    }

    public PointArray createPointArray() {
        return getPositionFactory().createPointArray();
    }

    public PointArray createPointArray( double[] array ) {
        return getPositionFactory().createPointArray(array, 0, array.length / crs.getCoordinateSystem().getDimension() );
    }
    public PointArray createPointArray( double[] array, int start, int end ) {
        return getPositionFactory().createPointArray(array, start, end );
    }

    public PointArray createPositionList( float[] array, int start, int end ) {
        return getPositionFactory().createPointArray(array, start, end );
    }

	public Curve createCurve(List segments) throws MismatchedReferenceSystemException, MismatchedDimensionException {
		if (segments == null)
			throw new NullPointerException("Segments are required to create a curve");

		// A curve will be created
		// - The curve will be set as parent curves for the Curve segments
		// - Start and end params for the CurveSegments will be set
		return getPrimitiveFactory().createCurve(segments);
	}
    public Curve createCurve(PointArray points) throws MismatchedReferenceSystemException, MismatchedDimensionException {
        if (points == null)
            throw new NullPointerException("Points are required to create a curve");

        // A curve will be created
        // - The curve will be set as parent curves for the Curve segments
        // - Start and end params for the CurveSegments will be set
        List<LineSegment> segmentList = new ArrayList<LineSegment>();
        for( int i=0; i<points.length();i++){
            int start = i;
            int end = (i+1)%points.size();
            DirectPosition point1 = points.getDirectPosition( start, null );
            DirectPosition point2 = points.getDirectPosition( end, null );
            LineSegment segment = createLineSegment( point1, point2 );
            segmentList.add( segment );
        }
        return getPrimitiveFactory().createCurve( segmentList );
    }

	/**
	 * Create a point with the provided ordinates.
	 * @param ord1
	 * @param ord2
	 * @return createPoint( new double[]{ ord1, ord2})
	 */
	public Point createPoint( double ord1, double ord2 ){
	    return createPoint( new double[]{ ord1, ord2});
	}
	/**
	 * Create a point with the provided ordinates.
	 * @param ord1
	 * @param ord2
	 * @param ord3
	 * @return createPoint( new double[]{ ord1, ord2, ord3 })
	 */
	public Point createPoint( double ord1, double ord2, double ord3 ){
        return createPoint( new double[]{ ord1, ord2, ord3 });
    }
	/**
	 * Create a point with the provided ordinates
	 * @param ordinates
	 * @return getPrimitiveFactory().createPoint(coordinates)
	 * @throws MismatchedDimensionException
	 */
	public Point createPoint(double[] ordinates) throws MismatchedDimensionException {
		if (ordinates == null)
			throw new NullPointerException("Ordinates required to create a point");
		int dimension = this.getCoordinateReferenceSystem().getCoordinateSystem().getDimension();
        if (ordinates.length != dimension)
			throw new MismatchedDimensionException("Create point requires "+dimension+" ordinates ("+ordinates.length+" provided");

		return getPrimitiveFactory().createPoint(ordinates);
	}

	public Point createPoint(Position position) throws MismatchedReferenceSystemException, MismatchedDimensionException {
		if (position == null) {
			throw new NullPointerException();
		}
		setCoordianteReferenceSystem(position.getPosition().getCoordinateReferenceSystem());
		DirectPosition copy = (DirectPosition) getPositionFactory().createDirectPosition(position.getPosition().getCoordinates());
		return getPrimitiveFactory().createPoint(copy);
	}

	public Primitive createPrimitive(Envelope envelope) throws MismatchedReferenceSystemException, MismatchedDimensionException {
		LineSegment segment = processBoundsToSegment(envelope);		
		setCoordianteReferenceSystem(envelope.getCoordinateReferenceSystem());
		return processSegmentToPrimitive( envelope, segment, 1 );	
	}
	
	private Primitive processSegmentToPrimitive(Envelope bounds, LineSegment segment, int dimension) {
		CoordinateSystemAxis axis = crs.getCoordinateSystem().getAxis( dimension );
		
		if( axis.getDirection() == AxisDirection.OTHER ){
			return processSegmentToPrimitive( bounds, segment, dimension+1 );
		}
		Ring ring = processBoundsToRing( bounds, segment, dimension );
		return processRingToPrimitive( bounds, ring, dimension+1 );				
	}
	
	private Ring processBoundsToRing( Envelope bounds, LineSegment segment, final int D ){
		DirectPosition one =  getPositionFactory().createDirectPosition(segment.getStartPoint().getCoordinates());
		one.setOrdinate( D, bounds.getMinimum(D) );
		
		DirectPosition two =  getPositionFactory().createDirectPosition(segment.getEndPoint().getCoordinates());
		two.setOrdinate( D, bounds.getMinimum(D) );
		
		DirectPosition three =  getPositionFactory().createDirectPosition(two.getCoordinates()); 
		three.setOrdinate( D, bounds.getMaximum(D) );
		
		DirectPosition four =  getPositionFactory().createDirectPosition(one.getCoordinates());
		four.setOrdinate( D, bounds.getMaximum(D) );
		
		LineSegment edge1 = getGeometryFactory().createLineSegment(one, two);
		LineSegment edge2 = getGeometryFactory().createLineSegment(two, three);
		LineSegment edge3 = getGeometryFactory().createLineSegment(three, four);
		LineSegment edge4 = getGeometryFactory().createLineSegment(four, one);
		
		List<OrientableCurve> edges = new ArrayList<OrientableCurve>();
		edges.add( createCurve( Arrays.asList(edge1) ));
		edges.add( createCurve( Arrays.asList(edge2) ));
		edges.add( createCurve( Arrays.asList(edge3) ));
		edges.add( createCurve( Arrays.asList(edge4) ));
		return createRing( edges );
	}
	
	private Primitive processRingToPrimitive(Envelope bounds, Ring ring, int dimension) {
		int D = crs.getCoordinateSystem().getDimension();
		if( dimension == D ){ // create Surface from ring and return			
			SurfaceBoundary boundary = createSurfaceBoundary( ring, Collections.EMPTY_LIST );
			return createSurface( boundary );
		}		
		CoordinateSystemAxis axis = crs.getCoordinateSystem().getAxis( dimension );
		if( axis.getDirection() == AxisDirection.OTHER ){
			return processRingToPrimitive( bounds, ring, dimension+1 );
		}
		return processRingToVolumne( bounds, ring, dimension+1 );
	}
	
	private Primitive processRingToVolumne(Envelope bounds, Ring ring, int i) {
		// go into a volume
		throw new UnsupportedOperationException("Not yet 3D");
	}
	
	private LineSegment processBoundsToSegment( Envelope bounds ) {
		final int D=0;
		CoordinateReferenceSystem crs = bounds.getCoordinateReferenceSystem();
		CoordinateSystemAxis axis = crs.getCoordinateSystem().getAxis( D );
		
		DirectPosition positionA = getPositionFactory().createDirectPosition(null);
		DirectPosition positionB = getPositionFactory().createDirectPosition(null);	
		if( axis.getDirection() != AxisDirection.OTHER ){
			positionA.setOrdinate(D, bounds.getMinimum(D) );
			positionB.setOrdinate(D, bounds.getMaximum(D) );
		}		
		return getGeometryFactory().createLineSegment(positionA, positionB);
	}
	
	public Ring createRing(List<OrientableCurve> orientableCurves)
		throws MismatchedReferenceSystemException, MismatchedDimensionException {
		/**
		 * Creates a Ring from triple Array of DirectPositions (Array of arrays,
		 * which each represent a future Curve. Each array contain an array of
		 * positions, which each represent a future lineString)
		 */
		for (OrientableCurve orientableCurve : orientableCurves) {
			if (this.getCoordinateReferenceSystem().getCoordinateSystem().getDimension() != orientableCurve.getCoordinateDimension()) {
				throw new MismatchedDimensionException();
			}
			if (this.getCoordinateReferenceSystem() != orientableCurve
					.getCoordinateReferenceSystem()) {
				throw new MismatchedReferenceSystemException();
			}
		}
		return getPrimitiveFactory().createRing(orientableCurves);
	}

	public Solid createSolid(SolidBoundary boundary) throws MismatchedReferenceSystemException, MismatchedDimensionException {
		if (this.getCoordinateReferenceSystem().getCoordinateSystem().getDimension() != boundary.getCoordinateDimension()) {
			throw new MismatchedDimensionException();
		}
		if (this.getCoordinateReferenceSystem() != boundary.getCoordinateReferenceSystem()) {
				throw new MismatchedReferenceSystemException();
		}
		return getPrimitiveFactory().createSolid(boundary);
	}

    public SurfaceBoundary createSurfaceBoundary(PointArray points ) throws MismatchedReferenceSystemException, MismatchedDimensionException {
        Curve curve = createCurve( points );
        return createSurfaceBoundary( curve);
    }
	public Surface createSurface(List surfaces) throws MismatchedReferenceSystemException, MismatchedDimensionException {
		return getPrimitiveFactory().createSurface(surfaces);
	}

	public Surface createSurface(SurfaceBoundary boundary) throws MismatchedReferenceSystemException, MismatchedDimensionException {
		return getPrimitiveFactory().createSurface(boundary);
	}

	public SurfaceBoundary createSurfaceBoundary(Ring exterior, List interiors) throws MismatchedReferenceSystemException, MismatchedDimensionException {
		return getPrimitiveFactory().createSurfaceBoundary(exterior, interiors);
	}
	
	public SurfaceBoundary createSurfaceBoundary(Ring exterior) throws MismatchedReferenceSystemException, MismatchedDimensionException {
        return createSurfaceBoundary( exterior, new ArrayList<Ring>() );
    }

	public SurfaceBoundary createSurfaceBoundary(OrientableCurve curve) throws MismatchedReferenceSystemException, MismatchedDimensionException {
	    List<OrientableCurve> exterior = new ArrayList<OrientableCurve>(1);
	    exterior.add( curve );
	    Ring ring = createRing( exterior );
        return createSurfaceBoundary( ring );
    }
	
	/* not implemented in GeometryFactory yet
	public Arc createArc(Position startPoint, Position midPoint, Position endPoint) throws MismatchedReferenceSystemException, MismatchedDimensionException {
		// TODO Auto-generated method stub
		return null;
	}

	public Arc createArc(Position startPoint, Position endPoint, double bulge, double[] normal) throws MismatchedReferenceSystemException, MismatchedDimensionException {
		// TODO Auto-generated method stub
		return null;
	}

	public ArcByBulge createArcByBulge(Position startPoint, Position endPoint, double bulge, double[] normal) throws MismatchedReferenceSystemException, MismatchedDimensionException {
		// TODO Auto-generated method stub
		return null;
	}

	public ArcString createArcString(List points) throws MismatchedReferenceSystemException, MismatchedDimensionException {
		// TODO Auto-generated method stub
		return null;
	}

	public ArcStringByBulge createArcStringByBulge(List points, double[] bulges, List normals) throws MismatchedReferenceSystemException, MismatchedDimensionException {
		// TODO Auto-generated method stub
		return null;
	}

	public BSplineCurve createBSplineCurve(int degree, PointArray points, List knots, KnotType knotSpec) throws MismatchedReferenceSystemException, MismatchedDimensionException {
		// TODO Auto-generated method stub
		return null;
	}

	public BSplineSurface createBSplineSurface(List points, int[] degree, List[] knots, KnotType knotSpec) throws MismatchedReferenceSystemException, MismatchedDimensionException {
		// TODO Auto-generated method stub
		return null;
	}
	
	public Geodesic createGeodesic(Position startPoint, Position endPoint) throws MismatchedReferenceSystemException, MismatchedDimensionException {
		// TODO Auto-generated method stub
		return null;
	}

	public GeodesicString createGeodesicString(List points) throws MismatchedReferenceSystemException, MismatchedDimensionException {
		// TODO Auto-generated method stub
		return null;
	}
	*/

	public DirectPosition createDirectPosition() {
		return createDirectPosition(null);
	}

	public Envelope createEnvelope(DirectPosition lowerCorner, DirectPosition upperCorner) throws MismatchedReferenceSystemException, MismatchedDimensionException {
		return getGeometryFactory().createEnvelope(lowerCorner, upperCorner);
	}

	public LineSegment createLineSegment(Position startPoint, Position endPoint) throws MismatchedReferenceSystemException, MismatchedDimensionException {
		return getGeometryFactory().createLineSegment(startPoint, endPoint);
	}

	public LineString createLineString(List points) throws MismatchedReferenceSystemException, MismatchedDimensionException {
		return getGeometryFactory().createLineString(points);
	}
	public LineString createLineString(PointArray points) throws MismatchedReferenceSystemException, MismatchedDimensionException {
	    return getGeometryFactory().createLineString(points);
    }
	public MultiPrimitive createMultiPrimitive() {
		return getGeometryFactory().createMultiPrimitive();
	}

	public Polygon createPolygon(SurfaceBoundary boundary) throws MismatchedReferenceSystemException, MismatchedDimensionException {
		return getGeometryFactory().createPolygon(boundary);
	}

	public Polygon createPolygon(SurfaceBoundary boundary, Surface spanSurface) throws MismatchedReferenceSystemException, MismatchedDimensionException {
		return getGeometryFactory().createPolygon(boundary, spanSurface);
	}

	public PolyhedralSurface createPolyhedralSurface(List tiles) throws MismatchedReferenceSystemException, MismatchedDimensionException {
		return getGeometryFactory().createPolyhedralSurface(tiles);
	}

	public Tin createTin(Set post, Set stopLines, Set breakLines, double maxLength) throws MismatchedReferenceSystemException, MismatchedDimensionException {
		return getGeometryFactory().createTin(post, stopLines, breakLines, maxLength);
	}

	public CompositeCurve createCompositeCurve(List generator) {
		return getComplexFactory().createCompositeCurve(generator);
	}

	public CompositePoint createCompositePoint(Point generator) {
		return getComplexFactory().createCompositePoint(generator);
	}

	public CompositeSurface createCompositeSurface(List generator) {
		return getComplexFactory().createCompositeSurface(generator);
	}

	public MultiCurve createMultiCurve(Set curves) {
		return getAggregateFactory().createMultiCurve(curves);
	}

	public MultiPoint createMultiPoint(Set points) {
		return getAggregateFactory().createMultiPoint(points);
	}

	public MultiPrimitive createMultiPrimitive(Set primitives) {
		return getAggregateFactory().createMultiPrimitive(primitives);
	}

	public MultiSurface createMultiSurface(Set surfaces) {
		return getAggregateFactory().createMultiSurface(surfaces);
	}

}
