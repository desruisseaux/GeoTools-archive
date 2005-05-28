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
package org.geotools.referencing.crs;

import java.util.Map;

// OpenGIS dependencies
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.datum.EngineeringDatum;

 
/**
 * A contextually local coordinate reference system.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @deprecated Renamed as {@link DefaultEngineeringCRS}.
 */
public class EngineeringCRS extends DefaultEngineeringCRS {
    /**
     * Constructs an engineering CRS from a name.
     */
    public EngineeringCRS(final String            name,
                          final EngineeringDatum datum,
                          final CoordinateSystem    cs)
    {
        super(name, datum, cs);
    }

    /**
     * Constructs an engineering CRS from a set of properties.
     */
    public EngineeringCRS(final Map         properties,
                          final EngineeringDatum datum,
                          final CoordinateSystem    cs)
    {
        super(properties, datum, cs);
    }
}
