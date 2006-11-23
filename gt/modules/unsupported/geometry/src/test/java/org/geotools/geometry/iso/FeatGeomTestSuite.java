package org.geotools.geometry.iso;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.geotools.geometry.iso.aggregate.AggregateTestSuite;
import org.geotools.geometry.iso.complex.ComplexTestSuite;
import org.geotools.geometry.iso.coordinate.CoordinateTestSuite;
import org.geotools.geometry.iso.operations.OperationsTestSuite;
import org.geotools.geometry.iso.primitive.PrimitiveTestSuite;
import org.geotools.geometry.iso.util.AlgorithmND.AlgorithmNDTestSuite;

public class FeatGeomTestSuite {
	
    public static Test suite() {

        TestSuite suite = new TestSuite();
	
        // *** COORDINATES
        suite.addTest(CoordinateTestSuite.suite());
        
        // *** PRIMITIVES
        suite.addTest(PrimitiveTestSuite.suite());

        // *** COMPLEXES
        suite.addTest(ComplexTestSuite.suite());

        // *** AGGREGATE
        suite.addTest(AggregateTestSuite.suite());

        // *** OPERATIONS
        suite.addTest(OperationsTestSuite.suite());

        // *** UTILS
        suite.addTest(AlgorithmNDTestSuite.suite());

        
        return suite;
    }

    /**
     * Runs the test suite using the textual runner.
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}