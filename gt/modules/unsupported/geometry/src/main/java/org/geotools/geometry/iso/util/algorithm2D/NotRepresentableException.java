/**
 * This class was copied from the JTS Topology Suite of Vivid Solutions and modified under the terms of GNU Lesser General Public Licence.
 * Date: September 2006 
 */

package org.geotools.geometry.iso.util.algorithm2D;

/**
 * Indicates that a {@link HCoordinate} has been computed which is not
 * representable on the Cartesian plane.
 * 
 * @see HCoordinate
 */
public class NotRepresentableException extends Exception {

	public NotRepresentableException() {
		super("Projective point not representable on the Cartesian plane.");
	}

}
