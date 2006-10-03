package org.geotools.feature.simple;

import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.DefaultFeatureTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import junit.framework.TestCase;

public class SimpleFeatureBuilderTest extends TestCase {

	SimpleFeatureBuilder builder;
	
	protected void setUp() throws Exception {
		AttributeType[] attributes = new AttributeType[] {
			AttributeTypeFactory.newAttributeType( "point", Point.class ),
			AttributeTypeFactory.newAttributeType( "integer", Integer.class ), 
		};
		
		FeatureType type = DefaultFeatureTypeFactory.newFeatureType( attributes, "test" );
		builder = new SimpleFeatureBuilder( new SimpleFeatureFactoryImpl() );
		builder.init();
		builder.setType( type );
		
	}
	
	public void test() throws Exception {
		GeometryFactory gf = new GeometryFactory();
		builder.add( gf.createPoint( new Coordinate( 0, 0 ) ) );
		builder.add( new Integer( 1 ) );
		
		Feature feature = builder.feature( "fid" );
		assertNotNull( feature );
		
		assertEquals( 2, feature.getNumberOfAttributes() );
		
		assertTrue( gf.createPoint( new Coordinate( 0, 0) ).equals( (Geometry) feature.getAttribute( "point" ) ) );
		assertEquals( new Integer( 1 ) , feature.getAttribute( "integer" ) );
	}
}
