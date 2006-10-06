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
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.referencing.datum.BursaWolfParameters;
import org.geotools.referencing.operation.transform.GeocentricTranslation;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.DirectPosition;
import java.util.Random;


public class BursaWolfParamCalculatorTest extends TestCase {
    /**
     * Run the suite from the command line.
     *
     * @param args DOCUMENT ME!
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(BursaWolfParamCalculatorTest.class);
    }

    /**
     * Returns the test suite.
     *
     * @return DOCUMENT ME!
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(BursaWolfParamCalculatorTest.class);

        return suite;
    }

    /**
     * Test {@link BursaWolfParamCalculator}.
     */
    public void testBursaWolfParamCalculaterXrotation() {
        Random random = new Random();

        double R = 6370000;
        double angle = (((random.nextDouble() * 10) / 3600) * Math.PI) / 180;
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        DirectPosition[] ptSrc = new GeneralDirectPosition[3];
        DirectPosition[] ptDst = new GeneralDirectPosition[3];

        ptSrc[0] = new GeneralDirectPosition(R, 0, 0);
        ptSrc[1] = new GeneralDirectPosition(0, cos * R, -sin * R);
        ptSrc[2] = new GeneralDirectPosition(0, sin * R, cos * R);

        ptDst[0] = new GeneralDirectPosition(R, 0, 0);
        ptDst[1] = new GeneralDirectPosition(0, R, 0);
        ptDst[2] = new GeneralDirectPosition(0, 0, R);

        try {
            double[] points = new double[ptSrc.length * 3];

            for (int i = 0; i < ptSrc.length; i++) {
                points[i * 3] = ptSrc[i].getCoordinates()[0];
                points[(i * 3) + 1] = ptSrc[i].getCoordinates()[1];
                points[(i * 3) + 2] = ptSrc[i].getCoordinates()[2];
            }

            double[] dstPoints = new double[points.length];

            AbstractParamCalculator BWPT = new BursaWolfParamCalculator(ptSrc,
                    ptDst);
            BWPT.getMathTransform()
                .transform(points, 0, dstPoints, 0, (points.length / 3));

            for (int i = 0; i < ptDst.length; i++) {
                assertEquals(dstPoints[i * 3], ptDst[i].getCoordinates()[0],
                    1E-2);
                assertEquals(dstPoints[(i * 3) + 1],
                    ptDst[i].getCoordinates()[1], 1E-2);
                assertEquals(dstPoints[(i * 3) + 2],
                    ptDst[i].getCoordinates()[2], 1E-2);
            }
        } catch (CalculationException e) {
            System.out.println(e.getMessage());
        } catch (TransformException f) {
            System.out.println(f.getMessage());
        }
    }

    /**
     * The test that generates random transformation parameters and
     * source points. The destination poinst are calculated using generated
     * parameters. Then the parameters are computed by the calculater and
     * comared with original.
     *
     * @throws TransformException
     */
    public void test2BursaWolfParamCalculater() throws TransformException {
        double R = 6370000;
        Random random = new Random();
        int numberOfPoints = 3 * 10;

        //double[] points = new double[numberOfPoints];
        DirectPosition[] ptSrc = new GeneralDirectPosition[numberOfPoints];
        DirectPosition[] ptDst = new GeneralDirectPosition[numberOfPoints];

        BursaWolfParameters bwp = new BursaWolfParameters(null);
        bwp.dx = random.nextDouble() * 100;
        bwp.dy = random.nextDouble() * 100;
        bwp.dz = random.nextDouble() * 100;
        bwp.ex = random.nextDouble() * 10;
        bwp.ey = random.nextDouble() * 10;
        bwp.ez = random.nextDouble() * 10;
        bwp.ppm = random.nextDouble() * 10;

        GeocentricTranslation gt = new GeocentricTranslation(bwp);

        for (int i = 0; i < (3 * 10); i++) {
            double gamma = ((45 + (random.nextDouble() * 10)) * Math.PI) / 180;
            double alfa = ((45 + (random.nextDouble() * 10)) * Math.PI) / 180;

            //   generate source points
            ptSrc[i] = new GeneralDirectPosition(R * Math.sin(gamma) * Math.cos(
                        alfa), R * Math.sin(gamma) * Math.cos(alfa),
                    R * Math.cos(gamma));

            double[] pom = new double[3];

            //  generates destination points
            gt.transform(ptSrc[i].getCoordinates(), 0, pom, 0, 1);
            ptDst[i] = new GeneralDirectPosition(pom);
        }

        BursaWolfParamCalculator BWPT = new BursaWolfParamCalculator(ptSrc,
                ptDst);
        assertEquals(BWPT.getBursaWolfParameters(null).dx, bwp.dx, 1E-2);
        assertEquals(BWPT.getBursaWolfParameters(null).dy, bwp.dy, 1E-2);
        assertEquals(BWPT.getBursaWolfParameters(null).dz, bwp.dz, 1E-2);
        assertEquals(BWPT.getBursaWolfParameters(null).ex, bwp.ex, 1E-2);
        assertEquals(BWPT.getBursaWolfParameters(null).ey, bwp.ey, 1E-2);
        assertEquals(BWPT.getBursaWolfParameters(null).ez, bwp.ez, 1E-2);
        assertEquals(BWPT.getBursaWolfParameters(null).ppm, bwp.ppm, 1E-2);

        assertEquals(BWPT.getStandardDeviation(), 0, 1E-2);
    }
}
