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
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

// OpenGIS dependencies
import org.opengis.metadata.Identifier;
import org.opengis.metadata.citation.Citation;
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.util.GenericName;
import org.opengis.util.InternationalString;
import org.opengis.util.LocalName;
import org.opengis.util.NameSpace;
import org.opengis.util.ScopedName;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Logging;
import org.geotools.resources.i18n.LoggingKeys;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.util.GrowableInternationalString;
import org.geotools.util.WeakValueHashMap;


/**
 * An identification of a CRS object. The main interface implemented by this class
 * is {@link Identifier}. However, this class also implements {@link GenericName} in
 * order to make it possible to give identifiers in the list of
 * {@linkplain AbstractIdentifiedObject#getAlias aliases}. Casting an alias's
 * {@linkplain GenericName generic name} to an {@linkplain Identifier identifier}
 * gives access to more informations, like the URL of the authority.
 * <P>
 * The {@linkplain GenericName generic name} will be infered from {@linkplain Identifier identifier}
 * attributes. More specifically, a {@linkplain ScopedName scoped name} will be constructed using
 * the shortest authority's {@linkplain Citation#getAlternateTitles alternate titles} (or
 * the {@linkplain Citation#getTitle main title} if there is no alternate titles) as the
 * {@linkplain ScopedName#getScope scope}, and the {@linkplain #getCode code} as the
 * {@linkplain ScopedName#asLocalName head}. This heuristic rule seems raisonable
 * since, according ISO 19115, the {@linkplain Citation#getAlternateTitles alternate
 * titles} often contains abreviation (for example "DCW" as an alternative title for
 * "<cite>Digital Chart of the World</cite>").
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class NamedIdentifier implements Identifier, GenericName, Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 8474731565582774497L;

    /**
     * @todo Replace by static import once we are allowed to compile for J2SE 1.5.
     */
    private static final String REMARKS_KEY = org.opengis.referencing.IdentifiedObject.REMARKS_KEY;

    /**
     * A pool of {@link LocalName} values for given {@link InternationalString}.
     * Will be constructed only when first needed.
     */
    private static Map SCOPES;

    /**
     * Identifier code or name, optionally from a controlled list or pattern
     * defined by a code space.
     */
    private final String code;

    /**
     * Organization or party responsible for definition and maintenance of the
     * code space or code.
     */
    private final Citation authority;

    /**
     * Identifier of the version of the associated code space or code, as specified
     * by the code space or code authority. This version is included only when the
     * {@linkplain #getCode code} uses versions. When appropriate, the edition is
     * identified by the effective date, coded using ISO 8601 date format.
     */
    private final String version;

    /**
     * Comments on or information about this identifier, or {@code null} if none.
     */
    private final InternationalString remarks;

    /**
     * The name of this identifier as a generic name. If {@code null}, will
     * be constructed only when first needed. This field is serialized (instead
     * of being recreated after deserialization) because it may be a user-supplied
     * value.
     */
    private GenericName name;

    /**
     * Constructs an identifier from a set of properties. Keys are strings from the table below.
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
     *     <td nowrap>&nbsp;{@link #CODE_KEY "code"}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link String}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link #getCode}</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;{@link #AUTHORITY_KEY "authority"}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link String} or {@link Citation}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link #getAuthority}</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;{@link #VERSION_KEY "version"}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link String}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link #getVersion}</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;{@link #REMARKS_KEY "remarks"}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link String} or {@link InternationalString}&nbsp;</td>
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
    public NamedIdentifier(final Map properties) throws IllegalArgumentException {
        this(properties, true);
    }

    /**
     * Constructs an identifier from an authority and code informations. This is a convenience
     * constructor for commonly-used parameters. If more control are wanted (for example adding
     * remarks), use the {@linkplain #NamedIdentifier(Map) constructor with a properties map}.
     *
     * @param authority The authority (e.g. {@link Citations#OGC OGC} or {@link Citations#EPSG EPSG}).
     * @param code      The code. The {@linkplain Locale#US English name} is used
     *                  for the code, and the international string is used for the
     *                  {@linkplain GenericName generic name}.
     */
    public NamedIdentifier(final Citation authority, final InternationalString code) {
        this(authority, code.toString(Locale.US));
        name = getName(authority, code);
    }

    /**
     * Constructs an identifier from an authority and code informations. This is a convenience
     * constructor for commonly-used parameters. If more control are wanted (for example adding
     * remarks), use the {@linkplain #NamedIdentifier(Map) constructor with a properties map}.
     *
     * @param authority The authority (e.g. {@link Citations#OGC OGC} or {@link Citations#EPSG EPSG}).
     * @param code      The code. This parameter is mandatory.
     */
    public NamedIdentifier(final Citation authority, final String code) {
        this(authority, code, null);
    }

    /**
     * Constructs an identifier from an authority and code informations. This is a convenience
     * constructor for commonly-used parameters. If more control are wanted (for example adding
     * remarks), use the {@linkplain #NamedIdentifier(Map) constructor with a properties map}.
     *
     * @param authority The authority (e.g. {@link Citations#OGC OGC} or {@link Citations#EPSG EPSG}).
     * @param code      The code. This parameter is mandatory.
     * @param version   The version, or {@code null} if none.
     */
    public NamedIdentifier(final Citation authority, final String code, final String version) {
        this(toMap(authority, code, version));
    }

    /**
     * Work around for RFE #4093999 in Sun's bug database
     * ("Relax constraint on placement of this()/super() call in constructors").
     */
    private static Map toMap(final Citation authority,
                             final String   code,
                             final String   version)
    {
        final Map properties = new HashMap(4);
        if (authority != null) properties.put(AUTHORITY_KEY, authority);
        if (code      != null) properties.put(     CODE_KEY, code     );
        if (version   != null) properties.put(  VERSION_KEY, version  );
        return properties;
    }

    /**
     * Implementation of the constructor. The remarks in the {@code properties} will be
     * parsed only if the {@code standalone} argument is set to {@code true}, i.e.
     * this identifier is being constructed as a standalone object. If {@code false}, then
     * this identifier is assumed to be constructed from inside the {@link AbstractIdentifiedObject}
     * constructor.
     *
     * @param properties The properties to parse, as described in the public constructor.
     * @param standalone {@code true} for parsing "remarks" as well.
     *
     * @throws InvalidParameterValueException if a property has an invalid value.
     * @throws IllegalArgumentException if a property is invalid for some other reason.
     */
    NamedIdentifier(final Map properties, final boolean standalone) throws IllegalArgumentException {
        ensureNonNull("properties", properties);
        Object code      = null;
        Object version   = null;
        Object authority = null;
        Object remarks   = null;
        GrowableInternationalString growable = null;
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
                case 3373707: {
                    if (!standalone && key.equals("name")) {
                        code = value;
                        continue;
                    }
                    break;
                }
                case 3059181: {
                    if (key.equals(CODE_KEY)) {
                        code = value;
                        continue;
                    }
                    break;
                }
                case 351608024: {
                    if (key.equals(VERSION_KEY)) {
                        version = value;
                        continue;
                    }
                    break;
                }
                case 1475610435: {
                    if (key.equals(AUTHORITY_KEY)) {
                        if (value instanceof String) {
                            value = Citations.fromName(value.toString());
                        }
                        authority = value;
                        continue;
                    }
                    break;
                }
                case 1091415283: {
                    if (standalone && key.equals(REMARKS_KEY)) {
                        if (value instanceof InternationalString) {
                            remarks = value;
                            continue;
                        }
                    }
                    break;
                }
            }
            /*
             * Search for additional locales (e.g. "remarks_fr").
             */
            if (standalone && value instanceof String) {
                if (growable == null) {
                    if (remarks instanceof GrowableInternationalString) {
                        growable = (GrowableInternationalString) remarks;
                    } else {
                        growable = new GrowableInternationalString();
                    }
                }
                growable.add(REMARKS_KEY, key, value.toString());
            }
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
                Logger.getLogger("org.geotools.referencing").log(
                                 Logging.format(Level.WARNING, LoggingKeys.LOCALES_DISCARTED));
            }
        }
        /*
         * Stores the definitive reference to the attributes. Note that casts are performed only
         * there (not before). This is a wanted feature, since we want to catch ClassCastExceptions
         * are rethrown them as more informative exceptions.
         */
        try {
            key=      CODE_KEY; this.code      = (String)              (value=code);
            key=   VERSION_KEY; this.version   = (String)              (value=version);
            key= AUTHORITY_KEY; this.authority = (Citation)            (value=authority);
            key=   REMARKS_KEY; this.remarks   = (InternationalString) (value=remarks);
        } catch (ClassCastException exception) {
            InvalidParameterValueException e = new InvalidParameterValueException(Errors.format(
                                   ErrorKeys.ILLEGAL_ARGUMENT_$2, key, value), key, value);
            e.initCause(exception);
            throw e;
        }
        ensureNonNull(CODE_KEY, code);
    }
    
    /**
     * Makes sure an argument is non-null. This is method duplicate
     * {@link AbstractIdentifiedObject#ensureNonNull(String, Object)}
     * except for the more accurate stack trace. It is duplicated
     * there in order to avoid a dependency to {@link AbstractIdentifiedObject}.
     *
     * @param  name   Argument name.
     * @param  object User argument.
     * @throws InvalidParameterValueException if {@code object} is null.
     */
    private static void ensureNonNull(final String name, final Object object)
        throws IllegalArgumentException
    {
        if (object == null) {
            throw new InvalidParameterValueException(Errors.format(
                        ErrorKeys.NULL_ARGUMENT_$1, name), name, object);
        }
    }

    /**
     * Identifier code or name, optionally from a controlled list or pattern.
     *
     * @return The code.
     */
    public String getCode() {
        return code;
    }

    /**
     * Organization or party responsible for definition and maintenance of the
     * {@linkplain #getCode code}.
     *
     * @return The authority, or {@code null} if not available.
     */
    public Citation getAuthority() {
        return authority;
    }

    /**
     * Identifier of the version of the associated code space or code, as specified by the
     * code authority. This version is included only when the {@linkplain #getCode code}
     * uses versions. When appropriate, the edition is identified by the effective date,
     * coded using ISO 8601 date format.
     *
     * @return The version, or {@code null} if not available.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Comments on or information about this identifier, or {@code null} if none.
     */
    public InternationalString getRemarks() {
        return remarks;
    }
    
    /**
     * Returns the generic name of this identifier. The name will be constructed
     * automatically the first time it will be needed. The name's scope is infered
     * from the shortest alternative title (if any). This heuristic rule seems raisonable
     * since, according ISO 19115, the {@linkplain Citation#getAlternateTitles alternate
     * titles} often contains abreviation (for example "DCW" as an alternative title for
     * "Digital Chart of the World"). If no alternative title is found or if the main title
     * is yet shorter, then it is used.
     */
    private GenericName getName() {
        // No need to synchronize; this is not a big deal if the name is created twice.
        if (name == null) {
            name = getName(authority, code);
        }
        return name;
    }

    /**
     * Constructs a generic name from the specified authority and code.
     */
    private static GenericName getName(final Citation authority, final CharSequence code) {
        if (authority == null) {
            return new org.geotools.util.LocalName(code);
        }
        InternationalString title = authority.getTitle();
        int length = title.length();
        final Collection alt = authority.getAlternateTitles();
        if (alt != null) {
            for (final Iterator it=alt.iterator(); it.hasNext();) {
                final InternationalString candidate = (InternationalString) it.next();
                final int candidateLength = candidate.length();
                if (candidateLength>0 && candidateLength<length) {
                    title = candidate;
                    length = candidateLength;
                }
            }
        }
        GenericName scope;
        synchronized (NamedIdentifier.class) {
            if (SCOPES == null) {
                SCOPES = new WeakValueHashMap();
            }
            scope = (GenericName) SCOPES.get(title);
            if (scope == null) {
                scope = new org.geotools.util.LocalName(title);
                SCOPES.put(title, scope);
            }
        }
        return new org.geotools.util.ScopedName(scope, code);
    }
    
    /**
     * Returns the scope (name space) of this generic name. If this name has no scope
     * (e.g. is the root), then this method returns {@code null}.
     * 
     * @deprecated Repalced by scope()
     */
    public GenericName getScope() {
        return getName().getScope();
    }
    
    /**
     * Returns a view of this object as a scoped name,
     * or {@code null} if this name has no scope.
     */
    public ScopedName asScopedName() {
        return getName().asScopedName();
    }
    
    /**
     * Returns a view of this object as a local name. The local name returned by this method
     * will have the same {@linkplain LocalName#getScope scope} than this generic name.
     */
    public LocalName asLocalName() {
        return getName().asLocalName();
    }
    
    /**
     * Returns the sequence of {@linkplain LocalName local names} making this generic name.
     * Each element in this list is like a directory name in a file path name.
     * The length of this sequence is the generic name depth.
     */
    public List getParsedNames() {
        return getName().getParsedNames();
    }
    
    /**
     * Returns a local-dependent string representation of this generic name. This string
     * is similar to the one returned by {@link #toString} except that each element has
     * been localized in the {@linkplain InternationalString#toString(Locale) specified locale}.
     * If no international string is available, then this method returns an implementation mapping
     * to {@link #toString} for all locales.
     */
    public InternationalString toInternationalString() {
        return getName().toInternationalString();
    }

    /**
     * Returns a string representation of this generic name. This string representation
     * is local-independant. It contains all elements listed by {@link #getParsedNames}
     * separated by an arbitrary character (usually {@code :} or {@code /}).
     */
    public String toString() {
        return getName().toString();
    }

    /**
     * Compares this name with the specified object for order. Returns a negative integer,
     * zero, or a positive integer as this name lexicographically precedes, is equals to,
     * or follows the specified object.
     */
    public int compareTo(final Object object) {
        return getName().compareTo(object);
    }

    /**
     * Compares this identifier with the specified object for equality.
     */
    public boolean equals(final Object object) {
        if (object!=null && object.getClass().equals(getClass())) {
            final NamedIdentifier that = (NamedIdentifier) object;
            return Utilities.equals(this.code,      that.code     ) &&
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
        int hash = (int)serialVersionUID;
        if (code != null) {
            hash ^= code.hashCode();
        }
        if (version != null) {
            hash = hash*37 + version.hashCode();
        }
        return hash;
    }

    /**
     * @since GeoAPI 2.1
     */
        public NameSpace scope() {
                return getName().scope();
        }
        /**
         * @since GeoAPI 2.1
         */
        public int depth() {
                return getName().depth();
        }
        /** @since GeoAPI 2.1 */
        public LocalName name() {
                return getName().name();
        }
        /** @since GeoAPI 2.1 */
        public GenericName toFullyQualifiedName() {
                return getName().toFullyQualifiedName();
        }
        /** @since GeoAPI 2.1 */
        public ScopedName push(GenericName scope) {
                return getName().push( scope );
        }
}
