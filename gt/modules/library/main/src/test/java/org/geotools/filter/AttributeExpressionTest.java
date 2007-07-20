package org.geotools.filter;

import org.geotools.feature.DefaultFeatureBuilder;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureFactoryImpl;
import org.geotools.feature.simple.SimpleTypeBuilder;
import org.geotools.feature.simple.SimpleTypeFactoryImpl;
import org.geotools.feature.type.DefaultFeatureTypeBuilder;

import junit.framework.TestCase;

public class AttributeExpressionTest extends TestCase {

	public void testFeature() {
		DefaultFeatureTypeBuilder typeBuilder = new DefaultFeatureTypeBuilder();
		
		typeBuilder.setName( "test" );
		typeBuilder.setNamespaceURI( "http://www.geotools.org/test" );
		typeBuilder.add( "foo", Integer.class );
		typeBuilder.add( "bar", Double.class );
		
		FeatureType type = (FeatureType) typeBuilder.buildFeatureType();
		
		DefaultFeatureBuilder builder = new DefaultFeatureBuilder();
		
		builder.setType( type );
		builder.add( new Integer( 1 ) );
		builder.add( new Double( 2.0 ) );

		Feature feature = (Feature) builder.feature( "fid" );
		
		AttributeExpressionImpl ex = new AttributeExpressionImpl( "foo" );
		assertEquals( new Integer( 1 ), ex.evaluate( feature ) );
		
		ex = new AttributeExpressionImpl( "@id" );
		assertEquals( "fid", ex.evaluate( feature ) );
	}
	
	
}
