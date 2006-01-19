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
package org.geotools.ct;

// J2SE dependencies
import java.util.Arrays;


/**
 * A one dimensional, constant transform. Output values are set to a constant value regardless
 * of input values. This class is really a special case of {@link LinearTransform1D} in which
 * <code>{@link #scale} = 0</code> and <code>{@link #offset} = constant</code>. However, this
 * specialized <code>ConstantTransform1D</code> class is faster.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @deprecated Replaced by {@link org.geotools.referencing.operation.ConstantTransform1D}
 *             in the <code>org.geotools.referencing.operation.transform</code> package.
 */
final class ConstantTransform1D extends LinearTransform1D {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -1583675681650985947L;

    /**
     * Construct a new constant transform.
     *
     * @param offset The <code>offset</code> term in the linear equation.
     */
    protected ConstantTransform1D(final double offset) {
        super(0, offset);
    }
    
    /**
     * Transforms the specified value.
     */
    public double transform(double value) {
        return offset;
    }
    
    /**
     * Transforms a list of coordinate point ordinal values.
     */
    public void transform(final float[] srcPts, int srcOff,
                          final float[] dstPts, int dstOff, int numPts)
    {
        Arrays.fill(dstPts, dstOff, dstOff+numPts, (float)offset);
    }
    
    /**
     * Transforms a list of coordinate point ordinal values.
     */
    public void transform(final double[] srcPts, int srcOff,
                          final double[] dstPts, int dstOff, int numPts)
    {
        Arrays.fill(dstPts, dstOff, dstOff+numPts, offset);
    }
}
