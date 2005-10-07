/*
 * AttributeTypeTest.java
 * JUnit based test
 *
 * Created on July 18, 2003, 12:56 PM
 */

package org.geotools.feature.type;

import javax.xml.namespace.QName;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.TypeFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

/**
 * Port of GeoTools 2.2.x AttributeTypeTest to the proposed API
 * @author jamesm
 */
public class AttributeTypeImplTest extends TestCase {
    
	TypeFactory typeFactory;
	
    public AttributeTypeImplTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(AttributeTypeImplTest.class);
        return suite;
    }
    
    public void setUp() throws Exception{
    	super.setUp();
    	this.typeFactory = new TypeFactoryImpl();
    }
    
    public void testAttributeTypeFactory(){
        AttributeType type = typeFactory.createType("testAttribute", Double.class); 
        assertNotNull(type);
        boolean IDENTIFIED = false, NILLABLE = true;
        type = typeFactory.createType(new QName("testAttribute"), Double.class, IDENTIFIED, NILLABLE, null); 
        assertNotNull(type);
        NILLABLE = false;
        type = typeFactory.createType(new QName("testAttribute"), Double.class, IDENTIFIED, NILLABLE, null);
        assertNotNull(type);
    }

    public void testGetName(){
        AttributeType type = typeFactory.createType("testAttribute", Double.class);
        assertEquals("testAttribute", type.name());
        assertEquals(new QName("testAttribute"), type.getName());
    }

    public void testGetBinding(){
        AttributeType type = typeFactory.createType("testAttribute", Double.class);
        assertEquals(Double.class, type.getBinding());
    }
    
    public void testEquals(){
        AttributeType typeA = typeFactory.createType("testAttribute", Double.class);
        AttributeType typeB = typeFactory.createType("testAttribute", Double.class);
        AttributeType typeC = typeFactory.createType("differnetName", Double.class);
        AttributeType typeD = typeFactory.createType("testAttribute", Integer.class);
        AttributeType typeE = typeFactory.createType("secondDifferentName", Integer.class);
        AttributeType typeF = typeFactory.createType("secondDifferentName", Integer.class);
        
        assertTrue(typeA instanceof AttributeTypeImpl);
        assertTrue(typeB instanceof AttributeTypeImpl);
        assertTrue(typeC instanceof AttributeTypeImpl);
        assertTrue(typeD instanceof AttributeTypeImpl);
        assertTrue(typeE instanceof AttributeTypeImpl);
        assertTrue(typeF instanceof AttributeTypeImpl);

        assertTrue(typeA.equals(typeA));
        assertTrue(typeA.equals(typeB));
        assertTrue(typeE.equals(typeF));
        
        assertFalse(typeA.equals(typeC));
        assertFalse(typeA.equals(typeD));
        assertFalse(typeA.equals(null));
        assertFalse(typeA.equals(typeE));
    }
    
    public void testIsNillable(){
        AttributeType type = typeFactory.createType("testAttribute", Double.class); 
        assertTrue(type.isNillable());
        type = typeFactory.createType(new QName("testAttribute"), Double.class, false, true, null);
        assertTrue(type.isNillable());
        type = typeFactory.createType(new QName("testAttribute"), Double.class, false, false, null);
        assertFalse(type.isNillable());
    }
    
    public void testIsGeometry(){
        AttributeType type = typeFactory.createType("testAttribute", Double.class);
        assertFalse(type instanceof GeometryType);
        type = typeFactory.createType("testAttribute", Point.class);
        assertTrue(type instanceof GeometryType);
        type = typeFactory.createType("testAttribute", Geometry.class);
        assertTrue(type instanceof GeometryType);
    }
    
    /*    
    public void testValidate(){
        AttributeType type = AttributeTypeFactory.newAttributeType("testAttribute", Double.class, true);
        try{
            type.validate(new Double(3));
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
        type = AttributeTypeFactory.newAttributeType("testAttribute", Double.class, false);
        try{
            type.validate(null);
            type.validate((Double)null);
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
        FeatureType a = FeatureTypeFactory.newFeatureType(new AttributeType[]{},"noAttribs");
        FeatureType b = FeatureTypeFactory.newFeatureType(new AttributeType[]{AttributeTypeFactory.newAttributeType("testAttribute", Double.class)},"oneAttribs");
        //Direct construction should never be used like this, however it is the only way to test
        //the code fully
        AttributeType feat = AttributeTypeFactory.newAttributeType( "good",a, false);        
    }
    
    public void testFeatureValidate() throws SchemaException {
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
    }
    
  */  
    
}
