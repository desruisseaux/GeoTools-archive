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
import java.util.Map;
import javax.media.jai.PropertySource;

// OpenGIS dependencies
import org.opengis.referencing.crs.CoordinateReferenceSystem;



/**
 * Base class for Geotools implementation of grid coverage.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @deprecated Renamed as {@link AbstractGridCoverage}.
 */
public abstract class GridCoverage extends AbstractGridCoverage {
    /**
     * Constructs a grid coverage using the specified coordinate reference system. If the
     * coordinate reference system is <code>null</code>, then the subclasses must override
     * {@link #getDimension()}.
     *
     * @param name The grid coverage name.
     * @param crs The coordinate reference system. This specifies the coordinate
     *        system used when accessing a coverage or grid coverage with the
     *        <code>evaluate(...)</code> methods.
     * @param source The source for this coverage, or <code>null</code> if none.
     *        Source may be (but is not limited to) a {@link PlanarImage} or an
     *        other <code>AbstractCoverage</code> object.
     * @param properties The set of properties for this coverage, or <code>null</code> if
     *        there is none. "Properties" in <cite>Java Advanced Imaging</cite> is what
     *        OpenGIS calls "Metadata".  There is no <code>getMetadataValue(...)</code>
     *        method in this implementation. Use {@link #getProperty} instead. Keys may
     *        be {@link String} or {@link CaselessStringKey} objects,  while values may
     *        be any {@link Object}.
     */
    protected GridCoverage(final String                   name,
                           final CoordinateReferenceSystem crs,
                           final PropertySource         source,
                           final Map                properties)
    {
        super(name, crs, source, properties);
    }
    
    /**
     * Constructs a new coverage with the same
     * parameters than the specified coverage.
     */
    protected GridCoverage(final GridCoverage coverage) {
        super(coverage);
    }
}
