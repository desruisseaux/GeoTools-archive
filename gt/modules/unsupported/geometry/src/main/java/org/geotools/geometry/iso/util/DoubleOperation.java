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

package org.geotools.geometry.iso.util;

/**
 * This class offers elementary arithmetic operations for the elementary data type <code>double</code>.
 * The operations are separated from the rest of the code to keep the option to interchange this package with a exact computation package (where needed).
 * 
 * The implementations in this class are non-robust in sense that round-errors may occur.
 * 
 * @author Sanjay Jena
 *
 */
public class DoubleOperation {
	
	/**
	 * Returns the sum of two doubles: d1 + d2
	 * 
	 * @param d1 First value to add
	 * @param d2 Second value to add
	 * @return Sum of the two values
	 */
	public static double add(double d1, double d2) {
		return d1 + d2;
	}
	
	/**
	 * Returns the subtraction of two doubles: d1 - d2
	 * 
	 * @param d1 First value
	 * @param d2 Value to subtract from first value
	 * @return Subtraction d1 - d2
	 */
	public static double subtract(double d1, double d2) {
		return d1 - d2;
	}
	
	/**
	 * Returns the multiplication of two doubles: d1 * d2
	 * 
	 * @param d1 First value to multiplicate
	 * @param d2 Second value to multiplicate
	 * @return Product of the two values
	 */
	public static double mult(double d1, double d2) {
		return d1 * d2;
	}
	
	/**
	 * Returns the division of two doubles: d1 / d2
	 * 
	 * @param d1 Dividend
	 * @param d2 Divisor
	 * @return Division of the two values
	 */
	public static double div(double d1, double d2) {
		return d1 / d2;
	}




}
