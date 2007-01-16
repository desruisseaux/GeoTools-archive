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
package org.geotools.referencing.operation.builder.algorithm;

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
 * This provides the transformation method based on RubberSheeting (also
 * known as Billinear interpolated transformation) The class is accessed
 * {@linkplain org.geotools.referencing.operation.builder.RubberSheetBuilder
 * RubberSheetBuilder}. More about Rubber Sheet transformation can be seen <a
 * href =
 * "http://planner.t.u-tokyo.ac.jp/member/fuse/rubber_sheeting.pdf">here</a>.
 *
 * @since 2.4
 * @author Jan Jezek
 *
 * @todo Consider moving this class to the {@linkplain
 *       org.geotools.referencing.operation.transform} package.
 */
public class RubberSheetTransform extends AbstractMathTransform
    implements MathTransform2D {
    /**
     * The HashMap where the keys are the original {@link
     * Polygon} and values are {@link
     * #org.opengis.referencing.operation.MathTransform}.
     */
    private HashMap trianglesToKeysMap;

/**
     * Constructs the RubberSheetTransform.
     * 
     * @param trianglesToAffineTransform The HashMap where the keys are the original
     *        {@linkplain org.geotools.referencing.operation.builder.algorithm.TINTriangle}
     *        and values are {@linkplain org.opengis.referencing.operation.MathTransform}.
     */
    public RubberSheetTransform(HashMap trianglesToAffineTransform) {
        this.trianglesToKeysMap = trianglesToAffineTransform;
    }

    /**
     * Gets the dimension of input points, which is 2.
     *
     * @return dimension of input points
     */
    public final int getSourceDimensions() {
        return 2;
    }

    /**
     * Gets the dimension of output points, which is 2.
     *
     * @return imension of output points
     */
    public final int getTargetDimensions() {
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
        ExtendedPosition ptSrc = null;

        ArrayList arraySrcPts = new ArrayList();

        while (--numPts >= 0) {
            final DirectPosition coSrc = new DirectPosition2D(srcPts[srcOff++],
                    srcPts[srcOff++]);
            DirectPosition coDst = new DirectPosition2D(0, 0);
            ptSrc = new ExtendedPosition(coSrc, coDst);

            arraySrcPts.add(ptSrc);
        }

        HashMap trianglestoPoints = mapTrianglesToPoints((ArrayList) arraySrcPts);

        // Circle goes trough each triangle
        for (Iterator k = trianglestoPoints.keySet().iterator(); k.hasNext();) {
            TINTriangle triangle = (TINTriangle) k.next();
            AffineTransform AT = (AffineTransform) trianglesToKeysMap.get(triangle);

            // Circle for transforming points within this triangle
            for (Iterator j = ((ArrayList) trianglestoPoints.get(triangle))
                    .iterator(); j.hasNext();) {
                ExtendedPosition co = (ExtendedPosition) j.next();
                Point2D ptS = new Point2D.Double(co.x, co.y);
                Point2D ptD = new Point2D.Double();
                AT.transform(ptS, ptD);

                DirectPosition coDst = new DirectPosition2D(co
                        .getCoordinateReferenceSystem(), ptD.getX(), ptD.getY());
                co.setMappedposition(coDst);
            }
        }

        for (Iterator j = arraySrcPts.iterator(); j.hasNext();) {
            ExtendedPosition mc = (ExtendedPosition) j.next();
            dstPts[dstOff++] = mc.getMappedposition().getCoordinates()[0];
            dstPts[dstOff++] = mc.getMappedposition().getCoordinates()[1];
        }
    }

    /**
     * String representation.
     *
     * @return String expression of the triangle and its affine transform
     *         parameters
     *
     * @todo This method doesn't meet the {@link MathTransform#toString}
     *       constract, which should uses Well Known Text (WKT) format as much
     *       as possible.
     */
    public String toString() {
        final String lineSeparator = System.getProperty("line.separator", "\n");
        final StringBuffer buffer = new StringBuffer();

        for (final Iterator i = trianglesToKeysMap.keySet().iterator();
                i.hasNext();) {
            TINTriangle trian = (TINTriangle) i.next();
            MathTransform mt = (MathTransform) trianglesToKeysMap.get(trian);
            buffer.append(trian.toString());
            buffer.append(lineSeparator);
            buffer.append(mt.toString());
            buffer.append(lineSeparator);
        }

        return buffer.toString();
    }
}
