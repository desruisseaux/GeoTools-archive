/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, Geotools Project Managment Committee (PMC)
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
package org.geotools.filter.function;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureIterator;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.expression.Expression;
import org.geotools.filter.expression.FunctionExpression;
import org.geotools.filter.parser.ParseException;

/**
 *
 * @author James
 * @source $URL$
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
}
