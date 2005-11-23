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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.logging.Level;
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
import org.opengis.util.LocalName;
import org.opengis.util.ScopedName;

// Geotools dependencies
import org.geotools.metadata.iso.citation.CitationImpl;
import org.geotools.referencing.wkt.Formattable;
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Logging;
import org.geotools.resources.i18n.LoggingKeys;
import org.geotools.util.GrowableInternationalString;
import org.geotools.util.NameFactory;


/**
 * A base class for metadata applicable to reference system objects.
 * When {@link AuthorityFactory} is used to create an object, the
 * {@linkplain Identifier#getAuthority authority} and {@linkplain Identifier#getCode
 * authority code} values are set to the authority name of the factory object, and the
 * authority code supplied by the client, respectively. When {@link ObjectFactory} creates an
 * object, the {@linkplain #getName() name} is set to the value supplied by the client and
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
 * @since 2.1
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class AbstractIdentifiedObject extends Formattable implements IdentifiedObject, Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -5173281694258483264L;

    /**
     * An empty array of identifiers. This is usefull for fetching identifiers as an array,
     * using the following idiom:
     * <blockquote><pre>
     * {@linkplain #getIdentifiers()}.toArray(EMPTY_IDENTIFIER_ARRAY);
     * </pre></blockquote>
     */
    public static final Identifier[] EMPTY_IDENTIFIER_ARRAY = new Identifier[0];

    /**
     * An empty array of alias. This is usefull for fetching alias as an array,
     * using the following idiom:
     * <blockquote><pre>
     * {@linkplain #getAlias()}.toArray(EMPTY_ALIAS_ARRAY);
     * </pre></blockquote>
     */
    public static final GenericName[] EMPTY_ALIAS_ARRAY = new GenericName[0];
   
    /**
     * A comparator for sorting identified objects by {@linkplain #getName() name}.
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
            final Collection/*<Identifier>*/ a1 = ((IdentifiedObject)o1).getIdentifiers();
            final Collection/*<Identifier>*/ a2 = ((IdentifiedObject)o2).getIdentifiers();
            return doCompare((a1!=null && !a1.isEmpty()) ? ((Identifier) a1.iterator().next()).getCode() : null,
                             (a2!=null && !a2.isEmpty()) ? ((Identifier) a2.iterator().next()).getCode() : null);
            // TODO: remove (Identifier) cast once we will be allowed to compile for J2SE 1.5.
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
     * The name for this object or code. Should never be {@code null}.
     */
    private final Identifier name;

    /**
     * An alternative name by which this object is identified.
     */
    private final Collection/*<GenericName>*/ alias;

    /**
     * An identifier which references elsewhere the object's defining information.
     * Alternatively an identifier by which this object can be referenced.
     */
    private final Set/*<Identifier>*/ identifiers;

    /**
     * Comments on or information about this object, or {@code null} if none.
     */
    private final InternationalString remarks;

    /**
     * Constructs a new identified object with the same values than the specified one.
     * This copy constructor provides a way to wrap an arbitrary implementation into a
     * Geotools one or a user-defined one (as a subclass), usually in order to leverage
     * some implementation-specific API. This constructor performs a shallow copy,
     * i.e. the properties are not cloned.
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
     * argument shall contains at least a {@code "name"} property. Other properties listed
     * in the table below are optional.
     * <p>
     * <table border='1'>
     *   <tr bgcolor="#CCCCFF" class="TableHeadingColor">
     *     <th nowrap>Property name</th>
     *     <th nowrap>Value type</th>
     *     <th nowrap>Value given to</th>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;{@link #NAME_KEY "name"}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link String} or {@link Identifier}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link #getName()}</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;{@link #ALIAS_KEY "alias"}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link String}, <code>{@linkplain String}[]</code>,
     *     {@link GenericName} or <code>{@linkplain GenericName}[]</code>&nbsp;</td>
     *     <td nowrap>&nbsp;{@link #getAlias}</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;{@link Identifier#AUTHORITY_KEY "authority"}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link String} or {@link Citation}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link Identifier#getAuthority} on the {@linkplain #getName() name}</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;{@link Identifier#VERSION_KEY "version"}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link String}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link Identifier#getVersion} on the {@linkplain #getName() name}</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;{@link #IDENTIFIERS_KEY "identifiers"}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link Identifier} or <code>{@linkplain Identifier}[]</code>&nbsp;</td>
     *     <td nowrap>&nbsp;{@link #getIdentifiers}</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;{@link #REMARKS_KEY "remarks"}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link String} or {@link InternationalString}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link #getRemarks}</td>
     *   </tr>
     * </table>
     * <P>
     * Additionally, all localizable attributes like {@code "remarks"} may have a language and
     * country code suffix. For example the {@code "remarks_fr"} property stands for remarks in
     * {@linkplain java.util.Locale#FRENCH French} and the {@code "remarks_fr_CA"} property stands
     * for remarks in {@linkplain java.util.Locale#CANADA_FRENCH French Canadian}.
     * <P>
     * Note that the {@code "authority"} and {@code "version"} properties are ignored if the
     * {@code "name"} property is already a {@link Citation} object instead of a {@link String}.
     *
     * @throws InvalidParameterValueException if a property has an invalid value.
     * @throws IllegalArgumentException if a property is invalid for some other reason.
     */
    public AbstractIdentifiedObject(final Map properties) throws IllegalArgumentException {
        this(properties, null, null);
    }

    /**
     * Constructs an object from a set of properties and copy unrecognized properties in the
     * specified map. The {@code properties} argument is treated as in the {@linkplain
     * AbstractIdentifiedObject#AbstractIdentifiedObject(Map) one argument constructor}. All
     * properties unknow to this {@code AbstractIdentifiedObject} constructor are copied
     * in the {@code subProperties} map, after their key has been normalized (usually
     * lower case, leading and trailing space removed).
     *
     * <P>If {@code localizables} is non-null, then all keys listed in this argument are
     * treated as localizable one (i.e. may have a suffix like "_fr", "_de", etc.). Localizable
     * properties are stored in the {@code subProperties} map as {@link InternationalString}
     * objects.</P>
     *
     * @param properties    Set of properties. Should contains at least {@code "name"}.
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
                    if (key.equals(NAME_KEY)) {
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
                    if (key.equals(ALIAS_KEY)) {
                        alias = NameFactory.toArray(value);
                        continue NEXT_KEY;
                    }
                    break;
                }
                // -----------------------------------------
                // "identifiers": Identifier or Identifier[]
                // -----------------------------------------
                case 1368189162: {
                    if (key.equals(IDENTIFIERS_KEY)) {
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
                    if (key.equals(REMARKS_KEY)) {
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
                if (growable.add(REMARKS_KEY, key, value.toString())) {
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
                Logger.getLogger("org.geotools.referencing").log(Logging.format(
                        Level.WARNING, LoggingKeys.LOCALES_DISCARTED));
            }
        }
        if (subProperties!=null && subGrowables!=null) {
            for (int i=0; i<subGrowables.length; i++) {
                if (subGrowables[i]!=null && !subGrowables[i].getLocales().isEmpty()) {
                    final String prefix = localizables[i];
                    if (subProperties.get(prefix) == null) {
                        subProperties.put(prefix, subGrowables[i]);
                    } else {
                        Logger.getLogger("org.geotools.referencing").log(Logging.format(
                                Level.WARNING, LoggingKeys.LOCALES_DISCARTED));
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
            key=        NAME_KEY; this.name        =          (Identifier) (value=name);
            key=       ALIAS_KEY; this.alias       = asSet((GenericName[]) (value=alias));
            key= IDENTIFIERS_KEY; this.identifiers = asSet( (Identifier[]) (value=identifiers));
            key=     REMARKS_KEY; this.remarks     = (InternationalString) (value=remarks);
        } catch (ClassCastException exception) {
            InvalidParameterValueException e = new InvalidParameterValueException(Errors.format(
                                   ErrorKeys.ILLEGAL_ARGUMENT_$2, key, value), key, value);
            e.initCause(exception);
            throw e;
        }
        ensureNonNull(NAME_KEY, name);
        ensureNonNull(NAME_KEY, name.toString());
    }

    /**
     * The primary name by which this object is identified.
     *
     * @see #getName(Citation)
     */
    public Identifier getName() {
        return name;
    }

    /**
     * An alternative name by which this object is identified.
     *         
     * @return The aliases, or an empty array if there is none.
     *
     * @see #getName(Citation)
     */
    public Collection/*<GenericName>*/ getAlias() {
        return (alias!=null) ? alias : Collections.EMPTY_SET;
    }

    /**
     * An identifier which references elsewhere the object's defining information.
     * Alternatively an identifier by which this object can be referenced.
     *
     * @return This object identifiers, or an empty array if there is none.
     *
     * @see #getIdentifier(Citation)
     */
    public Set/*<Identifier>*/ getIdentifiers() {
        return (identifiers!=null) ? identifiers : Collections.EMPTY_SET;
    }

    /**
     * Comments on or information about this object, including data source information.
     */
    public InternationalString getRemarks(){       
        return remarks;
    }

    /**
     * Returns the informations provided in the specified indentified object as a map of
     * properties. The returned map contains key such as {@link #NAME_KEY NAME_KEY}, and
     * values from methods such as {@link #getName}.
     *
     * @param  info The identified object to view as a properties map.
     * @return An view of the identified object as an immutable map.
     */
    public static Map getProperties(final IdentifiedObject info) {
        return new Properties(info);
    }

    /**
     * Returns the properties to be given to an identified object derived from the specified one.
     * This method is typically used for creating a new CRS identical to an existing one except
     * for axis units. This method returns the same properties than the supplied argument (as of
     * <code>{@linkplain #getProperties(IdentifiedObject) getProperties}(info)</code>), except for
     * the following:
     * <p>
     * <ul>
     *   <li>The {@linkplain #getName() name}'s authority is replaced by the specified one.</li>
     *   <li>All {@linkplain #getIdentifiers identifiers} are removed, because the new object
     *       to be created is probably not endorsed by the original authority.</li>
     * </ul>
     * <p>
     * This method returns a mutable map. Consequently, callers can add their own identifiers
     * directly to this map if they wish.
     *
     * @param  info The identified object to view as a properties map.
     * @param  authority The new authority for the object to be created, or {@code null} if it
     *         is not going to have any declared authority.
     * @return An view of the identified object as a mutable map.
     */
    public static Map getProperties(final IdentifiedObject info, final Citation authority) {
        final Map properties = new HashMap(getProperties(info));
        properties.put(NAME_KEY, new NamedIdentifier(authority, info.getName().getCode()));
        properties.remove(IDENTIFIERS_KEY);
        return properties;
    }

    /**
     * Returns an identifier according the given authority. This method checks first all
     * {@link #getIdentifiers identifiers} in their iteration order. It returns the first
     * identifier with an {@linkplain Identifier#getAuthority identifier authority} title
     * {@linkplain CitationImpl#titleMatches(Citation,Citation) matching} at least one title
     * from the specified authority.
     *
     * @param  authority The authority for the identifier to return.
     * @return The object's identifier, or {@code null} if no identifier matching the specified
     *         authority was found.
     *
     * @since 2.2
     */
    public Identifier getIdentifier(final Citation authority) {
        return getIdentifier0(this, authority);
    }

    /**
     * Returns an identifier according the given authority. This method performs the same search
     * than {@link #getIdentifier(Citation)} on arbitrary implementations of GeoAPI interface.
     *
     * @param  info The object to get the identifier from.
     * @param  authority The authority for the identifier to return.
     * @return The object's identifier, or {@code null} if no identifier matching the specified
     *         authority was found.
     *
     * @since 2.2
     */
    public static Identifier getIdentifier(final IdentifiedObject info, final Citation authority) {
        if (info instanceof AbstractIdentifiedObject) {
            // Gives a chances to subclasses to get their overriden method invoked.
            return ((AbstractIdentifiedObject) info).getIdentifier(authority);
        }
        return getIdentifier0(info, authority);
    }

    /**
     * Implementation of {@link #getIdentifier(Citation)}.
     */
    private static Identifier getIdentifier0(final IdentifiedObject info, final Citation authority) {
        for (final Iterator it=info.getIdentifiers().iterator(); it.hasNext();) {
            final Identifier identifier = (Identifier) it.next();
            if (authority == null) {
                return identifier;
            }
            final Citation infoAuthority = identifier.getAuthority();
            if (infoAuthority != null) {
                if (CitationImpl.titleMatches(authority, infoAuthority)) {
                    return identifier;
                }
            }
        }
        return (authority==null) ? info.getName() : null;
    }

    /**
     * Returns this object's name according the given authority. This method checks first the
     * {@linkplain #getName() primary name}, then all {@link #getAlias() alias} in their iteration
     * order. The objects being examined are {@link Identifier}s or {@link GenericName}s. If a
     * generic name implements the {@code Identifier} interface (e.g. {@link NamedIdentifier}),
     * then the identifier view has precedence.
     * <p>
     * This method returns the {@linkplain Identifier#getCode code} (for identifiers) or the
     * {@linkplain GenericName#asLocalName local name} (for alias) of the first object that
     * meets the following conditions:
     * <p>
     * <ul>
     *   <li>An {@linkplain Identifier#getAuthority identifier authority} title
     *       {@linkplain CitationImpl#titleMatches(Citation,Citation) matching}
     *       at least one title from the specified authority.</li>
     *   <li>A {@linkplain GenericName#getScope name scope}
     *       {@linkplain CitationImpl#titleMatches(Citation,String) matching}
     *       at least one title from the specified authority.</li>
     * </ul>
     *
     * @param  authority The authority for the name to return.
     * @return The object's name (either a {@linkplain Identifier#getCode code} or a
     *         {@linkplain GenericName#asLocalName local name}), or {@code null} if no
     *         name matching the specified authority was found.
     *
     * @see #getName()
     * @see #getAlias()
     *
     * @since 2.2
     */
    public String getName(final Citation authority) {
        return getName0(this, authority);
    }

    /**
     * Returns an object's name according the given authority. This method performs the same search
     * than {@link #getName(Citation)} on arbitrary implementations of GeoAPI interface.
     *
     * @param  info The object to get the name from.
     * @param  authority The authority for the name to return.
     * @return The object's name (either a {@linkplain Identifier#getCode code} or a
     *         {@linkplain GenericName#asLocalName local name}), or {@code null} if no
     *         name matching the specified authority was found.
     *
     * @since 2.2
     */
    public static String getName(final IdentifiedObject info, final Citation authority) {
        if (info instanceof AbstractIdentifiedObject) {
            // Gives a chances to subclasses to get their overriden method invoked.
            return ((AbstractIdentifiedObject) info).getName(authority);
        }
        return getName0(info, authority);
    }

    /**
     * Implementation of {@link #getName(Citation)}.
     */
    private static String getName0(final IdentifiedObject info, final Citation authority) {
        Identifier identifier = info.getName();
        if (authority == null) {
            return identifier.getCode();
        }
        String name = null;
        Citation infoAuthority = identifier.getAuthority();
        if (infoAuthority != null) {
            if (CitationImpl.titleMatches(authority, infoAuthority)) {
                name = identifier.getCode();
            } else {
                for (final Iterator it=info.getAlias().iterator(); it.hasNext();) {
                    final GenericName alias = (GenericName) it.next();
                    if (alias instanceof Identifier) {
                        identifier = (Identifier) alias;
                        infoAuthority = identifier.getAuthority();
                        if (infoAuthority != null) {
                            if (CitationImpl.titleMatches(authority, infoAuthority)) {
                                name = identifier.getCode();
                                break;
                            }
                        }
                    } else {
                        final GenericName scope = alias.getScope();
                        if (scope != null) {
                            if (CitationImpl.titleMatches(authority, scope.toString())) {
                                name = alias.asLocalName().toString();
                                break;
                            }
                        }
                    }
                }
            }
        }
        return name;
    }

    /**
     * Returns {@code true} if either the {@linkplain #getName() primary name} or at least
     * one {@linkplain #getAlias alias} matches the specified string. This method performs
     * the search in the following order, regardless of any authority:
     * <ul>
     *   <li>The {@linkplain #getName() primary name} of this object</li>
     *   <li>The {@linkplain ScopedName fully qualified name} of an alias</li>
     *   <li>The {@linkplain LocalName local name} of an alias</li>
     * </ul>
     *
     * @param  name The name to compare.
     * @return {@code true} if the primary name of at least one alias
     *         matches the specified {@code name}.
     */
    public boolean nameMatches(final String name) {
        return nameMatches(this, alias, name);
    }

    /**
     * Returns {@code true} if either the {@linkplain #getName() primary name} or at least
     * one {@linkplain #getAlias alias} matches the specified string. This method performs the
     * same check than the {@linkplain #nameMatches(String) non-static method} on arbitrary
     * object implementing the GeoAPI interface.
     *
     * @param  object The object to check.
     * @param  name The name.
     * @return {@code true} if the primary name of at least one alias
     *         matches the specified {@code name}.
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
     * Implementation of {@code nameMatches} method.
     *
     * @param  object The object to check.
     * @param  alias  The list of alias in {@code object} (may be {@code null}).
     *                This method will never modify this list. Consequently, it may be a
     *                direct reference to an internal array.
     * @param  name The name.
     * @return {@code true} if the primary name of at least one alias
     *         matches the specified {@code name}.
     */
    private static boolean nameMatches(final IdentifiedObject object,
                                       final Collection/*<GenericName>*/ alias, String name)
    {
        name = name.trim();
        if (name.equalsIgnoreCase(object.getName().getCode().trim())) {
            return true;
        }
        if (alias != null) {
            for (final Iterator it=alias.iterator(); it.hasNext();) {
                final GenericName asName = (GenericName) it.next();
                final ScopedName asScoped = asName.asScopedName();
                if (asScoped!=null && name.equalsIgnoreCase(asScoped.toString().trim())) {
                    return true;
                }
                if (name.equalsIgnoreCase(asName.asLocalName().toString().trim())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Compares the specified object with this object for equality.
     *
     * @param  object The other object (may be {@code null}).
     * @return {@code true} if both objects are equal.
     */
    public final boolean equals(final Object object) {
        return (object instanceof AbstractIdentifiedObject) &&
                equals((AbstractIdentifiedObject)object, true);
    }

    /**
     * Compares this object with the specified object for equality.
     *
     * If {@code compareMetadata} is {@code true}, then all available properties are
     * compared including {@linkplain #getName() name}, {@linkplain #getRemarks remarks},
     * {@linkplain #getIdentifiers identifiers code}, etc.
     *
     * If {@code compareMetadata} is {@code false}, then this method compare
     * only the properties needed for computing transformations. In other words,
     * {@code sourceCS.equals(targetCS, false)} returns {@code true} only if
     * the transformation from {@code sourceCS} to {@code targetCS} is
     * the identity transform, no matter what {@link #getName()} saids.
     * <P>
     * Some subclasses (especially {@link org.geotools.referencing.datum.AbstractDatum}
     * and {@link org.geotools.parameter.AbstractParameterDescriptor}) will test for the
     * {@linkplain #getName() name}, since objects with different name have completly
     * different meaning. For example nothing differentiate the {@code "semi_major"} and
     * {@code "semi_minor"} parameters except the name. The name comparaison may be loose
     * however, i.e. we may accept a name matching an alias.
     *
     * @param  object The object to compare to {@code this}.
     * @param  compareMetadata {@code true} for performing a strict comparaison, or
     *         {@code false} for comparing only properties relevant to transformations.
     * @return {@code true} if both objects are equal.
     */
    public boolean equals(final AbstractIdentifiedObject object, final boolean compareMetadata) {
        if (object!=null && object.getClass().equals(getClass())) {
            if (!compareMetadata) {
                return true;
            }
            return Utilities.equals(name,        object.name       ) &&
                   Utilities.equals(alias,       object.alias      ) &&
                   Utilities.equals(identifiers, object.identifiers) &&
                   Utilities.equals(remarks,     object.remarks    );
        }
        return false;
    }

    /**
     * Compares two Geotools's {@code AbstractIdentifiedObject} objects for equality. This
     * method is equivalent to {@code object1.<b>equals</b>(object2, <var>compareMetadata</var>)}
     * except that one or both arguments may be null. This convenience method is provided for
     * implementation of {@code equals} in subclasses.
     *
     * @param  object1 The first object to compare (may be {@code null}).
     * @param  object2 The second object to compare (may be {@code null}).
     * @param  compareMetadata {@code true} for performing a strict comparaison, or
     *         {@code false} for comparing only properties relevant to transformations.
     * @return {@code true} if both objects are equal.
     */
    static boolean equals(final AbstractIdentifiedObject object1,
                          final AbstractIdentifiedObject object2,
                          final boolean          compareMetadata)
    {
        return (object1==object2) || (object1!=null && object1.equals(object2, compareMetadata));
    }

    /**
     * Compares two OpenGIS's {@code IdentifiedObject} objects for equality. This convenience
     * method is provided for implementation of {@code equals} in subclasses.
     *
     * @param  object1 The first object to compare (may be {@code null}).
     * @param  object2 The second object to compare (may be {@code null}).
     * @param  compareMetadata {@code true} for performing a strict comparaison, or
     *         {@code false} for comparing only properties relevant to transformations.
     * @return {@code true} if both objects are equal.
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
     * Compares two arrays of OpenGIS's {@code IdentifiedObject} objects for equality. This
     * convenience method is provided for implementation of {@code equals} method in subclasses.
     *
     * @param  array1 The first array to compare (may be {@code null}).
     * @param  array2 The second array to compare (may be {@code null}).
     * @param  compareMetadata {@code true} for performing a strict comparaison, or
     *         {@code false} for comparing only properties relevant to transformations.
     * @return {@code true} if both arrays are equal.
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
     * Compares two collectionss of OpenGIS's {@code IdentifiedObject} objects for equality.
     * The comparaison take order in account, which make it more appropriate for {@link List}
     * or {@link LinkedHashSet} comparaisons. This convenience method is provided for
     * implementation of {@code equals} method in subclasses.
     *
     * @param  collection1 The first collection to compare (may be {@code null}).
     * @param  collection2 The second collection to compare (may be {@code null}).
     * @param  compareMetadata {@code true} for performing a strict comparaison, or
     *         {@code false} for comparing only properties relevant to transformations.
     * @return {@code true} if both collections are equal.
     */
    protected static boolean equals(final Collection/*<? extends IdentifiedObject>*/ collection1,
                                    final Collection/*<? extends IdentifiedObject>*/ collection2,
                                    final boolean compareMetadata)
    {
        if (collection1 == collection2) {
            return true;
        }
        if (collection1==null || collection2==null) {
            return false;
        }
        final Iterator it1 = collection1.iterator();
        final Iterator it2 = collection2.iterator();
        while (it1.hasNext()) {
            if (!it2.hasNext() ||
                !equals((IdentifiedObject) it1.next(),
                        (IdentifiedObject) it2.next(), compareMetadata))
            {
                return false;
            }
        }
        return !it2.hasNext();
    }

    /**
     * Compares two objects for order. Any object may be null. This method is
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
     * Returns a hash value for this identified object. {@linkplain #getName() Name},
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
     * Returns the specified array as an immutable set, or {@code null} if the
     * array is empty or null. This is a convenience method for sub-classes
     * constructors.
     *
     * @param  array The array to copy in a set. May be {@code null}.
     * @return A set containing the array elements, or {@code null} if none or empty.
     */
    protected static Set asSet(final Object[] array) {
        if (array == null) {
            return null;
        }
        switch (array.length) {
            case 0:  return null;
            case 1:  return Collections.singleton(array[0]);
            default: return Collections.unmodifiableSet(new LinkedHashSet(Arrays.asList(array)));
        }
        
    }
    
    /**
     * Makes sure that an argument is non-null. This is a
     * convenience method for subclass constructors.
     *
     * @param  name   Argument name.
     * @param  object User argument.
     * @throws InvalidParameterValueException if {@code object} is null.
     */
    protected static void ensureNonNull(final String name, final Object object)
            throws IllegalArgumentException
    {
        if (object == null) {
            throw new InvalidParameterValueException(Errors.format(
                        ErrorKeys.NULL_ARGUMENT_$1, name), name, object);
        }
    }
    
    /**
     * Makes sure an array element is non-null. This is
     * a convenience method for subclass constructors.
     *
     * @param  name  Argument name.
     * @param  array User argument.
     * @param  index Index of the element to check.
     * @throws InvalidParameterValueException if {@code array[i]} is null.
     */
    protected static void ensureNonNull(final String name, final Object[] array, final int index)
            throws IllegalArgumentException
    {
        if (array[index] == null) {
            throw new InvalidParameterValueException(Errors.format(
                        ErrorKeys.NULL_ARGUMENT_$1, name+'['+index+']'), name, array);
        }
    }
    
    /**
     * Makes sure that the specified unit is a temporal one.
     * This is a convenience method for subclass constructors.
     *
     * @param  unit Unit to check.
     * @throws IllegalArgumentException if {@code unit} is not a temporal unit.
     */
    protected static void ensureTimeUnit(final Unit unit) throws IllegalArgumentException {
        if (!SI.SECOND.isCompatible(unit)) {
            throw new IllegalArgumentException(Errors.format(ErrorKeys.NON_TEMPORAL_UNIT_$1, unit));
        }
    }
    
    /**
     * Makes sure that the specified unit is a linear one.
     * This is a convenience method for subclass constructors.
     *
     * @param  unit Unit to check.
     * @throws IllegalArgumentException if {@code unit} is not a linear unit.
     */
    protected static void ensureLinearUnit(final Unit unit) throws IllegalArgumentException {
        if (!SI.METER.isCompatible(unit)) {
            throw new IllegalArgumentException(Errors.format(ErrorKeys.NON_LINEAR_UNIT_$1, unit));
        }
    }
    
    /**
     * Makes sure that the specified unit is an angular one.
     * This is a convenience method for subclass constructors.
     *
     * @param  unit Unit to check.
     * @throws IllegalArgumentException if {@code unit} is not an angular unit.
     */
    protected static void ensureAngularUnit(final Unit unit) throws IllegalArgumentException {
        if (!SI.RADIAN.isCompatible(unit) && !Unit.ONE.equals(unit)) {
            throw new IllegalArgumentException(Errors.format(ErrorKeys.NON_ANGULAR_UNIT_$1, unit));
        }
    }
}
