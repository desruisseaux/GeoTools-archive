package org.geotools.geometry.iso.primitive;

import java.util.ArrayList;
import java.util.List;

import org.geotools.geometry.iso.FeatGeomFactoryImpl;
import org.geotools.geometry.iso.PositionFactoryImpl;
import org.geotools.geometry.iso.PrecisionModel;
import org.geotools.geometry.iso.aggregate.AggregateFactoryImpl;
import org.geotools.geometry.iso.complex.ComplexFactoryImpl;
import org.geotools.geometry.iso.coordinate.DirectPositionImpl;
import org.geotools.geometry.iso.coordinate.EnvelopeImpl;
import org.geotools.geometry.iso.coordinate.GeometryFactoryImpl;
import org.geotools.geometry.iso.coordinate.LineSegmentImpl;
import org.geotools.geometry.iso.io.CollectionFactoryMemoryImpl;
import org.geotools.geometry.iso.util.elem2D.Geo2DFactory;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.Envelope;
import org.opengis.geometry.PositionFactory;
import org.opengis.geometry.Precision;
import org.opengis.geometry.coordinate.LineSegment;
import org.opengis.geometry.primitive.OrientableCurve;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.picocontainer.PicoContainer;
import org.picocontainer.defaults.DefaultPicoContainer;

import junit.framework.TestCase;

public class PrimitiveFactoryTest2 extends TestCase {
	
	/**
	 * Creates a pico container that knows about all the geom factories
	 * @param crs
	 * @return container
	 */
	protected PicoContainer container( CoordinateReferenceSystem crs ){
		
		DefaultPicoContainer container = new DefaultPicoContainer(); // parent
		
		// Teach Container about Factory Implementations we want to use
		container.registerComponentImplementation(PositionFactoryImpl.class);
		container.registerComponentImplementation(FeatGeomFactoryImpl.class);
		container.registerComponentImplementation(AggregateFactoryImpl.class);
		container.registerComponentImplementation(ComplexFactoryImpl.class);
		container.registerComponentImplementation(GeometryFactoryImpl.class);
		container.registerComponentImplementation(CollectionFactoryMemoryImpl.class);
		container.registerComponentImplementation(PrimitiveFactoryImpl.class);
		container.registerComponentImplementation(Geo2DFactory.class);
		
		// Teach Container about other dependacies needed
		container.registerComponentInstance( crs );
		Precision pr = new PrecisionModel();
		container.registerComponentInstance( pr );
		
		return container;		
	}
	
	public void testProcessBoundsToInitialSegment(){
		
		CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
		PicoContainer container = container( crs ); // normal 2D
		PrimitiveFactoryImpl factory = (PrimitiveFactoryImpl) container.getComponentInstanceOfType( PrimitiveFactoryImpl.class );
		PositionFactory positionFactory = (PositionFactory ) container.getComponentInstanceOfType( PositionFactory.class );
		
		DirectPosition positionA = positionFactory.createDirectPosition(new double[]{10, 10});
		DirectPosition positionB = positionFactory.createDirectPosition(new double[]{70, 30});
		Envelope bounds = new EnvelopeImpl( positionA, positionB );
		
		LineSegmentImpl expected = new LineSegmentImpl( crs, new double[]{10,Double.NaN}, new double[]{70,Double.NaN}, 0.0 );		
		
		LineSegment actual =  factory.processBoundsToSegment( bounds );
		assertNotNull( actual );
		assertEquals( expected, actual);
	}
	
	public void testProcessBoundsToRing(){
		
		CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
		PicoContainer container = container( crs ); // normal 2D
		PrimitiveFactoryImpl factory = (PrimitiveFactoryImpl) container.getComponentInstanceOfType( PrimitiveFactoryImpl.class );
		PositionFactory positionFactory = (PositionFactory ) container.getComponentInstanceOfType( PositionFactory.class );
		
		DirectPosition positionA = positionFactory.createDirectPosition(new double[]{10, 10});
		DirectPosition positionB = positionFactory.createDirectPosition(new double[]{70, 30});
		Envelope bounds = new EnvelopeImpl( positionA, positionB );
		
		LineSegmentImpl segment = new LineSegmentImpl( crs, new double[]{10,Double.NaN}, new double[]{70,Double.NaN}, 0.0 );
		
		// create expected ring
		DirectPosition one = new DirectPositionImpl( segment.getStartPoint() );
		one.setOrdinate( 1, bounds.getMinimum(1) );
		
		DirectPosition two = new DirectPositionImpl( segment.getEndPoint() );
		two.setOrdinate( 1, bounds.getMinimum(1) );
		
		DirectPosition three = new DirectPositionImpl( two );
		three.setOrdinate( 1, bounds.getMaximum(1) );
		
		DirectPosition four = new DirectPositionImpl( one );
		four.setOrdinate( 1, bounds.getMaximum(1) );
		
		LineSegment edge1 = new LineSegmentImpl( one, two, 0.0 );
		LineSegment edge2 = new LineSegmentImpl( two, three, 0.0 );
		LineSegment edge3 = new LineSegmentImpl( three, four, 0.0 );
		LineSegment edge4 = new LineSegmentImpl( four, one, 0.0 );
		
		List<OrientableCurve> edges = new ArrayList<OrientableCurve>();
		edges.add( new CurveImpl( edge1 ));
		edges.add( new CurveImpl( edge2 ));
		edges.add( new CurveImpl( edge3 ));
		edges.add( new CurveImpl( edge4 ));
		RingImpl expected = new RingImpl( edges );
		
		// create ring and test
		RingImpl actual = factory.processBoundsToRing(bounds, segment, 1);
		assertNotNull( actual );
		assertEquals( expected, actual);
	}
	
	public void testCreatePrimitive() {
		
		CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
		PicoContainer container = container( crs ); // normal 2D
		PrimitiveFactoryImpl factory = (PrimitiveFactoryImpl) container.getComponentInstanceOfType( PrimitiveFactoryImpl.class );
		PositionFactory positionFactory = (PositionFactory ) container.getComponentInstanceOfType( PositionFactory.class );
		
		DirectPosition positionA = positionFactory.createDirectPosition(new double[]{10, 10});
		DirectPosition positionB = positionFactory.createDirectPosition(new double[]{70, 30});
		Envelope bounds = new EnvelopeImpl( positionA, positionB );
		
		// create expected ring
		DirectPosition one = new DirectPositionImpl( positionA );
		one.setOrdinate( 1, bounds.getMinimum(1) );
		
		DirectPosition two = new DirectPositionImpl( positionB );
		two.setOrdinate( 1, bounds.getMinimum(1) );
		
		DirectPosition three = new DirectPositionImpl( two );
		three.setOrdinate( 1, bounds.getMaximum(1) );
		
		DirectPosition four = new DirectPositionImpl( one );
		four.setOrdinate( 1, bounds.getMaximum(1) );
		
		LineSegment edge1 = new LineSegmentImpl( one, two, 0.0 );
		LineSegment edge2 = new LineSegmentImpl( two, three, 0.0 );
		LineSegment edge3 = new LineSegmentImpl( three, four, 0.0 );
		LineSegment edge4 = new LineSegmentImpl( four, one, 0.0 );
		
		List<OrientableCurve> edges = new ArrayList<OrientableCurve>();
		edges.add( new CurveImpl( edge1 ));
		edges.add( new CurveImpl( edge2 ));
		edges.add( new CurveImpl( edge3 ));
		edges.add( new CurveImpl( edge4 ));
		RingImpl expectedRing = new RingImpl( edges );
		
		SurfaceBoundaryImpl sb = new SurfaceBoundaryImpl(crs,expectedRing,null);
		//PrimitiveImpl expected = new PrimitiveImpl(crs,sb,null); //(PrimitiveImpl) sb;
		
		
		PrimitiveImpl impl = factory.createPrimitive(bounds);
		assertNotNull(impl);
	}
}
