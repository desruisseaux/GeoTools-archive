/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2006, Geotools Project Managment Committee (PMC)
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
import org.opengis.referencing.operation.TransformException;


/**
 * Base class for transformations between a <cite>height above the ellipsoid</cite> and a
 * <cite>height above the geoid</cite>. This transform expects three-dimensional geographic
 * coordinates in (<var>longitude</var>,<var>latitude</var>,<var>height</var>) order. The
 * transformations are usually backed by some ellipsoid-dependent database.
 *
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class VerticalTransform extends AbstractMathTransform {
    /**
     * Creates a new instance of {@code VerticalTransform}.
     */
    public VerticalTransform() {
    }

    /**
     * Gets the dimension of input points.
     */
    public final int getSourceDimensions() {
        return 3;
    }

    /**
     * Gets the dimension of output points.
     */
    public final int getTargetDimensions() {
        return 3;
    }

    /**
     * Returns the height above the ellipsoid for the specified geographic coordinate.
     *
     * @param  longitude The geodetic longitude, in decimal degrees.
     * @param  latitude  The geodetic latitude, in decimal degrees.
     * @return The height above the ellipsoid for the specified geographic coordinates.
     * @throws TransformException if the height can't be computed for the specified coordinates.
     */
    public abstract double height(final double longitude, final double latitude)
            throws TransformException;

    /**
     * Transforms a list of coordinate point ordinal values.
     */
    public void transform(final float[] srcPts, int srcOff,
                          final float[] dstPts, int dstOff, int numPts)
            throws TransformException
    {
        final int step;
        if (srcPts == dstPts && srcOff < dstOff) {
            srcOff += 3*(numPts-1);
            dstOff += 3*(numPts-1);
            step = -3;
        } else {
            step = +3;
        }
        while (--numPts >= 0) {
            final float x,y;
            dstPts[dstOff + 0] = x = srcPts[srcOff + 0];
            dstPts[dstOff + 1] = y = srcPts[srcOff + 1];
            dstPts[dstOff + 2] = (float) (srcPts[srcOff + 2] + height(x,y));
            srcOff += step;
            dstOff += step;
        }
    }

    /**
     * Transforms a list of coordinate point ordinal values.
     */
    public void transform(final double[] srcPts, int srcOff,
                          final double[] dstPts, int dstOff, int numPts)
            throws TransformException
    {
        final int step;
        if (srcPts == dstPts && srcOff < dstOff) {
            srcOff += 3*(numPts-1);
            dstOff += 3*(numPts-1);
            step = -3;
        } else {
            step = +3;
        }
        while (--numPts >= 0) {
            final double x,y;
            dstPts[dstOff + 0] = x = srcPts[srcOff + 0];
            dstPts[dstOff + 1] = y = srcPts[srcOff + 1];
            dstPts[dstOff + 2] =     srcPts[srcOff + 2] + height(x,y);
            srcOff += step;
            dstOff += step;
        }
    }
}
