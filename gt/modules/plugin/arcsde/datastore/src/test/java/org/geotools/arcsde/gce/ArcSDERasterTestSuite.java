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
package org.geotools.arcsde.gce;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ArcSDERasterTestSuite {

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite();

        suite.addTestSuite(BandCopierTest.class);
        suite.addTestSuite(ArcSDEPyramidTest.class);
        suite.addTestSuite(ArcSDEImageIOReaderFunctionalTest.class);
        suite.addTestSuite(ArcSDEImageIOReaderOutputFormatsTest.class);

        suite.addTestSuite(ArcSDEGCReaderSpatialTest.class);
        suite.addTestSuite(ArcSDEGCReaderSymbolizedTest.class);

        return suite;
    }
}
