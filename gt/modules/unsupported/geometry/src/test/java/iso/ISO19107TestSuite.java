package iso;

import iso.aggregate.AggregateTestSuite;
import iso.complex.ComplexTestSuite;
import iso.coordinate.CoordinateTestSuite;
import iso.operations.OperationsTestSuite;
import iso.primitive.PrimitiveTestSuite;
import iso.util.AlgorithmND.AlgorithmNDTestSuite;
import junit.framework.Test;
import junit.framework.TestSuite;

public class ISO19107TestSuite {
	
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