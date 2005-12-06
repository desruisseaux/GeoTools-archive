/*
 * FunctionTestSupport.java
 *
 * Created on May 11, 2005, 9:02 PM
 */

package org.geotools.filter.function;

import org.geotools.data.DataUtilities;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.feature.Feature;
import junit.framework.TestCase;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.filter.ExpressionBuilder;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;

/**
 *
 * @author James
 */
public class FunctionTestSupport extends TestCase {
    
    protected FeatureCollection fc;
    protected FilterFactory fac = FilterFactoryFinder.createFilterFactory();
    protected ExpressionBuilder builder = new ExpressionBuilder();
    protected FeatureType dataType;
    
    /** Creates a new instance of FunctionTestSupport */
    public FunctionTestSupport(String testName) {
        super(testName);
    }
    
    
    protected void setUp() throws java.lang.Exception {
        dataType = DataUtilities.createType("classification.test1",
                "id:0,foo:int,bar:double");
        
        
        double dVal[] = new double[]{2.5,80.433,24.5,9.75,18,53,43.2,16};
        int iVal[] = new int[]{4,90,20,43,29,61,8,12};
        
        Feature[] testFeatures = new Feature[iVal.length];
        
        for(int i=0; i< iVal.length; i++){
            testFeatures[i] = dataType.create(new Object[] {
                new Integer(i+1),
                        new Integer(iVal[i]),
                        new Double(dVal[i]),
            },"classification.t"+(i+1));
            
            
        }
        
        MemoryDataStore store = new MemoryDataStore();
        store.createSchema(dataType);
        store.addFeatures(testFeatures);
        
        fc = store.getFeatureSource("test1").getFeatures().collection();
        
    }
}
