/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006-2007, GeoTools Project Management Committee (PMC)
 *    (C) 2006       University of Applied Sciences K�ln (Fachhochschule K�ln)
 *    (C) 2001-2006  Vivid Solutions
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


package org.geotools.geometry.iso.util;

/**
 * Thrown when the application is in an inconsistent state. Indicates a problem
 * with the code.
 */
public class AssertionFailedException extends RuntimeException {

	/**
	 * Creates an <code>AssertionFailedException</code>.
	 */
	public AssertionFailedException() {
		super();
	}

	/**
	 * Creates a <code>AssertionFailedException</code> with the given detail
	 * message.
	 * 
	 * @param message
	 *            a description of the assertion
	 */
	public AssertionFailedException(String message) {
		super(message);
	}
}
