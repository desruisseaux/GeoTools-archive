/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
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
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.referencing.cs;

import java.util.Map;
import org.opengis.referencing.cs.CoordinateSystemAxis;


/**
 * A three-dimensional coordinate system with one distance measured from the origin and two angular
 * coordinates.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @deprecated Renamed as {@link DefaultSphericalCS}.
 */
public class SphericalCS extends DefaultSphericalCS {
    /**
     * Constructs a three-dimensional coordinate system from a name.
     */
    public SphericalCS(final String               name,
                       final CoordinateSystemAxis axis0,
                       final CoordinateSystemAxis axis1,
                       final CoordinateSystemAxis axis2)
    {
        super(name, axis0, axis1, axis2);
    }

    /**
     * Constructs a three-dimensional coordinate system from a set of properties.
     */
    public SphericalCS(final Map             properties,
                       final CoordinateSystemAxis axis0,
                       final CoordinateSystemAxis axis1,
                       final CoordinateSystemAxis axis2)
    {
        super(properties, axis0, axis1, axis2);
    }
}
