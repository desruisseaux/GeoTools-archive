/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2002, Institut de Recherche pour le Développement
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
package org.geotools.referencing.operation;

// J2SE dependencies
import java.util.Random;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// OpenGIS dependencies
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.datum.DatumFactory;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform1D;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.referencing.operation.OperationNotFoundException;

// Geotools dependencies
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.crs.EngineeringCRS;
import org.geotools.referencing.crs.GeographicCRS;


/**
 * Base class for transformation test classes.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class TransformationTest extends TestCase {
    /**
     * The default datum factory.
     */
    protected static final DatumFactory datumFactory = FactoryFinder.getDatumFactory();

    /**
     * The default coordinate reference system factory.
     */
    protected static final CRSFactory crsFactory = FactoryFinder.getCRSFactory();

    /**
     * The default math transform factory.
     */
    protected static final MathTransformFactory mtFactory = FactoryFinder.getMathTransformFactory();

    /**
     * The default transformations factory.
     */
    protected static final CoordinateOperationFactory opFactory = FactoryFinder.getCoordinateOperationFactory();
    
    /**
     * Random numbers generator.
     */
    protected static final Random random = new Random(-3531834320875149028L);

    /**
     * Constructs a test case with the given name.
     */
    public TransformationTest(final String name) {
        super(name);
    }
    
    /**
     * Uses reflection to dynamically create a test suite containing all 
     * the <code>testXXX()</code> methods - from the JUnit FAQ.
     */
    public static Test suite() {
        return new TestSuite(TransformationTest.class);
    }
    
    /**
     * Runs the tests with the textual test runner.
     */
    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Quick self test, in part to give this test suite a test
     * and also to test the internal method.
     */
    public void testAssertPointsEqual(){
        String name = "self test";
        double a[]     = {10,   10  };
        double b[]     = {10.1, 10.1};
        double delta[] = { 0.2,  0.2};
        assertPointsEqual(name, a, b, delta);
    }

    /**
     * Make sure that <code>createOperation(sourceCRS, targetCRS)</code>
     * returns an identity transform when <code>sourceCRS</code> and <code>targetCRS</code>
     * are identical, and tests the generic CRS.
     */
    public void testGenericTransform() throws FactoryException {
        assertTrue(opFactory.createOperation(GeographicCRS.WGS84,
                   GeographicCRS.WGS84).getMathTransform().isIdentity());
        assertTrue(opFactory.createOperation(EngineeringCRS.CARTESIAN_2D,
                   EngineeringCRS.CARTESIAN_2D).getMathTransform().isIdentity());
        assertTrue(opFactory.createOperation(EngineeringCRS.CARTESIAN_3D,
                   EngineeringCRS.CARTESIAN_3D).getMathTransform().isIdentity());
        assertTrue(opFactory.createOperation(EngineeringCRS.GENERIC_2D,
                   EngineeringCRS.GENERIC_2D).getMathTransform().isIdentity());
        assertTrue(opFactory.createOperation(EngineeringCRS.GENERIC_2D,
                   EngineeringCRS.CARTESIAN_2D).getMathTransform().isIdentity());
        assertTrue(opFactory.createOperation(EngineeringCRS.CARTESIAN_2D,
                   EngineeringCRS.GENERIC_2D).getMathTransform().isIdentity());
        assertTrue(opFactory.createOperation(GeographicCRS.WGS84,
                   EngineeringCRS.GENERIC_2D).getMathTransform().isIdentity());
        assertTrue(opFactory.createOperation(EngineeringCRS.GENERIC_2D,
                   GeographicCRS.WGS84).getMathTransform().isIdentity());
        try {
            opFactory.createOperation(EngineeringCRS.CARTESIAN_2D,
                                      GeographicCRS.WGS84);
            fail();
        } catch (OperationNotFoundException exception) {
            // This is the expected exception.
        }
        try {
            opFactory.createOperation(GeographicCRS.WGS84,
                                      EngineeringCRS.CARTESIAN_2D);
            fail();
        } catch (OperationNotFoundException exception) {
            // This is the expected exception.
        }
    }

    /**
     * Tests one transformation which imply datum shift.
     */
    public void testDatumShift() throws Exception {
        final CoordinateReferenceSystem sourceCRS = crsFactory.createFromWKT(
                "GEOGCS[\"NTF (Paris)\",\n"                                             +
                "  DATUM[\"Nouvelle_Triangulation_Francaise\",\n"                       +
                "    SPHEROID[\"Clarke 1880 (IGN)\", 6378249.2, 293.466021293627,\n"    +
                "      AUTHORITY[\"EPSG\",\"7011\"]],\n"                                +
                "    TOWGS84[-168,-60,320,0,0,0,0],\n"                                  +
                "    AUTHORITY[\"EPSG\",\"6275\"]],\n"                                  +
                "  PRIMEM[\"Paris\", 2.5969213, AUTHORITY[\"EPSG\",\"8903\"]],\n"       +
                "  UNIT[\"grad\", 0.015707963267949, AUTHORITY[\"EPSG\", \"9105\"]],\n" +
                "  AXIS[\"Lat\",NORTH],\n"+
                "  AXIS[\"Long\",EAST],\n" +
                "  AUTHORITY[\"EPSG\",\"4807\"]]");

        final CoordinateReferenceSystem targetCRS = crsFactory.createFromWKT(
                "GEOGCS[\"WGS84\",\n"                                   +
                "  DATUM[\"WGS84\",\n"                                  +
                "    SPHEROID[\"WGS84\", 6378137.0, 298.257223563]],\n" +
                "  PRIMEM[\"Greenwich\", 0.0],\n"                       +
                "  UNIT[\"degree\", 0.017453292519943295],\n"+""        +
                "  AXIS[\"Longitude\",EAST],"                           +
                "  AXIS[\"Latitude\",NORTH]]");

        final CoordinateOperation operation = opFactory.createOperation(sourceCRS, targetCRS);
        assertSame(sourceCRS, operation.getSourceCRS());
        assertSame(targetCRS, operation.getTargetCRS());
    }
    
    /**
     * Convenience method for checking if a boolean value is false.
     */
    public static void assertFalse(final boolean value) {
        assertTrue(!value);
    }

    /**
     * Returns <code>true</code> if the specified number is real
     * (neither NaN or infinite).
     */
    public static boolean isReal(final double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

    /**
     * Verify that the specified transform implements {@link MathTransform1D}
     * or {@link MathTransform2D} as needed.
     *
     * @param transform The transform to test.
     */
    public static void assertInterfaced(final MathTransform transform) {
        int dim = transform.getSourceDimensions();
        if (transform.getTargetDimensions() != dim) {
            dim = 0;
        }
        assertTrue("MathTransform1D", (dim==1) == (transform instanceof MathTransform1D));
        assertTrue("MathTransform2D", (dim==2) == (transform instanceof MathTransform2D));
    }

    /**
     * Compare two arrays of points.
     *
     * @param name      The name of the comparaison to be performed.
     * @param expected  The expected array of points.
     * @param actual    The actual array of points.
     * @param delta     The maximal difference tolerated in comparaisons for each dimension.
     *                  This array length must be equal to coordinate dimension (usually 1, 2 or 3).
     */
    public static void assertPointsEqual(final String   name,
                                         final double[] expected,
                                         final double[] actual,
                                         final double[] delta)
    {
        final int dimension = delta.length;
        final int stop = Math.min(expected.length, actual.length)/dimension * dimension;
        assertEquals("Array length for expected points", stop, expected.length);
        assertEquals("Array length for actual points",   stop,   actual.length);
        final StringBuffer buffer = new StringBuffer(name);
        buffer.append(": point[");
        final int start = buffer.length();
        for (int i=0; i<stop; i++) {
            buffer.setLength(start);
            buffer.append(i/dimension);
            buffer.append(", dimension ");
            buffer.append(i % dimension);
            buffer.append(" of ");
            buffer.append(dimension);
            buffer.append(']');
            if (isReal(expected[i])) {
                // The "two steps" method in ConcatenatedTransformTest sometime produces
                // random NaN numbers. This "two steps" is used only for comparaison purpose;
                // the "real" (tested) method work better.
                assertEquals(buffer.toString(), expected[i], actual[i], delta[i % dimension]);
            }
        }
    }
}
