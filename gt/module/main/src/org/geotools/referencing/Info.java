/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.referencing;

// J2SE dependencies
import java.util.Map;
import java.util.Locale;
import java.util.Iterator;
import java.io.Serializable;
import java.io.ObjectStreamException;
import javax.units.Unit;
import javax.units.SI;

// OpenGIS dependencies
import org.opengis.referencing.Identifier;
import org.opengis.parameter.InvalidParameterValueException;

// Geotools dependencies
import org.geotools.util.WeakHashSet;
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.referencing.wkt.Formattable;


/**
 * A base class for metadata applicable to reference system objects.
 * When {@link AuthorityFactory} is used to create an object, the
 * {@linkplain Identifier#getAuthority authority} and {@linkplain Identifier#getCode
 * authority code} values are set to the authority name of the factory object, and the
 * authority code supplied by the client, respectively. When {@link Factory} creates an
 * object, the {@linkplain #getName name} is set to the value supplied by the client and
 * ll of the other metadata items are left empty.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class Info extends Formattable implements org.opengis.referencing.Info, Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -5543338998051359448L;

    /**
     * An empty array of identifiers.
     */
    private static final Identifier[] NO_IDENTIFIER = new Identifier[0];

    /**
     * The name for this object or code. Should never be <code>null</code>.
     * Keys are {@link Locale} objects and values are {@link String}.
     */
    private final Map name;

    /**
     * Set of alternative identifications of this object. The first identifier, if
     * any, is normally the primary identification code, and any others are aliases.
     */
    private final Identifier[] identifiers;

    /**
     * Comments on or information about this object, or <code>null</code> if none.
     * Keys are {@link Locale} objects and values are {@link String}.
     */
    private final Map remarks;
    
    /**
     * Construct an object from a set of properties. Keys are strings from the table below.
     * Key are case-insensitive, and leading and trailing spaces are ignored. The map given in
     * argument shall contains at least a <code>"name"</code> property. Other properties listed
     * in the table below are optional.
     *
     * <table border='1'>
     *   <tr bgcolor="#CCCCFF" class="TableHeadingColor">
     *     <th nowrap>Property name</th>
     *     <th nowrap>Value type</th>
     *     <th nowrap>Value given to</th>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;<code>"name"</code>&nbsp;</td>
     *     <td nowrap>&nbsp;{@link String}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link #getName}</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;<code>"remarks"</code>&nbsp;</td>
     *     <td nowrap>&nbsp;{@link String}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link #getRemarks}</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;<code>"authority"</code>&nbsp;</td>
     *     <td nowrap>&nbsp;{@link String} or {@link org.opengis.metadata.citation.Citation}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link Identifier#getAuthority} on the first identifier</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;<code>"code"</code>&nbsp;</td>
     *     <td nowrap>&nbsp;{@link String}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link Identifier#getCode} on the first identifier</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;<code>"codeSpace"</code>&nbsp;</td>
     *     <td nowrap>&nbsp;{@link String}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link Identifier#getCodeSpace} on the first identifier</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;<code>"version"</code>&nbsp;</td>
     *     <td nowrap>&nbsp;{@link String}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link Identifier#getVersion} on the first identifier</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;<code>"identifiers"</code>&nbsp;</td>
     *     <td nowrap>&nbsp;<code>{@linkplain Identifier}</code>[]&nbsp;</td>
     *     <td nowrap>&nbsp;{@link #getIdentifiers}</td>
     *   </tr>
     * </table>
     *
     * <P>Additionally, all localizable attributes like <code>"name"</code> and <code>"remarks"</code>
     * may have a language and country code suffix. For example the <code>"remarks_fr"</code> property
     * stands for remarks in {@linkplain java.util.Locale#FRENCH French} and the <code>"remarks_fr_CA"</code>
     * property stands for remarks in {@linkplain java.util.Locale#CANADA_FRENCH French Canadian}.</P>
     *
     * @throws InvalidParameterValueException if a property has an invalid value.
     * @throws IllegalArgumentException if a property is invalid for some other reason.
     */
    public Info(final Map properties) throws IllegalArgumentException {
        this(properties, null, null);
    }

    /**
     * Construct an object from a set of properties and copy unrecognized properties in the
     * specified map. The <code>properties</code> argument is treated as in the one argument
     * constructor. All properties unknow to this <code>Info</code> constructor are copied in
     * the <code>subProperties</code> map, after their key has been normalized (usually lower
     * case, leading and trailing space removed). If <code>localizables</code> is non-null, then
     * all keys listed in this argument are treated as localizable one (i.e. may have a suffix
     * like "_fr", "_de", etc.). Localizable properties are stored in the <code>subProperties</code>
     * map as {@link Map} of {@link Locale}, {@link Object} pairs.
     *
     * @param properties    Set of properties. Should contains at least <code>"name"</code>.
     * @param subProperties The map in which to copy unrecognized properties.
     * @param localizables  Optional list of localized properties.
     *
     * @throws InvalidParameterValueException if a property has an invalid value.
     * @throws IllegalArgumentException if a property is invalid for some other reason.
     */
    protected Info(final Map properties, final Map subProperties, final String[] localizables)
            throws IllegalArgumentException
    {
        ensureNonNull("properties", properties);
        Map          name        = null;
        Map          remarks     = null;
        Identifier[] identifiers = null;
        /*
         * Iterate through each map entry. This have two purposes:
         *
         *   1) Ignore case (a call to properties.get("foo") can't do that)
         *   2) Find localized remarks.
         *
         * This algorithm is sub-optimal if the map contains a lot of entries of no interest to
         * this identifier. Hopefully, most users will fill a map only with usefull entries.
         */
check:  for (final Iterator it=properties.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            String    key   = ((String) entry.getKey()).trim().toLowerCase();
            Object    value = entry.getValue();
            /*
             * Note: String.hashCode() is part of J2SE specification,
             *       so it should not change across implementations.
             */
            switch (key.hashCode()) {
                case 1368189162: {
                    if (key.equals("identifiers")) {
                        identifiers = (Identifier[]) value;
                        if (identifiers != null) {
                            identifiers = (Identifier[]) identifiers.clone();
                        }
                        continue check;
                    }
                }
                // Fix case for common keywords.
                case -1528693765: if (key.equals("anchorpoint"))      key="anchorPoint";      break;
                case  1127093059: if (key.equals("realizationepoch")) key="realizationEpoch"; break;
                case -1109785975: if (key.equals("validarea"))        key="validArea";        break;
            }
            Locale locale = getLocale(key, "name");
            if (locale != null) {
                name = addLocalizedString(name, locale, value);
                continue check;
            }
            locale = getLocale(key, "remarks");
            if (locale != null) {
                remarks = addLocalizedString(remarks, locale, value);
                continue check;
            }
            if (localizables != null) {
                for (int i=0; i<localizables.length; i++) {
                    final String prefix = localizables[i];
                    locale = getLocale(key, prefix);
                    if (locale != null) {
                        Map map = (Map)subProperties.get(prefix);
                        map = addLocalizedString(map, locale, value);
                        subProperties.put(prefix, map);
                        continue check;
                    }
                }
            }
            subProperties.put(key, value);
        }
        this.name        = name;
        this.identifiers = identifiers;
        this.remarks     = remarks;
        ensureNonNull("name", name);
        ensureNonNull("name", name.get(null));
    }

    /**
     * The name by which this object is identified. 
     *
     * @param  locale The desired locale for the name to be returned,
     *         or <code>null</code> for a non-localized string.
     * @return The remarks, or <code>null</code> if not available.
     */
    public String getName(Locale locale) {
        return getLocalized(name, locale);
    }

    /**
     * Set of alternative identifications of this object. The first identifier, if
     * any, is normally the primary identification code, and any others are aliases.
     *
     * @return This object identifiers, or an empty array if there is none.
     */
    public Identifier[] getIdentifiers() {
        if (identifiers != null) {
            return (Identifier[]) identifiers.clone();
        }
        return NO_IDENTIFIER;
    }

    /**
     * Comments on or information about this object, including data source information.
     *
     * @param  locale The desired locale for the remarks to be returned,
     *         or <code>null</code> for a non-localized string.
     * @return The remarks, or <code>null</code> if not available.
     */
    public String getRemarks(final Locale locale) {
        return getLocalized(remarks, locale);
    }
    
    /**
     * Returns a hash value for this info. {@linkplain #getName Name},
     * {@linkplain #getIdentifiers identifiers} and {@linkplain #getRemarks remarks}
     * are not taken in account. In other words, two info objects will return the same
     * hash value if they are equal in the sense of
     * <code>{@link #equals(Info,boolean) equals}(Info, <strong>false</strong>)</code>.
     *
     * @return The hash code value. This value doesn't need to be the same
     *         in past or future versions of this class.
     */
    public int hashCode() {
        // Subclasses need to overrides this!!!!
        return (int)serialVersionUID ^ getClass().hashCode();
    }
    
    /**
     * Compares the specified object with this info for equality.
     *
     * @param  object The other object (may be <code>null</code>).
     * @return <code>true</code> if both objects are equal.
     */
    public final boolean equals(final Object object) {
        return (object instanceof Info) && equals((Info)object, true);
    }

    /**
     * Compare this object with the specified object for equality.
     *
     * If <code>compareMetadata</code> is <code>true</code>, then all available properties
     * are compared including {@linkplain #getName name}, {@linkplain #getRemarks remarks},
     * {@linkplain #getIdentifiers identifiers code}, etc.
     *
     * If <code>compareMetadata</code> is <code>false</code>, then this method compare
     * only the properties needed for computing transformations. In other words,
     * <code>sourceCS.equals(targetCS, false)</code> returns <code>true</code> only if
     * the transformation from <code>sourceCS</code> to <code>targetCS</code> is
     * the identity transform, no matter what {@link #getIdentifiers} saids.
     *
     * @param  object The object to compare to <code>this</code>.
     * @param  compareMetadata <code>true</code> for performing a strict comparaison, or
     *         <code>false</code> for comparing only properties relevant to transformations.
     * @return <code>true</code> if both objects are equal.
     */
    public boolean equals(final Info object, final boolean compareMetadata) {
        if (object!=null && object.getClass().equals(getClass())) {
            if (!compareMetadata) {
                return true;
            }
            return equals(name,        object.name       ) &&
                   equals(identifiers, object.identifiers) &&
                   equals(remarks,     object.remarks    );
        }
        return false;
    }

    /**
     * Compare two objects for equality. This method is equivalent to
     * <code>object1.<b>equals</b>(object2)</code> except that one or
     * both arguments may be null. This convenience method is provided
     * for implementation of <code>equals</code> in subclasses.
     *
     * @param  object1 The first object to compare (may be <code>null</code>).
     * @param  object2 The second object to compare (may be <code>null</code>).
     * @return <code>true</code> if both objects are equal.
     */
    protected static boolean equals(final Object object1, final Object object2) {
        return Utilities.equals(object1, object2);
    }

    /**
     * Compare two Geotools's <code>Info</code> objects for equality. This method is equivalent to
     * <code>object1.<b>equals</b>(object2, <var>compareMetadata</var>)</code> except that one or
     * both arguments may be null. This convenience method is provided for implementation of
     * <code>equals</code> in subclasses.
     *
     * @param  object1 The first object to compare (may be <code>null</code>).
     * @param  object2 The second object to compare (may be <code>null</code>).
     * @param  compareMetadata <code>true</code> for performing a strict comparaison, or
     *         <code>false</code> for comparing only properties relevant to transformations.
     * @return <code>true</code> if both objects are equal.
     */
    static boolean equals(final Info    object1,
                          final Info    object2,
                          final boolean compareMetadata)
    {
        return (object1==object2) || (object1!=null && object1.equals(object2, compareMetadata));
    }

    /**
     * Compare two OpenGIS's <code>Info</code> objects for equality. This convenience
     * method is provided for implementation of <code>equals</code> in subclasses.
     *
     * @param  object1 The first object to compare (may be <code>null</code>).
     * @param  object2 The second object to compare (may be <code>null</code>).
     * @param  compareMetadata <code>true</code> for performing a strict comparaison, or
     *         <code>false</code> for comparing only properties relevant to transformations.
     * @return <code>true</code> if both objects are equal.
     */
    protected static boolean equals(final org.opengis.referencing.Info object1,
                                    final org.opengis.referencing.Info object2,
                                    final boolean compareMetadata)
    {
        if (!(object1 instanceof Info)) return equals(object1, object2);
        if (!(object2 instanceof Info)) return equals(object2, object1);
        return equals((Info)object1, (Info)object2, compareMetadata);
    }
    
    /**
     * Makes sure that an argument is non-null. This is a
     * convenience method for subclass constructors.
     *
     * @param  name   Argument name.
     * @param  object User argument.
     * @throws InvalidParameterValueException if <code>object</code> is null.
     */
    protected static void ensureNonNull(final String name, final Object object)
        throws IllegalArgumentException
    {
        if (object == null) {
            throw new InvalidParameterValueException(Resources.format(
                        ResourceKeys.ERROR_NULL_ARGUMENT_$1, name), name, object);
        }
    }
    
    /**
     * Makes sure an array element is non-null. This is
     * a convenience method for subclass constructors.
     *
     * @param  name  Argument name.
     * @param  array User argument.
     * @param  index Index of the element to check.
     * @throws InvalidParameterValueException if <code>array[i]</code> is null.
     */
    protected static void ensureNonNull(final String name, final Object[] array, final int index)
        throws IllegalArgumentException
    {
        if (array[index] == null) {
            throw new InvalidParameterValueException(Resources.format(
                        ResourceKeys.ERROR_NULL_ARGUMENT_$1, name+'['+index+']'), name, array);
        }
    }
    
    /**
     * Makes sure that the specified unit is a temporal one.
     *  This is a convenience method for subclass constructors.
     *
     * @param  unit Unit to check.
     * @throws IllegalArgumentException if <code>unit</code> is not a temporal unit.
     */
    protected static void ensureTimeUnit(final Unit unit) throws IllegalArgumentException {
        if (!SI.SECOND.isCompatible(unit)) {
            throw new IllegalArgumentException(Resources.format(
                        ResourceKeys.ERROR_NON_TEMPORAL_UNIT_$1, unit));
        }
    }
    
    /**
     * Makes sure that the specified unit is a linear one.
     *  This is a convenience method for subclass constructors.
     *
     * @param  unit Unit to check.
     * @throws IllegalArgumentException if <code>unit</code> is not a linear unit.
     */
    protected static void ensureLinearUnit(final Unit unit) throws IllegalArgumentException {
        if (!SI.METER.isCompatible(unit)) {
            throw new IllegalArgumentException(Resources.format(
                        ResourceKeys.ERROR_NON_LINEAR_UNIT_$1, unit));
        }
    }
    
    /**
     * Makes sure that the specified unit is an angular one.
     *  This is a convenience method for subclass constructors.
     *
     * @param  unit Unit to check.
     * @throws IllegalArgumentException if <code>unit</code> is not an angular unit.
     */
    protected static void ensureAngularUnit(final Unit unit) throws IllegalArgumentException {
        if (!SI.RADIAN.isCompatible(unit)) {
            throw new IllegalArgumentException(Resources.format(
                        ResourceKeys.ERROR_NON_ANGULAR_UNIT_$1, unit));
        }
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
    protected static String getLocalized(final Map map, final Locale locale) {
        return org.geotools.referencing.Identifier.getLocalized(map, locale);
    }

    /**
     * Convenience method which delegate the work to {@link org.geotools.referencing.Identifier}.
     */
    private static Locale getLocale(final String key, final String prefix) {
        return org.geotools.referencing.Identifier.getLocale(key, prefix);
    }

    /**
     * Convenience method which delegate the work to {@link org.geotools.referencing.Identifier}.
     */
    private static Map addLocalizedString(final Map map, final Locale locale, final Object value) {
        return org.geotools.referencing.Identifier.addLocalizedString(map, locale, value);
    }
    
    /**
     * Returns a reference to a unique instance of this <code>Info</code>.
     * This method is automatically invoked during deserialization.
     *
     * @return A canonical instance of this object.
     * @throws ObjectStreamException if the operation failed.
     */
    protected Object readResolve() throws ObjectStreamException {
        org.geotools.referencing.Identifier.canonicalizeKeys(name);
        org.geotools.referencing.Identifier.canonicalizeKeys(remarks);
        return org.geotools.referencing.Identifier.POOL.canonicalize(this);
    }
}
