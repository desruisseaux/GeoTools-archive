/*
 * EqualIntervalFunctionTest.java
 * JUnit based test
 *
 * Created on 09 March 2005, 12:43
 */

package org.geotools.filter.function;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureIterator;
import org.geotools.filter.Expression;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.FunctionExpression;
import org.geotools.filter.parser.ParseException;

/**
 *
 * @author James
 */
public class EqualIntervalFunctionTest extends FunctionTestSupport {
   
    
    public EqualIntervalFunctionTest(String testName) {
        super(testName);
    }
    
    protected void tearDown() throws java.lang.Exception {
    }
    
    public static junit.framework.Test suite() {
        junit.framework.TestSuite suite = new junit.framework.TestSuite(EqualIntervalFunctionTest.class);
        
        return suite;
    }
    
    /**
     * Test of getName method, of class org.geotools.filter.functions.EqualIntervalFunction.
     */
    public void testInstance() {
        FunctionExpression equInt = FilterFactoryFinder.createFilterFactory().createFunctionExpression("EqualInterval");
        assertNotNull(equInt);
    }
    
    
    /**
     * Test of getName method, of class org.geotools.filter.functions.EqualIntervalFunction.
     */
    public void testGetName() {
        FunctionExpression equInt = FilterFactoryFinder.createFilterFactory().createFunctionExpression("EqualInterval");
        System.out.println("testGetName");
        assertEquals("EqualInterval",equInt.getName());
    }
    
    /**
     * Test of setNumberOfClasses method, of class org.geotools.filter.functions.EqualIntervalFunction.
     */
    public void testSetNumberOfClasses() throws Exception{
        System.out.println("testSetNumberOfClasses");
        
        Expression classes = (Expression)builder.parse(dataType, "3");
        Expression exp = (Expression)builder.parse(dataType, "foo");
        EqualIntervalFunction func = (EqualIntervalFunction)fac.createFunctionExpression("EqualInterval");
        func.setArgs(new Expression[]{exp,classes});
        assertEquals(3,func.getNumberOfClasses());
        classes = (Expression)builder.parse(dataType, "12");
        func.setArgs(new Expression[]{exp,classes});
        assertEquals(12,func.getNumberOfClasses());
        
    }
    
    /**
     * Test of calculateSlot method, of class org.geotools.filter.functions.EqualIntervalFunction.
     */
    public void testCalculateSlot() throws ParseException {
        System.out.println("testCalculateSlot");
        Expression classes = (Expression)builder.parse(dataType, "3");
        Expression exp = (Expression)builder.parse(dataType, "foo");
        FunctionExpression func = fac.createFunctionExpression("EqualInterval");
        func.setArgs(new Expression[]{exp,classes});
        
        FeatureIterator list = fc.features();
        while(list.hasNext()){
            Feature f = list.next();
            int slot = ((Number)func.getValue(f)).intValue();
            System.out.println(slot);
        }
        
    }
    
    /**
     * Test of getValue method, of class org.geotools.filter.functions.EqualIntervalFunction.
     */
    public void testGetValue() throws Exception{
        System.out.println("testGetValue");
        Expression classes = (Expression)builder.parse(dataType, "2");
        Expression exp = (Expression)builder.parse(dataType, "foo");
        FunctionExpression func = fac.createFunctionExpression("EqualInterval");
        func.setArgs(new Expression[]{exp,classes});
        
        FeatureIterator list = fc.features();
        while(list.hasNext()){
            Feature f = list.next();
            int slot = ((Number)func.getValue(f)).intValue();
            int value = ((Number)f.getAttribute("foo")).intValue();
            if(value < 46){
                assertEquals(0,slot);
            } else{
                assertEquals(1,slot);
            }
        }
    }
    
    public void testDecimalPlaces() throws Exception{
    	System.out.println("testDecimalPlaces");
    	FunctionExpression func = fac.createFunctionExpression("EqualInterval");
        EqualIntervalFunction eif = (EqualIntervalFunction) func;
        int val = eif.decimalPlaces(100.0);
        System.out.println("width 100.0 truncate at "+val+" decimal place(s)");
        assertTrue(val == 0);
        val = eif.decimalPlaces(1.1);
        System.out.println("width 1.1 truncate at "+val+" decimal place(s)");
        assertTrue(val == 1);
        val = eif.decimalPlaces(0.9);
        System.out.println("width 0.9 truncate at "+val+" decimal place(s)");
        assertTrue(val == 1);
        val = eif.decimalPlaces(0.1);
        System.out.println("width 0.1 truncate at "+val+" decimal place(s)");
        assertTrue(val == 2);
        val = eif.decimalPlaces(0.01);
        System.out.println("width 0.01 truncate at "+val+" decimal place(s)");
        assertTrue(val == 3);
        val = eif.decimalPlaces(0.001);
        System.out.println("width 0.001 truncate at "+val+" decimal place(s)");
        assertTrue(val == 4);
    }
    
    public void testRound() throws Exception{
    	System.out.println("testRound");
    	FunctionExpression func = fac.createFunctionExpression("EqualInterval");
        EqualIntervalFunction eif = (EqualIntervalFunction) func;
        double val = eif.round(100.0, 0);
        System.out.println("round 0 digits, 100.0 --> "+val);
        assertEquals(100.0, val, 0);
        val = eif.round(1.12, 1);
        System.out.println("round 1 digit, 1.12 --> "+val);
        assertEquals(1.1, val, 0);
        val = eif.round(0.34523, 2);
        System.out.println("round 2 digits, 0.34523 --> "+val);
        assertEquals(0.35, val, 0);
    }
}
