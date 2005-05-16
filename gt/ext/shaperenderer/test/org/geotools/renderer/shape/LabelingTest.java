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
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.referencing.crs.GeographicCRS;
import org.geotools.resources.TestData;
import org.geotools.styling.SLDParser;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;

/**
 * Tests the LiteRenderer labelling algorithms
 * 
 * @author jeichar
 * @since 0.9.0
 */
public class LabelingTest extends TestCase {

	private long timout=4000;
	private static final int CENTERX = 160;
	private static final int CENTERY = 40;
    private static final boolean INTERACTIVE=true;

	
	public void disabletestPointLabeling() throws Exception{
//		FeatureCollection collection=createPointFeatureCollection();
//		Style style=loadStyle("PointStyle.sld");
//		assertNotNull(style);
//		MapContext map = new DefaultMapContext();
//        map.addLayer(collection, style);
//        ShapeRenderer renderer = new ShapeRenderer(map);
//        Envelope env = map.getLayerBounds();
//        int boundary=10;
//        env = new Envelope(env.getMinX() - boundary, env.getMaxX() + boundary, 
//        		env.getMinY() - boundary, env.getMaxY() + boundary);
////        Rendering2DTest.INTERACTIVE=INTERACTIVE;
//        Rendering2DTest.showRender("testPointLabeling", renderer, timout, env);
	}

	static Style loadStyle(String sldFilename) throws IOException {
        StyleFactory factory = StyleFactory.createStyleFactory();

        java.net.URL surl = TestData.getResource(LabelingTest.class, sldFilename);
        SLDParser stylereader = new SLDParser(factory, surl);

        Style style = stylereader.readXML()[0];
        return style;
	}

	public void testLineLabeling() throws Exception{		
        ShapefileDataStore ds=(ShapefileDataStore) Rendering2DTest.getLines("theme1.shp");
		Style style=loadStyle("LineStyle.sld");
		assertNotNull(style);
		MapContext map = new DefaultMapContext();
        map.addLayer(ds.getFeatureSource(), style);
        ShapeRenderer renderer = new ShapeRenderer(map);
        Envelope env = map.getLayerBounds();
        int boundary=10;
        Rendering2DTest.INTERACTIVE=INTERACTIVE;
        env = new Envelope(env.getMinX() - boundary, env.getMaxX() + boundary, 
        		env.getMinY() - boundary, env.getMaxY() + boundary);
        Rendering2DTest.showRender("testLineLabeling", renderer, timout, env);
	}

	public void testPolyLabeling() throws Exception{		
        ShapefileDataStore ds=(ShapefileDataStore) Rendering2DTest.getPolygons("smallMultiPoly.shp");
        FeatureSource source=ds.getFeatureSource(ds.getTypeNames()[0]);

		Style style=loadStyle("PolyStyle.sld");
		assertNotNull(style);
		MapContext map = new DefaultMapContext();
        map.addLayer(ds.getFeatureSource(), style);
        ShapeRenderer renderer = new ShapeRenderer(map);
        Envelope env = map.getLayerBounds();
        int boundary=1;
        env = new Envelope(env.getMinX() - boundary, env.getMaxX() + boundary, 
        		env.getMinY() - boundary, env.getMaxY() + boundary);
        Rendering2DTest.INTERACTIVE=INTERACTIVE;
        Rendering2DTest.showRender("testPolyLabeling", renderer, timout, env);
	}

}
