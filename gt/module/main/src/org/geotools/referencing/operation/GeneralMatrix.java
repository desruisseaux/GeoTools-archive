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
package org.geotools.referencing.operation;

// J2SE dependencies
import java.awt.geom.AffineTransform;
import java.text.FieldPosition;
import java.text.NumberFormat;

import javax.vecmath.GMatrix;

import org.geotools.resources.Utilities;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.resources.cts.Resources;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.operation.Matrix;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;


/**
 * A two dimensional array of numbers. Row and column numbering begins with zero.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see javax.vecmath.GMatrix
 * @see java.awt.geom.AffineTransform
 * @see javax.media.jai.PerspectiveTransform
 * @see javax.media.j3d.Transform3D
 * @see <A HREF="http://math.nist.gov/javanumerics/jama/">Jama matrix</A>
 * @see <A HREF="http://jcp.org/jsr/detail/83.jsp">JSR-83 Multiarray package</A>
 */
public class GeneralMatrix extends GMatrix implements Matrix {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 2315556279279988442L;
    
    /**
     * Construct a square identity matrix of size
     * <code>size</code>&nbsp;&times;&nbsp;<code>size</code>.
     */
    public GeneralMatrix(final int size) {
        super(size,size);
    }
    
    /**
     * Construct a matrix of size
     * <code>numRow</code>&nbsp;&times;&nbsp;<code>numCol</code>.
     * Elements on the diagonal <var>j==i</var> are set to 1.
     */
    public GeneralMatrix(final int numRow, final int numCol) {
        super(numRow, numCol);
    }
    
    /**
     * Constructs a <code>numRow</code>&nbsp;&times;&nbsp;<code>numCol</code> matrix
     * initialized to the values in the <code>matrix</code> array. The array values
     * are copied in one row at a time in row major fashion. The array should be
     * exactly <code>numRow*numCol</code> in length. Note that because row and column
     * numbering begins with zero, <code>row</code> and <code>numCol</code> will be
     * one larger than the maximum possible matrix index values.
     */
    public GeneralMatrix(final int numRow, final int numCol, final double[] matrix) {
        super(numRow, numCol, matrix);
        if (numRow*numCol != matrix.length) {
            throw new IllegalArgumentException(String.valueOf(matrix.length));
        }
    }
    
    /**
     * Constructs a new matrix from a two-dimensional array of doubles.
     *
     * @param  matrix Array of rows. Each row must have the same length.
     * @throws IllegalArgumentException if the specified matrix is not regular
     *         (i.e. if all rows doesn't have the same length).
     */
    public GeneralMatrix(final double[][] matrix) throws IllegalArgumentException {
        super(matrix.length, (matrix.length!=0) ? matrix[0].length : 0);
        final int numRow = getNumRow();
        final int numCol = getNumCol();
        for (int j=0; j<numRow; j++) {
            if (matrix[j].length!=numCol) {
                throw new IllegalArgumentException(Resources.format(ResourceKeys.ERROR_MATRIX_NOT_REGULAR));
            }
            setRow(j, matrix[j]);
        }
    }
    
    /**
     * Constructs a new matrix and copies the initial values from the parameter matrix.
     */
    public GeneralMatrix(final Matrix matrix) {
        this(matrix.getNumRow(), matrix.getNumCol());
        final int height = getNumRow();
        final int width  = getNumCol();
        for (int j=0; j<height; j++) {
            for (int i=0; i<width; i++) {
                setElement(j, i, matrix.getElement(j, i));
            }
        }
    }
    
    /**
     * Constructs a new matrix and copies the initial values from the parameter matrix.
     */
    public GeneralMatrix(final GMatrix matrix) {
        super(matrix);
    }
    
    /**
     * Construct a 3&times;3 matrix from the specified affine transform.
     */
    public GeneralMatrix(final AffineTransform transform) {
        super(3,3, new double[] {
            transform.getScaleX(), transform.getShearX(), transform.getTranslateX(),
            transform.getShearY(), transform.getScaleY(), transform.getTranslateY(),
            0,                     0,                     1
        });
        assert isAffine() : this;
    }
    
    /**
     * Constructs a transform that maps a source region to a destination region.
     * Axis order and direction are left unchanged.
     *
     * <P>If the source dimension is equals to the destination dimension,
     * then the transform is affine. However, the following special cases
     * are also handled:</P>
     *
     * <UL>
     *   <LI>If the target dimension is smaller than the source dimension,
     *       then extra dimensions are dropped.</LI>
     *   <LI>If the target dimension is greater than the source dimension,
     *       then the coordinates in the new dimensions are set to 0.</LI>
     * </UL>
     *
     * @param srcRegion The source region.
     * @param dstRegion The destination region.
     */
    public GeneralMatrix(final Envelope srcRegion,
                         final Envelope dstRegion)
    {
        super(dstRegion.getDimension()+1, srcRegion.getDimension()+1);
        // Next lines should be first if only Sun could fix RFE #4093999 (sigh...)
        final int srcDim = srcRegion.getDimension();
        final int dstDim = dstRegion.getDimension();
        for (int i=Math.min(srcDim, dstDim); --i>=0;) {
            double scale     = dstRegion.getLength (i) / srcRegion.getLength (i);
            double translate = dstRegion.getMinimum(i) - srcRegion.getMinimum(i)*scale;
            setElement(i, i,         scale);
            setElement(i, srcDim, translate);
        }
        setElement(dstDim, srcDim, 1);
        assert (srcDim != dstDim) || isAffine() : this;
    }
    
    /**
     * Constructs a transform changing axis order and/or direction.
     * For example, the transform may converts (NORTH,WEST) coordinates
     * into (EAST,NORTH). Axis direction can be inversed only. For example,
     * it is illegal to transform (NORTH,WEST) coordinates into (NORTH,DOWN).
     *
     * <P>If the source dimension is equals to the destination dimension,
     * then the transform is affine. However, the following special cases
     * are also handled:</P>
     * <BR>
     * <UL>
     *   <LI>If the target dimension is smaller than the source dimension,
     *       extra axis are dropped. An exception is thrown if the target
     *       contains some axis not found in the source.</LI>
     * </UL>
     *
     * @param  srcAxis The set of axis direction for source coordinate system.
     * @param  dstAxis The set of axis direction for destination coordinate system.
     * @throws IllegalArgumentException If <code>dstAxis</code> contains some axis
     *         not found in <code>srcAxis</code>, or if some colinear axis were found.
     */
    public GeneralMatrix(final AxisDirection[] srcAxis,
                         final AxisDirection[] dstAxis)
    {
        this(null, srcAxis, null, dstAxis, false);
    }
    
    /**
     * Constructs a transform mapping a source region to a destination region.
     * Axis order and/or direction can be changed during the process.
     * For example, the transform may convert (NORTH,WEST) coordinates
     * into (EAST,NORTH). Axis direction can be inversed only. For example,
     * it is illegal to transform (NORTH,WEST) coordinates into (NORTH,DOWN).
     *
     * <P>If the source dimension is equals to the destination dimension,
     * then the transform is affine. However, the following special cases
     * are also handled:</P>
     * <BR>
     * <UL>
     *   <LI>If the target dimension is smaller than the source dimension,
     *       extra axis are dropped. An exception is thrown if the target
     *       contains some axis not found in the source.</LI>
     * </UL>
     *
     * @param srcRegion The source region.
     * @param srcAxis   Axis direction for each dimension of the source region.
     * @param dstRegion The destination region.
     * @param dstAxis   Axis direction for each dimension of the destination region.
     * @throws MismatchedDimensionException if the envelope dimension doesn't
     *         matches the axis direction array length.
     * @throws IllegalArgumentException If <code>dstAxis</code> contains some axis
     *         not found in <code>srcAxis</code>, or if some colinear axis were found.
     */
    public GeneralMatrix(final Envelope srcRegion, final AxisDirection[] srcAxis,
                         final Envelope dstRegion, final AxisDirection[] dstAxis)
    {
        this(srcRegion, srcAxis, dstRegion, dstAxis, true);
    }
    
    /**
     * Implementation of constructors expecting envelope and/or axis directions.
     *
     * @param validRegions   <code>true</code> if source and destination regions must
     *        be taken in account. If <code>false</code>, then source and destination
     *        regions will be ignored and may be null.
     */
    private GeneralMatrix(final Envelope srcRegion, final AxisDirection[] srcAxis,
                          final Envelope dstRegion, final AxisDirection[] dstAxis,
                          final boolean validRegions)
    {
        super(dstAxis.length+1, srcAxis.length+1);
        if (validRegions) {
            ensureDimensionMatch("srcRegion", srcRegion, srcAxis.length);
            ensureDimensionMatch("dstRegion", dstRegion, dstAxis.length);
        }
        /*
         * Map source axis to destination axis.  If no axis is moved (for example if the user
         * want to transform (NORTH,EAST) to (SOUTH,EAST)), then source and destination index
         * will be equal.   If some axis are moved (for example if the user want to transform
         * (NORTH,EAST) to (EAST,NORTH)),  then ordinates at index <code>srcIndex</code> will
         * have to be moved at index <code>dstIndex</code>.
         */
        setZero();
        for (int dstIndex=0; dstIndex<dstAxis.length; dstIndex++) {
            boolean hasFound = false;
            final AxisDirection dstAxe = dstAxis[dstIndex];
            final AxisDirection search = dstAxe.absolute();
            for (int srcIndex=0; srcIndex<srcAxis.length; srcIndex++) {
                final AxisDirection srcAxe = srcAxis[srcIndex];
                if (search.equals(srcAxe.absolute())) {
                    if (hasFound) {
                        // TODO: Use the localized version of 'getName' in GeoAPI 1.1
                        throw new IllegalArgumentException(
                                    Resources.format(ResourceKeys.ERROR_COLINEAR_AXIS_$2,
                                    srcAxe.name(), dstAxe.name()));
                    }
                    hasFound = true;
                    /*
                     * Set the matrix elements. Some matrix elements will never
                     * be set. They will be left to zero, which is their wanted
                     * value.
                     */
                    final boolean normal = srcAxe.equals(dstAxe);
                    double scale = (normal) ? +1 : -1;
                    double translate = 0;
                    if (validRegions) {
                        translate  = (normal) ? dstRegion.getMinimum(dstIndex)
                                              : dstRegion.getMaximum(dstIndex);
                        scale     *= dstRegion.getLength(dstIndex) /
                                     srcRegion.getLength(srcIndex);
                        translate -= srcRegion.getMinimum(srcIndex) * scale;
                    }
                    setElement(dstIndex, srcIndex,       scale);
                    setElement(dstIndex, srcAxis.length, translate);
                }
            }
            if (!hasFound) {
                // TODO: Use the localized version of 'getName' in GeoAPI 1.1
                throw new IllegalArgumentException(Resources.format(
                            ResourceKeys.ERROR_NO_SOURCE_AXIS_$1, dstAxis[dstIndex].name()));
            }
        }
        setElement(dstAxis.length, srcAxis.length, 1);
        assert (srcAxis.length != dstAxis.length) || isAffine() : this;
    }
    
    /**
     * Convenience method for checking object dimension validity.
     * This method is usually invoked for argument checking.
     *
     * @param  name      The name of the argument to check.
     * @param  envelope  The envelope to check.
     * @param  dimension The expected dimension for the object.
     * @throws MismatchedDimensionException if the envelope doesn't have the expected dimension.
     */
    private static void ensureDimensionMatch(final String   name,
					     final Envelope envelope,
                                             final int      dimension)
        throws MismatchedDimensionException
    {
        final int dim = envelope.getDimension();
        if (dimension != dim) {
            throw new MismatchedDimensionException(Resources.format(
                        ResourceKeys.ERROR_MISMATCHED_DIMENSION_$3, name,
                        new Integer(dim), new Integer(dimension)));
        }
    }

    /**
     * Wrap the specified matrix in a Geotools implementation of {@link Matrix}.
     * If <code>matrix</code> is already an instance of <code>GeneralMatrix</code>,
     * then it is returned unchanged. Otherwise, all elements are copied in a new
     * <code>GeneralMatrix</code> object.
     */
    public static GeneralMatrix wrap(final Matrix matrix) {
        if (matrix instanceof GeneralMatrix) {
            return (GeneralMatrix) matrix;
        } else {
            return new GeneralMatrix(matrix);
        }
    }
    
    /**
     * Retrieves the specifiable values in the transformation matrix into a
     * 2-dimensional array of double precision values. The values are stored
     * into the 2-dimensional array using the row index as the first subscript
     * and the column index as the second. Values are copied; changes to the
     * returned array will not change this matrix.
     */
    public final double[][] getElements() {
        final int numCol = getNumCol();
        final double[][] matrix = new double[getNumRow()][];
        for (int j=0; j<matrix.length; j++) {
            getRow(j, matrix[j]=new double[numCol]);
        }
        return matrix;
    }
    
    /**
     * Returns <code>true</code> if this matrix is an affine transform.
     * A transform is affine if the matrix is square and last row contains
     * only zeros, except in the last column which contains 1.
     */
    public final boolean isAffine() {
        int dimension  = getNumRow();
        if (dimension != getNumCol()) {
            return false;
        }
        dimension--;
        for (int i=0; i<=dimension; i++) {
            if (getElement(dimension, i) != (i==dimension ? 1 : 0)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Returns <code>true</code> if this matrix is an identity matrix.
     */
    public final boolean isIdentity() {
        final int numRow = getNumRow();
        final int numCol = getNumCol();
        if (numRow != numCol) {
            return false;
        }
        for (int j=0; j<numRow; j++) {
            for (int i=0; i<numCol; i++) {
                if (getElement(j,i) != (i==j ? 1 : 0)) {
                    return false;
                }
            }
        }
        assert isAffine() : this;
        return true;
    }
    
    /**
     * Returns an affine transform for this matrix.
     * This is a convenience method for interoperability with Java2D.
     *
     * @throws IllegalStateException if this matrix is not 3x3,
     *         or if the last row is not [0 0 1].
     */
    public final AffineTransform toAffineTransform2D() throws IllegalStateException {
        int check;
        if ((check=getNumRow())!=3 || (check=getNumCol())!=3) {
            throw new IllegalStateException(Resources.format(
                        ResourceKeys.ERROR_NOT_TWO_DIMENSIONAL_$1, new Integer(check-1)));
        }
        if (isAffine()) {
            return new AffineTransform(getElement(0,0), getElement(1,0),
            getElement(0,1), getElement(1,1),
            getElement(0,2), getElement(1,2));
        }
        throw new IllegalStateException(Resources.format(
                    ResourceKeys.ERROR_NOT_AN_AFFINE_TRANSFORM));
    }
    
    /**
     * Returns a string representation of this matrix.
     * The returned string is implementation dependent.
     * It is usually provided for debugging purposes only.
     */
    public String toString() {
        final int    numRow = getNumRow();
        final int    numCol = getNumCol();
        StringBuffer buffer = new StringBuffer();
        final int      columnWidth = 12;
        final String lineSeparator = System.getProperty("line.separator", "\n");
        final FieldPosition  dummy = new FieldPosition(0);
        final NumberFormat  format = NumberFormat.getNumberInstance();
        format.setMinimumFractionDigits(6);
        format.setMaximumFractionDigits(6);
        for (int j=0; j<numRow; j++) {
            for (int i=0; i<numCol; i++) {
                final int position = buffer.length();
                buffer = format.format(getElement(j,i), buffer, dummy);
                buffer.insert(position, Utilities.spaces(columnWidth-(buffer.length()-position)));
            }
            buffer.append(lineSeparator);
        }
        return buffer.toString();
    }
}
