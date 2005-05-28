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
import org.opengis.referencing.cs.CartesianCS;
import org.opengis.referencing.cs.SphericalCS;
import org.opengis.referencing.datum.GeodeticDatum;


/**
 * A 3D coordinate reference system with the origin at the approximate centre of mass of the earth.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @deprecated Renamed as {@link DefaultGeocentricCRS}.
 */
public class GeocentricCRS extends DefaultGeocentricCRS {
    /**
     * Constructs a geocentric CRS from a name.
     */
    public GeocentricCRS(final String         name,
                         final GeodeticDatum datum,
                         final CartesianCS      cs)
    {
        super(name, datum, cs);
    }

    /**
     * Constructs a geocentric CRS from a name.
     */
    public GeocentricCRS(final String         name,
                         final GeodeticDatum datum,
                         final SphericalCS      cs)
    {
        super(name, datum, cs);
    }

    /**
     * Constructs a geographic CRS from a set of properties.
     */
    public GeocentricCRS(final Map      properties,
                         final GeodeticDatum datum,
                         final CartesianCS      cs)
    {
        super(properties, datum, cs);
    }

    /**
     * Constructs a geographic CRS from a set of properties.
     */
    public GeocentricCRS(final Map      properties,
                         final GeodeticDatum datum,
                         final SphericalCS      cs)
    {
        super(properties, datum, cs);
    }
}
