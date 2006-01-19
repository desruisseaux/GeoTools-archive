/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2002, Institut de Recherche pour le Développement
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

// JAI dependencies
import javax.media.jai.Interpolation;

// OpenGIS dependencies
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

// Geotools dependencies
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.processing.Operations;
import org.geotools.coverage.processing.GridCoverageProcessor2D;
import org.geotools.coverage.processing.operation.Resample;
import org.geotools.factory.Hints;


/**
 * Resample a grid coverage using a different grid geometry.
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @deprecated No public replacement.
 */
public final class Resampler2D {
    private Resampler2D() {
    }
    
    /**
     * Creates a new coverage with a different coordinate reference reference system.
     *
     * @param  sourceCoverage The source grid coverage.
     * @param  targetCRS Coordinate reference system for the new grid coverage, or {@code null}.
     * @param  targetGG The target grid geometry, or {@code null} for default.
     * @param  interpolation The interpolation to use.
     * @param  hints The rendering hints. This is usually provided by {@link GridCoverageProcessor2D}.
     *         This method will looks for {@link Hints#COORDINATE_OPERATION_FACTORY}
     *         and {@link Hints#JAI_INSTANCE} keys.
     * @return The new grid coverage, or {@code sourceCoverage} if no resampling was needed.
     * @throws FactoryException is a transformation step can't be created.
     * @throws TransformException if a transformation failed.
     */
    public static GridCoverage2D reproject(      GridCoverage2D       sourceCoverage,
                                           final CoordinateReferenceSystem targetCRS,
                                                 GridGeometry2D             targetGG,
                                           final Interpolation         interpolation,
                                           final Hints                         hints)
            throws FactoryException, TransformException
    {
        return (GridCoverage2D) new Operations(hints).resample(
                sourceCoverage, targetCRS, targetGG, interpolation);
    }

    /**
     * The "Resample" operation.
     *
     * @since 2.1
     * @version $Id$
     * @author Martin Desruisseaux
     *
     * @deprecated Replaced by {@link Resample}.
     */
    public static final class Operation extends Resample {
        /**
         * Constructs a "Resample" operation.
         */
        public Operation() {
        }
    }
}
