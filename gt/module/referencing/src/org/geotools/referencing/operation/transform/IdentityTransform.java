/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2002, Institut de Recherche pour le Développement
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
import java.awt.geom.AffineTransform;
import java.io.Serializable;

// OpenGIS dependencies
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.Matrix;
import org.opengis.spatialschema.geometry.DirectPosition;

// Geotools dependencies
import org.geotools.referencing.operation.LinearTransform;
import org.geotools.referencing.operation.matrix.MatrixFactory;


/**
 * The identity transform. The data are only copied without any transformation. This class is
 * used for identity transform of dimension greater than 2. For 1D and 2D identity transforms,
 * {@link LinearTransform1D} and {@link java.awt.geom.AffineTransform} already provide their
 * own optimisations.
 *
 * @since 2.0
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class IdentityTransform extends AbstractMathTransform
                            implements LinearTransform, Serializable
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -5339040282922138164L;
    
    /**
     * The input and output dimension.
     */
    private final int dimension;

    /**
     * Identity transforms for dimensions ranging from to 0 to 7.
     * Elements in this array will be created only when first requested.
     */
    private static final LinearTransform[] POOL = new LinearTransform[8];
    
    /**
     * Constructs an identity transform of the specified dimension.
     */
    protected IdentityTransform(final int dimension) {
        this.dimension = dimension;
    }

    /**
     * Constructs an identity transform of the specified dimension.
     */
    public static LinearTransform create(final int dimension) {
        // No need to synchronize; not a big deal in a few objects are duplicated.
        LinearTransform candidate;
        if (dimension < POOL.length) {
            candidate = POOL[dimension];
            if (candidate != null) {
                return candidate;
            }
        }
        switch (dimension) {
            case 1:  candidate = LinearTransform1D.IDENTITY;                   break;
            case 2:  candidate = new AffineTransform2D(new AffineTransform()); break;
            default: candidate = new IdentityTransform(dimension);             break;
        }
        if (dimension < POOL.length) {
            POOL[dimension] = candidate;
        }
        return candidate;
    }
    
    /**
     * Tests whether this transform does not move any points.
     */
    public boolean isIdentity() {
        return true;
    }
    
    /**
     * Gets the dimension of input points.
     */
    public int getSourceDimensions() {
        return dimension;
    }
    
    /**
     * Gets the dimension of output points.
     */
    public int getTargetDimensions() {
        return dimension;
    }

    /**
     * Returns the parameter descriptors for this math transform.
     */
    public ParameterDescriptorGroup getParameterDescriptors() {
        return ProjectiveTransform.ProviderAffine.PARAMETERS;
    }

    /**
     * Returns the matrix elements as a group of parameters values.
     *
     * @return A copy of the parameter values for this math transform.
     */
    public ParameterValueGroup getParameterValues() {
        return ProjectiveTransform.getParameterValues(getMatrix());
    }
    
    /**
     * Returns a copy of the identity matrix.
     */
    public Matrix getMatrix() {
        return MatrixFactory.create(dimension+1);
    }
    
    /**
     * Gets the derivative of this transform at a point. For an identity transform,
     * the derivative is the same everywhere.
     */
    public Matrix derivative(final DirectPosition point) {
        return MatrixFactory.create(dimension);
    }
    
    /**
     * Transforms an array of floating point coordinates by this transform.
     */
    public void transform(final float[] srcPts, int srcOff,
                          final float[] dstPts, int dstOff, int numPts)
    {
        System.arraycopy(srcPts, srcOff, dstPts, dstOff, numPts*dimension);
    }
    
    /**
     * Transforms an array of floating point coordinates by this transform.
     */
    public void transform(final double[] srcPts, int srcOff,
                          final double[] dstPts, int dstOff, int numPts)
    {
        System.arraycopy(srcPts, srcOff, dstPts, dstOff, numPts*dimension);
    }
    
    /**
     * Returns the inverse transform of this object, which
     * is this transform itself
     */
    public MathTransform inverse() {
        return this;
    }
    
    /**
     * Returns a hash value for this transform.
     * This value need not remain consistent between
     * different implementations of the same class.
     */
    public int hashCode() {
        return (int)serialVersionUID + dimension;
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
            final IdentityTransform that = (IdentityTransform) object;
            return this.dimension == that.dimension;
        }
        return false;
    }
}
