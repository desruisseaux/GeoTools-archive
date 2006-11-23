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

import java.util.Set;

import org.geotools.geometry.iso.FeatGeomFactoryImpl;
import org.geotools.geometry.iso.coordinate.EnvelopeImpl;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.complex.Complex;
import org.opengis.spatialschema.geometry.primitive.CurveBoundary;
import org.opengis.spatialschema.geometry.primitive.Point;

/**
 * The boundary of Curves shall be represented as CurveBoundary.
 * 
 * @author Jackson Roehrig & Sanjay Jena
 */
public class CurveBoundaryImpl extends PrimitiveBoundaryImpl implements
		CurveBoundary {

	/**
	 * 
	 * startPoint, endPoint A CurveBoundary contains two Point references.
	 * 
	 * CurveBoundary::startPoint : Reference<Point>; CurveBoundary::endPoint :
	 * Reference<Point>;
	 * 
	 */
	private Point startPoint = null;

	private Point endPoint = null;

	/**
	 * Constructor
	 * 
	 * @param factory
	 * @param start
	 * @param end
	 */
	public CurveBoundaryImpl(FeatGeomFactoryImpl factory, Point start, Point end) {
		super(factory);
		if (start.equals(end))
			throw new IllegalArgumentException("Start- and Endpoint of the CurveBoundary cannot be equal");
		this.startPoint = start;
		this.endPoint = end;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.geometry.featgeom.root.GeometryImpl#clone()
	 */
	public CurveBoundaryImpl clone() throws CloneNotSupportedException {
		// ok
		// Return new CurveBoundary with the cloned start and end point of this CurveBoundary
		return this.getGeometryFactory().getPrimitiveFactory().createCurveBoundary(this.getStartPoint().clone(), this.getEndPoint().clone());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.primitive.CurveBoundary#getStartPoint()
	 */
	public PointImpl getStartPoint() {
		// TODO test
		// TODO documentation
		return (PointImpl) this.startPoint;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.primitive.CurveBoundary#getEndPoint()
	 */
	public PointImpl getEndPoint() {
		// TODO test
		// TODO documentation
		return (PointImpl) this.endPoint;
	}

	public String toString() {
		return "[CurveBoundary: StartPoint: " + this.startPoint + " EndPoint: " //$NON-NLS-1$//$NON-NLS-2$
				+ this.endPoint + "]"; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.geometry.featgeom.root.GeometryImpl#getDimension(org.opengis.spatialschema.geometry.DirectPosition)
	 */
	public int getDimension(final DirectPosition point) {
		// TODO semantic JR, SJ
		// TODO What is going to happen with the direct position?
		// The dimension of a CurveBoundary is the dimension of two Points. So we return the dimension value 0. 
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.geometry.featgeom.root.GeometryImpl#getEnvelope()
	 */
	public EnvelopeImpl getEnvelope() {
		// TODO Test
		/* Build Envelope with StartPoint */
		// EnvelopeImpl tmpEnv = new EnvelopeImpl(this.startPoint.getPosition(), this.startPoint.getPosition());
		EnvelopeImpl tmpEnv = this.getGeometryFactory().getCoordinateFactory().createEnvelope(this.startPoint.getEnvelope());
		/* Extend Envelope with EndPoint */
		tmpEnv.expand(this.endPoint.getPosition().getCoordinates());
		return tmpEnv;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.geometry.featgeom.complex.ComplexImpl#createBoundary()
	 */
	public Set<Complex> createBoundary() {
		// TODO semantic JR, SJ
		// TODO implementation
		// TODO test
		// TODO documentation
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.root.Geometry#isSimple()
	 */
	public boolean isSimple() {
		// A curveBoundary (start and end point) is always simple
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.geometry.featgeom.root.GeometryImpl#getClosure()
	 */
	public Complex getClosure() {
		// TODO semantic JR, SJ
		// TODO implementation
		// TODO test
		// TODO documentation
		// A curveboundary consists of two disjoint points. a compositePoint can only represent one single point. hence we don´t have a chance to return a Complex here!
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.geometry.featgeom.root.GeometryImpl#getRepresentativePoint()
	 */
	@Override
	public DirectPosition getRepresentativePoint() {
		// Use start point of Boundary as representative point
		return this.startPoint.getPosition();
	}

}
