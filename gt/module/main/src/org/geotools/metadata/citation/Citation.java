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
package org.geotools.metadata.citation;

// J2SE direct dependencies
import java.util.Locale;
import java.io.Serializable;

// Geotools dependencies
import org.geotools.resources.Utilities;


/**
 * Standardized resource reference.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class Citation implements org.opengis.metadata.citation.Citation, Serializable {
    /**
     * An immutable empty array of strings.
     */
    private static final String[] EMPTY = new String[0];

    /**
     * The title.
     */
    private final String title;

    /**
     * Construct a citation with the specified title.
     */
    public Citation(final String title) {
        this.title = title;
    }

    /**
     * Name by which the cited resource is known.
     *
     * @param  locale The desired locale for the title to be returned, or <code>null</code>
     *         for a title in some default locale (may or may not be the
     *         {@linkplain Locale#getDefault() system default}).
     * @return The citation title in the given locale.
     *         If no name is available in the given locale, then some default locale is used.
     */
    public String getTitle(final Locale locale) {
        return title;
    }

    /**
     * Short name or other language name by which the cited information is known.
     * Example: "DCW" as an alternative title for "Digital Chart of the World.
     *
     * @param  locale The desired locale for the title to be returned, or <code>null</code>
     *         for a title in some default locale (may or may not be the
     *         {@linkplain Locale#getDefault() system default}).
     * @return The citation title in the given locale.
     *         If no name is available in the given locale, then some default locale is used.
     */
    public String[] getAlternateTitles(final Locale locale) {
        return EMPTY;
    }

    /**
     * Compare this citation with the specified object for equality.
     */
    public boolean equals(final Object object) {
        if (object!=null && object.getClass().equals(getClass())) {
            final Citation that = (Citation) object;
            return Utilities.equals(this.title, that.title);
        }
        return false;
    }

    /**
     * Returns a hash code value for this citation.
     */
    public int hashCode() {
        return title.hashCode();
    }

    /**
     * Returns a string representation of this citation.
     */
    public String toString() {
        return title;
    }
}
