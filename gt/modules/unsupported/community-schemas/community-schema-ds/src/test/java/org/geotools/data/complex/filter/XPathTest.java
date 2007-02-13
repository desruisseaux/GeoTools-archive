package org.geotools.data.complex.filter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.geotools.data.ComplexTestData;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.TypeName;

/**
 * @author gabriel
 * 
 */
public class XPathTest extends TestCase {

//	private SimpleFeature simpleFeature;

//	private Feature complexFeature;

	protected void setUp() throws Exception {
		super.setUp();
//		simpleFeature = createSimpleFeature();
		// complexFeature = createComplexFeature();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
//		simpleFeature = null;
//		complexFeature = null;
	}

	// public void testSelf() throws Exception {
	// Object val;
	//
	// val = XPath.get(simpleFeature, ".");
	// assertEquals(simpleFeature, val);
	//
	// TypeFactory typef = new TypeFactoryImpl();
	// AttributeType simpleType = typef.createType("SimpleType", String.class);
	// AttributeDescriptor simpleTypeNode = new NodeImpl(simpleType, 1, 1, new
	// AttributeName("simpleElement"));
	// AttributeImpl simpleAtt = new AttributeImpl(simpleTypeNode);
	// simpleAtt.set("test value");
	//
	// val = XPath.get(simpleAtt, ".");
	// assertEquals(simpleAtt, val);
	//
	// }

	// public void testSimpleFeatureGetValue() throws Exception {
	// Attribute val;
	//
	// val = (Attribute) XPath.get(simpleFeature, "oid");
	// assertEquals("testOid", val.get());
	//
	// val = (Attribute) XPath.get(simpleFeature, "number");
	// assertEquals(new Integer(1), val.get());
	//
	// val = (Attribute) XPath.get(simpleFeature, "the_geom");
	// assertTrue(val.get() instanceof Point);
	//
	// }

//	public void testComplexFeatureGetValue() throws Exception {
//		Object val;
//
//		TypeFactory tf = new TypeFactoryImpl();
//		DescriptorFactory df = new DescriptorFactoryImpl();
//		AttributeFactory af = new AttributeFactoryImpl();
//
//		FeatureType complexType = ComplexTestData
//				.createExample01MultiValuedComplexProperty(tf, df);
//		Descriptor schema = complexType.getDescriptor();
//
//		Feature complex = af.createFeature(new NodeImpl(complexType), null);
//		List /* <Attribute> */contents = new LinkedList();
//
//		String nsUri = complexType.getName().getNamespaceURI();
//
//		AttributeDescriptor node = Descriptors.node(schema, new AttributeName(
//				nsUri, "sitename"));
//		Attribute att = af.create(node, null, "sitename1");
//		contents.add(att);
//
//		node = Descriptors.node(schema, new AttributeName(nsUri, "anzlic_no"));
//		att = af.create(node, null, "anzlic_no1");
//		contents.add(att);
//
//		Point location = new GeometryFactory()
//				.createPoint(new Coordinate(1, 1));
//		node = Descriptors.node(schema, new AttributeName(nsUri, "location"));
//		att = af.create(node, null, location);
//		contents.add(att);
//
//		AttributeDescriptor mtype = Descriptors.node(schema, new AttributeName(
//				nsUri, "measurement"));
//
//		ComplexAttribute measurement = af.createComplex(mtype, "id1");
//		ComplexType measurementType = (ComplexType) measurement.getType();
//
//		List/* <Attribute> */mcontent = new ArrayList/* <Attribute> */();
//		node = Descriptors.node(measurementType.getDescriptor(),
//				new AttributeName(nsUri, "determinand_description"));
//		mcontent.add(af.create(node, null, "determinand1"));
//		node = Descriptors.node(measurementType.getDescriptor(),
//				new AttributeName(nsUri, "result"));
//		mcontent.add(af.create(node, null, "1000"));
//		measurement.set(mcontent);
//
//		contents.add(measurement);
//
//		node = Descriptors.node(schema, new AttributeName(nsUri, "project_no"));
//		att = af.create(node, null, "project_no1");
//		contents.add(att);
//
//		complex.set(contents);
//
//		val = XPath.get(complex, "sitename");
//		assertNotNull(val);
//		assertEquals("sitename1", ((Attribute) val).get());
//
//		val = XPath.get(complex, "anzlic_no");
//		assertNotNull(val);
//		assertEquals("anzlic_no1", ((Attribute) val).get());
//
//		val = XPath.get(complex, "location");
//		assertNotNull(val);
//		assertTrue(location.equals(((Attribute) val).get()));
//
//		AttributeName expectedName = new AttributeName(ComplexTestData.NSURI,
//				"measurement");
//
//		val = XPath.get(complex, "measurement");
//		assertNotNull(val);
//		assertEquals(expectedName, ((Attribute) val).getType().getName());
//
//		val = XPath.get(complex, "measurement[1]");
//		assertNotNull(val);
//		assertEquals(expectedName, ((Attribute) val).getType().getName());
//
//		val = XPath.get(complex, "measurement/determinand_description");
//		assertNotNull(val);
//		assertEquals("determinand1", ((Attribute) val).get());
//
//		val = XPath.get(complex, "measurement/result");
//		assertNotNull(val);
//		assertEquals("1000", ((Attribute) val).get());
//
//		val = XPath.get(complex, "measurement/result/../..");
//		assertNotNull(val);
//		assertEquals(complex, val);
//	}

	public void testSteps() throws Exception {
		FeatureType complexType = ComplexTestData
				.createExample01MultiValuedComplexProperty(
						new org.geotools.feature.iso.type.TypeFactoryImpl());
		TypeName name = complexType.getName();
		try {
			XPath.steps(name, null);
			fail("passed null");
		} catch (NullPointerException e) {
		}

		try {
			XPath.steps(name, " ");
			fail("passed empty");
		} catch (IllegalArgumentException e) {
		}

		List expected;
		String xpath;

		xpath = "/";
		assertEquals(1, XPath.steps(name, xpath).size());
		XPath.Step step = (XPath.Step) XPath.steps(name, xpath).get(0);
		assertEquals(".", step.getName());

		expected = Collections.singletonList(new XPath.Step(".", 1));
		xpath = "wq_plus";
		assertEquals(expected, XPath.steps(name, xpath));

		expected = Collections.singletonList(new XPath.Step(".", 1));
		xpath = "/wq_plus";
		assertEquals(expected, XPath.steps(name, xpath));

		expected = Collections.singletonList(new XPath.Step(".", 1));
		xpath = "wq_plus/measurement/result/../../measurement/determinand_description/../..";
		assertEquals(expected, XPath.steps(name, xpath));

		expected = Arrays
				.asList(new XPath.Step[] { new XPath.Step(".", 1),
						new XPath.Step("measurement", 2),
						new XPath.Step("result", 1) });

		xpath = "wq_plus/measurement/result/../../measurement[2]/result";
		assertEquals(expected, XPath.steps(name, xpath));

		expected = Arrays
				.asList(new XPath.Step[] { new XPath.Step(".", 1),
						new XPath.Step("measurement", 1),
						new XPath.Step("result", 1) });
		xpath = "wq_plus/measurement/result/../result/.";
		assertEquals(expected, XPath.steps(name, xpath));

		expected = Arrays.asList(new XPath.Step[] { new XPath.Step(".", 1),
				new XPath.Step("measurement", 5) });
		xpath = "measurement/result/../../measurement[5]";
		assertEquals(expected, XPath.steps(name, xpath));
	}

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
//	private static SimpleFeature createSimpleFeature() throws Exception {
//		final TypeFactory tf = new TypeFactoryImpl();
//		final AttributeFactory af = new AttributeFactoryImpl();
//
//		List/* <AttributeType> */types = new ArrayList/* <AttributeType> */();
//		types.add(tf.createType("oid", String.class));
//		types.add(tf.createType("name", String.class));
//		types.add(tf.createType("number", Integer.class));
//		types.add(tf.createType("the_geom", Point.class));
//
//		SimpleFeatureType type = tf.createFeatureType("testType", types,
//				(GeometryType) types.get(3));
//
//		SimpleFeature f = af.createSimpleFeature(new NodeImpl(type), null);
//		f.set("oid", "testOid");
//		f.set("name", "testName");
//		f.set("number", new Integer(1));
//		f.set("the_geom", new GeometryFactory().createPoint(new Coordinate(1,
//				-1)));
//		return f;
//	}

}
