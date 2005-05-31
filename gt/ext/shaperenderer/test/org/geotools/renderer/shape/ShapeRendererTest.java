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

import java.io.IOException;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileRendererUtil;
import org.geotools.data.shapefile.shp.ShapeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.Filter;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.renderer.lite.LiteShape2;
import org.geotools.renderer.lite.RenderListener;
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
		ShapefileDataStore ds = TestUtilites.getDataStore("theme1.shp");
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

	private static final boolean INTERACTIVE = false;

	double[] manyCoords = new double[] { 90.8307300832239, 243.83302237886664,
			85.959938002934, 238.46064227626084, 94.3885659571406,
			236.11317091199317, 99.23509261303661, 242.5690788218758,
			98.31961497838347, 245.82484960695865, 90.8307300832239,
			243.83302237886664 };

	public void testRemoveTransaction() throws Exception{
		ShapefileDataStore ds=TestUtilites.getDataStore("theme1.shp");
		Style st=TestUtilites.createTestStyle(null, "theme1");
		FeatureStore store=(FeatureStore) ds.getFeatureSource();
		Transaction t=new DefaultTransaction();
		store.setTransaction(t);
		FeatureCollection collection=store.getFeatures().collection();
		FeatureIterator iter=collection.features();
		store.removeFeatures(TestUtilites.filterFactory.createFidFilter(iter.next().getID()));		
		collection.close(iter);
		
		MapContext context=new DefaultMapContext();
		context.addLayer(store,st);
		ShapeRenderer renderer=new ShapeRenderer(context);
		TestUtilites.CountingRenderListener listener=new TestUtilites.CountingRenderListener();
		renderer.addRenderListener(listener);
		Envelope env = context.getLayerBounds();
        int boundary=7;
        TestUtilites.INTERACTIVE=INTERACTIVE;
        env = new Envelope(env.getMinX() - boundary, env.getMaxX() + boundary, 
        		env.getMinY() - boundary, env.getMaxY() + boundary);
		TestUtilites.showRender("testTransaction", renderer, 2000, env);
		
		assertEquals(2,listener.count);
	}

	public void testAddTransaction() throws Exception{
		final ShapefileDataStore ds=TestUtilites.getDataStore("theme1.shp");
		Style st=TestUtilites.createTestStyle(null, "theme1");
		FeatureStore store=(FeatureStore) ds.getFeatureSource();
		Transaction t=new DefaultTransaction();
		store.setTransaction(t);
		FeatureCollection collection=store.getFeatures().collection();
		FeatureIterator iter=collection.features();
		final Feature feature=iter.next();		
		collection.close(iter);

		store.addFeatures(new FeatureReader(){

			private boolean more=true;

			public FeatureType getFeatureType() {
				try {
					return ds.getSchema();
				} catch (IOException e) {
					return null;
				}
			}

			public Feature next() throws IOException, IllegalAttributeException, NoSuchElementException {
				more=false;
				return ds.getSchema().create(feature.getAttributes(new Object[feature.getNumberOfAttributes()]), "newFeature");
			}

			public boolean hasNext() throws IOException {
				return more;
			}

			public void close() throws IOException {
				//do nothing
			}
			
		});
		
		MapContext context=new DefaultMapContext();
		context.addLayer(store,st);
		ShapeRenderer renderer=new ShapeRenderer(context);
		TestUtilites.CountingRenderListener listener=new TestUtilites.CountingRenderListener();
		renderer.addRenderListener(listener);
		Envelope env = context.getLayerBounds();
        int boundary=7;
        TestUtilites.INTERACTIVE=INTERACTIVE;
        env = new Envelope(env.getMinX() - boundary, env.getMaxX() + boundary, 
        		env.getMinY() - boundary, env.getMaxY() + boundary);
		TestUtilites.showRender("testTransaction", renderer, 2000, env);
		
		assertEquals(4,listener.count);
	}

	public void testModifyTransaction() throws Exception{
		ShapefileDataStore ds=TestUtilites.getDataStore("theme1.shp");
		Style st=TestUtilites.createTestStyle(null, "theme1");
		FeatureStore store=(FeatureStore) ds.getFeatureSource();
		Transaction t=new DefaultTransaction();
		store.setTransaction(t);
		FeatureCollection collection=store.getFeatures().collection();
		FeatureIterator iter=collection.features();
		store.modifyFeatures(ds.getSchema().getAttributeType("NAME"), "bleep", Filter.NONE);
		collection.close(iter);
		
		MapContext context=new DefaultMapContext();
		context.addLayer(store,st);
		ShapeRenderer renderer=new ShapeRenderer(context);
		TestUtilites.CountingRenderListener listener=new TestUtilites.CountingRenderListener();
		renderer.addRenderListener(listener);
		renderer.addRenderListener(new RenderListener(){

			public void featureRenderer(Feature feature) {
				assertEquals("bleep", feature.getAttribute("NAME"));
			}

			public void errorOccurred(Exception e) {
				assertFalse(true);
			}
			
		});
		Envelope env = context.getLayerBounds();
        int boundary=7;
        TestUtilites.INTERACTIVE=INTERACTIVE;
        env = new Envelope(env.getMinX() - boundary, env.getMaxX() + boundary, 
        		env.getMinY() - boundary, env.getMaxY() + boundary);
		TestUtilites.showRender("testTransaction", renderer, 2000, env);
		
		assertEquals(3,listener.count);
	}
}
