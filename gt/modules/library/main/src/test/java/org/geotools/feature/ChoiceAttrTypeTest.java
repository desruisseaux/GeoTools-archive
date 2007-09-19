/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.feature;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.geotools.feature.type.FeatureAttributeType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;


/**
 * Tests for the Choice attribute.
 *
 * @author Chris Holmes, TOPP
 * @source $URL$
 */
public class ChoiceAttrTypeTest extends TestCase {
    /** The logger for the default core module. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.feature");

    public ChoiceAttrTypeTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(ChoiceAttrTypeTest.class);

        return suite;
    }

    public void testAttributeTypeFactory() {
        //TODO: we have no ChoiceAttributeTypeFactory.
    }

    public void testGetName() {
        AttributeType type = SampleFeatureFixtures.getChoiceAttrType1();
        assertEquals("choiceTest1", type.getLocalName());
    }

    public void testGetType() {
        AttributeType type = SampleFeatureFixtures.getChoiceAttrType1();
        assertEquals(Object.class, type.getBinding());
    }

    public void testEquals() {
        AttributeType typeA = SampleFeatureFixtures.getChoiceAttrType1();
        LOGGER.finer("created: " + typeA);

        AttributeType typeB = SampleFeatureFixtures.getChoiceAttrType1();
        AttributeType typeC = SampleFeatureFixtures.getChoiceAttrType2();
        AttributeType typeD = SampleFeatureFixtures.getChoiceGeomType();
        AttributeType[] choices = SampleFeatureFixtures.createType1Choices();
	String nil = (String) null;
        AttributeType typeE = SampleFeatureFixtures.createChoiceAttrType(nil,
                choices);
        choices = SampleFeatureFixtures.createType1Choices();
        AttributeType typeF = SampleFeatureFixtures.createChoiceAttrType(nil,
                choices);
        assertTrue(typeA.equals(typeA));
        assertTrue(typeA.equals(typeB));
        assertTrue(typeE.equals(typeF));
        assertTrue(!typeA.equals(typeC));
        assertTrue(!typeA.equals(typeD));
        assertTrue(!typeA.equals(null));
        assertTrue(!typeA.equals(typeE));
    }

    public void testIsNillable() {
        AttributeType[] choices = SampleFeatureFixtures.createType1Choices();


        AttributeType type, type2;
        type = SampleFeatureFixtures.createChoiceAttrType("testAtt", choices);
        assertEquals(true, type.isNillable());

	choices[0] = AttributeTypeFactory.newAttributeType("testString", String.class, false);
	type2 = SampleFeatureFixtures.createChoiceAttrType("testAtt2", choices);
        //if one choice is nillable then the overall choice should be.
	assertEquals(true, type.isNillable());
	LOGGER.finer("type is: " + type + "\ntype2 is: " + type2);
	//a choice between two non nillable choices should not be nillable
	type = SampleFeatureFixtures.createChoiceAttrType("choiceTest2", choices);
        type = choices[1] = AttributeTypeFactory.newAttributeType("tester", String.class, false);
        assertEquals(false, type.isNillable());
    }

    public void testIsGeometry() {
        AttributeType type = SampleFeatureFixtures.getChoiceAttrType2();
        assertEquals(false, type instanceof GeometryAttributeType);
        type = SampleFeatureFixtures.getChoiceGeomType();
        assertEquals(true, type instanceof GeometryAttributeType);
    }

    
       public void testValidate(){
           AttributeType type = SampleFeatureFixtures.getChoiceAttrType1();
           try{
               type.validate(new Double(3));
               type.validate(new Byte((byte)3));
	       type.validate("blorg");
           }
           catch(IllegalArgumentException iae){
               fail();
           }
           try{
               type.validate(new Integer(3));
               fail("Integer should not be validated by a Double type");
           }
           catch(IllegalArgumentException iae){
    
           }
           try{
               type.validate(null);
           }
           catch(IllegalArgumentException iae){
               fail("null should have been allowed as type is Nillable");
           }
           type =  SampleFeatureFixtures.getChoiceAttrType2();
           try{
               type.validate(null);
               type.validate((String)null);
               fail("null should not have been allowed as type is not Nillable");
           }
           catch(IllegalArgumentException iae){
    
           }
    
    
           type = AttributeTypeFactory.newAttributeType("testAttribute", List.class, true);
           try{
               type.validate(new ArrayList());
           }
           catch(IllegalArgumentException iae){
               fail("decended types should be allowed");
           }
    
	   
       } 


    public void testFeatureConstruction() throws Exception {
        FeatureType a = SampleFeatureFixtures.createChoiceFeatureType();

        //FeatureType a = FeatureTypeFactory.newFeatureType(new AttributeType[]{},"noAttribs");
        //FeatureType b = FeatureTypeFactory.newFeatureType(new AttributeType[]{AttributeTypeFactory.newAttributeType("testAttribute", Double.class)},"oneAttribs");
        //Direct construction should never be used like this, however it is the only way to test    `
        //the code fully
        //AttributeType feat = AttributeTypeFactory.newAttributeType( "good",a, false);        
    }

    /*    public void testFeatureValidate() throws SchemaException {
       try{
           //FeatureType b = FeatureTypeFactory.newFeatureType(new AttributeType[]{AttributeTypeFactory.newAttributeType("testAttribute", Double.class)},"oneAttribs");
    
           FeatureType type = FeatureTypeFactory.newFeatureType(new AttributeType[]{},"noAttribs");
           AttributeType feat = AttributeTypeFactory.newAttributeType("foo",  type);
           Feature good = type.create(new Object[]{});
           feat.validate(good);
       }
       catch(IllegalAttributeException iae){
           fail();
       }
       Feature bad = null;
       FeatureType b = FeatureTypeFactory.newFeatureType(new AttributeType[]{AttributeTypeFactory.newAttributeType("testAttribute", Double.class)},"oneAttribs");
    
       try{
           bad = b.create(new Object[]{new Double(4)});
       }
       catch(IllegalAttributeException iae){
           fail();
       }
    
       try{
            FeatureType type = FeatureTypeFactory.newFeatureType(new AttributeType[]{},"noAttribs");
            AttributeType feat = AttributeTypeFactory.newAttributeType("foo",  type);
            feat.validate(bad);
            fail();
       }
       catch(IllegalArgumentException iae){
    
       }
    
    
    
       }
    
    
       public void testNumericConstruction(){
           //Direct construction should never be used like this, however it is the only way to test
           //the code fully
           AttributeType num = AttributeTypeFactory.newAttributeType("good",  Double.class, false, 0,new Double(0));
    
           try{
               num = AttributeTypeFactory.newAttributeType("bad",  String.class, false,0,new Double(0));
               fail("Numeric type should not be constructable with type String");
           }
           catch(IllegalArgumentException iae){
           }
       }
    
    
       public void testIsNested(){
           AttributeType type = AttributeTypeFactory.newAttributeType("testAttribute", Double.class, true);
       //        assertEquals(false, type.isNested());
           }
    
    
           public void testParse(){
               AttributeType type = AttributeTypeFactory.newAttributeType("testAttribute", Double.class, true);
               assertEquals(null, type.parse(null));
               assertEquals(new Double(1.1),(Double)type.parse(new Double(1.1)));
    
               type = AttributeTypeFactory.newAttributeType("testAttribute", Integer.class, true);
               assertEquals(new Integer(10),(Integer)type.parse(new Integer(10)));
    
               type = AttributeTypeFactory.newAttributeType("testAttribute", String.class, true);
               assertEquals("foo",type.parse("foo"));
    
               type = AttributeTypeFactory.newAttributeType("testAttribute", Number.class, true);
               assertEquals(3d,((Number)type.parse(new Long(3))).doubleValue(),0);
               assertEquals(4.4d,((Number)type.parse("4.4")).doubleValue(),0);
               type = AttributeTypeFactory.newAttributeType("testAttribute", Number.class, true);
    
    
           }
           public void testParseNumberSubclass() throws Exception {
    
               AttributeType type = AttributeTypeFactory.newAttributeType("testbigdecimal", BigDecimal.class,true);
               Object value = type.parse(new BigDecimal(111.111));
    
               // I modified this test to pass using BigDecimal. -IanS
       //        assertEquals(new Double(111.111),value);
       //        assertEquals(Double.class,value.getClass());
    
               assertEquals(new BigDecimal(111.111),value);
               assertEquals(BigDecimal.class,value.getClass());
           }
    
           public void testBigNumberSupport() throws Exception {
               AttributeType decimal = AttributeTypeFactory.newAttributeType("decimal", BigDecimal.class,true);
               AttributeType integer = AttributeTypeFactory.newAttributeType("integer", BigInteger.class,true);
               BigDecimal decimalValue = new BigDecimal(200);
               BigInteger integerValue = new BigInteger("200");
               Object[] vals = new Object[] {
                 "200",
                 new Integer(200),
                 new Double(200),
                 new Long(200),
                 decimalValue
               };
    
    
               // BigDecimal tests
               for (int i = 0, ii = vals.length; i < ii; i++) {
                   checkNumericAttributeSetting(decimal, vals[i], decimalValue);
               }
    
    
               // BigInteger tests
               for (int i = 0, ii = vals.length; i < ii; i++) {
                   checkNumericAttributeSetting(decimal, vals[i], integerValue);
               }
    
               checkNull(decimal);
               checkNull(integer);
           }
    
           private void checkNull(AttributeType type) {
               if (type.isNillable()) {
                   assertNull(type.parse(null));
                   type.validate(null);
               }
           }
    
           private void checkNumericAttributeSetting(AttributeType type,Object value,Number expected) {
               Number parsed = (Number) type.parse(value);
               type.validate(parsed);
               assertEquals(parsed.intValue(),expected.intValue());
           }
    
           public void testTextualSupport() throws Exception {
               AttributeType textual = AttributeTypeFactory.newAttributeType("textual",String.class,true);
    
               Object[] vals = new Object[] {
                   "stringValue",
                   new StringBuffer("stringValue"),
                   new Date(System.currentTimeMillis()),
                   new Long(1000000)
               };
               for (int i = 0, ii = vals.length; i < ii; i++) {
                   Object p = textual.parse(vals[i]);
                   textual.validate(p);
                   assertEquals(p.getClass(),String.class);
               }
    
               checkNull(textual);
           }
    
           public void textTemporalSupport() throws Exception {
               AttributeType temporal = AttributeTypeFactory.newAttributeType("temporal",Date.class,true);
    
               Date d = new Date();
    
               Object[] vals = new Object[] {
                 new String(d.toString()),
                 new Date(),
                 new Long(d.getTime())
               };
    
               for (int i = 0, ii = vals.length; i < ii; i++) {
                   checkTemporalAttributeSetting(temporal, vals[i], d);
               }
    
               checkNull(temporal);
           }
    
           private void checkTemporalAttributeSetting(AttributeType type,Object value,Date expected) {
               Object p = type.parse(value);
               type.validate(p);
               assertEquals(expected,p);
               }*/
}
