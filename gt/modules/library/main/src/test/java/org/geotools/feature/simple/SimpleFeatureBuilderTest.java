package org.geotools.feature.simple;

import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.DefaultFeatureTypeFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import junit.framework.TestCase;

public class SimpleFeatureBuilderTest extends TestCase {

	SimpleFeatureBuilder builder;
	
	protected void setUp() throws Exception {
		SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
		typeBuilder.setName( "test" );
		typeBuilder.add( "point", Point.class );
		typeBuilder.add( "integer", Integer.class );
		typeBuilder.add( "float", Float.class );
		
		SimpleFeatureType featureType = typeBuilder.buildFeatureType();
		
		builder = new SimpleFeatureBuilder();
		builder.setType( featureType );
	}
	
	public void testSanity() throws Exception {
		GeometryFactory gf = new GeometryFactory();
		builder.add( gf.createPoint( new Coordinate( 0, 0 ) ) );
		builder.add( new Integer( 1 ) );
		builder.add( new Float( 2.0 ) );
		
		SimpleFeature feature = builder.feature( "fid" );
		assertNotNull( feature );
		
		assertEquals( 3, feature.getNumberOfAttributes() );
		
		assertTrue( gf.createPoint( new Coordinate( 0, 0) ).equals( (Geometry) feature.getValue( "point" ) ) );
		assertEquals( new Integer( 1 ) , feature.getValue( "integer" ) );
		assertEquals( new Float( 2.0 ) , feature.getValue( "float" ) );
	}
	
	public void testTooFewAttributes() throws Exception {
	    GeometryFactory gf = new GeometryFactory();
        builder.add( gf.createPoint( new Coordinate( 0, 0 ) ) );
        builder.add( new Integer( 1 ) );
        
        SimpleFeature feature = builder.feature( "fid" );
        assertNotNull( feature );
        
        assertEquals( 3, feature.getNumberOfAttributes() );
        
        assertTrue( gf.createPoint( new Coordinate( 0, 0) ).equals( (Geometry) feature.getValue( "point" ) ) );
        assertEquals( new Integer( 1 ) , feature.getValue( "integer" ) );
        assertNull( feature.getValue( "float" ) );
	}
	
	public void testSetSequential() throws Exception {
        GeometryFactory gf = new GeometryFactory();
        builder.set( "point", gf.createPoint( new Coordinate( 0, 0 ) ) );
        builder.set( "integer", new Integer( 1 ) );
        builder.set( "float",  new Float( 2.0 ) );
        
        SimpleFeature feature = builder.feature( "fid" );
        assertNotNull( feature );
        
        assertEquals( 3, feature.getNumberOfAttributes() );
        
        assertTrue( gf.createPoint( new Coordinate( 0, 0) ).equals( (Geometry) feature.getValue( 0 ) ) );
        assertEquals( new Integer( 1 ) , feature.getValue( 1 ) );
        assertEquals( new Float( 2.0 ) , feature.getValue( 2 ) );
	}
	
	public void testSetNonSequential() throws Exception {
	    GeometryFactory gf = new GeometryFactory();
	    builder.set( "float",  new Float( 2.0 ) );
	    builder.set( "point", gf.createPoint( new Coordinate( 0, 0 ) ) );
        builder.set( "integer", new Integer( 1 ) );
        
        SimpleFeature feature = builder.feature( "fid" );
        assertNotNull( feature );
        
        assertEquals( 3, feature.getNumberOfAttributes() );
        
        assertTrue( gf.createPoint( new Coordinate( 0, 0) ).equals( (Geometry) feature.getValue( 0 ) ) );
        assertEquals( new Integer( 1 ) , feature.getValue( 1 ) );
        assertEquals( new Float( 2.0 ) , feature.getValue( 2 ) );
	}
	
	public void testSetTooFew() throws Exception {
	    builder.set("integer", new Integer(1));
	    SimpleFeature feature = builder.feature( "fid" );
        assertNotNull( feature );
        
        assertEquals( 3, feature.getNumberOfAttributes() );
        
        assertNull( feature.getValue( 0 ) );
        assertEquals( new Integer( 1 ) , feature.getValue( 1 ) );
        assertNull( feature.getValue( 2 ) );
	}
}
