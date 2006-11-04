/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.renderer.shape;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.geotools.TestData;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileRendererUtil;
import org.geotools.data.shapefile.dbf.IndexedDbaseFileReader;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.renderer.RenderListener;
import org.geotools.styling.Style;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Tests ShapeRenderer class
 * 
 * @author jeichar
 * @since 2.1.x
 * @source $URL:
 *         http://svn.geotools.org/geotools/branches/2.2.x/ext/shaperenderer/test/org/geotools/renderer/shape/ShapeRendererTest.java $
 */
public class ShapeRendererTest extends TestCase {
    private File shp2;

    private File shx2;

    private File prj2;

    private File dbf2;

    private String typename;

    private File directory;

    protected void setUp() throws Exception {
        Logger.getLogger("org.geotools.data.shapefile").setLevel(Level.FINE);
        File shp = new File(TestData.url(Rendering2DTest.class, "theme1.shp")
                .getFile());
        File shx = new File(TestData.url(Rendering2DTest.class, "theme1.shx")
                .getFile());
        File prj = new File(TestData.url(Rendering2DTest.class, "theme1.prj")
                .getFile());
        File dbf = new File(TestData.url(Rendering2DTest.class, "theme1.dbf")
                .getFile());

        directory = TestData.file(Rendering2DTest.class, ".");
        
        shp2 = File.createTempFile("theme2", ".shp", directory);
        typename = shp2.getName().substring(0, shp2.getName().lastIndexOf("."));
        shx2 = new File(directory, typename + ".shx");
        prj2 = new File(directory, typename + ".prj");
        dbf2 = new File(directory, typename + ".dbf");

        copy(shp, shp2);
        copy(shx, shx2);
        copy(prj, prj2);
        copy(dbf, dbf2);
    }

    protected void tearDown() throws Exception {
        dbf2.deleteOnExit();
        shx2.deleteOnExit();
        shp2.deleteOnExit();
        prj2.deleteOnExit();
        File fix=new File( directory, typename+".fix");
        File qix=new File( directory, typename+".qix");
        
        if( shp2.exists() && !shp2.delete() )
            System.out.println("failed to delete: "+shp2.getAbsolutePath());
        if( shx2.exists() && !shx2.delete() )
            System.out.println("failed to delete: "+shx2.getAbsolutePath());

        if( prj2.exists() && !prj2.delete()) 
            System.out.println("failed to delete: "+prj2.getAbsolutePath());

        if( dbf2.exists() && !dbf2.delete() )
            System.out.println("failed to delete: "+dbf2.getAbsolutePath());
        
        if( fix.exists() && !fix.delete() ){
            fix.deleteOnExit();
            System.out.println("failed to delete: "+fix.getAbsolutePath());
        }
        if( qix.exists() && !qix.delete() ){
            qix.deleteOnExit();
            System.out.println("failed to delete: "+qix.getAbsolutePath());
        }
    }

    void copy(File src, File dst) throws IOException {
        InputStream in = null;
        OutputStream out = null;

        try {
            in = new FileInputStream(src);
            out = new FileOutputStream(dst, false);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;

            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } finally {
            if (in != null) {
                in.close();
            }

            if (out != null) {
                out.close();
            }
        }
    }

    public void testCreateFeature() throws Exception {
        ShapefileRenderer renderer = new ShapefileRenderer(null);
        Style style = LabelingTest.loadStyle("LineStyle.sld");
        ShapefileDataStore ds = TestUtilites.getDataStore(shp2.getName());
        IndexedDbaseFileReader reader = ShapefileRendererUtil
                        .getDBFReader(ds);
        renderer.dbfheader = reader.getHeader();
        FeatureType type = renderer.createFeatureType(null, style, ds
                .getSchema());
        assertEquals("NAME", type.getAttributeType(0).getName());
        assertEquals(2, type.getAttributeCount());
        Envelope bounds = ds.getFeatureSource().getBounds();
        ShapefileReader shpReader = ShapefileRendererUtil
                        .getShpReader(ds, bounds, 
                                new Rectangle(0,0,(int)bounds.getWidth(), (int)bounds.getHeight()),
                                null, false);
        Feature feature = renderer.createFeature(type, shpReader.nextRecord(), reader, "id");
        shpReader.close();
        reader.close();
        
        assertEquals("id", feature.getID());
        assertEquals("dave street", feature.getAttribute(0));
    }

    public void testRemoveTransaction() throws Exception {
        ShapefileDataStore ds = TestUtilites.getDataStore(shp2.getName());
        Style st = TestUtilites.createTestStyle(null, typename);
        final FeatureStore store = (FeatureStore) ds.getFeatureSource();
        Transaction t = new DefaultTransaction();
        store.setTransaction(t);
        FeatureCollection collection = store.getFeatures();
        FeatureIterator iter = collection.features();
        FidFilter createFidFilter = TestUtilites.filterFactory.createFidFilter(iter
                        .next().getID());
        collection.close(iter);
        store.removeFeatures(createFidFilter);

        MapContext context = new DefaultMapContext();
        context.addLayer(store, st);
        ShapefileRenderer renderer = new ShapefileRenderer(context);
        TestUtilites.CountingRenderListener listener = new TestUtilites.CountingRenderListener();
        renderer.addRenderListener(listener);
        Envelope env = context.getLayerBounds();
        int boundary = 7;
        env = new Envelope(env.getMinX() - boundary, env.getMaxX() + boundary,
                env.getMinY() - boundary, env.getMaxY() + boundary);
        TestUtilites.showRender("testTransaction", renderer, 2000, env);
        assertEquals(2, listener.count);
        t.commit();

        collection = store.getFeatures();
        iter = collection.features();
        final Feature feature = iter.next();
        collection.close(iter);

        // now add a new feature new fid should be theme2.4 remove it and assure
        // that it is not rendered
        store.addFeatures(DataUtilities.collection(new Feature[] { store
                .getSchema().create(
                        feature.getAttributes(new Object[feature
                                .getNumberOfAttributes()]), "newFeature") })); //$NON-NLS-1$
        t.commit();
        listener.count = 0;
        TestUtilites.showRender("testTransaction", renderer, 2000, env);
        assertEquals(3, listener.count);

        iter = store.getFeatures().features();
        Feature last = null;
        while (iter.hasNext()) {
            last = iter.next();
        }
        iter.close();

        store.removeFeatures(TestUtilites.filterFactory.createFidFilter(last
                .getID()));

        listener.count = 0;
        TestUtilites.showRender("testTransaction", renderer, 2000, env);
        assertEquals(2, listener.count);

    }

    public void testAddTransaction() throws Exception {
        final ShapefileDataStore ds = TestUtilites.getDataStore(shp2.getName());
        Style st = TestUtilites.createTestStyle(null, typename);
        FeatureStore store = (FeatureStore) ds.getFeatureSource();
        Transaction t = new DefaultTransaction();
        store.setTransaction(t);
        FeatureCollection collection = store.getFeatures();
        FeatureIterator iter = collection.features();
        final Feature feature = iter.next();
        collection.close(iter);

        store.addFeatures(DataUtilities.collection(new Feature[] { ds
                .getSchema().create(
                        feature.getAttributes(new Object[feature
                                .getNumberOfAttributes()]), "newFeature") }));

        MapContext context = new DefaultMapContext();
        context.addLayer(store, st);
        ShapefileRenderer renderer = new ShapefileRenderer(context);
        TestUtilites.CountingRenderListener listener = new TestUtilites.CountingRenderListener();
        renderer.addRenderListener(listener);
        Envelope env = context.getLayerBounds();
        int boundary = 7;
        env = new Envelope(env.getMinX() - boundary, env.getMaxX() + boundary,
                env.getMinY() - boundary, env.getMaxY() + boundary);
        TestUtilites.showRender("testTransaction", renderer, 2000, env);

        assertEquals(4, listener.count);
    }

    public void testModifyTransaction() throws Exception {
        ShapefileDataStore ds = TestUtilites.getDataStore(shp2.getName());
        Style st = TestUtilites.createTestStyle(null, typename);
        FeatureStore store = (FeatureStore) ds.getFeatureSource();
        Transaction t = new DefaultTransaction();
        store.setTransaction(t);
        store.modifyFeatures(ds.getSchema().getAttributeType("NAME"), "bleep",
                Filter.INCLUDE);

        MapContext context = new DefaultMapContext();
        context.addLayer(store, st);
        ShapefileRenderer renderer = new ShapefileRenderer(context);
        TestUtilites.CountingRenderListener listener = new TestUtilites.CountingRenderListener();
        renderer.addRenderListener(listener);
        renderer.addRenderListener(new RenderListener() {

            public void featureRenderer(Feature feature) {
                assertEquals("bleep", feature.getAttribute("NAME"));
            }

            public void errorOccurred(Exception e) {
                assertFalse(true);
            }

        });
        Envelope env = context.getLayerBounds();
        int boundary = 7;
        env = new Envelope(env.getMinX() - boundary, env.getMaxX() + boundary,
                env.getMinY() - boundary, env.getMaxY() + boundary);
        TestUtilites.showRender("testTransaction", renderer, 2000, env);

        assertEquals(3, listener.count);
    }
}