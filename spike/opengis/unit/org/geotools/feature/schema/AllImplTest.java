package org.geotools.feature.schema;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.geotools.feature.impl.AttributeFactoryImpl;
import org.geotools.feature.type.TypeFactoryImpl;
import org.opengis.feature.Attribute;
import org.opengis.feature.AttributeFactory;
import org.opengis.feature.schema.AttributeDescriptor;
import org.opengis.feature.schema.DescriptorFactory;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.TypeFactory;


public class AllImplTest extends TestCase {

	private TypeFactory typeFactory = new TypeFactoryImpl();
	private DescriptorFactory descFactory = new DescriptorFactoryImpl();

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/*
	 * Test method for 'org.geotools.feature.schema.AllImpl.equals(Object)'
	 */
	public void testEquals() {
		AllImpl all1 = new AllImpl(new HashSet<AttributeDescriptor>());
		assertFalse(all1.equals(null));
		assertFalse(all1.equals(new Object()));
		assertTrue(all1.equals(all1));

		AttributeType type1 = typeFactory.createType("string1", String.class);
		AttributeType type2 = typeFactory.createType("int1", Integer.class);

		NodeImpl node1 = new NodeImpl(type1);
		NodeImpl node2 = new NodeImpl(type2);

		NodeImpl node3 = new NodeImpl(type1);
		NodeImpl node4 = new NodeImpl(type2);

		Set<AttributeDescriptor> content1 = new HashSet<AttributeDescriptor>();
		Set<AttributeDescriptor> content2 = new HashSet<AttributeDescriptor>();
		content1.add(node1);
		content1.add(node2);
		content2.add(node3);

		all1 = new AllImpl(content1);
		AllImpl all2 = new AllImpl(content2);
		assertFalse(all1.equals(all2));

		content2.add(node4);
		all2 = new AllImpl(content2);
		assertTrue(all1.equals(all2));
	}

	/**
	 * No null list of AttributeDescriptors 
	 */
	public void testConstructNullParam() {
		try {
			new AllImpl(null);
			fail("expected NullPointerException");
		} catch (NullPointerException e) {
			// OK
		}
	}
	
	/**
	 * No duplicate names in content, even if they're of different types
	 */
	public void testConstructDuplicateNames(){
		
		AttributeType type1 = typeFactory.createType("duplicateName", String.class);
		AttributeType type2 = typeFactory.createType("duplicateName", Integer.class);
		AttributeDescriptor node1 = descFactory.node(type1, 1, 1);
		AttributeDescriptor node2 = descFactory.node(type2, 1, 1);
		
		Set<AttributeDescriptor>allContents = new HashSet<AttributeDescriptor>();
		allContents.add(node1);
		allContents.add(node2);
		try{
			new AllImpl(allContents, 1, 1);
			fail("AllImpl should not allow duplicate content names");
		}catch(IllegalArgumentException e){
			//OK
		}
	}
	
	/**
	 * No content multiplicity other than 0:0, 0:1, 1:1
	 */
	public void testConstructIllegalContentMultiplicity(){
		AttributeType type1 = typeFactory.createType("string1", String.class);
		AttributeType type2 = typeFactory.createType("int1", Integer.class);

		NodeImpl nodeOk = new NodeImpl(type1, 0, 0);
		NodeImpl badNode = new NodeImpl(type2, 0, 2);

		Set<AttributeDescriptor> contents = new HashSet<AttributeDescriptor>();
		contents.add(nodeOk);
		contents.add(badNode);
		
		try{
			new AllImpl(contents, 0, 1);
			fail("Allowed content with illegal multiplicity");
		}catch(IllegalArgumentException e){
			//OK
		}
	}
	
	/**
	 * 
	 *
	 */
	public void testConstruct(){
		AttributeType type1 = typeFactory.createType("string", String.class);
		AttributeType type2 = typeFactory.createType("int", Integer.class);

		AttributeDescriptor node1 = descFactory.node(type1, 0, 0);
		AttributeDescriptor node2 = descFactory.node(type2, 0, 0);
		
		Set<AttributeDescriptor>allContents = new HashSet<AttributeDescriptor>();
		allContents.add(node1);
		allContents.add(node2);
		
		AllImpl all;

		all = new AllImpl(allContents, 1, 1);
		assertNotSame(allContents, all.all());
		assertEquals(allContents, all.all());

		node1 = descFactory.node(type1, 0, 1);
		node2 = descFactory.node(type2, 1, 1);
		
		allContents = new HashSet<AttributeDescriptor>();
		allContents.add(node1);
		allContents.add(node2);
		
		all = new AllImpl(allContents, 0, Integer.MAX_VALUE);
		assertNotSame(allContents, all.all());
		assertEquals(allContents, all.all());
	}

	/**
	 * null not valid as argument of validate()
	 */
	public void testValidateNull() {
		AllImpl all = createTestDescriptor();
		try{
			all.validate(null);			
		}catch(NullPointerException e){
			//OK
			return;
		}
		fail("expected NullPointerException");
	}
	
	/**
	 * No null member of validate parameter list
	 */
	public void testValidateNullInAttributes() {
		AllImpl all = createTestDescriptor();
		List<Attribute> illegalContents = new ArrayList<Attribute>();
		illegalContents.add(null);
		try{
			all.validate(illegalContents);			
		}catch(NullPointerException e){
			//OK
			return;
		}
		fail("expected NullPointerException");
	}

	/**
	 * No members of wrong type in validate
	 */
	public void testValidateIllegalContentType() {
		AllImpl all = createTestDescriptor();
		List<Attribute> illegalContents = new ArrayList<Attribute>();
		
		AttributeType badType = typeFactory.createType(new QName("http://anotherns.net", "zeroone"), String.class);
		Attribute badAttribute = new AttributeFactoryImpl().create(badType, null, "value");
		illegalContents.add(badAttribute);
		
		AttributeType goodType = typeFactory.createType("zeroone", String.class);
		Attribute goodAttribute = new AttributeFactoryImpl().create(goodType, null, "value");
		illegalContents.add(goodAttribute);
		
		try{
			all.validate(illegalContents);			
		}catch(IllegalArgumentException e){
			//OK
			return;
		}
		fail("expected IllegalArgumentException");
	}

	/**
	 * validates contents multiplicity restrictions
	 */
	public void testValidateMultiplicityRangeCheck() {
		AllImpl all = createTestDescriptor();
		AttributeType zerozeroType = typeFactory.createType("zerozero", String.class);
		AttributeType zerooneType = typeFactory.createType("zeroone", String.class);
		AttributeType oneoneType = typeFactory.createType("oneone", String.class);
		AttributeFactory attf = new AttributeFactoryImpl();
		
		List<Attribute> illegalContents = new ArrayList<Attribute>();
		try{
			all.validate(illegalContents);			
			fail("instance of oneone type expected, multiplicity set to 1:1 for this type");
		}catch(IllegalArgumentException e){
			//OK
		}
		
		Attribute oneoneAttribute = attf.create(oneoneType, null, "value");
		List<Attribute> validContents = new ArrayList<Attribute>();
		validContents.add(oneoneAttribute);
		
		all.validate(validContents); //single no exception check
		
		//so it does not complains because of missing this attribute
		illegalContents.add(oneoneAttribute);
		
		Attribute zerozeroAttribute = attf.create(zerozeroType, null, "value");
		illegalContents.add(zerozeroAttribute);
		try{
			all.validate(illegalContents);			
			fail("expected IllegalArgumentException, found instance of att with 0:0 multiplicity");
		}catch(IllegalArgumentException e){
			//OK
		}

		illegalContents.remove(zerozeroAttribute);
		
		Attribute zerooneAttribute1 = attf.create(zerooneType, null, "value");
		Attribute zerooneAttribute2 = attf.create(zerooneType, null, "value2");
		illegalContents.add(zerooneAttribute1);
		illegalContents.add(zerooneAttribute2);
		try{
			all.validate(illegalContents);			
			fail("expected IllegalArgumentException, more than one att");
		}catch(IllegalArgumentException e){
			//OK
		}
		
		validContents.add(zerooneAttribute1);
		all.validate(validContents); //single no exception check
	}

	private AllImpl createTestDescriptor(){
		AttributeType type1 = typeFactory.createType("zerozero", String.class);
		AttributeType type2 = typeFactory.createType("zeroone", String.class);
		AttributeType type3 = typeFactory.createType("oneone", String.class);

		AttributeDescriptor node1 = descFactory.node(type1, 0, 0);
		AttributeDescriptor node2 = descFactory.node(type2, 0, 1);
		AttributeDescriptor node3 = descFactory.node(type3, 1, 1);
		
		Set<AttributeDescriptor>allContents = new HashSet<AttributeDescriptor>();
		allContents.add(node1);
		allContents.add(node2);
		allContents.add(node3);
		
		AllImpl all = new AllImpl(allContents, 1, 1);
		return all;
	}

}
