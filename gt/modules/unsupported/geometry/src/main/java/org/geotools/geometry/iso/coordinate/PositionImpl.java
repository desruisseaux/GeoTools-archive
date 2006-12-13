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

package org.geotools.geometry.iso.coordinate;

import org.geotools.geometry.iso.primitive.PointImpl;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.geometry.Position;

/**
 * @author Jackson Roehrig & Sanjay Jena
 * 
 * The data type Position is a union type consisting of either a
 * DirectPosition2D or of a reference to a Point from which a DirectPosition2D
 * is obtained. The use of this data type allows the identification of a
 * position either directly as a coordinate (variant direct) or indirectly as a
 * reference to a Point (variant indirect). Position::direct [0,1] :
 * DirectPosition2D Position::indirect [0,1] : PointRef Position: {direct.isNull =
 * indirect.isNotNull}
 */

public class PositionImpl implements Position {

	// The Position is either represented by a DirectPosition or Point
	private Object position = null;

	/**
	 * Creates a new <code>Position</code> instance.
	 * 
	 * @param directPosition
	 *            an <code>DirectPosition2D</code> value
	 */
	public PositionImpl(final DirectPosition directPosition) {
		if (directPosition == null)
			throw new IllegalArgumentException("DirectPosition is null"); //$NON-NLS-1$
		this.position = directPosition;
	}

	/**
	 * Creates a new <code>Position</code> instance.
	 * 
	 * @param pointRef
	 *            an <code>PointRef</code> value
	 */
	public PositionImpl(final PointImpl pointRef) {
		if (pointRef == null)
			throw new IllegalArgumentException("PointRef not passed"); //$NON-NLS-1$
		this.position = pointRef;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.geometry.Position#getPosition()
	 */
	public DirectPositionImpl getPosition() {
		// ok
		return (this.position instanceof DirectPositionImpl) ? (DirectPositionImpl) this.position
				: ((PointImpl) this.position).getPosition();
	}

	/**
	 * Returns true, if the Position is representated as a PointReference.
	 * Returns false, if the Position is representated as a DirectPoint.
	 * 
	 * @return true if the Position is representated as a PointReference
	 */
	public boolean hasPoint() {
		return (this.position instanceof PointImpl);
	}

	/**
	 * Returns a Point or null
	 * 
	 * @return the Point if the position is of type Point. If position is an
	 *         instance of DirectPositionImpl, return a new Point if force is
	 *         true and null is force is false
	 */
	public PointImpl getPoint() {
		return this.hasPoint() ? (PointImpl) this.position : null;
	}

	/**
	 * @param position
	 *            The position to set.
	 */
	public void setDirectPosition(DirectPositionImpl position) {
		this.position = position;
	}

	/**
	 * Returns the coordinate dimension of the position
	 * 
	 * @return dimension
	 */
	public int getCoordinateDimension() {
		return this.hasPoint() ? ((PointImpl) this.position)
				.getCoordinateDimension()
				: ((DirectPositionImpl) this.position).getDimension();
	}

	public String toString() {
		return "[GM_Position: " + this.getPosition() + "]";
	}

}
