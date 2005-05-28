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
 */
package org.geotools.referencing.wkt;


/**
 * Thrown by {@link Formattable#toWKT} when an object can't be formatted as WKT.
 * A formatting may fails because an object is too complex for the WKT format
 * capability (for example an {@linkplain org.geotools.referencing.crs.DefaultEngineeringCRS
 * engineering CRS} with different unit for each axis), or because only some specific
 * implementations can be formatted as WKT.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class UnformattableObjectException extends UnsupportedOperationException {
    /**
     * Constructs an exception with the specified detail message.
     *
     * @param message The detail message.
     */
    public UnformattableObjectException(final String message) {
        super(message);
    }
}
