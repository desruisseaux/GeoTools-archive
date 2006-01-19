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
package org.geotools.resources;

// J2SE direct dependencies
import java.util.AbstractSet;
import java.util.Iterator;


/**
 * An immutable set built from an iterator, which will be filled only when needed. This
 * implementation do <strong>not</strong> check if all elements in the iterator are really
 * unique; we assume that it was already verified by {@link javax.imageio.spi.ServiceRegistry}.
 * This set is constructed by {@link org.geotools.referencing.FactoryFinder}.
 *
 * @since 2.0
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class LazySet extends AbstractSet {
    /**
     * The iterator to use for filling this set.
     */
    private final Iterator iterator;

    /**
     * The elements in this set. This array will grown as needed.
     */
    private Object[] elements = new Object[4];

    /**
     * The current size of this set. This size will increases as long as there is some elements
     * remaining in the iterator. This is <strong>not</strong> the size returned by {@link #size()}.
     */
    private int size;

    /**
     * Construct a set to be filled using the specified iterator.
     * Iteration in the given iterator will occurs only when needed.
     */
    public LazySet(final Iterator iterator) {
        this.iterator = iterator;
    }

    /**
     * Add the next element from the iterator to this set. This method doesn't check
     * if more element were available; the check must have been done before to invoke
     * this method.
     */
    private void addNext() {
        if (size >= elements.length) {
            final Object[] old = elements;
            elements = new Object[size*2];
            System.arraycopy(old, 0, elements, 0, size);
        }
        elements[size++] = iterator.next();
    }

    /**
     * Returns an iterator over the elements contained in this set.
     * This is not the same iterator than the one given to the constructor.
     */
    public Iterator iterator() {
        return new Iter();
    }

    /**
     * Returns the number of elements in this set. Invoking this method
     * force the set to immediately iterates through all remaining elements.
     */
    public int size() {
        while (iterator.hasNext()) {
            addNext();
        }
        return size;
    }

    /**
     * Tests if this set has no elements.
     */
    public boolean isEmpty() {
        return size==0 && !iterator.hasNext();
    }

    /**
     * Returns {@code true} if an element exists at the given index.
     * The element is not loaded immediately.
     *
     * <strong>NOTE: This method is for use by iterators only.</strong>
     * It is not suited for more general usage since it doesn't check
     * for negative index and for skipped elements.
     */
    final boolean exists(final int index) {
        return index<size || iterator.hasNext();
    }

    /**
     * Returns the element at the specified position in this set.
     */
    public Object get(final int index) {
        while (index >= size) {
            if (!iterator.hasNext()) {
                throw new IndexOutOfBoundsException(String.valueOf(index));
            }
            addNext();
        }
        return elements[index];
    }

    /**
     * The iterator implementation for the {@linkplain LazySet lazy set}.
     */
    private final class Iter implements Iterator {
        /** Index of the next element to be returned. */
        private int cursor;

        /** Check if there is more elements. */
        public boolean hasNext() {
            return exists(cursor);
        }

        /** Returns the next element. */
        public Object next() {
            return get(cursor++);
        }

        /** Always throws an exception, since {@link LazySet} are immutable. */
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
