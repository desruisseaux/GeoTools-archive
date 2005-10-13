package org.geotools.feature.impl;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.geotools.feature.Descriptors;
import org.geotools.feature.Types;
import org.geotools.feature.schema.DescriptorFactoryImpl;
import org.geotools.feature.type.ComplexTestData;
import org.geotools.feature.type.TypeFactoryImpl;
import org.opengis.feature.Attribute;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.schema.Descriptor;
import org.opengis.feature.schema.DescriptorFactory;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.TypeFactory;

public class ComplexAttributeImplTest extends TestCase {

	private static TypeFactory typeFactory  = new TypeFactoryImpl();
	private static DescriptorFactory descFactory = new DescriptorFactoryImpl();
	private static AttributeFactoryImpl attFactory = new AttributeFactoryImpl();
	
	ComplexType<?> measurementType = ComplexTestData.createMeasurementType(typeFactory, descFactory);

	List<Attribute>testContents;
	
	//types of measurement attributes
	AttributeType determinantType, resultType;
	
	protected void setUp() throws Exception {
		super.setUp();
		measurementType = ComplexTestData.createMeasurementType(typeFactory, descFactory);
		
		//determinand_description(String), result(String)
		List<AttributeType> contentTypes = Descriptors.types(measurementType.getDescriptor());
		
		testContents = new ArrayList<Attribute>();
		testContents.add(attFactory.create(contentTypes.get(0), null, "Sample determinant"));
		testContents.add(attFactory.create(contentTypes.get(1), null, "Sample result"));
		
		List<AttributeType>attTypes = Descriptors.types(measurementType.getDescriptor());
		determinantType = attTypes.get(0);
		resultType = attTypes.get(1);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		measurementType = null;
	}

	/**
	 * 
	 */
	public void testConstruct() {
		ComplexAttributeImpl att = new ComplexAttributeImpl(measurementType);
		
		assertNotNull(att.attribtues);
		assertEquals(0, att.attribtues.size());
		assertNull(att.ID);
		assertEquals(measurementType, att.TYPE);
		
		att = new ComplexAttributeImpl("testId", measurementType);
		assertEquals("testId", att.ID);
	}

	/**
	 * 
	 */
	public void testGetType() {
		ComplexAttributeImpl att = new ComplexAttributeImpl(measurementType);
		assertSame(measurementType, att.getType());
	}

	/**
	 * Test method for 'org.geotools.feature.impl.ComplexAttributeImpl.getID()'
	 */
	public void testGetID() {
		ComplexAttribute instance = attFactory.create(measurementType, null);
		assertNotNull(instance.getID());
		
		measurementType = ComplexTestData.createMeasurementType(typeFactory, descFactory);
		instance = attFactory.create(measurementType, null);
		assertNotNull(instance.getID());

		instance = attFactory.create(measurementType, "test-id");
		assertEquals("test-id", instance.getID());
	}


	/**
	 * tests ComplexAttributeImple.types():List<AttributeType> is actually
	 * a types view of the current contents of a complex
	 */
	public void testTypes() {
		ComplexAttributeImpl att = new ComplexAttributeImpl(null, measurementType);
		List<AttributeType>typesView = att.types();
		assertNotNull(typesView);
		assertEquals(0, typesView.size());
		
		att.set(testContents);
		//content was modified, view should remain unmodified
		//since its meant to have been returned like a safe copy
		assertEquals(0, typesView.size());

		typesView = att.types();
		assertNotNull(typesView);
		assertEquals(2, typesView.size());
		
		assertEquals(determinantType, typesView.get(0));
		assertEquals(resultType, typesView.get(1));
	
	
		//create type with attributes with multipl. different than 1:1
		List<Descriptor> atts = new ArrayList<Descriptor>();
		atts.add(descFactory.node(determinantType, 0, Integer.MAX_VALUE));
		atts.add(descFactory.node(resultType, 1, Integer.MAX_VALUE));
		Descriptor descriptor = descFactory.ordered(atts, 1, 1);
		
		
		//now it can contain 0:* determinants followed by 1:* results.
		//not pretty, but useful for this test pourposes
		measurementType = typeFactory.createType("test", descriptor);
		att = new ComplexAttributeImpl(null, measurementType);
		
		testContents.clear();
		testContents.add(attFactory.create(determinantType, null, "det1"));
		testContents.add(attFactory.create(determinantType, null, "det2"));
		testContents.add(attFactory.create(determinantType, null, "det3"));
		testContents.add(attFactory.create(resultType, null, "1"));
		
		att.set(testContents);
		typesView = att.types();
		assertEquals(4, typesView.size());
		assertEquals(determinantType, typesView.get(0));
		assertEquals(determinantType, typesView.get(1));
		assertEquals(determinantType, typesView.get(2));
		assertEquals(resultType, typesView.get(3));
	}

	/**
	 * Test method for 'org.geotools.feature.impl.ComplexAttributeImpl.values()'
	 */
	public void testValues() {
		ComplexAttributeImpl att = new ComplexAttributeImpl(null, measurementType);
		List<Object>valuesView = att.values();
		assertNotNull(valuesView);
		assertEquals(0, valuesView.size());
		
		att.set(testContents);
		
		//view over defensive copy not affected
		assertEquals(0, valuesView.size());
		
		valuesView = att.values();
		assertNotNull(valuesView);
		assertEquals(2, valuesView.size());
		
		assertEquals("Sample determinant", valuesView.get(0));
		assertEquals("Sample result", valuesView.get(1));
	
	
		//create type with attributes with multipl. different than 1:1
		List<Descriptor> atts = new ArrayList<Descriptor>();
		atts.add(descFactory.node(determinantType, 0, Integer.MAX_VALUE));
		atts.add(descFactory.node(resultType, 1, Integer.MAX_VALUE));
		Descriptor descriptor = descFactory.ordered(atts, 1, 1);
		//now it can contain 0:* determinants followed by 1:* results.
		//not pretty, but useful for this test pourposes
		measurementType = typeFactory.createType("test", descriptor);
		
		att = new ComplexAttributeImpl(null, measurementType);
		
		testContents.clear();
		testContents.add(attFactory.create(determinantType, null, "det1"));
		testContents.add(attFactory.create(determinantType, null, "det2"));
		testContents.add(attFactory.create(determinantType, null, "det3"));
		testContents.add(attFactory.create(resultType, null, "1"));
		
		att.set(testContents);
		valuesView = att.values();
		assertEquals(4, valuesView.size());
		assertEquals("det1", valuesView.get(0));
		assertEquals("det2", valuesView.get(1));
		assertEquals("det3", valuesView.get(2));
		assertEquals("1", valuesView.get(3));
	}

	/**
	 * Test method for 'org.geotools.feature.impl.ComplexAttributeImpl.get()'
	 */
	public void testGet() {
		ComplexAttributeImpl att = new ComplexAttributeImpl(null, measurementType);
		assertNotNull(att.getAttributes());
		assertEquals(0, att.getAttributes().size());
		
		att.set(testContents);
		
		assertNotNull(att.getAttributes());
		assertNotSame(testContents, att.getAttributes());
		assertEquals(testContents, att.getAttributes());
	}

	/**
	 * Test method for 'org.geotools.feature.impl.ComplexAttributeImpl.set(Object)'
	 */
	public void testSet() {
		ComplexAttributeImpl att = new ComplexAttributeImpl(null, measurementType);
		att.set(testContents);
		
		assertNotNull(att.attribtues);
		assertNotSame(testContents, att.attribtues);
		assertEquals(testContents, att.attribtues);
	}

	public void testSetValidatesTypes() {
		ComplexAttributeImpl att = new ComplexAttributeImpl(null, measurementType);
		AttributeType goodType = Descriptors.types(measurementType.getDescriptor()).get(0);
		
		//same name, different binding
		AttributeType badType = typeFactory.createType(goodType.getName(), Integer.class);
		
		testContents.set(0, attFactory.create(badType, null, new Integer(1)));
		
		try{
			att.set(testContents);
			fail("attribute of wrong type was not validated");
		}catch(IllegalArgumentException e){
			//OK
		}
	}


	public void testSetValidatesStructure() {
		ComplexAttributeImpl att = new ComplexAttributeImpl(null, measurementType);

		//swap attribute order, violates Ordered descriptor
		Attribute buff = testContents.get(0);
		testContents.set(0, testContents.get(1));
		testContents.set(1, buff);

		try{
			att.set(testContents);
			fail("adherence to descriptor's structure not validated");
		}catch(IllegalArgumentException e){
			//OK
		}
	}

	public void testSetValidatesMultiplicity() {
		ComplexAttributeImpl att = new ComplexAttributeImpl(null, measurementType);

		//add attribute of valid type, but with multiplicity 0:0.
		AttributeType attType = Descriptors.types(att.getType().getDescriptor()).get(0);
		testContents.add(attFactory.create(attType, null, "second determinant"));

		try{
			att.set(testContents);
			fail("attribute multiplicity not validated");
		}catch(IllegalArgumentException e){
			//OK
		}
	}

	/**
	 * Test method for 'org.geotools.feature.impl.ComplexAttributeImpl.get(AttributeType)'
	 */
	public void testGetByAttributeType() {
		ComplexAttributeImpl att = new ComplexAttributeImpl(null, measurementType);
		att.set(testContents);
		
		Attribute att1 = testContents.get(0);
		
		//since multiplicity is 1:1, returns the actual value
		Object content = att.get(att1.getType());
		assertNotNull(content);
		assertEquals(att1.get(), content);
		
		
		//create type with an attribute with multipl. different than 1:1
		List<Descriptor> atts = new ArrayList<Descriptor>();
		atts.add(descFactory.node(determinantType, 0, Integer.MAX_VALUE));
		atts.add(descFactory.node(resultType, 1, Integer.MAX_VALUE));
		Descriptor descriptor = descFactory.ordered(atts, 1, 1);

		//now it can contain 0:* determinants followed by 1:* results.
		//not pretty, but useful for this test pourposes
		measurementType = typeFactory.createType("test", descriptor);
		att = new ComplexAttributeImpl(measurementType);
		
		testContents.clear();
		testContents.add(attFactory.create(determinantType, null, "det1"));
		testContents.add(attFactory.create(determinantType, null, "det2"));
		testContents.add(attFactory.create(determinantType, null, "det3"));
		testContents.add(attFactory.create(resultType, null, "1"));
		
		att = new ComplexAttributeImpl(null, measurementType);
		att.set(testContents);
		//no "result" attributes yet
		Object values = att.get(resultType);
		assertNotNull(values);
		assertTrue(values instanceof List);
		assertEquals(1, ((List)values).size());
		
		values = att.get(determinantType);
		assertEquals(3, ((List)values).size());
		
	}

}
