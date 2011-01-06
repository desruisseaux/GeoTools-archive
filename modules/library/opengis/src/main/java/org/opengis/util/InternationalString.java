/*$************************************************************************************************
 **
 ** $Id: InternationalString.java 1153 2007-12-19 18:22:42Z jive $
 **
 ** $URL: https://geoapi.svn.sourceforge.net/svnroot/geoapi/tags/2.3-M2/geoapi/src/main/java/org/opengis/util/InternationalString.java $
 **
 ** Copyright (C) 2003-2005 Open GIS Consortium, Inc.
 ** All Rights Reserved. http://www.opengis.org/legal/
 **
 *************************************************************************************************/
package org.opengis.util;

import java.util.Locale;
import org.opengis.annotation.Extension;


/**
 * A {@linkplain String string} that has been internationalized into several {@linkplain Locale locales}.
 * This interface is used as a replacement for the {@link String} type whenever an attribute needs to be
 * internationalization capable.
 *
 * <P>The {@linkplain Comparable natural ordering} is defined by the {@linkplain String#compareTo
 * lexicographical ordering of strings} in the default locale, as returned by {@link #toString()}.
 * This string also defines the {@linkplain CharSequence character sequence} for this object.</P>
 *
 * @author Martin Desruisseaux (IRD)
 * @since GeoAPI 2.0
 *
 * @see javax.xml.registry.infomodel.InternationalString
 * @see NameFactory#createInternationalString
 */
@Extension
public interface InternationalString extends CharSequence, Comparable<InternationalString> {
    /**
     * Returns this string in the given locale. If no string is available in the given locale,
     * then some default locale is used. The default locale is implementation-dependent. It
     * may or may not be the {@linkplain Locale#getDefault() system default}.
     *
     * @param  locale The desired locale for the string to be returned, or {@code null}
     *         for a string in the implementation default locale.
     * @return The string in the given locale if available, or in the default locale otherwise.
     */
    String toString(Locale locale);

    /**
     * Returns this string in the default locale. The default locale is implementation-dependent.
     * It may or may not be the {@linkplain Locale#getDefault() system default}. If the default
     * locale is the {@linkplain Locale#getDefault() system default} (a recommended practice),
     * then invoking this method is equivalent to invoking
     * <code>{@linkplain #toString(Locale) toString}({@linkplain Locale#getDefault})</code>.
     *
     * <P>All methods from {@link CharSequence} operate on this string. This string is also
     * used as the criterion for {@linkplain Comparable natural ordering}.</P>
     *
     * @return The string in the default locale.
     */
    ///@Override    
    String toString();
}
