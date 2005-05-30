/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Management Committee (PMC)
 * (C) 2005, Institut de Recherche pour le Développement
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
package org.geotools.coverage;

// J2SE dependencies
import java.util.Date;

// OpenGIS dependencies
import org.opengis.coverage.PointOutsideCoverageException;

// Geotools dependencies
import org.geotools.resources.gcs.ResourceKeys;
import org.geotools.resources.gcs.Resources;


/**
 * Thrown when an {@code evaluate(...)} method method is invoked with a point outside coverage.
 * This subclass of {@code PointOutsideCoverage} exception is used when the dimension of the
 * out-of-bounds ordinate is known.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @since 2.1
 */
public class OrdinateOutsideCoverageException extends PointOutsideCoverageException {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -4718948524305632185L;

    /**
     * The dimension of the out-of-bounds ordinate.
     */
    private final int dimension;
    
    /**
     * Creates an exception with the specified message.
     *
     * @param  message The detail message. The detail message is saved for 
     *         later retrieval by the {@link #getMessage()} method.
     * @param  dimension The dimension of the out-of-bounds ordinate.
     */
    public OrdinateOutsideCoverageException(final String message, final int dimension) {
        super(message);
        this.dimension = dimension;
    }

    /**
     * Creates an exception with the specified cause and an automaticaly formatted message. This
     * constructor assumes that the out-of-bounds value was the temporal ordinate (i.e. the date).
     * This condition should be verified before to invoke this constructor. A localized error
     * message including the specified date is then formatted.
     * <br><br>
     * This constructor is for internal use by {@code evaluate(Point2D, Date, ...)} methods in
     * {@link SpatioTemporalCoverage3D}, in order to replace dates as numerical values by a more
     * explicit string. Users can still get the numerical value if they looks at the cause of this
     * exception.
     */
    OrdinateOutsideCoverageException(final OrdinateOutsideCoverageException cause, final Date date)
    {
        super(Resources.format(ResourceKeys.ERROR_DATE_OUTSIDE_COVERAGE_$1, date));
        dimension = cause.dimension;
        initCause(cause);
    }

    /**
     * Returns the dimension of the out-of-bounds ordinate.
     */
    public int getOutOfBoundsDimension() {
        return dimension;
    }
}
