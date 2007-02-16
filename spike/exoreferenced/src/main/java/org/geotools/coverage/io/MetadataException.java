/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2001, Institut de Recherche pour le D�veloppement
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
 */
package org.geotools.coverage.io;

// J2SE dependencies
import javax.imageio.IIOException;


/**
 * The base class for error related to grid coverage's metadata.
 * This exception is thrown by the helper class {@link MetadataBuilder}.
 *
 * @source $URL: http://svn.geotools.org/geotools/trunk/gt/modules/library/coverage/src/main/java/org/geotools/coverage/io/MetadataException.java $
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @since 2.2
 */
public class MetadataException extends IIOException {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -3146379152278866037L;

    /**
     * The key for the faulty metadata, or {@code null} if none.
     */
    private final MetadataBuilder.Key key;

    /**
     * The alias used for the metadata, or {@code null} if none.
     */
    private final String alias;

    /**
     * Constructs an exception with the specified message and no key.
     *
     * @param message The message, or {@code null} if none.
     */
    public MetadataException(final String message) {
        super(message);
        key   = null;
        alias = null;
    }

    /**
     * Constructs an exception with the specified message and exception as its cause.
     *
     * @param message The message, or {@code null} if none.
     * @param cause   The cause for this exception.
     */
    public MetadataException(final String message, final Throwable cause) {
        super(message, cause);
        key   = null;
        alias = null;
    }

    /**
     * Constructs an exception with the specified message. This exception is usually raised because
     * no value was defined for the key {@code key}, or the value was ambiguous.
     *
     * @param message The message, or {@code null} if none.
     * @param key     The metadata key which was the cause for this exception, or {@code null} if
     *                none. This is a format neutral key, for example {@link MetadataBuilder#DATUM}.
     * @param alias   The alias used for for the key {@code key}, or {@code null} if none. This is
     *                usually the name used in the external file parsed.
     */
    public MetadataException(final String          message,
                             final MetadataBuilder.Key key,
                             final String            alias)
    {
        super(message);
        this.key   = key;
        this.alias = alias;
    }

    /**
     * Constructs an exception from the specified cause.
     *
     * @param cause   The cause for this exception.
     * @param key     The metadata key which was the cause for this exception, or {@code null} if
     *                none. This is a format neutral key, for example {@link MetadataBuilder#DATUM}.
     * @param alias   The alias used for for the key {@code key}, or {@code null} if none. This is
     *                usually the name used in the external file parsed.
     */
    public MetadataException(final Exception         cause,
                             final MetadataBuilder.Key key,
                             final String            alias)
    {
        super(cause.getLocalizedMessage(), cause);
        this.key   = key;
        this.alias = alias;
    }

    /**
     * Returns the metadata key which has raised this exception. This exception has usually
     * been raised because no value was defined for this key, or the value was ambiguous.
     *
     * @return The metadata key, or {@code null} if none.
     */
    public MetadataBuilder.Key getMetadataKey() {
        return key;
    }

    /**
     * Returns the alias used for the key {@link #getMetadataKey}. This is usually the name
     * used in the external file to be parsed. The alias is format-dependent, while the key
     * (as returned by {@link #getMetadataKey}) if format neutral.
     *
     * @return The alias, or {@code null} if none.
     */
    public String getMetadataAlias() {
        return alias;
    }

    /**
     * Returns a string representation of this exception. This implementation is similar to
     * {@link Throwable#toString()}, except that the string will includes key and alias names
     * if they are defined. The localized message, if any, may be written on the next line.
     * Example:
     *
     * <blockquote><pre>
     * org.geotools.coverage.io.MissingMetadataException(key="YMaximum", alias="ULY"):
     * Aucune valeur n'est d�finie pour la propri�t� "ULY".
     * </pre></blockquote>
     */
    public String toString() {
        final MetadataBuilder.Key key = getMetadataKey();
        final String alias = getMetadataAlias();
        if (key==null && alias==null) {
            return super.toString();
        }
        final StringBuffer buffer = new StringBuffer(getClass().getName());
        buffer.append('[');
        if (key != null) {
            buffer.append("key=\"");
            buffer.append(key);
            buffer.append('"');
            if (alias != null) {
                buffer.append(", ");
            }
        }
        if (alias != null) {
            buffer.append("alias=\"");
            buffer.append(alias);
            buffer.append('"');
        }
        buffer.append(']');
        final String message = getLocalizedMessage();
        if (message != null) {
            buffer.append(':');
            buffer.append(System.getProperty("line.separator", "\n"));
            buffer.append(message);
        }
        return buffer.toString();
    }
}
