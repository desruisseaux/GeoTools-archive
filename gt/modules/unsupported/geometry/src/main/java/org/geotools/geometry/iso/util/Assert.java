/*
 * This implementation of the OGC Feature Geometry Abstract Specification
 * (ISO 19107) is a project of the University of Applied Sciences Cologne
 * (Fachhochschule K�ln) in collaboration with GeoTools and GeoAPI.
 *
 * Copyright (C) 2006 University of Applied Sciences K�ln
 *                    (Fachhochschule K�ln) and GeoTools
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
 *     Institut f�r Technologie in den Tropen
 *     Fachhochschule K�ln
 *     Betzdorfer Strasse 2
 *     D-50679 K�ln
 *     Jackson.Roehrig@fh-koeln.de
 *
 *     Sanjay Dominik Jena
 *     san.jena@gmail.com
 *
 */
/*
 * This class was copied from the JTS Topology Suite Version 1.7.2
 * of Vivid Solutions and modified and reused in this library under
 * the terms of GNU Lesser General Public Licence.
 * The original copyright of the Vivid Solutions JTS is stated as follows:
 *
 *------------------------------------------------------------------------
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
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
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 *------------------------------------------------------------------------
 */ 


package org.geotools.geometry.iso.util;


// TODO SJ: It has to be discussed whether the Implementation uses Asserts or Throwable Exceptions; probably the Exceptions are the better solutions.

/**
 * A utility for making programming assertions.
 */
public class Assert {

	/**
	 * Throws an <code>AssertionFailedException</code> if the given assertion
	 * is not true.
	 * 
	 * @param assertion
	 *            a condition that is supposed to be true
	 * @throws AssertionFailedException
	 *             if the condition is false
	 */
	public static void isTrue(boolean assertion) {
		isTrue(assertion, null);
	}

	/**
	 * Throws an <code>AssertionFailedException</code> with the given message
	 * if the given assertion is not true.
	 * 
	 * @param assertion
	 *            a condition that is supposed to be true
	 * @param message
	 *            a description of the assertion
	 * @throws AssertionFailedException
	 *             if the condition is false
	 */
	public static void isTrue(boolean assertion, String message) {
		if (!assertion) {
			if (message == null) {
				throw new AssertionFailedException();
			} else {
				throw new AssertionFailedException(message);
			}
		}
	}

	/**
	 * Throws an <code>AssertionFailedException</code> if the given objects
	 * are not equal, according to the <code>equals</code> method.
	 * 
	 * @param expectedValue
	 *            the correct value
	 * @param actualValue
	 *            the value being checked
	 * @throws AssertionFailedException
	 *             if the two objects are not equal
	 */
	public static void equals(Object expectedValue, Object actualValue) {
		equals(expectedValue, actualValue, null);
	}

	/**
	 * Throws an <code>AssertionFailedException</code> with the given message
	 * if the given objects are not equal, according to the <code>equals</code>
	 * method.
	 * 
	 * @param expectedValue
	 *            the correct value
	 * @param actualValue
	 *            the value being checked
	 * @param message
	 *            a description of the assertion
	 * @throws AssertionFailedException
	 *             if the two objects are not equal
	 */
	public static void equals(Object expectedValue, Object actualValue,
			String message) {
		if (!actualValue.equals(expectedValue)) {
			throw new AssertionFailedException("Expected " + expectedValue
					+ " but encountered " + actualValue
					+ (message != null ? ": " + message : ""));
		}
	}

	/**
	 * Always throws an <code>AssertionFailedException</code>.
	 * 
	 * @throws AssertionFailedException
	 *             thrown always
	 */
	public static void shouldNeverReachHere() {
		shouldNeverReachHere(null);
	}

	/**
	 * Always throws an <code>AssertionFailedException</code> with the given
	 * message.
	 * 
	 * @param message
	 *            a description of the assertion
	 * @throws AssertionFailedException
	 *             thrown always
	 */
	public static void shouldNeverReachHere(String message) {
		throw new AssertionFailedException("Should never reach here"
				+ (message != null ? ": " + message : ""));
	}
}
