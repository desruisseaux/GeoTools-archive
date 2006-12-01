package org.geotools.filter.expression;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureFactoryImpl;
import org.geotools.feature.simple.SimpleTypeBuilder;
import org.geotools.feature.simple.SimpleTypeFactoryImpl;
import org.geotools.filter.expression.SimpleFeaturePropertyAccessorFactory;

import junit.framework.TestCase;

public class SimpleFeaturePropertyAccessorTest extends TestCase {

	Feature feature;
	PropertyAccessor accessor = SimpleFeaturePropertyAccessorFactory.ATTRIBUTE_ACCESS;
	
	protected void setUp() throws Exception {
		SimpleTypeBuilder typeBuilder = new SimpleTypeBuilder( new SimpleTypeFactoryImpl() );
		typeBuilder.init();
		typeBuilder.setName( "test" );
		typeBuilder.setNamespaceURI( "http://www.geotools.org/test" );
		typeBuilder.addAttribute( "foo", Integer.class );
		typeBuilder.addAttribute( "bar", Double.class );
		
		FeatureType type = typeBuilder.feature();
		
		SimpleFeatureBuilder builder = new SimpleFeatureBuilder( new SimpleFeatureFactoryImpl() );
		builder.init();
		builder.setType( type );
		builder.add( new Integer( 1 ) );
		builder.add( new Double( 2.0 ) );

		feature = builder.feature( "fid" );
		accessor = SimpleFeaturePropertyAccessorFactory.ATTRIBUTE_ACCESS;
	}
	
	
	public void testCanHandle() {
		assertTrue( accessor.canHandle( feature, "foo", null ) );
		assertTrue( accessor.canHandle( feature, "bar", null ) );
		
		assertFalse( accessor.canHandle( feature, "illegal", null ) );
	}
	
	public void testGet() {
		assertEquals( new Integer( 1 ), accessor.get( feature, "foo", null ) );
		assertEquals( new Double( 2.0 ), accessor.get( feature, "bar", null ) );
		assertEquals( "fid", SimpleFeaturePropertyAccessorFactory.FID_ACCESS.get( feature, "@id", null) );
		assertEquals( "fid", SimpleFeaturePropertyAccessorFactory.FID_ACCESS.get( feature, "@gml:id", null) );
                assertFalse( accessor.canHandle( feature, "illegal", null ) );
		assertNull( accessor.get( feature, "illegal", null ) );
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
}
