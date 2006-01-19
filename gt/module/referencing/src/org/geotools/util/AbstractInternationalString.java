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
package org.geotools.util;

// J2SE dependencies
import java.util.Locale;

// OpenGIS dependencies
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;


/**
 * A {@linkplain String string} that has been internationalized into several
 * {@linkplain Locale locales}. This class is used as a replacement for the
 * {@link String} type whenever an attribute needs to be internationalization
 * capable. The default value (as returned by {@link #toString()} and other
 * {@link CharSequence} methods} is the string in the current {@linkplain
 * Locale#getDefault system default}.
 *
 * <P>The {@linkplain Comparable natural ordering} is defined by the string in
 * {@linkplain Locale#getDefault default locale}, as returned by {@link #toString()}.
 * This string also defines the {@linkplain CharSequence character sequence}.</P>
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class AbstractInternationalString implements InternationalString {
    /**
     * The string in the {@linkplain Locale#getDefault system default} locale, or {@code null}
     * if this string has not yet been determined. This is the default string returned by
     * {@link #toString()} and others methods from the {@link CharSequence} interface.
     *
     * <P>This field is not serialized because serialization is often used for data transmission
     * between a server and a client, and the client may not use the same locale than the server.
     * We want the locale to be examined again on the client side.</P>
     *
     * <P>This field is read and write by {@link SimpleInternationalString}.</P>
     */
    transient String defaultValue;

    /**
     * Constructs an international string.
     */
    public AbstractInternationalString() {
    }
    
    /**
     * Makes sure an argument is non-null.
     *
     * @param  name   Argument name.
     * @param  object User argument.
     * @throws IllegalArgumentException if {@code object} is null.
     */
    static void ensureNonNull(final String name, final Object object)
            throws IllegalArgumentException
    {
        if (object == null) {
            throw new IllegalArgumentException(Errors.format(ErrorKeys.NULL_ARGUMENT_$1, name));
        }
    }
    
    /**
     * Returns the length of the string in the {@linkplain Locale#getDefault default locale}.
     * This is the length of the string returned by {@link #toString()}.
     */
    public int length() {
        if (defaultValue == null) {
            defaultValue = toString();
            if (defaultValue == null) {
                return 0;
            }
        }
        return defaultValue.length();
    }

    /**
     * Returns the character of the string in the {@linkplain Locale#getDefault default locale}
     * at the specified index. This is the character of the string returned by {@link #toString()}.
     *
     * @param  index The index of the character.
     * @return The character at the specified index.
     * @throws IndexOutOfBoundsException if the specified index is out of bounds.
     */
    public char charAt(final int index) throws IndexOutOfBoundsException {
        if (defaultValue == null) {
            defaultValue = toString();
            if (defaultValue == null) {
                throw new IndexOutOfBoundsException(String.valueOf(index));
            }
        }
        return defaultValue.charAt(index);
    }

    /**
     * Returns a subsequence of the string in the {@linkplain Locale#getDefault default locale}.
     * The subsequence is a {@link String} object starting with the character value at the specified
     * index and ending with the character value at index {@code end - 1}.
     * 
     * @param   start The start index, inclusive.
     * @param   end   The end index, exclusive.
     * @return  The specified subsequence.
     * @throws  IndexOutOfBoundsException  if {@code start} or {@code end} is
     *          out of range.
     */
    public CharSequence subSequence(final int start, final int end) {
        if (defaultValue == null) {
            defaultValue = toString();
            if (defaultValue == null) {
                throw new IndexOutOfBoundsException(String.valueOf(start));
            }
        }
        return defaultValue.substring(start, end);
    }

    /**
     * Returns this string in the given locale. If no string is available in the given locale,
     * then some default locale is used. The default locale is implementation-dependent. It
     * may or may not be the {@linkplain Locale#getDefault() system default}).
     *
     * @param  locale The desired locale for the string to be returned, or {@code null}
     *         for a string in the implementation default locale.
     * @return The string in the given locale if available, or in the default locale otherwise.
     */
    public abstract String toString(final Locale locale);

    /**
     * Returns this string in the default locale. Invoking this method is equivalent to invoking
     * <code>{@linkplain #toString(Locale) toString}({@linkplain Locale#getDefault})</code>. All
     * methods from {@link CharSequence} operate on this string. This string is also used as the
     * criterion for {@linkplain Comparable natural ordering}.
     *
     * @return The string in the default locale.
     */
    public String toString() {
        if (defaultValue == null) {
            defaultValue = toString(Locale.getDefault());
            if (defaultValue == null) {
                return "";
            }
        }
        return defaultValue;
    }

    /**
     * Compare this string with the specified object for order. This method compare
     * the string in the {@linkplain Locale#getDefault default locale}, as returned
     * by {@link #toString()}.
     */
    public int compareTo(final Object object) {
        return toString().compareTo(object.toString());
    }
}
