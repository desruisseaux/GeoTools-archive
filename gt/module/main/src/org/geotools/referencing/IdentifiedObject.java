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
package org.geotools.referencing;

// J2SE dependencies
import java.util.Map;


/**
 * A base class for metadata applicable to reference system objects.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @deprecated Renamed as {@link DefaultIdentifiedObject}.
 */
public class IdentifiedObject extends DefaultIdentifiedObject {
    /**
     * Constructs a new identified object with the same values than the specified one.
     */
    public IdentifiedObject(final org.opengis.referencing.IdentifiedObject object) {
        super(object);
    }

    /**
     * Constructs an object from a set of properties.
     */
    public IdentifiedObject(final Map properties) {
        super(properties);
    }

    /**
     * Constructs an object from a set of properties.
     */
    protected IdentifiedObject(final Map properties,
                               final Map subProperties,
                               final String[] localizables)
            throws IllegalArgumentException
    {
        super(properties, subProperties, localizables);
    }
}
