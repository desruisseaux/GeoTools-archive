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
import java.util.LinkedHashSet;

import org.geotools.resources.Utilities;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.resources.cts.Resources;
import org.opengis.util.Cloneable;


/**
 * Acts as a typed {@link java.util.Set} while we wait for Java 5.0.
 * 
 * @version $Id$
 * @author Jody Garnett (Refractions Research)
 * @author Martin Desruisseaux
 *
 * @todo Provides synchronization facility on arbitrary lock, for use with the metadata package.
 *       The lock would be the metadata that owns this collection. Be carefull to update the lock
 *       after a clone (this work my be done in <code>MetadataEntity.unmodifiable(Object)</code>).
 */
public class CheckedHashSet extends LinkedHashSet implements Cloneable {
    /**
     * Serial version UID for compatibility with different versions.
     */
    private static final long serialVersionUID = -9014541457174735097L;

    /**
     * The class type.
     */
    private final Class type;

    /**
     * Constructs a set of the specified type.
     *
     * @param type The element type (should not be null).
     */
    public CheckedHashSet(final Class type) {
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
     * Adds the specified element to this set if it is not already present.
     *
     * @param  element element to be added to this set.
     * @return <code>true</code> if the set did not already contain the specified element.
     * @throws IllegalArgumentException if the specified element is not of the expected type.
     */
    public boolean add(final Object element) {
        ensureValidType(element);
        return super.add(element);
    }
}
