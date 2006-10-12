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
package org.geotools.referencing.operation.calculator.algorithm;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.operation.transform.AbstractMathTransform;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.DirectPosition;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


/**
 * The class RubberSheetTransform provides the transformation method based
 * on RubberSheeting (also known as Billinear interpolted transformation) The
 * class is generated by RubberSheetCalculator. Document about the this
 * tranformation can be seen <a href =
 * "http://planner.t.u-tokyo.ac.jp/member/fuse/rubber_sheeting.pdf">here</a>.
 *
 * @author jezekjan
 */
public class RubberSheetTransform extends AbstractMathTransform
    implements MathTransform2D {
    /**
     * The HashMap where the keys are the original {@linkPlain
     * #Polygon} and values are {@linkPlain
     * #org.opengis.referencing.operation.MathTransform}.
     */
    private HashMap trianglesToKeysMap;

/**
 * Constructs the RubberSheetTransform.
 * 
 * @param trianglesToAffineTransform The HashMap where the keys are the original {@link org.geotools.referencing.operation.calculator.algorithm.TINTriangle} and values
 * are {@link org.opengis.referencing.operation.MathTransform}.
 */
    public RubberSheetTransform(HashMap trianglesToAffineTransform) {
        this.trianglesToKeysMap = trianglesToAffineTransform;
    }

    /* (non-Javadoc)
     * @see org.geotools.referencing.operation.transform.AbstractMathTransform#getSourceDimensions()
     */
    public int getSourceDimensions() {
        return 2;
    }

    /* (non-Javadoc)
     * @see org.geotools.referencing.operation.transform.AbstractMathTransform#getTargetDimensions()
     */
    public int getTargetDimensions() {
        return 2;
    }

    /**
     * The method for generating a Map of Triangles mapped to ArrayList
     * of points within this triangle.
     *
     * @param arraySrcPts of source points
     *
     * @return HashMap of wher keys are triangles and values are  ArrayLists of
     *         points within these triangles
     */
    private HashMap mapTrianglesToPoints(ArrayList arraySrcPts) {
        HashMap trianglesToPoints = new HashMap();

        for (Iterator i = trianglesToKeysMap.keySet().iterator(); i.hasNext();) {
            TINTriangle triangle = (TINTriangle) i.next();
            ArrayList pointsInTriangle = new ArrayList();

            for (Iterator k = arraySrcPts.iterator(); k.hasNext();) {
                DirectPosition p = ((DirectPosition) k.next());

                if (triangle.containsOrIsVertex(p)) {
                    pointsInTriangle.add(p);

                    //arraySrcPts.remove(p);
                }
            }

            trianglesToPoints.put(triangle, pointsInTriangle);
        }

        return trianglesToPoints;
    }

    /* (non-Javadoc)
     * @see org.opengis.referencing.operation.MathTransform#transform(double[], int, double[], int, int)
     */
    public void transform(double[] srcPts, int srcOff, final double[] dstPts,
        int dstOff, int numPts) throws TransformException {
        MappedPosition ptSrc = null;

        ArrayList arraySrcPts = new ArrayList();

        while (--numPts >= 0) {
            final DirectPosition coSrc = new DirectPosition2D(srcPts[srcOff++],
                    srcPts[srcOff++]);
            DirectPosition coDst = new DirectPosition2D(0, 0);
            ptSrc = new MappedPosition(coSrc, coDst);

            arraySrcPts.add(ptSrc);
        }

        HashMap trianglestoPoints = mapTrianglesToPoints((ArrayList) arraySrcPts);

        // cicle  goes throught each triangle 
        for (Iterator k = trianglestoPoints.keySet().iterator(); k.hasNext();) {
            TINTriangle triangle = (TINTriangle) k.next();
            AffineTransform AT = (AffineTransform) trianglesToKeysMap.get(triangle);

            // cicle for transforming points within this triangle
            for (Iterator j = ((ArrayList) trianglestoPoints.get(triangle))
                    .iterator(); j.hasNext();) {
                MappedPosition co = (MappedPosition) j.next();
                Point2D ptS = new Point2D.Double(co.x, co.y);
                Point2D ptD = new Point2D.Double();
                AT.transform(ptS, ptD);

                DirectPosition coDst = new DirectPosition2D(co
                        .getCoordinateReferenceSystem(), ptD.getX(), ptD.getY());
                co.setMappedposition(coDst);
            }
        }

        for (Iterator j = arraySrcPts.iterator(); j.hasNext();) {
            MappedPosition mc = (MappedPosition) j.next();
            dstPts[dstOff++] = mc.getMappedposition().getCoordinates()[0];
            dstPts[dstOff++] = mc.getMappedposition().getCoordinates()[1];
        }
    }

    /**
     * String representation.
     *
     * @return String expresion of the triangle and its affine transform
     *         parameters
     */
    public String toString() {
        String theString = "";

        for (Iterator i = trianglesToKeysMap.keySet().iterator(); i.hasNext();) {
            TINTriangle trian = (TINTriangle) i.next();
            MathTransform mt = (MathTransform) trianglesToKeysMap.get(trian);
            theString = theString + trian.toString() + "\n";
            theString = theString + mt.toString() + "\n";
        }

        return theString;
    }
}
