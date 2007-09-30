package org.geotools.feature.simple;

import junit.framework.TestCase;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class SimpleFeatureBuilderTest extends TestCase {

	SimpleFeatureBuilder builder;
	
	protected void setUp() throws Exception {
		SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
		typeBuilder.setName( "test" );
		typeBuilder.add( "point", Point.class );
		typeBuilder.add( "integer", Integer.class );
		typeBuilder.add( "float", Float.class );
		
		SimpleFeatureType featureType = typeBuilder.buildFeatureType();
		
		builder = new SimpleFeatureBuilder(featureType);
	}
	
	public void testSanity() throws Exception {
		GeometryFactory gf = new GeometryFactory();
		builder.add( gf.createPoint( new Coordinate( 0, 0 ) ) );
		builder.add( new Integer( 1 ) );
		builder.add( new Float( 2.0 ) );
		
		SimpleFeature feature = builder.buildFeature( "fid" );
		assertNotNull( feature );
		
		assertEquals( 3, feature.getAttributeCount() );
		
		assertTrue( gf.createPoint( new Coordinate( 0, 0) ).equals( (Geometry) feature.getAttribute( "point" ) ) );
		assertEquals( new Integer( 1 ) , feature.getAttribute( "integer" ) );
		assertEquals( new Float( 2.0 ) , feature.getAttribute( "float" ) );
	}
	
	public void testTooFewAttributes() throws Exception {
	    GeometryFactory gf = new GeometryFactory();
        builder.add( gf.createPoint( new Coordinate( 0, 0 ) ) );
        builder.add( new Integer( 1 ) );
        
        SimpleFeature feature = builder.buildFeature( "fid" );
        assertNotNull( feature );
        
        assertEquals( 3, feature.getAttributeCount() );
        
        assertTrue( gf.createPoint( new Coordinate( 0, 0) ).equals( (Geometry) feature.getAttribute( "point" ) ) );
        assertEquals( new Integer( 1 ) , feature.getAttribute( "integer" ) );
        assertNull( feature.getAttribute( "float" ) );
	}
	
	public void testSetSequential() throws Exception {
        GeometryFactory gf = new GeometryFactory();
        builder.set( "point", gf.createPoint( new Coordinate( 0, 0 ) ) );
        builder.set( "integer", new Integer( 1 ) );
        builder.set( "float",  new Float( 2.0 ) );
        
        SimpleFeature feature = builder.buildFeature( "fid" );
        assertNotNull( feature );
        
        assertEquals( 3, feature.getAttributeCount() );
        
        assertTrue( gf.createPoint( new Coordinate( 0, 0) ).equals( (Geometry) feature.getAttribute( 0 ) ) );
        assertEquals( new Integer( 1 ) , feature.getAttribute( 1 ) );
        assertEquals( new Float( 2.0 ) , feature.getAttribute( 2 ) );
	}
	
	public void testSetNonSequential() throws Exception {
	    GeometryFactory gf = new GeometryFactory();
	    builder.set( "float",  new Float( 2.0 ) );
	    builder.set( "point", gf.createPoint( new Coordinate( 0, 0 ) ) );
        builder.set( "integer", new Integer( 1 ) );
        
        SimpleFeature feature = builder.buildFeature( "fid" );
        assertNotNull( feature );
        
        assertEquals( 3, feature.getAttributeCount() );
        
        assertTrue( gf.createPoint( new Coordinate( 0, 0) ).equals( (Geometry) feature.getAttribute( 0 ) ) );
        assertEquals( new Integer( 1 ) , feature.getAttribute( 1 ) );
        assertEquals( new Float( 2.0 ) , feature.getAttribute( 2 ) );
	}
	
	public void testSetTooFew() throws Exception {
	    builder.set("integer", new Integer(1));
	    SimpleFeature feature = builder.buildFeature( "fid" );
        assertNotNull( feature );
        
        assertEquals( 3, feature.getAttributeCount() );
        
        assertNull( feature.getAttribute( 0 ) );
        assertEquals( new Integer( 1 ) , feature.getAttribute( 1 ) );
        assertNull( feature.getAttribute( 2 ) );
	}
}
