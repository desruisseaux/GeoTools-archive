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

// OpenGIS dependencies
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;

// Geotools dependencies
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * An utility class for the separation of {@linkplain ConcatenatedTransform concatenation} of
 * {@linkplain PassThroughTransform pass through transforms}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class DimensionFilter {
    /**
     * The input dimension to keep.
     * This sequence can contains any integers in the range 0 inclusive to
     * <code>transform.{@linkplain MathTransform#getSourceDimensions getSourceDimensions()}</code>
     * exclusive.
     */
    private final int[] input;

    /**
     * The in which to store output dimensions. This sequence will be filled with output dimensions
     * selected by the methods in this class. The integers will be in the range 0 inclusive to
     * <code>transform.{@linkplain MathTransform#getTargetDimensions getTargetDimensions()}</code>
     * exclusive.
     */
    private int[] output;

    /**
     * 
     */
    public DimensionFilter(final int lower, final int upper) {
        input = new int[upper-lower];
        for (int i=0; i<input.length; i++) {
            input[i] = i+lower;
        }
    }

    /**
     * Reduces the number of input dimensions for the specified transform. The remaining output
     * dimensions will be selected automatically according the specified input dimensions. For
     * example if the supplied {@code transform} has (<var>x</var>, <var>y</var>, <var>z</var>)
     * inputs and (<var>longitude</var>, <var>latitude</var>, <var>height</var>) outputs, then
     * {@code create(transform, new int[]{0,1}), null)} will returns a transform with
     * (<var>x</var>, <var>y</var>) inputs and (probably)
     * (<var>longitude</var>, <var>latitude</var>) outputs. This method can be used in
     * order to separate {@linkplain ConcatenatedTransform concatenation} of
     * {@linkplain PassThroughTransform pass through transforms}.
     *
     * @param  transform The transform to reduces.
     * @return A transform expecting only the specified input dimensions.
     *         The following invariant should hold:
     * <blockquote><pre>
     * subTransform.{@linkplain MathTransform#getSourceDimensions getSourceDimensions()} ==  inputDimensions.length;
     * subTransform.{@linkplain MathTransform#getTargetDimensions getTargetDimensions()} == outputDimensions.length;
     * </pre></blockquote>
     *
     * @throws FactoryException if the transform is not separable.
     */
    public MathTransform preConcatenate(final MathTransform transform) throws FactoryException {
//        final int dimSource = transform.getSourceDimensions();
//        final int dimTarget = transform.getTargetDimensions();
//        final int dimInput  = inputDimensions.getNumElements();
//        final int lower     = JAIUtilities.getMinimum(inputDimensions);
//        final int upper     = JAIUtilities.getMaximum(inputDimensions)+1;
//        if (lower<0 || lower>=upper) {
//            throw new IllegalArgumentException(Resources.format(
//                    ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2,
//                    "minimum(inputDimensions)", new Integer(lower)));
//        }
//        if (upper > dimSource) {
//            throw new IllegalArgumentException(Resources.format(
//                    ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2,
//                    "maximum(inputDimensions)", new Integer(upper-1)));
//        }
//        /*
//         * Check for easiest cases: same transform, identity transform or concatenated transforms.
//         */
//        if (dimInput == dimSource) {
//            assert lower==0 && upper==dimSource;
//            JAIUtilities.fill(outputDimensions, 0, dimTarget);
//            return transform;
//        }
//        if (transform.isIdentity()) {
//            JAIUtilities.add(outputDimensions, inputDimensions, 0);
//            return createIdentityTransform(dimInput);
//        }
//        if (transform instanceof ConcatenatedTransform) {
//            final ConcatenatedTransform ctr = (ConcatenatedTransform) transform;
//            final IntegerSequence trans = new IntegerSequence();
//            final MathTransform step1 = createSubTransform(ctr.transform1, inputDimensions, trans);
//            final MathTransform step2 = createSubTransform(ctr.transform2, trans, outputDimensions);
//            return createConcatenatedTransform(step1, step2);
//        }
//        /*
//         * Special case for the pass through transform:  if at least one input dimension
//         * belong to the passthrough's sub-transform, then delegates part of the work to
//         * <code>subTransform(passThrough.transform, ...)</code>
//         */
//        if (transform instanceof PassThroughTransform) {
//            final PassThroughTransform passThrough = (PassThroughTransform) transform;
//            final int dimPass  = passThrough.transform.getSourceDimensions();
//            final int dimDiff  = passThrough.transform.getTargetDimensions() - dimPass;
//            final int subLower = passThrough.firstAffectedOrdinate;
//            final int subUpper = subLower + dimPass;
//            final IntegerSequence subInputs = new IntegerSequence();
//            for (inputDimensions.startEnumeration(); inputDimensions.hasMoreElements();) {
//                int n = inputDimensions.nextElement();
//                if (n>=subLower && n<subUpper) {
//                    subInputs.insert(n - subLower);
//                } else if (outputDimensions != null) {
//                    if (n >= subUpper) {
//                        n += dimDiff;
//                    }
//                    outputDimensions.insert(n);
//                }
//            }
//            if (subInputs.getNumElements() == 0) {
//                // No input dimension belong to the sub-transform.
//                return createIdentityTransform(dimInput);
//            }
//            final IntegerSequence subOutputs;
//            final MathTransform subTransform;
//            subOutputs = (outputDimensions!=null) ? new IntegerSequence() : null;
//            subTransform = createSubTransform(passThrough.transform, subInputs, subOutputs);
//            JAIUtilities.add(outputDimensions, subOutputs, subLower);
//            if (JAIUtilities.containsAll(inputDimensions, lower, subLower) &&
//                JAIUtilities.containsAll(inputDimensions, subUpper, upper))
//            {
//                final int  firstAffectedOrdinate = Math.max(0, subLower-lower);
//                final int   numTrailingOrdinates = Math.max(0, upper-subUpper);
//                return createPassThroughTransform(firstAffectedOrdinate,
//                                                  subTransform, numTrailingOrdinates);
//            }
//            // TODO: handle more general case here...
//        }
//        /*
//         * If the transformation is specified by a matrix, select all output dimensions which
//         * do not depends on any of the discarted input dimensions.
//         */
//        if (transform instanceof LinearTransform) {
//            int nRows = 0;
//            boolean hasLastRow = false;
//            double[][] rows = ((LinearTransform)transform).getMatrix().getElements();
//            assert rows.length-1 == dimTarget;
//reduce:     for (int j=0; j<rows.length; j++) {
//                final double[] row = rows[j];
//                assert row.length-1 == dimSource : row.length;
//                int nCols = 0;
//                // Stop at row.length-1 because we don't
//                // want to check the translation term.
//                for (int i=0; i<dimSource; i++) {
//                    if (JAIUtilities.contains(inputDimensions, i)) {
//                        row[nCols++] = row[i];
//                    } else if (row[i] != 0) {
//                        // Output dimension 'j' depends on one of discarted input dimension 'i'.
//                        continue reduce;
//                    }
//                }
//                if (j == dimTarget) {
//                    hasLastRow = true;
//                } else if (outputDimensions != null) {
//                    outputDimensions.insert(j);
//                }
//                assert nCols == dimInput : nCols;
//                row [nCols++] = row[dimSource]; // Copy the translation term.
//                rows[nRows++] = XArray.resize(row, nCols);
//            }
//            rows = (double[][]) XArray.resize(rows, nRows);
//            if (hasLastRow) {
//                return createAffineTransform(new Matrix(rows));
//            }
//            // In an affine transform,  the last row is not supposed to have dependency
//            // to any input dimension. But in this particuler case, our matrix has such
//            // dependencies. REVISIT: is there anything we could do about that?
//        }
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
     * @param  outputDimensions The output dimension to keep.
     *         This sequence can contains any integers in the range 0 inclusive to
     *         <code>transform.{@linkplain MathTransform#getTargetDimensions getTargetDimensions()}</code>
     *         exclusive.
     * @return The <code>transform</code> keeping only the specified output dimensions.
     */
//    public MathTransform createFilterTransform(MathTransform transform,
//                                               final IntegerSequence outputDimensions)
//    {
//        final int dimSource = transform.getSourceDimensions();
//        final int dimTarget = transform.getTargetDimensions();
//        final int dimOutput = outputDimensions.getNumElements();
//        final int lower     = JAIUtilities.getMinimum(outputDimensions);
//        final int upper     = JAIUtilities.getMaximum(outputDimensions)+1;
//        if (lower<0 || lower>=upper) {
//            throw new IllegalArgumentException(Resources.format(
//                    ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2,
//                    "minimum(outputDimensions)", new Integer(lower)));
//        }
//        if (upper > dimTarget) {
//            throw new IllegalArgumentException(Resources.format(
//                    ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2,
//                    "maximum(outputDimensions)", new Integer(upper)));
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
//            if (!JAIUtilities.containsAny(outputDimensions, subLower, subUpper)) {
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
//        for (outputDimensions.startEnumeration(); outputDimensions.hasMoreElements(); j++) {
//            int i = outputDimensions.nextElement();
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
}
