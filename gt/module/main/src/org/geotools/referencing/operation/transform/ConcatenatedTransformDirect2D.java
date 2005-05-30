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
import java.awt.Shape;
import java.awt.geom.Point2D;

// OpenGIS dependencies
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.operation.TransformException;

// Geotools dependencies
import org.geotools.referencing.operation.GeneralMatrix;


/**
 * Concatenated transform where both transforms are two-dimensional.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @since 2.0
 */
final class ConcatenatedTransformDirect2D extends ConcatenatedTransformDirect
                                       implements MathTransform2D
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 6009454091075588885L;
    
    /**
     * The first math transform. This field is identical
     * to {@link ConcatenatedTransform#transform1}. Only
     * the type is different.
     */
    private final MathTransform2D transform1;
    
    /**
     * The second math transform. This field is identical
     * to {@link ConcatenatedTransform#transform1}. Only
     * the type is different.
     */
    private final MathTransform2D transform2;
    
    /**
     * Constructs a concatenated transform.
     */
    public ConcatenatedTransformDirect2D(final MathTransform2D transform1,
                                         final MathTransform2D transform2)
    {
        super(transform1, transform2);
        this.transform1 = transform1;
        this.transform2 = transform2;
    }
    
    /**
     * Check if transforms are compatibles with this implementation.
     */
    boolean isValid() {
        return super.isValid() && getSourceDimensions()==2 && getTargetDimensions()==2;
    }
    
    /**
     * Transforms the specified {@code ptSrc}
     * and stores the result in {@code ptDst}.
     */
    public Point2D transform(final Point2D ptSrc, Point2D ptDst) throws TransformException {
        assert isValid();
        ptDst = transform1.transform(ptSrc, ptDst);
        return  transform2.transform(ptDst, ptDst);
    }
    
    /**
     * Transform the specified shape.
     */
    public Shape createTransformedShape(final Shape shape) throws TransformException {
        assert isValid();
        return transform2.createTransformedShape(transform1.createTransformedShape(shape));
    }
    
    /**
     * Gets the derivative of this transform at a point.
     *
     * @param  point The coordinate point where to evaluate the derivative.
     * @return The derivative at the specified point (never {@code null}).
     * @throws TransformException if the derivative can't be evaluated at the specified point.
     */
    public Matrix derivative(final Point2D point) throws TransformException {
        final GeneralMatrix matrix1 = wrap(transform1.derivative(point));
        final GeneralMatrix matrix2 = wrap(transform2.derivative(transform1.transform(point,null)));
        matrix2.mul(matrix1);
        return matrix2;
    }
}
