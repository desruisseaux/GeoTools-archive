/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.referencing.operation.transform;

// J2SE dependencies
import java.io.Serializable;

// OpenGIS dependencies
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;
import org.opengis.spatialschema.geometry.DirectPosition;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.referencing.operation.GeneralMatrix;
import org.geotools.referencing.wkt.Formatter;


/**
 * Transform which passes through a subset of ordinates to another transform.
 * This allows transforms to operate on a subset of ordinates.  For example,
 * if you have (<var>latitude</var>,<var>longitude</var>,<var>height</var>)
 * coordinates, then you may wish to convert the height values from feet to
 * meters without affecting the latitude and longitude values.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class PassThroughTransform extends AbstractMathTransform implements Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -1673997634240223449L;
    
    /**
     * Index of the first affected ordinate.
     */
    protected final int firstAffectedOrdinate;
    
    /**
     * Number of unaffected ordinates after the affected ones.
     * Always 0 when used through the strict OpenGIS API.
     */
    protected final int numTrailingOrdinates;
    
    /**
     * The sub transform.
     */
    protected final MathTransform transform;
    
    /**
     * The inverse transform. This field will be computed only when needed.
     * But it is serialized in order to avoid rounding error.
     */
    private PassThroughTransform inverse;
    
    /**
     * Create a pass through transform.
     *
     * @param transform The sub transform.
     * @param firstAffectedOrdinate Index of the first affected ordinate.
     * @param numTrailingOrdinates Number of trailing ordinates to pass through.
     *        Affected ordinates will range from <code>firstAffectedOrdinate</code>
     *        inclusive to <code>dimTarget-numTrailingOrdinates</code> exclusive.
     */
    public PassThroughTransform(final MathTransform transform,
                                final int firstAffectedOrdinate,
                                final int numTrailingOrdinates)
    {
        if (transform instanceof PassThroughTransform) {
            final PassThroughTransform passThrough = (PassThroughTransform) transform;
            this.firstAffectedOrdinate = passThrough.firstAffectedOrdinate + firstAffectedOrdinate;
            this.numTrailingOrdinates  = passThrough.numTrailingOrdinates  + numTrailingOrdinates;
            this.transform             = passThrough.transform;
        }  else {
            this.firstAffectedOrdinate = firstAffectedOrdinate;
            this.numTrailingOrdinates  = numTrailingOrdinates;
            this.transform             = transform;
        }
    }

    /**
     * Ordered sequence of positive integers defining the positions in a coordinate
     * tuple of the coordinates affected by this pass-through transform. The returned
     * index are for source coordinates.
     *
     * @return The modified coordinates.
     */
    public int[] getModifiedCoordinates() {
        final int[] index = new int[transform.getDimSource()];
        for (int i=0; i<index.length; i++) {
            index[i] = i + firstAffectedOrdinate;
        }
        return index;
    }
    
    /**
     * Gets the dimension of input points.
     */
    public int getDimSource() {
        return firstAffectedOrdinate + transform.getDimSource() + numTrailingOrdinates;
    }
    
    /**
     * Gets the dimension of output points.
     */
    public int getDimTarget() {
        return firstAffectedOrdinate + transform.getDimTarget() + numTrailingOrdinates;
    }
    
    /**
     * Tests whether this transform does not move any points.
     */
    public boolean isIdentity() {
        return transform.isIdentity();
    }
    
    /**
     * Transforms a list of coordinate point ordinal values.
     */
    public void transform(final float[] srcPts, int srcOff,
                          final float[] dstPts, int dstOff, int numPts)
        throws TransformException
    {
        final int subDimSource = transform.getDimSource();
        final int subDimTarget = transform.getDimTarget();
        int srcStep = numTrailingOrdinates;
        int dstStep = numTrailingOrdinates;
        if (srcPts==dstPts && srcOff<dstOff) {
            final int dimSource = getDimSource();
            final int dimTarget = getDimTarget();
            srcOff += numPts * dimSource;
            dstOff += numPts * dimTarget;
            srcStep -= 2*dimSource;
            dstStep -= 2*dimTarget;
        }
        while (--numPts >= 0) {
            System.arraycopy   (srcPts, srcOff,                        dstPts, dstOff,              firstAffectedOrdinate);
            transform.transform(srcPts, srcOff+=firstAffectedOrdinate, dstPts, dstOff+=firstAffectedOrdinate,           1);
            System.arraycopy   (srcPts, srcOff+=subDimSource,          dstPts, dstOff+=subDimTarget, numTrailingOrdinates);
            srcOff += srcStep;
            dstOff += dstStep;
        }
    }
    
    /**
     * Transforms a list of coordinate point ordinal values.
     */
    public void transform(final double[] srcPts, int srcOff,
                          final double[] dstPts, int dstOff, int numPts)
        throws TransformException
    {
        final int subDimSource = transform.getDimSource();
        final int subDimTarget = transform.getDimTarget();
        int srcStep = numTrailingOrdinates;
        int dstStep = numTrailingOrdinates;
        if (srcPts==dstPts && srcOff<dstOff) {
            final int dimSource = getDimSource();
            final int dimTarget = getDimTarget();
            srcOff += numPts * dimSource;
            dstOff += numPts * dimTarget;
            srcStep -= 2*dimSource;
            dstStep -= 2*dimTarget;
        }
        while (--numPts >= 0) {
            System.arraycopy   (srcPts, srcOff,                        dstPts, dstOff,              firstAffectedOrdinate);
            transform.transform(srcPts, srcOff+=firstAffectedOrdinate, dstPts, dstOff+=firstAffectedOrdinate,           1);
            System.arraycopy   (srcPts, srcOff+=subDimSource,          dstPts, dstOff+=subDimTarget, numTrailingOrdinates);
            srcOff += srcStep;
            dstOff += dstStep;
        }
    }
    
    /**
     * Gets the derivative of this transform at a point.
     */
    public Matrix derivative(final DirectPosition point) throws TransformException {
        final int nSkipped = firstAffectedOrdinate + numTrailingOrdinates;
        final int transDim = transform.getDimSource();
        final int pointDim = point.getDimension();
        if (pointDim != transDim+nSkipped) {
            throw new MismatchedDimensionException(Resources.format(
                        ResourceKeys.ERROR_MISMATCHED_DIMENSION_$3, "point",
                        new Integer(pointDim), new Integer(transDim+nSkipped)));
        }
        final GeneralDirectPosition subPoint = new GeneralDirectPosition(transDim);
        for (int i=0; i<transDim; i++) {
            subPoint.ordinates[i] = point.getOrdinate(i + firstAffectedOrdinate);
        }
        return expand(wrap(transform.derivative(subPoint)),
                      firstAffectedOrdinate, numTrailingOrdinates, 0);
    }

    /**
     * Create a pass through transform from a matrix. This method is invoked when the
     * sub-transform can be express as a matrix. It is also invoked for computing the
     * matrix returned by {@link #derivative}.
     *
     * @param subMatrix The sub-transform as a matrix.
     * @param firstAffectedOrdinate Index of the first affected ordinate.
     * @param numTrailingOrdinates Number of trailing ordinates to pass through.
     * @param affine 0 if the matrix do not contains translation terms, or 1 if
     *        the matrix is an affine transform with translation terms.
     */
    static Matrix expand(final GeneralMatrix subMatrix,
                         final int firstAffectedOrdinate,
                         final int numTrailingOrdinates,
                         final int affine)
    {
        final int         nSkipped = firstAffectedOrdinate + numTrailingOrdinates;
        final int           numRow = subMatrix.getNumRow() - affine;
        final int           numCol = subMatrix.getNumCol() - affine;
        final GeneralMatrix matrix = new GeneralMatrix(numRow + nSkipped + affine,
                                                       numCol + nSkipped + affine);
        matrix.setZero();

        //  Set UL part to 1:   [ 1  0             ]
        //                      [ 0  1             ]
        //                      [                  ]
        //                      [                  ]
        //                      [                  ]
        for (int j=0; j<firstAffectedOrdinate; j++) {
            matrix.setElement(j, j, 1);
        }
        //  Set central part:   [ 1  0  0  0  0  0 ]
        //                      [ 0  1  0  0  0  0 ]
        //                      [ 0  0  ?  ?  ?  0 ]
        //                      [ 0  0  ?  ?  ?  0 ]
        //                      [                  ]
        subMatrix.copySubMatrix(0, 0, numRow, numCol,
                                firstAffectedOrdinate, firstAffectedOrdinate, matrix);
        
        //  Set LR part to 1:   [ 1  0  0  0  0  0 ]
        //                      [ 0  1  0  0  0  0 ]
        //                      [ 0  0  ?  ?  ?  0 ]
        //                      [ 0  0  ?  ?  ?  0 ]
        //                      [ 0  0  0  0  0  1 ]
        final int offset = numCol-numRow;
        final int numRowOut = numRow + nSkipped;
        for (int j=numRowOut-numTrailingOrdinates; j<numRowOut; j++) {
            matrix.setElement(j, j+offset, 1);
        }
        if (affine != 0) {
            // Copy the translation terms in the last column.
            subMatrix.copySubMatrix(0, numCol, numRow, affine,
                                    firstAffectedOrdinate, numCol+nSkipped, matrix);
            // Copy the last row as a safety, but it should contains only 0.
            subMatrix.copySubMatrix(numRow, 0, affine, numCol,
                                    numRow+nSkipped, firstAffectedOrdinate, matrix);
            // Copy the lower right corner, which should contains only 1.
            subMatrix.copySubMatrix(numRow, numCol, affine, affine,
                                    numRow+nSkipped, numCol+nSkipped, matrix);
        }
        return matrix;
    }
    
    /**
     * Creates the inverse transform of this object.
     */
    public MathTransform inverse() throws NoninvertibleTransformException {
        // No need to synchronize. Not a big deal if two objects are created.
        if (inverse == null) {
            inverse = new PassThroughTransform(transform.inverse(),
                                               firstAffectedOrdinate,
                                               numTrailingOrdinates);
            inverse.inverse = this;
        }
        return inverse;
    }
    
    /**
     * Returns a hash value for this transform.
     * This value need not remain consistent between
     * different implementations of the same class.
     */
    public int hashCode() {
        int code = (int)serialVersionUID + firstAffectedOrdinate + 37*numTrailingOrdinates;
        if (transform != null) {
            code ^= transform.hashCode();
        }
        return code;
    }
    
    /**
     * Compares the specified object with this math transform for equality.
     */
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final PassThroughTransform that = (PassThroughTransform) object;
            return this.firstAffectedOrdinate == that.firstAffectedOrdinate &&
                   this.numTrailingOrdinates  == that.numTrailingOrdinates  &&
                   Utilities.equals(this.transform, that.transform);
        }
        return false;
    }
    
    /**
     * Format the inner part of a
     * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
     * Known Text</cite> (WKT)</A> element.
     *
     * @param  formatter The formatter to use.
     * @return The WKT element name.
     *
     * @todo The {@link #numTrailingOrdinates} parameter is not part of OpenGIS specification.
     *       We should returns a more complex WKT when <code>numTrailingOrdinates != 0</code>,
     *       using an affine transform to change the coordinates order.
     */
    protected String formatWKT(final Formatter formatter) {
        formatter.append(firstAffectedOrdinate);
        if (numTrailingOrdinates != 0) {
            formatter.append(numTrailingOrdinates);
        }
        formatter.append(transform);
        return "PASSTHROUGH_MT";
    }
}
