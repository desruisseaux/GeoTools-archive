/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.util;

// J2SE dependencies
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * Acts as a typed {@link java.util.List} while we wait for Java 5.0.
 * 
 * @author Jody Garnett (Refractions Research)
 * @author Martin Desruisseaux
 */
public class CheckedArrayList extends ArrayList {
    /**
     * Serial version UID for compatibility with different versions.
     */
    private static final long serialVersionUID = -587331971085094268L;
    
    /**
     * The class type.
     */
    private final Class type;

    /**
     * Constructs a list of the specified type.
     *
     * @param type The element type (should not be null).
     */
    public CheckedArrayList(final Class type) {
        this.type = type;
        if (type == null) {
            throw new NullPointerException(
                      Resources.format(ResourceKeys.ERROR_NULL_ARGUMENT_$1, "type"));
        }
    }

    /**
     * Checks the type of the specified object. The default implementation ensure
     * that the object is assignable to the type specified at construction time.
     *
     * @param  element the object to check, or <code>null</code>.
     * @throws IllegalArgumentException if the specified element is not of the expected type.
     */
    protected void ensureValidType(final Object element) throws IllegalArgumentException {
        if (element!=null && !type.isInstance(element)) {
            throw new IllegalArgumentException(Resources.format(ResourceKeys.ERROR_ILLEGAL_CLASS_$2,
                      Utilities.getShortClassName(element), Utilities.getShortName(type)));
        }
    }

    /**
     * Checks the type of all elements in the specified collection.
     *
     * @param  collection the collection to check, or <code>null</code>.
     * @throws IllegalArgumentException if at least one element is not of the expected type.
     */
    private void ensureValid(final Collection collection) throws IllegalArgumentException {
        if (collection != null) {
            for (final Iterator it=collection.iterator(); it.hasNext();) {
                ensureValidType(it.next());
            }
        }
    }

    /**
     * Replaces the element at the specified position in this list with the specified element.
     *
     * @param  index   index of element to replace.
     * @param  element element to be stored at the specified position.
     * @return the element previously at the specified position.
     * @throws IndexOutOfBoundsException if index out of range.
     * @throws IllegalArgumentException if the specified element is not of the expected type.
     */
    public Object set(final int index, final Object element) {
        ensureValidType(element);
        return super.set(index, element);
    }

    /**
     * Appends the specified element to the end of this list.
     *
     * @param  element element to be appended to this list.
     * @return always <code>true</code>.
     * @throws IllegalArgumentException if the specified element is not of the expected type.
     */
    public boolean add(final Object element) {
        ensureValidType(element);
        return super.add(element);
    }

    /**
     * Inserts the specified element at the specified position in this list.
     *
     * @param  index index at which the specified element is to be inserted.
     * @param  element element to be inserted.
     * @throws IndexOutOfBoundsException if index out of range.
     * @throws IllegalArgumentException if the specified element is not of the expected type.
     */
    public void add(final int index, final Object element) {
        ensureValidType(element);
        super.add(index, element);
    }

    /**
     * Appends all of the elements in the specified collection to the end of this list,
     * in the order that they are returned by the specified Collection's Iterator.
     *
     * @param collection the elements to be inserted into this list.
     * @return <code>true</code> if this list changed as a result of the call.
     * @throws IllegalArgumentException if at least one element is not of the expected type.
     */
    public boolean addAll(final Collection collection) {
        ensureValid(collection);
        return super.addAll(collection);
    }

    /**
     * Inserts all of the elements in the specified collection into this list,
     * starting at the specified position.
     *
     * @param index index at which to insert first element fromm the specified collection.
     * @param collection elements to be inserted into this list.
     * @return <code>true</code> if this list changed as a result of the call.
     * @throws IllegalArgumentException if at least one element is not of the expected type.
     */
    public boolean addAll(final int index, final Collection collection) {
        ensureValid(collection);
        return super.addAll(index, collection);
    }
}
