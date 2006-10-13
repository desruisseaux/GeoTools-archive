/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2005, Geotools Project Managment Committee (PMC)
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
package org.geotools.referencing.operation.calculator;

import org.opengis.referencing.operation.TransformException;


/**
 * Thrown when a calculation can't be performed because of the
 * configuration wrong configuration of the Coordinate Refernce System the
 * {@linkplain AbstractParamCalculator#ptSrc} and {@linkplain AbstractParamCalculator#ptDst}.
 */
public class CRSException extends TransformException {
    private static final long serialVersionUID = -6328524211656251233L;

    /**
     * Constructs an exception with no detail message.
     */
    public CRSException() {
        super();
    }

    /**
     * Constructs an exception with the specified detail message.
     *
     * @param  message The cause for this exception. The cause is saved
     *         for later retrieval by the {@link #getCause()} method.
     */
    public CRSException(String message) {
        super(message);
    }
}
