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

// J2SE dependencies
import java.util.Arrays;
import java.io.Serializable;
import java.awt.geom.Point2D;
import java.awt.geom.AffineTransform;
import javax.vecmath.GMatrix;
import javax.vecmath.SingularMatrixException;

// OpenGIS dependencies
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.spatialschema.geometry.DirectPosition;

// Geotools dependencies and resources
import org.geotools.parameter.ParameterValue;
import org.geotools.referencing.wkt.Formatter;
import org.geotools.referencing.operation.GeneralMatrix;
import org.geotools.referencing.operation.LinearTransform;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * Transforms multi-dimensional coordinate points using a {@linkplain GeneralMatrix matrix}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class MatrixTransform extends AbstractMathTransform implements LinearTransform, Serializable {
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
    protected MatrixTransform(final Matrix matrix) {
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
        final int dimension = matrix.getNumRow();
        if (dimension == matrix.getNumCol()) {
            if (matrix.isIdentity()) {
                return IdentityTransform.create(dimension);
            }
            final GeneralMatrix m = wrap(matrix);
            if (m.isAffine()) {
                switch (dimension) {
                    case 2: return LinearTransform1D.create(m.getElement(0,0), m.getElement(0,1));
                    case 3: return create(m.toAffineTransform2D());
                }
            }
        }
        return new MatrixTransform(matrix);
    }

    /**
     * Creates a transform for the specified matrix as a Java2D object.
     * This method is provided for interoperability with
     * <A HREF="http://java.sun.com/products/java-media/2D/index.jsp">Java2D</A>.
     */
    public static LinearTransform create(final AffineTransform matrix) {
        return new AffineTransform2D(matrix);
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
     * The transformed points <code>(x',y',z')</code> are computed as below (note that
     * this computation is similar to {@link javax.media.jai.PerspectiveTransform}):
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
     * The transformed points <code>(x',y',z')</code> are computed as below (note that
     * this computation is similar to {@link javax.media.jai.PerspectiveTransform}):
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
    public int getDimSource() {
        return numCol-1;
    }
    
    /**
     * Gets the dimension of output points.
     */
    public int getDimTarget() {
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
        return new MatrixTransform(matrix);
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
            final MatrixTransform that = (MatrixTransform) object;
            return this.numRow == that.numRow &&
                   this.numCol == that.numCol &&
                   Arrays.equals(this.elt, that.elt);
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
     */
    protected String formatWKT(final Formatter formatter) {
        return formatWKT(formatter, getMatrix());
    }

    /**
     * Implementation of {@link #formatWKT(Formatter)} for the specified matrix.
     */
    static String formatWKT(final Formatter formatter, final Matrix matrix) {
        final int numRow = matrix.getNumRow();
        final int numCol = matrix.getNumCol();
        formatter.append("Affine");
        formatter.append(new ParameterValue("num_row", numRow));
        formatter.append(new ParameterValue("num_col", numCol));
        final StringBuffer eltBuf = new StringBuffer("elt_");
        for (int j=0; j<numRow; j++) {
            for (int i=0; i<numCol; i++) {
                final double value = matrix.getElement(j,i);
                if (value != (i==j ? 1 : 0)) {
                    eltBuf.setLength(4);
                    eltBuf.append(j);
                    eltBuf.append('_');
                    eltBuf.append(i);
                    formatter.append(new ParameterValue(eltBuf.toString(), value, null));
                }
            }
        }
        return "PARAM_MT";
    }
    
// TODO
//    
//    /**
//     * The provider for {@link MatrixTransform}.
//     *
//     * @version $Id$
//     * @author Martin Desruisseaux
//     */
//    static final class Provider extends MathTransformProvider {
//        /**
//         * Create a provider for affine transform.
//         * The default matrix size is 4&times;4.
//         */
//        public Provider() {
//            super("Affine", ResourceKeys.AFFINE_TRANSFORM, null);
//            final int defaultSize = MatrixParameters.DEFAULT_SIZE.intValue();
//            putInt("num_row", defaultSize, MatrixParameters.POSITIVE_RANGE);
//            putInt("num_col", defaultSize, MatrixParameters.POSITIVE_RANGE);
//        }
//    
//        /**
//         * Returns a newly created parameter list. This custom parameter list
//         * is different from the default one in that it is "extensible", i.e.
//         * new parameters may be added if the matrix's size growth.
//         */
//        public ParameterList getParameterList() {
//            return new MatrixParameters();
//        }
//        
//        /**
//         * Returns a transform for the specified parameters.
//         *
//         * @param  parameters The parameter values in standard units.
//         * @return A {@link MathTransform} object of this classification.
//         *
//         * @task REVISIT: Should we invoke {@link MathTransformFactory#createAffineTransform}
//         *       instead? It would force us to keep a reference to {@link MathTransformFactory}
//         *       (and not forget to change the reference if this provider is copied into an
//         *       other factory)...
//         */
//        public MathTransform create(final ParameterList parameters) {
//            final Matrix matrix = MatrixParameters.getMatrix(parameters);
//            if (matrix.isAffine()) {
//                switch (matrix.getNumRow()) {
//                    case 3: return new AffineTransform2D(matrix.toAffineTransform2D());
//                    case 2: return LinearTransform1D.create(matrix.getElement(0,0),
//                                                            matrix.getElement(0,1));
//                }
//            }
//            if (matrix.isIdentity()) {
//                // The 1D and 2D cases have their own optimized identity transform,
//                // which is why this test must come after the 'isAffine()' test.
//                return new IdentityTransform(matrix.getNumRow()-1);
//            }
//            return new MatrixTransform(matrix);
//        }
//    }
}
