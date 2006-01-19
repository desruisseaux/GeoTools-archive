/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Managment Committee (PMC)
 * (C) 2005, Institut de Recherche pour le Développement
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
package org.geotools.referencing.operation.matrix;

// J2SE dependencies and extensions
import javax.vecmath.Matrix3d;
import java.awt.geom.AffineTransform;

// OpenGIS dependencies
import org.opengis.referencing.operation.Matrix;


/**
 * A matrix of fixed {@value #SIZE}&times;{@value #SIZE} size. This specialized matrix provides
 * better accuracy than {@link GeneralMatrix} for matrix inversion and multiplication.
 *
 * @since 2.2
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class Matrix3 extends Matrix3d implements XMatrix {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 8902061778871586611L;

    /**
     * The matrix size, which is {@value}.
     */
    public static final int SIZE = 3;

    /**
     * Creates a new identity matrix.
     */
    public Matrix3() {
        setIdentity();
    }

    /**
     * Creates a new matrix initialized to the specified values.
     */
    public Matrix3(double m00, double m01, double m02,
                   double m10, double m11, double m12,
                   double m20, double m21, double m22)
    {
        super(m00, m01, m02,
              m10, m11, m12,
              m20, m21, m22);
    }
    
    /**
     * Constructs a 3&times;3 matrix from the specified affine transform.
     */
    public Matrix3(final AffineTransform transform) {
        m00=transform.getScaleX(); m01=transform.getShearX(); m02=transform.getTranslateX();
        m10=transform.getShearY(); m11=transform.getScaleY(); m12=transform.getTranslateY();
        m22=1;
    }

    /**
     * Creates a new matrix initialized to the same value than the specified one.
     * The specified matrix size must be {@value #SIZE}&times;{@value #SIZE}.
     */
    public Matrix3(final Matrix matrix) {
        if (matrix.getNumRow()!=SIZE || matrix.getNumCol()!=SIZE) {
            // TODO: localize. Same message than BursaWolfParameters.
            throw new IllegalArgumentException("Illegal matrix size.");
        }
        for (int j=0; j<SIZE; j++) {
            for (int i=0; i<SIZE; i++) {
                setElement(j,i, matrix.getElement(j,i));
            }
        }
    }

    /**
     * Returns the number of rows in this matrix, which is always {@value #SIZE}
     * in this implementation.
     */
    public final int getNumRow() {
        return SIZE;
    }

    /**
     * Returns the number of colmuns in this matrix, which is always {@value #SIZE}
     * in this implementation.
     */
    public final int getNumCol() {
        return SIZE;
    }

    /**
     * Returns {@code true} if this matrix is an identity matrix.
     */
    public final boolean isIdentity() {
        for (int j=0; j<SIZE; j++) {
            for (int i=0; i<SIZE; i++) {
                if (getElement(j,i) != ((i==j) ? 1 : 0)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public final boolean isAffine() {
        return m20==0 && m21==0 && m22==1;
    }

    /**
     * {@inheritDoc}
     */
    public final void multiply(final Matrix matrix) {
        final Matrix3d m;
        if (matrix instanceof Matrix3d) {
            m = (Matrix3d) matrix;
        } else {
            m = new Matrix3(matrix);
        }
        mul(m);
    }

    /**
     * Returns a string representation of this matrix. The returned string is implementation
     * dependent. It is usually provided for debugging purposes only.
     */
    public String toString() {
        return GeneralMatrix.toString(this);
    }
}
