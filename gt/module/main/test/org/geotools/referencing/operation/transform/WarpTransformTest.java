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
     * Small numbers for floating point comparaisons.
     */
    private static final float EPS = 1E-8f;

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
        void transform(Point point);
    }

    /**
     * Constructs a warp and tests the transformations. Coefficients will be tested later
     * (by the caller).
     */
    private static WarpPolynomial executeTest(final Formula formula, final int degree) {
        final Rectangle bounds = new Rectangle(0, 0, 1, 1); // Prevent scaling in Warp creation.
        final Point[] sources = {
            new Point(0,0),
            new Point(0,1),
            new Point(1,1),
//            new Point(3,4),
//            new Point(5,2),
//            new Point(2,1),
//            new Point(6,4)
        };
        final Point[] dest = new Point[sources.length];
        for (int i=0; i<dest.length; i++) {
            formula.transform(dest[i] = new Point(sources[i]));
        }
        final WarpTransform2D transform = new WarpTransform2D(
                bounds, sources, 0, bounds, dest, 0, sources.length, degree);
        final Warp warp = transform.getWarp();
        assertTrue("Expected a polynomial warp but got "+Utilities.getShortClassName(warp),
                   warp instanceof WarpPolynomial);
        final WarpPolynomial poly = (WarpPolynomial) warp;
        assertEquals("Unexpected X scaling", 1, poly.getPreScaleX(),  0);
        assertEquals("Unexpected Y scaling", 1, poly.getPreScaleY(),  0);
        assertEquals("Unexpected X scaling", 1, poly.getPostScaleX(), 0);
        assertEquals("Unexpected Y scaling", 1, poly.getPostScaleY(), 0);
        /*
         * Compares transformations to the expected points.
         * Uses the transform(Point2D, ...) method.
         */
        for (int i=0; i<sources.length; i++) {
            final String message = "Point #" + i;
            Point2D point = new Point2D.Double(sources[i].x, sources[i].y);
            point = transform.transform(point, point);
            assertEquals(message, dest[i].x, point.getX(), EPS);
            assertEquals(message, dest[i].y, point.getY(), EPS);
        }
        return poly;
    }

    /**
     * Tests an affine warp.
     */
    public void testAffine() {
        if (true) {
            // Disabled for now, since the test doesn't pass yet.
            return;
        }
        final int scaleX = 1;
        final int scaleY = 1;
        final WarpPolynomial warp = executeTest(new Formula() {
            public void transform(final Point point) {
                point.x *= scaleX;
                point.y *= scaleY;
            }
        }, 1);
        assertTrue("Expected an affine warp but got "+Utilities.getShortClassName(warp),
                   warp instanceof WarpAffine);
        final float[] xCoeffs = warp.getXCoeffs();
        final float[] yCoeffs = warp.getYCoeffs();
        assertEquals("Unexpected X translation", 0, xCoeffs[0], EPS);
        assertEquals("Unexpected Y translation", 0, yCoeffs[0], EPS);
        assertEquals("Unexpected X scale",  scaleX, xCoeffs[1], EPS);
        assertEquals("Unexpected X scale",  scaleY, yCoeffs[1], EPS);
    }
}
