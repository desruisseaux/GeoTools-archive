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
package org.geotools.metadata.sql;

// J2SE dependencies
import java.sql.SQLException;


/**
 * Throws when a metadata method failed. The cause for this exception
 * is typically a {@link SQLException}.
 *
 * @version $Id$
 * @author Touraïvane
 * @author Martin Desruisseaux
 *
 * @since 2.1
 */
public class MetadataException extends RuntimeException {
    /**
     * Constructs an instance of {@code MetadataException} with the specified
     * detail message.
     *
     * @param message The detail message.
     */
    public MetadataException(final String message) {
        super(message);
    }

    /**
     * Constructs an instance of {@code MetadataException} with the specified cause.
     *
     * @param message The detail message.
     * @param cause The cause of this exception.
     */
    public MetadataException(final String message, final Exception cause) {
        super(message, cause);
    }
}
