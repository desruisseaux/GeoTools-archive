/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Management Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
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
package org.geotools.coverage.io;

// Resources
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;


/**
 * Thrown when a metadata is required but can't be found. This error typically occurs
 * when a raster is being read but the file doesn't contains enough information for
 * constructing the raster's coordinate system.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @since 2.2
 */
public class MissingMetadataException extends MetadataException {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -5215286265847774754L;

    /**
     * Constructs an exception with the specified message. This exception is
     * usually raised because no value was defined for the key {@code key}.
     *
     * @param message The message. If {@code null}, a message will be constructed from the alias.
     * @param key     The metadata key which was the cause for this exception, or {@code null} if
     *                none. This is a format neutral key, for example {@link MetadataBuilder#DATUM}.
     * @param alias   The alias used for for the key {@code key}, or {@code null} if none. This is
     *                usually the name used in the external file parsed.
     */
    public MissingMetadataException(final String message,
                                    final MetadataBuilder.Key key,
                                    final String alias)
    {
        super((message!=null) ? message :  Errors.format(
                (alias!=null) ? ErrorKeys.UNDEFINED_PROPERTY_$1 :
                                ErrorKeys.UNDEFINED_PROPERTY, alias), key, alias);
    }
}
