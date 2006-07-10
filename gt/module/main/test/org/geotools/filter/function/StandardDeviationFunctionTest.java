package org.geotools.filter.function;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureIterator;
import org.geotools.filter.Expression;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.FunctionExpression;
import org.geotools.filter.parser.ParseException;

/**
 *
 * @author Cory Horner, Refractions Research
 * @source $URL: http://svn.geotools.org/geotools/branches/2.2.x/module/main/test/org/geotools/filter/function/EqualIntervalFunctionTest.java $
 */
public class StandardDeviationFunctionTest extends FunctionTestSupport {
   
    
    public StandardDeviationFunctionTest(String testName) {
        super(testName);
    }
    
    protected void tearDown() throws java.lang.Exception {
    }
    
    public static junit.framework.Test suite() {
        junit.framework.TestSuite suite = new junit.framework.TestSuite(StandardDeviationFunctionTest.class);
        
        return suite;
    }
    
    public void testInstance() {
        FunctionExpression stdDev = FilterFactoryFinder.createFilterFactory().createFunctionExpression("StandardDeviation");
        assertNotNull(stdDev);
    }
    
    public void testGetName() {
        FunctionExpression equInt = FilterFactoryFinder.createFilterFactory().createFunctionExpression("StandardDeviation");
        System.out.println("testGetName");
        assertEquals("StandardDeviation",equInt.getName());
    }
    
    public void testSetNumberOfClasses() throws Exception{
        System.out.println("testSetNumberOfClasses");
        
        Expression classes = (Expression)builder.parse(dataType, "3");
        Expression exp = (Expression)builder.parse(dataType, "foo");
        StandardDeviationFunction func = (StandardDeviationFunction)fac.createFunctionExpression("StandardDeviation");
        func.setArgs(new Expression[]{exp,classes});
        assertEquals(3,func.getNumberOfClasses());
        classes = (Expression)builder.parse(dataType, "12");
        func.setArgs(new Expression[]{exp,classes});
        assertEquals(12,func.getNumberOfClasses());
    }
    
    public void testCalculateSlot() throws ParseException {
        System.out.println("testCalculateSlot");
        Expression classes = (Expression)builder.parse(dataType, "3");
        Expression exp = (Expression)builder.parse(dataType, "foo");
        FunctionExpression func = fac.createFunctionExpression("StandardDeviation");
        func.setArgs(new Expression[]{exp,classes});
        
        FeatureIterator list = fc.features();
        while(list.hasNext()){
            Feature f = list.next();
            int slot = ((Number)func.getValue(f)).intValue();
            System.out.println(slot);
        }
        
    }
    
    public void testGetValue() throws Exception{
        System.out.println("testGetValue");
        Expression classes = (Expression)builder.parse(dataType, "5");
        Expression exp = (Expression)builder.parse(dataType, "foo");
        FunctionExpression func = fac.createFunctionExpression("StandardDeviation");
        func.setArgs(new Expression[]{exp,classes});
        
        FeatureIterator list = fc.features();
        //feature 1
        Feature f = list.next();
        int slot = ((Number)func.getValue(f)).intValue();
        assertEquals(1, slot);
        //feature 2
        f = list.next();
        slot = ((Number)func.getValue(f)).intValue();
        assertEquals(4, slot);
        //feature 3
        f = list.next();
        slot = ((Number)func.getValue(f)).intValue();
        assertEquals(2, slot);
        //feature 4
        f = list.next();
        slot = ((Number)func.getValue(f)).intValue();
        assertEquals(2, slot);
        //feature 5
        f = list.next();
        slot = ((Number)func.getValue(f)).intValue();
        assertEquals(2, slot);
        //feature 6
        f = list.next();
        slot = ((Number)func.getValue(f)).intValue();
        assertEquals(3, slot);
        //feature 7
        f = list.next();
        slot = ((Number)func.getValue(f)).intValue();
        assertEquals(1, slot);
        //feature 8
        f = list.next();
        slot = ((Number)func.getValue(f)).intValue();
        assertEquals(1, slot);
    }
    
    
}
