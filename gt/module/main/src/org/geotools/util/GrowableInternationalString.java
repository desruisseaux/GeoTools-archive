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
package org.geotools.util;

// J2SE dependencies
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.resources.cts.Resources;


/**
 * An implementation of international string using a {@linkplain Map map}
 * of strings for different {@linkplain Locale locales}. Strings for new
 * locales can be {@linkplain #add(Locale,String) added}, but existing
 * strings can't be removed or modified. This behavior is a compromise
 * between making constructionss easier, and being suitable for use in
 * immutable objects.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class GrowableInternationalString extends AbstractInternationalString implements Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 5760033376627376937L;

    /**
     * The set of locales created in this virtual machine through methods of this class.
     * Used in order to {@linkplain #canonicalize canonicalize} the {@link Locale} objects.
     */
    private static final Map LOCALES = new HashMap();

    /**
     * The string values in different locales (never <code>null</code>).
     * Keys are {@link Locale} objects and values are {@link String}s.
     */
    private Map localMap;

    /**
     * An unmodifiable view of the entry set in {@link #localMap}. This is the set of locales
     * defined in this international string. Will be constructed only when first requested.
     */
    private transient Set localSet;

    /**
     * Constructs an initially empty international string. Localized strings can been added
     * using one of {@link #add add(...)} methods.
     */
    public GrowableInternationalString() {
        localMap = Collections.EMPTY_MAP;
    }

    /**
     * Constructs an international string initialized with the specified string.
     * Additional localized strings can been added using one of {@link #add add(...)}
     * methods. The string specified to this constructor is the one that will be
     * returned if no localized string is found for the {@link Locale} argument
     * in a call to {@link #toString(Locale)}.
     *
     * @param string The string in no specific locale.
     */
    public GrowableInternationalString(final String string) {
        if (string != null) {
            localMap = Collections.singletonMap(null, string);
        } else {
            localMap = Collections.EMPTY_MAP;
        }
    }

    /**
     * Add a string for the given locale.
     *
     * @param  locale The locale for the <code>string</code> value, or <code>null</code>.
     * @param  string The localized string.
     * @throws IllegalArgumentException if a different string value was already set for
     *         the given locale.
     */
    public synchronized void add(final Locale locale, final String string)
            throws IllegalArgumentException
    {
        if (string != null) {
            switch (localMap.size()) {
                case 0: {
                    localMap = Collections.singletonMap(locale, string);
                    defaultValue = null; // Will be recomputed when first needed.
                    return;
                }
                case 1: {
                    localMap = new HashMap(localMap);
                    break;
                }
            }
            String old = (String) localMap.get(locale);
            if (old != null) {
                if (string.equals(old)) {
                    return;
                }
                // TODO: provide a localized message "String value already set for locale ...".
                throw new IllegalArgumentException();
            }
            localMap.put(locale, string);
            defaultValue = null; // Will be recomputed when first needed.
        }
    }

    /**
     * Add a string for the given property key. This is a convenience method for constructing an
     * {@code AbstractInternationalString} during iteration through the
     * {@linkplain java.util.Map.Entry entries} in a {@link Map}. It infers the {@link Locale}
     * from the property <code>key</code>, using the following steps:
     * <ul>
     *   <li>If the <code>key</code> do not starts with the specified <code>prefix</code>, then
     *       this method do nothing and returns <code>false</code>.</li>
     *   <li>Otherwise, the characters after the <code>prefix</code> are parsed as an ISO language
     *       and country code, and the {@link #add(Locale,String)} method is
     *       invoked.</li>
     * </ul>
     *
     * <P>For example if the prefix is <code>"remarks"</code>, then the <code>"remarks_fr"</code>
     * property key stands for remarks in {@linkplain Locale#FRENCH French} while the
     * <code>"remarks_fr_CA"</code> property key stands for remarks in
     * {@linkplain Locale#CANADA_FRENCH French Canadian}.</P>
     *
     * @param  prefix The prefix to skip at the begining of the <code>key</code>.
     * @param  key The property key.
     * @param  string The localized string for the specified <code>key</code>.
     * @return <code>true</code> if the key has been recognized, or <code>false</code> otherwise.
     * @throws IllegalArgumentException if the locale after the prefix is an illegal code, or a
     *         different string value was already set for the given locale.
     */
    public boolean add(final String prefix, final String key, final String string)
            throws IllegalArgumentException
    {
        if (!key.startsWith(prefix)) {
            return false;
        }
        int position  = prefix.length();
        final int length = key.length();
        final String[] parts = new String[] {"", "", ""};
        for (int i=0; /*break condition inside*/; i++) {
            if (position == length) {
                final Locale locale = (i==0) ? (Locale)null :
                       canonicalize(new Locale(parts[0] /* language */,
                                               parts[1] /* country  */,
                                               parts[2] /* variant  */));
                add(locale, string);
                return true;
            }
            if (key.charAt(position)!='_' || i==parts.length) {
                // Unknow character, or two many characters
                break;
            }
            int next = key.indexOf('_', ++position);
            if (next < 0) {
                next = length;
            } else if (next == position) {
                // Found two consecutive '_' characters
                break;
            }
            parts[i] = key.substring(position, position=next);
        }
        throw new IllegalArgumentException(Resources.format(ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2,
                                           "locale", key.substring(prefix.length())));
    }

    /**
     * Returns a canonical instance of the given locale.
     *
     * @param  locale The locale to canonicalize.
     * @return The canonical instance of <code>locale</code>.
     */
    private static synchronized Locale canonicalize(final Locale locale) {
        /**
         * Initialize the LOCALES map with the set of locales defined in the Locale class.
         * This operation is done only once.
         */
        if (LOCALES.isEmpty()) try {
            final Field[] fields = Locale.class.getFields();
            for (int i=0; i<fields.length; i++) {
                final Field field = fields[i];
                if (Modifier.isStatic(field.getModifiers())) {
                    if (Locale.class.isAssignableFrom(field.getType())) {
                        final Locale toAdd = (Locale) field.get(null);
                        LOCALES.put(toAdd, toAdd);
                    }
                }
            }
        } catch (Exception exception) {
            /*
             * Not a big deal if this operation fails (this is actually just an
             * optimization for reducing memory usage). Log a warning and continue.
             */
            Utilities.unexpectedException("org.geotools.referencing",
                                          "Identifier", "<cinit>", exception);
        }
        /*
         * Now canonicalize the locale.
         */
        final Locale candidate = (Locale) LOCALES.get(locale);
        if (candidate != null) {
            return candidate;
        }
        LOCALES.put(locale, locale);
        return locale;
    }

    /**
     * Returns the set of locales defined in this international string.
     */
    public Set getLocales() {
        // No need to synchronize; this is not a big deal if this object is built twice.
        if (localSet == null) {
            localSet = Collections.unmodifiableSet(localMap.entrySet());
        }
        return localSet;
    }

    /**
     * Returns a string in the specified locale. If there is no string for the specified
     * <code>locale</code>, then this method search for a locale without the
     * {@linkplain Locale#getVariant variant} part. If no string are found,
     * then this method search for a locale without the {@linkplain Locale#getCountry country}
     * part. For example if the <code>"fr_CA"</code> locale was requested but not found, then
     * this method looks for the <code>"fr"</code> locale. The <code>null</code> locale
     * (which stand for unlocalized message) is tried last.
     *
     * @param  locale The locale to look for, or <code>null</code>.
     * @return The string in the specified locale, or in a default locale.
     */
    public String toString(Locale locale) {
        String text;
        while (locale != null) {
            text = (String) localMap.get(locale);
            if (text != null) {
                return text;
            }
            final String language = locale.getLanguage();
            final String country  = locale.getCountry ();
            final String variant  = locale.getVariant ();
            if (variant.length() != 0) {
                locale = new Locale(language, country);
                continue;
            }
            if (country.length() != 0) {
                locale = new Locale(language);
                continue;
            }
            break;
        }
        
        // Try the string in the 'null' locale.
        text = (String) localMap.get(null);
        if (text == null) {
            // No 'null' locale neither. Returns the first string in whatever locale.
            final Iterator it = localMap.values().iterator();
            if (it.hasNext()) {
                return (String) it.next();
            }
        }
        return text;
    }

    /**
     * Compares this international string with the specified object for equality.
     */
    public boolean equals(final Object object) {
        if (object!=null && object.getClass().equals(getClass())) {
            final GrowableInternationalString that = (GrowableInternationalString) object;
            return Utilities.equals(this.localMap, that.localMap);
        }
        return false;
    }

    /**
     * Returns a hash code value for this international text.
     */
    public int hashCode() {
        return (int)serialVersionUID ^ localMap.hashCode();
    }
    
    /**
     * Canonicalize the locales after deserialization.
     */
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        final int size = localMap.size();
        if (size == 0) {
            return;
        }
        final Map.Entry[] entries;
        entries = (Map.Entry[]) localMap.entrySet().toArray(new Map.Entry[size]);
        if (size == 1) {
            final Map.Entry entry = entries[0];
            localMap = Collections.singletonMap(canonicalize((Locale)entry.getKey()),
                                                                     entry.getValue());
        } else {
            localMap.clear();
            for (int i=0; i<entries.length; i++) {
                final Map.Entry entry = entries[i];
                localMap.put(canonicalize((Locale)entry.getKey()), entry.getValue());
            }
        }
    }    
}
