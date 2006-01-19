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
package org.geotools.pt;

// J2SE dependencies
import java.awt.geom.AffineTransform;

import javax.vecmath.GMatrix;

import org.geotools.cs.AxisOrientation;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;


/**
 * A two dimensional array of numbers. Row and column numbering begins with zero.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @deprecated Replaced by {@link org.geotools.referencing.operation.GeneralMatrix}
 *             in the <code>org.geotools.referencing.operation</code> package.
 */
public class Matrix extends org.geotools.referencing.operation.GeneralMatrix {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 3126899762163038129L;
    
    /**
     * Construct a square identity matrix of size
     * <code>size</code>&nbsp;&times;&nbsp;<code>size</code>.
     */
    public Matrix(final int size) {
        super(size);
    }
    
    /**
     * Construct a matrix of size
     * <code>numRow</code>&nbsp;&times;&nbsp;<code>numCol</code>.
     * Elements on the diagonal <var>j==i</var> are set to 1.
     */
    public Matrix(final int numRow, final int numCol) {
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
    public Matrix(final int numRow, final int numCol, final double[] matrix) {
        super(numRow, numCol, matrix);
    }
    
    /**
     * Constructs a new matrix from a two-dimensional array of doubles.
     *
     * @param  matrix Array of rows. Each row must have the same length.
     * @throws IllegalArgumentException if the specified matrix is not regular
     *         (i.e. if all rows doesn't have the same length).
     */
    public Matrix(final double[][] matrix) throws IllegalArgumentException {
        super(matrix);
    }
    
    /**
     * Constructs a new matrix and copies the initial
     * values from the parameter matrix.
     */
    public Matrix(final GMatrix matrix) {
        super(matrix);
    }
    
    /**
     * Construct a 3&times;3 matrix from the specified affine transform.
     */
    public Matrix(final AffineTransform transform) {
        super(transform);
    }
    
    /**
     * Construct an affine transform mapping a source region to a destination
     * region. The regions must have the same number of dimensions, but their
     * axis order and axis orientation may be different.
     *
     * @param srcRegion The source region.
     * @param srcAxis   Axis orientation for each dimension of the source region.
     * @param dstRegion The destination region.
     * @param dstAxis   Axis orientation for each dimension of the destination region.
     * @param validRegions   <code>true</code> if source and destination regions must
     *        be taken in account. If <code>false</code>, then source and destination
     *        regions will be ignored and may be null.
     */
    private Matrix(final Envelope srcRegion, final AxisOrientation[] srcAxis,
                   final Envelope dstRegion, final AxisOrientation[] dstAxis,
                   final boolean validRegions)
    {
        this(srcAxis.length+1);
        /*
         * Arguments check. NOTE: those exceptions are catched
         * by 'org.geotools.ct.CoordinateTransformationFactory'.
         * If exception type change, update the factory class.
         */
        final int dimension = srcAxis.length;
        if (dstAxis.length != dimension) {
            throw new MismatchedDimensionException(Errors.format(
                        ErrorKeys.MISMATCHED_DIMENSION_$2,
                        new Integer(dimension), new Integer(dstAxis.length)));
        }
        if (validRegions) {
            srcRegion.ensureDimensionMatch(dimension);
            dstRegion.ensureDimensionMatch(dimension);
        }
        /*
         * Map source axis to destination axis.  If no axis is moved (for example if the user
         * want to transform (NORTH,EAST) to (SOUTH,EAST)), then source and destination index
         * will be equal.   If some axis are moved (for example if the user want to transform
         * (NORTH,EAST) to (EAST,NORTH)),  then ordinates at index <code>srcIndex</code> will
         * have to be moved at index <code>dstIndex</code>.
         */
        setZero();
        for (int srcIndex=0; srcIndex<dimension; srcIndex++) {
            boolean hasFound = false;
            final AxisOrientation srcAxe = srcAxis[srcIndex];
            final AxisOrientation search = srcAxe.absolute();
            for (int dstIndex=0; dstIndex<dimension; dstIndex++) {
                final AxisOrientation dstAxe = dstAxis[dstIndex];
                if (search.equals(dstAxe.absolute())) {
                    if (hasFound) {
                        throw new IllegalArgumentException(Errors.format(ErrorKeys.COLINEAR_AXIS_$2,
                        srcAxe.getName(null), dstAxe.getName(null)));
                    }
                    hasFound = true;
                    /*
                     * Set the matrix elements. Some matrix elements will never
                     * be set. They will be left to zero, which is their wanted
                     * value.
                     */
                    final boolean normal = srcAxe.equals(dstAxe);
                    double scale     = (normal) ? +1 : -1;
                    double translate = 0;
                    if (validRegions) {
                        translate  = (normal) ? dstRegion.getMinimum(dstIndex) : dstRegion.getMaximum(dstIndex);
                        scale     *= dstRegion.getLength(dstIndex) / srcRegion.getLength(srcIndex);
                        translate -= srcRegion.getMinimum(srcIndex)*scale;
                    }
                    setElement(dstIndex, srcIndex,  scale);
                    setElement(dstIndex, dimension, translate);
                }
            }
            if (!hasFound) {
                throw new IllegalArgumentException();
            }
        }
        setElement(dimension, dimension, 1);
        assert isAffine();
    }
    
    /**
     * Construct an affine transform changing axis order and/or orientation.
     * For example, the affine transform may convert (NORTH,WEST) coordinates
     * into (EAST,NORTH). Axis orientation can be inversed only. For example,
     * it is illegal to transform (NORTH,WEST) coordinates into (NORTH,DOWN).
     *
     * @param  srcAxis The set of axis orientation for source coordinate system.
     * @param  dstAxis The set of axis orientation for destination coordinate system.
     * @throws MismatchedDimensionException if <code>srcAxis</code>
     *         and <code>dstAxis</code> don't have the same length.
     * @throws IllegalArgumentException if the affine transform can't
     *         be created for some other raison.
     */
    public static Matrix createAffineTransform(final AxisOrientation[] srcAxis,
                                               final AxisOrientation[] dstAxis)
    {
        return new Matrix(null, srcAxis, null, dstAxis, false);
    }
    
    /**
     * Construct an affine transform that maps
     * a source region to a destination region.
     * Axis order and orientation are left unchanged.
     *
     * @param  srcRegion The source region.
     * @param  dstRegion The destination region.
     * @throws MismatchedDimensionException if regions don't have the same dimension.
     */
    public static Matrix createAffineTransform(final Envelope srcRegion,
                                               final Envelope dstRegion)
    {
        final int dimension = srcRegion.getDimension();
        dstRegion.ensureDimensionMatch(dimension);
        final Matrix matrix = new Matrix(dimension+1);
        for (int i=0; i<dimension; i++) {
            final double scale     = dstRegion.getLength(i) / srcRegion.getLength(i);
            final double translate = dstRegion.getMinimum(i) - srcRegion.getMinimum(i)*scale;
            matrix.setElement(i, i,         scale);
            matrix.setElement(i, dimension, translate);
        }
        matrix.setElement(dimension, dimension, 1);
        assert matrix.isAffine();
        return matrix;
    }
    
    /**
     * Construct an affine transform mapping a source region to a destination
     * region. Axis order and/or orientation can be changed during the process.
     * For example, the affine transform may convert (NORTH,WEST) coordinates
     * into (EAST,NORTH). Axis orientation can be inversed only. For example,
     * it is illegal to transform (NORTH,WEST) coordinates into (NORTH,DOWN).
     *
     * @param  srcRegion The source region.
     * @param  srcAxis   Axis orientation for each dimension of the source region.
     * @param  dstRegion The destination region.
     * @param  dstAxis   Axis orientation for each dimension of the destination region.
     * @throws MismatchedDimensionException if all arguments don't have the same dimension.
     * @throws IllegalArgumentException if the affine transform can't be created
     *         for some other raison.
     */
    public static Matrix createAffineTransform(Envelope srcRegion, AxisOrientation[] srcAxis,
                                               Envelope dstRegion, AxisOrientation[] dstAxis)
    {
        return new Matrix(srcRegion, srcAxis, dstRegion, dstAxis, true);
    }
}
