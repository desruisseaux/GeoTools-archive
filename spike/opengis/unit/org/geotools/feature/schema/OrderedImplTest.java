package org.geotools.feature.schema;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.geotools.feature.impl.AttributeFactoryImpl;
import org.geotools.feature.type.TypeFactoryImpl;
import org.opengis.feature.Attribute;
import org.opengis.feature.AttributeFactory;
import org.opengis.feature.schema.AttributeDescriptor;
import org.opengis.feature.schema.Descriptor;
import org.opengis.feature.schema.DescriptorFactory;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.TypeFactory;

public class OrderedImplTest extends TestCase {
	private TypeFactory typeFactory = new TypeFactoryImpl();

	private DescriptorFactory descFactory = new DescriptorFactoryImpl();

	private AttributeFactory attf = new AttributeFactoryImpl();
	
	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/*
	 * Test method for 'org.geotools.feature.schema.OrderedImpl.equals(Object)'
	 */
	public void testEquals() {
		OrderedImpl sequence1 = new OrderedImpl(new ArrayList<Descriptor>());
		assertFalse(sequence1.equals(null));
		assertFalse(sequence1.equals(new Object()));
		assertTrue(sequence1.equals(sequence1));

		AttributeType type1 = typeFactory.createType("string1", String.class);
		AttributeType type2 = typeFactory.createType("int1", Integer.class);

		NodeImpl node1 = new NodeImpl(type1);
		NodeImpl node2 = new NodeImpl(type2);

		NodeImpl node3 = new NodeImpl(type1);
		NodeImpl node4 = new NodeImpl(type2);

		List<Descriptor> content1 = new ArrayList<Descriptor>();
		List<Descriptor> content2 = new ArrayList<Descriptor>();
		content1.add(node1);
		content1.add(node2);
		content2.add(node3);

		sequence1 = new OrderedImpl(content1);
		OrderedImpl sequence2 = new OrderedImpl(content2);
		assertFalse(sequence1.equals(sequence2));

		content2.add(node4);
		sequence2 = new OrderedImpl(content2);
		assertTrue(sequence1.equals(sequence2));
		assertTrue(sequence2.equals(sequence1));
	}

	/**
	 * No null list of AttributeDescriptors
	 */
	public void testConstructNullParam() {
		try {
			new OrderedImpl(null);
			fail("expected NullPointerException");
		} catch (NullPointerException e) {
			// OK
		}
	}

	/**
	 * null not valid as argument of validate()
	 */
	public void testValidateNull() {
		OrderedImpl sequence = createTestDescriptor();
		try {
			sequence.validate(null);
		} catch (NullPointerException e) {
			// OK
			return;
		}
		fail("expected NullPointerException");
	}

	/**
	 * No null member of validate parameter list
	 */
	public void testValidateNullInAttributes() {
		OrderedImpl seq = createTestDescriptor();
		List<Attribute> illegalContents = new ArrayList<Attribute>();
		illegalContents.add(null);
		try {
			seq.validate(illegalContents);
		} catch (NullPointerException e) {
			// OK
			return;
		}
		fail("expected NullPointerException");
	}

	/**
	 * No members of wrong type in validate
	 */
	public void testValidateIllegalContentType() {
		OrderedImpl seq = createTestDescriptor();
		List<Attribute> illegalContents = new ArrayList<Attribute>();

		AttributeType badType = typeFactory.createType(new QName(
				"http://anotherns.net", "zeroone"), String.class);
		Attribute badAttribute = new AttributeFactoryImpl().create(badType,
				null, "value");
		illegalContents.add(badAttribute);

		try {
			seq.validate(illegalContents);
		} catch (IllegalArgumentException e) {
			// OK
			return;
		}
		fail("expected IllegalArgumentException");
	}

	/**
	 * Tests that OrderedImpl.validate checks content
	 * is in right order according to schema
	 */
	public void testValidateSequence() {
		OrderedImpl seq = createTestDescriptor();

		// types used in createTestDescriptor
		AttributeType s1 = typeFactory.createType("s1", String.class);
		AttributeType i1 = typeFactory.createType("i1", Integer.class);
		AttributeType i2 = typeFactory.createType("i2", Integer.class);

		List<Attribute> contents = new ArrayList<Attribute>();

		//Sequence is:
		// <xs:element name="s1" type="xs:string"/>
		// <xs:element name="i1" type="xs:int" maxOccurs="2"/>
		// <xs:element name="s1" type="xs:string" minOccurs="0"
		// maxOccurs="unbounded"/>
		// <xs:element name="i2" type="xs:int"/>

		contents.add(attf.create(s1, null,  "s1_1"));
		// i1 has multiplicity 1:2, putting less than 1
		// should fail validation
		contents.add(attf.create(i2, null,  "1"));
	
		try{
			seq.validate(contents);
			fail("Expected exception since i1 does not complies with multiplicity restriction");
		}catch(IllegalArgumentException e){
			//OK
		}

		contents.clear();
		contents.add(attf.create(s1, null,  "s1_1"));
		// i1 has multiplicity 1:2, putting more than 2
		// should fail validation
		contents.add(attf.create(i1, null,  "11"));
		contents.add(attf.create(i1, null,  "12"));
		contents.add(attf.create(i1, null,  "13"));
		contents.add(attf.create(s1, null,  "s1_2"));
		contents.add(attf.create(i2, null,  "21"));

		try{
			seq.validate(contents);
			fail("Expected exception since there are too many i1 instances");
		}catch(IllegalArgumentException e){
			//OK
		}

		contents.clear();
		//try a valid content
		contents.add(attf.create(s1, null,  "s1_1"));
		contents.add(attf.create(i1, null,  "11"));
		contents.add(attf.create(i1, null,  "12"));
		contents.add(attf.create(s1, null,  "s1_2"));
		contents.add(attf.create(s1, null,  "s1_3"));
		contents.add(attf.create(s1, null,  "s1_4"));
		contents.add(attf.create(s1, null,  "s1_5"));
		contents.add(attf.create(s1, null,  "s1_6"));
		contents.add(attf.create(s1, null,  "s1_7"));
		contents.add(attf.create(s1, null,  "s1_8"));
		contents.add(attf.create(i2, null,  "21"));

		seq.validate(contents); //single no-exception check
	}

	/**
	 * validates contents multiplicity restrictions
	 */
	public void testValidateMultiplicityRangeCheck() {
		OrderedImpl seq = createTestDescriptor();

		// types used in createTestDescriptor
		AttributeType s1 = typeFactory.createType("s1", String.class);
		AttributeType i1 = typeFactory.createType("i1", Integer.class);
		AttributeType i2 = typeFactory.createType("i2", Integer.class);

		List<Attribute> contents = new ArrayList<Attribute>();

		//Sequence is:
		// <xs:element name="s1" type="xs:string"/>
		// <xs:element name="i1" type="xs:int" maxOccurs="2"/>
		// <xs:element name="s1" type="xs:string" minOccurs="0"
		// maxOccurs="unbounded"/>
		// <xs:element name="i2" type="xs:int"/>

		contents.add(attf.create(s1, null,  "s1_1"));
		// i1 has multiplicity 1:2, putting less than 1
		// should fail validation
		contents.add(attf.create(i2, null,  "1"));
	
		try{
			seq.validate(contents);
			fail("Expected exception since i1 does not complies with multiplicity restriction");
		}catch(IllegalArgumentException e){
			//OK
		}

		contents.clear();
		contents.add(attf.create(s1, null,  "s1_1"));
		// i1 has multiplicity 1:2, putting more than 2
		// should fail validation
		contents.add(attf.create(i1, null,  "11"));
		contents.add(attf.create(i1, null,  "12"));
		contents.add(attf.create(i1, null,  "13"));
		contents.add(attf.create(s1, null,  "s1_2"));
		contents.add(attf.create(i2, null,  "21"));

		try{
			seq.validate(contents);
			fail("Expected exception since there are too many i1 instances");
		}catch(IllegalArgumentException e){
			//OK
		}

		contents.clear();
		//try a valid content
		contents.add(attf.create(s1, null,  "s1_1"));
		contents.add(attf.create(i1, null,  "11"));
		contents.add(attf.create(i1, null,  "12"));
		contents.add(attf.create(s1, null,  "s1_2"));
		contents.add(attf.create(s1, null,  "s1_3"));
		contents.add(attf.create(s1, null,  "s1_4"));
		contents.add(attf.create(s1, null,  "s1_5"));
		contents.add(attf.create(s1, null,  "s1_6"));
		contents.add(attf.create(s1, null,  "s1_7"));
		contents.add(attf.create(s1, null,  "s1_8"));
		contents.add(attf.create(i2, null,  "21"));

		seq.validate(contents); //single no-exception check
	}

	
	/**
	 * Tests that a sequence that has multiplicity greater than 1:1
	 * validates itself
	 */
	public void testValidateSelfRepeatingSequence() {
		OrderedImpl seq = createTestDescriptor();

		// types used in createTestDescriptor
		AttributeType s1 = typeFactory.createType("s1", String.class);
		AttributeType i1 = typeFactory.createType("i1", Integer.class);
		AttributeType i2 = typeFactory.createType("i2", Integer.class);

		List<Attribute> contents = new ArrayList<Attribute>();

		//Sequence is:
		// <xs:sequence minOccurs="0" maxOccurs="unbounded">
		// <xs:element name="s1" type="xs:string"/>
		// <xs:element name="i1" type="xs:int" maxOccurs="2"/>
		// <xs:element name="s1" type="xs:string" minOccurs="0"
		// maxOccurs="unbounded"/>
		// <xs:element name="i2" type="xs:int"/>
		// </xs:sequence>

		//first occurrence of the sequence
		contents.add(attf.create(s1, null,  "s1_1_1"));
		contents.add(attf.create(i1, null,  "111"));
		contents.add(attf.create(i1, null,  "112"));
		contents.add(attf.create(s1, null,  "s1_1_2"));
		contents.add(attf.create(i2, null,  "211"));

		//second occurrence of the sequence
		contents.add(attf.create(s1, null,  "s1_2_1"));
		contents.add(attf.create(i1, null,  "121"));
		contents.add(attf.create(i1, null,  "122"));
		contents.add(attf.create(s1, null,  "s1_2_2"));
		contents.add(attf.create(i2, null,  "221"));

		//third occurrence of the sequence
		contents.add(attf.create(s1, null,  "s1_3_1"));
		contents.add(attf.create(i1, null,  "122"));
		contents.add(attf.create(i2, null,  "221"));
		
		
		seq.validate(contents); //single no-exception check
	}
	
	/**
	 * Creates a test OrderedImpl like the following xml schema sequence:
	 * 
	 * <pre><code>
	 *  	 &lt;xs:sequence minOccurs=&quot;1&quot; maxOccurs=&quot;unbounded&quot;&gt;
	 *  	 &lt;xs:element name=&quot;s1&quot; type=&quot;xs:string&quot;/&gt;
	 *  	 &lt;xs:element name=&quot;i1&quot; type=&quot;xs:int&quot; maxOccurs=&quot;2&quot;/&gt;
	 *  	 &lt;xs:element name=&quot;s1&quot; type=&quot;xs:string&quot; minOccurs=&quot;0&quot; maxOccurs=&quot;unbounded&quot;/&gt;
	 *  	 &lt;xs:element name=&quot;i2&quot; type=&quot;xs:int&quot;/&gt;
	 *  	 &lt;/xs:sequence&gt;
	 * </code></pre>
	 * 
	 * @return
	 */
	private OrderedImpl createTestDescriptor() {
		AttributeType s1 = typeFactory.createType("s1", String.class);
		AttributeType i1 = typeFactory.createType("i1", Integer.class);
		AttributeType i2 = typeFactory.createType("i2", Integer.class);

		AttributeDescriptor node1 = descFactory.node(s1, 1, 1);
		AttributeDescriptor node2 = descFactory.node(i1, 1, 2);
		AttributeDescriptor node3 = descFactory.node(s1, 0, Integer.MAX_VALUE);
		AttributeDescriptor node4 = descFactory.node(i2, 1, 1);

		List<Descriptor> allContents = new LinkedList<Descriptor>();
		allContents.add(node1);
		allContents.add(node2);
		allContents.add(node3);
		allContents.add(node4);

		OrderedImpl sequence = new OrderedImpl(allContents, 0,
				Integer.MAX_VALUE);
		return sequence;
	}

}
