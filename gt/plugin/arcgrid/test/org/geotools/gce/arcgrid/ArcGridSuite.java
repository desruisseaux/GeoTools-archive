/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
package org.geotools.gce.arcgrid;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * DOCUMENT ME!
 *
 * @author aaime
 */
public class ArcGridSuite extends TestCase {
    public ArcGridSuite(String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("All ArcGridDataSource Tests");

        suite.addTestSuite(ArcGridHeaderTest.class);
        suite.addTestSuite(ArcGridRenderTest.class);
        suite.addTestSuite(ArcGridReaderTest.class);
        suite.addTestSuite(ArcGridReadWriteTest.class);
        suite.addTestSuite(ServiceTest.class);

        return suite;
    }
}
