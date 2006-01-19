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
 *
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.coverage.operation;

// JAI dependencies
import javax.media.jai.Interpolation;
import javax.media.jai.InterpolationNearest;

// Geotools dependencies
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.processing.operation.Interpolate;


/**
 * A grid coverage using an {@linkplain Interpolation interpolation} for evaluating points. This
 * interpolator is not used for {@linkplain InterpolationNearest nearest-neighbor interpolation}
 * (use the plain {@link GridCoverage2D} class for that). It should work for other kinds of
 * interpolation however.
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @deprecated Moved as {@link org.geotools.coverage.grid.Interpolator2D}.
 */
public final class Interpolator2D {
    private Interpolator2D() {
    }

    /**
     * Constructs a new interpolator using default interpolations.
     *
     * @param  coverage The coverage to interpolate.
     */
    public static GridCoverage2D create(final GridCoverage2D coverage) {
        return org.geotools.coverage.grid.Interpolator2D.create(coverage);
    }
    
    /**
     * Constructs a new interpolator for a single interpolation.
     *
     * @param  coverage The coverage to interpolate.
     * @param  interpolation The interpolation to use.
     */
    public static GridCoverage2D create(final GridCoverage2D coverage,
                                        final Interpolation interpolation)
    {
        return org.geotools.coverage.grid.Interpolator2D.create(coverage, interpolation);
    }

    /**
     * Constructs a new interpolator for an interpolation and its fallbacks. The fallbacks
     * are used if the primary interpolation failed because of {@linkplain Float#NaN NaN}
     * values in the interpolated point neighbor.
     *
     * @param  coverage The coverage to interpolate.
     * @param  interpolations The interpolation to use and its fallback (if any).
     */
    public static GridCoverage2D create(GridCoverage2D coverage, final Interpolation[] interpolations) {
        return org.geotools.coverage.grid.Interpolator2D.create(coverage, interpolations);
    }

    /**
     * The "Interpolate" operation. This operation specifies the interpolation type
     * to be used to interpolate values for points which fall between grid cells.
     * The default value is nearest neighbor. The new interpolation type operates
     * on all sample dimensions. See package description for more details.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     *
     * @deprecated Replaced by {@link Interpolate}.
     */
    public static final class Operation extends Interpolate {
        /**
         * Constructs an "Interpolate" operation.
         */
        public Operation() {
        }
    }
}
