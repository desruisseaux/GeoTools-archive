/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, Geotools Project Managment Committee (PMC)
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
package org.geotools;

import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.feature.FeatureFlatTest;
import org.geotools.filter.ExpressionTest;
import org.geotools.filter.FilterEqualsTest;
import org.geotools.filter.FilterTest;
import org.geotools.styling.StyleFactoryImplTest;
import org.geotools.styling.TextSymbolTest;
/**
 *
 * @author jamesm
 * @source $URL$
 */                                
public class DefaultCoreSuite extends TestCase {
    private static final Logger LOGGER = Logger.getLogger("org.geotools.core");
    public DefaultCoreSuite(java.lang.String testName) {
        super(testName);
    }        
    
    public static void main(java.lang.String[] args) {
        org.geotools.util.Logging.GEOTOOLS.forceMonolineConsoleOutput();
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        //_log = Logger.getLogger(DefaultCoreSuite.class);
       
        
        TestSuite suite = new TestSuite("All core tests");                
        suite.addTestSuite(FeatureFlatTest.class);
        suite.addTestSuite(ExpressionTest.class);
        suite.addTestSuite(FilterEqualsTest.class);
        suite.addTestSuite(FilterTest.class);
        suite.addTestSuite(StyleFactoryImplTest.class);
        suite.addTestSuite(TextSymbolTest.class); 
        return suite;
    }
}
