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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.crs.AbstractCRS;
import org.geotools.referencing.crs.DefaultEngineeringCRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.operation.builder.algorithm.Quadrilateral;
import org.geotools.referencing.operation.builder.algorithm.TriangulationException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;
import org.opengis.spatialschema.geometry.MismatchedReferenceSystemException;

// J2SE and extensions
import java.util.Random;
import javax.vecmath.MismatchedSizeException;


/**
 * AffineParamCalculatorTest
 *
 * @author jezekjan
 */
public class ParamCalculatorTest extends TestCase {
    /**
     * Run the suite from the command line.
     *
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(ParamCalculatorTest.class);
    }

    /**
     * Returns the test suite.
     *
     * @return test suite
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(ParamCalculatorTest.class);

        return suite;
    }

    /**
     * Test {@link AffineTransformBuilder}.
     *
     * @param countOfVertices counf of genareted points
     *
     * @return points
     */
    private DirectPosition[] generateCoords(int countOfVertices) {
        return generateCoords(countOfVertices, 435345);
    }

    private DirectPosition[] generateCoords(int countOfVertices, long seed) {
        CoordinateReferenceSystem crs = DefaultEngineeringCRS.CARTESIAN_2D;
        DirectPosition[] vert = new DirectPosition[countOfVertices];
        Random randomCoord = new Random(seed);

        for (int i = 0; i < countOfVertices; i++) {
            double x = randomCoord.nextDouble() * 1000;
            double y = randomCoord.nextDouble() * 1000;
            vert[i] = new DirectPosition2D(crs, x, y);
        }

        return vert;
    }

    private DirectPosition[] generateCoordsWithCRS(int countOfVertices, AbstractCRS crs) {
        DirectPosition[] vert = generateCoords(countOfVertices);

        for (int i = 0; i < countOfVertices; i++) {
            vert[i] = new DirectPosition2D(crs, vert[i].getOrdinate(0),
                    vert[i].getOrdinate(1));
        }

        return vert;
    }

    private void transformTest(MathTransform mt, DirectPosition[] ptSrc, DirectPosition[] ptDst)
            throws FactoryException, TransformException
    {
        double[] points = new double[ptSrc.length * 2];
        double[] ptCalculated = new double[ptSrc.length * 2];

        for (int i = 0; i < ptSrc.length; i++) {
            points[2 * i] = ptSrc[i].getCoordinates()[0];
            points[(2 * i) + 1] = ptSrc[i].getCoordinates()[1];
        }

        mt.transform(points, 0, ptCalculated, 0, ptSrc.length);

        for (int i = 0; i < ptDst.length; i++) {
            assertTrue((ptDst[i].getCoordinates()[0] - ptCalculated[2 * i]) < 0.001);
            assertTrue((ptDst[i].getCoordinates()[1]
                - ptCalculated[(2 * i) + 1]) < 0.001);
        }
    }

    public void testRubberCalculator() throws MismatchedSizeException, MismatchedDimensionException,
                FactoryException, TransformException, TriangulationException
    {
        DirectPosition[] ptSrc = generateCoords(7, 324);
        DirectPosition[] ptDst = generateCoords(7, 124);

        CoordinateReferenceSystem crs = DefaultEngineeringCRS.CARTESIAN_2D;

        Quadrilateral quad = new Quadrilateral(new DirectPosition2D(crs, 1000, 0),
                new DirectPosition2D(crs, 0, 0),
                new DirectPosition2D(crs, 0, 1000),
                new DirectPosition2D(crs, 1000, 1000));
        MathTransformBuilder ppc = new RubberSheetBuilder(ptSrc, ptDst, quad);

        transformTest(ppc.getMathTransform(), ptSrc, ptDst);
        assertTrue(ppc.getStandardDeviation() < 0.00001);
    }

    public void testProjectiveCalculator() throws MismatchedSizeException,
            MismatchedDimensionException, FactoryException, TransformException
    {
        DirectPosition[] ptSrc = generateCoords(4, 3243);
        DirectPosition[] ptDst = generateCoords(4, 2344);
        MathTransformBuilder ppc = new ProjectiveTransformBuilder(ptSrc, ptDst);
        //System.out.println(ppc.getMathTransform().toWKT());
        transformTest(ppc.getMathTransform(), ptSrc, ptDst);
        assertTrue(ppc.getStandardDeviation() < 0.00001);
    }

    public void testAffineCalculator() throws MismatchedSizeException,
            MismatchedDimensionException, FactoryException, TransformException
    {
        DirectPosition[] ptSrc = generateCoords(3, 2345);
        DirectPosition[] ptDst = generateCoords(3, 2312);
        MathTransformBuilder ppc = new AffineTransformBuilder(ptSrc, ptDst);
        transformTest(ppc.getMathTransform(), ptSrc, ptDst);
        assertTrue(ppc.getStandardDeviation() < 0.00001);
    }

    public void testSimilarCalculator() throws MismatchedSizeException,
            MismatchedDimensionException, FactoryException, TransformException
    {
        DirectPosition[] ptSrc = generateCoords(2, 142);
        DirectPosition[] ptDst = generateCoords(2, 1244);
        MathTransformBuilder ppc = new SimilarTransformBuilder(ptSrc, ptDst);
        //System.out.println(ppc.getMMatrix());
        transformTest(ppc.getMathTransform(), ptSrc, ptDst);
        assertTrue(ppc.getStandardDeviation() < 0.00001);
    }

    public void testCalculationException() throws TransformException {
        // The exception should be thrown when the number of points is not the same
        DirectPosition[] ptSrc = generateCoords(2);
        DirectPosition[] ptDst = generateCoords(3);

        try {
            new SimilarTransformBuilder(ptSrc, ptDst);
            fail("Expected MismatchedSizeException");
        } catch (MismatchedSizeException e) {
        }

        // The exception should be thrown when the number of points is less then neccesary
        ptSrc = generateCoords(2);
        ptDst = generateCoords(2);

        try {
            new AffineTransformBuilder(ptSrc, ptDst);
            fail("Expected MismatchedSizeException");
        } catch (MismatchedSizeException e) {
        }

        // The exception should be thrown when the CRS of all points is not the same
        ptSrc = generateCoords(4);
        ptSrc[0] = new DirectPosition2D(12, 12);
        ptDst = generateCoords(4);

        try {
            new AffineTransformBuilder(ptSrc, ptDst);
            fail("Expected MismatchedReferenceSystemException");
        } catch (MismatchedReferenceSystemException e) {
        }

        //The exception should be thrown when the CRS is not null or DefaultEngineeringCRS.CARTESIAN_2D
        ptSrc = generateCoordsWithCRS(4, DefaultGeographicCRS.WGS84);
        ptDst = generateCoords(4);

        try {
            new AffineTransformBuilder(ptSrc, ptDst);
            fail("Expected MismatchedReferenceSystemException");
        } catch (MismatchedReferenceSystemException e) {
        }
    }
}
