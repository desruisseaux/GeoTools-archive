/*
 * This implementation of the OGC Feature Geometry Abstract Specification
 * (ISO 19107) is a project of the University of Applied Sciences Cologne
 * (Fachhochschule Köln) in collaboration with GeoTools and GeoAPI.
 *
 * Copyright (C) 2006 University of Applied Sciences Köln
 *                    (Fachhochschule Köln) and GeoTools
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * For more information, contact:
 *
 *     Prof. Dr. Jackson Roehrig
 *     Institut für Technologie in den Tropen
 *     Fachhochschule Köln
 *     Betzdorfer Strasse 2
 *     D-50679 Köln
 *     Jackson.Roehrig@fh-koeln.de
 *
 *     Sanjay Dominik Jena
 *     san.jena@gmail.com
 *
 */

package org.geotools.geometry.iso;

/**
 * The UnsupportedDimensionException will be thrown when methods are called,
 * which are not capable to treat the dimension of the input data, or do not
 * always work correctly in that dimension.
 * 
 * @author Sanjay Dominik Jena
 * 
 */
public class UnsupportedDimensionException extends Exception {

	/**
	 * Creates a <code>UnsupportedDimensionException</code> with the given
	 * detail message.
	 * 
	 * @param message
	 *            a description of this
	 *            <code>UnsupportedDimensionException</code>
	 */
	public UnsupportedDimensionException(String message) {
		super(message);
	}

	/**
	 * Creates a <code>UnsupportedDimensionException</code> with
	 * <code>e</code>s detail message.
	 * 
	 * @param e
	 *            an exception that occurred while trying to operate a function
	 *            which is not operable for that coordiante dimension
	 */
	public UnsupportedDimensionException(Exception e) {
		this(e.toString());
	}

}
