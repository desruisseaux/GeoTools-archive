/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2004, Institut de Recherche pour le D�veloppement
 *   
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.metadata.sql;

// J2SE dependencies
import java.sql.SQLException;


/**
 * Throws when a metadata method failed. The cause for this exception
 * is typically a {@link SQLException}.
 *
 * @source $URL$
 * @version $Id$
 * @author Toura�vane
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
