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

package org.geotools.geometry.iso.complex;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.geotools.geometry.iso.FeatGeomFactoryImpl;
import org.geotools.geometry.iso.coordinate.DirectPositionImpl;
import org.geotools.geometry.iso.coordinate.EnvelopeImpl;
import org.geotools.geometry.iso.coordinate.LineStringImpl;
import org.geotools.geometry.iso.io.GeometryToString;
import org.geotools.geometry.iso.primitive.CurveImpl;
import org.geotools.geometry.iso.primitive.OrientableCurveImpl;
import org.geotools.geometry.iso.primitive.PrimitiveFactoryImpl;
import org.geotools.geometry.iso.util.DoubleOperation;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.spatialschema.geometry.complex.Complex;
import org.opengis.spatialschema.geometry.complex.CompositeCurve;
import org.opengis.spatialschema.geometry.primitive.Curve;
import org.opengis.spatialschema.geometry.primitive.CurveBoundary;
import org.opengis.spatialschema.geometry.primitive.OrientableCurve;
import org.opengis.spatialschema.geometry.primitive.OrientablePrimitive;
import org.opengis.spatialschema.geometry.primitive.Primitive;

/**
 * 
 * A composite curve, CompositeCurve shall be a Composite with all the geometric
 * properties of a curve. These properties are instantiated in the operation
 * "curve". Essentially, a composite curve is a list of orientable curves
 * (OrientableCurve) agreeing in orientation in a manner such that each curve
 * (except the first) begins where the previous one ends.
 * 
 * @author Jackson Roehrig & Sanjay Jena
 */
public class CompositeCurveImpl extends CompositeImpl<OrientableCurveImpl>
		implements CompositeCurve {

	protected EnvelopeImpl envelope = null;

	/**
	 * The association role Composition::generator associates this
	 * CompositeCurve to the primitive Curves and OrientableCurves in its
	 * generating set, the curves that form the core of this complex.
	 * CompositeCurve::generator : Sequence<OrientableCurve> -- the start point
	 * of each orientable curve in the generator is the -- end point of the
	 * previous one CompositeCurve: forAll (1 < j < generator.count - 1)?
	 * generator[j].endPoint = generator[j+1].startPoint;
	 * 
	 * NOTE To get a full representation of the elements in the Complex, the
	 * Points on the boundary of the generator set of Curve would be added to
	 * the curves in the generator list.
	 * 
	 * The generator elements will be passed through the super constructor. The
	 * plausibility of the Curves will be checked within the constructor of the
	 * CompositeCurve
	 * 
	 * Constructs a Composite Curve
	 * 
	 * @param factory
	 * 
	 * @param generator
	 */
	protected CompositeCurveImpl(FeatGeomFactoryImpl factory,
			List<OrientableCurve> generator) {
		/* Pass elements to super constructor */
		super(factory, generator);
		this.checkConsistency();
	}

	/**
	 * - Check plausibility, if all curve elements are continuous
	 * - Build Envelope
	 */
	private void checkConsistency() {
		/* Check plausibility, if all curve elements are continuous */
		Iterator<? extends Primitive> ci = this.elements.iterator();
		if (!ci.hasNext())
			throw new IllegalArgumentException("Curve has no elements."); //$NON-NLS-1$
		CurveImpl c0 = (CurveImpl) ci.next();
		;
		//this.envelope = new EnvelopeImpl(c0.getEnvelope());
		this.envelope = c0.getGeometryFactory().getCoordinateFactory().createEnvelope(c0.getEnvelope());
		while (ci.hasNext()) {
			CurveImpl c1 = (CurveImpl) ci.next();
			this.envelope.expand(c1.getEnvelope());
			if (!c0.getEndPoint().equals(c1.getStartPoint()))
				throw new IllegalArgumentException(
						"Curve elements are not continous. The end point of a curve has to accord to the start point of the following curve."); //$NON-NLS-1$
		}

	}

	/* (non-Javadoc)
	 * @see org.geotools.geometry.featgeom.complex.ComplexImpl#getElements()
	 */
	public List<Primitive> getElements() {
		// Override to return a List
		return (List<Primitive>) super.getElements();
	}

	/* (non-Javadoc)
	 * @see org.geotools.geometry.featgeom.complex.ComplexImpl#createBoundary()
	 */
	public Set<Complex> createBoundary() {
		/*
		 * used after construction, when the generators were already defined.
		 * derived classes (e.g. Ring) may overwrite these method
		 */
		List<Primitive> generator = this.getElements();
		if (generator == null)
			throw new IllegalArgumentException(
					"Could not create the boundary of CompositeCurve."); //$NON-NLS-1$
		TreeSet<Complex> result = new TreeSet<Complex>();
		PrimitiveFactoryImpl pf = this.getGeometryFactory()
				.getPrimitiveFactory();
		result.add(pf.createCurveBoundary(
				pf.createPoint((DirectPositionImpl) ((CurveImpl) generator
						.get(0)).getStartPoint()),
				pf.createPoint((DirectPositionImpl) ((CurveImpl) generator
						.get(0)).getEndPoint())));
		return result;
	}

	/* (non-Javadoc)
	 * @see org.geotools.geometry.featgeom.complex.CompositeImpl#getGeneratorClass()
	 */
	public Class getGeneratorClass() {
		return OrientableCurveImpl.class;
	}

	/**
	 * Returns all control points of the Ring as one LineString
	 * 
	 * @return Returns all control points of the Ring as one LineString
	 */
	// public LineString asLineString() {
	// int len = 0;
	// /* All Curves of the CompositeCurve */
	// OrientableCurve tmpCurves[] = this.getGenerators();
	// /* Two dimensional array with all LineSegments for each curve */
	// LineSegment tmpLineSegments[][] = new LineSegment[tmpCurves.length][];
	// /* Copy LineSegments into array and count number of segments */
	// for (int i=0; i<tmpCurves.length; i++) {
	// tmpLineSegments[i] = tmpCurves[i].asLineString(0, 0).asLineSegment();
	// len += tmpLineSegments[i].length;
	// }
	// /* Array of Position - one position for each point of the LineSegments */
	// Position tmpPositions[] = new Position[len];
	// /* First Point */
	// tmpPositions[0] = new Position(tmpLineSegments[0][0].startPoint());
	// int index = 1;
	// /* Copy all following end points into the array of the positions */
	// for (int i=0; i<tmpLineSegments.length; i++) {
	// for (int j=0; j<tmpLineSegments[i].length; j++) {
	// tmpPositions[index] = new Position(tmpLineSegments[i][j].endPoint());
	// index++;
	// }
	// }
	// /* Return new LineString, created by the array of Positions of all curves
	// */
	// return new LineString(tmpPositions);
	// }

	/***************************************************************************
	 * ********************************************************* * Method
	 * implementation of Interface Object **
	 * *********************************************************
	 **************************************************************************/

	/**
	 * The method <code>dimension</code> returns the inherent dimension of
	 * this Object, which is less than or equal to the coordinate dimension. The
	 * dimension of a collection of geometric objects is the largest dimension
	 * of any of its pieces. Points are 0-dimensional, curves are 1-dimensional,
	 * surfaces are 2-dimensional, and solids are 3-dimensional. Locally, the
	 * dimension of a geometric object at a point is the dimension of a local
	 * neighborhood of the point - that is the dimension of any coordinate
	 * neighborhood of the point. Dimension is unambiguously defined only for
	 * DirectPositions interior to this Object. If the passed DirectPosition2D
	 * is NULL, then the method returns the largest possible dimension for any
	 * DirectPosition2D in this Object.
	 * 
	 * @param point
	 *            a <code>DirectPosition2D</code> value
	 * @return an <code>int</code> value
	 */
	public int getDimension(@SuppressWarnings("unused")
	final DirectPositionImpl point) {
		return 1;
	}


	/* (non-Javadoc)
	 * @see org.geotools.geometry.featgeom.root.GeometryImpl#getEnvelope()
	 */
	public Envelope getEnvelope() {
		return this.envelope;
	}


	/* (non-Javadoc)
	 * @see org.geotools.geometry.featgeom.root.GeometryImpl#getBoundary()
	 */
	public CurveBoundary getBoundary() {
		// TODO
		return null;// this.getSubComplexes();
	}

	/**
	 * @return LineStringImpl
	 */
	public LineStringImpl asLineString() {
		return this.asLineString(0.0, 0.0);
	}

	/**
	 * @param maxSpacing
	 * @param maxOffset
	 * @return LineStringImpl
	 */
	public LineStringImpl asLineString(double maxSpacing, double maxOffset) {
		// The function "asLineString" constructs a line string (sequence of
		// line segments) where the control points (ends of
		// the segments) lie on this curve. If "maxSpacing" is given (not zero),
		// then the distance between control points along
		// the generated curve shall be not more than "maxSpacing". If
		// "maxOffset" is given (not zero), the distance between
		// generated curve at any point and the original curve shall not be more
		// than the "maxOffset". If both parameters are
		// set, then both criteria shall be met. If the original control points
		// of the curve lie on the curve, then they shall be
		// included in the returned LineString's controlPoints. If both
		// parameters are set to zero, then the line string
		// returned shall be constructed from the control points of the original
		// curve.
		// GenericCurve::asLineString(spacing : Distance = 0, offset : Distance
		// = 0)
		// : LineString
		// NOTE This function is useful in creating linear approximations of the
		// curve for simple actions such as display. It is often
		// referred to as a "stroked curve". For this purpose, the "maxOffset"
		// version is useful in maintaining a minimal representation of
		// the curve appropriate for the display device being targeted. This
		// function is also useful in preparing to transform a curve from
		// one coordinate reference system to another by transforming its
		// control points. In this case, the "maxSpacing" version is more
		// appropriate. Allowing both parameters to default to zero does not
		// seem to have any useful geographic nor geometric
		// interpretation unless further information is known about how the
		// curves were constructed.

		List<Primitive> primitives = this.getElements();
		if (primitives == null || primitives.isEmpty())
			return null;
		// JR error: parameter maxSpacing and maxOffset were not passed
		LineStringImpl result = ((CurveImpl) primitives.get(0)).asLineString(
				maxSpacing, maxOffset);
		for (int i = 0; i < primitives.size(); ++i) {
			CurveImpl curve = ((CurveImpl) primitives.get(i));
			result = result.merge(curve.asLineString(maxSpacing, maxOffset));
		}
		return result;

	}

	/**
	 * @return length
	 */
	public double getLength() {
		List<OrientableCurve> tmpCurves = this.getGenerators();
		if (tmpCurves.isEmpty())
			return 0.0;

		double length = 0.0;
		/* Add envelopes of the other Curves */
		for (int i = 1; i < tmpCurves.size(); ++i) {
			length = DoubleOperation.add(length, ((CurveImpl) tmpCurves.get(i)).length());
			//length += ((CurveImpl) tmpCurves.get(i)).length();
		}
		return length;
	}

	/**
	 * @param distance
	 */
	public void split(double distance) {
		for (Primitive primitive : this.getElements()) {
			((CurveImpl) primitive).split(distance);
		}
	}

	/* (non-Javadoc)
	 * @see org.geotools.geometry.featgeom.root.GeometryImpl#clone()
	 */
	public CompositeCurveImpl clone() throws CloneNotSupportedException {
		// TODO Test
		Iterator<Curve> elementIter = (Iterator<Curve>) this.elements.iterator();
		List<OrientableCurve> newElements = new ArrayList<OrientableCurve>();
		while (elementIter.hasNext()) {
			newElements.add((Curve) elementIter.next().clone());
		}
		return (CompositeCurveImpl) this.getGeometryFactory().getComplexFactory().createCompositeCurve(newElements);
	}

	/* (non-Javadoc)
	 * @see org.opengis.spatialschema.geometry.root.Geometry#isSimple()
	 */
	public boolean isSimple() {
		// TODO semantic SJ, JR
		// TODO implementation
		// TODO test
		// TODO documentation
		return false;
	}

	/* (non-Javadoc)
	 * @see org.opengis.spatialschema.geometry.root.Geometry#isCycle()
	 */
	public boolean isCycle() {
		DirectPosition start = ((Curve)this.getGenerators().get(0)).getStartPoint();
		DirectPosition end = ((Curve)this.getGenerators().get(this.elements.size())).getEndPoint();
		// Compare start point with end point
		return (start.equals(end));
	}

	/* (non-Javadoc)
	 * @see org.opengis.spatialschema.geometry.primitive.OrientableCurve#getComposite()
	 */
	public CompositeCurve getComposite() {
		// TODO semantic SJ, JR
		// TODO implementation
		// TODO test
		// TODO documentation
		return null;
	}

	/* (non-Javadoc)
	 * @see org.opengis.spatialschema.geometry.primitive.OrientablePrimitive#getOrientation()
	 */
	public int getOrientation() {
		// TODO semantic SJ, JR
		// TODO implementation
		// TODO test
		// TODO documentation
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.opengis.spatialschema.geometry.primitive.OrientablePrimitive#getPrimitive()
	 */
	public Primitive getPrimitive() {
		// TODO semantic SJ, JR
		// TODO implementation
		// TODO test
		// TODO documentation
		return null;
	}

	/* (non-Javadoc)
	 * @see org.opengis.spatialschema.geometry.primitive.Primitive#getContainedPrimitives()
	 */
	public Set getContainedPrimitives() {
		// TODO semantic SJ, JR
		// TODO implementation
		// TODO test
		// TODO documentation
		return null;
	}

	/* (non-Javadoc)
	 * @see org.opengis.spatialschema.geometry.primitive.Primitive#getContainingPrimitives()
	 */
	public Set getContainingPrimitives() {
		// TODO semantic SJ, JR
		// TODO implementation
		// TODO test
		// TODO documentation
		return null;
	}

	/* (non-Javadoc)
	 * @see org.opengis.spatialschema.geometry.primitive.Primitive#getComplexes()
	 */
	public Set getComplexes() {
		// TODO semantic SJ, JR
		// TODO implementation
		// TODO test
		// TODO documentation
		return null;
	}

	/* (non-Javadoc)
	 * @see org.opengis.spatialschema.geometry.primitive.Primitive#getProxy()
	 */
	public OrientablePrimitive[] getProxy() {
		// TODO semantic SJ, JR
		// TODO implementation
		// TODO test
		// TODO documentation
		return null;
	}

	/* (non-Javadoc)
	 * @see org.opengis.spatialschema.geometry.complex.Composite#getGenerators()
	 */
	public List<OrientableCurve> getGenerators() {
		// ok
		// Return the curves which define this CompositeCurve
		return (List<OrientableCurve>) this.elements;
	}

	/* (non-Javadoc)
	 * @see org.geotools.geometry.featgeom.root.GeometryImpl#getDimension(org.opengis.spatialschema.geometry.DirectPosition)
	 */
	public int getDimension(DirectPosition point) {
		// TODO semantic SJ, JR
		// TODO implementation
		// TODO test
		// TODO documentation
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.geotools.geometry.featgeom.root.GeometryImpl#getRepresentativePoint()
	 */
	public DirectPosition getRepresentativePoint() {
		// Use representative point of the first curve of the generator list
		return this.elements.get(0).getRepresentativePoint();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return GeometryToString.getString(this);
	}

}
