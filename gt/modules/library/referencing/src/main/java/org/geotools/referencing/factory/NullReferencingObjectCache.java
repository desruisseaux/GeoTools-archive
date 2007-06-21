/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, GeoTools Project Managment Committee (PMC)
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
package org.geotools.referencing.factory;

import java.util.Map;


/**
 * Null implementation for the ReferencingObjectCache. Used for cases where
 * caching is *not* desired.
 * 
 * @since 2.4
 * @version $Id$
 * @source $URL$
 * @author Cory Horner (Refractions Research)
 */
final class NullReferencingObjectCache implements ReferencingObjectCache {
    /**
     * Do nothing since this map is already empty.
     */
    public void clear() {
    }

    /**
     * @todo This method should not be defined there.
     */
    public Map findPool() {
        return null;
    }

    /**
     * Returns {@code null} since this map is empty.
     */
    public Object get(Object key) {
        return null;
    }

    /**
     * Do nothing since this map do not cache anything.
     */
    public void put(Object key, Object object) {
    }
}
