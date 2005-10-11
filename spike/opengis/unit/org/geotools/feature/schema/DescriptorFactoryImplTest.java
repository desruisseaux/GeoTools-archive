package org.geotools.feature.schema;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.geotools.feature.type.AttributeTypeImpl;
import org.opengis.feature.schema.AllDescriptor;
import org.opengis.feature.schema.AttributeDescriptor;
import org.opengis.feature.schema.ChoiceDescriptor;
import org.opengis.feature.schema.Descriptor;
import org.opengis.feature.schema.DescriptorFactory;
import org.opengis.feature.schema.OrderedDescriptor;
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
	
	public void testNodeOccurs(){
		AttributeType type = new AttributeTypeImpl(new QName("integer"), Integer.class);
		AttributeDescriptor descriptor; 

		try{
			descriptor = factory.node(type, -1, 1);
			fail("Expected IllegalArgumentException, min -1");
		}catch(IllegalArgumentException e){}
		
		try{
			descriptor = factory.node(type, 2, 1);
			fail("Expected IllegalArgumentException, max < min");
		}catch(IllegalArgumentException e){}

		
		descriptor = factory.node(type, 1, 1);
		assertEquals(1, descriptor.getMinOccurs());
		assertEquals(1, descriptor.getMaxOccurs());
		assertEquals(type, descriptor.getType());
	}

	public void testChoiceOccurs(){
		AttributeType type = new AttributeTypeImpl(new QName("integer"), Integer.class);
		Set<Descriptor>types = new HashSet<Descriptor>();
		types.add(factory.node(type, 1, 1));
		ChoiceDescriptor descriptor; 

		try{
			descriptor = factory.choice(types, -1, 1);
			fail("Expected IllegalArgumentException, min -1");
		}catch(IllegalArgumentException e){}
		
		try{
			descriptor = factory.choice(types, 2, 1);
			fail("Expected IllegalArgumentException, max < min");
		}catch(IllegalArgumentException e){}

		
		descriptor = factory.choice(types, 1, 1);
		assertEquals(1, descriptor.getMinOccurs());
		assertEquals(1, descriptor.getMaxOccurs());
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
		AttributeDescriptor s1 = new NodeImpl(new AttributeTypeImpl("s1", String.class));
		attributes.add(int1);
		attributes.add(int2);
		attributes.add(s1);
		AllDescriptor all = factory.all(attributes, 1, 1);
		
	  	Set<AttributeDescriptor>set = all.all();
	  	assertNotSame(attributes, set);
	  	assertEquals(attributes, set);
	}

	/*
	 * Test method for 'org.geotools.feature.schema.DescriptorFactoryImpl.ordered(List<Descriptor>, int, int)'
	 */
	public void testOrdered() {
		try{
			factory.ordered(null, 0, 1);
			fail("NullPointerException expected");
		}catch(NullPointerException e){
			//OK
		}
		List<Descriptor> attributes = new LinkedList<Descriptor>();
		

		AttributeDescriptor int1 = new NodeImpl(new AttributeTypeImpl("int2", Integer.class));
		AttributeDescriptor int2 = new NodeImpl(new AttributeTypeImpl("int1", Integer.class));
		
		Set<AttributeDescriptor>allAtts = new HashSet<AttributeDescriptor>();
		allAtts.add(int1);
		allAtts.add(int2);
		AllDescriptor all = factory.all(allAtts, 0, Integer.MAX_VALUE);
		AttributeDescriptor s1 = new NodeImpl(new AttributeTypeImpl("s1", String.class));
		
		attributes.add(int1);
		attributes.add(int2);
		attributes.add(all);
		attributes.add(s1);
		
		OrderedDescriptor ordered = factory.ordered(attributes, 1, 1);
		
	  	List<? extends Descriptor>created = ordered.sequence();
	  	assertNotSame(attributes, created);
	  	assertTrue(created.containsAll(attributes));
	  	assertEquals(attributes, created);
	}

	/*
	 * Test method for 'org.geotools.feature.schema.DescriptorFactoryImpl.choice(Collection<Descriptor>, int, int)'
	 */
	public void testChoice() {
		throw new UnsupportedOperationException("not yet implemented");
	}
	
	public void testExtends(){
		throw new UnsupportedOperationException("not yet implemented");
	}
	
	public void testRestricts(){
		throw new UnsupportedOperationException("not yet implemented");
	}

}
