package org.geotools.feature.impl;

import org.geotools.feature.schema.DescriptorFactoryImpl;
import org.geotools.feature.type.ComplexTestData;
import org.geotools.feature.type.TypeFactoryImpl;
import org.opengis.feature.AttributeFactory;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Feature;
import org.opengis.feature.schema.DescriptorFactory;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.TypeFactory;

import junit.framework.TestCase;

public class ComplexAttributeImplTest extends TestCase {

	private static TypeFactory typeFactory  = new TypeFactoryImpl();
	private static DescriptorFactory descFactory = new DescriptorFactoryImpl();
	private static AttributeFactory attFactory = new AttributeFactoryImpl();
	
	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * 
	 */
	public void testConstruct() {
		throw new UnsupportedOperationException("not yet implemented");
	}

	/**
	 * 
	 */
	public void testGetType() {
		throw new UnsupportedOperationException("not yet implemented");
	}

	/**
	 * Test method for 'org.geotools.feature.impl.ComplexAttributeImpl.getID()'
	 */
	public void testGetID() {
		ComplexType type = ComplexTestData.createExample01Type(typeFactory, descFactory);		
		ComplexAttribute instance = attFactory.create(type, null);
		assertNotNull(instance.getID());
		
		type = ComplexTestData.createMeasurementType(typeFactory, descFactory);
		instance = attFactory.create(type, null);
		assertNotNull(instance.getID());

		instance = attFactory.create(type, "test-id");
		assertEquals("test-id", instance.getID());
	}


	/**
	 */
	public void testTypes() {
		throw new UnsupportedOperationException("not yet implemented");
	}

	/*
	 * Test method for 'org.geotools.feature.impl.ComplexAttributeImpl.createTypesView(List<Attribute>)'
	 */
	public void testCreateTypesView() {
		throw new UnsupportedOperationException("not yet implemented");
	}

	/*
	 * Test method for 'org.geotools.feature.impl.ComplexAttributeImpl.values()'
	 */
	public void testValues() {
		throw new UnsupportedOperationException("not yet implemented");
	}

	/*
	 * Test method for 'org.geotools.feature.impl.ComplexAttributeImpl.createValuesView(List<Attribute>)'
	 */
	public void testCreateValuesView() {
		throw new UnsupportedOperationException("not yet implemented");
	}

	/*
	 * Test method for 'org.geotools.feature.impl.ComplexAttributeImpl.name()'
	 */
	public void testName() {
		throw new UnsupportedOperationException("not yet implemented");
	}

	/*
	 * Test method for 'org.geotools.feature.impl.ComplexAttributeImpl.get()'
	 */
	public void testGet() {
		throw new UnsupportedOperationException("not yet implemented");
	}

	/**
	 * Test method for 'org.geotools.feature.impl.ComplexAttributeImpl.set(Object)'
	 */
	public void testSet() {
		ComplexType type = ComplexTestData.createExample01Type(typeFactory, descFactory);		
		ComplexAttribute instance = attFactory.create(type, null);

		instance.set(new Object());
		
		type = ComplexTestData.createMeasurementType(typeFactory, descFactory);
		instance = attFactory.create(type, null);
		assertNotNull(instance.getID());

		instance = attFactory.create(type, "test-id");
		assertEquals("test-id", instance.getID());
	}

	/**
	 * Test method for 'org.geotools.feature.impl.ComplexAttributeImpl.get(AttributeType)'
	 */
	public void testGetAttributeType() {
		throw new UnsupportedOperationException("not yet implemented");
	}

}
