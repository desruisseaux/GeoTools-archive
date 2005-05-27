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
 * A one-dimensional coordinate system that consists of the points that lie on the single axis
 * described.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @deprecated Renamed as {@link DefaultLinearCS}.
 */
public class LinearCS extends DefaultLinearCS {
    /**
     * Constructs a coordinate system from a name.
     */
    public LinearCS(final String name, final CoordinateSystemAxis axis) {
        super(name, axis);
    }

    /**
     * Constructs a coordinate system from a set of properties.
     */
    public LinearCS(final Map properties, final CoordinateSystemAxis axis) {
        super(properties, axis);
    }
}
