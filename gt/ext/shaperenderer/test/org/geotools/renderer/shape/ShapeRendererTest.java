/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.renderer.shape;

import junit.framework.TestCase;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileRendererUtil;
import org.geotools.data.shapefile.shp.ShapeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.renderer.lite.LiteShape2;
import org.geotools.styling.Style;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @TODO class description
 * 
 * @author jeichar
 * @since 2.1.x
 */
public class ShapeRendererTest extends TestCase {

	public void testCreateFeature() throws Exception {
		ShapeRenderer renderer = new ShapeRenderer(null);
		Style style = LabelingTest.loadStyle("LineStyle.sld");
		ShapefileDataStore ds = Rendering2DTest.getLines("theme1.shp");
		renderer.dbfheader = ShapefileRendererUtil.getDBFReader(ds).getHeader();
		FeatureType type = renderer.createFeatureType(style, ds.getSchema());
		assertEquals("NAME", type.getAttributeType(0).getName());
		assertEquals(2, type.getAttributeCount());
		Feature feature = renderer.createFeature(type, ShapefileRendererUtil
				.getShpReader(ds, ds.getFeatureSource().getBounds(), null)
				.nextRecord(), ShapefileRendererUtil.getDBFReader(ds), "id");
		assertEquals("id", feature.getID());
		assertEquals("dave street", feature.getAttribute(0));
	}

	public void testGetLiteShape() throws Exception {
		double[][] coords = new double[2][];
		coords[0] = new double[] { 0.0, 0.0, 0.0, 100.0, 100.0, 100.0, 100.0,
				0.0, 10.0, 0.0 };
		coords[1] = new double[] { 300.0, 300.0, 300.0, 400.0, 400.0, 400.0,
				0.0, 0.0 };
		Envelope env = new Envelope(0, 400, 0, 400);

		ShapeRenderer renderer = new ShapeRenderer(null);

		LiteShape2 shape = renderer.getLiteShape2(new SimpleGeometry(
				ShapeType.POLYGON, coords, env));

		Geometry jtsGeom = shape.getGeometry();
		assertEquals(5, jtsGeom.getNumPoints());
		Coordinate[] coordinates = jtsGeom.getCoordinates();
		assertEquals(new Coordinate(0, 0), coordinates[0]);
		assertEquals(new Coordinate(0, 100), coordinates[1]);
		assertEquals(new Coordinate(100, 100), coordinates[2]);
		assertEquals(new Coordinate(100, 0), coordinates[3]);
		assertEquals(new Coordinate(0, 0), coordinates[4]);

		shape = renderer.getLiteShape2(new SimpleGeometry(ShapeType.ARC,
				coords, env));
		jtsGeom = shape.getGeometry();
		assertTrue(jtsGeom.getNumPoints() == 5);
		coordinates = jtsGeom.getCoordinates();
		assertEquals(new Coordinate(0, 0), coordinates[0]);
		assertEquals(new Coordinate(0, 100), coordinates[1]);
		assertEquals(new Coordinate(100, 100), coordinates[2]);
		assertEquals(new Coordinate(100, 0), coordinates[3]);
		assertEquals(new Coordinate(10, 0), coordinates[4]);

		shape = renderer.getLiteShape2(new SimpleGeometry(ShapeType.POLYGONZ,
				coords, env));
		jtsGeom = shape.getGeometry();
		assertTrue(jtsGeom.getNumPoints() == 5);
		coordinates = jtsGeom.getCoordinates();
		assertEquals(new Coordinate(0, 0), coordinates[0]);
		assertEquals(new Coordinate(0, 100), coordinates[1]);
		assertEquals(new Coordinate(100, 100), coordinates[2]);
		assertEquals(new Coordinate(100, 0), coordinates[3]);
		assertEquals(new Coordinate(0, 0), coordinates[4]);

		shape = renderer.getLiteShape2(new SimpleGeometry(ShapeType.MULTIPOINT,
				coords, env));
		jtsGeom = shape.getGeometry();
		assertTrue(jtsGeom.getNumPoints() == 5);
		coordinates = jtsGeom.getCoordinates();
		assertEquals(new Coordinate(0, 0), coordinates[0]);
		assertEquals(new Coordinate(0, 100), coordinates[1]);
		assertEquals(new Coordinate(100, 100), coordinates[2]);
		assertEquals(new Coordinate(100, 0), coordinates[3]);
		assertEquals(new Coordinate(10, 0), coordinates[4]);
		ShapeRenderer.NUM_SAMPLES=12;
		shape = renderer.getLiteShape2(new SimpleGeometry(ShapeType.POLYGON,
				new double[][] { manyCoords }, env));
		jtsGeom = shape.getGeometry();
		coordinates = jtsGeom.getCoordinates();
		assertEquals(new Coordinate(manyCoords[0], manyCoords[1]),
				coordinates[0]);
		assertEquals(new Coordinate(manyCoords[2], manyCoords[3]),
				coordinates[1]);
		assertEquals(new Coordinate(manyCoords[4], manyCoords[5]),
				coordinates[2]);
		assertEquals(new Coordinate(manyCoords[6], manyCoords[7]),
				coordinates[3]);
		assertEquals(new Coordinate(manyCoords[8], manyCoords[9]),
				coordinates[4]);
		assertEquals(new Coordinate(manyCoords[10], manyCoords[11]),
				coordinates[5]);
	}

	double[] manyCoords = new double[] { 90.8307300832239, 243.83302237886664,
			85.959938002934, 238.46064227626084, 94.3885659571406,
			236.11317091199317, 99.23509261303661, 242.5690788218758,
			98.31961497838347, 245.82484960695865, 90.8307300832239,
			243.83302237886664 };

}
