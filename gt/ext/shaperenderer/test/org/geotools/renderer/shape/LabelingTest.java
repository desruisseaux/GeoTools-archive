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
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.resources.TestData;
import org.geotools.styling.SLDParser;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Tests the LiteRenderer labelling algorithms
 * 
 * @author jeichar
 * @since 0.9.0
 */
public class LabelingTest extends TestCase {

	private long timout=1000;
	private static final int CENTERX = 160;
	private static final int CENTERY = 40;
    private static final boolean INTERACTIVE=false;


	static Style loadStyle(String sldFilename) throws IOException {
        StyleFactory factory = StyleFactory.createStyleFactory();

        java.net.URL surl = TestData.getResource(LabelingTest.class, sldFilename);
        SLDParser stylereader = new SLDParser(factory, surl);

        Style style = stylereader.readXML()[0];
        return style;
	}

	public void testLineLabeling() throws Exception{		
        ShapefileDataStore ds=(ShapefileDataStore) TestUtilites.getDataStore("theme1.shp");
		Style style=loadStyle("LineStyle.sld");
		assertNotNull(style);
		MapContext map = new DefaultMapContext();
        map.addLayer(ds.getFeatureSource(), style);
        ShapefileRenderer renderer = new ShapefileRenderer(map);
        Envelope env = map.getLayerBounds();
        int boundary=10;
        TestUtilites.INTERACTIVE=INTERACTIVE;
        env = new Envelope(env.getMinX() - boundary, env.getMaxX() + boundary, 
        		env.getMinY() - boundary, env.getMaxY() + boundary);
        TestUtilites.showRender("testLineLabeling", renderer, timout, env);
	}

	public void testPolyLabeling() throws Exception{		
        ShapefileDataStore ds=(ShapefileDataStore) TestUtilites.getDataStore("smallMultiPoly.shp");
        FeatureSource source=ds.getFeatureSource(ds.getTypeNames()[0]);

		Style style=loadStyle("PolyStyle.sld");
		assertNotNull(style);
		MapContext map = new DefaultMapContext();
        map.addLayer(ds.getFeatureSource(), style);
        ShapefileRenderer renderer = new ShapefileRenderer(map);
        Envelope env = map.getLayerBounds();
        int boundary=1;
        env = new Envelope(env.getMinX() - boundary, env.getMaxX() + boundary, 
        		env.getMinY() - boundary, env.getMaxY() + boundary);
        TestUtilites.INTERACTIVE=INTERACTIVE;
        TestUtilites.showRender("testPolyLabeling", renderer, timout, env);
	}

	public void testPolyLabelingZoomedOut() throws Exception{		
        ShapefileDataStore ds=(ShapefileDataStore) TestUtilites.getDataStore("smallMultiPoly.shp");
        FeatureSource source=ds.getFeatureSource(ds.getTypeNames()[0]);

		Style style=loadStyle("PolyStyle.sld");
		assertNotNull(style);
		MapContext map = new DefaultMapContext();
        map.addLayer(ds.getFeatureSource(), style);
        ShapefileRenderer renderer = new ShapefileRenderer(map);
        Envelope env = map.getLayerBounds();
        int boundary=30;
        env = new Envelope(env.getMinX() - boundary, env.getMaxX() + boundary, 
        		env.getMinY() - boundary, env.getMaxY() + boundary);
        TestUtilites.INTERACTIVE=INTERACTIVE;
        TestUtilites.showRender("testPolyLabeling", renderer, timout, env);
	}

}
