/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
 * (C) 2004, Institut de Recherche pour le D�veloppement
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
package org.geotools.util;

// J2SE dependencies
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.geotools.resources.Utilities;
import org.opengis.util.InternationalString;


/**
 * Base class for {@linkplain ScopedName generic scoped} and
 * {@linkplain LocalName local name} structure for type and attribute
 * name in the context of name spaces.
 *
 * <P>Note: this class has a natural ordering that is inconsistent with equals.
 *    The natural ordering may be case-insensitive and ignore the character
 *    separator between name elements.</P>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see NameFactory
 */
public abstract class GenericName implements org.opengis.util.GenericName, Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 8685047583179337259L;

    /**
     * The default separator character.
     */
    public static final char DEFAULT_SEPARATOR = ':';

    /**
     * Creates a new instance of generic name.
     */
    protected GenericName() {
    }

    /**
     * Ensures that the given name is a {@link String} or an {@link InternationalString}.
     * This is used for subclass constructors.
     */
    static CharSequence validate(final CharSequence name) {
        return (name==null || name instanceof InternationalString) ? name : name.toString();
    }
    
    /**
     * Returns the scope (name space) of this generic name. If this name has no scope
     * (e.g. is the root), then this method returns <code>null</code>.
     */
    public abstract org.opengis.util.GenericName getScope();
    
    /**
     * Returns the sequence of {@linkplain LocalName local names} making this generic name.
     * Each element in this list is like a directory name in a file path name.
     * The length of this sequence is the generic name depth.
     */
    public abstract List getParsedNames();

    /**
     * Returns the separator character. Default to <code>':'</code>.
     * This method is overriden by {@link org.geotools.util.ScopedName}.
     */
    char getSeparator() {
        return DEFAULT_SEPARATOR;
    }

    /**
     * Returns a string representation of this generic name. This string representation
     * is local-independant. It contains all elements listed by {@link #getParsedNames}
     * separated by an arbitrary character (usually <code>:</code> or <code>/</code>).
     * This rule implies that the <code>toString()</code> method for a
     * {@linkplain ScopedName scoped name} will contains the scope, while the
     * <code>toString()</code> method for the {@linkplain LocalName local version} of
     * the same name will not contains the scope.
     */
    public String toString() {
        final StringBuffer buffer = new StringBuffer();
        final List    parsedNames = getParsedNames();
        final char      separator = getSeparator();
        for (final Iterator it=parsedNames.iterator(); it.hasNext();) {
            if (buffer.length() != 0) {
                buffer.append(separator);
            }
            buffer.append(it.next());
        }
        return buffer.toString();
    }

    /**
     * Returns a local-dependent string representation of this generic name. This string
     * is similar to the one returned by {@link #toString} except that each element has
     * been localized in the {@linkplain InternationalString#toString(Locale)
     * specified locale}. If no international string is available, then this method should
     * returns an implementation mapping to {@link #toString} for all locales.
     */
    public InternationalString toInternationalString() {
        return new International(getParsedNames(), getSeparator());
    }

    /**
     * An international string built from a snapshot of {@link GenericName}.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    private static final class International extends org.geotools.util.InternationalString
                                          implements Serializable
    {
        /**
         * Serial number for interoperability with different versions.
         */
        private static final long serialVersionUID = -4234089612436334148L;

        /**
         * The sequence of {@linkplain LocalName local names} making this generic name.
         * This is the value returned by {@link GenericName#getParsedNames}.
         */
        private final List parsedNames;

        /**
         * The separator character. This is the value returned by {@link GenericName#getSeparator}.
         */
        private final char separator;

        /**
         * Constructs a new international string from the specified {@link GenericName} fields.
         *
         * @param parsedNames The value returned by {@link GenericName#getParsedNames}.
         * @param separator   The value returned by {@link GenericName#getSeparator}.
         */
        public International(final List parsedNames, final char separator) {
            this.parsedNames = parsedNames;
            this.separator   = separator;
        }

        /**
         * Returns a string representation for the specified locale.
         */
        public String toString(final Locale locale) {
            final StringBuffer buffer = new StringBuffer();
            for (final Iterator it=parsedNames.iterator(); it.hasNext();) {
                if (buffer.length() != 0) {
                    buffer.append(separator);
                }
                buffer.append(((org.opengis.util.GenericName) it.next())  // Remove cast with 1.5
                              .toInternationalString().toString(locale));
            }
            return buffer.toString();
        }

        /**
         * Compares this international string with the specified object for equality.
         */
        public boolean equals(final Object object) {
            if (object!=null && object.getClass().equals(getClass())) {
                final International that = (International) object;
                return Utilities.equals(this.parsedNames, that.parsedNames) &&
                                        this.separator == that.separator;
            }
            return false;
        }

        /**
         * Returns a hash code value for this international text.
         */
        public int hashCode() {
            return (int)serialVersionUID ^ parsedNames.hashCode();
        }
    }

    /**
     * Compares this name with the specified object for order. Returns a negative integer,
     * zero, or a positive integer as this name lexicographically precedes, is equals to,
     * or follows the specified object. The comparaison is performed in the following
     * order:
     * <ul>
     *   <li>Compares each element in the {@linkplain #getParsedNames list of parsed names}. If an
     *       element of this name lexicographically precedes or follows the corresponding element
     *       of the specified name, returns a negative or a positive integer respectively.</li>
     *   <li>If all elements in both names are lexicographically equal, then if this name has less
     *       or more elements than the specified name, returns a negative or a positive integer
     *       respectively.</li>
     *   <li>Otherwise, returns 0.</li>
     * </ul>
     */
    public int compareTo(final Object object) {
        final org.opengis.util.GenericName that = (org.opengis.util.GenericName) object;
        final Iterator thisNames = this.getParsedNames().iterator();
        final Iterator thatNames = that.getParsedNames().iterator();
        while (thisNames.hasNext()) {
            if (!thatNames.hasNext()) {
                return +1;
            }
            final Comparable thisNext = (Comparable) thisNames.next();
            final Comparable thatNext = (Comparable) thatNames.next();
            if (thisNext==this && thatNext==that) {
                // Never-ending loop: usually an implementation error
                throw new IllegalStateException();
            }
            final int compare = thisNext.compareTo(thatNext);
            if (compare != 0) {
                return compare;
            }
        }
        return thatNames.hasNext() ? -1 : 0;
    }

    /**
     * Compares this generic name with the specified object for equality.
     */
    public boolean equals(final Object object) {
        if (object!=null && object.getClass().equals(getClass())) {
            final GenericName that = (GenericName) object;
            return Utilities.equals(this.getParsedNames(), that.getParsedNames()) &&
                                    this.getSeparator() == that.getSeparator();
        }
        return false;
    }

    /**
     * Returns a hash code value for this generic name.
     */
    public int hashCode() {
        return (int)serialVersionUID ^ getParsedNames().hashCode();
    }
}
