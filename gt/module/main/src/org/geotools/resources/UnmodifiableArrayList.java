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
package org.geotools.resources;

// J2SE dependencies
import java.io.Serializable;
import java.util.AbstractList;


/**
 * An unmodifiable view of an array.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class UnmodifiableArrayList extends AbstractList implements Serializable {
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
     * Returns <code>true</code> if this collection contains the specified element.
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
