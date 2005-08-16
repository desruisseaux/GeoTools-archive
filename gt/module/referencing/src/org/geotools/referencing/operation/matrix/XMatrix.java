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

// OpenGIS dependencies
import org.opengis.referencing.operation.Matrix;


/**
 * A matrix capables to perform some matrix operations. The basic {@link Matrix} interface
 * is basically just a two dimensional array of numbers. The {@code XMatrix} interface add
 * {@linkplain #invert inversion} and {@linkplain #multiply multiplication} capabilities.
 * It is used as a bridge across various matrix implementations in Java3D
 * ({@link javax.vecmath.Matrix3f}, {@link javax.vecmath.Matrix3d}, {@link javax.vecmath.Matrix4f},
 * {@link javax.vecmath.Matrix4d}, {@link javax.vecmath.GMatrix}).
 *
 * @since 2.2
 * @version $Id$
 * @author Martin Desruisseaux
 */
public interface XMatrix extends Matrix {
    /**
     * Sets all the values in this matrix to zero.
     */
    void setZero();

    /**
     * Sets this matrix to the identity matrix.
     */
    void setIdentity();

    /**
     * Returns {@code true} if this matrix is an affine transform.
     * A transform is affine if the matrix is square and last row contains
     * only zeros, except in the last column which contains 1.
     */
    boolean isAffine();

    /**
     * Negates the value of this matrix: {@code this} = {@code -this}.
     */
    void negate();

    /**
     * Sets the value of this matrix to its transpose.
     */
    void transpose();

    /**
     * Inverts this matrix in place.
     */
    void invert();

    /**
     * Sets the value of this matrix to the result of multiplying itself with the specified matrix.
     * In other words, performs {@code this} = {@code this} &times; {@code matrix}. In the context
     * of coordinate transformations, this is equivalent to
     * <code>{@linkplain java.awt.geom.AffineTransform#concatenate AffineTransform.concatenate}</code>:
     * first transforms by the supplied transform and then transform the result by the original
     * transform.
     */
    void multiply(Matrix matrix);
}
