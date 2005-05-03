/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
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
package org.geotools.coverage.operation;

// J2SE dependencies
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.RasterFormatException;

// JAI dependencies
import javax.media.jai.Warp;

// OpenGIS dependencies
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.gcs.ResourceKeys;
import org.geotools.resources.gcs.Resources;
import org.geotools.referencing.operation.transform.WarpTransform2D;


/**
 * Wraps an arbitrary {@link MathTransform2D} into an image warp operation.
 * This warp operation is used by {@link Resampler2D} when no standard warp
 * operation has been found applicable.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class WarpTransform extends Warp {
    /**
     * The coverage name. Used for formatting error message.
     */
    private final InternationalString name;

    /**
     * The <strong>inverse</strong> of the transform to apply.
     * This transform maps destination pixels to source pixels.
     */
    private final MathTransform2D inverse;
    
    /**
     * Constructs a new <code>WarpTransform</code> using the given transform.
     *
     * @param name    The coverage name. Used for formatting error message.
     * @param inverse The <strong>inverse</strong> of the transformation to apply.
     *                This inverse transform maps destination pixels to source pixels.
     */
    private WarpTransform(final InternationalString name, final MathTransform2D inverse) {
        this.name    = name;
        this.inverse = inverse;
    }
    
    /**
     * Constructs a new <code>WarpTransform</code> using the given transform.
     *
     * @param name    The coverage name. Used for formatting error message.
     * @param inverse The <strong>inverse</strong> of the transformation to apply.
     *                This inverse transform maps destination pixels to source pixels.
     */
    public static Warp create(final InternationalString name, final MathTransform2D inverse) {
        if (inverse instanceof WarpTransform2D) {
            return ((WarpTransform2D) inverse).getWarp();
        }
        return new WarpTransform(name, inverse);
    }
    

    /**
     * Returns the transform from destination pixels to source pixels.
     */
    public MathTransform2D getTransform() {
        return inverse;
    }

    /**
     * Computes the source pixel positions for a given rectangular
     * destination region, subsampled with an integral period.
     */
    public float[] warpSparseRect(final int xmin,    final int ymin,
                                  final int width,   final int height,
                                  final int periodX, final int periodY, float[] destRect)
    {
        if (periodX < 1) throw new IllegalArgumentException(String.valueOf(periodX));
        if (periodY < 1) throw new IllegalArgumentException(String.valueOf(periodY));

        final int xmax  = xmin + width;
        final int ymax  = ymin + height;
        final int count = ((width+(periodX-1))/periodX) * ((height+(periodY-1))/periodY);
        if (destRect == null) {
            destRect = new float[2*count];
        }
        int index = 0;
        for (int y=ymin; y<ymax; y+=periodY) {
            for (int x=xmin; x<xmax; x+=periodX) {
                destRect[index++] = x;
                destRect[index++] = y;
            }
        }
        try {
            inverse.transform(destRect, 0, destRect, 0, count);
        } catch (TransformException exception) {
            // At least one transformation failed. In Geotools MapProjection
            // implementation, unprojected coordinates are set to (NaN,NaN).
            RasterFormatException e = new RasterFormatException(Resources.format(
                            ResourceKeys.ERROR_CANT_REPROJECT_$1, name));
            e.initCause(exception);
            throw e;
        }
        return destRect;
    }

    /**
     * Computes a rectangle that is guaranteed to enclose the region of the source
     * that is required in order to produce a given rectangular output region.
     */
    public Rectangle mapDestRect(final Rectangle destRect) {
        try {
            // According OpenGIS specification, GridGeometry maps pixel's center. But
            // the bounding box is for all pixels, not pixel's centers. Offset by
            // -0.5 (use -0.5 for maximum too, not +0.5, since maximum is exclusive).
            Rectangle2D bounds = new Rectangle2D.Double(
                    destRect.x-0.5, destRect.y-0.5, destRect.width, destRect.height);
            // TODO: This rectangle may be approximative. We should improve the algorithm.
            bounds = CRSUtilities.transform(inverse, bounds, bounds);
            return bounds.getBounds();
        } catch (TransformException exception) {
            IllegalArgumentException e = new IllegalArgumentException(Resources.format(
                            ResourceKeys.ERROR_BAD_PARAMETER_$2, "destRect", destRect));
            e.initCause(exception);
            throw e;
        }
    }

    /**
     * Computes a rectangle that is guaranteed to enclose the region of the destination
     * that can potentially be affected by the pixels of a rectangle of a given source.
     */
    public Rectangle mapSourceRect(final Rectangle sourceRect) {
        try {
            // According OpenGIS specification, GridGeometry maps pixel's center. But
            // the bounding box is for all pixels, not pixel's centers. Offset by
            // -0.5 (use -0.5 for maximum too, not +0.5, since maximum is exclusive).
            Rectangle2D bounds = new Rectangle2D.Double(
                    sourceRect.x-0.5, sourceRect.y-0.5, sourceRect.width, sourceRect.height);
            // TODO: This rectangle may be approximative. We should improve the algorithm.
            bounds = CRSUtilities.transform((MathTransform2D)inverse.inverse(), bounds, bounds);
            return bounds.getBounds();
        } catch (TransformException exception) {
            IllegalArgumentException e = new IllegalArgumentException(Resources.format(
                            ResourceKeys.ERROR_BAD_PARAMETER_$2, "sourceRect", sourceRect));
            e.initCause(exception);
            throw e;
        }
    }

    /**
     * Computes the source point corresponding to the supplied point.
     *
     * @param destPt The position in destination image coordinates
     *               to map to source image coordinates.
     */
    public Point2D mapDestPoint(final Point2D destPt) {
        try {
            return inverse.transform(destPt, null);
        } catch (TransformException exception) {
            IllegalArgumentException e = new IllegalArgumentException(Resources.format(
                            ResourceKeys.ERROR_BAD_PARAMETER_$2, "destPt", destPt));
            e.initCause(exception);
            throw e;
        }
    }

    /**
     * Computes the destination point corresponding to the supplied point.
     *
     * @param sourcePt The position in source image coordinates
     *                 to map to destination image coordinates.
     */
    public Point2D mapSourcePoint(final Point2D sourcePt) {
        try {
            return ((MathTransform2D)inverse.inverse()).transform(sourcePt, null);
        } catch (TransformException exception) {
            IllegalArgumentException e = new IllegalArgumentException(Resources.format(
                            ResourceKeys.ERROR_BAD_PARAMETER_$2, "sourcePt", sourcePt));
            e.initCause(exception);
            throw e;
        }
    }
}
