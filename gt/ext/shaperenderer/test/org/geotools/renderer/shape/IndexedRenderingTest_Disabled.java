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
 * @source $URL$
 */
package org.geotools.renderer.shape;

import com.vividsolutions.jts.geom.Envelope;
import junit.framework.TestCase;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.Lock;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileRendererUtil;
import org.geotools.data.shapefile.indexed.ShapeFileIndexer;
import org.geotools.filter.IllegalFilterException;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.resources.TestData;
import org.geotools.styling.Style;
import java.io.IOException;


public class IndexedRenderingTest_Disabled extends TestCase {
    private static final boolean INTERACTIVE = false;
    private Lock lock = new Lock();

    protected void tearDown() {
        try {
            TestData.file(IndexedRenderingTest_Disabled.class, "lakes.qix")
                    .delete();
        } catch (IOException e) {
        }

        try {
            TestData.file(IndexedRenderingTest_Disabled.class, "lakes.grx")
                    .delete();
        } catch (IOException e) {
        }

        try {
            TestData.file(IndexedRenderingTest_Disabled.class, "streams.qix")
                    .delete();
        } catch (IOException e) {
        }

        try {
            TestData.file(IndexedRenderingTest_Disabled.class, "streams.grx")
                    .delete();
        } catch (IOException e) {
        }
    }

    public void testQuadTree() throws Exception {
        ShapeFileIndexer indexer = new ShapeFileIndexer();
        indexer.setIdxType(ShapeFileIndexer.QUADTREE);
        indexer.setShapeFileName(TestData.file(
                IndexedRenderingTest_Disabled.class, "lakes.shp").getPath());
        indexer.index(true, lock);

        performRenderTest(IndexInfo.QUAD_TREE,
            TestUtilites.getDataStore("lakes.shp").getFeatureSource(),
            TestUtilites.createTestStyle("lakes", null), 100);
        performRenderTest(IndexInfo.QUAD_TREE,
            TestUtilites.getDataStore("lakes.shp").getFeatureSource(),
            TestUtilites.createTestStyle("lakes", null),
            new Envelope(553330.3289999997, 570423.7210000004,
                5233544.282249999, 5245830.157749998), 91);

        indexer.setShapeFileName(TestData.file(
                IndexedRenderingTest_Disabled.class, "streams.shp").getPath());
        indexer.index(true, lock);

        performRenderTest(IndexInfo.QUAD_TREE,
            TestUtilites.getDataStore("streams.shp").getFeatureSource(),
            TestUtilites.createTestStyle(null, "streams"), 116);

        performRenderTest(IndexInfo.QUAD_TREE,
            TestUtilites.getDataStore("streams.shp").getFeatureSource(),
            TestUtilites.createTestStyle(null, "streams"),
            new Envelope(552383.9726608695, 569483.1330782612,
                5233875.318546738, 5246165.340096739), 97);
    }

    private void performRenderTest(byte treeType, FeatureSource ds, Style s,
        int expectedFeatures)
        throws IOException, IllegalFilterException, Exception {
        performRenderTest(treeType, ds, s, null, expectedFeatures);
    }

    /**
     * DOCUMENT ME!
     *
     * @param treeType DOCUMENT ME!
     * @param ds DOCUMENT ME!
     * @param s DOCUMENT ME!
     * @param env DOCUMENT ME!
     * @param expectedFeatures DOCUMENT ME!
     *
     * @throws IOException
     * @throws IllegalFilterException
     * @throws Exception
     */
    private void performRenderTest(byte treeType, FeatureSource ds, Style s,
        Envelope env, int expectedFeatures)
        throws IOException, IllegalFilterException, Exception {
        MapContext map = new DefaultMapContext();
        map.addLayer(ds, s);

        ShapefileRenderer renderer = new ShapefileRenderer(map);
        assertEquals(treeType, renderer.layerIndexInfo[0].treeType);
        map.setAreaOfInterest(map.getLayerBounds(),
            ds.getSchema().getDefaultGeometry().getCoordinateSystem());

        if (env == null) {
            env = map.getLayerBounds();
            env = new Envelope(env.getMinX(), env.getMaxX(), env.getMinY(),
                    env.getMaxY());
        }

        IndexInfo.Reader reader = new IndexInfo.Reader(renderer.layerIndexInfo[0],
                ShapefileRendererUtil.getShpReader(
                    (ShapefileDataStore) ds.getDataStore(), null, null, false),
                new Envelope(env.getMinX() + 10, env.getMaxX() - 10,
                    env.getMinY() + 10, env.getMaxY() - 10));
        assertNotNull("Should find records", reader.goodRecs);
        TestUtilites.INTERACTIVE = INTERACTIVE;
        TestUtilites.showRender("testQuadTree", renderer, 1000, env,
            expectedFeatures);
    }

    public void testRTree() throws Exception {
        String file = TestData.file(IndexedRenderingTest_Disabled.class,
                "lakes.shp").toString();
        ShapeFileIndexer indexer = new ShapeFileIndexer();
        indexer.setIdxType(ShapeFileIndexer.RTREE);
        indexer.setShapeFileName(file);
        indexer.index(true, lock);

        performRenderTest(IndexInfo.R_TREE,
            TestUtilites.getDataStore("lakes.shp").getFeatureSource(),
            TestUtilites.createTestStyle("lakes", null), 100);

        performRenderTest(IndexInfo.R_TREE,
            TestUtilites.getDataStore("lakes.shp").getFeatureSource(),
            TestUtilites.createTestStyle("lakes", null),
            new Envelope(552238.3401249995, 570174.7868350003,
                5233300.242658593, 5246192.063731406), 93);

        indexer.setShapeFileName(TestData.file(
                IndexedRenderingTest_Disabled.class, "streams.shp").getPath());
        indexer.index(true, lock);

        performRenderTest(IndexInfo.R_TREE,
            TestUtilites.getDataStore("streams.shp").getFeatureSource(),
            TestUtilites.createTestStyle(null, "streams"), 116);

        performRenderTest(IndexInfo.R_TREE,
            TestUtilites.getDataStore("streams.shp").getFeatureSource(),
            TestUtilites.createTestStyle(null, "streams"),
            new Envelope(552383.9726608695, 569483.1330782612,
                5233875.318546738, 5246165.340096739), 97);
    }
}
