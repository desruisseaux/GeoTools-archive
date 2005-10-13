package org.geotools.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.geotools.feature.impl.AttributeFactoryImpl;
import org.geotools.feature.impl.AttributeImpl;
import org.geotools.feature.schema.DescriptorFactoryImpl;
import org.geotools.feature.type.ComplexTestData;
import org.geotools.feature.type.TypeFactoryImpl;
import org.opengis.feature.Attribute;
import org.opengis.feature.AttributeFactory;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Feature;
import org.opengis.feature.schema.Descriptor;
import org.opengis.feature.schema.DescriptorFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.TypeFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class XPathTest extends TestCase {

	private SimpleFeature simpleFeature;

	private Feature complexFeature;

	protected void setUp() throws Exception {
		super.setUp();
		simpleFeature = createSimpleFeature();
		// complexFeature = createComplexFeature();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		simpleFeature = null;
		complexFeature = null;
	}

	public void testSelf() throws Exception {
		Object val;

		val = XPath.get(simpleFeature, ".");
		assertEquals(simpleFeature, val);

		TypeFactory typef = new TypeFactoryImpl();
		AttributeType simpleType = typef.createType("SimpleType", String.class);
		AttributeImpl simpleAtt = new AttributeImpl(simpleType);
		simpleAtt.set("test value");

		val = XPath.get(simpleAtt, ".");
		assertEquals(simpleAtt, val);

	}

	public void testNestedGetValue() {

	}

	public void testSimpleFeatureGetValue() throws Exception {
		Attribute val;

		val = (Attribute) XPath.get(simpleFeature, "oid");
		assertEquals("testOid", val.get());

		val = (Attribute) XPath.get(simpleFeature, "number");
		assertEquals(new Integer(1), val.get());

		val = (Attribute) XPath.get(simpleFeature, "the_geom");
		assertTrue(val.get() instanceof Point);
		
	}

	public void testComplexFeatureGetValue() throws Exception {
		Object val;

		TypeFactory tf = new TypeFactoryImpl();
		DescriptorFactory df = new DescriptorFactoryImpl();
		AttributeFactory af = new AttributeFactoryImpl();

		FeatureType<?> complexType = ComplexTestData
				.createExample01MultiValuesComplexProperty(tf, df);
		Descriptor schema = complexType.getDescriptor();
		Feature complex = af.create(complexType, null);
		String nsUri = complexType.getName().getNamespaceURI();

		AttributeType type = Descriptors.type(schema, new QName(nsUri,
				"sitename"));
		Attribute att = af.create(type, null, "sitename1");
		complex.add(0, att);

		type = Descriptors.type(schema, new QName(nsUri, "anzlic_no"));
		att = af.create(type, null, "anzlic_no1");
		complex.add(1, att);

		Point location = new GeometryFactory()
				.createPoint(new Coordinate(1, 1));
		type = Descriptors.type(schema, new QName(nsUri, "location"));
		att = af.create(type, null, location);
		complex.add(2, att);

		ComplexType mtype = (ComplexType) Descriptors.type(schema, new QName(
				nsUri, "measurement"));

		ComplexAttribute measurement = af.create(mtype, "id1");
		List<Attribute>mcontent = new ArrayList<Attribute>();
		type = Descriptors.type(mtype.getDescriptor(), new QName(nsUri,
				"determinand_description"));
		mcontent.add(af.create(type, null, "determinand1"));
		type = Descriptors.type(mtype.getDescriptor(), new QName(nsUri,
				"result"));
		mcontent.add(af.create(type, null, "1000"));
		measurement.set(mcontent);

		complex.add(3, measurement);

		type = Descriptors.type(schema, new QName(nsUri, "project_no"));
		att = af.create(type, null, "project_no1");
		complex.add(4, att);

		/*
		 * Map<String, String> ns = Descriptors.namespaces(schema); val =
		 * XPath.get(ns, complex, "sitename");
		 */
		val = XPath.get(complex, "sitename");
		assertNotNull(val);
		assertEquals("sitename1", ((Attribute) val).get());

		val = XPath.get(complex, "anzlic_no");
		assertNotNull(val);
		assertEquals("anzlic_no1", ((Attribute) val).get());

		val = XPath.get(complex, "location");
		assertNotNull(val);
		assertTrue(location.equals(((Attribute) val).get()));

		val = XPath.get(complex, "measurement");
		assertNotNull(val);
		assertEquals("measurement", ((Attribute) val).getType().name());

		val = XPath.get(complex, "measurement[1]");
		assertNotNull(val);
		assertEquals("measurement", ((Attribute) val).getType().name());

		val = XPath.get(complex, "measurement/determinand_description");
		assertNotNull(val);
		assertEquals("determinand1", ((Attribute) val).get());

		val = XPath.get(complex, "measurement/result");
		assertNotNull(val);
		assertEquals("1000", ((Attribute) val).get());

		val = XPath.get(complex, "measurement/result/../..");
		assertNotNull(val);
		assertEquals(complex, val);
	}

	/*
	 * public void testSimpleFeatureSetValue(){
	 * 
	 * FeatureXPath.setValue(simpleFeature, "oid", "newId");
	 * assertEquals("newId", simpleFeature.getAttribute("oid"));
	 * 
	 * FeatureXPath.setValue(simpleFeature, "number", new Integer(2));
	 * assertEquals(new Integer(2), simpleFeature.getAttribute("number"));
	 * 
	 * Point newPoint = new GeometryFactory().createPoint(new Coordinate(2,
	 * -2)); FeatureXPath.setValue(simpleFeature, "the_geom", newPoint);
	 * assertEquals(2D, ((Point)simpleFeature.getAttribute("the_geom")).getX(),
	 * 0); assertEquals(-2D,
	 * ((Point)simpleFeature.getAttribute("the_geom")).getY(), 0);
	 *  }
	 */

	/**
	 * Creates a simple feature with the following characteristics:
	 * <p>
	 * <ul>
	 * <li>schema: "oid:String,name:String,number:Integer,the_geom:Point"
	 * <ul>
	 * Values:
	 * <li>oid: "testOid"
	 * <li>name: "testName"
	 * <li>number: new Integer(1)
	 * <li>the_geom: POINT(1, -1)
	 * </ul>
	 * </ul>
	 * </p>
	 * 
	 * @return
	 * @throws Exception
	 */
	private static SimpleFeature createSimpleFeature() throws Exception {
		final TypeFactory tf = new TypeFactoryImpl();
		final DescriptorFactory df = new DescriptorFactoryImpl();
		final AttributeFactory af = new AttributeFactoryImpl();

		List<AttributeType> types = new ArrayList<AttributeType>();
		types.add(tf.createType("oid", String.class));
		types.add(tf.createType("name", String.class));
		types.add(tf.createType("number", Integer.class));
		types.add(tf.createType("the_geom", Point.class));

		SimpleFeatureType type = tf.createFeatureType("testType", types,
				(GeometryType) types.get(3));

		SimpleFeature f = af.create(type, null);
		f.set("oid", "testOid");
		f.set("name", "testName");
		f.set("number", new Integer(1));
		f.set("the_geom", new GeometryFactory().createPoint(new Coordinate(1,
				-1)));
		return f;
	}

	/*
	 * private static Feature createComplexFeature(){
	 *  }
	 */

}
