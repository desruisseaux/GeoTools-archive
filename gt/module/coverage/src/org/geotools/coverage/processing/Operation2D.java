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
package org.geotools.coverage.processing;

// J2SE dependencies
import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.Map;

// OpenGIS dependencies
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.referencing.IdentifiedObject;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.coverage.FactoryFinder;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.parameter.DefaultParameterDescriptor;
import org.geotools.referencing.NamedIdentifier;


/**
 * An operation working on {@link GridCoverage2D} sources.
 *
 * @since 2.1
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class Operation2D extends AbstractOperation {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 574096338873406394L;

    /**
     * Convenience constant for the first source {@link GridCoverage2D}. The parameter name
     * is {@code "Source"} (as specified in OGC implementation specification) and the alias
     * is {@code "source0"} (for compatibility with <cite>Java Advanced Imaging</cite>).
     */
    public static final ParameterDescriptor SOURCE_0;
    static {
        final Map properties = new HashMap(4);
        properties.put(IdentifiedObject.NAME_KEY,  new NamedIdentifier(Citations.OGC, "Source"));
        properties.put(IdentifiedObject.ALIAS_KEY, new NamedIdentifier(Citations.JAI, "source0"));
        SOURCE_0 = new DefaultParameterDescriptor(properties, GridCoverage2D.class,
                        null, null, null, null, null, true);
    }

    /**
     * Constructs an operation. The operation name will be the same than the
     * parameter descriptor name.
     *
     * @param descriptor The parameters descriptor.
     */
    public Operation2D(final ParameterDescriptorGroup descriptor) {
        super(descriptor);
    }

    /**
     * Returns the {@link GridCoverageProcessor2D} instance used for an operation.
     * The instance is fetch from the rendering hints given to the {@link #doOperation} method.
     *
     * @param  hints The rendering hints, or {@code null} if none.
     * @return The {@code GridCoverageProcessor2D} instance in use (never {@code null}).
     *
     * @deprecated Replaced by {@link #getProcessor}.
     */
    protected static GridCoverageProcessor2D getGridCoverageProcessor(final RenderingHints hints) {
        if (hints != null) {
            final Object value = hints.get(Hints.GRID_COVERAGE_PROCESSOR);
            if (value instanceof GridCoverageProcessor2D) {
                return (GridCoverageProcessor2D) value;
            }
        }
        return GridCoverageProcessor2D.getDefault();
    }

    /**
     * Returns the factory to use for creating new {@link GridCoverage2D} objects.
     *
     * @since 2.2
     */
    protected static GridCoverageFactory getFactory(final Hints hints) {
        return FactoryFinder.getGridCoverageFactory(hints);
    }
}
