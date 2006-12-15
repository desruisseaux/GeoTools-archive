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
package org.geotools.geometry.iso.util.algorithm2D;

import java.util.Iterator;
import java.util.List;

import org.geotools.geometry.iso.FeatGeomFactoryImpl;
import org.geotools.geometry.iso.aggregate.MultiSurfaceImpl;
import org.geotools.geometry.iso.coordinate.DirectPositionImpl;
import org.geotools.geometry.iso.primitive.CurveImpl;
import org.geotools.geometry.iso.primitive.RingImpl;
import org.geotools.geometry.iso.primitive.SurfaceBoundaryImpl;
import org.geotools.geometry.iso.primitive.SurfaceImpl;
import org.geotools.geometry.iso.root.GeometryImpl;
import org.opengis.spatialschema.geometry.primitive.OrientableSurface;



/**
 * Computes the centroid of an area geometry.
 * <h2>Algorithm</h2>
 * Based on the usual algorithm for calculating the centroid as a weighted sum
 * of the centroids of a decomposition of the area into (possibly overlapping)
 * triangles. The algorithm has been extended to handle holes and
 * multi-polygons. See
 * <code>http://www.faqs.org/faqs/graphics/algorithms-faq/</code> for further
 * details of the basic approach.
 */
public class CentroidArea2D {

	private FeatGeomFactoryImpl factory = null;
	
	// the point all triangles are based at
	private DirectPositionImpl basePt = null;

	// partial area sum
	private double areasum2 = 0;

	// partial centroid sum
	//private DirectPositionImpl cg3 = new DirectPositionImpl();
	double centSumX = 0.0;
	double centSumY = 0.0;
	double centSumZ = 0.0;
	

	/**
	 * Creates a new Centroid operation
	 * 
	 * @param factory
	 */
	public CentroidArea2D(FeatGeomFactoryImpl factory) {
		this.factory = factory;
		this.basePt = null;
		
	}

	/**
	 * Adds the area defined by a Geometry to the centroid total. If the
	 * geometry has no area it does not contribute to the centroid.
	 * 
	 * @param geom
	 *            the geometry to add
	 */
	public void add(GeometryImpl geom) {
		if (geom instanceof SurfaceImpl) {
			SurfaceBoundaryImpl sb = ((SurfaceImpl) geom).getBoundary();
			this.setBasePoint((DirectPositionImpl) ((CurveImpl)sb.getExterior().getGenerators().get(0)).getStartPoint());
			this.addSurface(sb);
		} else if (geom instanceof MultiSurfaceImpl) {
			Iterator<OrientableSurface> surfaces = ((MultiSurfaceImpl) geom).getElements().iterator();
			while (surfaces.hasNext()) {
				this.add((GeometryImpl) surfaces.next());
			}
		}
	}

	public DirectPositionImpl getCentroid() {
		DirectPositionImpl centroid = this.factory.getCoordinateFactory().createDirectPosition();
		centroid.setX(this.centSumX / 3 / this.areasum2);
		centroid.setY(this.centSumY / 3 / this.areasum2);
		return centroid;
	}

	private void setBasePoint(DirectPositionImpl basePt) {
		if (this.basePt == null)
			this.basePt = basePt;
	}

	private void addSurface(SurfaceBoundaryImpl sb) {
		this.addShell(sb.getExterior().asDirectPositions());
		for (int i = 0; i < sb.getInteriors().size(); i++) {
			this.addHole(((RingImpl)sb.getInteriors().get(i)).asDirectPositions());
		}
	}

	private void addShell(List<DirectPositionImpl> pts) {
		boolean isPositiveArea = !CGAlgorithms.isCCW(pts);
		for (int i = 0; i < pts.size() - 1; i++) {
			addTriangle(basePt, pts.get(i), pts.get(i+1), isPositiveArea);
		}
	}

	private void addHole(List<DirectPositionImpl> pts) {
		boolean isPositiveArea = CGAlgorithms.isCCW(pts);
		for (int i = 0; i < pts.size() - 1; i++) {
			addTriangle(basePt, pts.get(i), pts.get(i+1), isPositiveArea);
		}
	}

	private void addTriangle(DirectPositionImpl p0, DirectPositionImpl p1, DirectPositionImpl p2,
			boolean isPositiveArea) {
		double sign = (isPositiveArea) ? 1.0 : -1.0;

		//this.centroid3(p0, p1, p2);
		double tempSumX = p0.getX() + p1.getX() + p2.getX();
		double tempSumY = p0.getY() + p1.getY() + p2.getY();
		//double tempSumZ = 0.0;

		double area2 = area2(p0, p1, p2);
		this.centSumX += sign * area2 * tempSumX;
		this.centSumY += sign * area2 * tempSumY;
		this.areasum2 += sign * area2;
	}

	/**
	 * Returns twice the signed area of the triangle p1-p2-p3, positive if a,b,c
	 * are oriented ccw, and negative if cw.
	 */
	private static double area2(DirectPositionImpl p1, DirectPositionImpl p2, DirectPositionImpl p3) {
		return (p2.getX() - p1.getX()) * (p3.getY() - p1.getY()) - (p3.getX() - p1.getX()) * (p2.getY() - p1.getY());
	}

}
