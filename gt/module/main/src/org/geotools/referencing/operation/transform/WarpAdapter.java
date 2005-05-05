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
package org.geotools.referencing.operation.transform;

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

// Geotools dependencies
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.gcs.ResourceKeys;
import org.geotools.resources.gcs.Resources;


/**
 * Wraps an arbitrary {@link MathTransform2D} into an image warp operation.
 * This warp operation is used by {@link org.geotools.coverage.operation.Resampler2D}
 * when no standard warp operation has been found applicable.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class WarpAdapter extends Warp {
    /**
     * The coverage name. Used for formatting error message.
     */
    private final CharSequence name;

    /**
     * The <strong>inverse</strong> of the transform to apply for projecting an image.
     * This transform maps destination pixels to source pixels.
     */
    private final MathTransform2D inverse;
    
    /**
     * Constructs a new <code>WarpAdapter</code> using the given transform.
     *
     * @param name    The coverage name. Used for formatting error message.
     * @param inverse The <strong>inverse</strong> of the transformation to apply for projecting
     *                an image. This inverse transform maps destination pixels to source pixels.
     */
    public WarpAdapter(final CharSequence name, final MathTransform2D inverse) {
        this.name    = name;
        this.inverse = inverse;
    }    

    /**
     * Returns the transform from image's destination pixels to source pixels.
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
                destRect[index++] = x + 0.5f;
                destRect[index++] = y + 0.5f;
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
        while (--index >= 0) {
            destRect[index] -= 0.5f;
        }
        return destRect;
    }

    /**
     * Computes the source point corresponding to the supplied point.
     *
     * @param destPt The position in destination image coordinates
     *               to map to source image coordinates.
     */
    public Point2D mapDestPoint(final Point2D destPt) {
        Point2D result = new Point2D.Double(destPt.getX()+0.5, destPt.getY()+0.5);
        try {
            result = inverse.transform(result, result);
        } catch (TransformException exception) {
            IllegalArgumentException e = new IllegalArgumentException(Resources.format(
                            ResourceKeys.ERROR_BAD_PARAMETER_$2, "destPt", destPt));
            e.initCause(exception);
            throw e;
        }
        result.setLocation(result.getX()-0.5, result.getY()-0.5);
        return result;
    }

    /**
     * Computes the destination point corresponding to the supplied point.
     *
     * @param sourcePt The position in source image coordinates
     *                 to map to destination image coordinates.
     */
    public Point2D mapSourcePoint(final Point2D sourcePt) {
        Point2D result = new Point2D.Double(sourcePt.getX()+0.5, sourcePt.getY()+0.5);
        try {
            result = ((MathTransform2D)inverse.inverse()).transform(result, result);
        } catch (TransformException exception) {
            IllegalArgumentException e = new IllegalArgumentException(Resources.format(
                            ResourceKeys.ERROR_BAD_PARAMETER_$2, "sourcePt", sourcePt));
            e.initCause(exception);
            throw e;
        }
        result.setLocation(result.getX()-0.5, result.getY()-0.5);
        return result;
    }
}
