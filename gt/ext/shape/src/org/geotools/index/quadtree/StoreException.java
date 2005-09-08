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
 *
 */
/*
 * Created on 19-ago-2004
 */
package org.geotools.index.quadtree;

/**
 * DOCUMENT ME!
 *
 * @author Tommaso Nolli
 */
public class StoreException extends Exception {
    /**
     *
     */
    public StoreException() {
        super();
    }

    /**
     * DOCUMENT ME!
     *
     * @param message
     */
    public StoreException(String message) {
        super(message);
    }

    /**
     * DOCUMENT ME!
     *
     * @param cause
     */
    public StoreException(Throwable cause) {
        super(cause);
    }

    /**
     * DOCUMENT ME!
     *
     * @param message
     * @param cause
     */
    public StoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
