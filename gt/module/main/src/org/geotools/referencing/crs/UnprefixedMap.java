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


/**
 * A map without the <code>"conversion."</code> prefix in front of property keys.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class UnprefixedMap extends DerivedMap {
    /**
     * The prefix to remove for this map.
     */
    private final String prefix;

    /**
     * Creates a new unprefixed map from the specified base map and prefix to remove.
     *
     * @param base The base map.
     * @param prefix The prefix to remove from the keys in the base map.
     *               <strong>Must</strong> be lower case.
     */
    public UnprefixedMap(final Map base, final String prefix) {
        super(base);
        this.prefix = prefix;
        assert prefix.equals(prefix.toLowerCase()) : prefix;
    }

    /**
     * Remove the prefix from the specified key. If the key doesn't begins with
     * the prefix, then this method returns <code>null</code>.
     *
     * @param  key A ley from the {@linkplain #base} map.
     * @return The key that this view should contains instead of <code>key</code>,
     *         or <code>null</code>.
     */
    protected Object baseToDerived(final Object key) {
        final String baseKey = key.toString().trim();
        if (baseKey.toLowerCase().startsWith(prefix)) {
            return baseKey.substring(prefix.length());
        }
        return null;
    }

    /**
     * Add the prefix to the specified key.
     *
     * @param  key A key in this map.
     * @return The key stored in the {@linkplain #base} map.
     */
    protected Object derivedToBase(Object key) {
        return prefix + key;
    }
}
