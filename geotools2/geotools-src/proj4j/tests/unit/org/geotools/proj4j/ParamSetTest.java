/*
 * ParamSetTest.java
 * JUnit based test
 *
 * Created on February 20, 2002, 3:09 PM
 */                

package org.geotools.proj4j;

import junit.framework.*;
import java.util.Hashtable;

/**
 *
 * @author jamesm
 */                                
public class ParamSetTest extends TestCase {
    
    public ParamSetTest(java.lang.String testName) {
        super(testName);
    }        
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(ParamSetTest.class);
        
        return suite;
    }
    
    /** Test of addParam method, of class org.geotools.proj4j.ParamSet. */
    public void testAddParam() {
        System.out.println("testAddParam");
        ParamSet params = new ParamSet();
        params.addParam("proj=poly");
        params.addParam("ellips","clrk66");
        params.addParam("no_defs");
        assertEquals(true,params.contains("proj"));
        assertEquals(true,params.contains("ellips"));
        assertEquals(true,params.contains("no_defs"));
        assertEquals(false,params.contains("foobar"));
        assertEquals("poly",params.getStringParam("proj"));
        assertEquals("clrk66",params.getStringParam("ellips"));
        assertNull(params.getStringParam("foo"));
    }
    public void testAddParamIfNotSet(){
        ParamSet params = new ParamSet();
        params.addParam("proj=poly");
        params.addParam("ellips","clrk66");
        params.addParam("no_defs");
        params.addParamIfNotSet("foo=bar");
        assertEquals("bar",params.getStringParam("foo"));
        params.addParamIfNotSet("proj=foobar");
        assertEquals("poly",params.getStringParam("proj"));
    }
    /** Test of contains method, of class org.geotools.proj4j.ParamSet. */
    public void testContains() {
        assertEquals(true,testSet.contains("testInt"));
        assertEquals(true,testSet.contains("testString"));
        assertEquals(false,testSet.contains("foobar"));
    }
    
    /** Test of getIntegerParam method, of class org.geotools.proj4j.ParamSet. */
    public void testGetIntegerParam() {
        assertEquals(123,testSet.getIntegerParam("testInt"));
        assertEquals(0,testSet.getIntegerParam("testString"));
    }
    
    /** Test of getFloatParam method, of class org.geotools.proj4j.ParamSet. */
    public void testGetFloatParam() {
        assertEquals(456.321f,testSet.getFloatParam("testFloat"),0);
        assertEquals(0,testSet.getFloatParam("testString"),0);
    }
    
    /** Test of getStringParam method, of class org.geotools.proj4j.ParamSet. */
    public void testGetStringParam() {
        assertEquals("123",testSet.getStringParam("testInt"));
        assertEquals("foo",testSet.getStringParam("testString"));
    }
    
    /** Test of getRadiansParam method, of class org.geotools.proj4j.ParamSet. */
    public void testGetRadiansParam() {
        System.out.println("testGetRadiansParam");
        assertEquals(0.19794943,testSet.getRadiansParam("testRadian"),0);
        assertEquals(0.19794943,testSet.getRadiansParam("testDMS"),1e-6);
        assertEquals(0.19794943,testSet.getRadiansParam("testD"),1e-6);
      
    }
    
    public ParamSet testSet;
    
    public void setUp(){
        testSet = new ParamSet();
        testSet.addParam("testInt=123");
        testSet.addParam("testFloat=456.321");
        testSet.addParam("testString=foo");
        testSet.addParam("testRadian=0.19794943r");
        testSet.addParam("testDMS=11d20'30\"");
        testSet.addParam("testD=11.341667");
        
    }
        
    
    // Add test methods here, they have to start with 'test' name.
    // for example: 
    // public void testHello() {}


}
