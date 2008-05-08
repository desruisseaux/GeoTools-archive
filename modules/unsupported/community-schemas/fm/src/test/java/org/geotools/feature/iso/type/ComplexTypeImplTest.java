package org.geotools.feature.iso.type;

import java.util.ArrayList;
import java.util.Collections;

import junit.framework.TestCase;

import org.geotools.feature.iso.Types;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;

public class ComplexTypeImplTest extends TestCase {

	ComplexType s,t;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		//create a type
		s = new ComplexTypeImpl(
			Types.typeName("superType"), Collections.EMPTY_LIST, false, true,
			Collections.EMPTY_SET, null, null
		);
		
		AttributeType attType1 = new AttributeTypeImpl(
			Types.typeName("attType1"), String.class, false, false, 
			Collections.EMPTY_SET, null, null
		);
		AttributeType attType2 = new AttributeTypeImpl(
			Types.typeName("attType2"), Integer.class, false, false, 
			Collections.EMPTY_SET, null, null
		);
		ArrayList schema = new ArrayList();
		schema.add(
			new AttributeDescriptorImpl(
				attType1, Types.attributeName("att1"), 1, 1, true
			)
		);
		schema.add(
			new AttributeDescriptorImpl(
				attType2, Types.attributeName("att2"), 1, 1, true
			)		
		);
		
		t = new ComplexTypeImpl(
			Types.typeName("complexType"), schema, false, false,
			Collections.EMPTY_SET, s, null
		);
		
		
	}

	/*
	 * Test method for 'org.geotools.feature.type.ComplexTypeImpl.getSuper()'
	 */
	public void testGetSuper() {
		assertNotNull(t.getSuper());
		assertEquals(s,t.getSuper());
	}
		
	/*
	 * Test method for 'org.geotools.feature.type.ComplexTypeImpl.types()'
	 */
	public void testTypes() {
		assertNotNull(Types.descriptor(t,"att1"));
		assertNotNull(Types.descriptor(t,"att2"));
	}


}
