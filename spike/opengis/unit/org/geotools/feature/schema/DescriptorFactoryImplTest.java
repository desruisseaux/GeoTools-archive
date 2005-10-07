package org.geotools.feature.schema;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.geotools.feature.type.AttributeTypeImpl;
import org.opengis.feature.schema.AllDescriptor;
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
		try{
			factory.all(null, 0, 1);
			fail("NullPointerException expected");
		}catch(NullPointerException e){
			//OK
		}
		Set<AttributeDescriptor> attributes = new HashSet<AttributeDescriptor>();
		

		AttributeDescriptor int1 = new NodeImpl(new AttributeTypeImpl("int2", Integer.class));
		AttributeDescriptor int2 = new NodeImpl(new AttributeTypeImpl("int1", Integer.class));
		AttributeDescriptor s2 = new NodeImpl(new AttributeTypeImpl("s1", String.class));
		AllDescriptor all = factory.all(attributes, 1, 1);
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
	
	public void testExtends(){
		
	}
	
	public void testRestricts(){
	}

}
