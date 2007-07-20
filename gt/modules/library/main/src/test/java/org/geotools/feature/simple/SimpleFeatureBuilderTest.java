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
		SimpleTypeBuilder typeBuilder = new SimpleTypeBuilder();
		typeBuilder.setName( "test" );
		typeBuilder.add( "point", Point.class );
		typeBuilder.add( "integer", Integer.class );
		
		SimpleFeatureType featureType = typeBuilder.buildFeatureType();
		
		builder = new SimpleFeatureBuilder();
		builder.setType( featureType );
	}
	
	public void testSanity() throws Exception {
		GeometryFactory gf = new GeometryFactory();
		builder.add( gf.createPoint( new Coordinate( 0, 0 ) ) );
		builder.add( new Integer( 1 ) );
		
		SimpleFeature feature = builder.feature( "fid" );
		assertNotNull( feature );
		
		assertEquals( 2, feature.getNumberOfAttributes() );
		
		assertTrue( gf.createPoint( new Coordinate( 0, 0) ).equals( (Geometry) feature.getValue( "point" ) ) );
		assertEquals( new Integer( 1 ) , feature.getValue( "integer" ) );
	}
}
