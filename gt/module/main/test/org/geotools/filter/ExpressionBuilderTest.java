/*
 * ExpressionBuilderTest.java
 * JUnit based test
 *
 * Created on 14 December 2004, 11:56
 */

package org.geotools.filter;

import org.geotools.filter.expression.Expression;
import org.geotools.filter.parser.ParseException;

/**
 *
 * @author James
 * @source $URL$
 */
public class ExpressionBuilderTest extends FilterTestSupport {
    
    private FilterFactory filterFac = FilterFactoryFinder.createFilterFactory();
    
    public ExpressionBuilderTest(String testName) {
        super(testName);
    }
    
    
    public static junit.framework.Test suite() {
        junit.framework.TestSuite suite = new junit.framework.TestSuite(ExpressionBuilderTest.class);
        
        return suite;
    }
    
    /**
     * Test of parse method, of class org.geotools.filter.ExpressionBuilder.
     */
    public void testParseSimpleExpressions() {
        System.out.println("testSimpleExpressions");
        try{
            Expression exp = (Expression)ExpressionBuilder.parse("10 + 5");
            assertEquals(15, ((Number)exp.getValue(testFeature)).intValue());
            exp = (Expression)ExpressionBuilder.parse("10 + (4 / 2)");
            assertEquals(12, ((Number)exp.getValue(testFeature)).intValue());
            exp = (Expression)ExpressionBuilder.parse("10 + 4 / 2");
            assertEquals(12, ((Number)exp.getValue(testFeature)).intValue());
            exp = (Expression)ExpressionBuilder.parse("(10 + 4) / 2");
            assertEquals(7, ((Number)exp.getValue(testFeature)).intValue());
            
        } catch(ParseException pe){
            fail(pe.getMessage());
        }
    }
    
    /**
     * Test of parse method, of class org.geotools.filter.ExpressionBuilder.
     */
    public void testParseAttributeExpressions() {
        System.out.println("testAttributeExpressions");
        try{
            Expression exp = (Expression)ExpressionBuilder.parse("10 + testInteger");
            assertEquals(1012, ((Number)exp.getValue(testFeature)).intValue());
            
            
        } catch(ParseException pe){
            fail(pe.getMessage());
        }
    }
    
     /**
     * Test of parse method, of class org.geotools.filter.ExpressionBuilder.
     */
    public void testParseAttributeAndValidateExpressions() {
        System.out.println("testAttributeExpressions");
        try{
            Expression exp = (Expression)ExpressionBuilder.parse(testFeature.getFeatureType(),"10 + testInteger");
            assertEquals(1012, ((Number)exp.getValue(testFeature)).intValue());
            
            
        } catch(ParseException pe){
            fail(pe.getMessage());
        }
        
        try{
            Expression exp = (Expression)ExpressionBuilder.parse(testFeature.getFeatureType(),"10 + notAnAttribute");
            fail("Exception should have been thrown");
        }
        catch(Exception ife){
   
        }
    }
    
    public void testParseComparisonFilters() {
        System.out.println("testComparisonFilters");
        try{
            Filter filter = (Filter)ExpressionBuilder.parse("5 < 10");
            assertTrue(filter.contains(testFeature));
            filter = (Filter)ExpressionBuilder.parse("5 > 10");
            assertFalse(filter.contains(testFeature));
            filter = (Filter)ExpressionBuilder.parse("testInteger < 2000");
            assertTrue(filter.contains(testFeature));
            
        } catch(ParseException pe){
            fail(pe.getMessage());
        }
    }
    
    public void testParseBooleanFilters() {
        System.out.println("testBooleanFilters");
        try{
            Filter filter = (Filter)ExpressionBuilder.parse("5 < 10 AND 1 < 2");
            assertTrue(filter.contains(testFeature));
            filter = (Filter)ExpressionBuilder.parse("5 > 10 OR 1 < 2");
            assertTrue(filter.contains(testFeature));
            filter = (Filter)ExpressionBuilder.parse("5 > 10 AND 1 < 2");
            assertFalse(filter.contains(testFeature));
            
            filter = (Filter)ExpressionBuilder.parse("3 < 4 AND (1 < 2 OR 5 > 10)");
            assertTrue(filter.contains(testFeature));
            filter = (Filter)ExpressionBuilder.parse("3 < 4 AND 1 < 2 OR 5 > 10");
            assertTrue(filter.contains(testFeature));
            
            filter = (Filter)ExpressionBuilder.parse("3 < 4 AND (NOT(2 < 4 AND 5 < 4))");
            assertTrue(filter.contains(testFeature));
            filter = (Filter)ExpressionBuilder.parse("3 < 4 AND (NOT (2 < 4)) AND 5 < 4");
            assertFalse(filter.contains(testFeature));
            
        } catch(ParseException pe){
            fail(pe.getMessage());
        }
    }
    
    public void testRoundtripFilter(){
        System.out.println("testRoudtrip");
        roundtripFilter("[[[ 3 < 4 ] AND NOT [ 2 < 4 ]] AND [ 5 < 4 ]]");
        roundtripFilter("[3<4  AND  2<4 ] OR 5<4");
        roundtripFilter("3<4 && 2<4");
    }
    
     public void testRoundtripExpression(){
        System.out.println("testRoudtrip");
        roundtripExpression("((2 + 4) / 2)");
        roundtripExpression("(2 + (4 / 2))");
        roundtripExpression("(2 + ((4 / 2) / 1))");
        
    }
    
    protected void roundtripFilter(String in){
        try{
            Filter filter = (Filter)ExpressionBuilder.parse(in);
            String out = filter.toString();
            assertEquals(filter, (Filter)ExpressionBuilder.parse(out));
        } catch(ParseException pe){
            fail(pe.getMessage());
        }
    }
    
    protected void roundtripExpression(String in){
        try{
            Expression exp = (Expression)ExpressionBuilder.parse(in);
            Expression out = (Expression)ExpressionBuilder.parse(exp.toString());
            assertEquals(exp.getValue(testFeature), out.getValue(testFeature));
        } catch(ParseException pe){
            fail(pe.getMessage());
        }
    }
    
    /**
     * Test of getFormattedErrorMessage method, of class org.geotools.filter.ExpressionBuilder.
     */
    public void testGetFormattedErrorMessage() {
        System.out.println("testGetFormattedErrorMessage");
        String exp = "12 / ] + 4";
        try{
            ExpressionBuilder.parse(exp);
        } catch(ParseException pe){
            String formated = ExpressionBuilder.getFormattedErrorMessage(pe, exp);
            assertTrue(formated.startsWith("12 / ] + 4\n     ^"));
        }
    }
    
    
}
