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
package org.geotools.referencing.operation.calculator;

import org.geotools.referencing.crs.DefaultEngineeringCRS;
import org.geotools.referencing.operation.matrix.GeneralMatrix;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.spatialschema.geometry.DirectPosition;


/**
 * The class for calculating 8 parameters of projective transformation
 * (2D). The calculation uses least square method. Projective transform
 * equation:<pre>  [ x']   [  m00  m01  m02  ] [ x ]   
 *  [ y'] = [  m10  m11  m12  ] [ y ]                               
 *  [ 1 ]   [  m20  m21    1  ] [ 1 ]                       
 *    x' = m * x </pre>In the case that we have more identical points we
 * can write it like this  (in Matrix):<pre> 
 *  [ x'<sub>1</sub> ]      [ x<sub>1</sub> y<sub>1</sub> 1  0  0  0 -x'x  -x'y]   [ m00 ]
 *  [ x'<sub>2</sub> ]      [ x<sub>2</sub> y<sub>2</sub> 1  0  0  0 -x'x  -x'y]   [ m01 ]
 *  [  .  ]      [             .              ]   [ m02 ]
 *  [  .  ]      [             .              ] * [ m10 ]
 *  [ x'<sub>n</sub> ]   =  [ x<sub>n</sub> y<sub>n</sub> 1  0  0  0 -x'x  -x'y]   [ m11 ]
 *  [ y'<sub>1</sub> ]      [ 0  0  0  x<sub>1</sub> y<sub>1</sub> 1 -y'x  -y'y]   [ m12 ]
 *  [ y'<sub>2</sub> ]      [ 0  0  0  x<sub>2</sub> y<sub>2</sub> 1  -y'x  -y'y]   [ m20 ]  
 *  [  .  ]      [             .              ]   [ m21 ] 
 *  [  .  ]      [             .              ]     
 *  [ y'<sub>n</sub> ]      [ 0  0  0  x<sub>n</sub> y<sub>n</sub> 1  -y'x  -y'y]   
 *  x' = A*m </pre>Using the least square method we get this result:
 * <pre><blockquote>
 *  m = (A<sup>T</sup>A)<sup>-1</sup> A<sup>T</sup>x'  </blockquote> </pre>
 *
 * @author jezekjan
 */
public class ProjectiveParamCalculator extends AbstractParamCalculator {
    protected ProjectiveParamCalculator() {
    }

/**
         * Creates ProjectiveParamCalculator for the set of properties.        
         * @param ptSrc Set of source points
         * @param ptDst Set of destination points
         * @throws CalculationException -if the number or dimesion of properties is not set properly.
         * @throws CRSException -if the CRS of {@link #ptSrt} and {@link #ptDst} have wrong Coordinate Reference System.
         */
    public ProjectiveParamCalculator(DirectPosition[] ptSrc,
        DirectPosition[] ptDst) throws CalculationException, CRSException {
        this.ptDst = ptDst;
        this.ptSrc = ptSrc;

        super.checkPoints(4, 2);

        checkCRS();
    }

    /**
     * Checking of the Coordinate Reference System.
     *
     * @throws CRSException if the system is not the {@linkplain DefaultEngineeringCRS}.
     */
    protected void checkCRS() throws CRSException {
        if ((ptDst[0].getCoordinateReferenceSystem() != DefaultEngineeringCRS.CARTESIAN_2D)
                && (ptDst[0].getCoordinateReferenceSystem() != null)) {
            throw new CRSException(
                "DefaultEngineeringCRS.CARTESIAN_2D is expected for this method");
        }
    }

    /**
     * The method returns the array of projective transformation
     * paremeters m00, m01, m02, m10, m11, m12, m22, m23.
     *
     * @return double array of parameters
     */
    protected double[] generateMMatrix() {
        // super.checkPoints(3, 2);
        GeneralMatrix A = new GeneralMatrix(2 * ptSrc.length, 8);
        GeneralMatrix X = new GeneralMatrix(2 * ptSrc.length, 1);

        int numRow = X.getNumRow();

        // Creates X matrix
        for (int j = 0; j < (numRow / 2); j++) {
            double xs = ptSrc[j].getCoordinates()[0];
            double ys = ptSrc[j].getCoordinates()[1];
            double xd = ptDst[j].getCoordinates()[0];

            A.setElement(j, 0, xs);
            A.setElement(j, 1, ys);
            A.setElement(j, 2, 1);
            A.setElement(j, 3, 0);
            A.setElement(j, 4, 0);
            A.setElement(j, 5, 0);
            A.setElement(j, 6, -xd * xs);
            A.setElement(j, 7, -xd * ys);

            X.setElement(j, 0, ptDst[j].getCoordinates()[0]);
        }

        for (int j = numRow / 2; j < numRow; j++) {
            double xs = ptSrc[j - (numRow / 2)].getCoordinates()[0];
            double ys = ptSrc[j - (numRow / 2)].getCoordinates()[1];
            double yd = ptDst[j - (numRow / 2)].getCoordinates()[1];

            A.setElement(j, 0, 0);
            A.setElement(j, 1, 0);
            A.setElement(j, 2, 0);
            A.setElement(j, 3, ptSrc[j - (numRow / 2)].getCoordinates()[0]);
            A.setElement(j, 4, ptSrc[j - (numRow / 2)].getCoordinates()[1]);
            A.setElement(j, 5, 1);
            A.setElement(j, 6, -yd * xs);
            A.setElement(j, 7, -yd * ys);

            X.setElement(j, 0, ptDst[j - (numRow / 2)].getCoordinates()[1]);
        }

        return calculateLSM(A, X);
    }

    /**
     * Calculates the parameters useing the least square method.  The
     * equation:<pre><blockquote>
     *  m = (A<sup>T</sup>A)<sup>-1</sup> A<sup>T</sup>x'  </blockquote> </pre>
     *
     * @param A A Matrix.
     * @param X x' Matric.
     *
     * @return m matrix.
     */
    protected double[] calculateLSM(GeneralMatrix A, GeneralMatrix X) {
        GeneralMatrix AT = (GeneralMatrix) A.clone();
        AT.transpose();

        GeneralMatrix ATA = new GeneralMatrix(A.getNumCol(), A.getNumCol());
        GeneralMatrix ATX = new GeneralMatrix(A.getNumCol(), 1);
        GeneralMatrix x = new GeneralMatrix(A.getNumCol(), 1);
        ATA.mul(AT, A);
        ATX.mul(AT, X);
        ATA.invert();
        ATX.mul(AT, X);
        x.mul(ATA, ATX);
        x.transpose();

        return x.getElements()[0];
    }

    /**
     * Returns the matrix of parameters for Projective tranformation.
     * This method should by override for the special cases like affine or
     * similar transformation. The M matrix looks like this:
     * <pre>                                                       
     * [  m00  m01  m02  ]                           
     * [  m10  m11  m12  ]                              
     * [  m20  m21   1   ]                                                                   
     * </pre>
     *
     * @return Matrix M
     */
    protected GeneralMatrix getProjectiveMatrix() {
        GeneralMatrix M = new GeneralMatrix(3, 3);
        double[] param = generateMMatrix();
        double[] m0 = { param[0], param[1], param[2] };
        double[] m1 = { param[3], param[4], param[5] };
        double[] m2 = { param[6], param[7], 1 };
        M.setRow(0, m0);
        M.setRow(1, m1);
        M.setRow(2, m2);

        return M;
    }

    public MathTransform getMathTransform() {
        return ProjectiveTransform.create(getProjectiveMatrix());
    }
}
