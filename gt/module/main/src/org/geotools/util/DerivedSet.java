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
import java.util.Iterator;
import java.util.AbstractSet;
import java.io.Serializable;


/**
 * A set whose values are derived from an other set. The values are derived only when
 * requested, which make it possible to backup potentially large sets. Implementations
 * need only to overrides {@link #baseToDerived} and {@link #derivedToBase} methods.
 * This set do not supports <code>null</code> value, since <code>null</code> is used
 * when no mapping from {@linkplain #base} to <code>this</code> exists.
 * This class is serializable if the underlying {@linkplain #base} set is serializable
 * too.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class DerivedSet extends AbstractSet implements Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -4662336508586424581L;

    /**
     * The base set whose values are derived from.
     *
     * @see #baseToDerived
     * @see #derivedToBase
     */
    protected final Set base;
    
    /**
     * Creates a new derived set from the specified base set.
     *
     * @param base The base set.
     */
    public DerivedSet(final Set base) {
        this.base = base;
    }

    /**
     * Transforms a value in the {@linkplain #base} set to a value in this set.
     * If there is no mapping in the derived set for the specified element,
     * then this method returns <code>null</code>.
     *
     * @param  element A value in the {@linkplain #base} set.
     * @return The value that this view should contains instead of <code>element</code>,
     *         or <code>null</code>.
     */
    protected abstract Object baseToDerived(final Object element);

    /**
     * Transforms a value in this set to a value in the {@linkplain #base} set.
     *
     * @param  element A value in this set.
     * @return The value stored in the {@linkplain #base} set.
     */
    protected abstract Object derivedToBase(final Object element);

    /**
     * Returns an iterator over the elements contained in this set.
     * The iterator will invokes {@link #baseToDerived} for each element.
     *
     * @return an iterator over the elements contained in this set.
     */
    public Iterator iterator() {
        return new Iter(base.iterator());
    }
    
    /**
     * Returns the number of elements in this set. The default implementation counts
     * the number of elements returned by the {@link #iterator iterator}.
     *
     * @return the number of elements in this set.
     */
    public int size() {
        int count = 0;
        for (final Iterator it=iterator(); it.hasNext();) {
            it.next();
            count++;
        }
        return count;
    }

    /**
     * Returns <code>true</code> if this set contains no elements.
     *
     * @return <code>true</code> if this set contains no elements.
     */
    public boolean isEmpty() {
        return base.isEmpty() || super.isEmpty();
    }

    /**
     * Returns <code>true</code> if this set contains the specified element.
     * The default implementation invokes
     * <code>{@linkplain #base}.contains({@linkplain #derivedToBase derivedToBase}(element))</code>.
     *
     * @param  element object to be checked for containment in this set.
     * @return <code>true</code> if this set contains the specified element.
     */
    public boolean contains(final Object element) {
        return base.contains(derivedToBase(element));
    }

    /**
     * Ensures that this set contains the specified element.
     * The default implementation invokes
     * <code>{@linkplain #base}.add({@linkplain #derivedToBase derivedToBase}(element))</code>.
     *
     * @param  element element whose presence in this set is to be ensured.
     * @return <code>true</code> if the set changed as a result of the call.
     * @throws UnsupportedOperationException if the {@linkplain #base} set doesn't
     *         supports the <code>add</code> operation.
     */
    public boolean add(final Object element) throws UnsupportedOperationException {
        return base.add(derivedToBase(element));
    }

    /**
     * Removes a single instance of the specified element from this set.
     * The default implementation invokes
     * <code>{@linkplain #base}.remove({@linkplain #derivedToBase derivedToBase}(element))</code>.
     *
     * @param  element element to be removed from this set, if present.
     * @return <code>true</code> if the set contained the specified element.
     * @throws UnsupportedOperationException if the {@linkplain #base} set doesn't
     *         supports the <code>remove</code> operation.
     */
    public boolean remove(final Object element) throws UnsupportedOperationException {
        return base.remove(derivedToBase(element));
    }

    /**
     * Iterates through the elements in the set.
     */
    private final class Iter implements Iterator {
        /**
         * The iterator from the {@linkplain #base} set.
         */
        private final Iterator iterator;

        /**
         * The next element to be returned, or <code>null</code>.
         */
        private transient Object next;

        /**
         * The iterator from the {@linkplain #base} set.
         */
        public Iter(final Iterator iterator) {
            this.iterator = iterator;
        }

        /**
         * Returns <code>true</code> if the iteration has more elements.
         */
        public boolean hasNext() {
            while (next == null) {
                if (!iterator.hasNext()) {
                    return false;
                }
                next = baseToDerived(iterator.next());
            }
            return true;
        }

        /**
         * Returns the next element in the iteration.
         */
        public Object next() {
            while (next == null) {
                next = baseToDerived(iterator.next());
            }
            final Object value = next;
            next = null;
            return value;
        }

        /**
         * Removes from the underlying set the last element returned by the iterator.
         *
         * @throws UnsupportedOperationException if the {@linkplain #base} set doesn't
         *         supports the <code>remove</code> operation.
         */
        public void remove() {
            iterator.remove();
        }
    }
}
