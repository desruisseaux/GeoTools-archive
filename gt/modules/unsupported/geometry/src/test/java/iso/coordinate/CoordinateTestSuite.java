package iso.coordinate;

import junit.framework.Test;
import junit.framework.TestSuite;

public class CoordinateTestSuite {
	
    public static Test suite() {

        TestSuite suite = new TestSuite();
	
        suite.addTestSuite(CoordinateFactoryTest.class);
        suite.addTestSuite(DirectPositionTest.class);
        suite.addTestSuite(EnvelopeTest.class);
        suite.addTestSuite(LineStringLineSegmentTest.class);
        suite.addTestSuite(PointArrayTest.class);

        return suite;
    }

    /**
     * Runs the test suite using the textual runner.
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
