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
package org.geotools.feature;

/**
 * Indicates client class has attempted to create an invalid feature.
 *
 * @author Rob Hranac, Vision for New York
 * @version $Id: IllegalFeatureException.java,v 1.4 2003/05/07 16:53:04 jmacgill Exp $
 */
public class IllegalFeatureException extends Exception {
    /**
     * Constructor with no argument.
     */
    public IllegalFeatureException() {
        super();
    }

    /**
     * Constructor with message argument.
     *
     * @param message Reason for the exception being thrown
     */
    public IllegalFeatureException(String message) {
        super(message);
    }
}
