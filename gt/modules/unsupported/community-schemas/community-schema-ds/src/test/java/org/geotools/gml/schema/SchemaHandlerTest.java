package org.geotools.gml.schema;

import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import junit.framework.TestCase;

import org.geotools.feature.type.GMLTypes;
import org.geotools.feature.type.SimpleTypes;
import org.geotools.util.AttributeName;
import org.opengis.feature.schema.AttributeDescriptor;
import org.opengis.feature.schema.OrderedDescriptor;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.TypeFactory;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;


public class SchemaHandlerTest extends TestCase {

	private static final Attributes NULL_ATTS = new AttributesImpl();

	/**
	 * Namespace URI of parsed types
	 */
	private static final String NS_URI = "http://online.socialchange.net.au";

	SchemaHandler handler;
	SAXParser saxParser;
	
	protected void setUp() throws Exception {
		super.setUp();
		handler = new SchemaHandler();
		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setNamespaceAware(true);
		saxParser = spf.newSAXParser();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		handler = null;
		saxParser = null;
	}

	public void testSimpleFeatureType() throws Exception {
		String res = "simpleFeature.xsd";
		InputStream in = getClass().getResourceAsStream(res);
		saxParser.parse(in, this.handler);

		TypeFactory parsedTypes = handler.getTypeFactory();
		assertNotNull(parsedTypes);
		
		AttributeType type = parsedTypes.getType(new AttributeName(NS_URI, "simpleFeatureType"));
		assertNotNull(type);
		assertTrue(type.getClass().getName(), type instanceof SimpleFeatureType);
		SimpleFeatureType ft = (SimpleFeatureType) type;
		String local = ft.getName().getLocalPart();
		String uri = ft.getName().getNamespaceURI();
		assertEquals("simpleFeatureType", local);
		assertEquals(NS_URI, uri);

		List/*<AttributeType>*/ attributes = ft.types();
		assertEquals(3, attributes.size());

		AttributeType att = (AttributeType) attributes.get(0);
		assertTrue(att instanceof GeometryType);
		assertEquals(new AttributeName(GMLTypes.GML_NSURI, "GeometryPropertyType"), att.getName());
		assertEquals(Geometry.class, att.getBinding());

		att = (AttributeType) attributes.get(1);
		assertEquals(new AttributeName(SimpleTypes.NSURI, "string"), att.getName());
		assertEquals(String.class, att.getBinding());
		assertFalse(att.isNillable().booleanValue());

		att = (AttributeType) attributes.get(2);
		assertEquals(new AttributeName(SimpleTypes.NSURI, "int"), att.getName());
		assertEquals(Integer.class, att.getBinding());
		assertTrue(att.isNillable().booleanValue());
	}

	public void testComplexFeatureType() throws Exception {
		String res = "complexFeature.xsd";
		InputStream in = getClass().getResourceAsStream(res);
		saxParser.parse(in, this.handler);

		TypeFactory parsedTypes = handler.getTypeFactory();
		assertNotNull(parsedTypes);
		
		AttributeType type = parsedTypes.getType(new AttributeName(NS_URI, "wq_plus_Type"));
		assertNotNull(parsedTypes);
		
		AttributeDescriptor node = parsedTypes.getDescriptor(new AttributeName(NS_URI, "wq_plus"));
		assertNotNull(node);

		assertTrue(type instanceof FeatureType);
		FeatureType wqPlus = (FeatureType)type;

		AttributeType nodeType = node.getType();
		
		//@REVISIT: parser creates a subtype just because nullability differs
		//assertEquals(wqPlus, nodeType);		
		
		assertTrue(wqPlus.isIdentified());
		assertFalse(wqPlus.isAbstract());
		
		assertTrue(wqPlus.getDescriptor() instanceof OrderedDescriptor);
		OrderedDescriptor schema = (OrderedDescriptor) wqPlus.getDescriptor();
		List/*<? extends Descriptor>*/ sequence = schema.sequence();
		assertEquals(3, sequence.size());

		AttributeDescriptor measNode = (AttributeDescriptor) sequence.get(0);
		assertEquals(0, measNode.getMinOccurs());
		assertEquals(Integer.MAX_VALUE, measNode.getMaxOccurs());
		
		assertTrue(measNode.getType() instanceof ComplexType);
		ComplexType measurement = (ComplexType) measNode.getType();

		org.opengis.feature.AttributeName typeName = SimpleTypes.ANONYMOUS; //anonymous complex type
		assertEquals(typeName, measurement.getName());
		assertTrue(measurement.isIdentified());
		assertTrue(measurement.getDescriptor() instanceof OrderedDescriptor);
		assertEquals(2, ((OrderedDescriptor) measurement.getDescriptor())
				.sequence().size());
		OrderedDescriptor md = (OrderedDescriptor) measurement.getDescriptor();
		AttributeDescriptor att1 = (AttributeDescriptor) md.sequence().get(0);
		AttributeDescriptor att2 = (AttributeDescriptor) md.sequence().get(1);
		assertEquals("string", att1.getType().getName().getLocalPart());
		assertEquals("string", att2.getType().getName().getLocalPart());
		
		
		AttributeDescriptor the_geom = (AttributeDescriptor)sequence.get(1);
		assertEquals(1, the_geom.getMinOccurs());
		assertEquals(1, the_geom.getMaxOccurs());
		
		AttributeDescriptor siteName = (AttributeDescriptor)sequence.get(2);
		assertEquals(1, siteName.getMinOccurs());
		assertEquals(Integer.MAX_VALUE, siteName.getMaxOccurs());
		
		assertEquals(new AttributeName(GMLTypes.GML_NSURI, "PointPropertyType"), the_geom.getType().getName());
		assertEquals(new AttributeName(SimpleTypes.NSURI, "string"), siteName.getType().getName());
		
		assertTrue(the_geom.getType() instanceof GeometryType);
		assertEquals(Point.class, the_geom.getType().getBinding());
	}
}
