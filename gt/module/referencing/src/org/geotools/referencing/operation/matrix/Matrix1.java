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

// J2SE dependencies
import java.io.Serializable;
import javax.vecmath.SingularMatrixException;

// OpenGIS dependencies
import org.opengis.referencing.operation.Matrix;


/**
 * A matrix of fixed {@value #SIZE}&times;{@value #SIZE} size. This trivial matrix is returned as a
 * result of {@linkplain org.opengis.referencing.operation.MathTransform1D} derivative computation.
 *
 * @since 2.2
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class Matrix1 implements XMatrix, Serializable {
    /** Serial number for interoperability with different versions. */
    private static final long serialVersionUID = -4829171016106097031L;

    /** The only element in this matrix. */
    public double m00;

    /** The matrix size, which is {@value}. */
    public static final int SIZE = 1;

    /**
     * Creates a new identity matrix.
     */
    public Matrix1() {
        m00 = 1;
    }

    /**
     * Creates a new matrix initialized to the specified value.
     */
    public Matrix1(final double m00) {
        this.m00 = m00;
    }

    /**
     * Creates a new matrix initialized to the same value than the specified one.
     * The specified matrix size must be {@value #SIZE}&times;{@value #SIZE}.
     */
    public Matrix1(final Matrix matrix) {
        if (matrix.getNumRow()!=SIZE || matrix.getNumCol()!=SIZE) {
            // TODO: localize. Same message than BursaWolfParameters.
            throw new IllegalArgumentException("Illegal matrix size.");
        }
        m00 = matrix.getElement(0,0);
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
     * Returns the element at the specified index.
     */
    public final double getElement(final int row, final int col) {
        if (row==0 && col==0) {
            return m00;
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Set the element at the specified index.
     */
    public final void setElement(final int row, final int col, final double value) {
        if (row==0 && col==0) {
            m00 = value;
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * {@inheritDoc}
     */
    public final void setZero() {
        m00 = 0;
    }

    /**
     * {@inheritDoc}
     */
    public final void setIdentity() {
        m00 = 1;
    }

    /**
     * Returns {@code true} if this matrix is an identity matrix.
     */
    public final boolean isIdentity() {
        return m00==1;
    }

    /**
     * {@inheritDoc}
     */
    public final boolean isAffine() {
        return m00==1;
    }

    /**
     * {@inheritDoc}
     */
    public final void negate() {
        m00 = -m00;
    }

    /**
     * {@inheritDoc}
     */
    public final void transpose() {
        // Nothing to do for a 1x1 matrix.
    }

    /**
     * Inverts this matrix in place.
     */
    public final void invert() {
        if (m00 == 0) {
            throw new SingularMatrixException();
        }
        m00 = 1/m00;
    }

    /**
     * {@inheritDoc}
     */
    public final void multiply(final Matrix matrix) {
        if (matrix.getNumRow()!=SIZE || matrix.getNumCol()!=SIZE) {
            // TODO: localize. Same message than BursaWolfParameters.
            throw new IllegalArgumentException("Illegal matrix size.");
        }
        m00 *= matrix.getElement(0,0);
    }

    /**
     * Returns {@code true} if the specified object is of type {@code Matrix1} and
     * all of the data members are equal to the corresponding data members in this matrix.
     */
    public boolean equals(final Object object) {
        if (object!=null && object.getClass().equals(getClass())) {
            final Matrix1 that = (Matrix1) object;
            return Double.doubleToLongBits(this.m00) == Double.doubleToLongBits(that.m00);
        }
        return false;
    }

    /**
     * Returns a hash code value based on the data values in this object.
     */
    public int hashCode() {
        return (int)(Double.doubleToLongBits(m00) ^ serialVersionUID);
    }
    
    /**
     * Returns a string representation of this matrix. The returned string is implementation
     * dependent. It is usually provided for debugging purposes only.
     */
    public String toString() {
        return GeneralMatrix.toString(this);
    }

    /**
     * Returns a clone of this matrix.
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // Should not happen, since we are cloneable.
            throw new AssertionError(e);
        }
    }
}
