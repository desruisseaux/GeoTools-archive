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
package org.geotools.util;

// Geotools implementation
import org.geotools.resources.Utilities;


/**
 * Throws when an operation can't use arbitrary implementation of an interface, and
 * a given instance doesn't meet the requirement. For example this exception may be
 * thrown when an operation requires a Geotools implementation of a
 * <A HREF="http://geoapi.sourceforge.net">GeoAPI</A> interface.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @since 2.0
 */
public class UnsupportedImplementationException extends UnsupportedOperationException {
    /**
     * Constructs an exception with the specified detail message.
     *
     * @param message The detail message.
     */
    public UnsupportedImplementationException(final String message) {
        super(message);
    }

    /**
     * Constructs an exception with an error message formatted for the specified class.
     *
     * @param classe The unexpected implementation class.
     */
    public UnsupportedImplementationException(final Class classe) {
        // TODO: Provides a localized message.
        super(Utilities.getShortName(classe));
    }

    /**
     * Constructs an exception with an error message formatted for the specified class
     * and a cause.
     *
     * @param classe The unexpected implementation class.
     * @param cause The cause for the exception.
     */
    public UnsupportedImplementationException(final Class classe, final Exception cause) {
        // TODO: Provides a localized message.
        super(Utilities.getShortName(classe));
        initCause(cause); // TODO: use the constructor with cause arg. in J2E 1.5.
    }
}
