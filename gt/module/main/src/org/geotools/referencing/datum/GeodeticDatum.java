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
package org.geotools.referencing.datum;

import java.util.Map;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.datum.PrimeMeridian;



/**
 * Defines the location and precise orientation in 3-dimensional space of a defined ellipsoid
 * (or sphere) that approximates the shape of the earth.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @deprecated Renamed as {@link DefaultGeodeticDatum}.
 */
public class GeodeticDatum extends DefaultGeodeticDatum {
    /**
     * Constructs a geodetic datum from a name.
     */
    public GeodeticDatum(final String        name,
                         final Ellipsoid     ellipsoid,
                         final PrimeMeridian primeMeridian)
    {
        super(name, ellipsoid, primeMeridian);
    }

    /**
     * Constructs a geodetic datum from a set of properties.
     */
    public GeodeticDatum(final Map           properties,
                         final Ellipsoid     ellipsoid,
                         final PrimeMeridian primeMeridian)
    {
        super(properties, ellipsoid, primeMeridian);
    }
}
