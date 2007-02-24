/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *
 *   (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *   (C) 2001, Institut de Recherche pour le Développement
 *   (C) 2000, Frank Warmerdam
 *   (C) 1999, Fisheries and Oceans Canada
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
 *    This package contains formulas from the PROJ package of USGS.
 *    USGS's work is fully acknowledged here. This derived work has
 *    been relicensed under LGPL with Frank Warmerdam's permission.
 */
package org.geotools.referencing.operation.projection;

// OpenGIS dependencies
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValueGroup;


/**
 * The polar case of the {@linkplain Stereographic stereographic} projection.
 * This default implementation uses USGS equation (i.e. iteration) for computing
 * the {@linkplain #inverseTransformNormalized inverse transform}.
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author André Gosselin
 * @author Martin Desruisseaux
 * @author Rueben Schulz
 *
 * @deprecated Renamed as {@link PolarStereographic} for consistency with OGC and EPSG names.
 */
public class StereographicPolar extends PolarStereographic {
    /**
     * Constructs a polar stereographic projection.
     *
     * @param  parameters The group of parameter values.
     * @param  descriptor The expected parameter descriptor.
     * @param  forceSouthPole For projection to North pole if {@link Boolean#FALSE},
     *         to South pole if {@link Boolean#TRUE}, or do not force (i.e. detect
     *         from other parameter values) if {@code null}.
     * @throws ParameterNotFoundException if a required parameter was not found.
     */
    StereographicPolar(final ParameterValueGroup      parameters,
                       final ParameterDescriptorGroup descriptor,
                       final Boolean                  forceSouthPole)
            throws ParameterNotFoundException
    {
        super(parameters, descriptor, forceSouthPole);
    }
}
