package org.geotools.feature.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.geotools.feature.schema.NodeImpl;
import org.geotools.feature.schema.OrderedImpl;
import org.geotools.filter.Filter;
import org.opengis.feature.schema.Descriptor;
import org.opengis.feature.schema.OrderedDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.TypeFactory;

public class TypeFactoryImplTest extends ComplexTestData {

	TypeFactory factory;
	
	protected void setUp() throws Exception {
		super.setUp();
		factory = new TypeFactoryImpl();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		factory = null;
	}

	/**
	 * Tests factory method createType(String, Class).
	 * <p>
	 * Assertions on method preconditions:
	 * <ul>
	 *  <li>no argument is null
	 * </ul>
	 * </p>
	 * <p>
	 * Assertions on the created AttributeType:
	 * <ul>
	 *  <li>it is not null
	 *  <li><code>name()</code> is correct
	 *  <li><code>getName()</code> is correct
	 *  <li><code>getBinding()</code> is correct
	 *  <li><code>getRestrictions()</code> is an empty <code>java.util.Set</code>
	 *  <li><code>isIdentified()</code> returns false
	 *  <li><code>isAbstract()</code> returns false
	 *  <li><code>getSuper()</code> returns null
	 *  <li><code>isNillable()</code> returns null
	 * </ul>
	 * </p>
	 */
	public void testCreateSimpleTypeStringClass() {
		AttributeType type;
		try{
			type = factory.createType((String)null, String.class);
			fail("null name should raise an exception");
		}catch (NullPointerException npe){
			//OK
		}

		try{
			type = factory.createType("", String.class);
			fail("empty name should raise an exception");
		}catch (IllegalArgumentException iae){
			//OK
		}

		try{
			type = factory.createType("\tname", String.class);
			fail("name with forward space should raise an exception");
		}catch (IllegalArgumentException iae){
			//OK
		}

		try{
			type = factory.createType("TestString", (Class)null);
			fail("null binding should raise an exception");
		}catch (NullPointerException npe){
			//OK
		}
		
		Set<Filter> filters = Collections.emptySet();
		type = factory.createType("TestString", String.class);
		checkType(type, new QName("TestString"), String.class, filters, false, false, null, true);
	}

	
	/**
	 * Tests factory method createAttributeType(QName, Class)
	 */
	public void testCreateSimpleTypeQNameClass() {
		QName qname;
		AttributeType type;
		 
		qname = new QName("localPart");
		type = factory.createType(qname, String.class);
		assertEquals(qname, type.getName());
		
		qname = new QName("http://www.opengis.net/gml", "name");
		type = factory.createType(qname, String.class);
		assertEquals(qname, type.getName());

		Set<Filter> filters = Collections.emptySet();
		checkType(type, qname, String.class, filters, false, false, null, true);
	}

	/**
	 * Tests method createAttributeType(QName, Class, boolean, boolean, Set<Filter>)
	 */
	public void testCreateSimpleTypeQNameClassBooleanBooleanSetOfFilter() {
		QName name = new QName("http://www.opengis.net/gml", "name");
		Class binding = Integer.class;
		boolean identified = true;
		boolean nillable = true;
		Set<Filter> restrictions = new HashSet<Filter>();
		restrictions.add(Filter.ALL);

		AttributeType type = factory.createType(name, binding, identified, nillable, restrictions);
		checkType(type, name, binding, restrictions, identified, false, null, nillable);
	}
	

	/*
	 * Test method for 'org.geotools.feature.type.AttributeTypeFactoryImpl.createAttributeType(QName, Class, boolean, boolean, Set<Filter>, AttributeType, boolean)'
	 */
	public void testCreateSimpleTypeQNameClassBooleanBooleanSetOfFilterAttributeTypeBoolean() {
		QName name = new QName("http://www.opengis.net/gml", "name");
		Class binding = Integer.class;
		boolean identified = true;
		boolean nillable = true;
		Set<Filter> restrictions = new HashSet<Filter>();
		restrictions.add(Filter.ALL);

		AttributeType type = factory.createType(name, binding, identified, nillable, restrictions);
		checkType(type, name, binding, restrictions, identified, false, null, nillable);
	}
	
	public void testCreateComplexType(){
		final Descriptor schema;
		final List<Descriptor> contentSchema = new ArrayList<Descriptor>();
		
		//intentionally avoidng the use of DescriptorFactory here
		AttributeType simple1 = new AttributeTypeImpl("name", String.class);
		Descriptor node1 = new NodeImpl(simple1, 0, 1);
		
		contentSchema.add(node1);
		
		AttributeType simple2 = new AttributeTypeImpl("quantity", Integer.class);
		Descriptor node2 = new NodeImpl(simple2);

		contentSchema.add(node2);
		
		schema = new OrderedImpl(contentSchema, 0, Integer.MAX_VALUE /*unbounded?*/); 
		
		ComplexType type = factory.createType("complex", schema);
		
		final Set<Filter> filters = Collections.emptySet();
		
		checkType(type, new QName("complex"), null, filters, false, false, null, false);
		//assertEquals(schema, type.getDescriptor());
		
		assertTrue(type.getDescriptor() instanceof OrderedDescriptor);
		List<Descriptor> sequence = ((OrderedDescriptor)type.getDescriptor()).sequence();
		assertEquals(node1, sequence.get(0));
		assertEquals(node2, sequence.get(1));
	}
	
}
