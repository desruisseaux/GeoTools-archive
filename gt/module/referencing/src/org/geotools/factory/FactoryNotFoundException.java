/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Managment Committee (PMC)
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
package org.geotools.factory;


/**
 * Thrown when a factory can't be found in the registery.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class FactoryNotFoundException extends FactoryRegistryException {
    /**
     * Creates a new exception with the specified detail message.
     */
    public FactoryNotFoundException(final String message) {
        super(message);
    }

    /**
     * Creates a new exception with the specified detail message and cause.
     */
    public FactoryNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
