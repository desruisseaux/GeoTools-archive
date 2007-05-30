package org.geotools.geometry.xml;

import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.Geometry;
import org.opengis.geometry.TransfiniteSet;

/**
 * @author <a href="mailto:joel@lggi.com">Joel Skelton</a>
 */
public class GeometryTestOperation extends Assert {
    private String operation;
    private String arg1;
    private String arg2;
    private String arg3;
    private Object expectedResult;
    private Object actualResult;
    private Map<String, OperationHandler> operationMap;

    private static final Logger LOG = Logger.getLogger("org.geotools.geometry");

    /**
     * Constructor
     * @param operation the operation to perform
     * @param arg1      first argument
     * @param arg2      second argument
     * @param arg3      third argument
     * @param expectedResult the passing result of the operation
     */
    public GeometryTestOperation(String operation, String arg1, String arg2, String arg3, Object expectedResult) {
        this.operation = operation;
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.arg3 = arg3;
        this.expectedResult = expectedResult;
        this.actualResult = null;

        setupOperationMap();

    }

    /**
     * Returns the operation to be performed.
     * 
     * @return String operation 
     */
    public String getOperation() {
        return operation;
    }
    
    /**
     * Sets the expected result.
     * 
     * @param expectedResult
     */
    public void setExpectedResult(Object expectedResult) {
        this.expectedResult = expectedResult;
    }
    
    private void setupOperationMap() {
        operationMap = new HashMap<String, OperationHandler>();
        OperationHandler noOpHandler = new NoOp();
        operationMap.put("contains", new ContainsOp());
        operationMap.put("convexhull", new ConvexHullOp());
        operationMap.put("difference", new DifferenceOp());
        operationMap.put("getboundary", new BoundaryOp());
        operationMap.put("getCentroid", new CentroidOp());
        operationMap.put("getInteriorPoint", new RepresentativePointOp());
        operationMap.put("intersection", new IntersectionOp());
        operationMap.put("intersects", new IntersectsOp());
        operationMap.put("isSimple", new IsSimpleOp());
        operationMap.put("symdifference", new SymmetricDifferenceOp());
        operationMap.put("union", new UnionOp());
    }

    protected Object getExpectedResult() {
		return expectedResult;
	}

	private Geometry setGeomArg(String s, Geometry a, Geometry b) {
        if (s.equalsIgnoreCase("a")) {
            return a;
        } else if (s.equalsIgnoreCase("b")) {
            return b;
        } else {
            return null;
        }
    }

    /**
     * Performs the contained operation
     * @param a argument object a
     * @param b argument object b
     * @return
     */
    public boolean run(Geometry a, Geometry b) {
        boolean result = false;
        result = operationMap.get(operation).doOperation(a, b);
        return result;
    }

    /**
     * The interface used for operation handlers
     */
    private abstract class OperationHandler {
        boolean doOperation(Geometry a, Geometry b) {
            return false;
        }

        protected boolean compareTransfiniteSetResult(TransfiniteSet result) {
        	if (expectedResult == null && result == null) {
        		return true;
        	}
            if (expectedResult instanceof TransfiniteSet) {
            	TransfiniteSet expect = (TransfiniteSet)expectedResult;
                return result.equals(expect);
            } else {
                return false;
            }
        }
        
    	protected boolean compareDirectPositionResult(DirectPosition result) {
            if (expectedResult instanceof DirectPosition) {
            	DirectPosition expect = (DirectPosition)expectedResult;
                return result.equals(expect);
            } else {
                return false;
            }
    	}
        
    }

    /**
     * Class defining a null operation
     */
    private class NoOp extends OperationHandler {
        public boolean doOperation(Geometry a, Geometry b) {
            return false;
        }
    }

    /**
     * Class defining the "contains" operation
     */
    private class ContainsOp extends OperationHandler {
        /**
         * The actual working method of the operation.
         * @param a Geometry object
         * @param b Geometry Object
         * @return a boolean indicating whether the result matched the expectation
         */
        public boolean doOperation(Geometry a, Geometry b) {
            Boolean expected = (Boolean)expectedResult;
            Geometry geom1 = setGeomArg(arg1, a, b);
            Geometry geom2 = setGeomArg(arg2, a, b);
            actualResult = geom1.contains(geom2);
            return actualResult == expected;
        }
    }

    /**
     * Class defining the "intersects" operation
     */
    private class IntersectsOp extends OperationHandler {
        /**
         * The actual working method of the operation.
         * @param a Geometry object
         * @param b Geometry Object
         * @return a boolean indicating whether the result matched the expectation
         */
        public boolean doOperation(Geometry a, Geometry b) {
            Boolean expected = (Boolean)expectedResult;
            Geometry geom1 = setGeomArg(arg1, a, b);
            Geometry geom2 = setGeomArg(arg2, a, b);
            actualResult = geom1.intersects(geom2);
            return actualResult == expected;
        }
    }

    /**
     * Class defining the "intersects" operation
     */
    private class IsSimpleOp extends OperationHandler {
        /**
         * The actual working method of the operation.
         * @param a Geometry object
         * @param b Geometry Object (not used)
         * @return a boolean indicating whether object A is simple
         */
        public boolean doOperation(Geometry a, Geometry b) {
            Boolean expected = (Boolean)expectedResult;
            Geometry geom1 = setGeomArg(arg1, a, b);
            actualResult = geom1.isSimple();
            return actualResult == expected;
        }
    }

    

    /**
     * Class defining the "intersects" operation
     */
    private class IntersectionOp extends OperationHandler {
        /**
         * performs the intersection on the two arguments
         * @param a Geometry object
         * @param b Geometry Object
         * @return a boolean indicating whether the result matched the expectation
         */
        public boolean doOperation(Geometry a, Geometry b) {
            Geometry geom1 = setGeomArg(arg1, a, b);
            Geometry geom2 = setGeomArg(arg2, a, b);
            TransfiniteSet result = geom1.intersection(geom2);
            actualResult = result;
            return compareTransfiniteSetResult(result);
        }
    }

    /**
     * Class defining the boundary operation
     */
    private class BoundaryOp extends OperationHandler {
        /**
         * Calculates the boundary of object A
         * @param a Geometry object
         * @param b Geometry Object (not used)
         * @return a boolean indicating whether the result matched the expectation
         */
        public boolean doOperation(Geometry a, Geometry b) {
            Geometry geom1 = setGeomArg(arg1, a, b);
            TransfiniteSet result = geom1.getBoundary();
            actualResult = result;
            return compareTransfiniteSetResult(result);
        }
    }    

    /**
     * Class defining the centroid operation
     */
    private class CentroidOp extends OperationHandler {
        /**
         * Calculates the centroid of object A
         * @param a Geometry object
         * @param b Geometry Object (not used)
         * @return a boolean indicating whether the result matched the expectation
         */
        public boolean doOperation(Geometry a, Geometry b) {
            Geometry geom1 = setGeomArg(arg1, a, b);
            DirectPosition result = geom1.getCentroid();
            actualResult = result;
            return compareDirectPositionResult(result);
        }
    }    
    
    /**
     * Class defining the centroid operation
     */
    private class RepresentativePointOp extends OperationHandler {
        /**
         * Calculates a representative point for object A
         * @param a Geometry object
         * @param b Geometry Object (not used)
         * @return a boolean indicating whether the result matched the expectation
         */
        public boolean doOperation(Geometry a, Geometry b) {
            Geometry geom1 = setGeomArg(arg1, a, b);
            DirectPosition result = geom1.getRepresentativePoint();
            actualResult = result;
            return compareDirectPositionResult(result);
        }
    }    
    
        
    
    /**
     * Class defining the boundary operation
     */
    private class ConvexHullOp extends OperationHandler {
        /**
         * Calculates the convex hull of object A
         * @param a Geometry object
         * @param b Geometry Object (not used)
         * @return a boolean indicating whether the result matched the expectation
         */
        public boolean doOperation(Geometry a, Geometry b) {
            Geometry geom1 = setGeomArg(arg1, a, b);
            TransfiniteSet result = geom1.getConvexHull();
            actualResult = result;
            return compareTransfiniteSetResult(result);
        }
    }    

    /**
     * Class defining the boundary operation
     */
    private class DifferenceOp extends OperationHandler {
        /**
         * Calculates the difference of objects A and B (A - B)
         * @param a Geometry object
         * @param b Geometry object
         * @return a boolean indicating whether the result matched the expectation
         */
        public boolean doOperation(Geometry a, Geometry b) {
            Geometry geom1 = setGeomArg(arg1, a, b);
            Geometry geom2 = setGeomArg(arg2, a, b);
            TransfiniteSet result = geom1.difference(geom2);
            actualResult = result;
            return compareTransfiniteSetResult(result);
        }
    }    

    /**
     * Class defining the boundary operation
     */
    private class SymmetricDifferenceOp extends OperationHandler {
        /**
         * Calculates the symmetric difference of objects A and B (A - B)
         * @param a Geometry object
         * @param b Geometry object
         * @return a boolean indicating whether the result matched the expectation
         */
        public boolean doOperation(Geometry a, Geometry b) {
            Geometry geom1 = setGeomArg(arg1, a, b);
            Geometry geom2 = setGeomArg(arg2, a, b);
            TransfiniteSet result = geom1.symmetricDifference(geom2);
            actualResult = result;
            return compareTransfiniteSetResult(result);
        }
    }
        
    /**
     * Class defining the boundary operation
     */
    private class UnionOp extends OperationHandler {
        /**
         * Calculates the union of objects A and B (A + B)
         * @param a Geometry object
         * @param b Geometry object
         * @return a boolean indicating whether the result matched the expectation
         */
        public boolean doOperation(Geometry a, Geometry b) {
            Geometry geom1 = setGeomArg(arg1, a, b);
            Geometry geom2 = setGeomArg(arg2, a, b);
            TransfiniteSet result = geom1.union(geom2);
            actualResult = result;
            return compareTransfiniteSetResult(result);
        }
    }
    
    /**
     * Returns a string describing the operation for logging
     * @return a formatted string
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append( operation );        
        if( arg1 != null ){
            buf.append( " arg1=");
            buf.append( arg1 );
        }
        if( arg2 != null ){
            buf.append( " arg2=");
            buf.append( arg2 );
        }
        if( arg3 != null ){
            buf.append( " arg3=");
            buf.append( arg3 );
        }
        buf.append( " expected ");
        buf.append( expectedResult );
        return buf.toString();  
    }
    
    public void runTest(Geometry a, Geometry b) {       
        boolean test = operationMap.get(operation).doOperation(a, b);
        assertTrue(toString() + " but was " + actualResult, test);
    }

}
