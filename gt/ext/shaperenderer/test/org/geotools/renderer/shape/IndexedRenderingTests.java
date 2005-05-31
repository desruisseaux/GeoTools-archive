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

import junit.framework.TestCase;

import org.geotools.data.FeatureSource;
import org.geotools.data.shape.ShapeFileIndexer;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileRendererUtil;
import org.geotools.filter.IllegalFilterException;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.resources.TestData;
import org.geotools.styling.Style;

import com.vividsolutions.jts.geom.Envelope;

public class IndexedRenderingTests extends TestCase {
	private static final boolean INTERACTIVE = false;

	protected void tearDown() {
		try {
			TestData.file(IndexedRenderingTests.class, "lakes.qix").delete();
		} catch (IOException e) {
		}
		try {
			TestData.file(IndexedRenderingTests.class, "lakes.grx").delete();
		} catch (IOException e) {
		}
		try {
			TestData.file(IndexedRenderingTests.class, "streams.qix").delete();
		} catch (IOException e) {
		}
		try {
			TestData.file(IndexedRenderingTests.class, "streams.grx").delete();
		} catch (IOException e) {
		}
	}
	
	public void testQuadTree() throws Exception{
		ShapeFileIndexer indexer=new ShapeFileIndexer();
		indexer.setIdxType(ShapeFileIndexer.QUADTREE);
		indexer.setShapeFileName(TestData.file(IndexedRenderingTests.class, "lakes.shp").getPath());
		indexer.index(true);
		
		performRenderTest(IndexInfo.QUAD_TREE, 
				TestUtilites.getDataStore("lakes.shp").getFeatureSource(),
        		TestUtilites.createTestStyle("lakes", null));
		
		indexer.setShapeFileName(TestData.file(IndexedRenderingTests.class, "streams.shp").getPath());
		indexer.index(true);
		
		performRenderTest(IndexInfo.QUAD_TREE, 
				TestUtilites.getDataStore("streams.shp").getFeatureSource(),
        		TestUtilites.createTestStyle(null, "streams"));
		
		
	}

	/**
	 * @throws IOException
	 * @throws IllegalFilterException
	 * @throws Exception
	 */
	private void performRenderTest(byte treeType, FeatureSource ds, Style s) throws IOException, IllegalFilterException, Exception {
		MapContext map = new DefaultMapContext();
        map.addLayer(ds,s);
        ShapeRenderer renderer = new ShapeRenderer(map);
        assertEquals(treeType, renderer.layerIndexInfo[0].treeType );
        Envelope env = map.getLayerBounds();
        env = new Envelope(env.getMinX(), env.getMaxX(), env.getMinY(), env
                .getMaxY());
        IndexInfo.Reader reader=new IndexInfo.Reader(renderer.layerIndexInfo[0],ShapefileRendererUtil.getShpReader((ShapefileDataStore) ds.getDataStore(), null, null), env);        
        assertNotNull("Should find records", reader.goodRecs);
        TestUtilites.INTERACTIVE=INTERACTIVE;
        TestUtilites.showRender("testQuadTree", renderer, 1000, env);
	}
	
	public void testRTree() throws Exception{
		String file=TestData.file(IndexedRenderingTests.class, "lakes.shp").toString();
		ShapeFileIndexer indexer=new ShapeFileIndexer();
		indexer.setIdxType(ShapeFileIndexer.RTREE);
		indexer.setShapeFileName(file);
		indexer.index(true);
		
		performRenderTest(IndexInfo.R_TREE, 
				TestUtilites.getDataStore("lakes.shp").getFeatureSource(),
        		TestUtilites.createTestStyle("lakes", null));
		
		indexer.setShapeFileName(TestData.file(IndexedRenderingTests.class, "streams.shp").getPath());
		indexer.index(true);
		
		performRenderTest(IndexInfo.R_TREE, 
				TestUtilites.getDataStore("streams.shp").getFeatureSource(),
        		TestUtilites.createTestStyle(null, "streams"));
	}
}
