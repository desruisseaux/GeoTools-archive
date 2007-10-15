/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2005, Institut de Recherche pour le D�veloppement
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
 */
package org.geotools.util;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * A mutable set containing only one element. This set can't contains null element.
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class Singleton<E> extends AbstractSet<E> {
    /**
     * The element, or {@code null} if this set is empty.
     */
    private E element;

    /**
     * Creates a initially empty singleton.
     */
    public Singleton() {
    }

    /**
     * Returns 1 if this singleton contains an element, or 0 otherwise.
     */
    public int size() {
        return (element != null) ? 1 : 0;
    }

    /**
     * Returns {@code true} if this singleton contains no elements.
     */
    @Override
    public boolean isEmpty() {
	return element == null;
    }

    /**
     * Returns the element in this singleton.
     *
     * @return The singleton element (never null).
     * @throws NoSuchElementException if this singleton is empty.
     */
    public E get() throws NoSuchElementException {
        if (element == null) {
            throw new NoSuchElementException();
        }
        return element;
    }

    /**
     * Returns {@code true} if this singleton contains the specified element.
     */
    @Override
    public boolean contains(final Object object) {
        return element!=null && element.equals(object);
    }

    /**
     * Adds the specified element to this set if it is not already present.
     * If this set already contains an other element, an exception is thrown.
     *
     * @throws NullPointerException if the argument is null.
     * @throws IllegalArgumentException if this set already contains an other element.
     */
    @Override
    public boolean add(final E object) {
        if (object == null) {
            throw new NullPointerException();
        }
        if (element == null) {
            element = object;
            return true;
        }
        if (element.equals(object)) {
            element = object;
            return false;
        }
        throw new IllegalArgumentException();
    }

    /**
     * Removes the specified element from this singleton, if it is present.
     */
    @Override
    public boolean remove(final Object object) {
        if (element!=null && element.equals(object)) {
            element = null;
            return true;
        }
        return false;
    }

    /**
     * Removes the element from this singleton.
     */
    @Override
    public void clear() {
        element = null;
    }

    /**
     * Returns an iterator over the element of this singleton.
     */
    @Override
    public Iterator<E> iterator() {
        return new Iter();
    }

    /**
     * The iterator for this singleton.
     */
    private final class Iter implements Iterator<E> {
        /**
         * {@code false} if this iterator is done.
         */
        private boolean hasNext;

        /**
         * Construct a new iterator.
         */
        public Iter() {
            hasNext = (element != null);
        }

        /**
         * Returns {@code true} if there is more element to return.
         */
        public boolean hasNext() {
            return hasNext;
        }

        /**
         * Returns the next element.
         */
        public E next() {
            if (!hasNext) {
                throw new NoSuchElementException();
            }
            hasNext = false;
            return element;
        }

        /**
         * Remove the last element in this set.
         */
        public void remove() {
            if (hasNext) {
                throw new IllegalStateException();
            }
            element = null;
        }
    }
}
