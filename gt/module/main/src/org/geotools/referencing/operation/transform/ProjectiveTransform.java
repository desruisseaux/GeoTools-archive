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
 *
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.referencing.operation.transform;

// J2SE dependencies and extensions
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.vecmath.SingularMatrixException;

// OpenGIS dependencies
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.Conversion;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.referencing.operation.OperationMethod;
import org.opengis.spatialschema.geometry.DirectPosition;

// Geotools dependencies
import org.geotools.metadata.citation.Citation;
import org.geotools.parameter.MatrixParameterDescriptors;
import org.geotools.parameter.MatrixParameterValues;
import org.geotools.referencing.Identifier;
import org.geotools.referencing.operation.GeneralMatrix;
import org.geotools.referencing.operation.LinearTransform;
import org.geotools.referencing.operation.MathTransformProvider;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.resources.cts.Resources;


/**
 * A usually affine, or otherwise a projective transform. A projective transform is capable of
 * mapping an arbitrary quadrilateral into another arbitrary quadrilateral, while preserving the
 * straightness of lines. In the special case where the transform is affine, the parallelism of
 * lines in the source is preserved in the output.
 * <br><br>
 * Such a coordinate transformation can be represented by a square {@linkplain GeneralMatrix matrix}
 * of an arbitrary size. Point coordinates must have a dimension equals to
 * <code>{@linkplain Matrix#getNumCol}-1</code>. For example, for square matrix of size 4&times;4,
 * coordinate points are three-dimensional. The transformed points <code>(x',y',z')</code> are
 * computed as below (note that this computation is similar to
 * {@link javax.media.jai.PerspectiveTransform} in <cite>Java Advanced Imaging</cite>):
 *
 * <blockquote><pre>
 * [ u ]     [ m<sub>00</sub>  m<sub>01</sub>  m<sub>02</sub>  m<sub>03</sub> ] [ x ]
 * [ v ]  =  [ m<sub>10</sub>  m<sub>11</sub>  m<sub>12</sub>  m<sub>13</sub> ] [ y ]
 * [ w ]     [ m<sub>20</sub>  m<sub>21</sub>  m<sub>22</sub>  m<sub>23</sub> ] [ z ]
 * [ t ]     [ m<sub>30</sub>  m<sub>31</sub>  m<sub>32</sub>  m<sub>33</sub> ] [ 1 ]
 *
 *   x' = u/t
 *   y' = v/t
 *   y' = w/t
 * </pre></blockquote>
 *
 * In the special case of an affine transform, the last row contains only zero
 * values except in the last column, which contains 1.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see javax.media.jai.PerspectiveTransform
 * @see java.awt.geom.AffineTransform
 * @see <A HREF="http://mathworld.wolfram.com/AffineTransformation.html">Affine transformation on MathWorld</A>
 */
public class ProjectiveTransform extends AbstractMathTransform implements LinearTransform, Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -2104496465933824935L;
    
    /**
     * The number of rows.
     */
    private final int numRow;
    
    /**
     * The number of columns.
     */
    private final int numCol;
    
    /**
     * Elements of the matrix. Column indice vary fastest.
     */
    private final double[] elt;
    
    /**
     * Construct a transform from the specified matrix.
     * The matrix should be affine, but it will not be verified.
     *
     * @param matrix The matrix.
     */
    protected ProjectiveTransform(final Matrix matrix) {        
        numRow = matrix.getNumRow();
        numCol = matrix.getNumCol();
        elt = new double[numRow*numCol];
        int index = 0;
        for (int j=0; j<numRow; j++) {
            for (int i=0; i<numCol; i++) {
                elt[index++] = matrix.getElement(j,i);
            }
        }
    }

    /**
     * Creates a transform for the specified matrix.
     * The matrix should be affine, but it will not be verified.
     */
    public static LinearTransform create(final Matrix matrix) {
        final int dimension = matrix.getNumRow()-1;
        if (dimension == matrix.getNumCol()-1) {
            if (matrix.isIdentity()) {
                return IdentityTransform.create(dimension);
            }
            final GeneralMatrix m = wrap(matrix);
            if (m.isAffine()) {
                switch (dimension) {
                    case 1: return LinearTransform1D.create(m.getElement(0,0), m.getElement(0,1));
                    case 2: return create(m.toAffineTransform2D());
                }
            }
        }
        return new ProjectiveTransform(matrix);
    }

    /**
     * Creates a transform for the specified matrix as a Java2D object.
     * This method is provided for interoperability with
     * <A HREF="http://java.sun.com/products/java-media/2D/index.jsp">Java2D</A>.
     */
    public static LinearTransform create(final AffineTransform matrix) {
        if (matrix.isIdentity()) {
            return IdentityTransform.create(2);
        }
        return new AffineTransform2D(matrix);
    }

    /**
     * Creates a matrix that keep only a subset of the ordinate values.
     * The dimension of source coordinates is <code>sourceDim</code> and
     * the dimension of target coordinates is <code>toKeep.length</code>.
     *
     * @param  sourceDim the dimension of source coordinates.
     * @param  toKeep the indices of ordinate values to keep.
     * @return The matrix to give to the {@link #create(Matrix)}
     *         method in order to create the transform.
     * @throws IndexOutOfBoundsException if a value of <code>toKeep</code>
     *         is lower than 0 or not smaller than <code>sourceDim</code>.
     */
    public static Matrix createSelectMatrix(final int sourceDim, final int[] toKeep)
            throws IndexOutOfBoundsException
    {
        final int targetDim = toKeep.length;
        final GeneralMatrix matrix = new GeneralMatrix(targetDim+1, sourceDim+1);
        matrix.setZero();
        for (int j=0; j<targetDim; j++) {
            matrix.setElement(j, toKeep[j], 1);
        }
        matrix.setElement(targetDim, sourceDim, 1);
        return matrix;
    }

    /**
     * Returns the parameter descriptors for this math transform.
     */
    public ParameterDescriptorGroup getParameterDescriptors() {
        return Provider.PARAMETERS;
    }

    /**
     * Returns the matrix elements as a group of parameters values. The number of parameters
     * depends on the matrix size. Only matrix elements different from their default value
     * will be included in this group.
     *
     * @param  matrix The matrix to returns as a group of parameters.
     * @return A copy of the parameter values for this math transform.
     */
    static ParameterValueGroup getParameterValues(final Matrix matrix) {
        final MatrixParameterValues values;
        values = (MatrixParameterValues) Provider.PARAMETERS.createValue();        
        values.setMatrix(matrix);
        return values;
    }

    /**
     * Returns the matrix elements as a group of parameters values. The number of parameters
     * depends on the matrix size. Only matrix elements different from their default value
     * will be included in this group.
     *
     * @return A copy of the parameter values for this math transform.
     */
    public ParameterValueGroup getParameterValues() {
        return getParameterValues(getMatrix());
    }
    
    /**
     * Transforms an array of floating point coordinates by this matrix. Point coordinates
     * must have a dimension equals to <code>{@link Matrix#getNumCol}-1</code>. For example,
     * for square matrix of size 4&times;4, coordinate points are three-dimensional and
     * stored in the arrays starting at the specified offset (<code>srcOff</code>) in the order
     * <code>[x<sub>0</sub>, y<sub>0</sub>, z<sub>0</sub>,
     *        x<sub>1</sub>, y<sub>1</sub>, z<sub>1</sub>...,
     *        x<sub>n</sub>, y<sub>n</sub>, z<sub>n</sub>]</code>.
     *
     * @param srcPts The array containing the source point coordinates.
     * @param srcOff The offset to the first point to be transformed in the source array.
     * @param dstPts The array into which the transformed point coordinates are returned.
     * @param dstOff The offset to the location of the first transformed point that is stored
     *               in the destination array. The source and destination array sections can
     *               be overlaps.
     * @param numPts The number of points to be transformed
     */
    public void transform(float[] srcPts, int srcOff,
                          final float[] dstPts, int dstOff, int numPts)
    {
        final int  inputDimension = numCol-1; // The last ordinate will be assumed equals to 1.
        final int outputDimension = numRow-1;
        final double[]     buffer = new double[numRow];
        if (srcPts==dstPts) {
            // We are going to write in the source array. Checks if
            // source and destination sections are going to clash.
            final int upperSrc = srcOff + numPts*inputDimension;
            if (upperSrc > dstOff) {
                if (inputDimension >= outputDimension ? dstOff > srcOff :
                            dstOff + numPts*outputDimension > upperSrc) {
                    // If source overlaps destination, then the easiest workaround is
                    // to copy source data. This is not the most efficient however...
                    srcPts = new float[numPts*inputDimension];
                    System.arraycopy(dstPts, srcOff, srcPts, 0, srcPts.length);
                    srcOff = 0;
                }
            }
        }
        while (--numPts>=0) {
            int mix=0;
            for (int j=0; j<numRow; j++) {
                double sum=elt[mix + inputDimension];
                for (int i=0; i<inputDimension; i++) {
                    sum += srcPts[srcOff+i]*elt[mix++];
                }
                buffer[j] = sum;
                mix++;
            }
            final double w = buffer[outputDimension];
            for (int j=0; j<outputDimension; j++) {
                // 'w' is equals to 1 if the transform is affine.
                dstPts[dstOff++] = (float) (buffer[j]/w);
            }
            srcOff += inputDimension;
        }
    }
    
    /**
     * Transforms an array of floating point coordinates by this matrix. Point coordinates
     * must have a dimension equals to <code>{@link Matrix#getNumCol}-1</code>. For example,
     * for square matrix of size 4&times;4, coordinate points are three-dimensional and
     * stored in the arrays starting at the specified offset (<code>srcOff</code>) in the order
     * <code>[x<sub>0</sub>, y<sub>0</sub>, z<sub>0</sub>,
     *        x<sub>1</sub>, y<sub>1</sub>, z<sub>1</sub>...,
     *        x<sub>n</sub>, y<sub>n</sub>, z<sub>n</sub>]</code>.
     *
     * @param srcPts The array containing the source point coordinates.
     * @param srcOff The offset to the first point to be transformed in the source array.
     * @param dstPts The array into which the transformed point coordinates are returned.
     * @param dstOff The offset to the location of the first transformed point that is stored
     *               in the destination array. The source and destination array sections can
     *               be overlaps.
     * @param numPts The number of points to be transformed
     */
    public void transform(double[] srcPts, int srcOff,
                          final double[] dstPts, int dstOff, int numPts)
    {
        final int  inputDimension = numCol-1; // The last ordinate will be assumed equals to 1.
        final int outputDimension = numRow-1;
        final double[]     buffer = new double[numRow];
        if (srcPts==dstPts) {
            // We are going to write in the source array. Checks if
            // source and destination sections are going to clash.
            final int upperSrc = srcOff + numPts*inputDimension;
            if (upperSrc > dstOff) {
                if (inputDimension >= outputDimension ? dstOff > srcOff :
                            dstOff + numPts*outputDimension > upperSrc) {
                    // If source overlaps destination, then the easiest workaround is
                    // to copy source data. This is not the most efficient however...
                    srcPts = new double[numPts*inputDimension];
                    System.arraycopy(dstPts, srcOff, srcPts, 0, srcPts.length);
                    srcOff = 0;
                }
            }
        }
        while (--numPts>=0) {
            int mix=0;
            for (int j=0; j<numRow; j++) {
                double sum=elt[mix + inputDimension];
                for (int i=0; i<inputDimension; i++) {
                    sum += srcPts[srcOff+i]*elt[mix++];
                }
                buffer[j] = sum;
                mix++;
            }
            final double w = buffer[outputDimension];
            for (int j=0; j<outputDimension; j++) {
                // 'w' is equals to 1 if the transform is affine.
                dstPts[dstOff++] = buffer[j]/w;
            }
            srcOff += inputDimension;
        }
    }
    
    /**
     * Gets the derivative of this transform at a point.
     * For a matrix transform, the derivative is the
     * same everywhere.
     */
    public Matrix derivative(final Point2D point) {
        return derivative((DirectPosition)null);
    }
    
    /**
     * Gets the derivative of this transform at a point.
     * For a matrix transform, the derivative is the
     * same everywhere.
     */
    public Matrix derivative(final DirectPosition point) {
        final GeneralMatrix matrix = getGeneralMatrix();
        matrix.setSize(numRow-1, numCol-1);
        return matrix;
    }
    
    /**
     * Returns a copy of the matrix.
     */
    public Matrix getMatrix() {
        return getGeneralMatrix();
    }
    
    /**
     * Returns a copy of the matrix.
     */
    private GeneralMatrix getGeneralMatrix() {
        return new GeneralMatrix(numRow, numCol, elt);
    }
    
    /**
     * Gets the dimension of input points.
     */
    public int getSourceDimensions() {
        return numCol-1;
    }
    
    /**
     * Gets the dimension of output points.
     */
    public int getTargetDimensions() {
        return numRow-1;
    }
    
    /**
     * Tests whether this transform does not move any points.
     */
    public boolean isIdentity() {
        if (numRow != numCol) {
            return false;
        }
        int index=0;
        for (int j=0; j<numRow; j++) {
            for (int i=0; i<numCol; i++) {
                if (elt[index++] != (i==j ? 1 : 0)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Creates the inverse transform of this object.
     */
    public MathTransform inverse() throws NoninvertibleTransformException {
        if (isIdentity()) {
            return this;
        }
        final GeneralMatrix matrix = getGeneralMatrix();
        try {
            matrix.invert();
        } catch (SingularMatrixException exception) {
            NoninvertibleTransformException e = new NoninvertibleTransformException(
                    Resources.format(ResourceKeys.ERROR_NONINVERTIBLE_TRANSFORM));
            e.initCause(exception);
            throw e;
        }
        return new ProjectiveTransform(matrix);
    }
    
    /**
     * Returns a hash value for this transform.
     * This value need not remain consistent between
     * different implementations of the same class.
     */
    public int hashCode() {
        long code = serialVersionUID;
        for (int i=elt.length; --i>=0;) {
            code = code*37 + Double.doubleToLongBits(elt[i]);
        }
        return (int)(code >>> 32) ^ (int)code;
    }
    
    /**
     * Compares the specified object with
     * this math transform for equality.
     */
    public boolean equals(final Object object) {
        if (object == this) {
            // Slight optimization
            return true;
        }
        if (super.equals(object)) {
            final ProjectiveTransform that = (ProjectiveTransform) object;
            return this.numRow == that.numRow &&
                   this.numCol == that.numCol &&
                   Arrays.equals(this.elt, that.elt);
        }
        return false;
    }
    
    /**
     * The provider for {@link ProjectiveTransform}. This transform is registered
     * under the name "Affine", which is a special case of projective transform.
     * The default matrix size is
     * {@value org.geotools.parameter.MatrixParameterDescriptors#DEFAULT_MATRIX_SIZE}&times;{@value
     * org.geotools.parameter.MatrixParameterDescriptors#DEFAULT_MATRIX_SIZE}.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    public static final class Provider extends MathTransformProvider {
        /**
         * Serial number for interoperability with different versions.
         */
        private static final long serialVersionUID = 649555815622129472L;

        /**
         * The set of predefined providers.
         */
        private static OperationMethod[] methods = new OperationMethod[8];

        /**
         * The parameters group.
         *
         * @todo We should register EPSG parameter identifiers (A0, A1, A2, B0, B1, B2) as well.
         */
        static final ParameterDescriptorGroup PARAMETERS;
        static {
            final Identifier name = new Identifier(Citation.OPEN_GIS, "Affine");
            final Map  properties = new HashMap(4, 0.8f);
            properties.put(NAME_PROPERTY,        name);
            properties.put(IDENTIFIERS_PROPERTY, name);
            properties.put(ALIAS_PROPERTY, new Identifier[] {name,
                new Identifier(Citation.EPSG, "Affine general parametric transformation"),
                new Identifier(Citation.EPSG, "9624"),
                new Identifier(Citation.GEOTOOLS,
                    Resources.formatInternational(ResourceKeys.AFFINE_TRANSFORM))
            });
            PARAMETERS = new MatrixParameterDescriptors(properties);
        }

        /**
         * Creates a provider for affine transform with a default matrix size.
         */
        public Provider() {
            this(MatrixParameterDescriptors.DEFAULT_MATRIX_SIZE-1,
                 MatrixParameterDescriptors.DEFAULT_MATRIX_SIZE-1);
            methods[MatrixParameterDescriptors.DEFAULT_MATRIX_SIZE-2] = this;
        }

        /**
         * Creates a provider for affine transform with the specified dimensions.
         */
        private Provider(final int sourceDimensions, final int targetDimensions) {
            super(sourceDimensions, targetDimensions, PARAMETERS);
        }

        /**
         * Returns the operation type.
         */
        protected Class getOperationType() {
            return Conversion.class;
        }

        /**
         * Creates a projective transform from the specified group of parameter values.
         *
         * @param  values The group of parameter values.
         * @return The created math transform.
         * @throws ParameterNotFoundException if a required parameter was not found.
         */
        protected MathTransform createMathTransform(final ParameterValueGroup values)
                throws ParameterNotFoundException
        {
            return create(((MatrixParameterDescriptors) getParameters()).getMatrix(values));
        }

        /**
         * Returns the operation method for the specified math transform.
         * This method provides different methods for different matrix sizes.
         */
        protected OperationMethod getMethod(final MathTransform mt) {
            return getMethod(mt.getSourceDimensions(),
                             mt.getTargetDimensions());
        }

        /**
         * Returns the operation method for the specified source and target dimensions.
         * This method provides different methods for different matrix sizes.
         */
        public static OperationMethod getMethod(final int sourceDimensions,
                                                final int targetDimensions)
        {
            if (sourceDimensions == targetDimensions) {
                final int i = sourceDimensions - 1;
                if (i>=0 && i<methods.length) {
                    OperationMethod method = methods[i];
                    if (method == null) {
                        methods[i] = method = new Provider(sourceDimensions, targetDimensions);
                    }
                    return method;
                }
            }
            return new Provider(sourceDimensions, targetDimensions);
        }
    }
}
