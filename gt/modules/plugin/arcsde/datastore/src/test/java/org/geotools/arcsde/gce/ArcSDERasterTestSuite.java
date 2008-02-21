package org.geotools.arcsde.gce;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ArcSDERasterTestSuite {

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite();

        suite.addTestSuite(UnsignedByteBandCopierTest.class);
        suite.addTestSuite(ArcSDEPyramidTest.class);
        suite.addTestSuite(ArcSDEImageIOReaderFunctionalTest.class);
        suite.addTestSuite(ArcSDEImageIOReaderOutputFormatsTest.class);

        suite.addTestSuite(ArcSDEGCReaderSpatialTest.class);
        suite.addTestSuite(ArcSDEGCReaderSymbolizedTest.class);

        return suite;
    }
}
