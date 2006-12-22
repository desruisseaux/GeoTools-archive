/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, Geotools Project Managment Committee (PMC)
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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.vecmath.MismatchedSizeException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.crs.DefaultEngineeringCRS;
import org.geotools.referencing.operation.builder.algorithm.Quadrilateral;
import org.geotools.referencing.operation.builder.algorithm.TriangulationException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;


/**
 * AffineParamCalculatorTest
 *
 * @author jezekjan
 */
public class MathTransformBuilderTest extends TestCase {
    /**
     * Run the suite from the command line.
     *   
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     *  
     */
    public static Test suite() {
        return new TestSuite(MathTransformBuilderTest.class);
    }

    /**
     * Test {@link AffineTransformBuilder}.
     *
     * @param numberOfVertices count of generated points
     * @param seed for random generating.
     *
     * @return points
     */
    private List generateCoords(int numberOfVertices, long seed) {
        CoordinateReferenceSystem crs = DefaultEngineeringCRS.CARTESIAN_2D;

        return generateCoordsWithCRS(numberOfVertices, crs, seed);
    }

    private List generateCoordsWithCRS(int numberOfVertices,
        CoordinateReferenceSystem crs, long seed) {
        List vert = new ArrayList();

        Random randomCoord = new Random(seed);

        for (int i = 0; i < numberOfVertices; i++) {
            double xs = randomCoord.nextDouble() * 1000;
            double ys = randomCoord.nextDouble() * 1000;
            double xd = randomCoord.nextDouble() * 1000;
            double yd = randomCoord.nextDouble() * 1000;
            MappedPosition p = new MappedPosition(new DirectPosition2D(crs, xs,
                        ys), new DirectPosition2D(crs, xd, yd));
            vert.add(p);
        }

        return vert;
    }

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

    public void testRubberCalculator()
        throws MismatchedSizeException, MismatchedDimensionException,
            FactoryException, TransformException, TriangulationException {
        List pts = generateCoords(20, 8324);

        //  DirectPosition[] ptDst = generateCoords(7, 124);
        CoordinateReferenceSystem crs = DefaultEngineeringCRS.CARTESIAN_2D;

        Quadrilateral quad = new Quadrilateral(new DirectPosition2D(crs, 1000, 0),
                new DirectPosition2D(crs, 0, 0),
                new DirectPosition2D(crs, 0, 1000),
                new DirectPosition2D(crs, 1000, 1000));
        MathTransformBuilder ppc = new RubberSheetBuilder(pts, quad);

        transformTest(ppc.getMathTransform(), pts);
        assertTrue(ppc.getErrorStatistics().rms() < 0.00001);

        // Tests the formatting, but do not display to console for not polluting
        // the output device. We just check that at least one axis title is there.
        assertTrue(ppc.toString().indexOf(" x ") >= 0);
    }

    public void testProjectiveCalculator()
        throws MismatchedSizeException, MismatchedDimensionException,
            FactoryException, TransformException {
        List pts = generateCoords(4, 3243);

        MathTransformBuilder ppc = new ProjectiveTransformBuilder(pts);
        //System.out.println(ppc.getMathTransform().toWKT());
        transformTest(ppc.getMathTransform(), pts);
        assertTrue(ppc.getErrorStatistics().rms() < 0.00001);
    }

    public void testAffineCalculator()
        throws MismatchedSizeException, MismatchedDimensionException,
            FactoryException, TransformException {
        List pts = generateCoords(3, 2345);
        MathTransformBuilder ppc = new AffineTransformBuilder(pts);
        transformTest(ppc.getMathTransform(), pts);
        assertTrue(ppc.getErrorStatistics().rms() < 0.00001);
    }

    public void testSimilarCalculator()
        throws MismatchedSizeException, MismatchedDimensionException,
            FactoryException, TransformException {
        List pts = generateCoords(2, 24535);
        MathTransformBuilder ppc = new SimilarTransformBuilder(pts);        
        transformTest(ppc.getMathTransform(), pts);
        assertTrue(ppc.getErrorStatistics().rms() < 0.00001);
    }

    public void testCalculationException() throws TransformException {
       
        // The exception should be thrown when the number of points is less than necessary
        List pts = generateCoords(2, 2453655);

        try {
            new AffineTransformBuilder(pts);
            fail("Expected MismatchedSizeException");
        } catch (MismatchedSizeException e) {
        }

        
    }
}
