/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.data.shapefile;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 *
 * @source $URL$
 * @version $Id$
 * @author James McGill
 */
public class ShapefileSuite extends TestCase {
    
  public ShapefileSuite(String testName) {
    super(testName);
  }
    
  public static void main(java.lang.String[] args) {
    TestCaseSupport.verbose = true;
    junit.textui.TestRunner.run(suite());
  }
    
  public static Test suite() {
    TestSuite suite = new TestSuite("All ShapefileDataSource Tests");

    suite.addTestSuite(DbaseFileTest.class);
    suite.addTestSuite(ShapefileTest.class);
    suite.addTestSuite(ShapefileReadWriteTest.class);
    suite.addTestSuite(ShapefileDataStoreTest.class);
        
    return suite;
  }
}
