package org.geotools.feature;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.geotools.feature.type.FeatureCollectionTypeImpl;
import org.geotools.feature.type.TypeFactoryImpl;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureCollectionType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.TypeFactory;

import junit.framework.TestCase;

public class TypesTest extends TestCase {

	TypeFactory typeFactory = new TypeFactoryImpl();
	
	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * 
	 * Test method for 'org.geotools.feature.Types.memberTypes(FeatureCollectionType)'
	 */
	public void testMemberTypesFeatureCollectionType() {
		FeatureCollectionType collType;
		Set<FeatureType>members;
		Set<FeatureType> expected = new HashSet<FeatureType>();
		expected.add(TypeFactoryImpl.getGmlAbstractFeatureType());
		
		collType = typeFactory.createFeatureCollectionType();
		
		members = Types.memberTypes(collType);
		assertNotNull(members);
		assertFalse(members.isEmpty());
		assertTrue(members.containsAll(expected));
		
		expected.clear();
		FeatureType ft1 = typeFactory.createFeatureType("ft1", new ArrayList<AttributeType>(), null);
		FeatureType ft2 = typeFactory.createFeatureType("ft2", new ArrayList<AttributeType>(), null);
		expected.add(ft1);
		expected.add(ft2);
		
		collType = typeFactory.createFeatureCollectionType(null, expected, null, null, null, null, false);

		members = Types.memberTypes(collType);
		assertNotNull(members);
		assertFalse(members.isEmpty());
		assertFalse(members.contains(TypeFactoryImpl.getGmlAbstractFeatureType()));
		assertEquals(2, members.size());
		assertTrue(members.containsAll(expected));
	}

	/*
	 * Test method for 'org.geotools.feature.Types.schema(ComplexType)'
	 */
	public void testSchema() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/*
	 * Test method for 'org.geotools.feature.Types.ancestors(AttributeType)'
	 */
	public void testAncestors() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/*
	 * Test method for 'org.geotools.feature.Types.isDescendedFrom(AttributeType, QName)'
	 */
	public void testIsDescendedFromAttributeTypeQName() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/*
	 * Test method for 'org.geotools.feature.Types.isDescendedFrom(AttributeType, AttributeType)'
	 */
	public void testIsDescendedFromAttributeTypeAttributeType() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

}
