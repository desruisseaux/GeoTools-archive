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
package org.geotools.referencing;

// J2SE dependencies
import java.util.Map;
import java.util.HashMap;
import java.util.Locale;
import java.util.Iterator;
import java.util.Collections;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.io.Serializable;
import java.io.ObjectStreamException;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.parameter.InvalidParameterValueException;

// Geotools dependencies
import org.geotools.util.WeakHashSet;
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * An identification of a CRS object.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class Identifier implements org.opengis.referencing.Identifier, Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 3908988678966095825L;

    /**
     * Set of weak references to existing objects (identifiers, CRS, Datum, whatever).
     * This set is used in order to return a pre-existing object instead of creating a
     * new one.
     */
    static final WeakHashSet POOL = new WeakHashSet();

    /**
     * A locale without language. This is the locale returned by {@link #getLocale} if no
     * locale were specified after the <code>"name"</code> or <code>"remarks"</code> key.
     */
    static final Locale VOID_LOCALE = new Locale("");

    /**
     * The set of locales created in this virtual machine through the {@link #getLocale}
     * method. Used in order to canonicalize the {@link Locale} objects.
     */
    private static final Map LOCALES = new HashMap();

    /**
     * Initialize {@link #LOCALES} with the set of locales defined in {@link Locale}.
     */
    static {
        LOCALES.put(VOID_LOCALE, VOID_LOCALE);
        try {
            final Field[] fields = Locale.class.getFields();
            for (int i=0; i<fields.length; i++) {
                final Field field = fields[i];
                if (Modifier.isStatic(field.getModifiers())) {
                    if (Locale.class.isAssignableFrom(field.getType())) {
                        final Locale locale = (Locale) field.get(null);
                        LOCALES.put(locale, locale);
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
    }

    /**
     * Identifier code or name, optionally from a controlled list or pattern
     * defined by a code space.
     */
    private final String code;

    /**
     * Identifier of a code space within which one or more codes are defined. This code space
     * is optional but is normally included. This code space is often defined by some authority
     * organization, where one organization may define multiple code spaces. The range and format
     * of each Code Space identifier is defined by that code space authority.
     */
    private final String codeSpace;

    /**
     * Identifier of the version of the associated code space or code, as specified
     * by the code space or code authority. This version is included only when the
     * {@linkplain #getCode code} or {@linkplain #getCodeSpace codeSpace} uses versions.
     * When appropriate, the edition is identified by the effective date, coded using
     * ISO 8601 date format.
     */
    private final String version;

    /**
     * Organization or party responsible for definition and maintenance of the
     * code space or code.
     */
    private final Citation authority;

    /**
     * Comments on or information about this identifier, or <code>null</code> if none.
     * Keys are {@link Locale} objects and values are {@link String}.
     */
    private final Map remarks;

    /**
     * Construct an identifier from a set of properties. Keys are strings from the table below.
     * Key are case-insensitive, and leading and trailing spaces are ignored. The map given in
     * argument shall contains at least a <code>"code"</code> property. Other properties listed
     * in the table below are optional.
     * <br><br>
     * <table border='1'>
     *   <tr bgcolor="#CCCCFF" class="TableHeadingColor">
     *     <th nowrap>Property name</th>
     *     <th nowrap>Value type</th>
     *     <th nowrap>Value given to</th>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;<code>"authority"</code>&nbsp;</td>
     *     <td nowrap>&nbsp;{@link String} or {@link Citation}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link #getAuthority}</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;<code>"code"</code>&nbsp;</td>
     *     <td nowrap>&nbsp;{@link String}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link #getCode}</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;<code>"codeSpace"</code>&nbsp;</td>
     *     <td nowrap>&nbsp;{@link String}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link #getCodeSpace}</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;<code>"version"</code>&nbsp;</td>
     *     <td nowrap>&nbsp;{@link String}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link #getVersion}</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;<code>"remarks"</code>&nbsp;</td>
     *     <td nowrap>&nbsp;{@link String}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link #getRemarks}</td>
     *   </tr>
     * </table>
     *
     * <P><code>"remarks"</code> is a localizable attributes which may have a language and country
     * code suffix. For example the <code>"remarks_fr"</code> property stands for remarks in
     * {@linkplain Locale#FRENCH French} and the <code>"remarks_fr_CA"</code> property stands
     * for remarks in {@linkplain Locale#CANADA_FRENCH French Canadian}.</P>
     *
     * @throws InvalidParameterValueException if a property has an invalid value.
     * @throws IllegalArgumentException if a property is invalid for some other reason.
     */
    public Identifier(final Map properties) throws IllegalArgumentException {
        this(properties, true);
    }

    /**
     * Implementation of the constructor. The remarks in the <code>properties</code> will be
     * parsed only if the <code>parseRemarks</code> argument is set to <code>true</code>.
     *
     * @param properties The properties to parse, as described in the public constructor.
     * @param <code>parseRemarks</code> <code>true</code> for parsing "remarks" as well.
     *
     * @throws InvalidParameterValueException if a property has an invalid value.
     * @throws IllegalArgumentException if a property is invalid for some other reason.
     */
    Identifier(final Map properties, final boolean parseRemarks)
            throws IllegalArgumentException
    {
        ensureNonNull("properties", properties);
        Object code      = null;
        Object codeSpace = null;
        Object version   = null;
        Object authority = null;
        Map    remarks   = null;
        /*
         * Iterate through each map entry. This have two purposes:
         *
         *   1) Ignore case (a call to properties.get("foo") can't do that)
         *   2) Find localized remarks.
         *
         * This algorithm is sub-optimal if the map contains a lot of entries of no interest to
         * this identifier. Hopefully, most users will fill a map only with usefull entries.
         */
        String key   = null;
        Object value = null;
        for (final Iterator it=properties.entrySet().iterator(); it.hasNext();) {
            final Map.Entry entry = (Map.Entry) it.next();
            key   = ((String) entry.getKey()).trim().toLowerCase();
            value = entry.getValue();
            /*
             * Note: String.hashCode() is part of J2SE specification,
             *       so it should not change across implementations.
             */
            switch (key.hashCode()) {
                case     3059181: if (key.equals("code"))      code      = value; continue;
                case -1108676807: if (key.equals("codespace")) codeSpace = value; continue;
                case   351608024: if (key.equals("version"))   version   = value; continue;
                case  1475610435: if (key.equals("authority")) authority = value;
                                  if (value instanceof String) {
                                      value = new org.geotools.metadata.citation.Citation(value.toString());
                                  }
                                  continue;
            }
            if (parseRemarks) {
                final Locale locale = getLocale(key, "remarks");
                if (locale != null) {
                    remarks = addLocalizedString(remarks, locale, value);
                }
            }
        }
        try {
            key="code"     ; this.code      = (String)   (value=code);
            key="codeSpace"; this.codeSpace = (String)   (value=codeSpace);
            key="version"  ; this.version   = (String)   (value=version);
            key="authority"; this.authority = (Citation) (value=authority);
            key="remarks"  ; this.remarks   =             remarks;
        } catch (ClassCastException exception) {
            InvalidParameterValueException e = new InvalidParameterValueException(Resources.format(
                                   ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2, key, value), key, value);
            e.initCause(exception);
            throw e;
        }
        ensureNonNull("code", code);
        canonicalizeKeys(remarks);
    }
    
    /**
     * Makes sure an argument is non-null. This is method duplicate
     * {@link Info#ensureNonNull(String, Object)} except for the more accurate stack trace.
     * It is duplicated there in order to avoid a dependency to {@link Info}.
     *
     * @param  name   Argument name.
     * @param  object User argument.
     * @throws InvalidParameterValueException if <code>object</code> is null.
     */
    private static void ensureNonNull(final String name, final Object object)
        throws IllegalArgumentException
    {
        if (object == null) {
            throw new InvalidParameterValueException(Resources.format(
                        ResourceKeys.ERROR_NULL_ARGUMENT_$1, name), name, object);
        }
    }

    /**
     * Put the given (locale, value) pair in the specified map. If the map is null, a new
     * map will be automatically created. This method will favor singleton maps as much as
     * possible since the maps will often contains only one entry.
     *
     * @param  map    The map which contains localized string.
     * @param  locale The locale for the <code>value</code> string.
     * @param  value  The localized string. Usually a {@link String} instance. This method will
     *                check the type and throws an {@link InvalidParameterValueException} if the
     *                type is invalid.
     *
     * @throws InvalidParameterValueException if <code>value</code> is invalid.
     */
    static Map addLocalizedString(Map map, Locale locale, final Object value)
            throws IllegalArgumentException
    {
        if (value != null) {
            if (!(value instanceof CharSequence)) {
                throw new InvalidParameterValueException(Resources.format(
                            ResourceKeys.ERROR_ILLEGAL_CLASS_$2,
                            Utilities.getShortClassName(value),
                            Utilities.getShortClassName(CharSequence.class)),
                            locale.getDisplayName(), value);
            }
            if (VOID_LOCALE.equals(locale)) {
                locale = null;
            }
            if (map == null) {
                return Collections.singletonMap(locale, value.toString());
            }
            if (map.size() == 1) {
                map = new HashMap(map);
            }
            map.put(locale, value);
        }
        return map;
    }

    /**
     * Find the locale for the specified key. If the key starts with the given prefix, then the
     * part after the prefix will be broken into its language, contry and variant component. For
     * example <code>getLocale("remarks_fr", "remarks")</code> will returns {@link Locale#FRENCH}.
     * If no locale were specified after the key, then this method returns {@link #VOID_LOCALE}.
     * If the key is not valid, then this method returns <code>null</code>.
     *
     * @param  key The key to examine.
     * @param  prefix The prefix.
     * @return The locale, or <code>null</code> if the key is not valid.
     */
    static Locale getLocale(final String key, final String prefix) {
        if (key.startsWith(prefix)) {
            final int length = key.length();
            int position = prefix.length();
            final String[] parts = new String[] {"", "", ""};
            for (int i=0; /*break condition inside*/; i++) {
                if (position == length) {
                    return canonicalize(new Locale(parts[0] /* language */,
                                                   parts[1] /* country  */,
                                                   parts[2] /* variant  */));
                }
                if (key.charAt(position)!='_' || i==parts.length) {
                    break;
                }
                int next = key.indexOf('_', ++position);
                if (next < position) {
                    if (next < 0) {
                        next = length;
                    } else {
                        break;  // Found consecutive '_' character.
                    }
                }
                parts[i] = key.substring(position, position=next);
            }
        }
        return null;
    }

    /**
     * Returns a canonical instance of the given locale.
     */
    private static synchronized Locale canonicalize(final Locale locale) {
        final Locale candidate = (Locale) LOCALES.get(locale);
        if (candidate != null) {
            return candidate;
        }
        LOCALES.put(locale, locale);
        return locale;
    }

    /**
     * Canonicalize all {@link Locale} keys in the given map. This
     * method is invoked as an optimization after deserialization.
     */
    static void canonicalizeKeys(final Map map) {
        if (map != null  &&  map.size() != 1) {
            // For now, we do not apply this operation on singleton map since they are immutable.
            final Map.Entry[] entries = (Map.Entry[]) map.entrySet().toArray(new Map.Entry[map.size()]);
            map.clear();
            for (int i=0; i<entries.length; i++) {
                final Map.Entry entry = entries[i];
                map.put(canonicalize((Locale)entry.getKey()), entry.getValue());
            }
        }
    }

    /**
     * Identifier code or name, optionally from a controlled list or pattern
     * defined by a {@linkplain #getCodeSpace code space}.
     *
     * @return The code.
     */
    public String getCode() {
        return code;
    }

    /**
     * Identifier of a code space within which one or more codes are defined. This code space
     * is optional but is normally included. This code space is often defined by some authority
     * organization, where one organization may define multiple code spaces. The range and format
     * of each code space identifier is defined by that code space authority.
     *
     * @return The code space, or <code>null</code> if not available.
     */
    public String getCodeSpace() {
        return codeSpace;
    }

    /**
     * Identifier of the version of the associated code space or code, as specified
     * by the code space or code authority. This version is included only when the
     * {@linkplain #getCode code} or {@linkplain #getCodeSpace code space} uses versions.
     * When appropriate, the edition is identified by the effective date, coded using
     * ISO 8601 date format.
     *
     * @return The version, or <code>null</code> if not available.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Organization or party responsible for definition and maintenance of the
     * {@linkplain #getCodeSpace code space} or {@linkplain #getCode code}.
     *
     * @return The authority, or <code>null</code> if not available.
     */
    public Citation getAuthority() {
        return authority;
    }

    /**
     * Comments on or information about this identifier. In the first use of an
     * <code>Identifier</code> for an {@link Info} object, these remarks are information about this
     * object, including data source information. Additional uses of a <code>Identifier</code>
     * for an object, if any, are aliases, and the remarks are then about that alias.
     *
     * @param  locale The desired locale for the remarks to be returned,
     *         or <code>null</code> for a non-localized string.
     * @return The remarks, or <code>null</code> if not available.
     */
    public String getRemarks(final Locale locale) {
        return getLocalized(remarks, locale);
    }

    /**
     * Returns a localized entry in the given map. The keys must be {@link Locale} objects
     * and the value must be {@link String}s. If the <code>locale</code> argument is not
     * found in the map, then this method will try to remove first the
     * {@linkplain Locale#getVariant variant}, then the {@linkplain Locale#getCountry country}
     * part of the locale. For example if the <code>"fr_CA"</code> locale was requested but not
     * found, then this method will looks for the <code>"fr"</code> locale. The <code>null</code>
     * value (which stand for unlocalized message) is tried last.
     *
     * @param map The map to look into.
     * @param locale The locale to look for, or <code>null</code>.
     */
    static String getLocalized(final Map map, Locale locale) {
        if (map != null) {
            while (locale != null) {
                final String text = (String) map.get(locale);
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
            return (String) map.get(null);
        }
        return null;
    }

    /**
     * Compares this identifier with the specified object for equality.
     */
    public boolean equals(final Object object) {
        if (object!=null && object.getClass().equals(getClass())) {
            final Identifier that = (Identifier) object;
            return Utilities.equals(this.code,      that.code     ) &&
                   Utilities.equals(this.codeSpace, that.codeSpace) &&
                   Utilities.equals(this.version,   that.version  ) &&
                   Utilities.equals(this.authority, that.authority) &&
                   Utilities.equals(this.remarks,   that.remarks  );
        }
        return false;
    }

    /**
     * Returns a hash code value for this identifier.
     */
    public int hashCode() {
        int hash = code.hashCode();
        if (codeSpace != null) {
            hash = hash*37 + codeSpace.hashCode();
        }
        if (version != null) {
            hash = hash*37 + version.hashCode();
        }
        return hash;
    }
    
    /**
     * Returns the object to use after deserialization. This is usually <code>this</code>.
     * However, if an identical object was previously deserialized, then this method replace
     * <code>this</code> by the previously deserialized object in order to reduce memory usage.
     * This is correct only for immutable objects.
     *
     * @return A canonical instance of this object.
     * @throws ObjectStreamException if this object can't be replaced.
     */
    protected Object readResolve() throws ObjectStreamException {
        return POOL.canonicalize(this);
    }

    /**
     * Returns the object to write during serialization. This is usually <code>this</code>.
     * However, if identical objects are found in the same graph during serialization, then
     * they will be replaced by a single instance in order to reduce the amount of data sent
     * to the output stream. This is correct only for immutable objects.
     *
     * @return The object to serialize (usually <code>this</code>).
     * @throws ObjectStreamException if this object can't be replaced.
     */
    protected Object writeReplace() throws ObjectStreamException {
        return POOL.canonicalize(this);
    }
}
