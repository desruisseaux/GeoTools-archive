package org.geotools.feature.simple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.feature.impl.AttributeFactoryImpl;
import org.geotools.feature.type.TypeFactoryImpl;
import org.opengis.feature.Attribute;
import org.opengis.feature.AttributeFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.TypeFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class SimpleFeatureImplTest extends TestCase {

	private static TypeFactory typeFactory = new TypeFactoryImpl();

	private static AttributeFactory attFactory = new AttributeFactoryImpl();

	/**
	 * The logger for the default core module.
	 */
	private static final Logger LOGGER = Logger
			.getLogger(SimpleFeatureImplTest.class.getPackage().getName());

	/** Feature on which to preform tests */
	private SimpleFeature testFeature = null;

	TestSuite suite = null;

	public SimpleFeatureImplTest(String testName) {
		super(testName);
	}

	public static void main(String[] args) {
		// org.geotools.util.MonolineFormatter.initGeotools();
		junit.textui.TestRunner.run(suite());
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(SimpleFeatureImplTest.class);
		return suite;
	}

	public void setUp() throws Exception {
		super.setUp();
		testFeature = SampleFeatureFixtures.createFeature();
	}

	public void tearDown() throws Exception {
		super.tearDown();
		testFeature = null;
	}

	/**
	 * Test retrieving attribute values from a SimpleFeature by its
	 * AttributeType
	 * 
	 */
	public void testRetrieveType() {
		GeometryFactory gf = new GeometryFactory();
		SimpleFeatureType featureType = testFeature.getType();
		List<AttributeType> types = featureType.types();

		Object t = testFeature.get(types.get(0));
		assertTrue("geometry retrieval and match", ((Point) testFeature
				.get(types.get(0))).equals( gf.createPoint(new Coordinate(1, 2))));
		assertEquals("boolean retrieval and match", ((Boolean) testFeature
				.get(types.get(1))), new Boolean(true));
		assertEquals("character retrieval and match", ((Character) testFeature
				.get(types.get(2))), new Character('t'));
		assertEquals("byte retrieval and match", ((Byte) testFeature.get(types
				.get(3))), new Byte("10"));
		assertEquals("short retrieval and match", (Short) testFeature.get(types
				.get(4)), new Short("101"));
		assertEquals("integer retrieval and match", ((Integer) testFeature
				.get(types.get(5))), new Integer(1002));
		assertEquals("long retrieval and match", ((Long) testFeature.get(types
				.get(6))), new Long(10003));
		assertEquals("float retrieval and match", ((Float) testFeature
				.get(types.get(7))), new Float(10000.4));
		assertEquals("double retrieval and match", ((Double) testFeature
				.get(types.get(8))), new Double(100000.5));
		assertEquals("string retrieval and match", ((String) testFeature
				.get(types.get(9))), "test string data");

	}

	public void testRetrieveIndex() {
		GeometryFactory gf = new GeometryFactory();
		assertTrue("geometry retrieval and match", ((Point) testFeature.get(0))
				.equals(gf.createPoint(new Coordinate(1, 2))));
		assertEquals("boolean retrieval and match", ((Boolean) testFeature
				.get(1)), new Boolean(true));
		assertEquals("character retrieval and match", ((Character) testFeature
				.get(2)), new Character('t'));
		assertEquals("byte retrieval and match", ((Byte) testFeature.get(3)),
				new Byte("10"));
		assertEquals("short retrieval and match", ((Short) testFeature.get(4)),
				new Short("101"));
		assertEquals("integer retrieval and match", ((Integer) testFeature
				.get(5)), new Integer(1002));
		assertEquals("long retrieval and match", ((Long) testFeature.get(6)),
				new Long(10003));
		assertEquals("float retrieval and match", ((Float) testFeature.get(7)),
				new Float(10000.4));
		assertEquals("double retrieval and match",
				((Double) testFeature.get(8)), new Double(100000.5));
		assertEquals("string retrieval and match",
				((String) testFeature.get(9)), "test string data");

	}

	public void testRetrieveName() {
		GeometryFactory gf = new GeometryFactory();
		final Point point = gf.createPoint(new Coordinate(1, 2));
		assertTrue("geometry retrieval and match", ((Point) testFeature
				.get("testGeometry")).equals(point));
		assertEquals("boolean retrieval and match", ((Boolean) testFeature
				.get("testBoolean")), new Boolean(true));
		assertEquals("character retrieval and match", ((Character) testFeature
				.get("testCharacter")), new Character('t'));
		assertEquals("byte retrieval and match", ((Byte) testFeature
				.get("testByte")), new Byte("10"));
		assertEquals("short retrieval and match", ((Short) testFeature
				.get("testShort")), new Short("101"));
		assertEquals("integer retrieval and match", ((Integer) testFeature
				.get("testInteger")), new Integer(1002));
		assertEquals("long retrieval and match", ((Long) testFeature
				.get("testLong")), new Long(10003));
		assertEquals("float retrieval and match", ((Float) testFeature
				.get("testFloat")), new Float(10000.4));
		assertEquals("double retrieval and match", ((Double) testFeature
				.get("testDouble")), new Double(100000.5));
		assertEquals("string retrieval and match", ((String) testFeature
				.get("testString")), "test string data");

	}

	/*
	 * public void testBogusCreation() throws Exception { FeatureTypeFactory
	 * factory = FeatureTypeFactory.newInstance("test1");
	 * factory.addType(newAtt("billy", String.class, false));
	 * factory.addType(newAtt("jimmy", String.class, false)); FeatureType test =
	 * factory.getFeatureType(); try { test.create(null); fail("no error"); }
	 * catch (IllegalAttributeException iae) { }
	 * 
	 * try { test.create(new Object[32]); fail("no error"); } catch
	 * (IllegalAttributeException iae) { } }
	 */

	public void testBounds() throws Exception {
		GeometryFactory gf = new GeometryFactory();
		Geometry[] g = new Geometry[4];
		g[0] = gf.createPoint(new Coordinate(0, 0));
		g[1] = gf.createPoint(new Coordinate(0, 10));
		g[2] = gf.createPoint(new Coordinate(10, 0));
		g[3] = gf.createPoint(new Coordinate(10, 10));

		GeometryCollection gc = gf.createGeometryCollection(g);
		List<AttributeType> atts = new ArrayList<AttributeType>();
		atts.add(newAtt("p1", Point.class));
		atts.add(newAtt("p2", Point.class));
		atts.add(newAtt("p3", Point.class));
		atts.add(newAtt("p4", Point.class));
		SimpleFeatureType t = typeFactory.createFeatureType("test", atts, null);
		SimpleFeature f = attFactory.create(t, null, g);
		assertTrue(gc.getEnvelopeInternal().equals(f.getBounds()));

		g[1].getCoordinate().y = 20;
		g[2].getCoordinate().x = 20;
		f.set(1, g[1]);
		f.set(2, g[2]);
		gc = gf.createGeometryCollection(g);
		assertEquals(gc.getEnvelopeInternal(), f.getBounds());
	}

	/*
	 * public void testClone() { DefaultFeature f = (DefaultFeature)
	 * SampleFeatureFixtures .createFeature(); Feature c = (Feature) f.clone();
	 * for (int i = 0, ii = c.getNumberOfAttributes(); i < ii; i++) {
	 * assertEquals(c.getAttribute(i), f.getAttribute(i)); } }
	 */

	/*
	 * public void testClone2() throws Exception { FeatureType type =
	 * SampleFeatureFixtures.createTestType(); Object[] attributes =
	 * SampleFeatureFixtures.createAttributes(); DefaultFeature feature =
	 * (DefaultFeature) type .create(attributes, "fid"); Feature clone =
	 * (Feature) ((Cloneable) feature).clone(); assertTrue("Clone was not
	 * equal", feature.equals(clone)); }
	 */

	/*
	 * public void testOneWayCollectionMembership() { Feature f =
	 * SampleFeatureFixtures.createFeature(); FeatureCollection fc =
	 * FeatureCollections.newCollection(); f.setParent(fc);
	 * assertNotNull(f.getParent());
	 * f.setParent(FeatureCollections.newCollection());
	 * assertSame(f.getParent(), fc); }
	 */

	/*
	 * public void testToStringWontThrow() throws IllegalAttributeException {
	 * SimpleFeature f = (SimpleFeature) SampleFeatureFixtures.createFeature();
	 * f.setAttributes(new Object[f.getNumberOfAttributes()]); String s =
	 * f.toString(); }
	 */

	static AttributeType newAtt(String name, Class c) {
		return newAtt(name, c, true);
	}

	static AttributeType newAtt(String name, Class c, boolean nillable) {
		return typeFactory
				.createType(new QName(name), c, false, nillable, null);
	}

	public void testModify() throws Exception {
		String newData = "new test string data";
		testFeature.set("testString", newData);
		assertEquals("match modified (string) attribute", newData, testFeature
				.get("testString"));

		GeometryFactory gf = new GeometryFactory();
		Point newGeom = gf.createPoint(new Coordinate(3, 4));
		testFeature.set("testGeometry", newGeom);
		assertEquals("match modified (geometry) attribute", newGeom,
				testFeature.get("testGeometry"));

	}

	// public void testFindAttribute() {
	// DefaultFeature f = (DefaultFeature)
	// SampleFeatureFixtures.createFeature();
	// FeatureType t = f.getFeatureType();
	// for (int i = 0, ii = t.getAttributeCount(); i < ii; i++) {
	// AttributeType a = t.getAttributeType(i);
	// assertEquals(i, f.findAttributeByName(a.getName()));
	// }
	// assertEquals(-1, f.findAttributeByName("bilbo baggins"));
	// assertEquals(null, f.getAttribute("jimmy hoffa"));
	// }

	public void testAttributeAccess() throws Exception {
		// this ones kinda silly
		SimpleFeature f = (SimpleFeature) SampleFeatureFixtures.createFeature();
		List<Attribute> atts = f.getAttributes();

		for (int i = 0, ii = atts.size(); i < ii; i++) {
			Object expected = atts.get(i).get();
			assertEquals(expected, f.get(i));
		}

		List<Attribute> attsAgain = f.getAttributes();
		assertTrue(atts != attsAgain);

		f.set(atts);
		attsAgain = f.getAttributes();
		assertTrue(atts != attsAgain);

		for (int i = 0, ii = atts.size(); i < ii; i++) {
			assertEquals(atts.get(i), f.get(i));
			assertEquals(attsAgain.get(i), f.get(i));
		}
		try {
			f.set(1244, "x");
			fail("not out of bounds");
		} catch (IndexOutOfBoundsException aioobe) {
			// OK
		}
		try {
			f.set("1244", "x");
			fail("allowed bogus attribute setting");
			// } catch (IllegalAttributeException iae) {
		} catch (IllegalArgumentException iae) {
			// OK
		}
		try {
			f.set("testGeometry", "x");
			fail("allowed bogus attribute setting");
			// } catch (IllegalAttributeException iae) {
		} catch (IllegalArgumentException iae) {
			// OK
		} catch (RuntimeException rt) {
			// why is it being catched?
		}
	}

	// IanS - this is no longer good, cause we deal with parsing
	// public void testEnforceType() {
	//        
	// Date d = new Date();
	//        
	// Feature f = SampleFeatureFixtures.createFeature();
	// for (int i = 0, ii = f.getNumberOfAttributes(); i < ii; i++) {
	// try {
	// f.setAttribute(i, d);
	// } catch (IllegalAttributeException iae) {
	// continue;
	// }
	// fail("No error thrown during illegal set");
	// }
	//
	// }

	public void testEquals() throws Exception {
		SimpleFeature f1 = SampleFeatureFixtures.createFeature();
		SimpleFeature f2 = SampleFeatureFixtures.createFeature();
		assertEquals(f1, f1);
		assertEquals(f2, f2);

		assertTrue(!f1.equals(f2)); // due to different ids
		assertTrue(!f1.equals(null));
		/*
		 * FeatureType another = FeatureTypeFactory.newFeatureType( new
		 * AttributeType[] { newAtt("name", String.class) }, "different");
		 */
		TypeFactory typeFactory = new TypeFactoryImpl();
		AttributeFactory attFactory = new AttributeFactoryImpl();
		List<AttributeType> atts = Arrays
				.asList(new AttributeType[] { typeFactory.createType("name",
						String.class)

				});
		SimpleFeatureType another = typeFactory.createFeatureType("different",
				atts, null);
		SimpleFeature anotherFeature = attFactory.create(another, null);
		assertFalse(f1.equals(anotherFeature));
	}

	/*
	 * This is actually a test for FeatureTypeFlat, but there is no test for
	 * that written right now, so I'm just putting it here, as I just changed
	 * the getDefaultGeometry method, and it should have a unit test. It tests
	 * to make sure getDefaultGeometry returns null if there is no geometry, as
	 * we now allow
	 */
	public void testDefaultGeometry() throws Exception {
		SimpleFeatureType testType = testFeature.getType();

		AttributeType geometry = testType.get("testGeometry");
		assertSame(geometry, testType.getDefaultGeometry());
		assertEquals(testFeature.getDefaultGeometry().getEnvelopeInternal(),
				testFeature.getBounds());

		/*
		 * FeatureType another = FeatureTypeFactory.newFeatureType( new
		 * AttributeType[] { newAtt("name", String.class) }, "different");
		 * DefaultFeature f1 = (DefaultFeature) another.create(new Object[1]);
		 */
		TypeFactory typeFactory = new TypeFactoryImpl();
		AttributeFactory attFactory = new AttributeFactoryImpl();
		List<AttributeType> atts = Arrays
				.asList(new AttributeType[] { typeFactory.createType("name",
						String.class)

				});
		SimpleFeatureType another = typeFactory.createFeatureType("different",
				atts, null);
		SimpleFeature f1 = attFactory.create(another, null);
		assertNull(f1.getDefaultGeometry());

		try {
			f1.setDefaultGeometry(null);
			fail("allowed bogus default geometry set ");
			// } catch (IllegalAttributeException iae) {
		} catch (IllegalArgumentException iae) {

		}
	}

}
