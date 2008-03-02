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
     * Creates an initially empty rectangle. Every fields are zero, including subsamplings.
     */
    public SubsampledRectangle() {
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
}
