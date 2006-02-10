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
import javax.vecmath.Matrix4d;

// OpenGIS dependencies
import org.opengis.referencing.operation.Matrix;

// Geotools dependencies
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;


/**
 * A matrix of fixed {@value #SIZE}&times;{@value #SIZE} size. This specialized matrix provides
 * better accuracy than {@link GeneralMatrix} for matrix inversion and multiplication. It is used
 * primarily for supporting datum shifts.
 *
 * @since 2.2
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class Matrix4 extends Matrix4d implements XMatrix {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 5685762518066856310L;

    /**
     * The matrix size, which is {@value}.
     */
    public static final int SIZE = 4;
    
    /**
     * Creates a new identity matrix.
     */
    public Matrix4() {
        setIdentity();
    }

    /**
     * Creates a new matrix initialized to the specified values.
     */
    public Matrix4(double m00, double m01, double m02, double m03,
                   double m10, double m11, double m12, double m13,
                   double m20, double m21, double m22, double m23,
                   double m30, double m31, double m32, double m33)
    {
        super(m00, m01, m02, m03,
              m10, m11, m12, m13,
              m20, m21, m22, m23,
              m30, m31, m32, m33);
    }

    /**
     * Creates a new matrix initialized to the same value than the specified one.
     * The specified matrix size must be {@value #SIZE}&times;{@value #SIZE}.
     */
    public Matrix4(final Matrix matrix) {
        if (matrix.getNumRow()!=SIZE || matrix.getNumCol()!=SIZE) {
            throw new IllegalArgumentException(Errors.format(ErrorKeys.ILLEGAL_MATRIX_SIZE));
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
        return m30==0 && m31==0 && m32==0 && m33==1;
    }

    /**
     * {@inheritDoc}
     */
    public final void multiply(final Matrix matrix) {
        final Matrix4d m;
        if (matrix instanceof Matrix4d) {
            m = (Matrix4d) matrix;
        } else {
            m = new Matrix4(matrix);
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
