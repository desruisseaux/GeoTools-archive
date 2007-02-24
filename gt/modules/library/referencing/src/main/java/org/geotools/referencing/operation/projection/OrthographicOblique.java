/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *
 *   (C) 2004-2006, Geotools Project Managment Committee (PMC)
 *   (C) 2000, Frank Warmerdam
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
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValueGroup;


/**
 * The oblique case of the {@link Orthographic} projection. Only the spherical
 * form is given here.
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Rueben Schulz
 *
 * @deprecated Renamed as {@link ObliqueOrthographic} for consistency with OGC and EPSG naming.
 */
public class OrthographicOblique extends ObliqueOrthographic {
    /**
     * Constructs an oblique orthographic projection.
     *
     * @param  parameters The parameter values in standard units.
     * @throws ParameterNotFoundException if a mandatory parameter is missing.
     *
     * @since 2.4
     */
    protected OrthographicOblique(final ParameterValueGroup parameters) 
            throws ParameterNotFoundException
    {
        super(parameters);
    }
}
