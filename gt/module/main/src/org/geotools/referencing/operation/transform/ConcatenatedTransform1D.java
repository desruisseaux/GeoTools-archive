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
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform1D;
import org.opengis.referencing.operation.TransformException;

// Geotools dependencies
import org.geotools.geometry.DirectPosition;


/**
 * Concatenated transform in which the resulting transform is one-dimensional.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class ConcatenatedTransform1D extends ConcatenatedTransform implements MathTransform1D {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 8150427971141078395L;
    
    /**
     * Construct a concatenated transform.
     */
    public ConcatenatedTransform1D(final MathTransform transform1,
                                   final MathTransform transform2)
    {
        super(transform1, transform2);
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
        final double[] values = new double[] {value};
        final double[] buffer = new double[] {transform1.getDimTarget()};
        transform1.transform(values, 0, buffer, 0, 1);
        transform2.transform(buffer, 0, values, 0, 1);
        return values[0];
    }
    
    /**
     * Gets the derivative of this function at a value.
     */
    public double derivative(final double value) throws TransformException {
        final DirectPosition p = new DirectPosition(1);
        p.ordinates[0] = value;
        final Matrix m = derivative(p);
        assert m.getNumRow()==1 && m.getNumCol()==1;
        return m.getElement(0,0);
    }
}
