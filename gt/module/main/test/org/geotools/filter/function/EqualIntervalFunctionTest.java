/*
 * EqualIntervalFunctionTest.java
 * JUnit based test
 *
 * Created on 09 March 2005, 12:43
 */

package org.geotools.filter.function;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import junit.framework.*;
import org.geotools.data.DataUtilities;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.filter.Expression;
import org.geotools.filter.ExpressionBuilder;
import org.geotools.filter.ExpressionSAXParser;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFilter;
import org.geotools.filter.FunctionExpression;
import org.geotools.filter.TestFilterHandler;
import org.geotools.filter.function.EqualIntervalFunction;
import org.geotools.filter.parser.ParseException;
import org.geotools.gml.GMLFilterDocument;
import org.geotools.gml.GMLFilterGeometry;
import org.geotools.resources.TestData;
import org.xml.sax.helpers.ParserAdapter;

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
        FunctionExpression equInt = FilterFactory.createFilterFactory().createFunctionExpression("EqualInterval");
        assertNotNull(equInt);
    }
    
    
    /**
     * Test of getName method, of class org.geotools.filter.functions.EqualIntervalFunction.
     */
    public void testGetName() {
        FunctionExpression equInt = FilterFactory.createFilterFactory().createFunctionExpression("EqualInterval");
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
    
    
    
}
