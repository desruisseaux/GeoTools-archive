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

// J2SE dependencies
import java.util.Map;

// OpenGIS dependencies
import org.opengis.referencing.cs.CoordinateSystemAxis;


/**
 * A two- or three-dimensional coordinate system that consists of any combination of coordinate
 * axes not covered by any other Coordinate System type.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @deprecated Renamed as {@link DefaultUserDefinedCS}.
 */
public class UserDefinedCS extends DefaultUserDefinedCS {
    /**
     * Constructs a two-dimensional coordinate system from a name.
     */
    public UserDefinedCS(final String               name,
                         final CoordinateSystemAxis axis0,
                         final CoordinateSystemAxis axis1)
    {
        super(name, axis0, axis1);
    }

    /**
     * Constructs a three-dimensional coordinate system from a name.
     */
    public UserDefinedCS(final String               name,
                         final CoordinateSystemAxis axis0,
                         final CoordinateSystemAxis axis1,
                         final CoordinateSystemAxis axis2)
    {
        super(name, axis0, axis1, axis2);
    }

    /**
     * Constructs a two-dimensional coordinate system from a set of properties.
     */
    public UserDefinedCS(final Map             properties,
                         final CoordinateSystemAxis axis0,
                         final CoordinateSystemAxis axis1)
    {
        super(properties, axis0, axis1);
    }

    /**
     * Constructs a three-dimensional coordinate system from a set of properties.
     */
    public UserDefinedCS(final Map             properties,
                         final CoordinateSystemAxis axis0,
                         final CoordinateSystemAxis axis1,
                         final CoordinateSystemAxis axis2)
    {
        super(properties, axis0, axis1, axis2);
    }
}
