/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, Geotools Project Managment Committee (PMC)
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
package org.geotools.coverage.processing;

/**
 * 
 * @author Simone Giannecchini
 */
public class CannotCropException extends RuntimeException {
    private static final long serialVersionUID = -4382377333378224973L;

    public CannotCropException(String message) {
		super(message);
	}

	public CannotCropException(String message, Throwable exception) {
		super(message,exception);
	}
}
