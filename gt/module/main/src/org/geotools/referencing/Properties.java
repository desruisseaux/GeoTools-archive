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
import java.util.Set;
import java.util.HashSet;
import java.util.AbstractMap;

// OpenGIS dependencies
import org.opengis.referencing.IdentifiedObject;

// Geotools dependencies
import org.geotools.util.MapEntry;


/**
 * An immutable map fetching all properties from the specified identified object.
 * Calls to <code>get</code> methods are forwarded to the appropriate
 * {@link IdentifiedObject} method.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class Properties extends AbstractMap {
    /**
     * The object where all properties come from.
     */
    private final IdentifiedObject info;

    /**
     * The entries set. Will be constructed only when first needed.
     */
    private transient Set entries;
    
    /**
     * Creates new properties from the specified identified object.
     */
    public Properties(final IdentifiedObject info) {
        this.info = info;
    }

    /**
     * Returns true if this map contains a mapping for the specified key.
     */
    public boolean containsKey(final Object key) {
        return get(key) != null;
    }

    /**
     * Returns the value to which this map maps the specified key.
     * Returns null if the map contains no mapping for this key.
     */
    public Object get(final Object key) {
        if (key instanceof String) {
            final String s = ((String) key).trim();
            for (int i=0; i<KEYS.length; i++) {
                if (KEYS[i].equalsIgnoreCase(s)) {
                    return get(i);
                }
            }
        }
        return null;
    }

    /**
     * Returns the value to which this map maps the specified index.
     * Returns null if the map contains no mapping for this index.
     */
    private Object get(final int key) {
        switch (key) {
            case  0: return info.getName();
            case  1: return info.getIdentifiers();
            case  2: return info.getAlias();
            case  3: return info.getRemarks();
            default: return null;
        }
    }

    /**
     * The keys to search for. <STRONG>The index of each element in this array
     * must matches the index searched by {@link #get(int)}.</STRONG>
     *
     * @todo Add properties for {@link IdentifiedObject} sub-interfaces.
     */
    private static final String[] KEYS = {
        org.geotools.referencing.IdentifiedObject.NAME_PROPERTY,
        org.geotools.referencing.IdentifiedObject.IDENTIFIERS_PROPERTY,
        org.geotools.referencing.IdentifiedObject.ALIAS_PROPERTY,
        org.geotools.referencing.IdentifiedObject.REMARKS_PROPERTY
    };

    /**
     * Returns a set view of the mappings contained in this map.
     */
    public Set entrySet() {
        if (entries == null) {
            entries = new HashSet(Math.round(KEYS.length/0.75f)+1, 0.75f);
            for (int i=0; i<KEYS.length; i++) {
                final String key = KEYS[i];
                final Object value = get(i);
                if (value != null) {
                    entries.add(new MapEntry(key, value));
                }
            }
        }
        return entries;
    }
}
