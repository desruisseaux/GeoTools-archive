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

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;


/**
 * Base class for all factories. Factories can be grouped in two categories:
 * <BR>
 * <UL>
 *   <LI>{@linkplain AuthorityFactory Authority factories} creates objects from
 *       a compact string defined by an authority.</LI>
 *   <LI>{@linkplain ObjectFactory Object factories} allows applications
 *       to make objects that cannot be created by an authority factory.
 *       This factory is very flexible, whereas the authority factory is
 *       easier to use.</LI>
 * </UL>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class Factory implements org.opengis.referencing.Factory {
    /**
     * Construct a default factory.
     */
    protected Factory() {
    }

    /**
     * Returns the vendor responsible for creating this factory implementation. Many implementations
     * may be available for the same factory interface. The default implementation returns
     * {@linkplain org.geotools.metadata.citation.Citation#GEOTOOLS Geotools}.
     *
     * @return The vendor for this factory implementation.
     */
    public Citation getVendor() {
        return org.geotools.metadata.citation.Citation.GEOTOOLS;
    }
}
