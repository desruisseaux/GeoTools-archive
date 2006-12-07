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

	FeatureType type;
	Feature target;
	
	protected void setUp() throws Exception {
		SimpleTypeBuilder typeBuilder = new SimpleTypeBuilder( new SimpleTypeFactoryImpl() );
		typeBuilder.setName( "test" );
		typeBuilder.setNamespaceURI( "http://www.geotools.org/test" );
		typeBuilder.addAttribute( "name", String.class );
		typeBuilder.addAttribute( "description", String.class );
		typeBuilder.addAttribute( "geometry", Geometry.class );
		type = typeBuilder.feature();
		
		SimpleFeatureBuilder builder = new SimpleFeatureBuilder( new SimpleFeatureFactoryImpl() );
		builder.setType( type );
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
		
		accessor = accessor( "foo" );
		o = accessor.get( target, "foo", null );
		assertNull( o );
	}
	
	public void testSimpleXpathType() {
		PropertyAccessor accessor = accessor( "name" );
		Object o = accessor.get( type, "name",null );
		assertNotNull( o );
		assertEquals( type.getAttributeType("name"), o );
		
		accessor = accessor( "description" );
		o = accessor.get( type, "description", null );
		assertNotNull( o );
		assertEquals( type.getAttributeType("description"), o );
		
		accessor = accessor( "geometry" );
		o = accessor.get( type, "geometry", null );
		assertNotNull( o );
		assertEquals( type.getAttributeType( "geometry" ), o );
		
		accessor = accessor( "foo" );
		o = accessor.get( type, "foo", null );
		assertNull( o );
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

	public void testSimpleXpathWithNamespaceType() {
		PropertyAccessor accessor = accessor( "gml:name" );
		Object o = accessor.get( type, "gml:name", null );
		assertNotNull( o );
		assertEquals( type.getAttributeType( "name" ), o );
		
		accessor = accessor( "gml:description" );
		o = accessor.get( type, "gml:description", null );
		assertNotNull( o );
		assertEquals( type.getAttributeType( "description" ), o );
		
		accessor = accessor( "test:geometry" );
		o = accessor.get( type, "test:geometry", null );
		assertNotNull( o );
		assertEquals( type.getAttributeType( "geometry" ), o );
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
	
	public void testPathType() {
		PropertyAccessor accessor = accessor( "//name" );
		Object o = accessor.get( type, "//name",  null );
		assertNotNull( o );
		assertEquals( type.getAttributeType( "name" ), o );
		
		accessor = accessor( "//description" );
		o = accessor.get( type, "//description", null );
		assertNotNull( o );
		assertEquals( type.getAttributeType( "description" ), o );
		
		accessor = accessor( "//geometry" );
		o = accessor.get( type, "//geometry",  null );
		assertNotNull( o );
		assertEquals( type.getAttributeType( "geometry" ), o );
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
	
	public void testPathWithNamespaceType() {
		PropertyAccessor accessor = accessor( "//gml:name" );
		Object o = accessor.get( type, "//gml:name", null );
		assertNotNull( o );
		assertEquals( type.getAttributeType( "name" ), o );
		
		accessor = accessor( "//gml:description" );
		o = accessor.get( type, "//gml:description", null );
		assertNotNull( o );
		assertEquals( type.getAttributeType( "description" ), o );
		
		accessor = accessor( "//test:geometry" );
		o = accessor.get( type,  "//test:geometry",  null );
		assertNotNull( o );
		assertEquals( type.getAttributeType( "geometry" ), o );
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
	
	public void testIndexType() {
		
		PropertyAccessor accessor = accessor( "*[1]" );
		Object o = accessor.get( type, "*[1]", null );
		assertNotNull( o );
		assertEquals( type.getAttributeType( "name" ), o );
		
		accessor = accessor( "*[2]" );
		o = accessor.get( type, "*[2]",null );
		assertNotNull( o );
		assertEquals( type.getAttributeType( "description" ), o );
		
		accessor = accessor( "*[3]" );
		o = accessor.get( type,  "*[3]", null );
		assertNotNull( o ); 
		assertEquals( type.getAttributeType( "geometry" ), o );
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
	
	public void testPositionType() {
		PropertyAccessor accessor = accessor( "*[position()=1]" );
		Object o = accessor.get( type, "*[position()=1]", null );
		assertEquals( type.getAttributeType( "name" ), o );
		
		accessor = accessor( "*[position()=2]" );
		o = accessor.get( type,  "*[position()=2]", null );
		assertNotNull( o );
		assertEquals( type.getAttributeType( "description" ), o );
		
		accessor = accessor( "*[position()=3]" );
		o = accessor.get( type, "*[position()=3]", null );
		assertNotNull( o );
		assertEquals( type.getAttributeType( "geometry" ), o );
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
