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
import junit.framework.TestCase;

// Geotools dependencies
import org.geotools.referencing.FactoryFinder;

// OpenGIS dependencis
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.datum.DatumFactory;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform1D;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.MathTransformFactory;



/**
 * Base class for transform test cases. This class is not a test in itself;
 * only subclasses will be.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class TestTransform extends TestCase {
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
    public TestTransform(final String name) {
        super(name);
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
