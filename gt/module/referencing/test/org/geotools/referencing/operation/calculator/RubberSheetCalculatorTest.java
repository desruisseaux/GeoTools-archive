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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.crs.DefaultEngineeringCRS;
import org.geotools.referencing.operation.calculator.algorithm.Quadrilateral;
import org.geotools.referencing.operation.calculator.algorithm.RubberSheetTransform;
import org.geotools.referencing.operation.calculator.algorithm.TriangulationException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.DirectPosition;
import java.util.Random;


public class RubberSheetCalculatorTest extends TestCase {
    /**
     * Run the suite from the command line.
     *
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(RubberSheetCalculatorTest.class);
    }

    /**
     * Returns the test suite.
     *
     * @return suite
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(RubberSheetCalculatorTest.class);

        return suite;
    }

    /**
     * Test {@link RubberSheetCalculator}.
     *
     * @throws TransformException
     * @throws TriangulationException
     */
    public void testRubberSheetFactory()
        throws TransformException, TriangulationException {
        CoordinateReferenceSystem crs = DefaultEngineeringCRS.CARTESIAN_2D;

        Quadrilateral quad = new Quadrilateral(new DirectPosition2D(crs, 5, 5),
                new DirectPosition2D(crs, 30, 5),
                new DirectPosition2D(crs, 30, 30),
                new DirectPosition2D(crs, 5, 30));

        Random randomCoord = new Random();

        // number of points 
        int i = 1;
        DirectPosition[] ptSrc = new DirectPosition2D[i];
        DirectPosition[] ptDst = new DirectPosition2D[i];

        // Generates the vertices of identical points
        for (int j = 0; j < i; j++) {
            double x = quad.p0.getCoordinates()[0]
                + (randomCoord.nextDouble() * (quad.p1.getCoordinates()[0]
                - quad.p0.getCoordinates()[0]));
            double y = quad.p0.getCoordinates()[1]
                + (randomCoord.nextDouble() * (quad.p3.getCoordinates()[1]
                - quad.p0.getCoordinates()[1]));
            double mx = quad.p0.getCoordinates()[0]
                + (randomCoord.nextDouble() * (quad.p1.getCoordinates()[0]
                - quad.p0.getCoordinates()[0]));
            double my = quad.p0.getCoordinates()[1]
                + (randomCoord.nextDouble() * (quad.p3.getCoordinates()[1]
                - quad.p0.getCoordinates()[1]));
            ptSrc[j] = new DirectPosition2D(crs, x, y);
            ptDst[j] = new DirectPosition2D(crs, mx, my);

            //  ptSrc[j] = new DirectPosition2D(14, 14);
            //  ptDst[j] = new DirectPosition2D(16, 16);

            //	vertices.add(new MappedPosition(new DirectPosition2D(x,y), new DirectPosition2D(mx,my)));
        }

        RubberSheetCalculator rbsf = new RubberSheetCalculator(ptSrc, ptDst,
                quad);

        //HashMap m = rbsf.getMapTriangulation();
        MathTransform rbst = (RubberSheetTransform) rbsf.getMathTransform();

        // convert to the format for transformation
        double[] points = new double[ptSrc.length * 2];

        for (int j = 0; j < ptSrc.length; j++) {
            points[2 * j] = ptSrc[j].getCoordinates()[0];
            points[(2 * j) + 1] = ptSrc[j].getCoordinates()[1];
            i++;
        }

        double[] dstCalculated = new double[points.length];

        rbst.transform(points, 0, dstCalculated, 0, points.length / 2);

        double toleranc = 0.00001;
        i = 0;

        for (int j = 0; j < ptSrc.length; j++) {
            assertTrue((dstCalculated[2 * j] - ptDst[j].getCoordinates()[0]) < Math
                .abs(toleranc));
            assertTrue((dstCalculated[(2 * j) + 1]
                - ptDst[j].getCoordinates()[1]) < Math.abs(toleranc));
        }
    }
}
