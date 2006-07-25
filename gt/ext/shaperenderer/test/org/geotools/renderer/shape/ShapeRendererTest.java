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
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileRendererUtil;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
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
 * @source $URL$
 */
public class ShapeRendererTest extends TestCase {
	private static final boolean INTERACTIVE = false;
    private File shp2;
    private File shx2;
    private File prj2;
    private File dbf2;
    private String typename;

    protected void setUp() throws Exception {
        Logger.getLogger("org.geotools.data.shapefile").setLevel(Level.FINE);
        File shp=new File(TestData.url(Rendering2DTest.class, "theme1.shp").getFile() );
        File shx=new File(TestData.url(Rendering2DTest.class, "theme1.shx").getFile() );
        File prj=new File(TestData.url(Rendering2DTest.class, "theme1.prj").getFile() );
        File dbf=new File(TestData.url(Rendering2DTest.class, "theme1.dbf").getFile() );

        File directory = new File(Rendering2DTest.class.getResource("test-data").getFile());
        shp2=File.createTempFile("theme2", ".shp", directory);
        typename=shp2.getName().substring(0, shp2.getName().lastIndexOf("."));
        shx2=new File(directory, typename+".shx");
        prj2=new File(directory, typename+".prj");
        dbf2=new File(directory, typename+".dbf");

        copy(shp,shp2);
        copy(shx,shx2);
        copy(prj,prj2);
        copy(dbf,dbf2);
    }

    protected void tearDown() throws Exception {
        shp2.delete();
        shp2.deleteOnExit();
        shx2.delete();
        shx2.deleteOnExit();
        prj2.delete();
        prj2.deleteOnExit();
        dbf2.delete();
        dbf2.deleteOnExit();

    }

    void copy(File src, File dst) throws IOException {
        InputStream in = null;
        OutputStream out = null;

        try {
            in = new FileInputStream(src);
            out = new FileOutputStream(dst,false);

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
		renderer.dbfheader = ShapefileRendererUtil.getDBFReader(ds).getHeader();
		FeatureType type = renderer.createFeatureType(null, style, ds.getSchema());
		assertEquals("NAME", type.getAttributeType(0).getName());
		assertEquals(2, type.getAttributeCount());
		Feature feature = renderer.createFeature(type, ShapefileRendererUtil
				.getShpReader(ds, ds.getFeatureSource().getBounds(), null, false)
				.nextRecord(), ShapefileRendererUtil.getDBFReader(ds), "id");
		assertEquals("id", feature.getID());
		assertEquals("dave street", feature.getAttribute(0));
	}

	public void testRemoveTransaction() throws Exception{
		ShapefileDataStore ds=TestUtilites.getDataStore(shp2.getName());
		Style st=TestUtilites.createTestStyle(null, typename);
		final FeatureStore store=(FeatureStore) ds.getFeatureSource();
		Transaction t=new DefaultTransaction();
		store.setTransaction(t);
		FeatureCollection collection=store.getFeatures();
		FeatureIterator iter=collection.features();
		store.removeFeatures(TestUtilites.filterFactory.createFidFilter(iter.next().getID()));		
		collection.close(iter);
		
		MapContext context=new DefaultMapContext();
		context.addLayer(store,st);
		ShapefileRenderer renderer=new ShapefileRenderer(context);
		TestUtilites.CountingRenderListener listener=new TestUtilites.CountingRenderListener();
		renderer.addRenderListener(listener);
		Envelope env = context.getLayerBounds();
        int boundary=7;
        TestUtilites.INTERACTIVE=INTERACTIVE;
        env = new Envelope(env.getMinX() - boundary, env.getMaxX() + boundary, 
        		env.getMinY() - boundary, env.getMaxY() + boundary);
		TestUtilites.showRender("testTransaction", renderer, 2000, env);
		assertEquals(2,listener.count);
		t.commit();

        collection=store.getFeatures();
        iter=collection.features();
        final Feature feature=iter.next();      
        collection.close(iter);
        
        // now add a new feature new fid should be theme2.4 remove it and assure that it is not rendered
        store.addFeatures(new FeatureReader(){

            private boolean more=true;

            public FeatureType getFeatureType() {
                    return store.getSchema();
            }

            public Feature next() throws IOException, IllegalAttributeException, NoSuchElementException {
                more=false;
                return store.getSchema().create(feature.getAttributes(new Object[feature.getNumberOfAttributes()]), "newFeature");
            }

            public boolean hasNext() throws IOException {
                return more;
            }

            public void close() throws IOException {
                //do nothing
            }
            
        });
        t.commit();
        listener.count=0;
        TestUtilites.showRender("testTransaction", renderer, 2000, env);
        assertEquals(3,listener.count);
        
        iter=store.getFeatures().features();
        Feature last=null;
        while( iter.hasNext() ){
            last=iter.next();
        }
        iter.close();

        store.removeFeatures(TestUtilites.filterFactory.createFidFilter(last.getID()));

        listener.count=0;
        TestUtilites.showRender("testTransaction", renderer, 2000, env);
        assertEquals(2,listener.count);
        
	}

	public void testAddTransaction() throws Exception{
		final ShapefileDataStore ds=TestUtilites.getDataStore(shp2.getName());
		Style st=TestUtilites.createTestStyle(null, typename);
		FeatureStore store=(FeatureStore) ds.getFeatureSource();
		Transaction t=new DefaultTransaction();
		store.setTransaction(t);
		FeatureCollection collection=store.getFeatures();
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
		ShapefileRenderer renderer=new ShapefileRenderer(context);
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
		ShapefileDataStore ds=TestUtilites.getDataStore(shp2.getName());
		Style st=TestUtilites.createTestStyle(null, typename);
		FeatureStore store=(FeatureStore) ds.getFeatureSource();
		Transaction t=new DefaultTransaction();
		store.setTransaction(t);
		FeatureCollection collection=store.getFeatures().collection();
		FeatureIterator iter=collection.features();
		store.modifyFeatures(ds.getSchema().getAttributeType("NAME"), "bleep", Filter.NONE);
		collection.close(iter);
		
		MapContext context=new DefaultMapContext();
		context.addLayer(store,st);
		ShapefileRenderer renderer=new ShapefileRenderer(context);
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