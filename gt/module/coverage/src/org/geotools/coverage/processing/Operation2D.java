/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2001, Institut de Recherche pour le Développement
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
 * @source $URL$
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
     * Returns the factory to use for creating new {@link GridCoverage2D} objects.
     *
     * @since 2.2
     */
    protected static GridCoverageFactory getFactory(final Hints hints) {
        return FactoryFinder.getGridCoverageFactory(hints);
    }
}
