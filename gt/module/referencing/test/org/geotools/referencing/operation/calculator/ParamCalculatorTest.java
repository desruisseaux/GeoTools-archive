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
package org.geotools.referencing.operation.calculator;

// J2SE and extensions
import java.util.Random;
import javax.vecmath.MismatchedSizeException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.crs.AbstractCRS;
import org.geotools.referencing.crs.DefaultEngineeringCRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.operation.calculator.algorithm.Quadrilateral;
import org.geotools.referencing.operation.calculator.algorithm.TriangulationException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;
import org.opengis.spatialschema.geometry.MismatchedReferenceSystemException;


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
     * Test {@link AffineParamCalculator}.
     *
     * @param countOfVertices counf of genareted points
     *
     * @return points
     */
    private DirectPosition[] generateCoords(int countOfVertices) {
        CoordinateReferenceSystem crs = DefaultEngineeringCRS.CARTESIAN_2D;
        DirectPosition[] vert = new DirectPosition[countOfVertices];
        Random randomCoord = new Random();

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
             vert[i] = new DirectPosition2D(crs, vert[i].getOrdinate(0),  vert[i].getOrdinate(1));
         }
         return vert;
    }

    private void transformTest(MathTransform mt, DirectPosition[] ptSrc,
        DirectPosition[] ptDst) throws TransformException {
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

    public void testRubberCalculator()
        throws MismatchedSizeException, MismatchedDimensionException, TransformException, TriangulationException {
        DirectPosition[] ptSrc = generateCoords(7);
        DirectPosition[] ptDst = generateCoords(7);

        CoordinateReferenceSystem crs = DefaultEngineeringCRS.CARTESIAN_2D;

        Quadrilateral quad = new Quadrilateral(new DirectPosition2D(crs, 1000, 0),
                new DirectPosition2D(crs, 0, 0),
                new DirectPosition2D(crs, 0, 1000),
                new DirectPosition2D(crs, 1000, 1000));
        MathTransformBuilder ppc = new RubberSheetCalculator(ptSrc, ptDst,
                quad);
        //System.out.println(ppc.getMMatrix());
        transformTest(ppc.getMathTransform(), ptSrc, ptDst);
        assertTrue(ppc.getStandardDeviation() < 0.00001);
    }

    public void testProjectiveCalculator()
        throws MismatchedSizeException, MismatchedDimensionException, TransformException {
        DirectPosition[] ptSrc = generateCoords(4);
        DirectPosition[] ptDst = generateCoords(4);
        MathTransformBuilder ppc = new ProjectiveParamCalculator(ptSrc, ptDst);
        //System.out.println(ppc.getMMatrix());
        transformTest(ppc.getMathTransform(), ptSrc, ptDst);
        assertTrue(ppc.getStandardDeviation() < 0.00001);
    }

    public void testAffineCalculator()
        throws MismatchedSizeException, MismatchedDimensionException, TransformException {
        DirectPosition[] ptSrc = generateCoords(3);
        DirectPosition[] ptDst = generateCoords(3);
        MathTransformBuilder ppc = new AffineParamCalculator(ptSrc, ptDst);
        //System.out.println(ppc.getMMatrix());
        transformTest(ppc.getMathTransform(), ptSrc, ptDst);
        assertTrue(ppc.getStandardDeviation() < 0.00001);
    }

    public void testSimilarCalculator()
        throws MismatchedSizeException, MismatchedDimensionException, TransformException {
        DirectPosition[] ptSrc = generateCoords(2);
        DirectPosition[] ptDst = generateCoords(2);
        MathTransformBuilder ppc = new SimilarParamCalculator(ptSrc, ptDst);
        //System.out.println(ppc.getMMatrix());
        transformTest(ppc.getMathTransform(), ptSrc, ptDst);
        assertTrue(ppc.getStandardDeviation() < 0.00001);
    }

    public void testCalculationException() throws TransformException {
    	
    	// The exception should be thrown when the number of points is not the same
        DirectPosition[] ptSrc = generateCoords(2);
        DirectPosition[] ptDst = generateCoords(3);       
          try {
            new SimilarParamCalculator(ptSrc, ptDst);
            fail("Expected MismatchedSizeException");
        } catch (MismatchedSizeException e) {
        }
        // The exception should be thrown when the number of points is less then neccesary
        ptSrc = generateCoords(2);
        ptDst = generateCoords(2);       
          try {
            new AffineParamCalculator(ptSrc, ptDst);
            fail("Expected MismatchedSizeException");
        } catch (MismatchedSizeException e) {
        }
        
        // The exception should be thrown when the CRS of all points is not the same
        ptSrc = generateCoords(4);
        ptSrc[0]= new DirectPosition2D(12,12);
        ptDst = generateCoords(4);       
          try {
            new AffineParamCalculator(ptSrc, ptDst);
            fail("Expected MismatchedReferenceSystemException");
        } catch (MismatchedReferenceSystemException e) {
        }
        
        //The exception should be thrown when the CRS is not null or DefaultEngineeringCRS.CARTESIAN_2D
        ptSrc = generateCoordsWithCRS(4, DefaultGeographicCRS.WGS84);        
        ptDst = generateCoords(4);       
          try {
            new AffineParamCalculator(ptSrc, ptDst);
            fail("Expected MismatchedReferenceSystemException");
        } catch (MismatchedReferenceSystemException e) {
        }
    }
}
