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
import java.util.Locale;   // For javadoc
import java.util.Iterator;
import java.util.logging.Logger;
import java.io.Serializable;
import java.io.ObjectStreamException;

// OpenGIS dependencies
import org.opengis.util.InternationalString;
import org.opengis.metadata.citation.Citation;
import org.opengis.parameter.InvalidParameterValueException;

// Geotools dependencies
import org.geotools.util.WeakHashSet;
import org.geotools.util.GrowableInternationalString;
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * An identification of a CRS object.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class Identifier implements org.opengis.metadata.Identifier, Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 8474731565582774497L;

    /**
     * Key for the <code>"code"</code> property to be given to the
     * {@linkplain #Identifier(Map) constructor}. This is used
     * for setting the value to be returned by {@link #getCode()}.
     */
    public static final String CODE_PROPERTY = "code";

    /**
     * Key for the <code>"authority"</code> property to be given to the
     * {@linkplain #Identifier(Map) constructor}. This is used
     * for setting the value to be returned by {@link #getAuthority()}.
     */
    public static final String AUTHORITY_PROPERTY = "authority";

    /**
     * Key for the <code>"version"</code> property to be given to the
     * {@linkplain #Identifier(Map) constructor}. This is used
     * for setting the value to be returned by {@link #getVersion()}.
     */
    public static final String VERSION_PROPERTY = "version";
    
    /**
     * Key for the <code>"remarks"</code> property to be given to the
     * {@linkplain #Identifier(Map) constructor}. This is used
     * for setting the value to be returned by {@link #getRemarks()}.
     */
    public static final String REMARKS_PROPERTY = "remarks";

    /**
     * Set of weak references to existing objects (identifiers, CRS, Datum, whatever).
     * This set is used in order to return a pre-existing object instead of creating a
     * new one.
     */
    static final WeakHashSet POOL = new WeakHashSet();

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
     * Comments on or information about this identifier, or <code>null</code> if none.
     */
    private final InternationalString remarks;

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
     *     <td nowrap>&nbsp;{@link #CODE_PROPERTY "code"}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link String}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link #getCode}</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;{@link #AUTHORITY_PROPERTY "authority"}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link String} or {@link Citation}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link #getAuthority}</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;{@link #VERSION_PROPERTY "version"}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link String}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link #getVersion}</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;{@link #REMARKS_PROPERTY "remarks"}&nbsp;</td>
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
    public Identifier(final Map properties) throws IllegalArgumentException {
        this(properties, true);
    }

    /**
     * Constructs an identifier from an authority and code informations. This is a convenience
     * constructor for commonly-used parameters. If more control are wanted (for example adding
     * remarks), use the {@linkplain #Identifier(Map) constructor with a properties map}.
     *
     * @param authority The authority (e.g. {@link org.geotools.metadata.citation.Citation#OPEN_GIS}
     *                  or {@link org.geotools.metadata.citation.Citation#EPSG}).
     * @param code      The code. This parameter is mandatory.
     */
    public Identifier(final Citation authority, final String code) {
        this(authority, code, null);
    }

    /**
     * Constructs an identifier from an authority and code informations. This is a convenience
     * constructor for commonly-used parameters. If more control are wanted (for example adding
     * remarks), use the {@linkplain #Identifier(Map) constructor with a properties map}.
     *
     * @param authority The authority (e.g. {@link org.geotools.metadata.citation.Citation#OPEN_GIS}
     *                  or {@link org.geotools.metadata.citation.Citation#EPSG}).
     * @param code      The code. This parameter is mandatory.
     * @param version   The version, or <code>null</code> if none.
     */
    public Identifier(final Citation authority, final String code, final String version) {
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
        if (authority != null) properties.put(AUTHORITY_PROPERTY, authority);
        if (code      != null) properties.put(     CODE_PROPERTY, code     );
        if (version   != null) properties.put(  VERSION_PROPERTY, version  );
        return properties;
    }

    /**
     * Implementation of the constructor. The remarks in the <code>properties</code> will be
     * parsed only if the <code>standalone</code> argument is set to <code>true</code>, i.e.
     * this identifier is being constructed as a standalone object. If <code>false</code>, then
     * this identifier is assumed to be constructed from inside the {@link IdentifiedObject}
     * constructor.
     *
     * @param properties The properties to parse, as described in the public constructor.
     * @param <code>standalone</code> <code>true</code> for parsing "remarks" as well.
     *
     * @throws InvalidParameterValueException if a property has an invalid value.
     * @throws IllegalArgumentException if a property is invalid for some other reason.
     */
    Identifier(final Map properties, final boolean standalone) throws IllegalArgumentException {
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
                    if (key.equals(CODE_PROPERTY)) {
                        code = value;
                        continue;
                    }
                    break;
                }
                case 351608024: {
                    if (key.equals(VERSION_PROPERTY)) {
                        version = value;
                        continue;
                    }
                    break;
                }
                case 1475610435: {
                    if (key.equals(AUTHORITY_PROPERTY)) {
                        if (value instanceof String) {
                            value = new org.geotools.metadata.citation.Citation(value.toString());
                        }
                        authority = value;
                        continue;
                    }
                    break;
                }
                case 1091415283: {
                    if (standalone && key.equals(REMARKS_PROPERTY)) {
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
                growable.add(REMARKS_PROPERTY, key, value.toString());
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
                Logger.getLogger("org.geotools.referencing").warning(
                                 Resources.format(ResourceKeys.WARNING_LOCALES_DISCARTED));
            }
        }
        /*
         * Stores the definitive reference to the attributes. Note that casts are performed only
         * there (not before). This is a wanted feature, since we want to catch ClassCastExceptions
         * are rethrown them as more informative exceptions.
         */
        try {
            key=      CODE_PROPERTY; this.code      = (String)              (value=code);
            key=   VERSION_PROPERTY; this.version   = (String)              (value=version);
            key= AUTHORITY_PROPERTY; this.authority = (Citation)            (value=authority);
            key=   REMARKS_PROPERTY; this.remarks   = (InternationalString) (value=remarks);
        } catch (ClassCastException exception) {
            InvalidParameterValueException e = new InvalidParameterValueException(Resources.format(
                                   ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2, key, value), key, value);
            e.initCause(exception);
            throw e;
        }
        ensureNonNull(CODE_PROPERTY, code);
    }
    
    /**
     * Makes sure an argument is non-null. This is method duplicate
     * {@link IdentifiedObject#ensureNonNull(String, Object)} except for the more accurate stack
     * trace. It is duplicated there in order to avoid a dependency to {@link IdentifiedObject}.
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
     * @return The authority, or <code>null</code> if not available.
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
     * @return The version, or <code>null</code> if not available.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Comments on or information about this identifier, or <code>null</code> if none.
     */
    public InternationalString getRemarks() {
        return remarks;
    }

    /**
     * Returns a string representation of this identifier. This string is mostly for
     * debugging purpose and is implementation-dependent.
     */
    public String toString() {
        final StringBuffer buffer = new StringBuffer(Utilities.getShortClassName(this));
        buffer.append("[\"");
        if (authority != null) {
            buffer.append(authority.getTitle());
            buffer.append(':');
        }
        buffer.append(code);
        buffer.append('"');
        if (version != null) {
            buffer.append(", version ");
            buffer.append(version);
        }
        buffer.append(']');
        return buffer.toString();
    }

    /**
     * Compares this identifier with the specified object for equality.
     */
    public boolean equals(final Object object) {
        if (object!=null && object.getClass().equals(getClass())) {
            final Identifier that = (Identifier) object;
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
