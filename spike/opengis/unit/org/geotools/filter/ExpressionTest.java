/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.feature.impl.AttributeFactoryImpl;
import org.geotools.feature.type.TypeFactoryImpl;
import org.opengis.feature.AttributeFactory;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.TypeFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.PrecisionModel;


/**
 * Unit test for expressions.  This is a complimentary test suite with the
 * filter test suite.
 *
 * @author James MacGill, CCG
 * @author Rob Hranac, TOPP
 */
public class ExpressionTest extends TestCase {
    /** Standard logging instance */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.defaultcore");

    /** Feature on which to preform tests */
    private static Feature testSimpleFeature = null;
    private static Feature testFeature = null;
    
    /** Schema on which to preform tests */
    private static SimpleFeatureType testSchema = null;
    
	private TypeFactory typeFactory = new TypeFactoryImpl();
	private AttributeFactory attf = new AttributeFactoryImpl();
    
	static FilterFactory filterFactory = FilterFactory.createFilterFactory();
    boolean set = false;

    /** Test suite for this test case */
    TestSuite suite = null;

    /**
     * Constructor with test name.
     *
     * @param testName DOCUMENT ME!
     */
    public ExpressionTest(String testName) {
        super(testName);
    }

    /**
     * Main for test runner.
     *
     * @param args arguments to run main
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Required suite builder.
     *
     * @return A test suite for this unit test.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(ExpressionTest.class);

        return suite;
    }

    /**
     * Sets up a schema and a test feature.
     *
     * @throws SchemaException If there is a problem setting up the schema.
     * @throws IllegalFeatureException If problem setting up the feature.
     */
    protected void setUp() {
        if (set) {
            return;
        }

        set = true;

        // Create the schema attributes
        LOGGER.finer("creating flat feature...");

        AttributeType geometryAttribute = typeFactory.createType("testGeometry",
                LineString.class);
        LOGGER.finer("created geometry attribute");

        AttributeType booleanAttribute = typeFactory.createType("testBoolean",
                Boolean.class);
        LOGGER.finer("created boolean attribute");

        AttributeType charAttribute = typeFactory.createType("testCharacter",
                Character.class);
        AttributeType byteAttribute = typeFactory.createType("testByte",
                Byte.class);
        AttributeType shortAttribute = typeFactory.createType("testShort",
                Short.class);
        AttributeType intAttribute = typeFactory.createType("testInteger",
                Integer.class);
        AttributeType longAttribute = typeFactory.createType("testLong",
                Long.class);
        AttributeType floatAttribute = typeFactory.createType("testFloat",
                Float.class);
        AttributeType doubleAttribute = typeFactory.createType("testDouble",
                Double.class);
        AttributeType stringAttribute = typeFactory.createType("testString",
                String.class);

        // Builds the schema
        //FeatureTypeFactory feaTypeFactory = FeatureTypeFactory.newInstance("test");
        List<AttributeType>attTypes = new ArrayList<AttributeType>();
        
        //testSchema = new FeatureTypeFlat(geometryAttribute); 
        attTypes.add(geometryAttribute);
        LOGGER.finer("added geometry to feature type");
        attTypes.add(booleanAttribute);
        LOGGER.finer("added boolean to feature type");
        attTypes.add(charAttribute);
        LOGGER.finer("added character to feature type");
        attTypes.add(byteAttribute);
        LOGGER.finer("added byte to feature type");
        attTypes.add(shortAttribute);
        LOGGER.finer("added short to feature type");
        attTypes.add(intAttribute);
        LOGGER.finer("added int to feature type");
        attTypes.add(longAttribute);
        LOGGER.finer("added long to feature type");
        attTypes.add(floatAttribute);
        LOGGER.finer("added float to feature type");
        attTypes.add(doubleAttribute);
        LOGGER.finer("added double to feature type");
        attTypes.add(stringAttribute);
        LOGGER.finer("added string to feature type");
        
        testSchema = typeFactory.createFeatureType("test", attTypes, null);
        LOGGER.finer("created feature type");

        // Creates coordinates for the linestring
        Coordinate[] coords = new Coordinate[3];
        coords[0] = new Coordinate(1, 2);
        coords[1] = new Coordinate(3, 4);
        coords[2] = new Coordinate(5, 6);

        // Builds the test feature
        Object[] attributes = new Object[10];
        GeometryFactory gf = new GeometryFactory(new PrecisionModel());
        attributes[0] = gf.createLineString(coords);
        attributes[1] = new Boolean(true);
        attributes[2] = new Character('t');
        attributes[3] = new Byte("10");
        attributes[4] = new Short("101");
        attributes[5] = new Integer(1002);
        attributes[6] = new Long(10003);
        attributes[7] = new Float(10000.4);
        attributes[8] = new Double(100000.5);
        attributes[9] = "test string data";

        // Creates the feature itself
        //FlatFeatureFactory factory = new FlatFeatureFactory(testSchema);
        testSimpleFeature = attf.create(testSchema, null, attributes);
        LOGGER.finer("...feature created");
    }

    /**
     * Tests the attribute expression.
     *
     * @throws IllegalFilterException if filters mess up.
     */
    public void testAttribute() throws IllegalFilterException {
        // Test integer attribute
        Expression testAttribute = new AttributeExpressionImpl(testSchema,
                "testInteger");
        LOGGER.fine("integer attribute expression equals: " +
            testAttribute.getValue(testSimpleFeature));
        assertEquals(new Integer(1002), testAttribute.getValue(testSimpleFeature));
        
        // Test string attribute
        testAttribute = new AttributeExpressionImpl(testSchema, "testString");
        LOGGER.fine("string attribute expression equals: " +
            testAttribute.getValue(testSimpleFeature));
        assertEquals("test string data", testAttribute.getValue(testSimpleFeature));
    }

    /**
     * Tests the literal expression.
     *
     * @throws IllegalFilterException if there are problems
     */
    public void testLiteral() throws IllegalFilterException {
        // Test integer attribute
        Expression testLiteral = new LiteralExpressionImpl(new Integer(1002));
        LOGGER.fine("integer literal expression equals: " +
            testLiteral.getValue(testSimpleFeature));
        assertEquals(new Integer(1002), testLiteral.getValue(testSimpleFeature));
        
        // Test string attribute
        testLiteral = new LiteralExpressionImpl("test string data");
        LOGGER.fine("string literal expression equals: " +
            testLiteral.getValue(testSimpleFeature));
        assertEquals("test string data", testLiteral.getValue(testSimpleFeature));
    }

    /**
     * Tests the min function expression.
     *
     * @throws IllegalFilterException if filter problems
     */
    public void testMinFunction() throws IllegalFilterException {
        Expression a = new AttributeExpressionImpl(testSchema, "testInteger");
        Expression b = new LiteralExpressionImpl(new Double(1004));

        FunctionExpression min = filterFactory.createFunctionExpression("min");
        min.setArgs(new Expression[]{a,b});
        
        assertEquals(1002d,((Double)min.getValue(testSimpleFeature)).doubleValue(),0);
        
        b = filterFactory.createLiteralExpression(new Double(-100.001));
        min.setArgs(new Expression[]{a,b});
        assertEquals(-100.001,((Double)min.getValue(testSimpleFeature)).doubleValue(),0);
        
        assertEquals(FunctionExpressionImpl.FUNCTION,min.getType());
        
        assertEquals("Min", min.getName());
        assertEquals(2, min.getArgCount());
        assertEquals(min.getArgs()[0],a);
        assertEquals(min.getArgs()[1],b);
        min.toString();
    }
    
    public void testNonExistentFunction(){
        try{
            Expression nochance = filterFactory.createFunctionExpression("%$#%$%#%#$@#%@");
            assertNull(nochance);
        }
        catch(RuntimeException re){
        }
          
    }
    
    public void testFunctionNameTrim() throws IllegalFilterException {
        FunctionExpression min = filterFactory.createFunctionExpression("minFunction");
        assertTrue(min != null);  
    }
    /**
     * Tests the max function expression.
     *
     * @throws IllegalFilterException if filter problems
     */
    public void testMaxFunction() throws IllegalFilterException {
        Expression a = new AttributeExpressionImpl(testSchema, "testInteger");
        Expression b = new LiteralExpressionImpl(new Double(1004));

        FunctionExpression max = filterFactory.createFunctionExpression(
                "MaxFunction");
        max.setArgs(new Expression[] { a, b });
        assertEquals(1004d, ((Double) max.getValue(testSimpleFeature)).doubleValue(),
            0);

        b = new LiteralExpressionImpl(new Double(-100.001));
        max.setArgs(new Expression[]{a,b});
        assertEquals(1002d,((Double)max.getValue(testSimpleFeature)).doubleValue(),0);
        
        assertEquals("Max", max.getName());
        assertEquals(2, max.getArgCount());
        assertEquals(max.getArgs()[0],a);
        assertEquals(max.getArgs()[1],b);
        max.toString();
        
    }
    
    
    public void testInvalidMath(){
        try{
            MathExpressionImpl bad = new MathExpressionImpl(DefaultExpression.ATTRIBUTE);
            fail("Only math types should be allowed when constructing");
        }
        catch(IllegalFilterException ife){
        }
    }
    
    public void testDisalowedLeftAndRightExpressions() throws IllegalFilterException {
        GeometryFactory gf = new GeometryFactory(new PrecisionModel());
        Expression geom = new LiteralExpressionImpl(gf.createPoint(new Coordinate(2,2)));
        Expression text = new LiteralExpressionImpl("text");
        MathExpressionImpl mathTest = new MathExpressionImpl(Expression.MATH_ADD);
        try{
            mathTest.addLeftValue(geom);
            fail("geometries are not allowed in math expressions");
        }
        catch(IllegalFilterException ife){
        }
        try{
            mathTest.addRightValue(geom);
            fail("geometries are not allowed in math expressions");
        }
        catch(IllegalFilterException ife){
        }
        try{
            mathTest.addLeftValue(text);
            fail("text strings are not allowed in math expressions");
        }
        catch(IllegalFilterException ife){
        }
        try{
            mathTest.addRightValue(text);
            fail("text strings are not allowed in math expressions");
        }
        catch(IllegalFilterException ife){
        }
    }
    
    public void testIncompleteMathExpression() throws IllegalFilterException {
        Expression testAttribute1 = new LiteralExpressionImpl(new Integer(4));

        MathExpressionImpl mathTest = new MathExpressionImpl(DefaultExpression.MATH_ADD);
        mathTest.addLeftValue(testAttribute1);
        try{
            mathTest.getValue(testSimpleFeature);
            fail("math expressions should not work if right hand side is not set");
        }
        catch(IllegalArgumentException ife){
        }
        mathTest = new MathExpressionImpl(DefaultExpression.MATH_ADD);
        mathTest.addRightValue(testAttribute1);
        try{
            mathTest.getValue(testSimpleFeature);
            fail("math expressions should not work if left hand side is not set");
        }
        catch(IllegalArgumentException ife){
        }
    }
    
    /**
     * Tests the math expression.
     *
     * @throws IllegalFilterException if filter problems
     */
    public void testMath() throws IllegalFilterException {
        // Test integer attribute
        Expression testAttribute1 = new LiteralExpressionImpl(new Integer(4));
        Expression testAttribute2 = new LiteralExpressionImpl(new Integer(2));
        
        // Test addition
        MathExpressionImpl mathTest = new MathExpressionImpl(DefaultExpression.MATH_ADD);
        mathTest.addLeftValue(testAttribute1);
        mathTest.addRightValue(testAttribute2);
        LOGGER.fine("math test: " + testAttribute1.getValue(testSimpleFeature) +
            " + " + testAttribute2.getValue(testSimpleFeature) + " = " +
            mathTest.getValue(testSimpleFeature));
        assertEquals(new Double(6), mathTest.getValue(testSimpleFeature));
        
        // Test subtraction
        mathTest = new MathExpressionImpl(DefaultExpression.MATH_SUBTRACT);
        mathTest.addLeftValue(testAttribute1);
        mathTest.addRightValue(testAttribute2);
        LOGGER.fine("math test: " + testAttribute1.getValue(testSimpleFeature) +
            " - " + testAttribute2.getValue(testSimpleFeature) + " = " +
            mathTest.getValue(testSimpleFeature));
        assertEquals(new Double(2), mathTest.getValue(testSimpleFeature));
        
        // Test multiplication
        mathTest = new MathExpressionImpl(DefaultExpression.MATH_MULTIPLY);
        mathTest.addLeftValue(testAttribute1);
        mathTest.addRightValue(testAttribute2);
        LOGGER.fine("math test: " + testAttribute1.getValue(testSimpleFeature) +
            " * " + testAttribute2.getValue(testSimpleFeature) + " = " +
            mathTest.getValue(testSimpleFeature));
        assertEquals(new Double(8), mathTest.getValue(testSimpleFeature));
        
        // Test division
        mathTest = new MathExpressionImpl(DefaultExpression.MATH_DIVIDE);
        mathTest.addLeftValue(testAttribute1);
        mathTest.addRightValue(testAttribute2);
        LOGGER.fine("math test: " + testAttribute1.getValue(testSimpleFeature) +
            " / " + testAttribute2.getValue(testSimpleFeature) + " = " +
            mathTest.getValue(testSimpleFeature));
        assertEquals(new Double(2), mathTest.getValue(testSimpleFeature));
    }
}
