/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2004, Institut de Recherche pour le Développement
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
package org.geotools.resources;

// J2SE dependencies
import java.io.Serializable;
import java.util.AbstractList;


/**
 * An unmodifiable view of an array. Invoking
 *
 * <blockquote><code>
 * new UnmodifiableArrayList(array);
 * </code></blockquote>
 *
 * is equivalent to
 *
 * <blockquote><code>
 * {@linkplain Collections#unmodifiableList Collections.unmodifiableList}({@linkplain
 * Arrays#asList Arrays.asList}(array)));
 * </code></blockquote>
 *
 * But this class provides a very slight performance improvement since it uses one less level
 * of indirection.
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class UnmodifiableArrayList extends AbstractList implements Serializable {
    /**
     * For compatibility with different versions.
     */
    private static final long serialVersionUID = -3605810209653785967L;

    /**
     * The wrapped array.
     */
    private final Object[] array;

    /**
     * Create a new instance of an array list.
     * The array given in argument is <strong>not</strong> cloned.
     */
    public UnmodifiableArrayList(final Object[] array) {
        this.array = array;
    }

    /**
     * Returns the list size.
     */
    public int size() {
        return array.length;
    }

    /**
     * Returns the element at the specified index.
     */
    public Object get(final int index) {
        return array[index];
    }

    /**
     * Returns the index in this list of the first occurence of the specified
     * element, or -1 if the list does not contain this element. This method
     * is overriden only for performance reason (the default implementation
     * would work as well).
     */
    public int indexOf(final Object object) {
        if (object == null) {
            for (int i=0; i<array.length; i++) {
                if (array[i] == null) {
                    return i;
                }
            }
        } else {
            for (int i=0; i<array.length; i++) {
                if (object.equals(array[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Returns the index in this list of the last occurence of the specified
     * element, or -1 if the list does not contain this element. This method
     * is overriden only for performance reason (the default implementation
     * would work as well).
     */
    public int lastIndexOf(final Object object) {
        int i = array.length;
        if (object == null) {
            while (--i >= 0) {
                if (array[i] == null) {
                    break;
                }
            }
        } else {
            while (--i >= 0) {
                if (object.equals(array[i])) {
                    break;
                }
            }
        }
        return i;
    }

    /**
     * Returns {@code true} if this collection contains the specified element.
     * This method is overriden only for performance reason (the default implementation
     * would work as well).
     */
    public boolean contains(final Object object) {
        int i = array.length;
        if (object == null) {
            while (--i >= 0) {
                if (array[i] == null) {
                    return true;
                }
            }
        } else {
            while (--i >= 0) {
                if (object.equals(array[i])) {
                    return true;
                }
            }
        }
        return false;
    }
}
