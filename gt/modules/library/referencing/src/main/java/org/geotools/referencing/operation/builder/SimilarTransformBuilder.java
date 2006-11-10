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

// J2SE and extensions
import javax.vecmath.MismatchedSizeException;

import org.geotools.referencing.operation.matrix.GeneralMatrix;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;
import org.opengis.spatialschema.geometry.MismatchedReferenceSystemException;


/**
 * The class for calculating 4 parameters of linear transformation (2D).
 * The calculation uses least square method. The similar transform equation:
 * <pre>                                                  
 *  [ x']   [  a -b  Tx  ] [ x ]   [ a*x - b*y + Tx ]
 *  [ y'] = [  b  a  Ty  ] [ y ] = [ b*x + a*y + Ty ] </pre>In the case
 * that we have more identical points we can write it like this (in Matrix):
 * <pre>                                           
 *  [ x'<sub>1</sub> ]      [ x<sub>1</sub> -y<sub>1</sub>  1 0 ]   [ a  ]
 *  [ x'<sub>2</sub> ]      [ x<sub>2</sub> -y<sub>2</sub>  1 0 ]   [ b  ]
 *  [  .  ]      [      .      ]   [ Tx ]                          
 *  [  .  ]      [      .      ] * [ Ty ]                          
 *  [ x'<sub>n</sub> ]   =  [ x<sub>n</sub>  y<sub>n</sub>  1 0 ]   
 *  [ y'<sub>1</sub> ]      [ y<sub>1</sub>  x<sub>1</sub>  0 1 ]   
 *  [ y'<sub>2</sub> ]      [ y<sub>2</sub>  x<sub>2</sub>  0 1 ]  
 *  [  .  ]      [      .      ]                                      
 *  [  .  ]      [      .      ]                                    
 *  [ y'<sub>n</sub> ]      [ y<sub>n</sub> x<sub>n</sub>  0  1 ]     
 *    x' = A*m  </pre>Using the least square method we get this result:
 * <pre><blockquote>
 *  m = (A<sup>T</sup>A)<sup>-1</sup> A<sup>T</sup>x'  </blockquote> </pre>
 *
 * @author Jan Jezek
 */
public class SimilarTransformBuilder extends ProjectiveTransformBuilder {
    /**
     * Creates SimilarTransformBuilder for the set of properties.
     *
     * @param sourcePoints Set of source points
     * @param targetPoints Set of destination points
     */
    public SimilarTransformBuilder(DirectPosition[] ptSrc, DirectPosition[] ptDst)
        throws MismatchedSizeException, MismatchedDimensionException, MismatchedReferenceSystemException {
        setTargetPoints(ptDst);
        setSourcePoints(ptSrc);
    }

    /**
     * Returns the minimum number of points required by this builder, which is 2.
     */
    public int getMinimumPointCount() {
        return 2;
    }

    /**
     * The method returns the array of similar transformation.
     * paremeters a,b, Tx, Ty.
     *
     * @return double array of parameters
     */
    public double[] generateMMatrix() {
        final DirectPosition[] sourcePoints = getSourcePoints();
        final DirectPosition[] targetPoints = getTargetPoints();
        GeneralMatrix A = new GeneralMatrix(2 * sourcePoints.length, 4);
        GeneralMatrix X = new GeneralMatrix(2 * sourcePoints.length, 1);

        int numRow = A.getNumRow();

        // Creates A matrix
        for (int j = 0; j < (numRow / 2); j++) {
            A.setElement(j, 0, sourcePoints[j].getCoordinates()[0]); //X
            A.setElement(j, 1, -sourcePoints[j].getCoordinates()[1]); //Y
            A.setElement(j, 2, 1);
            A.setElement(j, 3, 0);

            X.setElement(j, 0, targetPoints[j].getCoordinates()[0]);
        }

        for (int j = numRow / 2; j < numRow; j++) {
            A.setElement(j, 0, sourcePoints[j - (numRow / 2)].getCoordinates()[1]); //Y
            A.setElement(j, 1, sourcePoints[j - (numRow / 2)].getCoordinates()[0]); //X
            A.setElement(j, 2, 0);
            A.setElement(j, 3, 1);

            X.setElement(j, 0, targetPoints[j - (numRow / 2)].getCoordinates()[1]);
        }

        GeneralMatrix AT = (GeneralMatrix) A.clone();
        AT.transpose();

        GeneralMatrix ATA = new GeneralMatrix(4, 4);
        GeneralMatrix ATX = new GeneralMatrix(4, 1);
        GeneralMatrix x = new GeneralMatrix(4, 1);
        ATA.mul(AT, A);
        ATX.mul(AT, X);
        ATA.invert();
        ATX.mul(AT, X);
        x.mul(ATA, ATX);
        x.transpose();

        return x.getElements()[0];
    }

    /**
     * Returns the matrix for Projective transformation setup as
     * Affine. The M matrix looks like this:
     * <pre>                                                       
     * [  a  -b  Tx  ]                           
     * [  b   a  Ty  ]                              
     * [  0   0  1   ]                                                                   
     * </pre>
     *
     * @return Matrix M.
     */
    protected GeneralMatrix getProjectiveMatrix() {
        GeneralMatrix M = new GeneralMatrix(3, 3);
        double[] param = generateMMatrix();
        double[] m0 = { param[0], -param[1], param[2] };
        double[] m1 = { param[1], param[0], param[3] };
        double[] m2 = { 0, 0, 1 };
        M.setRow(0, m0);
        M.setRow(1, m1);
        M.setRow(2, m2);

        return M;
    }
}
