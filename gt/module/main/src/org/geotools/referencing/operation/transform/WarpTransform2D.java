/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Management Committee (PMC)
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
import java.awt.geom.Point2D;
import java.io.Serializable;

// JAI dependencies
import javax.media.jai.Warp;

// OpenGIS dependencies
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.NoninvertibleTransformException;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.coverage.operation.WarpTransform;


/**
 * Wraps an arbitrary {@link Warp} object as a {@linkplain MathTransform2D two-dimensional transform}.
 * Calls to {@linkplain #transform(float[],int,float[],int,int) transform} methods are forwarded to
 * the {@link Warp#warpPoint(int,int,float[]) Warp.warpPoint} method. This implies that the current
 * implementation of {@code WarpTransform2D} may round source coordinates to nearest integers before
 * to apply the transformation.
 *
 * @version $Id$
 * @author Alessio Fabiani
 * @author Martin Desruisseaux
 *
 * @see Warp
 */
public class WarpTransform2D extends AbstractMathTransform implements MathTransform2D, Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -7949539694656719923L;

    /**
     * The warp object. Transformations will be applied using the
     * {@link Warp#warpPoint(int,int,float[]) warpPoint} method or something equivalent.
     */
    private final Warp warp;

    /**
     * The inverse math transform.
     */
    private final WarpTransform2D inverse;

    /**
     * Constructs a transform using the specified warp object. This private constructor is used
     * for the construction of {@link #inverse} transform only.
     */
    private WarpTransform2D(final Warp warp, final WarpTransform2D inverse) {
        this.warp    = warp;
        this.inverse = inverse;
    }

    /**
     * Constructs a transform using the specified warp object. Transformations will be applied
     * using the {@link Warp#warpPoint(int,int,float[]) warpPoint} method or something equivalent.
     *
     * @param warp    The image warp to wrap into a math transform.
     * @param inverse An image warp to uses for the {@linkplain #inverse inverse transform},
     *                or {@code null} in none.
     */
    protected WarpTransform2D(final Warp warp, final Warp inverse) {
        ensureNonNull("warp", warp);
        this.warp    = warp;
        this.inverse = (inverse!=null) ? new WarpTransform2D(inverse, this) : null;
    }

    /**
     * Constructs a transform using the specified warp object. Transformations will be applied
     * using the {@link Warp#warpPoint(int,int,float[]) warpPoint} method or something equivalent.
     *
     * @param warp The image warp to wrap into a math transform.
     */
    public static MathTransform2D create(final Warp warp) {
        if (warp instanceof WarpTransform) {
            return ((WarpTransform) warp).getTransform();
        }
        return new WarpTransform2D(warp, (Warp)null);
    }

    /**
     * Returns the wrapped image warp.
     */
    public Warp getWarp() {
        return warp;
    }

    /**
     * Returns the dimension of input points.
     */
    public int getSourceDimensions() {
        return 2;
    }

    /**
     * Returns the dimension of output points.
     */
    public int getTargetDimensions() {
        return 2;
    }

    /**
     * Tests if this transform is the identity transform.
     */
    public boolean isIdentity() {
        return false;
    }

    /**
     * Transforms source coordinates (usually pixel indices) into destination coordinates
     * (usually "real world" coordinates).
     *
     * @param ptSrc the specified coordinate point to be transformed.
     * @param ptDst the specified coordinate point that stores the result of transforming
     *              {@code ptSrc}, or {@code null}.
     * @return the coordinate point after transforming {@code ptSrc} and storing the result in
     *         {@code ptDst}.
     */
    public Point2D transform(final Point2D ptSrc, final Point2D ptDst) {
        final Point2D result = warp.mapDestPoint(ptSrc);
        if (ptDst == null) {
            return result;
        } else {
            ptDst.setLocation(result);
            return result;
        }
    }

    /**
     * Transforms source coordinates (usually pixel indices) into destination coordinates
     * (usually "real world" coordinates).
     */
    public void transform(final float[] srcPts, int srcOff,
                          final float[] dstPts, int dstOff, int numPts)
    {
        final int postIncrement;
        if (srcPts == dstPts  &&  srcOff < dstOff) {
            srcOff += (numPts-1)*2;
            dstOff += (numPts-1)*2;
            postIncrement = -4;
        } else {
            postIncrement = 0;
        }
        float[] dstCoords = null;
        while (--numPts >= 0) {
            final float xi = srcPts[srcOff++];
            final float yi = srcPts[srcOff++];
            warp.warpPoint(Math.round(xi), Math.round(yi), dstCoords);
            dstPts[dstOff++] = dstCoords[0];
            dstPts[dstOff++] = dstCoords[1];
            dstOff += postIncrement;
        }
    }

    /**
     * Transforms source coordinates (usually pixel indices) into destination coordinates
     * (usually "real world" coordinates).
     */
    public void transform(final double[] srcPts, int srcOff,
                          final double[] dstPts, int dstOff, int numPts)
    {
        final int postIncrement;
        if (srcPts == dstPts  &&  srcOff < dstOff) {
            srcOff += (numPts-1)*2;
            dstOff += (numPts-1)*2;
            postIncrement = -4;
        } else {
            postIncrement = 0;
        }
        float[] dstCoords = null;
        while (--numPts >= 0) {
            final double xi = srcPts[srcOff++];
            final double yi = srcPts[srcOff++];
            warp.warpPoint((int)Math.round(xi), (int)Math.round(yi), dstCoords);
            dstPts[dstOff++] = dstCoords[0];
            dstPts[dstOff++] = dstCoords[1];
            dstOff += postIncrement;
        }
    }

    /**
     * Returns the inverse transform.
     *
     * @throws NoninvertibleTransformException if no inverse warp were specified at construction time.
     */
    public MathTransform inverse() throws NoninvertibleTransformException {
        if (inverse != null) {
            return inverse;
        } else {
            return super.inverse();
        }
    }

    /**
     * Returns a hash value for this transform.
     */
    public int hashCode() {
        return (int)serialVersionUID ^ super.hashCode() ^ warp.hashCode();
    }

    /**
     * Compare this transform with the specified object for equality.
     */
    public boolean equals(final Object object) {
        if (super.equals(object)) {
            final WarpTransform2D that = (WarpTransform2D) object;
            return Utilities.equals(this.warp, that.warp);
        }
        return false;
    }
}
