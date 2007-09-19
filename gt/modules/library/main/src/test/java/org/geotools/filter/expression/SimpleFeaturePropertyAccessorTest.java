package org.geotools.filter.expression;

import junit.framework.TestCase;

import org.geotools.feature.DefaultFeatureBuilder;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.type.DefaultFeatureTypeBuilder;

public class SimpleFeaturePropertyAccessorTest extends TestCase {

	FeatureType type;
	Feature feature;
	PropertyAccessor accessor = SimpleFeaturePropertyAccessorFactory.ATTRIBUTE_ACCESS;
	
	protected void setUp() throws Exception {
		DefaultFeatureTypeBuilder typeBuilder = new DefaultFeatureTypeBuilder();
		
		typeBuilder.setName( "test" );
		typeBuilder.setNamespaceURI( "http://www.geotools.org/test" );
		typeBuilder.add( "foo", Integer.class );
		typeBuilder.add( "bar", Double.class );
		
		type = (FeatureType) typeBuilder.buildFeatureType();
		
		DefaultFeatureBuilder builder = new DefaultFeatureBuilder();
		builder.setType( type );
		builder.add( new Integer( 1 ) );
		builder.add( new Double( 2.0 ) );

		feature = (Feature) builder.build( "fid" );
		accessor = SimpleFeaturePropertyAccessorFactory.ATTRIBUTE_ACCESS;
	}
	
	
	public void testCanHandle() {
		assertTrue( accessor.canHandle( feature, "foo", null ) );
		assertTrue( accessor.canHandle( feature, "bar", null ) );
		
		assertFalse( accessor.canHandle( feature, "illegal", null ) );
	}
	
	public void testCanHandleType() {
		assertTrue( accessor.canHandle( type, "foo", null ) );
		assertTrue( accessor.canHandle( type, "bar", null ) );
		
		assertFalse( accessor.canHandle( type, "illegal", null ) );
	}
	
	public void testGet() {
		assertEquals( new Integer( 1 ), accessor.get( feature, "foo", null ) );
		assertEquals( new Double( 2.0 ), accessor.get( feature, "bar", null ) );
		assertEquals( "fid", SimpleFeaturePropertyAccessorFactory.FID_ACCESS.get( feature, "@id", null) );
		assertEquals( "fid", SimpleFeaturePropertyAccessorFactory.FID_ACCESS.get( feature, "@gml:id", null) );
                assertFalse( accessor.canHandle( feature, "illegal", null ) );
		assertNull( accessor.get( feature, "illegal", null ) );
	}
	
	public void testGetType() {
		assertEquals( type.getAttributeType( "foo" ), accessor.get( type, "foo", null ) );
		assertEquals( type.getAttributeType( "bar" ), accessor.get( type, "bar", null ) );
		assertNull( accessor.get( type, "illegal", null ) );
	}
	
	public void testSet() {
		try {
			accessor.set( feature, "foo", new Integer( 2 ), null );
		} catch (IllegalAttributeException e) {
			fail();
		}
		assertEquals( new Integer( 2 ), accessor.get( feature, "foo", null ) );
		
		try {
			accessor.set( feature, "bar", new Double( 1.0 ), null );
		} catch (IllegalAttributeException e) {
			fail();
		}
		assertEquals( new Double( 1.0 ), accessor.get( feature, "bar", null ) );
		try {
			accessor.set( feature, "@id", "fid2", null );
			fail( "Should have thrown exception trying to set fid" );
		}
		catch( IllegalAttributeException e ) {
		}
	}
	
	public void testSetType() {
		try {
			accessor.set( type, "foo", new Object(), null );
			fail( "trying to set attribute type should have thrown exception" );
		} catch (IllegalAttributeException e) {}
		
		
	}
	
}
