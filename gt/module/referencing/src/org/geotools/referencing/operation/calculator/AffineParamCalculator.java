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

import org.geotools.referencing.operation.matrix.GeneralMatrix;
import org.opengis.spatialschema.geometry.DirectPosition;


/**
 * The class for calculating 6 parameters of affine transformation (2D).
 * The calculation uses least square method. The Affine transform equation:
 * <pre>                                                       
 *  [ x']   [  m00  m01  m02  ] [ x ]   [ m00x + m01y + m02 ]
 *  [ y'] = [  m10  m11  m12  ] [ y ] = [ m10x + m11y + m12 ]
 *  [ 1 ]   [   0    0    1   ] [ 1 ]   [         1         ] 
 *   x' = m * x </pre>In the case that we have more identical points we can
 * write it like this  (in Matrix):<pre> 
 *  [ x'<sub>1</sub> ]      [ x<sub>1</sub> y<sub>1</sub> 1  0  0  0 ]   [ m00 ]
 *  [ x'<sub>2</sub> ]      [ x<sub>2</sub> y<sub>2</sub> 1  0  0  0 ]   [ m01 ]
 *  [  .  ]      [        .         ]   [ m02 ]
 *  [  .  ]      [        .         ] * [ m10 ]
 *  [ x'<sub>n</sub> ]   =  [ x<sub>n</sub> y<sub>n</sub> 1  0  0  0 ]   [ m11 ]
 *  [ y'<sub>1</sub> ]      [ 0  0  0  x<sub>1</sub> y<sub>1</sub> 1 ]   [ m12 ]
 *  [ y'<sub>2</sub> ]      [ 0  0  0  x<sub>2</sub> y<sub>2</sub> 1 ]  
 *  [  .  ]      [        .         ]                               
 *  [  .  ]      [        .         ]                        
 *  [ y'<sub>n</sub> ]      [ 0  0  0  x<sub>n</sub> y<sub>n</sub> 1 ]   
 *  x' = A*m </pre>Using the least square method we get this result:
 * <pre><blockquote>
 *  m = (A<sup>T</sup>A)<sup>-1</sup> A<sup>T</sup>x'  </blockquote> </pre>
 *
 * @author Jan Jezek
 */
public class AffineParamCalculator extends ProjectiveParamCalculator {
    protected AffineParamCalculator() {
    }

    /**
     * Creates AffineParamCalculator for the set of properties.
     * 
     * @param ptSrc Set of source points
     * @param ptDst Set of destination points
     */
    public AffineParamCalculator(DirectPosition[] ptSrc, DirectPosition[] ptDst)
        throws CalculationException, CRSException {
        this.ptDst = ptDst;
        this.ptSrc = ptSrc;

        super.checkPoints(3, 2);
        super.checkCRS();
    }

    /**
     * The method returns the array of affine transformation paremeters
     * m00, m01, m02, m10, m11, m12.
     *
     * @return double array of parameters
     */
    protected double[] generateMMatrix() {
        // super.checkPoints(3, 2);
        GeneralMatrix A = new GeneralMatrix(2 * ptSrc.length, 6);
        GeneralMatrix X = new GeneralMatrix(2 * ptSrc.length, 1);

        int numRow = X.getNumRow();

        // Creates X matrix
        for (int j = 0; j < (numRow / 2); j++) {
            A.setElement(j, 0, ptSrc[j].getCoordinates()[0]);
            A.setElement(j, 1, ptSrc[j].getCoordinates()[1]);
            A.setElement(j, 2, 1);
            A.setElement(j, 3, 0);
            A.setElement(j, 4, 0);
            A.setElement(j, 5, 0);

            X.setElement(j, 0, ptDst[j].getCoordinates()[0]);
        }

        for (int j = numRow / 2; j < numRow; j++) {
            A.setElement(j, 0, 0);
            A.setElement(j, 1, 0);
            A.setElement(j, 2, 0);
            A.setElement(j, 3, ptSrc[j - (numRow / 2)].getCoordinates()[0]);
            A.setElement(j, 4, ptSrc[j - (numRow / 2)].getCoordinates()[1]);
            A.setElement(j, 5, 1);

            X.setElement(j, 0, ptDst[j - (numRow / 2)].getCoordinates()[1]);
        }

        GeneralMatrix AT = (GeneralMatrix) A.clone();
        AT.transpose();

        return calculateLSM(A, X);
    }

    /**
     * Returns the matrix for Projective transformation setup as
     * Affine. The M matrix looks like this:
     * <pre>                                                       
     * [  m00  m01  m02  ]                           
     * [  m10  m11  m12  ]                              
     * [   0    0    1   ]                                                                   
     * </pre>
     *
     * @return Matrix M.
     */
    protected GeneralMatrix getProjectiveMatrix() {
        GeneralMatrix M = new GeneralMatrix(3, 3);
        double[] param = generateMMatrix();
        double[] m0 = { param[0], param[1], param[2] };
        double[] m1 = { param[3], param[4], param[5] };
        double[] m2 = { 0, 0, 1 };
        M.setRow(0, m0);
        M.setRow(1, m1);
        M.setRow(2, m2);

        return M;
    }
}
