/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Management Committee (PMC)
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
package org.geotools.referencing.operation.transform;

// J2SE dependencies
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.Random;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// JAI dependencies
import javax.media.jai.Warp;
import javax.media.jai.WarpAffine;
import javax.media.jai.WarpCubic;
import javax.media.jai.WarpQuadratic;
import javax.media.jai.WarpPolynomial;

// OpenGIS dependencies
import org.opengis.referencing.operation.TransformException;

// Geotools dependencies
import org.geotools.resources.Utilities;


/**
 * Tests the {@link WarpTransform2D} class.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class WarpTransformTest extends TestCase {
    /**
     * Width and height of a pseudo-image source image.
     */
    private static final int WIDTH=1000, HEIGHT=2000;

    /**
     * Runs the tests with the textual test runner.
     */
    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(WarpTransformTest.class);
    }
    
    /**
     * Constructs a test case with the given name.
     */
    public WarpTransformTest(final String name) {
        super(name);
    }

    /**
     * Transforms in place a point. This is used for testing
     * affine, quadratic and cubic warp from know formulas.
     */
    private static interface Formula {
        String message();
        void transform(Point point);
    }

    /**
     * Constructs a warp and tests the transformations. Coefficients will be tested later
     * (by the caller).
     */
    private static WarpPolynomial executeTest(final Formula formula, final int degree, final float EPS)
            throws TransformException
    {
        /*
         * Creates a set of points and transform them according the formula supplied in argument.
         */
        final Random   random = new Random(-854734760285695284L);
        final Point[] sources = new Point[100];
        final Point[]    dest = new Point[sources.length];
        for (int i=0; i<dest.length; i++) {
            Point p;
            sources[i] = p = new Point(random.nextInt(WIDTH), random.nextInt(HEIGHT));
            dest   [i] = p = new Point(p);
            formula.transform(p);
        }
        /*
         * Gets the transform. We specify a bounding box which contains all points.
         */
        final Point ext = new Point(WIDTH,HEIGHT);
        formula.transform(ext);
        final WarpTransform2D transform = new WarpTransform2D(
                new Rectangle(0, 0, WIDTH, HEIGHT), sources, 0,
                new Rectangle(0, 0, ext.x, ext.y),  dest,    0,
                sources.length, degree);
        final WarpTransform2D inverse = (WarpTransform2D) transform.inverse();
        assertNotNull("WKT formatting test", transform.toString());
        /*
         * Checks Warp properties.
         */
        final Warp warp = transform.getWarp();
        assertTrue("Expected a polynomial warp but got "+Utilities.getShortClassName(warp),
                   warp instanceof WarpPolynomial);
        final WarpPolynomial poly = (WarpPolynomial) warp;
        /*
         * Compares transformations to the expected points.
         */
        for (int i=0; i<sources.length; i++) {
            final String  message  = formula.message() + " Point #" + i;
            final Point   source   = sources[i];
            final Point   expected = dest   [i];
            final Point2D computed = new Point2D.Double(source.x, source.y);
            assertSame  (message, computed, transform.transform(computed, computed));
            assertEquals(message, expected.x, computed.getX(), EPS*expected.x);
            assertEquals(message, expected.y, computed.getY(), EPS*expected.y);
            //
            // Try using transform(float[], ...)
            //
            if (true) {
                final float[] array = new float[] {source.x, source.y};
                transform.transform(array, 0, array, 0, 1);
                assertEquals(message, expected.x, array[0], EPS*expected.x);
                assertEquals(message, expected.y, array[1], EPS*expected.y);
            }
            //
            // Try using transform(double[], ...)
            //
            if (true) {
                final double[] array = new double[] {source.x, source.y};
                transform.transform(array, 0, array, 0, 1);
                assertEquals(message, expected.x, array[0], EPS*expected.x);
                assertEquals(message, expected.y, array[1], EPS*expected.y);
            }
            //
            // Tests inverse transform
            //
            if (degree == 1) {
                computed.setLocation(expected.x, expected.y);
                assertSame  (message, computed, inverse.transform(computed, computed));
                assertEquals(message, source.x, computed.getX(), EPS*expected.x);
                assertEquals(message, source.y, computed.getY(), EPS*expected.y);
            }
        }
        return poly;
    }

    /**
     * Tests an affine warp.
     */
    public void testAffine() throws TransformException {
        final int[] scalesX = {1,2,3,4,5,6,  2,7,3,1,8};
        final int[] scalesY = {1,2,3,4,5,6,  6,2,5,9,1};
        for (int i=0; i<scalesX.length; i++) {
            final int scaleX = scalesX[i];
            final int scaleY = scalesY[i];
            final WarpPolynomial warp = executeTest(new Formula() {
                public String message() {
                    return "WarpAffine[" + scaleX + ',' + scaleY + ']';
                }
                public void transform(final Point point) {
                    point.x *= scaleX;
                    point.y *= scaleY;
                }
            }, 1, 1E-5f);
            assertTrue("Expected an affine warp but got "+Utilities.getShortClassName(warp),
                       warp instanceof WarpAffine);
        }
    }

    /**
     * Tests a quadratic warp.
     */
    public void testQuadratic() throws TransformException {
        final int[] scalesX = {1,2,3,4,5,6,  2,7,3,1,8};
        final int[] scalesY = {1,2,3,4,5,6,  6,2,5,9,1};
        for (int i=0; i<scalesX.length; i++) {
            final int scaleX = scalesX[i];
            final int scaleY = scalesY[i];
            final WarpPolynomial warp = executeTest(new Formula() {
                public String message() {
                    return "WarpQuadratic[" + scaleX + ',' + scaleY + ']';
                }
                public void transform(final Point point) {
                    point.x *= scaleX*point.x;
                    point.y *= scaleY;
                }
            }, 2, 1E-2f);
            assertTrue("Expected a quatratic warp but got "+Utilities.getShortClassName(warp),
                       warp instanceof WarpQuadratic);
        }
    }
}
