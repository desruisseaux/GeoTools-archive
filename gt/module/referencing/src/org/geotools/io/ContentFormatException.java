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
package org.geotools.io;

// J2SE dependencies
import java.io.IOException;


/**
 * Throws when a stream can't be parsed because some content use an invalid format.
 * This exception typically has a {@link java.text.ParseException} has its cause.
 * It is similar in spirit to {@link java.util.InvalidPropertiesFormatException}.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @since 2.2
 *
 * @see java.util.InvalidPropertiesFormatException
 */
public class ContentFormatException extends IOException {
    /**
     * Serial version for compatibility with different versions.
     */
    private static final long serialVersionUID = 6152194019351374599L;

    /**
     * Constructs a new exception with the specified detail message.
     * The detail message is saved for later retrieval by the {@link #getMessage()} method.
     */
    public ContentFormatException(final String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     * The cause is saved for later retrieval by the {@link #getCause()} method.
     */
    public ContentFormatException(final String message, final Throwable cause) {
        super(message);
        initCause(cause);
    }
}
