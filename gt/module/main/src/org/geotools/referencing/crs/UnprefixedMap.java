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
package org.geotools.referencing.crs;

// J2SE dependencies
import java.util.Map;

// Geotools dependencies
import org.geotools.util.DerivedMap;
import org.geotools.referencing.IdentifiedObject;


/**
 * A map without the <code>"conversion."</code> prefix in front of property keys. This
 * implementation performs a special processing for the <code>{@linkplain #prefix}.name</code>
 * key: if it doesn't exists, then the plain <code>name</code> key is used. In other words,
 * this map inherits the <code>"name"</code> property from the {@linkplain #base} map.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class UnprefixedMap extends DerivedMap {
    /**
     * The property key to process in a special way.
     */
    private static final String NAME_PROPERTY = IdentifiedObject.NAME_PROPERTY;

    /**
     * The prefix to remove for this map.
     */
    private final String prefix;

    /**
     * <code>true</code> if the <code>{@linkplain #prefix}.name</code> property exists
     */
    private final boolean hasName;

    /**
     * Creates a new unprefixed map from the specified base map and prefix to remove.
     *
     * @param base   The base map.
     * @param prefix The prefix to remove from the keys in the base map.
     */
    public UnprefixedMap(final Map base, final String prefix) {
        super(base);
        this.prefix  = prefix.trim();
        this.hasName = base.containsKey(this.prefix + NAME_PROPERTY);
    }

    /**
     * Remove the prefix from the specified key. If the key doesn't begins with
     * the prefix, then this method returns <code>null</code>.
     *
     * @param  key A key from the {@linkplain #base} map.
     * @return The key that this view should contains instead of <code>key</code>,
     *         or <code>null</code>.
     */
    protected Object baseToDerived(final Object key) {
        final int length = prefix.length();
        final String textualKey = key.toString().trim();
        if (prefix.regionMatches(true, 0, textualKey, 0, length)) {
            return textualKey.substring(length).trim();
        }
        if (!hasName && isName(textualKey)) {
            return textualKey;
        }
        return null;
    }

    /**
     * Add the prefix to the specified key.
     *
     * @param  key A key in this map.
     * @return The key stored in the {@linkplain #base} map.
     */
    protected Object derivedToBase(final Object key) {
        final String textualKey = key.toString().trim();
        if (!hasName && isName(textualKey)) {
            return textualKey;
        }
        return prefix + textualKey;
    }

    /**
     * Returns <code>true</code> if the specified key is <code>"name"</code>
     * or starts with <code>"name_"</code>
     */
    private static boolean isName(final String key) {
        final int length = NAME_PROPERTY.length();
        return NAME_PROPERTY.regionMatches(true, 0, key, 0, length) &&
               (key.length()==length || key.charAt(length)=='_');
    }
}
