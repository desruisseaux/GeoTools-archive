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
package org.geotools.referencing.operation;

// J2SE dependencies
import java.awt.geom.Point2D;
import java.awt.geom.AffineTransform;

// OpenGIS dependencies
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.spatialschema.geometry.DirectPosition;

// Geotools dependencies
import org.geotools.referencing.wkt.Formatter;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.resources.geometry.XAffineTransform;


/**
 * Transforms two-dimensional coordinate points using an {@link AffineTransform}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class AffineTransform2D extends XAffineTransform implements MathTransform2D, LinearTransform {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -5299837898367149069L;
    
    /**
     * The inverse transform. This field
     * will be computed only when needed.
     */
    private transient AffineTransform2D inverse;
    
    /**
     * Construct an affine transform.
     */
    protected AffineTransform2D(final AffineTransform transform) {
        super(transform);
    }
    
    /**
     * Throws an {@link UnsupportedOperationException} when a mutable method
     * is invoked, since <code>AffineTransform2D</code> must be immutable.
     */
    protected void checkPermission() {
        throw new UnsupportedOperationException(
                Resources.format(ResourceKeys.ERROR_UNMODIFIABLE_AFFINE_TRANSFORM));
    }
    
    /**
     * Gets the dimension of input points.
     */
    public int getDimSource() {
        return 2;
    }
    
    /**
     * Gets the dimension of output points.
     */
    public int getDimTarget() {
        return 2;
    }
    
    /**
     * Transforms the specified <code>ptSrc</code> and stores the result in <code>ptDst</code>.
     */
    public DirectPosition transform(final DirectPosition ptSrc, DirectPosition ptDst) {
        if (ptDst == null) {
            ptDst = new org.geotools.geometry.DirectPosition(2);
        }
        final double[] array = ptSrc.getCoordinates();
        transform(array, 0, array, 0, 1);
        ptDst.setOrdinate(0, array[0]);
        ptDst.setOrdinate(1, array[1]);
        return ptDst;
    }
    
    /**
     * Returns this transform as an affine transform matrix.
     */
    public Matrix getMatrix() {
        return new org.geotools.referencing.operation.Matrix(this);
    }
    
    /**
     * Gets the derivative of this transform at a point.
     * For an affine transform, the derivative is the
     * same everywhere.
     */
    public Matrix derivative(final Point2D point) {
        final Matrix matrix = new org.geotools.referencing.operation.Matrix(2);
        matrix.setElement(0,0, getScaleX());
        matrix.setElement(1,1, getScaleY());
        matrix.setElement(0,1, getShearX());
        matrix.setElement(1,0, getShearY());
        return matrix;
    }
    
    /**
     * Gets the derivative of this transform at a point.
     * For an affine transform, the derivative is the
     * same everywhere.
     */
    public Matrix derivative(final DirectPosition point) {
        return derivative((Point2D) null);
    }
    
    /**
     * Creates the inverse transform of this object.
     */
    public MathTransform inverse() throws NoninvertibleTransformException {
        if (inverse == null) {
            if (isIdentity()) {
                inverse = this;
            } else try {
                synchronized (this) {
                    inverse = new AffineTransform2D(createInverse());
                    inverse.inverse = this;
                }
            } catch (java.awt.geom.NoninvertibleTransformException exception) {
                throw new NoninvertibleTransformException(exception.getLocalizedMessage(), exception);
            }
        }
        return inverse;
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
        return MatrixTransform.formatWKT(formatter, getMatrix());
    }
}
