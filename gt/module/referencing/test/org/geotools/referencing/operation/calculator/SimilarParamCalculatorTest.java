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
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.DirectPosition;
import java.util.Random;


public class SimilarParamCalculatorTest extends TestCase {
    /**
     * Run the suite from the command line.
     *
     * @param args DOCUMENT ME!
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(SimilarParamCalculatorTest.class);
    }

    /**
     * Returns the test suite.
     *
     * @return DOCUMENT ME!
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(SimilarParamCalculatorTest.class);

        return suite;
    }

    /**
     * Test {@link SimilarParamCalculator}.
     *
     * @throws TransformException DOCUMENT ME!
     * @throws FactoryException DOCUMENT ME!
     */
    public void testLinearParamCalculator()
        throws TransformException, FactoryException {
        CoordinateReferenceSystem crs = DefaultEngineeringCRS.CARTESIAN_2D;
        DirectPosition[] ptSrc = new DirectPosition[2];
        DirectPosition[] ptDst = new DirectPosition[2];
        Random random = new Random();
        ptSrc[0] = new DirectPosition2D(crs, random.nextDouble() * 1000,
                random.nextDouble() * 1000);

        ptSrc[1] = new DirectPosition2D(crs, random.nextDouble() * 1000,
                random.nextDouble() * 1000);

        ptDst[0] = new DirectPosition2D(crs, random.nextDouble() * 1000,
                random.nextDouble() * 1000);
        ptDst[1] = new DirectPosition2D(crs, random.nextDouble() * 1000,
                random.nextDouble() * 1000);

        AbstractParamCalculator ltransform = new SimilarParamCalculator(ptSrc,
                ptDst);

        double[] points = new double[ptSrc.length * 2];
        double[] ptCalculated = new double[ptSrc.length * 2];

        for (int i = 0; i < ptSrc.length; i++) {
            points[2 * i] = ptSrc[i].getCoordinates()[0];
            points[(2 * i) + 1] = ptSrc[i].getCoordinates()[1];
        }

        ltransform.getMathTransform()
                  .transform(points, 0, ptCalculated, 0, ptSrc.length);

        for (int i = 0; i < ptDst.length; i++) {
            assertTrue((ptDst[i].getCoordinates()[0] - ptCalculated[2 * i]) < 0.001);
            assertTrue((ptDst[i].getCoordinates()[1]
                - ptCalculated[(2 * i) + 1]) < 0.001);
        }
    }
}
