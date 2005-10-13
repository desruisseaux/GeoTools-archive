package org.geotools.feature.schema;

import org.geotools.feature.type.ComplexTestData;
import org.geotools.feature.type.TypeFactoryImpl;
import org.opengis.feature.schema.DescriptorFactory;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.TypeFactory;

import junit.framework.TestCase;

public class NodeImplTest extends TestCase {

	private TypeFactory typeFactory = new TypeFactoryImpl();
	private DescriptorFactory descFactory = new DescriptorFactoryImpl();
	
	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/*
	 * Test method for 'org.geotools.feature.schema.NodeImpl.equals(Object)'
	 */
	public void testEquals() {
		AttributeType type1 = ComplexTestData.createGmlLocation(typeFactory, descFactory);
		AttributeType type2 = ComplexTestData.createExample01MultiValuesComplexProperty(typeFactory, descFactory);

		NodeImpl node1 = new NodeImpl(type1, 1, 1);
		NodeImpl node2 = new NodeImpl(type1, 1, 1);
		NodeImpl node3 = new NodeImpl(type1, 0, 1);
		NodeImpl node4 = new NodeImpl(type2, 0, 1);
		
		assertFalse(node1.equals(null));
		assertEquals(node1, node2);
		assertEquals(node2, node1);
		
		assertFalse(node2.equals(node3));
		assertFalse(node3.equals(node2));
		
		assertFalse(node1.equals(node4));
	}

	/*
	 */
	public void testConstruct() {
		try{
			new NodeImpl(null);
			fail("allowed null type");
		}catch(NullPointerException e){
			//OK
		}
		AttributeType type = ComplexTestData.createExample01MultiValuesComplexProperty(typeFactory, descFactory);
		NodeImpl node = new NodeImpl(type);
		assertEquals(type, node.getType());
		assertEquals(1, node.getMinOccurs());
		assertEquals(1, node.getMaxOccurs());
		
		node  = new NodeImpl(type, 5, 100);
		assertEquals(5, node.getMinOccurs());
		assertEquals(100, node.getMaxOccurs());
	}
	/*
	 * Test method for 'org.geotools.feature.schema.NodeImpl.validate(List<Attribute>)'
	 */
	public void testValidate() {
		NodeImpl node = new NodeImpl(ComplexTestData.createGmlLocation(typeFactory, descFactory));
		node.validate(null);
	}

}
