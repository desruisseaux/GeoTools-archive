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
import org.opengis.referencing.operation.TransformException;

// Geotools dependencies
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.GeneralDirectPosition;


/**
 * Base class for transform test cases. This class is not a test in itself;
 * only subclasses will be.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class TestTransform extends TestCase {
    /**
     * Small values for comparaisons of floating point numbers after transformations.
     */
    private static final double EPS = 1E-6;

    /**
     * The default datum factory.
     */
    protected static final DatumFactory datumFactory = FactoryFinder.getDatumFactory(null);

    /**
     * The default coordinate reference system factory.
     */
    protected static final CRSFactory crsFactory = FactoryFinder.getCRSFactory(null);

    /**
     * The default math transform factory.
     */
    protected static final MathTransformFactory mtFactory = FactoryFinder.getMathTransformFactory(null);

    /**
     * The default transformations factory.
     */
    protected static final CoordinateOperationFactory opFactory = FactoryFinder.getCoordinateOperationFactory(null);
    
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
     * Transforms a two-dimensional point and compare the result with the expected value.
     *
     * @param transform The transform to test.
     * @param  x The x value to transform.
     * @param  y The y value to transform.
     * @param ex The expected x value.
     * @param ey The expected y value.
     */
    public static void assertTransformEquals2_2(final MathTransform transform,
                                                final double  x, final double  y,
                                                final double ex, final double ey)
            throws TransformException
    {
        final DirectPosition2D source = new DirectPosition2D(x,y);
        final DirectPosition2D target = new DirectPosition2D();
        assertSame(target, transform.transform(source, target));
        final String message = "Expected ("+ex+", "+ey+"), "+
                               "transformed=("+target.x+", "+target.y+")";
        assertEquals(message, ex, target.x, EPS);
        assertEquals(message, ey, target.y, EPS);
    }

    /**
     * Transforms a three-dimensional point and compare the result with the expected value.
     *
     * @param transform The transform to test.
     * @param  x The x value to transform.
     * @param  y The y value to transform.
     * @param  z The z value to transform.
     * @param ex The expected x value.
     * @param ey The expected y value.
     * @param ez The expected z value.
     */
    public static void assertTransformEquals3_3(final MathTransform transform,
                                                final double  x, final double  y, final double  z,
                                                final double ex, final double ey, final double ez)
            throws TransformException
    {
        final GeneralDirectPosition source = new GeneralDirectPosition(x,y,z);
        final GeneralDirectPosition target = new GeneralDirectPosition(3);
        assertSame(target, transform.transform(source, target));
        final String message = "Expected ("+ex+", "+ey+", "+ez+"), "+
              "transformed=("+target.ordinates[0]+", "+target.ordinates[1]+", "+target.ordinates[2]+")";
        assertEquals(message, ex, target.ordinates[0], EPS);
        assertEquals(message, ey, target.ordinates[1], EPS);
        assertEquals(message, ez, target.ordinates[2], 1E-2); // Greater tolerance level for Z.
    }

    /**
     * Transforms a two-dimensional point and compare the result with the expected
     * three-dimensional value.
     *
     * @param transform The transform to test.
     * @param  x The x value to transform.
     * @param  y The y value to transform.
     * @param ex The expected x value.
     * @param ey The expected y value.
     * @param ez The expected z value.
     */
    public static void assertTransformEquals2_3(final MathTransform transform,
                                                final double  x, final double  y,
                                                final double ex, final double ey, final double ez)
            throws TransformException
    {
        final GeneralDirectPosition source = new GeneralDirectPosition(x,y);
        final GeneralDirectPosition target = new GeneralDirectPosition(3);
        assertSame(target, transform.transform(source, target));
        final String message = "Expected ("+ex+", "+ey+", "+ez+"), "+
              "transformed=("+target.ordinates[0]+", "+target.ordinates[1]+", "+target.ordinates[2]+")";
        assertEquals(message, ex, target.ordinates[0], EPS);
        assertEquals(message, ey, target.ordinates[1], EPS);
        assertEquals(message, ez, target.ordinates[2], 1E-2); // Greater tolerance level for Z.
    }

    /**
     * Transforms a three-dimensional point and compare the result with the expected
     * two-dimensional value.
     *
     * @param transform The transform to test.
     * @param  x The x value to transform.
     * @param  y The y value to transform.
     * @param  z The z value to transform.
     * @param ex The expected x value.
     * @param ey The expected y value.
     */
    public static void assertTransformEquals3_2(final MathTransform transform,
                                                final double  x, final double  y, final double  z,
                                                final double ex, final double ey)
            throws TransformException
    {
        final GeneralDirectPosition source = new GeneralDirectPosition(x,y,z);
        final GeneralDirectPosition target = new GeneralDirectPosition(2);
        assertSame(target, transform.transform(source, target));
        final String message = "Expected ("+ex+", "+ey+"), "+
              "transformed=("+target.ordinates[0]+", "+target.ordinates[1]+")";
        assertEquals(message, ex, target.ordinates[0], EPS);
        assertEquals(message, ey, target.ordinates[1], EPS);
    }    

    /**
     * Transforms a three-dimensional point and compare the result with the expected
     * one-dimensional value.
     *
     * @param transform The transform to test.
     * @param  x The x value to transform.
     * @param  y The y value to transform.
     * @param  z The z value to transform.
     * @param ez The expected z value.
     */
    public static void assertTransformEquals3_1(final MathTransform transform,
                                                final double  x, final double  y, final double  z,
                                                                                  final double ez)
            throws TransformException
    {
        final GeneralDirectPosition source = new GeneralDirectPosition(x,y,z);
        final GeneralDirectPosition target = new GeneralDirectPosition(1);
        assertSame(target, transform.transform(source, target));
        final String message = "Expected ("+ez+"), "+
              "transformed=("+target.ordinates[0]+")";
        assertEquals(message, ez, target.ordinates[0], 1E-2); // Greater tolerance level for Z.
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
