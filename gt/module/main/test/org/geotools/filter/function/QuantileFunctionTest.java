/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, Geotools Project Managment Committee (PMC)
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

import org.geotools.data.DataUtilities;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.visitor.FeatureVisitor;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.expression.Expression;
import org.geotools.filter.expression.ExpressionType;
import org.geotools.filter.expression.FunctionExpression;
import org.geotools.filter.expression.MathExpression;
import org.geotools.filter.parser.ParseException;

/**
 * 
 * @author Cory Horner, Refractions Research Inc.
 *
 * @source $URL$
 */
public class QuantileFunctionTest extends FunctionTestSupport {
   
    
    public QuantileFunctionTest(String testName) {
        super(testName);
    }
    
    protected void tearDown() throws java.lang.Exception {
    }
    
    public static junit.framework.Test suite() {
        junit.framework.TestSuite suite = new junit.framework.TestSuite(QuantileFunctionTest.class);
        
        return suite;
    }
    
    public void testInstance() {
        FunctionExpression equInt = FilterFactoryFinder.createFilterFactory().createFunctionExpression("Quantile");
        assertNotNull(equInt);
    }
    
    public void testGetName() {
        FunctionExpression qInt = FilterFactoryFinder.createFilterFactory().createFunctionExpression("Quantile");
        System.out.println("testGetName");
        assertEquals("Quantile",qInt.getName());
    }
    
    public void testSetNumberOfClasses() throws Exception{
        System.out.println("testSetNumberOfClasses");
        
        Expression classes = (Expression)builder.parse(dataType, "3");
        Expression exp = (Expression)builder.parse(dataType, "foo");
        QuantileFunction func = (QuantileFunction)fac.createFunctionExpression("Quantile");
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
        FunctionExpression func = fac.createFunctionExpression("Quantile");
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
        Expression classes = (Expression)builder.parse(dataType, "2");
        Expression exp = (Expression)builder.parse(dataType, "foo");
        FunctionExpression func = fac.createFunctionExpression("Quantile");
        func.setArgs(new Expression[]{exp,classes});
        
        FeatureIterator list = fc.features();
        while(list.hasNext()){
            Feature f = list.next();
            int slot = ((Number)func.getValue(f)).intValue();
            int value = ((Number)f.getAttribute("foo")).intValue();
            System.out.println(slot+"("+value+")");
            //TODO: test the values are put in the correct slots
        }
    }
    
    public void testNullNaNHandling() throws Exception {
    	//setup
    	System.out.println("testNullNaNHandling");
    	FunctionExpression func = fac.createFunctionExpression("Quantile");
    	QuantileFunction qf = (QuantileFunction) func;
 
    	//create a feature collection
    	FeatureType ft = DataUtilities.createType("classification.nullnan",
        "id:0,foo:int,bar:double");
    	Integer iVal[] = new Integer[] {
    			new Integer(0),
    			new Integer(0),
    			new Integer(0),
    			new Integer(13),
    			new Integer(13),
    			new Integer(13),
    			null,
    			null,
    			null};
    	Double dVal[] = new Double[] {
    			new Double(0.0),
    			new Double(50.01),
    			null,
    			new Double(0.0),
    			new Double(50.01),
    			null,
    			new Double(0.0),
    			new Double(50.01),
    			null};

    	Feature[] testFeatures = new Feature[iVal.length];

    	for(int i=0; i< iVal.length; i++){
    		testFeatures[i] = ft.create(new Object[] {
    				new Integer(i+1),
    				iVal[i],
    				dVal[i],
    		},"nantest.t"+(i+1));
    	}
    	MemoryDataStore store = new MemoryDataStore();
    	store.createSchema(ft);
    	store.addFeatures(testFeatures);
    	FeatureCollection thisFC = store.getFeatureSource("nullnan").getFeatures().collection();

    	//create the expression
        MathExpression divide = fac.createMathExpression(ExpressionType.MATH_DIVIDE);
        divide.addLeftValue((Expression)builder.parse(dataType, "foo"));
        divide.addRightValue((Expression)builder.parse(dataType, "bar"));
    	
    	qf.setNumberOfClasses(3);
    	qf.setCollection(thisFC);
    	qf.setExpression(divide);
    	
    	for (int j = 0; j < qf.classNum; j++) {
    		System.out.println(qf.getMin(j)+".."+qf.getMax(j));
    	}

    	thisFC.accepts(new FeatureVisitor() {

			public void visit(Feature arg0) {
				System.out.println(arg0.toString());
			}
    	}, null);
    }
}
