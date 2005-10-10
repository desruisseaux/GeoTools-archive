/*
 * FeatureTypeTest.java
 *
 * Created on July 21, 2003, 4:00 PM
 */

package org.geotools.feature.simple;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.geotools.feature.Types;
import org.geotools.feature.impl.AttributeFactoryImpl;
import org.geotools.feature.schema.DescriptorFactoryImpl;
import org.geotools.feature.type.TypeFactoryImpl;
import org.geotools.filter.Filter;
import org.opengis.feature.AttributeFactory;
import org.opengis.feature.schema.DescriptorFactory;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.TypeFactory;

/**
 * 
 * @author en
 * @author jgarnett
 */
public class SimpleFeatureTypeImplTest extends DataTestCase {

	TypeFactory typeFactory = new TypeFactoryImpl();
	DescriptorFactory descFactory = new DescriptorFactoryImpl();
	
	
	public SimpleFeatureTypeImplTest(String testName) {
		super(testName);
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(SimpleFeatureTypeImplTest.class);
		return suite;
	}

	public void testAbstractType() throws Exception {
		String localName = "AbstractThing";
		String nameSapace = "http://www.nowhereinparticular.net";
		QName name = new QName(nameSapace, localName);
		Set<Filter>restrictions = null;
		SimpleFeatureType superType = null;
		GeometryType defaultGeom = null;
		boolean isAbstract = true;
		List<AttributeType> types = new ArrayList<AttributeType>();

		SimpleFeatureType type1 = typeFactory.createFeatureType(name, types, defaultGeom, restrictions, superType, isAbstract);

		types.add(typeFactory.createType("X", String.class));

		SimpleFeatureType type2 = typeFactory.createFeatureType(name, types, defaultGeom, restrictions, type1, isAbstract);

		assertTrue(type1.isAbstract());
		assertTrue(type2.isAbstract());

		assertTrue(Types.isDescendedFrom(type2, type1));
		assertFalse(Types.isDescendedFrom(type1, type2));

		final QName gmlFeature = new QName("http://www.opengis.net/gml", "Feature");
		
		assertTrue(Types.isDescendedFrom(type1, gmlFeature));
		assertTrue(Types.isDescendedFrom(type2, gmlFeature));


		AttributeFactory attFact = new AttributeFactoryImpl();
		try {
			attFact.create(type1, null);
			fail("abstract type allowed create");
		} catch (UnsupportedOperationException uoe) {
			//OK
		}
		try {
			attFact.create(type2, null);
			fail("abstract type allowed create");
		} catch (UnsupportedOperationException uoe) {
			//OK
		}

		// with non-abstract super
		/*
		 * We no longer impose this restriction
		try {
			FeatureType[] supers = new FeatureType[1];
			supers[0] = FeatureTypeFactory.newFeatureType(null, "SillyThing",
					null, false);
			FeatureTypeFactory.newFeatureType(null, "BadFeature", null, true,
					supers);
			fail("allowed bad super");
		} catch (SchemaException se) {

		}
		*/
	}

	public void testEquals() throws Exception {
		final SimpleFeatureType ft, ft2;
		final String nameSpace1 = "http://www.nowhereinparticular.net";
		final String nameSpace2 = "http://www.somewhereelse.net";
		
		List<AttributeType>types = new ArrayList<AttributeType>();
		
		types.add(typeFactory.createType("X", String.class));
		ft = typeFactory.createFeatureType(new QName(nameSpace1, "Thing"), types, null);
		/*
		TypeBuilder at = FeatureTypeFactory.newInstance("Thing");
		at.setNamespace(new URI("http://www.nowhereinparticular.net"));
		at.addType(AttributeTypeFactory.newAttributeType("X", String.class));
		final FeatureType ft = at.getFeatureType();
		*/
		
		types.clear();
		types.add(typeFactory.createType("X", String.class));
		ft2 = typeFactory.createFeatureType(new QName(nameSpace1, "Thing"), types, null);
		
		/*
		at = FeatureTypeFactory.newInstance("Thing");
		at.setNamespace(new URI("http://www.nowhereinparticular.net"));
		at.addType(AttributeTypeFactory.newAttributeType("X", String.class));
		FeatureType ft2 = at.getFeatureType();
		*/

		assertEquals(ft, ft2);
		

		FeatureType ft3 = typeFactory.createFeatureType(new QName(nameSpace1, "Thingee"), types, null);		
		assertFalse(ft.equals(ft3));
		
		FeatureType ft4 = typeFactory.createFeatureType(new QName(nameSpace2, "Thing"), types, null);
		/*
		at = FeatureTypeFactory.createTemplate(ft);
		at.setNamespace(new URI("http://www.somewhereelse.net"));
		*/
		assertFalse(ft.equals(ft4));
		assertFalse(ft.equals(null));
	}

	/*
	public void testCopyFeature() throws Exception {
		Feature feature = lakeFeatures[0];
		assertDuplicate("feature", feature, feature.getFeatureType().duplicate(
				feature));
	}

	public void testDeepCopy() throws Exception {
		// primative
		String str = "FooBar";
		Integer i = new Integer(3);
		Float f = new Float(3.14);
		Double d = new Double(3.14159);
		AttributeType testType = AttributeTypeFactory.newAttributeType("test",
				Object.class);
		assertSame("String", str, testType.duplicate(str));
		assertSame("Integer", i, testType.duplicate(i));
		assertSame("Float", f, testType.duplicate(f));
		assertSame("Double", d, testType.duplicate(d));

		// collections
		Object objs[] = new Object[] { str, i, f, d, };
		int ints[] = new int[] { 1, 2, 3, 4, };
		List list = new ArrayList();
		list.add(str);
		list.add(i);
		list.add(f);
		list.add(d);
		Map map = new HashMap();
		map.put("a", str);
		map.put("b", i);
		map.put("c", f);
		map.put("d", d);
		assertDuplicate("objs", objs, testType.duplicate(objs));
		assertDuplicate("ints", ints, testType.duplicate(ints));
		assertDuplicate("list", list, testType.duplicate(list));
		assertDuplicate("map", map, testType.duplicate(map));

		// complex type
		Feature feature = lakeFeatures[0];

		Coordinate coords = new Coordinate(1, 3);
		Coordinate coords2 = new Coordinate(1, 3);
		GeometryFactory gf = new GeometryFactory();
		Geometry point = gf.createPoint(coords);
		Geometry point2 = gf.createPoint(coords2);

		// JTS does not implement Object equals contract
		assertTrue("jts identity", point != point2);
		assertTrue("jts equals1", point.equals(point2));
		assertTrue("jts equals", !point.equals((Object) point2));

		assertDuplicate("jts duplicate", point, point2);
		assertDuplicate("feature", feature, testType.duplicate(feature));
		assertDuplicate("point", point, testType.duplicate(point));
	}

	static Set immutable;
	static {
		immutable = new HashSet();
		immutable.add(String.class);
		immutable.add(Integer.class);
		immutable.add(Double.class);
		immutable.add(Float.class);
	}

	protected void assertDuplicate(String message, Object expected, Object value) {
		// Ensure value is equal to expected
		if (expected.getClass().isArray()) {
			int length1 = Array.getLength(expected);
			int length2 = Array.getLength(value);
			assertEquals(message, length1, length2);
			for (int i = 0; i < length1; i++) {
				assertDuplicate(message + "[" + i + "]",
						Array.get(expected, i), Array.get(value, i));
			}
			// assertNotSame( message, expected, value );
		} else if (expected instanceof Geometry) {
			// JTS Geometry does not meet the Obejct equals contract!
			// So we need to do our assertEquals statement
			//
			assertTrue(message, value instanceof Geometry);
			assertTrue(message, expected instanceof Geometry);
			Geometry expectedGeom = (Geometry) expected;
			Geometry actualGeom = (Geometry) value;
			assertTrue(message, expectedGeom.equals(actualGeom));
		} else if (expected instanceof Feature) {
			assertDuplicate(message, ((Feature) expected).getAttributes(null),
					((Feature) value).getAttributes(null));
		} else {
			assertEquals(message, expected, value);
		}
		// Ensure Non Immutables are actually copied
		if (!immutable.contains(expected.getClass())) {
			// assertNotSame( message, expected, value );
		}
	}
	*/
}
