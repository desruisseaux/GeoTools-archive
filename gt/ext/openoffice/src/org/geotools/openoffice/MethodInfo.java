/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Managment Committee (PMC)
 * (C) 2005, Institut de Recherche pour le Développement
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
 */
package org.geotools.openoffice;


/**
 * Information about a method to be exported as <A HREF="http://www.openoffice.org">OpenOffice</A>
 * add-in.
 *
 * @since 2.2
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class MethodInfo {
    /** The category name. */
    public final String category;

    /** The display name. */
    public final String display;

    /** A description of the exported method. */
    public final String description;

    /** Arguments names (even index) and descriptions (odd index). */
    public final String[] arguments;

    /** Constructs method informations. */
    public MethodInfo(final String   category,
                      final String   display,
                      final String   description,
                      final String[] arguments)
    {
        this.category    = category;
        this.display     = display;
        this.description = description;
        this.arguments   = arguments;
    }
}
