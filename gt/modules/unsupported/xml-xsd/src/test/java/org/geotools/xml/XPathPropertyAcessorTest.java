package org.geotools.xml;

import junit.framework.TestCase;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureFactoryImpl;
import org.geotools.feature.simple.SimpleTypeBuilder;
import org.geotools.feature.simple.SimpleTypeFactoryImpl;
import org.geotools.filter.expression.PropertyAccessor;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class XPathPropertyAcessorTest extends TestCase {

	Feature target;
	
	protected void setUp() throws Exception {
		SimpleTypeBuilder typeBuilder = new SimpleTypeBuilder( new SimpleTypeFactoryImpl() );
		typeBuilder.setName( "test" );
		typeBuilder.setNamespaceURI( "http://www.geotools.org/test" );
		typeBuilder.addAttribute( "name", String.class );
		typeBuilder.addAttribute( "description", String.class );
		typeBuilder.addAttribute( "geometry", Geometry.class );
		FeatureType featureType = typeBuilder.feature();
		
		SimpleFeatureBuilder builder = new SimpleFeatureBuilder( new SimpleFeatureFactoryImpl() );
		builder.setType( featureType );
		builder.add( "theName" );
		builder.add( "theDescription" );
		builder.add( new GeometryFactory().createPoint( new Coordinate( 0, 0 ) )  );
		target = builder.feature( "fid" );
	}
	
	public void testSimpleXpath() {
		PropertyAccessor accessor = accessor( "name" );
		Object o = accessor.get( target, "name",null );
		assertNotNull( o );
		assertEquals( "theName", o );
		
		accessor = accessor( "description" );
		o = accessor.get( target, "description", null );
		assertNotNull( o );
		assertEquals( "theDescription", o );
		
		accessor = accessor( "geometry" );
		o = accessor.get( target, "geometry", null );
		assertNotNull( o );
		assertTrue( o instanceof Point );
	}
	
	public void testSimpleXpathWithNamespace() {
		PropertyAccessor accessor = accessor( "gml:name" );
		Object o = accessor.get( target, "gml:name", null );
		assertNotNull( o );
		assertEquals( "theName", o );
		
		accessor = accessor( "gml:description" );
		o = accessor.get( target, "gml:description", null );
		assertNotNull( o );
		assertEquals( "theDescription", o );
		
		accessor = accessor( "test:geometry" );
		o = accessor.get( target, "test:geometry", null );
		assertNotNull( o );
		assertTrue( o instanceof Point );
	}

	public void testPath() {
		PropertyAccessor accessor = accessor( "//name" );
		Object o = accessor.get( target, "//name",  null );
		assertNotNull( o );
		assertEquals( "theName", o );
		
		accessor = accessor( "//description" );
		o = accessor.get( target, "//description", null );
		assertNotNull( o );
		assertEquals( "theDescription", o );
		
		accessor = accessor( "//geometry" );
		o = accessor.get( target, "//geometry",  null );
		assertNotNull( o );
		assertTrue( o instanceof Point );
	}
	
	public void testPathWithNamespace() {
		PropertyAccessor accessor = accessor( "//gml:name" );
		Object o = accessor.get( target, "//gml:name", null );
		assertNotNull( o );
		assertEquals( "theName", o );
		
		accessor = accessor( "//gml:description" );
		o = accessor.get( target, "//gml:description", null );
		assertNotNull( o );
		assertEquals( "theDescription", o );
		
		accessor = accessor( "//test:geometry" );
		o = accessor.get( target,  "//test:geometry",  null );
		assertNotNull( o );
		assertTrue( o instanceof Point );
	}
	
	public void testIndex() {
		
		PropertyAccessor accessor = accessor( "*[1]" );
		Object o = accessor.get( target, "*[1]", null );
		assertNotNull( o );
		assertEquals( "theName", o );
		
		accessor = accessor( "*[2]" );
		o = accessor.get( target, "*[2]",null );
		assertNotNull( o );
		assertEquals( "theDescription", o );
		
		accessor = accessor( "*[3]" );
		o = accessor.get( target,  "*[3]", null );
		assertNotNull( o ); 
		assertTrue( o instanceof Point );
		
		
	}
	
	public void testPosition() {
		PropertyAccessor accessor = accessor( "*[position()=1]" );
		Object o = accessor.get( target, "*[position()=1]", null );
		assertEquals( "theName", o );
		
		accessor = accessor( "*[position()=2]" );
		o = accessor.get( target,  "*[position()=2]", null );
		assertNotNull( o );
		assertEquals( "theDescription", o );
		
		accessor = accessor( "*[position()=3]" );
		o = accessor.get( target, "*[position()=3]", null );
		assertNotNull( o );
		assertTrue( o instanceof Point );
	}
	
	public void testId() {
		PropertyAccessor accessor = accessor( "./@gml:id" );
		Object o = accessor.get( target, "./@gml:id", null );
		assertEquals( "fid", o );
	}
	
	public void testEmptyXpath() {
		assertFalse( accessor( "" ).canHandle( target, "", null ) );
	}
	
	PropertyAccessor accessor( String xpath ) {
		
		return new XPathPropertyAccessorFactory().createPropertyAccessor( 
			Feature.class, xpath, null, null
		);
	}
	
}
