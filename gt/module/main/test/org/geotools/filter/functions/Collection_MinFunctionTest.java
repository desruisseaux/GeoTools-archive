/*
 * Collection_MinFunctionTest.java
 *
 * Created on May 11, 2005, 9:21 PM
 */

package org.geotools.filter.functions;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureIterator;
import org.geotools.filter.Expression;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FunctionExpression;

/**
 *
 * @author James
 */
public class Collection_MinFunctionTest extends FunctionTestSupport{
    
    /** Creates a new instance of Collection_MinFunctionTest */
    public Collection_MinFunctionTest(String testName) {
        super(testName);
    }
    
    
    public void testInstance() {
        FunctionExpression cmin = FilterFactory.createFilterFactory().createFunctionExpression("Collection_Min");
        assertNotNull(cmin);
    }
    
     /**
     * Test of getValue method, of class org.geotools.filter.functions.EqualIntervalFunction.
     */
    public void testGetValue() throws Exception{
        System.out.println("testGetValue");
        Expression exp = (Expression)builder.parse(dataType, "foo");
        FunctionExpression func = fac.createFunctionExpression("Collection_Min");
        func.setArgs(new Expression[]{exp});
        
        FeatureIterator list = fc.features();
        while(list.hasNext()){
            Feature f = list.next();
            double min = ((Number)func.getValue(f)).doubleValue();
           
            assertEquals(4,min,0);
            
        }
    }
    
}
