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
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;
import javax.units.SI;
import javax.units.Unit;

// OpenGIS dependencies
import org.opengis.metadata.Identifier;
import org.opengis.metadata.citation.Citation;
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.AuthorityFactory;
import org.opengis.referencing.ObjectFactory;
import org.opengis.util.GenericName;
import org.opengis.util.InternationalString;
import org.opengis.util.ScopedName;

// Geotools dependencies
import org.geotools.referencing.wkt.Formattable;
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.resources.cts.Resources;
import org.geotools.util.GrowableInternationalString;
import org.geotools.util.NameFactory;


/**
 * A base class for metadata applicable to reference system objects.
 * When {@link AuthorityFactory} is used to create an object, the
 * {@linkplain Identifier#getAuthority authority} and {@linkplain Identifier#getCode
 * authority code} values are set to the authority name of the factory object, and the
 * authority code supplied by the client, respectively. When {@link ObjectFactory} creates an
 * object, the {@linkplain #getName name} is set to the value supplied by the client and
 * all of the other metadata items are left empty.
 * <p>
 * This class is conceptually <cite>abstract</cite>, even if it is technically possible to
 * instantiate it. Typical applications should create instances of the most specific subclass with
 * {@code Default} prefix instead. An exception to this rule may occurs when it is not possible to
 * identify the exact type. For example it is not possible to infer the exact coordinate system from
 * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
 * Known Text</cite></A> is some cases (e.g. in a {@code LOCAL_CS} element). In such exceptional
 * situation, a plain {@link org.geotools.referencing.cs.AbstractCS} object may be instantiated.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class AbstractIdentifiedObject extends Formattable implements IdentifiedObject, Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -5173281694258483264L;

    /**
     * Key for the <code>{@value #NAME_PROPERTY}</code> property to be given to the
     * {@linkplain #AbstractIdentifiedObject(Map) constructor}. This is used
     * for setting the value to be returned by {@link #getName}.
     */
    public static final String NAME_PROPERTY = "name";

    /**
     * Key for the <code>{@value #ALIAS_PROPERTY}</code> property to be given to the
     * {@linkplain #AbstractIdentifiedObject(Map) constructor}. This is used
     * for setting the value to be returned by {@link #getAlias()}.
     */
    public static final String ALIAS_PROPERTY = "alias";

    /**
     * Key for the <code>{@value #IDENTIFIERS_PROPERTY}</code> property to be given to the
     * {@linkplain #AbstractIdentifiedObject(Map) constructor}. This is used
     * for setting the value to be returned by {@link #getIdentifiers()}.
     */
    public static final String IDENTIFIERS_PROPERTY = "identifiers";
    
    /**
     * Key for the <code>{@value #REMARKS_PROPERTY}</code> property to be given to the
     * {@linkplain #AbstractIdentifiedObject(Map) constructor}. This is used
     * for setting the value to be returned by {@link #getRemarks()}.
     */
    public static final String REMARKS_PROPERTY = "remarks";
   
    /**
     * A comparator for sorting identified objects by {@linkplain #getName name}.
     */
    public static final Comparator NAME_COMPARATOR = new NameComparator();
    private static final class NameComparator implements Comparator, Serializable {
        public int compare(final Object o1, final Object o2) {
            return doCompare(((IdentifiedObject) o1).getName().getCode(),
                             ((IdentifiedObject) o2).getName().getCode());
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
            final Identifier[] a1 = ((IdentifiedObject)o1).getIdentifiers();
            final Identifier[] a2 = ((IdentifiedObject)o2).getIdentifiers();
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
            return doCompare(((IdentifiedObject) o1).getRemarks(),
                             ((IdentifiedObject) o2).getRemarks());
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
     * Constructs a new identified object with the same values than the specified one.
     */
    public AbstractIdentifiedObject(final IdentifiedObject object) {
        name        = object.getName();
        alias       = object.getAlias();
        identifiers = object.getIdentifiers();
        remarks     = object.getRemarks();
    }

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
     *     <td nowrap>&nbsp;{@link NamedIdentifier#AUTHORITY_PROPERTY "authority"}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link String} or {@link Citation}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link Identifier#getAuthority} on the {@linkplain #getName name}</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;{@link NamedIdentifier#VERSION_PROPERTY "version"}&nbsp;</td>
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
    public AbstractIdentifiedObject(final Map properties) throws IllegalArgumentException {
        this(properties, null, null);
    }

    /**
     * Constructs an object from a set of properties and copy unrecognized properties in the
     * specified map. The <code>properties</code> argument is treated as in the {@linkplain
     * AbstractIdentifiedObject#AbstractIdentifiedObject(Map) one argument constructor}. All
     * properties unknow to this <code>AbstractIdentifiedObject</code> constructor are copied
     * in the <code>subProperties</code> map, after their key has been normalized (usually
     * lower case, leading and trailing space removed).
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
    protected AbstractIdentifiedObject(final Map properties,
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
                // Fix case for common keywords. They are not used
                // by this class, but are used by some subclasses.
                case -1528693765: if (key.equalsIgnoreCase("anchorPoint"))        key="anchorPoint";        break;
                case -1805658881: if (key.equalsIgnoreCase("bursaWolf"))          key="bursaWolf";          break;
                case   109688209: if (key.equalsIgnoreCase("operationVersion"))   key="operationVersion";   break;
                case  1126917133: if (key.equalsIgnoreCase("positionalAccuracy")) key="positionalAccuracy"; break;
                case  1127093059: if (key.equalsIgnoreCase("realizationEpoch"))   key="realizationEpoch";   break;
                case -1109785975: if (key.equalsIgnoreCase("validArea"))          key="validArea";          break;
                // ----------------------------
                // "name": String or Identifier
                // ----------------------------
                case 3373707: {
                    if (key.equals(NAME_PROPERTY)) {
                        if (value instanceof String) {
                            name = new NamedIdentifier(properties, false);
                            assert value.equals(((Identifier) name).getCode()) : name;
                        } else {
                            name = value;
                        }
                        continue NEXT_KEY;
                    }
                    break;
                }
                // -------------------------------------------------------
                // "alias": String, String[], GenericName or GenericName[]
                // -------------------------------------------------------
                case 92902992: {
                    if (key.equals(ALIAS_PROPERTY)) {
                        alias = NameFactory.toArray(value);
                        continue NEXT_KEY;
                    }
                    break;
                }
                // -----------------------------------------
                // "identifiers": Identifier or Identifier[]
                // -----------------------------------------
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
                // ----------------------------------------
                // "remarks": String or InternationalString
                // ----------------------------------------
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
             * 'growable.add(...)' will add the value only if the key starts
             * with the "remarks" prefix.
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
     * Returns the informations provided in the specified indentified object as a map of
     * properties. The returned map contains key such as {@link #NAME_PROPERTY}, and values
     * from methods such as {@link IdentifiedObject#getName}.
     *
     * @param  info The identified object to view as a properties map.
     * @return An view of the identified object as an immutable map.
     */
    public static Map getProperties(final IdentifiedObject info) {
        return new Properties(info);
    }
    
    /**
     * Returns a hash value for this identified object. {@linkplain #getName Name},
     * {@linkplain #getIdentifiers identifiers} and {@linkplain #getRemarks remarks}
     * are not taken in account. In other words, two identified objects will return
     * the same hash value if they are equal in the sense of
     * <code>{@link #equals(AbstractIdentifiedObject,boolean) equals}(AbstractIdentifiedObject,
     * <strong>false</strong>)</code>.
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
    public static boolean nameMatches(final IdentifiedObject object,
                                      final String name)
    {
        if (object instanceof AbstractIdentifiedObject) {
            return ((AbstractIdentifiedObject) object).nameMatches(name);
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
    private static boolean nameMatches(final IdentifiedObject object,
                                       final GenericName[] alias, String name)
    {
        name = name.trim();
        if (name.equalsIgnoreCase(object.getName().getCode().trim())) {
            return true;
        }
        if (alias != null) {
            for (int i=0; i<alias.length; i++) {
                final ScopedName asScoped = alias[i].asScopedName();
                if (asScoped!=null && name.equalsIgnoreCase(asScoped.toString().trim())) {
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
        return (object instanceof AbstractIdentifiedObject) &&
                equals((AbstractIdentifiedObject)object, true);
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
     * <P>
     * Some subclasses (especially {@link org.geotools.referencing.datum.AbstractDatum}
     * and {@link org.geotools.parameter.AbstractParameterDescriptor}) will test for
     * the {@linkplain #getName name}, since objects with different name have
     * completly different meaning. For example nothing differentiate the
     * <code>"semi_major"</code> and <code>"semi_minor"</code> parameters
     * except the name. The name comparaison may be loose however, i.e. we may
     * accept a name matching an alias.
     *
     * @param  object The object to compare to <code>this</code>.
     * @param  compareMetadata <code>true</code> for performing a strict comparaison, or
     *         <code>false</code> for comparing only properties relevant to transformations.
     * @return <code>true</code> if both objects are equal.
     */
    public boolean equals(final AbstractIdentifiedObject object, final boolean compareMetadata) {
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
     * Compare two Geotools's <code>AbstractIdentifiedObject</code> objects for equality. This
     * method is equivalent to {@code object1.<b>equals</b>(object2, <var>compareMetadata</var>)}
     * except that one or both arguments may be null. This convenience method is provided for
     * implementation of <code>equals</code> in subclasses.
     *
     * @param  object1 The first object to compare (may be <code>null</code>).
     * @param  object2 The second object to compare (may be <code>null</code>).
     * @param  compareMetadata <code>true</code> for performing a strict comparaison, or
     *         <code>false</code> for comparing only properties relevant to transformations.
     * @return <code>true</code> if both objects are equal.
     */
    static boolean equals(final AbstractIdentifiedObject object1,
                          final AbstractIdentifiedObject object2,
                          final boolean          compareMetadata)
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
    protected static boolean equals(final IdentifiedObject object1,
                                    final IdentifiedObject object2,
                                    final boolean  compareMetadata)
    {
        if (!(object1 instanceof AbstractIdentifiedObject)) return Utilities.equals(object1, object2);
        if (!(object2 instanceof AbstractIdentifiedObject)) return Utilities.equals(object2, object1);
        return equals((AbstractIdentifiedObject)object1, (AbstractIdentifiedObject)object2, compareMetadata);
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
    protected static boolean equals(final IdentifiedObject[] array1,
                                    final IdentifiedObject[] array2,
                                    final boolean   compareMetadata)
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
        if (!SI.RADIAN.isCompatible(unit) && !Unit.ONE.equals(unit)) {
            throw new IllegalArgumentException(Resources.format(
                        ResourceKeys.ERROR_NON_ANGULAR_UNIT_$1, unit));
        }
    }
}
