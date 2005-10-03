package org.geotools.feature.schema;

import javax.xml.namespace.QName;

import org.geotools.feature.type.AttributeTypeImpl;
import org.opengis.feature.schema.AttributeDescriptor;
import org.opengis.feature.schema.DescriptorFactory;
import org.opengis.feature.type.AttributeType;

import junit.framework.TestCase;

public class DescriptorFactoryImplTest extends TestCase {

	DescriptorFactory factory;
	
	protected void setUp() throws Exception {
		super.setUp();
		factory = new DescriptorFactoryImpl();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		factory = null;
	}

	/*
	 * Test method for 'org.geotools.feature.schema.DescriptorFactoryImpl.node(AttributeType, int, int)'
	 */
	public void testNode() {
		AttributeType type = null;
		final int min = 0;
		final int max = Integer.MAX_VALUE;
		AttributeDescriptor descriptor; 
		
		try {
			descriptor = factory.node(type, min, max);
			fail("NullPointerException expected");
		} catch (NullPointerException e) {
			// OK
		}
		
		type = new AttributeTypeImpl(new QName("integer"), Integer.class);
		descriptor = factory.node(type, min, max);
		
		assertEquals(min, descriptor.getMinOccurs());
		assertEquals(max, descriptor.getMaxOccurs());
		assertEquals(type, descriptor.getType());
	}

	/*
	 * Test method for 'org.geotools.feature.schema.DescriptorFactoryImpl.all(Collection<Descriptor>, int, int)'
	 */
	public void testAll() {

	}

	/*
	 * Test method for 'org.geotools.feature.schema.DescriptorFactoryImpl.ordered(List<Descriptor>, int, int)'
	 */
	public void testOrdered() {

	}

	/*
	 * Test method for 'org.geotools.feature.schema.DescriptorFactoryImpl.choice(Collection<Descriptor>, int, int)'
	 */
	public void testChoice() {

	}

}
