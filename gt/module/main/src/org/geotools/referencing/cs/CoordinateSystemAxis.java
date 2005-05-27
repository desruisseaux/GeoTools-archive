/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.referencing.cs;

import java.util.Map;
import javax.units.Unit;

// OpenGIS dependencies
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.util.InternationalString;


/**
 * Definition of a coordinate system axis.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @deprecated Renamed as {@link DefaultCoordinateSystemAxis}.
 */
public class CoordinateSystemAxis extends DefaultCoordinateSystemAxis {
    /**
     * Constructs an axis from a set of properties.
     */
    public CoordinateSystemAxis(final Map           properties,
                                final String        abbreviation,
                                final AxisDirection direction,
                                final Unit          unit)
    {
        super(properties, abbreviation, direction, unit);
    }

    /**
     * Constructs an axis with the same name as the abbreviation.
     */
    public CoordinateSystemAxis(final String        abbreviation,
                                final AxisDirection direction,
                                final Unit          unit)
    {
        super(abbreviation, direction, unit);
    }

    /**
     * Constructs an axis with a name as an international string and an abbreviation.
     */    
    public CoordinateSystemAxis(final InternationalString name,
                                final String        abbreviation,
                                final AxisDirection direction,
                                final Unit          unit)
    {
        super(name, abbreviation, direction, unit);
    }
}
