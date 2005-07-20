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

// J2SE direct dependencies
import java.util.Collections;
import java.util.List;
import java.util.Locale;

// OpenGIS dependencies
import org.opengis.util.GenericName;
import org.opengis.util.InternationalString;
import org.opengis.util.ScopedName;

// Geotools dependencies
import org.geotools.resources.Utilities;


/**
 * Identifier within a name space for a local object. This could be the target object of the
 * {@link GenericName}, or a pointer to another name space (with a new {@link GenericName})
 * one step closer to the target of the identifier.
 *
 * @since 2.1
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see NameFactory
 */
public class LocalName extends org.geotools.util.GenericName implements org.opengis.util.LocalName {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -5627125375582385822L;

    /**
     * The view of this object as a scoped name.
     */
    private final ScopedName asScopedName;

    /**
     * The name, either as a {@link String} or an {@link InternationalString}.
     */
    private final CharSequence name;

    /**
     * The name as a string.
     * If not provided, will be built only when first needed.
     */
    private transient String asString;

    /**
     * The name as an international string.
     * If not provided, will be built only when first needed.
     */
    private transient InternationalString asInternationalString;

    /**
     * The sequence of local name for this {@linkplain GenericName generic name}.
     * Since this object is itself a locale name, this list is always a singleton
     * containing only {@code this}. It will be built only when first needed.
     */
    private transient List parsedNames;

    /**
     * Constructs a local name from the specified string with no scope.
     * If the specified name is an {@link InternationalString}, then the
     * <code>{@linkplain InternationalString#toString(Locale) toString}(null)</code>
     * method will be used in order to fetch an unlocalized name. Otherwise, the
     * <code>{@linkplain CharSequence#toString toString}()</code> method will be used.
     *
     * @param name The local name (never {@code null}).
     */
    public LocalName(final CharSequence name) {
        this(null, name);
    }

    /**
     * Constructs a local name from the specified international string.
     *
     * This constructor is not public since it can't be used from outside
     * of {@link org.geotools.util.ScopedName} constructor (otherwise some
     * methods in this class may have the wrong semantic).
     *
     * @param asScopedName The view of this object as a scoped name.
     * @param name         The local name (never {@code null}).
     */
    LocalName(final ScopedName asScopedName, final CharSequence name) {
        this.asScopedName = asScopedName;
        this.name         = validate(name);
        AbstractInternationalString.ensureNonNull("name", name);
    }

    /**
     * Returns the scope (name space) of this generic name.
     * This method is protected from overriding by the user.
     */
    private GenericName getInternalScope() {
        return (asScopedName!=null) ? asScopedName.getScope() : null;
    }

    /**
     * Returns the scope (name space) of this generic name. This method returns the same
     * value than the one returned by the {@linkplain ScopedName scoped}
     * version of this name. In other words, the following relation shall be respected:
     * <blockquote><table border='0'><tr>
     *   <td nowrap>{@link ScopedName#asLocalName}</td>
     *   <td nowrap>{@code .getScope() ==}</td>
     *   <td nowrap align="right">{@link ScopedName}</td>
     *   <td nowrap>{@code .getScope()}</td>
     * </tr><tr>
     *   <td align="center"><font size=2>(a locale name)</font></td>
     *   <td>&nbsp;</td>
     *   <td align="center"><font size=2>(a scoped name)</font></td>
     *   <td>&nbsp;</td>
     * </tr></table></blockquote>
     */
    public GenericName getScope() {
        return getInternalScope();
    }

    /**
     * Returns the sequence of local name for this {@linkplain GenericName generic name}.
     * Since this object is itself a locale name, this method always returns a singleton
     * containing only {@code this}.
     */
    public List getParsedNames() {
        // No need to sychronize: it is not a big deal if this object is built twice.
        if (parsedNames == null) {
            parsedNames = Collections.singletonList(this);
        }
        return parsedNames;
    }

    /**
     * Returns a view of this object as a scoped name,
     * or {@code null} if this name has no scope.
     */
    public ScopedName asScopedName() {
        return asScopedName;
    }

    /**
     * Returns a view of this object as a local name. Since this object is already
     * a local name, this method always returns {@code this}.
     */
    public org.opengis.util.LocalName asLocalName() {
        return this;
    }

    /**
     * Returns a locale-independant string representation of this local name.
     * This string do not includes the scope, which is consistent with the
     * {@linkplain #getParsedNames parsed names} definition.
     */
    public String toString() {
        if (asString == null) {
            if (name instanceof InternationalString) {
                // We really want the 'null' locale, not the system default one.
                asString = ((InternationalString) name).toString(null);
            } else {
                asString = name.toString();
            }
        }
        return asString;
    }

    /**
     * Returns a local-dependent string representation of this locale name.
     */
    public InternationalString toInternationalString() {
        if (asInternationalString == null) {
            if (name instanceof InternationalString) {
                asInternationalString = (InternationalString) name;
            } else {
                asInternationalString = new SimpleInternationalString(name.toString());
            }
        }
        return asInternationalString;
    }

    /**
     * Compares this name with the specified object for order. Returns a negative integer,
     * zero, or a positive integer as this name lexicographically precedes, is equals to,
     * or follows the specified object. The comparaison is case-insensitive.
     */
    public int compareTo(final Object object) {
        return toString().compareToIgnoreCase(object.toString());
    }

    /**
     * Compares this local name with the specified object for equality.
     */
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final LocalName that = (LocalName) object;
            // Do not use 'asScopedName' in order to avoid never-ending loop.
            return Utilities.equals(this.getInternalScope(), that.getInternalScope()) &&
                   Utilities.equals(this.name,               that.name);
        }
        return false;
    }

    /**
     * Returns a hash code value for this local name.
     */
    public int hashCode() {
        int code = (int)serialVersionUID;
        // Do not use 'asScopedName' in order to avoid never-ending loop.
        if (name != null) code ^= name.hashCode();
        return code;
    }
}
