/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2002, Geotools Project Managment Committee (PMC)
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
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.data.shapefile;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 *
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
        
    // this test must go first!!!
    suite.addTestSuite(TestCaseSupportTest.class);

    suite.addTestSuite(DbaseFileTest.class);
    suite.addTestSuite(ShapefileTest.class);
    suite.addTestSuite(ShapefileReadWriteTest.class);
    suite.addTestSuite(ShapefileDataStoreTest.class);
        
    return suite;
  }
}
