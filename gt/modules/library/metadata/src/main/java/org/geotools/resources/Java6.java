/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2008, Geotools Project Managment Committee (PMC)
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

import java.util.Arrays;


/**
 * Temporary methods to be removed when we will be allowed to compile for Java 6.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class Java6 {
    private Java6() {
    }

    /**
     * Placeholder for {@link Arrays#binarySearch(int[], int, int, int)}.
     */
    public static int binarySearch(final int[] a, final int fromIndex, final int toIndex, final int key) {
        if (false) {
//          return Arrays.binarySearch(a, fromIndex, toIndex, key);
        }
        // I'm to lazy for implementing a real binary search.
        // Lets switch to Java 6 and throw away this code soon!
        for (int i=fromIndex; i<toIndex; i++) {
            final int v = a[i];
            if (v == key) {
                return i;
            }
            if (v > key) {
                return ~i;
            }
        }
        return ~toIndex;
    }
}
