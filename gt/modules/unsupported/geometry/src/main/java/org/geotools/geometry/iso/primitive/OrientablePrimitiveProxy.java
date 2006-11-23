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


package org.geotools.geometry.iso.primitive;

import java.util.Set;

import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.spatialschema.geometry.TransfiniteSet;
import org.opengis.spatialschema.geometry.complex.Complex;
import org.opengis.spatialschema.geometry.primitive.OrientablePrimitive;
import org.opengis.spatialschema.geometry.primitive.Primitive;
import org.opengis.spatialschema.geometry.primitive.PrimitiveBoundary;
import org.opengis.spatialschema.geometry.root.Geometry;

/**
 * OrientablePrimitive objects are essentially references to geometric
 * primitives that carry an "orientation" reversal flag (either "+" or "-") that
 * determines whether this primitive agrees or disagrees with the orientation of
 * the referenced object.
 * 
 * @author roehrig
 * 
 */
public class OrientablePrimitiveProxy implements OrientablePrimitive {
	/**
	 * Oriented Association Each Primitive of dimension 1 or 2 is associated to
	 * two GM_OrientablePrimitives, one for each possible orientation.
	 * 
	 * GM_Primitive::proxy [0,2] : Reference<GM_OrientablePrimitive>;
	 * GM_OrientablePrimitive::primitive [1] : Reference<GM_Primitive>;
	 * 
	 * For curves and surfaces, there are exactly two orientable primitives for
	 * each geometric object.
	 * 
	 * GM_Primitive: (proxy?notEmpty) = (dimension = 1 or dimension = 2);
	 * 
	 * GM_OrientablePrimitive: a, b :GM_OrientablePrimitive
	 * ((a.primitive=b.primitive)and(a.orientation=b.orientation)) implies a=b;
	 */
	private PrimitiveImpl primitive;

	/**
	 * Orientable primitives are often denoted by a sign (for the orientation)
	 * and a base geometry (curve or surface). The sign datatype is defined in
	 * ISO 19103. If "c" is a curve, then "<+, c>" is its positive orientable
	 * curve and "<-, c>" is its negative orientable curve. In most cases,
	 * leaving out the syntax for record "< , >" does not lead to confusion, so "<+,
	 * c>" may be written as "+c" or simply "c", and "<-, c>" as "-c". Curve
	 * space arithmetic can be performed if the curves align properly, so that:
	 * 
	 * For c, d : GM_OrientableCurves such that c.endPoint = d.startPoint then (
	 * c + d ) ==: GM_CompositeCurve = < c, d >
	 * 
	 * @param primitive
	 * 
	 */

	protected OrientablePrimitiveProxy(PrimitiveImpl primitive) {
		this.primitive = primitive;
	}

	public OrientablePrimitiveProxy clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	public int getOrientation() {
		return -1;
	}

	public Primitive getPrimitive() {
		return primitive;
	}

	public PrimitiveBoundary getBoundary() {
		return primitive.getBoundary();
	}

	public Set<Primitive> getContainedPrimitives() {
		return primitive.getContainedPrimitives();
	}

	public Set<Primitive> getContainingPrimitives() {
		return primitive.getContainingPrimitives();
	}

	public Set<Complex> getComplexes() {
		return primitive.getComplexes();
	}

	public OrientablePrimitive[] getProxy() {
		return primitive.getProxy();
	}

	public CoordinateReferenceSystem getCoordinateReferenceSystem() {
		return primitive.getCoordinateReferenceSystem();
	}

	public Geometry getMbRegion() {
		return primitive.getMbRegion();
	}

	public DirectPosition getRepresentativePoint() {
		return primitive.getRepresentativePoint();
	}

	public Complex getClosure() {
		return primitive.getClosure();
	}

	public boolean isSimple() {
		return primitive.isSimple();
	}

	public boolean isCycle() {
		return primitive.isCycle();
	}

	public double getDistance(Geometry geometry) {
		return primitive.getDistance(geometry);
	}

	public int getDimension(DirectPosition point) {
		return primitive.getDimension(point);
	}

	public int getCoordinateDimension() {
		return primitive.getCoordinateDimension();
	}

	public Set<Complex> getMaximalComplex() {
		return primitive.getMaximalComplex();
	}

	public Geometry transform(CoordinateReferenceSystem newCRS)
			throws TransformException {
		return primitive.transform(newCRS);
	}

	public Geometry transform(CoordinateReferenceSystem newCRS,
			MathTransform transform) throws TransformException {
		return primitive.transform(newCRS, transform);
	}

	public Envelope getEnvelope() {
		return primitive.getEnvelope();
	}

	public DirectPosition getCentroid() {
		return primitive.getCentroid();
	}

	public Geometry getConvexHull() {
		return primitive.getConvexHull();
	}

	public Geometry getBuffer(double distance) {
		return primitive.getBuffer(distance);
	}

	public boolean isMutable() {
		return primitive.isMutable();
	}

	public Geometry toImmutable() {
		return primitive.toImmutable();
	}

	public boolean contains(TransfiniteSet pointSet) {
		return primitive.contains(pointSet);
	}

	public boolean contains(DirectPosition point) {
		return primitive.contains(point);
	}

	public boolean intersects(TransfiniteSet pointSet) {
		return primitive.intersects(pointSet);
	}

	public boolean equals(TransfiniteSet pointSet) {
		return primitive.equals(pointSet);
	}

	public TransfiniteSet union(TransfiniteSet pointSet) {
		return primitive.union(pointSet);
	}

	public TransfiniteSet intersection(TransfiniteSet pointSet) {
		return primitive.intersection(pointSet);
	}

	public TransfiniteSet difference(TransfiniteSet pointSet) {
		return primitive;
	}

	public TransfiniteSet symmetricDifference(TransfiniteSet pointSet) {
		return primitive;
	}
}
