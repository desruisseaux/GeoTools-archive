package org.geotools.feature.simple;

import java.util.List;

import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.FeatureType;
import org.geotools.feature.Name;
import org.geotools.feature.type.SchemaImpl;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeatureCollectionType;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Schema;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

import junit.framework.TestCase;

/**
 * This test cases will check that the typeBuilder works as advertised.
 * <p>
 * This test uses the container set up for simple types implementation. If you
 * wish to subclass this you may reuse the test methods with alternate
 * implementations.
 * </p>
 * 
 * @author Jody Garnett
 * @author Justin Deoliveira
 */
public class SimpleTypeBuilderTest extends TestCase {

	static final String URI = "gopher://localhost/test";
	
	SimpleTypeBuilder builder;
	
	protected void setUp() throws Exception {
		Schema schema = new SchemaImpl( "test" );
		
		AttributeType pointType = AttributeTypeFactory.newAttributeType( "pointType", Point.class );
		schema.put( new Name( "test", "pointType" ), pointType );
		
		AttributeType intType = AttributeTypeFactory.newAttributeType( "intType", Integer.class );
		schema.put( new Name( "test", "intType" ), intType );
		
		builder = new SimpleTypeBuilder( new SimpleTypeFactoryImpl() );
		//builder.load( schema );
	}
	
	public void test() {
		builder.setName( "testName" );
		builder.setNamespaceURI( "testNamespaceURI" );
		builder.addGeometry( "point", Point.class );
		builder.addAttribute( "integer", Integer.class );
		
		FeatureType type = builder.feature();
		assertNotNull( type );
		
		assertEquals( 2, type.getAttributeCount() );
		
		AttributeType t = type.getAttributeType( "point" );
		assertNotNull( t );
		assertEquals( Point.class, t.getType() );
		
		t = type.getAttributeType( "integer" );
		assertNotNull( t );
		assertEquals( Integer.class, t.getType() );
		
		t = type.getDefaultGeometry();
		assertNotNull( t );
		assertEquals( Point.class, t.getType() );
	}
	
	/**
	 * Defines a simple setup of Address, Fullname, Person and then defines a
	 * collection of Person as a Country.
	 * 
	 * <pre><code>
	 *     +-------------------+
	 *     | ROAD (Feature)    |
	 *     +-------------------+
	 *     |name: Text         |
	 *     |route: Route       |
	 *     +-------------------+
	 *              *|
	 *               |members
	 *               |
	 *     +--------------------------+
	 *     | ROADS(FeatureCollection) |
	 *     +--------------------------+
	 * </code></pre>
	 * 
	 * <p>
	 * Things to note in this example:
	 * <ul>
	 * <li>Definition of "atomic" types like Text and Number that bind directly
	 * to Java classes
	 * <li>Definition of "geometry" types like Route that bind to a geometry
	 * implementation
	 * <li>Definition of a "simple feature" made of atomic and geometry types
	 * without support for descriptors or associations
	 * <li>Definition of a "simple feature collection" able to hold a
	 * collection of simple feature, but unable to hold attributes itself.
	 * </ul>
	 */
	public void testBuilding() throws Exception {
		//builder.load(new SimpleSchema()); // load java types
		builder.setNamespaceURI(URI);
		builder.setCRS(DefaultGeographicCRS.WGS84);

		builder.setName("ROAD");
		builder.addAttribute("name", String.class);
		builder.addGeometry("route", LineString.class);
		
		//SimpleFeatureType ROAD = builder.feature();
		FeatureType ROAD = builder.feature();
		
		assertEquals(2, ROAD.getAttributeCount());
		assertEquals(LineString.class, ROAD.getDefaultGeometry().getType());
		//assertTrue(List.class.isInstance(ROAD.attributes()));

//		builder.setName("ROADS");
//		builder.setMember(ROAD);
//
//		SimpleFeatureCollectionType ROADS = builder.collection();
//		assertEquals(0, ROADS.attributes().size());
//		assertEquals(ROAD, ROADS.getMemberType());
	}

	public void testTerse() throws Exception {
		//builder.load(new SimpleSchema()); // load java types
		builder.setNamespaceURI(URI);
		builder.setCRS(DefaultGeographicCRS.WGS84);

		/*SimpleFeatureType*/org.geotools.feature.FeatureType ROAD = 
			builder.name("ROAD").attribute("name", String.class).geometry("route", LineString.class)
				.feature();

		//assertEquals(2, ROAD.getNumberOfAttribtues());
		assertEquals(2, ROAD.getAttributeCount());
		assertEquals(LineString.class, ROAD.getDefaultGeometry().getType());
		//assertTrue(List.class.isInstance(ROAD.attributes()));
		assertEquals( DefaultGeographicCRS.WGS84, ROAD.getDefaultGeometry().getCoordinateSystem() );
		
//		SimpleFeatureCollectionType ROADS = builder.name("ROADS").member(ROAD).collection();
//
//		assertEquals(0, ROADS.attributes().size());
//		assertEquals(ROAD, ROADS.getMemberType());
//		assertEquals( DefaultGeographicCRS.WGS84, ROADS.getCRS() );		
	}
}
