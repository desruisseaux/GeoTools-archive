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
 *
 *    Created on May 11, 2005, 9:02 PM
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
 * @source $URL$
 */
public class FunctionTestSupport extends TestCase {
    
    protected FeatureCollection featureCollection;
    protected FilterFactory fac = FilterFactoryFinder.createFilterFactory();
    protected ExpressionBuilder builder = new ExpressionBuilder();
    protected FeatureType dataType;
    protected Feature[] testFeatures;
    
    /** Creates a new instance of FunctionTestSupport */
    public FunctionTestSupport(String testName) {
        super(testName);
    }
    
    
    protected void setUp() throws java.lang.Exception {
        dataType = DataUtilities.createType("classification.test1",
                "id:0,foo:int,bar:double");
        
        int iVal[] = new int[]{4,90,20,43,29,61,8,12};
        double dVal[] = new double[]{2.5,80.433,24.5,9.75,18,53,43.2,16};
        
        testFeatures = new Feature[iVal.length];
        
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
        
        featureCollection = store.getFeatureSource("test1").getFeatures();
    }
    
    public void testEmpty() {
        //to make tests pass
    }
}
