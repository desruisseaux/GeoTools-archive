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
import org.geotools.referencing.operation.LinearTransform;
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
 *
 * @todo This implementation is not really needed. We should concatenate the sub-transform
 *       with a ProjectiveTransform backed by a non-square matrix instead; it would provide
 *       more flexibility (especially toward subsequent concatenation). We could keep this
 *       implementation without 'sub-transform' as an optimization of ProjectiveTransform
 *       in the special case of passthrough transform. It would not need to be a public class
 *       however. We could provide a 'createPassThroughTransform' convenience method right
 *       into ProjectiveTransform for use by the GridCoverage package.
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
    protected final MathTransform subTransform;
    
    /**
     * The inverse transform. This field will be computed only when needed.
     * But it is serialized in order to avoid rounding error.
     */
    private PassThroughTransform inverse;
    
    /**
     * Create a pass through transform.
     *
     * @param firstAffectedOrdinate Index of the first affected ordinate.
     * @param subTransform The sub transform.
     * @param numTrailingOrdinates Number of trailing ordinates to pass through.
     *        Affected ordinates will range from <code>firstAffectedOrdinate</code>
     *        inclusive to <code>dimTarget-numTrailingOrdinates</code> exclusive.
     */
    protected PassThroughTransform(final int firstAffectedOrdinate,
                                   final MathTransform subTransform,
                                   final int numTrailingOrdinates)
    {
        if (firstAffectedOrdinate < 0) {
            throw new IllegalArgumentException(Resources.format(
                    ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2,
                    "firstAffectedOrdinate", new Integer(firstAffectedOrdinate)));
        }
        if (numTrailingOrdinates < 0) {
            throw new IllegalArgumentException(Resources.format(
                    ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2,
                    "numTrailingOrdinates", new Integer(numTrailingOrdinates)));
        }
        if (subTransform instanceof PassThroughTransform) {
            final PassThroughTransform passThrough = (PassThroughTransform) subTransform;
            this.firstAffectedOrdinate = passThrough.firstAffectedOrdinate + firstAffectedOrdinate;
            this.numTrailingOrdinates  = passThrough.numTrailingOrdinates  + numTrailingOrdinates;
            this.subTransform          = passThrough.subTransform;
        }  else {
            this.firstAffectedOrdinate = firstAffectedOrdinate;
            this.numTrailingOrdinates  = numTrailingOrdinates;
            this.subTransform          = subTransform;
        }
    }

    /**
     * Creates a transform which passes through a subset of ordinates to another transform.
     * This allows transforms to operate on a subset of ordinates. For example, if you have
     * (<var>latitidue</var>,<var>longitude</var>,<var>height</var>) coordinates, then you
     * may wish to convert the height values from feet to meters without affecting the
     * latitude and longitude values.
     *
     * @param  firstAffectedOrdinate Index of the first affected ordinate.
     * @param  subTransform The sub transform.
     * @param  numTrailingOrdinates Number of trailing ordinates to pass through.
     *         Affected ordinates will range from <code>firstAffectedOrdinate</code>
     *         inclusive to <code>dimTarget-numTrailingOrdinates</code> exclusive.
     * @return A pass through transform with the following dimensions:<br>
     *         <pre>
     * Source: firstAffectedOrdinate + subTransform.getDimSource() + numTrailingOrdinates
     * Target: firstAffectedOrdinate + subTransform.getDimTarget() + numTrailingOrdinates</pre>
     */
    public static MathTransform create(final int firstAffectedOrdinate,
                                       final MathTransform subTransform,
                                       final int numTrailingOrdinates)
    {
        if (firstAffectedOrdinate < 0) {
            throw new IllegalArgumentException(Resources.format(
                    ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2,
                    "firstAffectedOrdinate", new Integer(firstAffectedOrdinate)));
        }
        if (numTrailingOrdinates < 0) {
            throw new IllegalArgumentException(Resources.format(
                    ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2,
                    "numTrailingOrdinates", new Integer(numTrailingOrdinates)));
        }
        if (firstAffectedOrdinate==0 && numTrailingOrdinates==0) {
            return subTransform;
        }
        /*
         * Optimize the "Identity transform" case.
         */
        if (subTransform.isIdentity()) {
            final int dimension = subTransform.getDimSource();
            if (dimension == subTransform.getDimTarget()) {
                return IdentityTransform.create(firstAffectedOrdinate + dimension + numTrailingOrdinates);
            }
        }
        /*
         * Special case for transformation backed by a matrix. Is is possible to use a
         * new matrix for such transform, instead of wrapping the sub-transform into a
         * PassThroughTransform object. It is faster and easier to concatenate.
         */
        if (subTransform instanceof LinearTransform) {
            GeneralMatrix matrix = ProjectiveTransform.wrap(((LinearTransform)subTransform).getMatrix());
            matrix = PassThroughTransform.expand(matrix, firstAffectedOrdinate, numTrailingOrdinates, 1);
            return ProjectiveTransform.create(matrix);
        }
        /*
         * Construct the general PassThroughTransform object. An optimisation
         * for the "Pass through case" is done right in the  constructor.
         */
        return new PassThroughTransform(firstAffectedOrdinate, subTransform, numTrailingOrdinates);
    }

    /**
     * Ordered sequence of positive integers defining the positions in a coordinate
     * tuple of the coordinates affected by this pass-through transform. The returned
     * index are for source coordinates.
     *
     * @return The modified coordinates.
     */
    public int[] getModifiedCoordinates() {
        final int[] index = new int[subTransform.getDimSource()];
        for (int i=0; i<index.length; i++) {
            index[i] = i + firstAffectedOrdinate;
        }
        return index;
    }
    
    /**
     * Gets the dimension of input points.
     */
    public int getDimSource() {
        return firstAffectedOrdinate + subTransform.getDimSource() + numTrailingOrdinates;
    }
    
    /**
     * Gets the dimension of output points.
     */
    public int getDimTarget() {
        return firstAffectedOrdinate + subTransform.getDimTarget() + numTrailingOrdinates;
    }
    
    /**
     * Tests whether this transform does not move any points.
     */
    public boolean isIdentity() {
        return subTransform.isIdentity();
    }
    
    /**
     * Transforms a list of coordinate point ordinal values.
     */
    public void transform(final float[] srcPts, int srcOff,
                          final float[] dstPts, int dstOff, int numPts)
        throws TransformException
    {
        final int subDimSource = subTransform.getDimSource();
        final int subDimTarget = subTransform.getDimTarget();
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
            System.arraycopy      (srcPts, srcOff,                        dstPts, dstOff,              firstAffectedOrdinate);
            subTransform.transform(srcPts, srcOff+=firstAffectedOrdinate, dstPts, dstOff+=firstAffectedOrdinate,           1);
            System.arraycopy      (srcPts, srcOff+=subDimSource,          dstPts, dstOff+=subDimTarget, numTrailingOrdinates);
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
        final int subDimSource = subTransform.getDimSource();
        final int subDimTarget = subTransform.getDimTarget();
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
            System.arraycopy      (srcPts, srcOff,                        dstPts, dstOff,              firstAffectedOrdinate);
            subTransform.transform(srcPts, srcOff+=firstAffectedOrdinate, dstPts, dstOff+=firstAffectedOrdinate,           1);
            System.arraycopy      (srcPts, srcOff+=subDimSource,          dstPts, dstOff+=subDimTarget, numTrailingOrdinates);
            srcOff += srcStep;
            dstOff += dstStep;
        }
    }
    
    /**
     * Gets the derivative of this transform at a point.
     */
    public Matrix derivative(final DirectPosition point) throws TransformException {
        final int nSkipped = firstAffectedOrdinate + numTrailingOrdinates;
        final int transDim = subTransform.getDimSource();
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
        return expand(wrap(subTransform.derivative(subPoint)),
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
    private static GeneralMatrix expand(final GeneralMatrix subMatrix,
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
            inverse = new PassThroughTransform(firstAffectedOrdinate,
                                               subTransform.inverse(),
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
        if (subTransform != null) {
            code ^= subTransform.hashCode();
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
                   Utilities.equals(this.subTransform, that.subTransform);
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
        formatter.append(subTransform);
        return "PASSTHROUGH_MT";
    }
}
