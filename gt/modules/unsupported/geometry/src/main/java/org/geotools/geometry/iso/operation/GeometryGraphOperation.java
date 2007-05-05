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



package org.geotools.geometry.iso.operation;

import org.geotools.geometry.iso.DimensionModel;
import org.geotools.geometry.iso.PrecisionModel;
import org.geotools.geometry.iso.UnsupportedDimensionException;
import org.geotools.geometry.iso.root.GeometryImpl;
import org.geotools.geometry.iso.topograph2D.GeometryGraph;
import org.geotools.geometry.iso.util.algorithm2D.CGAlgorithms;
import org.geotools.geometry.iso.util.algorithm2D.LineIntersector;
import org.geotools.geometry.iso.util.algorithm2D.RobustLineIntersector;

/**
 * The base class for operations that require {@link org.geotools.geometry.iso.topograph2D.GeometryGraph)s.
 */
public abstract class GeometryGraphOperation {

	protected final CGAlgorithms cga = new CGAlgorithms();

	protected final LineIntersector li = new RobustLineIntersector();

	protected PrecisionModel resultPrecisionModel;

	/**
	 * The operation args into an array so they can be accessed by index
	 */
	protected GeometryGraph[] arg; // the arg(s) of the operation

	public GeometryGraphOperation(GeometryImpl g0, GeometryImpl g1)
			throws UnsupportedDimensionException {

		// use the most precise model for the result
		// TODO PRECISION CORRECTION?!
		// if (g0.getPrecisionModel().compareTo(g1.getPrecisionModel()) >= 0)
		// setComputationPrecision(g0.getPrecisionModel());
		// else
		// setComputationPrecision(g1.getPrecisionModel());

		// Throw Unsupported Dimension Exception if one of the geometries is not 2d or 2.5d
		DimensionModel g0Dim = g0.getFeatGeometryFactory().getDimensionModel();
		DimensionModel g1Dim = g1.getFeatGeometryFactory().getDimensionModel();
		if (!g0Dim.is2D() || !g1Dim.is2D()) {
			throw new UnsupportedDimensionException(
					"This operations do only work in 2D");
		}

		arg = new GeometryGraph[2];
		arg[0] = new GeometryGraph(0, g0);
		arg[1] = new GeometryGraph(1, g1);
	}

	public GeometryGraphOperation(GeometryImpl g0) {

		// TODO PRECISION CORRECTION
		// setComputationPrecision(g0.getPrecisionModel());

		arg = new GeometryGraph[1];
		arg[0] = new GeometryGraph(0, g0);
		;
	}

	public GeometryImpl getArgGeometry(int i) {
		return arg[i].getGeometry();
	}

	protected void setComputationPrecision(PrecisionModel pm) {
		resultPrecisionModel = pm;
		li.setPrecisionModel(resultPrecisionModel);
	}
}
