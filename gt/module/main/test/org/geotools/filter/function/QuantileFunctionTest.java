package org.geotools.filter.function;

import org.geotools.data.DataUtilities;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.filter.Expression;
import org.geotools.filter.ExpressionType;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.FunctionExpression;
import org.geotools.filter.MathExpression;
import org.geotools.filter.parser.ParseException;

/**
 * 
 * @author Cory Horner, Refractions Research Inc.
 *
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
    
    /**
     * Test of getName method, of class org.geotools.filter.functions.EqualIntervalFunction.
     */
    public void testInstance() {
        FunctionExpression equInt = FilterFactoryFinder.createFilterFactory().createFunctionExpression("Quantile");
        assertNotNull(equInt);
    }
    
    
    /**
     * Test of getName method, of class org.geotools.filter.functions.EqualIntervalFunction.
     */
    public void testGetName() {
        FunctionExpression qInt = FilterFactoryFinder.createFilterFactory().createFunctionExpression("Quantile");
        System.out.println("testGetName");
        assertEquals("Quantile",qInt.getName());
    }
    
    /**
     * Test of setNumberOfClasses method, of class org.geotools.filter.functions.EqualIntervalFunction.
     */
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
    
    /**
     * Test of calculateSlot method, of class org.geotools.filter.functions.EqualIntervalFunction.
     */
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
    
    /**
     * Test of getValue method, of class org.geotools.filter.functions.EqualIntervalFunction.
     */
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
//            if(value < 46){
//                assertEquals(0,slot);
//            } else{
//                assertEquals(1,slot);
//            }
            //TODO: test the values are put in the correct slots
        }
    }
    
    public void testRound() throws Exception{
    	System.out.println("testRound");
    	FunctionExpression func = fac.createFunctionExpression("Quantile");
    	QuantileFunction qf = (QuantileFunction) func;
        double val = qf.round(100.0, 0);
        System.out.println("round 0 digits, 100.0 --> "+val);
        assertEquals(100.0, val, 0);
        val = qf.round(1.12, 1);
        System.out.println("round 1 digit, 1.12 --> "+val);
        assertEquals(1.1, val, 0);
        val = qf.round(0.34523, 2);
        System.out.println("round 2 digits, 0.34523 --> "+val);
        assertEquals(0.35, val, 0);
    }
    
    public void testNullNaNHandling() throws Exception {
    	//setup
    	System.out.println("testNullNaNHandling");
    	FunctionExpression func = fac.createFunctionExpression("Quantile");
    	QuantileFunction qf = (QuantileFunction) func;
 
    	//create a feature collection
    	FeatureType ft = DataUtilities.createType("classification.nullnan",
        "id:0,foo:int,bar:double");
    	int iVal[] = new int[]{1,2,3,4,5};
    	double dVal[] = new double[]{0.0, 0.1, 1.0, 10.0, 100.0};

    	Feature[] testFeatures = new Feature[iVal.length+1];

    	for(int i=0; i< iVal.length; i++){
    		testFeatures[i] = ft.create(new Object[] {
    				new Integer(i+1),
    				new Integer(iVal[i]),
    				new Double(dVal[i]),
    		},"classification.t"+(i+1));
    	}
        //add one more null feature
    	int i = iVal.length;
    	testFeatures[i] = ft.create(new Object[] {
    			new Integer(i+1), null, new Double(1000.0)
    	},"classification.t"+(i+1));
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
    	
    	
    	for (int j = 0; j < 3; j++) {
    		System.out.println(qf.getMin(j)+".."+qf.getMax(j));
    	}
    	
    }
}
