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

// J2SE dependencies
import java.util.List;
import java.util.Arrays;

// OpenGIS dependencies
import org.opengis.util.LocalName;
import org.opengis.util.GenericName;
import org.opengis.util.InternationalString;

// Geotools utilities
import org.geotools.resources.Utilities;


/**
 * Fully qualified identifier for an object.
 * A <code>ScopedName</code> contains a {@link LocalName} as
 * {@linkplain #asLocalName head} and a {@linkplain GenericName},
 * which may be a {@link LocalName} or an other {@link org.opengis.util.ScopedName},
 * as {@linkplain #getScope tail}.
 *
 * <P>Instances of this objects are usually not created directly.
 *    Use one of {@linkplain NameFactory factory methods} instead.</P>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see NameFactory
 */
public class ScopedName extends org.geotools.util.GenericName
                     implements org.opengis.util.ScopedName
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -7664125655784137729L;

    /**
     * The scope of this variable (also know as the "tail").
     */
    private final GenericName scope;

    /**
     * The separator character.
     */
    private final char separator;

    /**
     * The head as a local name.
     */
    private final LocalName name;

    /**
     * The list of parsed names. Will be constructed only when first needed.
     */
    private transient List parsedNames;

    /**
     * Constructs a scoped name from the specified string.
     *
     * @param scope     The scope (or "tail") of the variable.
     * @param separator The separator character (usually <code>':'</code> or <code>'/'</code>).
     * @param name      The head (never <code>null</code>).
     */
    public ScopedName(final GenericName scope, final char separator, final String name) {
        org.geotools.util.InternationalString.ensureNonNull("scope", scope);
        org.geotools.util.InternationalString.ensureNonNull("name",  name);
        this.scope     = scope;
        this.separator = separator;
        this.name      = new org.geotools.util.LocalName(this, name);
    }

    /**
     * Constructs a scoped name from the specified international string.
     *
     * @param scope     The scope (or "tail") of the variable.
     * @param separator The separator character (usually <code>':'</code> or <code>'/'</code>).
     * @param name      The head (never <code>null</code>).
     */
    public ScopedName(final GenericName        scope,
                      final char           separator,
                      final InternationalString name)
    {
        org.geotools.util.InternationalString.ensureNonNull("scope", scope);
        org.geotools.util.InternationalString.ensureNonNull("name",  name);
        this.scope     = scope;
        this.separator = separator;
        this.name      = new org.geotools.util.LocalName(this, name);
    }

    /**
     * Returns the scope of this name.
     */
    public GenericName getScope() {
        return scope;
    }

    /**
     * Returns the separator character.
     */
    public char getSeparator() {
        return separator;
    }

    /**
     * Returns a view of this object as a scoped name. Since this object is already
     * a scoped name, this method always returns <code>this</code>.
     */
    public org.opengis.util.ScopedName asScopedName() {
        return this;
    }

    /**
     * Returns a view of this object as a local name. This is the last element in the
     * sequence of {@linkplain #getParsedNames parsed names}. The local name returned
     * by this method will still have the same {@linkplain LocalName#getScope scope}
     * than this scoped name. Note however that the string returned by
     * {@link LocalName#toString} will differs.
     */
    public LocalName asLocalName() {
        return name;
    }

    /**
     * Returns the sequence of local name for this {@linkplain GenericName generic name}.
     */
    public List getParsedNames() {
        if (parsedNames == null) {
            final List parents = scope.getParsedNames();
            final int size = parents.size();
            GenericName[] names = new GenericName[size + 1];
            names = (GenericName[]) parents.toArray(names);
            names[size] = name;
            parsedNames = Arrays.asList(names);
        }
        return parsedNames;
    }

    /**
     * Compares this scoped name with the specified object for equality.
     */
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final ScopedName that = (ScopedName) object;
            return Utilities.equals(this.name,  that.name);
            // No need to checks the scope, since the LocalName implementation
            // should checks it.
        }
        return false;
    }

    /**
     * Returns a hash code value for this generic name.
     */
    public int hashCode() {
        return (int)serialVersionUID ^ name.hashCode() ^ scope.hashCode();
    }
}
