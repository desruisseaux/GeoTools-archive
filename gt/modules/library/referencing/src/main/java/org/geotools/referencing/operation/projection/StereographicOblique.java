/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *
 *   (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *   (C) 2003, Gerald I. Evenden
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
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValueGroup;


/**
 * The USGS oblique/equatorial case of the Stereographic projection. This is similar but
 * <strong>NOT</strong> equal to EPSG code 9809 ({@code "Oblique_Stereographic"} EPSG name).
 * The later is rather implemented by {@link ObliqueStereographic}.
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author André Gosselin
 * @author Martin Desruisseaux
 * @author Rueben Schulz
 *
 * @deprecated Renamed and hidden as {@code StereographicUSGS}.
 */
public class StereographicOblique extends StereographicUSGS {    
    /**
     * Constructs an oblique stereographic projection (USGS equations).
     *
     * @param  parameters The group of parameter values.
     * @throws ParameterNotFoundException if a required parameter was not found.
     *
     * @since 2.4
     */
    protected StereographicOblique(final ParameterValueGroup parameters) 
            throws ParameterNotFoundException
    {
        super(parameters);
    }
}
