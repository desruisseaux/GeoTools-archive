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

// J2SE dependencies and extensions
import java.awt.geom.AffineTransform;
import javax.vecmath.GMatrix;

// OpenGIS dependencies
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.operation.Matrix;
import org.opengis.spatialschema.geometry.Envelope;


/**
 * A two dimensional array of numbers. Row and column numbering begins with zero.
 *
 * @since 2.0
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @deprecated Moved to the {@link org.geotools.referencing.operation.matrix} package.
 */
public class GeneralMatrix extends org.geotools.referencing.operation.matrix.GeneralMatrix {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 2315556279279988442L;
    
    /**
     * Constructs a square identity matrix of size
     * {@code size}&nbsp;&times;&nbsp;{@code size}.
     */
    public GeneralMatrix(final int size) {
        super(size, size);
    }
    
    /**
     * Constructs a matrix of size
     * {@code numRow}&nbsp;&times;&nbsp;{@code numCol}.
     * Elements on the diagonal <var>j==i</var> are set to 1.
     */
    public GeneralMatrix(final int numRow, final int numCol) {
        super(numRow, numCol);
    }
    
    /**
     * Constructs a {@code numRow}&nbsp;&times;&nbsp;{@code numCol} matrix
     * initialized to the values in the {@code matrix} array. The array values
     * are copied in one row at a time in row major fashion. The array should be
     * exactly <code>numRow*numCol</code> in length. Note that because row and column
     * numbering begins with zero, {@code numRow} and {@code numCol} will be
     * one larger than the maximum possible matrix index values.
     */
    public GeneralMatrix(final int numRow, final int numCol, final double[] matrix) {
        super(numRow, numCol, matrix);
    }
    
    /**
     * Constructs a new matrix from a two-dimensional array of doubles.
     *
     * @param  matrix Array of rows. Each row must have the same length.
     * @throws IllegalArgumentException if the specified matrix is not regular
     *         (i.e. if all rows doesn't have the same length).
     */
    public GeneralMatrix(final double[][] matrix) throws IllegalArgumentException {
        super(matrix.length);
    }
    
    /**
     * Constructs a new matrix and copies the initial values from the parameter matrix.
     */
    public GeneralMatrix(final Matrix matrix) {
        super(matrix);
    }
    
    /**
     * Constructs a new matrix and copies the initial values from the parameter matrix.
     */
    public GeneralMatrix(final GMatrix matrix) {
        super(matrix);
    }
    
    /**
     * Constructs a 3&times;3 matrix from the specified affine transform.
     */
    public GeneralMatrix(final AffineTransform transform) {
        super(transform);
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
        super(srcRegion, dstRegion);
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
     * @throws IllegalArgumentException If {@code dstAxis} contains some axis
     *         not found in {@code srcAxis}, or if some colinear axis were found.
     */
    public GeneralMatrix(final AxisDirection[] srcAxis,
                         final AxisDirection[] dstAxis)
    {
        super(srcAxis, dstAxis);
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
     * @throws IllegalArgumentException If {@code dstAxis} contains some axis
     *         not found in {@code srcAxis}, or if some colinear axis were found.
     */
    public GeneralMatrix(final Envelope srcRegion, final AxisDirection[] srcAxis,
                         final Envelope dstRegion, final AxisDirection[] dstAxis)
    {
        super(srcRegion, srcAxis, dstRegion, dstAxis);
    }
}
