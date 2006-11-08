package org.geotools.filter;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureFactoryImpl;
import org.geotools.feature.simple.SimpleTypeBuilder;
import org.geotools.feature.simple.SimpleTypeFactoryImpl;

import junit.framework.TestCase;

public class AttributeExpressionTest extends TestCase {

	public void testFeature() {
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

		Feature feature = builder.feature( "fid" );
		
		AttributeExpressionImpl ex = new AttributeExpressionImpl( "foo" );
		assertEquals( new Integer( 1 ), ex.evaluate( feature ) );
		
		ex = new AttributeExpressionImpl( "@id" );
		assertEquals( "fid", ex.evaluate( feature ) );
	}
	
	
}
