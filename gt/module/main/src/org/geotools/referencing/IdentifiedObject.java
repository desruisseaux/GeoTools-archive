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
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.Locale;  // For javadoc
import java.util.Iterator;
import java.util.Comparator;
import java.util.logging.Logger;
import java.io.Serializable;
import java.io.ObjectStreamException;
import javax.units.Unit;
import javax.units.SI;

// OpenGIS dependencies
import org.opengis.metadata.Identifier;
import org.opengis.metadata.citation.Citation; // For javadoc
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.util.InternationalString;
import org.opengis.util.GenericName;

// Geotools dependencies
import org.geotools.util.NameFactory;
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.referencing.wkt.Formattable;
import org.geotools.util.GrowableInternationalString;    


/**
 * A base class for metadata applicable to reference system objects.
 * When {@link AuthorityFactory} is used to create an object, the
 * {@linkplain Identifier#getAuthority authority} and {@linkplain Identifier#getCode
 * authority code} values are set to the authority name of the factory object, and the
 * authority code supplied by the client, respectively. When {@link Factory} creates an
 * object, the {@linkplain #getName name} is set to the value supplied by the client and
 * all of the other metadata items are left empty.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class IdentifiedObject extends Formattable
                           implements org.opengis.referencing.IdentifiedObject, Serializable
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -5173281694258483264L;

    /**
     * Key for the <code>"name"</code> property to be given to the
     * {@linkplain #IdentifiedObject(Map) constructor}. This is used
     * for setting the value to be returned by {@link #getName}.
     */
    public static final String NAME_PROPERTY = "name";

    /**
     * Key for the <code>"aliases"</code> property to be given to the
     * {@linkplain #IdentifiedObject(Map) constructor}. This is used
     * for setting the value to be returned by {@link #getAlias()}.
     */
    public static final String ALIAS_PROPERTY = "alias";

    /**
     * Key for the <code>"identifiers"</code> property to be given to the
     * {@linkplain #IdentifiedObject(Map) constructor}. This is used
     * for setting the value to be returned by {@link #getIdentifiers()}.
     */
    public static final String IDENTIFIERS_PROPERTY = "identifiers";
    
    /**
     * Key for the <code>"remarks"</code> property to be given to the
     * {@linkplain #IdentifiedObject(Map) constructor}. This is used
     * for setting the value to be returned by {@link #getRemarks()}.
     */
    public static final String REMARKS_PROPERTY = "remarks";
   
    /**
     * A comparator for sorting identified objects by {@linkplain #getName name}.
     */
    public static final Comparator NAME_COMPARATOR = new NameComparator();
    private static final class NameComparator implements Comparator, Serializable {
        public int compare(final Object o1, final Object o2) {
            return doCompare(((org.opengis.referencing.IdentifiedObject) o1).getName().getCode(),
                             ((org.opengis.referencing.IdentifiedObject) o2).getName().getCode());
        }
        protected Object readResolve() throws ObjectStreamException {
            return NAME_COMPARATOR;
        }
    }

    /**
     * A comparator for sorting identified objects by {@linkplain #getIdentifiers identifiers}.
     */
    public static final Comparator IDENTIFIER_COMPARATOR = new IdentifierComparator();
    private static final class IdentifierComparator implements Comparator, Serializable {
        public int compare(final Object o1, final Object o2) {
            final Identifier[] a1 = ((org.opengis.referencing.IdentifiedObject)o1).getIdentifiers();
            final Identifier[] a2 = ((org.opengis.referencing.IdentifiedObject)o2).getIdentifiers();
            return doCompare((a1!=null && a1.length!=0) ? a1[0].getCode() : null,
                             (a2!=null && a2.length!=0) ? a2[0].getCode() : null);
        }
        protected Object readResolve() throws ObjectStreamException {
            return IDENTIFIER_COMPARATOR;
        }
    }

    /**
     * A comparator for sorting identified objects by {@linkplain #getRemarks remarks}.
     */
    public static final Comparator REMARKS_COMPARATOR = new RemarksComparator();
    private static final class RemarksComparator implements Comparator, Serializable {
        public int compare(final Object o1, final Object o2) {
            return doCompare(((org.opengis.referencing.IdentifiedObject) o1).getRemarks(),
                             ((org.opengis.referencing.IdentifiedObject) o2).getRemarks());
        }
        protected Object readResolve() throws ObjectStreamException {
            return REMARKS_COMPARATOR;
        }
    }
    
    /**
     * An empty array of alias.
     */
    private static final GenericName[] NO_ALIAS = new GenericName[0];
    
    /**
     * An empty array of identifiers.
     */
    private static final Identifier[] NO_IDENTIFIER = new Identifier[0];

    /**
     * The name for this object or code. Should never be <code>null</code>.
     */
    private final Identifier name;

    /**
     * An alternative name by which this object is identified.
     */
    private final GenericName[] alias;

    /**
     * An identifier which references elsewhere the object's defining information.
     * Alternatively an identifier by which this object can be referenced.
     */
    private final Identifier[] identifiers;

    /**
     * Comments on or information about this object, or <code>null</code> if none.
     */
    private final InternationalString remarks;

    /**
     * Constructs an object from a set of properties. Keys are strings from the table below.
     * Key are case-insensitive, and leading and trailing spaces are ignored. The map given in
     * argument shall contains at least a <code>"name"</code> property. Other properties listed
     * in the table below are optional.
     * <br><br>
     * <table border='1'>
     *   <tr bgcolor="#CCCCFF" class="TableHeadingColor">
     *     <th nowrap>Property name</th>
     *     <th nowrap>Value type</th>
     *     <th nowrap>Value given to</th>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;{@link #NAME_PROPERTY "name"}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link String} or {@link Identifier}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link #getName}</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;{@link #ALIAS_PROPERTY "alias"}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link String}, <code>{@linkplain String}[]</code>,
     *     {@link GenericName} or <code>{@linkplain GenericName}[]</code>&nbsp;</td>
     *     <td nowrap>&nbsp;{@link #getAlias}</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;{@link org.geotools.referencing.Identifier#AUTHORITY_PROPERTY "authority"}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link String} or {@link Citation}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link Identifier#getAuthority} on the {@linkplain #getName name}</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;{@link org.geotools.referencing.Identifier#VERSION_PROPERTY "version"}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link String}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link Identifier#getVersion} on the {@linkplain #getName name}</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;{@link #IDENTIFIERS_PROPERTY "identifiers"}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link Identifier} or <code>{@linkplain Identifier}[]</code>&nbsp;</td>
     *     <td nowrap>&nbsp;{@link #getIdentifiers}</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;{@link #REMARKS_PROPERTY "remarks"}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link String} or {@link InternationalString}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link #getRemarks}</td>
     *   </tr>
     * </table>
     *
     * <P>Additionally, all localizable attributes like <code>"remarks"</code>
     * may have a language and country code suffix. For example the <code>"remarks_fr"</code>
     * property stands for remarks in {@linkplain java.util.Locale#FRENCH French} and the
     * <code>"remarks_fr_CA"</code> property stands for remarks in
     * {@linkplain java.util.Locale#CANADA_FRENCH French Canadian}.</P>
     *
     * <P>Note that the <code>"authority"</code> and <code>"version"</code> properties are
     * ignored if the <code>"name"</code> property is already a {@link Citation} object
     * instead of a {@link String}.</P>
     *
     * @throws InvalidParameterValueException if a property has an invalid value.
     * @throws IllegalArgumentException if a property is invalid for some other reason.
     */
    public IdentifiedObject(final Map properties) throws IllegalArgumentException {
        this(properties, null, null);
    }

    /**
     * Constructs an object from a set of properties and copy unrecognized properties in the
     * specified map. The <code>properties</code> argument is treated as in the {@linkplain
     * IdentifiedObject#IdentifiedObject(Map) one argument constructor}. All properties unknow to
     * this <code>IdentifiedObject</code> constructor are copied in the <code>subProperties</code>
     * map, after their key has been normalized (usually lower case, leading and trailing space
     * removed).
     *
     * <P>If <code>localizables</code> is non-null, then all keys listed in this argument are
     * treated as localizable one (i.e. may have a suffix like "_fr", "_de", etc.). Localizable
     * properties are stored in the <code>subProperties</code> map as {@link InternationalString}
     * objects.</P>
     *
     * @param properties    Set of properties. Should contains at least <code>"name"</code>.
     * @param subProperties The map in which to copy unrecognized properties.
     * @param localizables  Optional list of localized properties.
     *
     * @throws InvalidParameterValueException if a property has an invalid value.
     * @throws IllegalArgumentException if a property is invalid for some other reason.
     */
    protected IdentifiedObject(final Map properties,
                               final Map subProperties,
                               final String[] localizables)
            throws IllegalArgumentException
    {
        ensureNonNull("properties", properties);
        Object name        = null;
        Object alias       = null;
        Object identifiers = null;
        Object remarks     = null;
        GrowableInternationalString       growable = null;
        GrowableInternationalString[] subGrowables = null;
        /*
         * Iterate through each map entry. This have two purposes:
         *
         *   1) Ignore case (a call to properties.get("foo") can't do that)
         *   2) Find localized remarks.
         *
         * This algorithm is sub-optimal if the map contains a lot of entries of no interest to
         * this object. Hopefully, most users will fill a map only with usefull entries.
         */
NEXT_KEY: for (final Iterator it=properties.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            String    key   = ((String) entry.getKey()).trim().toLowerCase();
            Object    value = entry.getValue();
            /*
             * Note: String.hashCode() is part of J2SE specification,
             *       so it should not change across implementations.
             */
            switch (key.hashCode()) {
                // Fix case for common keywords.
                case -1528693765: if (key.equals("anchorpoint"))      key="anchorPoint";      break;
                case  1127093059: if (key.equals("realizationepoch")) key="realizationEpoch"; break;
                case -1109785975: if (key.equals("validarea"))        key="validArea";        break;
                case 3373707: {
                    if (key.equals(NAME_PROPERTY)) {
                        if (value instanceof String) {
                            name = new org.geotools.referencing.Identifier(properties, false);
                            assert value.equals(((Identifier) name).getCode()) : name;
                        } else {
                            name = value;
                        }
                        continue NEXT_KEY;
                    }
                    break;
                }
                case 92902992: {
                    if (key.equals(ALIAS_PROPERTY)) {
                        if (value instanceof String) {
                            alias = NameFactory.create((String) value);
                        } else if (value instanceof String[]) {
                            final String[] values = (String[]) value;
                            final GenericName[] names = new GenericName[values.length];
                            for (int i=0; i<values.length; i++) {
                                names[i] = NameFactory.create(values[i]);
                            }
                            alias = names;
                        } else if (value instanceof GenericName) {
                            alias = new GenericName[] {(GenericName) value};
                        } else {
                            alias = value;
                        }
                        continue NEXT_KEY;
                    }
                    break;
                }
                case 1368189162: {
                    if (key.equals(IDENTIFIERS_PROPERTY)) {
                        if (value != null) {
                            if (value instanceof Identifier) {
                                identifiers = new Identifier[] {(Identifier) value};
                            } else {
                                identifiers = value;
                            }
                        }
                        continue NEXT_KEY;
                    }
                    break;
                }
                case 1091415283: {
                    if (key.equals(REMARKS_PROPERTY)) {
                        if (value instanceof InternationalString) {
                            remarks = value;
                            continue NEXT_KEY;
                        }
                    }
                    break;
                }
            }
            /*
             * Search for additional locales for remarks (e.g. "remarks_fr").
             */
            if (value instanceof String) {
                if (growable == null) {
                    if (remarks instanceof GrowableInternationalString) {
                        growable = (GrowableInternationalString) remarks;
                    } else {
                        growable = new GrowableInternationalString();
                    }
                }
                if (growable.add(REMARKS_PROPERTY, key, value.toString())) {
                    continue NEXT_KEY;
                }
            }
            /*
             * Search for user-specified localizable properties.
             */
            if (subProperties == null) {
                continue NEXT_KEY;
            }
            if (localizables != null) {
                for (int i=0; i<localizables.length; i++) {
                    final String prefix = localizables[i];
                    if (key.equals(prefix)) {
                        if (value instanceof InternationalString) {
                            // Stores the value in 'subProperties' after the loop.
                            break;
                        }
                    }
                    if (value instanceof String) {
                        if (subGrowables == null) {
                            subGrowables = new GrowableInternationalString[localizables.length];
                        }
                        if (subGrowables[i] == null) {
                            final Object previous = subProperties.get(prefix);
                            if (previous instanceof GrowableInternationalString) {
                                subGrowables[i] = (GrowableInternationalString) previous;
                            } else {
                                subGrowables[i] = new GrowableInternationalString();
                            }
                        }
                        if (subGrowables[i].add(prefix, key, value.toString())) {
                            continue NEXT_KEY;
                        }
                    }
                }
            }
            subProperties.put(key, value);
        }
        /*
         * Get the localized remarks, if it was not yet set. If a user specified remarks
         * both as InternationalString and as String for some locales (which is a weird
         * usage...), then current implementation discart the later with a warning.
         */
        if (growable!=null && !growable.getLocales().isEmpty()) {
            if (remarks == null) {
                remarks = growable;
            } else {
                Logger.getLogger("org.geotools.referencing").warning(
                                 Resources.format(ResourceKeys.WARNING_LOCALES_DISCARTED));
            }
        }
        if (subProperties!=null && subGrowables!=null) {
            for (int i=0; i<subGrowables.length; i++) {
                if (subGrowables[i]!=null && !subGrowables[i].getLocales().isEmpty()) {
                    final String prefix = localizables[i];
                    if (subProperties.get(prefix) == null) {
                        subProperties.put(prefix, subGrowables[i]);
                    } else {
                        Logger.getLogger("org.geotools.referencing").warning(
                                         Resources.format(ResourceKeys.WARNING_LOCALES_DISCARTED));
                    }
                }
            }
        }
        /*
         * Stores the definitive reference to the attributes. Note that casts are performed only
         * there (not before). This is a wanted feature, since we want to catch ClassCastExceptions
         * are rethrown them as more informative exceptions.
         */
        String key=null; Object value=null;
        try {
            key=        NAME_PROPERTY; this.name        = (Identifier)          (value=name);
            key=       ALIAS_PROPERTY; this.alias       = (GenericName[])  clone(value=alias);
            key= IDENTIFIERS_PROPERTY; this.identifiers = (Identifier[])   clone(value=identifiers);
            key=     REMARKS_PROPERTY; this.remarks     = (InternationalString) (value=remarks);
        } catch (ClassCastException exception) {
            InvalidParameterValueException e = new InvalidParameterValueException(Resources.format(
                                   ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2, key, value), key, value);
            e.initCause(exception);
            throw e;
        }
        ensureNonNull(NAME_PROPERTY, name);
        ensureNonNull(NAME_PROPERTY, name.toString());
    }

    /**
     * If the specified object is an array, clone it.
     */
    private static Object clone(Object object) {
        if (object instanceof Object[]) {
            final Object[] array = (Object[]) object;
            object = (array.length!=0) ? array.clone() : null;
        }
        return object;
    }

    /**
     * The primary name by which this object is identified.
     */
    public Identifier getName() {
        return name;
    }

    /**
     * An alternative name by which this object is identified.
     *         
     * @return The aliases, or an empty array if there is none.
     */
    public GenericName[] getAlias() {
        if (alias != null) {
            return (GenericName[]) alias.clone();
        }
        return NO_ALIAS;
    }

    /**
     * An identifier which references elsewhere the object's defining information.
     * Alternatively an identifier by which this object can be referenced.
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
     */
    public InternationalString getRemarks(){       
        return remarks;
    }
    
    /**
     * Returns a hash value for this identified object. {@linkplain #getName Name},
     * {@linkplain #getIdentifiers identifiers} and {@linkplain #getRemarks remarks}
     * are not taken in account. In other words, two identified objects will return
     * the same hash value if they are equal in the sense of
     * <code>{@link #equals(IdentifiedObject,boolean) equals}(IdentifiedObject, <strong>false</strong>)</code>.
     *
     * @return The hash code value. This value doesn't need to be the same
     *         in past or future versions of this class.
     */
    public int hashCode() {
        // Subclasses need to overrides this!!!!
        return (int)serialVersionUID ^ getClass().hashCode();
    }

    /**
     * Returns <code>true</code> if either the {@linkplain #getName primary name} or at least
     * one {@linkplain #getAlias alias} matches the specified string. This method performs the
     * search in the following order:
     * <ul>
     *   <li>The {@linkplain #getName primary name} of this object</li>
     *   <li>The {@linkplain org.geotools.util.ScopedName fully qualified name} of an alias</li>
     *   <li>The {@linkplain org.geotools.util.LocalName local name} of an alias</li>
     * </ul>
     *
     * @param  name The name to compare.
     * @return <code>true</code> if the primary name of at least one alias
     *         matches the specified <code>name</code>.
     */
    public boolean nameMatches(final String name) {
        return nameMatches(this, alias, name);
    }

    /**
     * Returns <code>true</code> if either the {@linkplain #getName primary name} or at least
     * one {@linkplain #getAlias alias} matches the specified string. This method performs the
     * same check than the {@linkplain #nameMatches(String) non-static method} on arbitrary
     * object implementing the OpenGIS interface.
     *
     * @param  object The object to check.
     * @param  name The name.
     * @return <code>true</code> if the primary name of at least one alias
     *         matches the specified <code>name</code>.
     */
    public static boolean nameMatches(final org.opengis.referencing.IdentifiedObject object,
                                      final String name)
    {
        if (object instanceof IdentifiedObject) {
            return ((IdentifiedObject) object).nameMatches(name);
        } else {
            return nameMatches(object, object.getAlias(), name);
        }
    }

    /**
     * Implementation of <code>nameMatches</code> method.
     *
     * @param  object The object to check.
     * @param  alias  The list of alias in <code>object</code> (may be <code>null</code>).
     *                This method will never modify this list. Concequently, it may be a
     *                direct reference to an internal array.
     * @param  name The name.
     * @return <code>true</code> if the primary name of at least one alias
     *         matches the specified <code>name</code>.
     */
    private static boolean nameMatches(final org.opengis.referencing.IdentifiedObject object,
                                       final GenericName[] alias, String name)
    {
        name = name.trim();
        if (name.equalsIgnoreCase(object.getName().getCode().trim())) {
            return true;
        }
        if (alias != null) {
            for (int i=0; i<alias.length; i++) {
                if (name.equalsIgnoreCase(alias[i].asScopedName().toString().trim())) {
                    return true;
                }
                if (name.equalsIgnoreCase(alias[i].asLocalName().toString().trim())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Compares the specified object with this object for equality.
     *
     * @param  object The other object (may be <code>null</code>).
     * @return <code>true</code> if both objects are equal.
     */
    public final boolean equals(final Object object) {
        return (object instanceof IdentifiedObject) && equals((IdentifiedObject)object, true);
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
     * the identity transform, no matter what {@link #getName} saids.
     *
     * @param  object The object to compare to <code>this</code>.
     * @param  compareMetadata <code>true</code> for performing a strict comparaison, or
     *         <code>false</code> for comparing only properties relevant to transformations.
     * @return <code>true</code> if both objects are equal.
     */
    public boolean equals(final IdentifiedObject object, final boolean compareMetadata) {
        if (object!=null && object.getClass().equals(getClass())) {
            if (!compareMetadata) {
                return true;
            }
            return Utilities.equals(name,        object.name       ) &&
                      Arrays.equals(alias,       object.alias      ) &&
                      Arrays.equals(identifiers, object.identifiers) &&
                   Utilities.equals(remarks,     object.remarks    );
        }
        return false;
    }

    /**
     * Compare two Geotools's <code>IdentifiedObject</code> objects for equality. This method is
     * equivalent to <code>object1.<b>equals</b>(object2, <var>compareMetadata</var>)</code> except
     * that one or both arguments may be null. This convenience method is provided for
     * implementation of <code>equals</code> in subclasses.
     *
     * @param  object1 The first object to compare (may be <code>null</code>).
     * @param  object2 The second object to compare (may be <code>null</code>).
     * @param  compareMetadata <code>true</code> for performing a strict comparaison, or
     *         <code>false</code> for comparing only properties relevant to transformations.
     * @return <code>true</code> if both objects are equal.
     */
    static boolean equals(final IdentifiedObject object1,
                          final IdentifiedObject object2,
                          final boolean  compareMetadata)
    {
        return (object1==object2) || (object1!=null && object1.equals(object2, compareMetadata));
    }

    /**
     * Compare two OpenGIS's <code>IdentifiedObject</code> objects for equality. This convenience
     * method is provided for implementation of <code>equals</code> in subclasses.
     *
     * @param  object1 The first object to compare (may be <code>null</code>).
     * @param  object2 The second object to compare (may be <code>null</code>).
     * @param  compareMetadata <code>true</code> for performing a strict comparaison, or
     *         <code>false</code> for comparing only properties relevant to transformations.
     * @return <code>true</code> if both objects are equal.
     */
    protected static boolean equals(final org.opengis.referencing.IdentifiedObject object1,
                                    final org.opengis.referencing.IdentifiedObject object2,
                                    final boolean compareMetadata)
    {
        if (!(object1 instanceof IdentifiedObject)) return Utilities.equals(object1, object2);
        if (!(object2 instanceof IdentifiedObject)) return Utilities.equals(object2, object1);
        return equals((IdentifiedObject)object1, (IdentifiedObject)object2, compareMetadata);
    }

    /**
     * Compare two array of OpenGIS's <code>IdentifiedObject</code> objects for equality. This
     * convenience method is provided for implementation of <code>equals</code> in subclasses.
     *
     * @param  array1 The first array to compare (may be <code>null</code>).
     * @param  array2 The second array to compare (may be <code>null</code>).
     * @param  compareMetadata <code>true</code> for performing a strict comparaison, or
     *         <code>false</code> for comparing only properties relevant to transformations.
     * @return <code>true</code> if both arrays are equal.
     */
    protected static boolean equals(final org.opengis.referencing.IdentifiedObject[] array1,
                                    final org.opengis.referencing.IdentifiedObject[] array2,
                                    final boolean compareMetadata)
    {
        if (array1 != array2) {
            if (array1==null || array2==null || array1.length!=array2.length) {
                return false;
            }
            for (int i=array1.length; --i>=0;) {
                if (!equals(array1[i], array2[i], compareMetadata)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Compare two objects for order. Any object may be null. This method is
     * used for implementation of {@link #NAME_COMPARATOR} and its friends.
     */
    private static int doCompare(final Comparable c1, final Comparable c2) {
        if (c1 == null) {
            return (c2==null) ? 0 : -1;
        }
        if (c2 == null) {
            return +1;
        }
        return c1.compareTo(c2);
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
     * This is a convenience method for subclass constructors.
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
     * This is a convenience method for subclass constructors.
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
     * This is a convenience method for subclass constructors.
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
     * Returns the object to use after deserialization. This is usually <code>this</code>.
     * However, if an identical object was previously deserialized, then this method replace
     * <code>this</code> by the previously deserialized object in order to reduce memory usage.
     * This is correct only for immutable objects.
     *
     * @return A canonical instance of this object.
     * @throws ObjectStreamException if this object can't be replaced.
     */
    protected Object readResolve() throws ObjectStreamException {
        return org.geotools.referencing.Identifier.POOL.canonicalize(this);
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
        return org.geotools.referencing.Identifier.POOL.canonicalize(this);
    }
}
