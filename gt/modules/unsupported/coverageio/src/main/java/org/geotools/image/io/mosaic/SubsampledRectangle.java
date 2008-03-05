/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, GeoTools Project Managment Committee (PMC)
 *    (C) 2007, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.image.io.mosaic;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Collection;


/**
 * A rectangle with subsampling information. To be used as key in hash map.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
class SubsampledRectangle extends Rectangle {
    /**
     * For cross-version compatibility during serialization.
     */
    private static final long serialVersionUID = 7455133098267619711L;

    /**
     * The subsamplings. Valid values are greater than zero.
     */
    protected int xSubsampling, ySubsampling;

    /**
     * Creates an initially empty rectangle. Width and height are set to -1, which
     * stands for non-existant rectangle according {@link Rectangle} documentation.
     * Other fields (including subsamplings) are set to 0.
     */
    public SubsampledRectangle() {
        super(-1, -1);
    }

    /**
     * Creates a rectangle initialized to the specified bounds.
     * The subsampling is left undefined.
     */
    public SubsampledRectangle(final Rectangle bounds) {
        super(bounds);
    }

    /**
     * Creates a rectangle initialized to the specified bounds and subsampling.
     */
    public SubsampledRectangle(final Rectangle bounds, final Dimension subsampling) {
        super(bounds);
        xSubsampling = subsampling.width;
        ySubsampling = subsampling.height;
    }

    /**
     * Returns the subsamplings
     */
    public final Dimension getSubsampling() {
        return new Dimension(xSubsampling, ySubsampling);
    }

    /**
     * Sets the subsampling to the specified value.
     */
    public final void setSubsampling(final Dimension subsampling) {
        xSubsampling = subsampling.width;
        ySubsampling = subsampling.height;
    }

    /**
     * Tests if the bounds are equals to the specified rectangle, not taking in account
     * subsampling or any other information that may be contained in subclasses.
     */
    final boolean boundsEquals(final Rectangle other) {
        return super.equals(other);
    }

    /**
     * Compares the specified object to this rectangle for equality. Note that we
     * do <strong>not</strong> override {@link #hashCode()} for consistency with
     * {@link Rectangle#equals} implementation, which ignore subsampling if given
     * a {@code SubsampledRectangle} instance.
     */
    @Override
    public boolean equals(final Object object) {
        if (!super.equals(object)) {
            return false;
        }
        if (!(object instanceof SubsampledRectangle)) {
            return true; // For consistenct with Rectangle.equals(...) implementation.
        }
        final SubsampledRectangle that = (SubsampledRectangle) object;
        return this.xSubsampling == that.xSubsampling &&
               this.ySubsampling == that.ySubsampling;
    }

    /**
     * Returns the largest horizontal or vertical distance between this rectangle and the specified
     * one. Returns a negative number if the rectangles overlap. Diagonals are <strong>not</strong>
     * computed.
     * <p>
     * This method is not robust to integer arithmetic overflow. In such case, an
     * {@link AssertionError} is likely to be thrown if assertions are enabled.
     */
    public final int distance(final Rectangle rect) {
        int dx = rect.x - x;
        if (dx >= 0) {
            dx -= width;
        } else {
            dx += rect.width;
            dx = -dx;
        }
        int dy = rect.y - y;
        if (dy >= 0) {
            dy -= height;
        } else {
            dy += rect.height;
            dy = -dy;
        }
        final int distance = Math.max(dx, dy);
        assert (super.intersects(rect) ? (dx < 0 && dy < 0) : (distance >= 0)) : distance;
        return distance;
    }

    /**
     * Returns {@code true} if the rectangles in the given collection fill completly the given
     * ROI with no empty space.
     *
     * @todo This method is not yet correctly implemented. For now we performs a naive check
     *       which is suffisient for common {@link TileLayout}. We may need to revisit this
     *       method in a future version.
     */
    static boolean dense(final Rectangle roi, final Collection<? extends Rectangle> regions) {
        Rectangle bounds = null;
        for (final Rectangle rect : regions) {
            final Rectangle inter = roi.intersection(rect);
            if (bounds == null) {
                bounds = inter;
            } else {
                bounds.add(inter); // See java.awt.Rectangle javadoc for empty rectangle handling.
            }
        }
        return bounds == null || bounds.equals(roi);
    }
}
