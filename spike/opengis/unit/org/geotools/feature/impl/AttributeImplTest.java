package org.geotools.feature.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.geotools.feature.type.TypeFactoryImpl;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.ExpressionBuilder;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterType;
import org.geotools.filter.IllegalFilterException;
import org.opengis.feature.Attribute;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.TypeFactory;

public class AttributeImplTest extends TestCase {
	private static final Logger LOGGER = Logger.getLogger(AttributeImplTest.class.getPackage().getName());
	TypeFactory typeFact = new TypeFactoryImpl();

	AttributeType testType;

	protected void setUp() throws Exception {
		super.setUp();
		boolean NILLABLE = true;
		boolean IDENTIFIED = false;
		testType = typeFact.createType(new QName("test"), Object.class,
				IDENTIFIED, NILLABLE, null, null);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test constructing AttributeImple for unkown primitive types. Known
	 * primitive types get handled automatically by AttributeFactoryImpl create*
	 * methods
	 */
	public void testConstruct() {
		try {
			new AttributeImpl(null);
			fail("NullPointerException expected");
		} catch (NullPointerException e) {
			// OK
		}

		AttributeImpl att;
		boolean NILLABLE = false;
		boolean IDENTIFIED = true;

		testType = typeFact.createType(new QName("test"), Integer.class,
				IDENTIFIED, NILLABLE, null, null);
		try {
			att = new AttributeImpl(testType);
			fail("NullPointerException expected since its identified");
		} catch (NullPointerException e) {
			// OK
		}

		NILLABLE = true;
		testType = typeFact.createType(new QName("test"), Integer.class,
				IDENTIFIED, NILLABLE, null, null);
		Object content = new Integer(1);
		att = new AttributeImpl("anID", testType, content);
		assertEquals("anID", att.ID);
		assertEquals(content, att.content);

		try {
			/*
			 * AttributeImpl fails if content is not of binding type. Use of
			 * factory workarounds this by supplying specialized subclasses that
			 * deal with parsing between compatible types
			 */
			att = new AttributeImpl(null, testType, new Double(1));
			fail("expected IllegalArgumentException, value and type binding not same");
		} catch (IllegalArgumentException e) {
			// OK
		}
	}

	/**
	 * Test method for 'org.geotools.feature.impl.AttributeImpl.getID()'
	 */
	public void testGetID() {
		Attribute att = new AttributeImpl("1", testType);
		assertEquals("1", att.getID());

		att = new AttributeImpl(testType);
		assertNull(att.getID());

		att = new AttributeImpl("2", testType, new Object());
		assertEquals("2", att.getID());
	}

	/**
	 * Test method for 'org.geotools.feature.impl.AttributeImpl.get()'
	 */
	public void testGet() {
		boolean NILLABLE = true;
		boolean IDENTIFIED = false;
		testType = typeFact.createType(new QName("test"), Double.class,
				IDENTIFIED, NILLABLE, null, null);

		Attribute att = new AttributeImpl(null, testType, 21.1);
		assertEquals(new Double(21.1), att.get());

		att = new AttributeImpl(testType);
		assertNull(att.get());
	}

	/*
	 * Test method for 'org.geotools.feature.impl.AttributeImpl.getType()'
	 */
	public void testGetType() {
		boolean NILLABLE = true;
		boolean IDENTIFIED = false;
		testType = typeFact.createType(new QName("test"), Double.class,
				IDENTIFIED, NILLABLE, null, null);
		Attribute att = new AttributeImpl(testType);
		assertEquals(testType, att.getType());
		assertSame(testType, att.getType());

		NILLABLE = false;
		IDENTIFIED = false;
		testType = typeFact.createType(new QName("test"), Double.class,
				IDENTIFIED, NILLABLE, null, null);

		assertFalse(testType.equals(att.getType()));
	}

	/**
	 * Test method for 'org.geotools.feature.impl.AttributeImpl.set(Object)'
	 */
	public void testSet() {
		AttributeImpl att = new AttributeImpl(testType);
		att.set(null);
		assertNull(att.content);
		
		Object value = new Integer(1);
		att.set(value);
		assertNotNull(att.content);
		assertEquals(new Integer(1), att.content);
		assertSame(value, att.content);
	}

	/** 
	 * 
	 * Test method for 'org.geotools.feature.impl.AttributeImpl.equals(Object)'
	 */
	public void testEquals() {
		testType = typeFact.createType("test", Integer.class);
		AttributeImpl att1 = new AttributeImpl(testType);
		AttributeImpl att2 = new AttributeImpl(testType);

		testType = typeFact.createType("test", String.class);
		AttributeImpl att3 = new AttributeImpl(testType);

		assertEquals(att1, att2);
		assertFalse(att1.equals(att3));
		
		att1.set(new Integer(19));
		assertFalse(att1.equals(att2));
		att2.set(new Integer(19));
		assertEquals(att1, att2);
		
		att1 = new AttributeImpl("id1", testType);
		att2 = new AttributeImpl("id1", testType);
		assertEquals(att1, att2);
		att2 = new AttributeImpl("id2", testType);
		assertFalse(att1.equals(att2));

		att1 = new AttributeImpl("id1", testType, "one");
		att2 = new AttributeImpl("id1", testType, "one");
		assertEquals(att1, att2);
		att2 = new AttributeImpl("id2", testType, "two");
		assertFalse(att1.equals(att2));
}

	/**
	 * Test method for 'org.geotools.feature.impl.AttributeImpl.parse(Object)'
	 * parse(Object) is a no-op method in AttributeImpl, subclasses may
	 * override
	 */
	public void testParse() {
		AttributeImpl att = new AttributeImpl(testType);
		assertNull(att.parse(null));
		Object value = new Object();
		assertSame(value, att.parse(value));
	}

	/**
	 * Test for protected
	 * 'org.geotools.feature.impl.AttributeImpl.validate(Object)'
	 * 
	 */
	public void testValidateNull() {
		boolean NILLABLE = false;
		testType = typeFact.createType(new QName("test"), Double.class,
				false, NILLABLE, null, null);
		AttributeImpl att = new AttributeImpl(null, testType, new Double(2));
		
		try{
			att.validate(null);
			fail("validate should check for null when type is not nillable");
		}catch(NullPointerException e){
			//OK
		}

		NILLABLE = true;
		testType = typeFact.createType(new QName("test"), Double.class,
				false, NILLABLE, null, null);
		att = new AttributeImpl(null, testType, new Double(2));
		
		att.validate(null); //no exception check
	}

	/**
	 * Test for protected method
	 * 'org.geotools.feature.impl.AttributeImpl.validate(Object)'
	 * 
	 */
	public void testValidateBinding() {
		boolean NILLABLE = true;
		testType = typeFact.createType(new QName("test"), CharSequence.class,
				false, NILLABLE, null, null);
		AttributeImpl att = new AttributeImpl(testType);
		
		att.validate(null); //no-exception check
		att.validate("string val");
		att.validate(new StringBuffer("another charseq val"));
		
		try{
			att.validate(new Object());
			fail("binding not checked");
		}catch(IllegalArgumentException e){
			//OK
		}
	}

	public void testValidateRestrictions() throws Exception{

		Set<Filter>restrictions = new HashSet<Filter>();
		Filter gte = (Filter)ExpressionBuilder.parse(". >= 3");
		Filter lt = (Filter)ExpressionBuilder.parse(". < 7");
		restrictions.add(gte);
		restrictions.add(lt);
		
		testType = typeFact.createType(new QName("intAtt"), Integer.class, false, true, restrictions);

		AttributeImpl att = new AttributeImpl(testType);
		
		try{
			att.validate(new Integer(1));
		}catch(IllegalArgumentException e){
			LOGGER.info("validation ok: " + e.getMessage());
		}
		
		try{
			att.validate(new Integer(9));
		}catch(IllegalArgumentException e){
			LOGGER.info("validation ok: " + e.getMessage());
		}

		//simple no-exceptions checks with valid content
		att.validate(new Integer(3));
		att.validate(new Integer(4));
		att.validate(new Integer(5));
		att.validate(new Integer(6));
		
		/*
		FilterFactory ff = FilterFactory.createFilterFactory();
		
		CompareFilter gte = ff.createCompareFilter(FilterType.COMPARE_GREATER_THAN_EQUAL);
		gte.addLeftValue(ff.createAttributeExpression(testType));
		gte.addRightValue(ff.createLiteralExpression(3));
		
		restrictions.add(gte);

		CompareFilter lt = ff.createCompareFilter(FilterType.COMPARE_LESS_THAN);
		gte.addLeftValue(ff.createAttributeExpression(testType));
		gte.addRightValue(ff.createLiteralExpression(7));
		
		restrictions.add(lt);
		*/
	}

}
