/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Management Committee (PMC)
 * (C) 2004, Institut de Recherche pour le Développement
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
package org.geotools.coverage.grid;

// J2SE dependencies
import java.awt.geom.Point2D;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.util.Map;

// JAI dependencies
import javax.media.jai.PlanarImage;
import javax.media.jai.PropertySource;
import javax.media.jai.util.CaselessStringKey;

// OpenGIS dependencies
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.DirectPosition;

// Geotools dependencies
import org.geotools.coverage.AbstractCoverage;
import org.geotools.geometry.DirectPosition2D;


/**
 * Base class for Geotools implementation of grid coverage.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class AbstractGridCoverage extends AbstractCoverage implements GridCoverage {
    /**
     * Constructs a grid coverage using the specified coordinate reference system. If the
     * coordinate reference system is {@code null}, then the subclasses must override
     * {@link #getDimension()}.
     *
     * @param name The grid coverage name.
     * @param crs The coordinate reference system. This specifies the coordinate
     *        system used when accessing a coverage or grid coverage with the
     *        {@code evaluate(...)} methods.
     * @param source The source for this coverage, or {@code null} if none.
     *        Source may be (but is not limited to) a {@link PlanarImage} or an
     *        other {@code AbstractGridCoverage} object.
     * @param properties The set of properties for this coverage, or {@code null} if
     *        there is none. "Properties" in <cite>Java Advanced Imaging</cite> is what
     *        OpenGIS calls "Metadata". Keys may be {@link String} or {@link CaselessStringKey}
     *        objects, while values may be any {@link Object}.
     */
    protected AbstractGridCoverage(final String                   name,
                                   final CoordinateReferenceSystem crs,
                                   final PropertySource         source,
                                   final Map                properties)
    {
        super(name, crs, source, properties);
    }

    /**
     * Constructs a new coverage with the same parameters than the specified coverage.
     */
    protected AbstractGridCoverage(final AbstractGridCoverage coverage) {
        super(coverage);
    }

    /**
     * Construct a string for the specified point.
     * This is used for formatting error messages.
     *
     * @param  point The coordinate point to format.
     * @return The coordinate point as a string, without '(' or ')' characters.
     */
    static String toString(final Point2D point) {
        return toString((DirectPosition) new DirectPosition2D(point));
    }

    /**
     * Construct a string for the specified point.
     * This is used for formatting error messages.
     *
     * @param  point The coordinate point to format.
     * @return The coordinate point as a string, without '(' or ')' characters.
     */
    static String toString(final DirectPosition point) {
        final StringBuffer buffer = new StringBuffer();
        final FieldPosition dummy = new FieldPosition(0);
        final NumberFormat format = NumberFormat.getNumberInstance();
        final int       dimension = point.getDimension();
        for (int i=0; i<dimension; i++) {
            if (i != 0) {
                buffer.append(", ");
            }
            format.format(point.getOrdinate(i), buffer, dummy);
        }
        return buffer.toString();
    }
}
