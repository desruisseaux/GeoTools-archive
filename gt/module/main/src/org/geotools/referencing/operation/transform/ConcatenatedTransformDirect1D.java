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
import org.opengis.referencing.operation.MathTransform1D;
import org.opengis.referencing.operation.TransformException;


/**
 * Concatenated transform where both transforms are one-dimensional.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class ConcatenatedTransformDirect1D extends ConcatenatedTransformDirect
                                          implements MathTransform1D
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 1064398659892864966L;
    
    /**
     * The first math transform. This field is identical
     * to {@link ConcatenatedTransform#transform1}. Only
     * the type is different.
     */
    private final MathTransform1D transform1;
    
    /**
     * The second math transform. This field is identical
     * to {@link ConcatenatedTransform#transform1}. Only
     * the type is different.
     */
    private final MathTransform1D transform2;
    
    /**
     * Construct a concatenated transform.
     */
    public ConcatenatedTransformDirect1D(final MathTransform1D transform1,
                                         final MathTransform1D transform2)
    {
        super(transform1, transform2);
        this.transform1 = transform1;
        this.transform2 = transform2;
    }
    
    /**
     * Check if transforms are compatibles with this implementation.
     */
    boolean isValid() {
        return super.isValid() && getDimSource()==1 && getDimTarget()==1;
    }
    
    /**
     * Transforms the specified value.
     */
    public double transform(final double value) throws TransformException {
        return transform2.transform(transform1.transform(value));
    }
    
    /**
     * Gets the derivative of this function at a value.
     */
    public double derivative(final double value) throws TransformException {
        final double value1 = transform1.derivative(value);
        final double value2 = transform2.derivative(transform1.transform(value));
        return value2 * value1;
    }
}
