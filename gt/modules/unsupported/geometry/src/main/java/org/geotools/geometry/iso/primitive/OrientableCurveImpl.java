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

package org.geotools.geometry.iso.primitive;

import org.geotools.geometry.iso.FeatGeomFactoryImpl;
import org.opengis.spatialschema.geometry.primitive.OrientableCurve;

/**
 * 
 * OrientableCurve consists of a curve and an orientation inherited from
 * OrientablePrimitive. If the orientation is "+", then the OrientableCurve is a
 * Curve. If the orientation is "-", then the OrientableCurve is related to
 * another Curve with a parameterization that reverses the sense of the curve
 * traversal.
 * 
 * OrientableCurve: {Orientation = "+" implies primitive = self}; {Orientation =
 * "-" implies primitive.parameterization(length()-s) = parameterization(s)};
 * 
 * @author Jackson Roehrig & Sanjay Jena
 */
public abstract class OrientableCurveImpl extends OrientablePrimitiveImpl
		implements OrientableCurve {

	/**
	 * Constructor
	 * 
	 * @param factory
	 */
	protected OrientableCurveImpl(FeatGeomFactoryImpl factory) {
		super(factory);
	}

	/**
	 * TODO fuer was brauchen wir diesen constructor ? (SJ) Curves enthalten
	 * keine primitives, und sind auch in keien enthalten; verwechslung mit
	 * complexes?!?!
	 * 
	 * @param factory
	 * @param containedPrimitive
	 * @param containingPrimitive
	 * @param complex
	 */
	// protected OrientableCurveImpl(FeatGeomFactoryImpl factory, Set<Primitive>
	// containedPrimitive,
	// Set<Primitive> containingPrimitive, Set<Complex> complex) {
	// super(factory, containedPrimitive,containingPrimitive,complex);
	// }
	
	/**
	 * Returns an array with two orientable primitives, whereas the first one is
	 * "this" object and the second one the field proxy
	 * 
	 * @return an array OrientablePrimitive[2] with the positive and the
	 *         negative orientable primitive
	 */
	public OrientableCurve[] getProxy() {
		return new OrientableCurve[] { this, (OrientableCurve) this.proxy };
	}

}
