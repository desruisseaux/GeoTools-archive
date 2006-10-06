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
import org.geotools.referencing.operation.matrix.GeneralMatrix;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.DirectPosition;


/**
 * 
DOCUMENT ME!
 *
 * @author jezekjan
 */
public class AffineParamCalculatorTest extends TestCase {
    /**
     * Run the suite from the command line.
     *
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(AffineParamCalculatorTest.class);
    }

    /**
     * Returns the test suite.
     *
     * @return test suite
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(AffineParamCalculatorTest.class);

        return suite;
    }

    /**
     * Test {@link AffineParamCalculator}.
     *
     * @throws CalculationException CalculationException
     * @throws TransformException ransformException
     * @throws FactoryException FactoryException
     */
    public void testAffineParamCalculator()
        throws CalculationException, TransformException, FactoryException {
        CoordinateReferenceSystem crs = DefaultEngineeringCRS.CARTESIAN_2D;

        DirectPosition[] ptSrc = new DirectPosition[3];
        DirectPosition[] ptDst = new DirectPosition[3];
        ptSrc[0] = new DirectPosition2D(crs, 10, 10);
        ptSrc[1] = new DirectPosition2D(crs, 10, 20);
        ptSrc[2] = new DirectPosition2D(crs, 14, 16);

        ptDst[0] = new DirectPosition2D(crs, 1, 1);
        ptDst[1] = new DirectPosition2D(crs, 11, 21);
        ptDst[2] = new DirectPosition2D(crs, 0, 17);

        AffineParamCalculator apTo = new AffineParamCalculator(ptSrc, ptDst);
        AffineParamCalculator apFrom = new AffineParamCalculator(ptDst, ptSrc);

        //System.out.println(apTo.getMMatrix().toString());
        GeneralMatrix Mfrom = apFrom.getMMatrix();
        Mfrom.invert();

        // test that the inverse transformation fromMatrix is same as toMatrix  
        assertEquals(Mfrom.toString(), apTo.getMMatrix().toString());

        assertEquals(apTo.getStandardDeviation(), 0, 1E-5);
    }
}
