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
import java.util.Arrays;

// OpenGIS dependencies
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransformFactory;

// Geotools dependencies
import org.geotools.referencing.operation.GeneralMatrix;
import org.geotools.referencing.operation.LinearTransform;
import org.geotools.resources.XArray;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * An utility class for the separation of {@linkplain ConcatenatedTransform concatenation} of
 * {@linkplain PassThroughTransform pass through transforms}. Given an arbitrary
 * {@linkplain MathTransform math transform}, this utility class will returns a new math transform
 * that operates only of a given set of source dimensions. For example if the supplied
 * {@code transform} has (<var>x</var>, <var>y</var>, <var>z</var>) inputs and
 * (<var>longitude</var>, <var>latitude</var>, <var>height</var>) outputs, then
 * the following code:
 *
 * <blockquote><pre>
 * {@linkplain #setSourceDimensionRange setSourceDimensionRange}(0, 2);
 * MathTransform mt = {@linkplain #separate separate}(transform);
 * </pre></blockquote>
 *
 * <P>will returns a transform with (<var>x</var>, <var>y</var>) inputs and (probably)
 * (<var>longitude</var>, <var>latitude</var>) outputs. The later can be verified with
 * a call to {@link #getTargetDimensions}.</P>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class DimensionFilter {
    /**
     * The input dimensions to keep.
     * This sequence can contains any integers in the range 0 inclusive to
     * <code>transform.{@linkplain MathTransform#getSourceDimensions getSourceDimensions()}</code>
     * exclusive.
     */
    private int[] sourceDimensions;

    /**
     * The output dimensions to keep.
     * This sequence can contains any integers in the range 0 inclusive to
     * <code>transform.{@linkplain MathTransform#getTargetDimensions getTargetDimensions()}</code>
     * exclusive.
     */
    private int[] targetDimensions;

    /**
     * The factory for the creation of new math transforms.
     */
    private final MathTransformFactory factory;

    /**
     * Constructs a dimension filter.
     *
     * @param factory The factory for the creation of new math transforms.
     */
    public DimensionFilter(final MathTransformFactory factory) {
        this.factory = factory;
    }

    /**
     * Add an input dimension to keep. The {@code dimension} value apply to the
     * source dimensions of the transform to be given to
     * <code>{@linkplain #separate separate}(transform)</code>.
     * The number must be in the range 0 inclusive to
     * <code>transform.{@linkplain MathTransform#getSourceDimensions getSourceDimensions()}</code>
     * exclusive.
     */
    public void addSourceDimension(final int dimension) {
        sourceDimensions = add(sourceDimensions, dimension);
    }

    /**
     * Set the input dimensions to keep. The {@code index} values apply to the
     * source dimensions of the transform to be given to
     * <code>{@linkplain #separate separate}(transform)</code>.
     * All numbers must be in the range 0 inclusive to
     * <code>transform.{@linkplain MathTransform#getSourceDimensions getSourceDimensions()}</code>
     * exclusive. The {@code index} values must be in strictly increasing order.
     */
    public void setSourceDimensions(int[] index) {
        index = (int[]) index.clone();
        ensureValidSeries(index, "sourceDimensions");
        sourceDimensions = index;
    }

    /**
     * Set the range of input dimensions to keep. The {@code lower} and {@code upper} values
     * apply to the source dimensions of the transform to be given to
     * <code>{@linkplain #separate separate}(transform)</code>.
     *
     * @param lower The lower dimension, inclusive. Must not be smaller than 0.
     * @param upper The upper dimension, exclusive. Must not be greater than
     * <code>transform.{@linkplain MathTransform#getSourceDimensions getSourceDimensions()}</code>.
     */
    public void setSourceDimensionRange(final int lower, final int upper) {
        sourceDimensions = series(lower, upper);
    }

    /**
     * Returns the output dimensions.
     *
     * @throws IllegalStateException if this information is not available.
     */
    public int[] getTargetDimensions() throws IllegalStateException {
        if (targetDimensions != null) {
            return (int[]) targetDimensions.clone();
        }
        throw new IllegalStateException();
    }

    /**
     * Separates the math transform on the basis of {@linkplain #sourceDimensions input dimensions}.
     * The remaining {@linkplain #targetDimensions output dimensions} will be selected automatically
     * according the specified input dimensions.
     *
     * @param  transform The transform to reduces.
     * @return A transform expecting only the specified input dimensions.
     * @throws FactoryException if the transform is not separable.
     */
    private MathTransform separateInput(final MathTransform transform) throws FactoryException {
        targetDimensions    = null;
        final int dimSource = transform.getSourceDimensions();
        final int dimTarget = transform.getTargetDimensions();
        final int dimInput  = sourceDimensions.length;
        final int lower     = sourceDimensions[0];
        final int upper     = sourceDimensions[dimInput-1] + 1;
        if (upper > dimSource) {
            throw new IllegalArgumentException(Resources.format(
                    ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2,
                    "sourceDimensions", new Integer(upper-1)));
        }
        /*
         * Check for easiest cases: same transform, identity transform or concatenated transforms.
         */
        if (dimInput == dimSource) {
            assert lower==0 && upper==dimSource;
            targetDimensions = series(0, dimTarget);
            return transform;
        }
        if (transform.isIdentity()) {
            targetDimensions = sourceDimensions;
            return factory.createAffineTransform(new GeneralMatrix(dimInput+1));
        }
        if (transform instanceof ConcatenatedTransform) {
            final ConcatenatedTransform ctr = (ConcatenatedTransform) transform;
            final int[] original = sourceDimensions;
            final MathTransform step1, step2;
            step1 = separateInput(ctr.transform1); sourceDimensions = targetDimensions;
            step2 = separateInput(ctr.transform2); sourceDimensions = original;
            return factory.createConcatenatedTransform(step1, step2);
        }
        /*
         * Special case for the pass through transform:  if at least one input dimension
         * belong to the passthrough's sub-transform, then delegates part of the work to
         * <code>subTransform(passThrough.transform, ...)</code>
         */
        if (transform instanceof PassThroughTransform) {
            final PassThroughTransform passThrough = (PassThroughTransform) transform;
            final int dimPass  = passThrough.subTransform.getSourceDimensions();
            final int dimDiff  = passThrough.subTransform.getTargetDimensions() - dimPass;
            final int subLower = passThrough.firstAffectedOrdinate;
            final int subUpper = subLower + dimPass;
            final DimensionFilter subFilter = new DimensionFilter(factory);
            for (int i=0; i<sourceDimensions.length; i++) {
                int n = sourceDimensions[i];
                if (n>=subLower && n<subUpper) {
                    // Dimension n belong to the subtransform.
                    subFilter.addSourceDimension(n - subLower);
                } else {
                    // Dimension n belong to heading or trailing dimensions.
                    // Passthrough, after adjustement for trailing dimensions.
                    if (n >= subUpper) {
                        n += dimDiff;
                    }
                    targetDimensions = add(targetDimensions, n);
                }
            }
            if (subFilter.sourceDimensions == null) {
                /*
                 * No source dimensions belong to the sub-transform. The only remaining
                 * sources are heading and trailing dimensions. A passthrough transform
                 * without its sub-transform is an identity transform...
                 */ 
                return factory.createAffineTransform(new GeneralMatrix(dimInput+1));
            }
            /*
             * There is at least one dimension to separate in the sub-transform. Performs this
             * separation and gets the list of output dimensions. We need to offset the output
             * dimensions by the amount of leading dimensions once the separation is done, in
             * order to translate from the sub-transform's dimension numbering to the transform's
             * numbering.
             */
            final MathTransform subTransform = subFilter.separateInput(passThrough.subTransform);
            targetDimensions = subFilter.targetDimensions;
            for (int i=0; i<targetDimensions.length; i++) {
                targetDimensions[i] += subLower;
            }
            /*
             * If all source dimensions not in the sub-transform are consecutive numbers, we can
             * use our pass though transform implementation. The "consecutive numbers" requirement
             * (expressed in the 'if' statement below) is a consequence of a limitation in our
             * current implementation: our pass through transform doesn't accept arbitrary index
             * for modified ordinates.
             */
            if (containsAll(sourceDimensions, lower, subLower) &&
                containsAll(sourceDimensions, subUpper, upper))
            {
                final int  firstAffectedOrdinate = Math.max(0, subLower-lower);
                final int   numTrailingOrdinates = Math.max(0, upper-subUpper);
                return factory.createPassThroughTransform(
                        firstAffectedOrdinate, subTransform, numTrailingOrdinates);
            }
            // TODO: handle more general case here...
        }
        /*
         * If the transform is affine (or at least projective), express the transform as a matrix.
         * Then, select output dimensions that depends only on selected input dimensions. If an
         * output dimension depends on at least one discarted input dimension, then this output
         * dimension will be discarted as well.
         */
        if (transform instanceof LinearTransform) {
            int           nRows = 0;
            boolean  hasLastRow = false;
            final Matrix matrix = ((LinearTransform) transform).getMatrix();
            assert dimSource+1 == matrix.getNumCol() &&
                   dimTarget+1 == matrix.getNumRow() : matrix;
            double[][] rows = new double[dimTarget+1][];
reduce:     for (int j=0; j<rows.length; j++) {
                final double[] row = new double[dimInput+1];
                /*
                 * For each output dimension (i.e. a matrix row), find the matrix elements for
                 * each input dimension to be kept. If a dependance to at least one discarted
                 * input dimension is found, then the whole output dimension is discarted.
                 *
                 * NOTE: The following loop stops at matrix.getNumCol()-1 because we don't
                 *       want to check the translation term.
                 */
                int nCols=0, scan=0;
                for (int i=0; i<dimSource; i++) {
                    final double element = matrix.getElement(j,i);
                    if (scan<sourceDimensions.length && sourceDimensions[scan]==i) {
                        row[nCols++] = element;
                        scan++;
                    } else if (element != 0) {
                        // Output dimension 'j' depends on one of discarted input dimension 'i'.
                        // The whole row will be discarted.
                        continue reduce;
                    }
                }
                row[nCols++] = row[dimSource]; // Copy the translation term.
                assert nCols == row.length : nCols;
                if (j == dimTarget) {
                    hasLastRow = true;
                } else {
                    targetDimensions = add(targetDimensions, j);
                }
                rows[j] = row;
            }
            rows = (double[][]) XArray.resize(rows, nRows);
            if (hasLastRow) {
                return factory.createAffineTransform(new GeneralMatrix(rows));
            }
            // In an affine transform,  the last row is not supposed to have dependency
            // to any input dimension. But in this particuler case, our matrix has such
            // dependencies. TODO: is there anything we could do about that?
        }
        throw new FactoryException(Resources.format(ResourceKeys.ERROR_INSEPARABLE_TRANSFORM));
    }

    /**
     * Creates a transform which retains only a subset of an other transform's outputs. The number
     * and nature of inputs stay unchanged. For example if the supplied <code>transform</code> has
     * (<var>longitude</var>, <var>latitude</var>, <var>height</var>) outputs, then a sub-transform
     * may be used to keep only the (<var>longitude</var>, <var>latitude</var>) part. In most cases,
     * the created sub-transform is non-invertible since it loose informations.
     * <br><br>
     * This transform may be see as a non-square matrix transform with less rows
     * than columns, concatenated with <code>transform</code>. However, invoking
     * <code>createFilterTransfom(...)</code> allows the optimization of some common cases.
     *
     * @param  transform The transform to reduces.
     * @param  targetDimensions The output dimension to keep.
     *         This sequence can contains any integers in the range 0 inclusive to
     *         <code>transform.{@linkplain MathTransform#getTargetDimensions getTargetDimensions()}</code>
     *         exclusive.
     * @return The <code>transform</code> keeping only the specified output dimensions.
     */
//    public MathTransform createFilterTransform(MathTransform transform,
//                                               final IntegerSequence targetDimensions)
//    {
//        final int dimSource = transform.getSourceDimensions();
//        final int dimTarget = transform.getTargetDimensions();
//        final int dimOutput = targetDimensions.getNumElements();
//        final int lower     = JAIUtilities.getMinimum(targetDimensions);
//        final int upper     = JAIUtilities.getMaximum(targetDimensions)+1;
//        if (lower<0 || lower>=upper) {
//            throw new IllegalArgumentException(Resources.format(
//                    ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2,
//                    "minimum(targetDimensions)", new Integer(lower)));
//        }
//        if (upper > dimTarget) {
//            throw new IllegalArgumentException(Resources.format(
//                    ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2,
//                    "maximum(targetDimensions)", new Integer(upper)));
//        }
//        if (dimOutput == dimTarget) {
//            assert lower==0 && upper==dimTarget;
//            return transform;
//        }
//        /*
//         * If the transform is an instance of "pass through" transform but no dimension from its
//         * subtransform is requested, then ignore the subtransform (i.e. treat the whole transform
//         * as identity, except for the number of output dimension which may be different from the
//         * number of input dimension).
//         */
//        int dimPass = 0;
//        int dimDiff = 0;
//        int dimStep = dimTarget;
//        if (transform instanceof PassThroughTransform) {
//            final PassThroughTransform passThrough = (PassThroughTransform) transform;
//            final int subLower = passThrough.firstAffectedOrdinate;
//            final int subUpper = subLower + passThrough.transform.getTargetDimensions();
//            if (!JAIUtilities.containsAny(targetDimensions, subLower, subUpper)) {
//                transform = null;
//                dimStep = dimSource;
//                dimPass = subLower;
//                dimDiff = (subLower + passThrough.transform.getSourceDimensions()) - subUpper;
//            }
//        }
//        /*
//         * Create the matrix to be used as a filter,        [x']     [1  0  0  0] [x]
//         * and concatenate it to the transform. The         [z']  =  [0  0  1  0] [y]
//         * matrix will contains only a 1 for the output     [1 ]     [0  0  0  1] [z]
//         * dimension to keep, as in the following example:                        [1]
//         */
//        final Matrix matrix = new Matrix(dimOutput+1, dimStep+1);
//        matrix.setZero();
//        int j=0;
//        for (targetDimensions.startEnumeration(); targetDimensions.hasMoreElements(); j++) {
//            int i = targetDimensions.nextElement();
//            if (i >= dimPass) {
//                i += dimDiff;
//            }
//            matrix.setElement(j, i, 1);
//        }
//        // Affine transform has one more row/column than dimension.
//        matrix.setElement(dimOutput, dimStep, 1);
//        MathTransform filtered = createAffineTransform(matrix);
//        if (transform != null) {
//            filtered = createConcatenatedTransform(transform, filtered);
//        }
//        return filtered;
//    }

    /**
     * Returns {@code true} if {@code array} contains all index in the range {@code lower}
     * inclusive to {@code upper} exclusive.
     */
    private static boolean containsAll(final int[] array, final int lower, final int upper) {
        if (array != null) {
            assert XArray.isSorted(array);
            int index = Arrays.binarySearch(array, lower);
            if (index >= 0) {
                index += upper-lower;
                if (index < array.length) {
                    return array[index] == upper;
                }
            }
        }
        return lower == upper;
    }

    /**
     * Add the specified {@code value} to the specified {@code array}. Values are added
     * in increasing order. Duplicated value are not added.
     */
    private static int[] add(int[] array, int value) {
        if (array == null) {
            return new int[] {value};
        }
        assert XArray.isSorted(array);
        int i = Arrays.binarySearch(array, value);
        if (i < 0) {
            i = ~i;   // Tild, not the minus sign.
            array = XArray.insert(array, i, 1);
            array[i] = value;
        }
        return array;
    }

    /**
     * Returns a series of increasing values starting at {@code lower}.
     */
    private static int[] series(final int lower, final int upper) {
        final int[] array = new int[upper-lower];
        for (int i=0; i<array.length; i++) {
            array[i] = i+lower;
        }
        return array;
    }

    /**
     * Ensure that the specified array contains strictly increasing non-negative values.
     *
     * @param  array The array to check.
     * @param  name  The argument name. Used for formatting exception message if needed.
     * @throws IllegalArgumentException if the specified array is not a valid series.
     */
    private static void ensureValidSeries(int[] array, final String name)
            throws IllegalArgumentException
    {
        int last = -1;
        for (int i=0; i<array.length; i++) {
            final int value = array[i];
            if (value <= last) {
                throw new IllegalArgumentException(Resources.format(
                      ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2, name+'['+i+']', new Integer(value)));
            }
            last = value;
        }
    }
}
