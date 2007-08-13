package org.geotools.geometry;

import java.util.Collection;
import java.util.List;

import org.geotools.factory.GeoTools;
import org.geotools.factory.Hints;
import org.geotools.referencing.CRS;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.geometry.MismatchedReferenceSystemException;
import org.opengis.geometry.PositionFactory;
import org.opengis.geometry.Precision;
import org.opengis.geometry.aggregate.AggregateFactory;
import org.opengis.geometry.complex.ComplexFactory;
import org.opengis.geometry.coordinate.GeometryFactory;
import org.opengis.geometry.coordinate.Position;
import org.opengis.geometry.primitive.Curve;
import org.opengis.geometry.primitive.Point;
import org.opengis.geometry.primitive.PrimitiveFactory;
import org.opengis.geometry.primitive.Ring;
import org.opengis.geometry.primitive.Solid;
import org.opengis.geometry.primitive.SolidBoundary;
import org.opengis.geometry.primitive.Surface;
import org.opengis.geometry.primitive.SurfaceBoundary;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

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

    /**
     * Helper method allows you to take a raw collection of Position and
     * convert it into a PointArray.
     * @param origional
     * @return PointArray
     */
    @SuppressWarnings("unchecked")
    public List<Position> createPositionList( Collection<Position> origional ) {
        List<Position> list = (List<Position>) getPositionFactory().createPositionList();
        list.addAll( origional );
        return list;
    }
    
    @SuppressWarnings("unchecked")
    public List<Position> createPositionList() {
        return getPositionFactory().createPositionList();
    }

    @SuppressWarnings("unchecked")
    public List<Position> createPositionList( double[] array, int start, int end ) {
        return getPositionFactory().createPositionList(array, start, end );
    }

    @SuppressWarnings("unchecked")
    public List<Position> createPositionList( float[] array, int start, int end ) {
        return getPositionFactory().createPositionList(array, start, end );
    }

	public Curve createCurve(List segments) throws MismatchedReferenceSystemException, MismatchedDimensionException {
		if (segments == null)
			throw new NullPointerException();

		// A curve will be created
		// - The curve will be set as parent curves for the Curve segments
		// - Start and end params for the CurveSegments will be set
		return getPrimitiveFactory().createCurve(segments);
	}

	public Point createPoint(double[] coordinates) throws MismatchedDimensionException {
		if (coordinates == null)
			throw new NullPointerException();
		if (coordinates.length != this.getCoordinateReferenceSystem().getCoordinateSystem().getDimension())
			throw new MismatchedDimensionException();

		return getPrimitiveFactory().createPoint(coordinates);
	}

//	public Point createPoint(Position position) throws MismatchedReferenceSystemException, MismatchedDimensionException {
//		if (position == null) {
//			throw new NullPointerException();
//		}
//		if (position.getPosition().getDimension() != this.getCoordinateReferenceSystem().getCoordinateSystem().getDimension()) {
//			throw new MismatchedDimensionException();
//		}
//		DirectPositionImpl copy = (DirectPositionImpl) getPositionFactory().createDirectPosition(position.getPosition().getCoordinates());
//		return getPrimitiveFactory().createPoint(copy);
//	}

//	public Primitive createPrimitive(Envelope envelope) throws MismatchedReferenceSystemException, MismatchedDimensionException {
//		LineSegment segment = processBoundsToSegment(envelope);		
//		return processSegmentToPrimitive( envelope, segment, 1 );	
//	}
//	
//	private PrimitiveImpl processSegmentToPrimitive(Envelope bounds, LineSegment segment, int dimension) {
//		CoordinateSystemAxis axis = crs.getCoordinateSystem().getAxis( dimension );
//		
//		if( axis.getDirection() == AxisDirection.OTHER ){
//			return processSegmentToPrimitive( bounds, segment, dimension+1 );
//		}
//		RingImpl ring = processBoundsToRing( bounds, segment, dimension );
//		return processRingToPrimitive( bounds, ring, dimension+1 );				
//	}
//	
//	public RingImpl processBoundsToRing( Envelope bounds, LineSegment segment, final int D ){
//		DirectPosition one =  getPositionFactory().createDirectPosition(segment.getStartPoint().getCoordinates()); //new DirectPositionImpl( segment.getStartPoint() );
//		one.setOrdinate( D, bounds.getMinimum(D) );
//		
//		DirectPosition two =  getPositionFactory().createDirectPosition(segment.getEndPoint().getCoordinates()); //new DirectPositionImpl( segment.getEndPoint() );
//		two.setOrdinate( D, bounds.getMinimum(D) );
//		
//		DirectPosition three =  getPositionFactory().createDirectPosition(two.getCoordinates()); //new DirectPositionImpl( two );
//		three.setOrdinate( D, bounds.getMaximum(D) );
//		
//		DirectPosition four =  getPositionFactory().createDirectPosition(one.getCoordinates()); //new DirectPositionImpl( one );
//		four.setOrdinate( D, bounds.getMaximum(D) );
//		
//		LineSegment edge1 = new LineSegmentImpl( one, two, 0.0 );
//		LineSegment edge2 = new LineSegmentImpl( two, three, 0.0 );
//		LineSegment edge3 = new LineSegmentImpl( three, four, 0.0 );
//		LineSegment edge4 = new LineSegmentImpl( four, one, 0.0 );
//		
//		List<OrientableCurve> edges = new ArrayList<OrientableCurve>();
//		edges.add( new CurveImpl( edge1 ));
//		edges.add( new CurveImpl( edge2 ));
//		edges.add( new CurveImpl( edge3 ));
//		edges.add( new CurveImpl( edge4 ));
//		return new RingImpl( edges );
//	}
//	
//	@SuppressWarnings("unchecked")
//	private PrimitiveImpl processRingToPrimitive(Envelope bounds, RingImpl ring, int dimension) {
//		int D = crs.getCoordinateSystem().getDimension();
//		if( dimension == D ){ // create Surface from ring and return			
//			SurfaceBoundary boundary = new SurfaceBoundaryImpl( crs, ring, Collections.EMPTY_LIST );
//			return new SurfaceImpl( boundary );
//		}		
//		CoordinateSystemAxis axis = crs.getCoordinateSystem().getAxis( dimension );
//		if( axis.getDirection() == AxisDirection.OTHER ){
//			return processRingToPrimitive( bounds, ring, dimension+1 );
//		}
//		return processRingToVolumne( bounds, ring, dimension+1 );
//	}
//	
//	private PrimitiveImpl processRingToVolumne(Envelope bounds, RingImpl ring, int i) {
//		// go into a volume
//		throw new UnsupportedOperationException("Not yet 3D");
//	}
//	
//	public LineSegment processBoundsToSegment( Envelope bounds ){
//		final int D=0;
//		CoordinateReferenceSystem crs = bounds.getCoordinateReferenceSystem();
//		CoordinateSystemAxis axis = crs.getCoordinateSystem().getAxis( D );
//		
//		DirectPosition positionA = getPositionFactory().createDirectPosition(null); //new DirectPositionImpl(crs);
//		DirectPosition positionB = getPositionFactory().createDirectPosition(null); //new DirectPositionImpl(crs);		
//		if( axis.getDirection() != AxisDirection.OTHER ){
//			positionA.setOrdinate(D, bounds.getMinimum(D) );
//			positionB.setOrdinate(D, bounds.getMaximum(D) );
//		}		
//		PointArrayImpl array = new PointArrayImpl(crs );
//		array.add( positionA );
//		array.add( positionB );
//		
//		return new LineSegmentImpl( array, 0.0 );
//	}

	public Ring createRing(List curves) throws MismatchedReferenceSystemException, MismatchedDimensionException {
		// TODO Auto-generated method stub
		return null;
	}

	public Solid createSolid(SolidBoundary boundary) throws MismatchedReferenceSystemException, MismatchedDimensionException {
		// TODO Auto-generated method stub
		return null;
	}

	public Surface createSurface(List surfaces) throws MismatchedReferenceSystemException, MismatchedDimensionException {
		// TODO Auto-generated method stub
		return null;
	}

	public Surface createSurface(SurfaceBoundary boundary) throws MismatchedReferenceSystemException, MismatchedDimensionException {
		// TODO Auto-generated method stub
		return null;
	}

	public SurfaceBoundary createSurfaceBoundary(Ring exterior, List interiors) throws MismatchedReferenceSystemException, MismatchedDimensionException {
		// TODO Auto-generated method stub
		return null;
	}

}
