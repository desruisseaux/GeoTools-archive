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
import java.util.List;
import java.util.Set;

import org.geotools.geometry.iso.FeatGeomFactoryImpl;
import org.geotools.geometry.iso.aggregate.MultiCurveImpl;
import org.geotools.geometry.iso.complex.CompositeCurveImpl;
import org.geotools.geometry.iso.coordinate.DirectPositionImpl;
import org.geotools.geometry.iso.primitive.CurveImpl;
import org.geotools.geometry.iso.primitive.RingImpl;
import org.geotools.geometry.iso.root.GeometryImpl;
import org.opengis.spatialschema.geometry.primitive.Curve;
import org.opengis.spatialschema.geometry.primitive.OrientableCurve;


/**
 * Computes the centroid of a linear geometry.
 * <h2>Algorithm</h2>
 * Compute the average of the midpoints of all line segments weighted by the
 * segment length.
 */
public class CentroidLine {
	
	private FeatGeomFactoryImpl factory = null;
	
	DirectPositionImpl centSum = null;
	
	private double totalLength = 0.0;

	/**
	 * Creates a new Centroid operation
	 * 
	 * @param factory
	 */
	public CentroidLine(FeatGeomFactoryImpl factory) {
		this.factory = factory;
		this.centSum = this.factory.getCoordinateFactory().createDirectPosition();
	}

	/**
	 * Adds the linestring(s) defined by a Geometry to the centroid total. If
	 * the geometry is not linear it does not contribute to the centroid
	 * 
	 * @param geom
	 *            the geometry to add
	 */
	public void add(GeometryImpl geom) {
		if (geom instanceof CurveImpl) {
			this.addCurve((CurveImpl) geom);
		} else if (geom instanceof RingImpl) {
			this.addCurveIter(((RingImpl)geom).getGenerators().iterator());
		} else if (geom instanceof MultiCurveImpl) {
			this.addCurveIter(((MultiCurveImpl)geom).getElements().iterator());
		} else if (geom instanceof CompositeCurveImpl) {
			this.addCurveIter(((CompositeCurveImpl)geom).getGenerators().iterator());
		}
	}
	
	private void addCurveIter(Iterator<OrientableCurve> curveIter) {
		while (curveIter.hasNext()) {
			this.addCurve((CurveImpl) curveIter.next());
		}
	}
	
	private void addCurve(CurveImpl curve) {
		this.addPointSequence(curve.asDirectPositions());
	}

	/**
	 * Adds the length defined by an array of coordinates.
	 * 
	 * @param pts
	 *            an array of {@link Coordinate}s
	 */
	public void addPointSequence(List<DirectPositionImpl> pts) {
		
		DirectPositionImpl dpAct = pts.get(0);
		DirectPositionImpl dpNext;

		for (int i = 0; i < pts.size()-1; i++) {
			
			dpNext = pts.get(i+1);
			
			double segmentLen = dpAct.distance(dpNext);
			this.totalLength += segmentLen;
			
			DirectPositionImpl tempMid = dpAct.clone();
			tempMid.add(dpNext);
			tempMid.divideBy(2);
			tempMid.scale(segmentLen);
			this.centSum.add(tempMid);
			
			dpAct = dpNext;
		}
	}
	
	/**
	 * Returns the centroid for the added curves
	 * 
	 * @return Centroid position
	 */
	public DirectPositionImpl getCentroid() {
		this.centSum.divideBy(this.totalLength);
		return this.centSum;
	}

}
