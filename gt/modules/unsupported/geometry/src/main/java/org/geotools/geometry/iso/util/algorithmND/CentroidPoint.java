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
package org.geotools.geometry.iso.util.algorithmND;

import java.util.Iterator;

import org.geotools.geometry.iso.FeatGeomFactoryImpl;
import org.geotools.geometry.iso.aggregate.MultiPointImpl;
import org.geotools.geometry.iso.complex.CompositePointImpl;
import org.geotools.geometry.iso.coordinate.DirectPositionImpl;
import org.geotools.geometry.iso.primitive.PointImpl;
import org.geotools.geometry.iso.root.GeometryImpl;
import org.opengis.spatialschema.geometry.primitive.Point;

/**
 * Computes the centroid of a point geometry.
 * <h2>Algorithm</h2>
 * Compute the average of all points.
 */
public class CentroidPoint {
	
	private FeatGeomFactoryImpl factory = null;
	
	private int ptCount = 0;

	DirectPositionImpl centSum = null;

	/**
	 * Creates a new Centroid operation
	 * 
	 * @param factory
	 */
	public CentroidPoint(FeatGeomFactoryImpl factory) {
		this.factory = factory;
		this.centSum = this.factory.getCoordinateFactory().createDirectPosition();
	}

	/**
	 * Adds the point(s) defined by a Geometry to the centroid total. If the
	 * geometry is not of dimension 0 it does not contribute to the centroid.
	 * 
	 * @param geom
	 *            the geometry to add
	 */
	public void add(GeometryImpl geom) {
		if (geom instanceof PointImpl) {
			this.add(((PointImpl)geom).getPosition());
		} else if (geom instanceof MultiPointImpl) {
			Iterator<Point> points = ((MultiPointImpl) geom).getElements().iterator();
			while (points.hasNext()) {
				this.add((DirectPositionImpl) points.next().getPosition());
			}
		} else if (geom instanceof CompositePointImpl) {
			this.add((GeometryImpl) ((CompositePointImpl)geom).getGenerators().get(0));
		}
	}

	/**
	 * Adds the length defined by an array of coordinates.
	 * 
	 * @param pts
	 *            an array of {@link Coordinate}s
	 */
	private void add(DirectPositionImpl pt) {
		this.ptCount += 1;
		this.centSum.add(pt.getCoordinates());
	}

	/**
	 * Returns the centroid of the added points
	 * 
	 * @return Centroid position
	 */
	public DirectPositionImpl getCentroid() {
		this.centSum.divideBy(this.ptCount);
		return this.centSum;
//		DirectPositionImpl centroid = this.factory.getCoordinateFactory().createDirectPosition();
//		centroid.setCoordinate(thi);
//		centroid.setX(this.centSumX / this.ptCount);
//		centroid.setY(this.centSumY / this.ptCount);
//		return centroid;
	}

}
