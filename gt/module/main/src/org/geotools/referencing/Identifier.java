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
package org.geotools.referencing;

// J2SE dependencies
import java.util.Map;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.util.InternationalString;



/**
 * An identification of a CRS object.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @deprecated Renamed as {@link NamedIdentifier}.
 */
public class Identifier extends NamedIdentifier {
    /**
     * Constructs an identifier from a set of properties.
     */
    public Identifier(final Map properties) throws IllegalArgumentException {
        super(properties);
    }

    /**
     * Constructs an identifier from an authority and code informations.
     */
    public Identifier(final Citation authority, final InternationalString code) {
        super(authority, code);
    }

    /**
     * Constructs an identifier from an authority and code informations.
     */
    public Identifier(final Citation authority, final String code) {
        super(authority, code);
    }

    /**
     * Constructs an identifier from an authority and code informations.
     */
    public Identifier(final Citation authority, final String code, final String version) {
        super(authority, code, version);
    }
}
