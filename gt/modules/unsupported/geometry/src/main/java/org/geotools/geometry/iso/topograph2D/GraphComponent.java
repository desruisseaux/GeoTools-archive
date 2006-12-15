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



package org.geotools.geometry.iso.topograph2D;

import org.geotools.geometry.iso.util.Assert;

/**
 * A GraphComponent is the parent class for the objects' that form a graph. Each
 * GraphComponent can carry a Label.
 */
abstract public class GraphComponent {

	protected Label label;

	/**
	 * isInResult indicates if this component has already been included in the
	 * result
	 */
	private boolean isInResult = false;

	private boolean isCovered = false;

	private boolean isCoveredSet = false;

	private boolean isVisited = false;

	public GraphComponent() {
	}

	public GraphComponent(Label label) {
		this.label = label;
	}

	public Label getLabel() {
		return label;
	}

	public void setLabel(Label label) {
		this.label = label;
	}

	public void setInResult(boolean isInResult) {
		this.isInResult = isInResult;
	}

	public boolean isInResult() {
		return isInResult;
	}

	public void setCovered(boolean isCovered) {
		this.isCovered = isCovered;
		this.isCoveredSet = true;
	}

	public boolean isCovered() {
		return isCovered;
	}

	public boolean isCoveredSet() {
		return isCoveredSet;
	}

	public boolean isVisited() {
		return isVisited;
	}

	public void setVisited(boolean isVisited) {
		this.isVisited = isVisited;
	}

	/**
	 * @return a coordinate in this component (or null, if there are none)
	 */
	abstract public Coordinate getCoordinate();

	/**
	 * compute the contribution to an IM for this component
	 */
	abstract protected void computeIM(IntersectionMatrix im);

	/**
	 * An isolated component is one that does not intersect or touch any other
	 * component. This is the case if the label has valid locations for only a
	 * single Geometry.
	 * 
	 * @return true if this component is isolated
	 */
	abstract public boolean isIsolated();

	/**
	 * Update the IM with the contribution for this component. A component only
	 * contributes if it has a labelling for both parent geometries
	 */
	public void updateIM(IntersectionMatrix im) {
		Assert.isTrue(label.getGeometryCount() >= 2, "found partial label");
		computeIM(im);
	}

}
