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
package org.geotools.util;

// J2SE dependencies
import java.util.Set;
import java.util.Map;
import java.util.Collection;
import java.util.AbstractMap;
import java.io.Serializable;


/**
 * A map whose keys are derived from an other map. The keys are derived only when
 * requested, which make it possible to backup potentially large maps. Implementations
 * need only to overrides {@link #baseToDerived} and {@link #derivedToBase} methods.
 * This set do not supports <code>null</code> key, since <code>null</code> is used
 * when no mapping from {@linkplain #base} to <code>this</code> exists.
 * This class is serializable if the underlying {@linkplain #base} set is serializable
 * too.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class DerivedMap extends AbstractMap implements Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -6994867383669885934L;

    /**
     * The base map whose keys are derived from.
     *
     * @see #baseToDerived
     * @see #derivedToBase
     */
    protected final Map base;

    /**
     * Key set. Will be constructed only when first needed.
     *
     * @see #keySet
     */
    private transient Set keySet;

    /**
     * Entry set. Will be constructed only when first needed.
     *
     * @see #entrySet
     */
    private transient Set entrySet;

    /**
     * Creates a new derived map from the specified base map.
     *
     * @param base The base map.
     */
    public DerivedMap(final Map base) {
        this.base = base;
    }

    /**
     * Transforms a key from the {@linkplain #base} map to a key in this map.
     * If there is no key in the derived map for the specified base key,
     * then this method returns <code>null</code>.
     *
     * @param  key A ley from the {@linkplain #base} map.
     * @return The key that this view should contains instead of <code>key</code>,
     *         or <code>null</code>.
     */
    protected abstract Object baseToDerived(final Object key);

    /**
     * Transforms a key from this derived map to a key in the {@linkplain #base} map.
     *
     * @param  key A key in this map.
     * @return The key stored in the {@linkplain #base} map.
     */
    protected abstract Object derivedToBase(final Object element);

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map.
     */
    public int size() {
	return super.size();
    }

    /**
     * Returns <code>true</code> if this map contains no key-value mappings.
     *
     * @return <code>true</code> if this map contains no key-value mappings.
     */
    public boolean isEmpty() {
	return base.isEmpty() || super.isEmpty();
    }

    /**
     * Returns <code>true</code> if this map maps one or more keys to this value.
     * The default implementation invokes
     * <code>{@linkplain #base}.containsValue(value)</code>.
     * 
     * @return <code>true</code> if this map maps one or more keys to this value.
     */
    public boolean containsValue(final Object value) {
        return base.containsValue(value);
    }

    /**
     * Returns <code>true</code> if this map contains a mapping for the specified key.
     * The default implementation invokes
     * <code>{@linkplain #base}.containsKey({@linkplain #derivedToBase derivedToBase}(key))</code>.
     *
     * @param  key key whose presence in this map is to be tested.
     * @return <code>true</code> if this map contains a mapping for the specified key.
     */
    public boolean containsKey(final Object key) {
        return base.containsKey(derivedToBase(key));
    }

    /**
     * Returns the value to which this map maps the specified key.
     * The default implementation invokes
     * <code>{@linkplain #base}.get({@linkplain #derivedToBase derivedToBase}(key))</code>.
     *
     * @param  key key whose associated value is to be returned.
     * @return the value to which this map maps the specified key.
     */
    public Object get(final Object key) {
        return base.get(derivedToBase(key));
    }

    /**
     * Associates the specified value with the specified key in this map.
     * The default implementation invokes
     * <code>{@linkplain #base}.put({@linkplain #derivedToBase derivedToBase}(key), value)</code>.
     *
     * @param  key key with which the specified value is to be associated.
     * @param  value value to be associated with the specified key.
     * @return previous value associated with specified key, or <code>null</code>
     *	       if there was no mapping for key.
     * @throws UnsupportedOperationException if the {@linkplain #base} map doesn't
     *         supports the <code>put</code> operation.
     */
    public Object put(final Object key, final Object value) throws UnsupportedOperationException {
        return base.put(derivedToBase(key), value);
    }

    /**
     * Removes the mapping for this key from this map if present.
     * The default implementation invokes
     * <code>{@linkplain #base}.remove({@linkplain #derivedToBase derivedToBase}(key))</code>.
     *
     * @param  key key whose mapping is to be removed from the map.
     * @return previous value associated with specified key, or <code>null</code>
     *	       if there was no entry for key.
     * @throws UnsupportedOperationException if the {@linkplain #base} map doesn't
     *         supports the <code>remove</code> operation.
     */
    public Object remove(final Object key) throws UnsupportedOperationException {
        return base.remove(derivedToBase(key));
    }

    /**
     * Returns a set view of the keys contained in this map.
     *
     * @return a set view of the keys contained in this map.
     */
    public Set keySet() {
        if (keySet == null) {
            keySet = new KeySet(base.keySet());
        }
        return keySet;
    }

    /**
     * Returns a collection view of the values contained in this map.
     *
     * @return a collection view of the values contained in this map.
     */
    public Collection values() {
        return base.values();
    }

    /**
     * Returns a set view of the mappings contained in this map.
     *
     * @return a set view of the mappings contained in this map.
     */
    public Set entrySet() {
        if (entrySet == null) {
            entrySet = new EntrySet(base.entrySet());
        }
        return entrySet;
    }

    /**
     * The key set.
     */
    private final class KeySet extends DerivedSet {
        private static final long serialVersionUID = -2931806200277420177L;

        public KeySet(final Set base) {
            super(base);
        }

        protected Object baseToDerived(final Object element) {
            return DerivedMap.this.baseToDerived(element);
        }
        
        protected Object derivedToBase(final Object element) {
            return DerivedMap.this.derivedToBase(element);
        }
    }

    /**
     * The entry set.
     */
    private final class EntrySet extends DerivedSet {
        private static final long serialVersionUID = -2931806200277420177L;

        public EntrySet(final Set base) {
            super(base);
        }

        protected Object baseToDerived(final Object element) {
            return new Entry((Map.Entry) element);
        }
        
        protected Object derivedToBase(final Object element) {
            return ((Entry) element).entry;
        }
    }

    /**
     * The entry element.
     */
    private final class Entry implements Map.Entry {
        public final Map.Entry entry;

        public Entry(final Map.Entry entry) {
            this.entry = entry;
        }

        public Object getKey() {
            return baseToDerived(entry.getValue());
        }
        
        public Object getValue() {
            return entry.getValue();
        }
        
        public Object setValue(Object value) {
            return entry.setValue(value);
        }
    }
}
