/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2005, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.referencing.operation.builder;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.crs.DefaultEngineeringCRS;
import org.geotools.referencing.operation.builder.AffineTransformBuilder;
import org.geotools.referencing.operation.builder.MissingInfoException;
import org.geotools.referencing.operation.builder.ProjectiveTransformBuilder;
import org.geotools.referencing.operation.builder.RubberSheetBuilder;
import org.geotools.referencing.operation.builder.SimilarTransformBuilder;
import org.geotools.referencing.operation.builder.algorithm.Quadrilateral;
import org.geotools.referencing.operation.builder.algorithm.TriangulationException;
import org.geotools.referencing.operation.matrix.GeneralMatrix;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.vecmath.MismatchedSizeException;


/**
 * 
 * @source $URL$
 * @version $Id$
 * @author jezekjan
 */
public class MathTransformBuilderTest extends TestCase {
    /**
     * Run the suite from the command line.
     *
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     *
     * @return DOCUMENT ME!
     */
    public static Test suite() {
        return new TestSuite(MathTransformBuilderTest.class);
    }

    /**
     * Coordinates List generator.
     *
     * @param numberOfVertices count of generated points
     * @param seed for random generating.
     *
     * @return points
     */
    private List generateCoords(int numberOfVertices, long seed) {
        CoordinateReferenceSystem crs = DefaultEngineeringCRS.CARTESIAN_2D;

        return generateCoordsWithCRS(numberOfVertices, crs, seed, true);
    }

    /**
     * Coordinates List generator.
     *
     * @param numberOfVertices count of generated points
     * @param seed for random generating.
     * @param includeAccuracy set true to generate points with accuracy.
     *
     * @return points
     */
    private List generateCoords(int numberOfVertices, long seed,
        boolean includeAccuracy) {
        CoordinateReferenceSystem crs = DefaultEngineeringCRS.CARTESIAN_2D;

        return generateCoordsWithCRS(numberOfVertices, crs, seed,
            includeAccuracy);
    }

    /**
     * Coordinates List generator.
     *
     * @param numberOfVertices count of generated points
     * @param crs Coordinate Reference System of generated points
     * @param seed seed for generate random numbers
     * @param includeAccuracy set true to generate points with accuracy.
     *
     * @return points
     */
    private List generateCoordsWithCRS(int numberOfVertices,
        CoordinateReferenceSystem crs, long seed, boolean includeAccuracy) {
        List vert = new ArrayList();

        Random randomCoord = new Random(seed);

        for (int i = 0; i < numberOfVertices; i++) {
            double xs = randomCoord.nextDouble() * 1000;
            double ys = randomCoord.nextDouble() * 1000;
            double xd = randomCoord.nextDouble() * 1000;
            double yd = randomCoord.nextDouble() * 1000;
            MappedPosition p = new MappedPosition(new DirectPosition2D(crs, xs,
                        ys), new DirectPosition2D(crs, xd, yd));

            if (includeAccuracy) {
                p.setAccuracy(randomCoord.nextDouble());
            }

            vert.add(p);
        }

        return vert;
    }

    /**
     * Test expected values against transformed values.
     *
     * @param mt mathTransform that will be tested
     * @param pts MappedPositions of source and target values.
     *
     * @throws FactoryException
     * @throws TransformException
     */
    private void transformTest(MathTransform mt, List pts)
        throws FactoryException, TransformException {
        double[] points = new double[pts.size() * 2];
        double[] ptCalculated = new double[pts.size() * 2];

        for (int i = 0; i < pts.size(); i++) {
            points[2 * i] = ((MappedPosition) pts.get(i)).getSource()
                             .getCoordinates()[0];
            points[(2 * i) + 1] = ((MappedPosition) pts.get(i)).getSource()
                                   .getCoordinates()[1];
        }

        mt.transform(points, 0, ptCalculated, 0, pts.size());

        for (int i = 0; i < pts.size(); i++) {
            assertTrue((((MappedPosition) pts.get(i)).getTarget()
                         .getCoordinates()[0] - ptCalculated[2 * i]) < 0.001);
            assertTrue((((MappedPosition) pts.get(i)).getTarget()
                         .getCoordinates()[1] - ptCalculated[(2 * i) + 1]) < 0.001);
        }
    }

    /**
     * Test for {@linkplain RubberSheetBuilder RubberSheetBuilder}.
     *
     * @throws MismatchedSizeException
     * @throws MismatchedDimensionException
     * @throws FactoryException
     * @throws TransformException
     * @throws TriangulationException
     */
    public void testRubberBuilder()
        throws MismatchedSizeException, MismatchedDimensionException,
            FactoryException, TransformException, TriangulationException {
        List pts = generateCoords(20, 8324);

        CoordinateReferenceSystem crs = DefaultEngineeringCRS.CARTESIAN_2D;

        Quadrilateral quad = new Quadrilateral(new DirectPosition2D(crs, 1000, 0),
                new DirectPosition2D(crs, 0, 0),
                new DirectPosition2D(crs, 0, 1000),
                new DirectPosition2D(crs, 1000, 1000));
        MathTransformBuilder ppc = new RubberSheetBuilder(pts, quad);

        transformTest(ppc.getMathTransform(), pts);
        assertTrue(ppc.getErrorStatistics().rms() < 0.00001);

        // Tests the formatting, but do not display to console for not polluting
        // the output device. We just check that at least one axis title is
        // there.
        assertTrue(ppc.toString().indexOf(" x ") >= 0);
    }

    /**
     * Test for {@linkplain ProjectiveTransformBuilder ProjectiveTransformBuilder}.
     *
     * @throws MismatchedSizeException
     * @throws MismatchedDimensionException
     * @throws FactoryException
     * @throws TransformException
     */
    public void testProjectiveBuilder()
        throws MismatchedSizeException, MismatchedDimensionException,
            FactoryException, TransformException {
        List pts = generateCoords(4, 312243);

        MathTransformBuilder ppc = new ProjectiveTransformBuilder(pts);
        transformTest(ppc.getMathTransform(), pts);

        assertTrue(ppc.getErrorStatistics().rms() < 0.0001);
    }

    /**
     * Test that all Matrixes are filled properly.
     *
     * @throws MismatchedSizeException
     * @throws MismatchedDimensionException
     * @throws FactoryException
     * @throws TransformException
     */
    public void testLSMCalculation()
        throws MismatchedSizeException, MismatchedDimensionException,
            FactoryException, TransformException {
        List pts = generateCoords(15, 3121123);
        LSMTester buildTester = new LSMTester(pts);
        buildTester.includeWeights(true);
        buildTester.testLSM();
    }

    /**
     * Test for {@linkplain AffineTransformBuilder AffineTransformBuilder}.
     * @throws MismatchedSizeException
     * @throws MismatchedDimensionException
     * @throws FactoryException
     * @throws TransformException
     */
    public void testAffineBuilder()
        throws MismatchedSizeException, MismatchedDimensionException,
            FactoryException, TransformException {
        List pts = generateCoords(3, 2345);
        MathTransformBuilder ppc = new AffineTransformBuilder(pts);
        transformTest(ppc.getMathTransform(), pts);
        assertTrue(ppc.getErrorStatistics().rms() < 0.00001);
    }

    /**
     * Test for {@linkplain SimilarTransformBuilder SimilarTransformBuilder}.
     * @throws MismatchedSizeException
     * @throws MismatchedDimensionException
     * @throws FactoryException
     * @throws TransformException
     */
    public void testSimilarBuilder()
        throws MismatchedSizeException, MismatchedDimensionException,
            FactoryException, TransformException {
        List pts = generateCoords(2, 24535);
        MathTransformBuilder ppc = new SimilarTransformBuilder(pts);
        transformTest(ppc.getMathTransform(), pts);
        assertTrue(ppc.getErrorStatistics().rms() < 0.00001);
    }

    /**
     * Test for MismatchedSizeException.
     * @throws TransformException
     */
    public void testMismatchedSizeException() throws TransformException {
        // The exception should be thrown when the number of points is less than
        // necessary
        List pts = generateCoords(2, 2453655);

        try {
            new AffineTransformBuilder(pts);
            fail("Expected MismatchedSizeException");
        } catch (MismatchedSizeException e) {
        }
    }

    /**
     * Test for MissingInfoException.
     * @throws FactoryException
     */
    public void testMissingInfoException() throws FactoryException {
        // The exception should be thrown when the number of points is less than
        // necessary
        List pts = generateCoords(5, 2434765, false);

        try {
            AffineTransformBuilder builder = new AffineTransformBuilder(pts);
            builder.includeWeights(true);
            fail("Expected FactoryException");
        } catch (MissingInfoException e) {
        }
    }

    /**
     * Implements the method for testing the calculation of least
     * square method. The main requirement of least square method is that
     * A<sup>T<sup>PAx + A<sup>T<sup>PX = 0.
     *
     * @author jezekjas
     *
     */
    class LSMTester extends ProjectiveTransformBuilder {
        LSMTester(List pts) {
            super(pts);
        }

        public void testLSM() {
            // fill Matrix by calculateLSM()
            this.calculateLSM();

            GeneralMatrix AT = (GeneralMatrix) A.clone();
            AT.transpose();

            GeneralMatrix ATP = new GeneralMatrix(AT.getNumRow(), P.getNumCol());
            GeneralMatrix ATPA = new GeneralMatrix(AT.getNumRow(), A.getNumCol());
            GeneralMatrix ATPX = new GeneralMatrix(AT.getNumRow(), 1);
            GeneralMatrix x = new GeneralMatrix(A.getNumCol(), 1);
            ATP.mul(AT, P); // ATP
            ATPA.mul(ATP, A); // ATPA
            ATPX.mul(ATP, X); // ATPX

            GeneralMatrix ATPAI = (GeneralMatrix) ATPA.clone();
            ATPAI.invert();

            x.mul(ATPAI, ATPX);

            // assert the ATPAx + ATPX should be 0;.
            x.mul(ATPA, x);
            x.sub(ATPX);

            double[] tx = new double[x.getNumRow()];
            x.getColumn(0, tx);

            for (int i = 0; i < tx.length; i++) {
                assertTrue(tx[i] < 0.001);
            }
        }
    }
}
