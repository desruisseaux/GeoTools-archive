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

	private long timout=3000;
	private static final int CENTERX = 160;
	private static final int CENTERY = 40;
    private static final boolean INTERACTIVE=false;


	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void disabletestPointLabeling() throws Exception{
		FeatureCollection collection=createPointFeatureCollection();
		Style style=loadStyle("PointStyle.sld");
		assertNotNull(style);
		MapContext map = new DefaultMapContext();
        map.addLayer(collection, style);
        ShapeRenderer renderer = new ShapeRenderer(map);
        Envelope env = map.getLayerBounds();
        int boundary=10;
        env = new Envelope(env.getMinX() - boundary, env.getMaxX() + boundary, 
        		env.getMinY() - boundary, env.getMaxY() + boundary);
//        Rendering2DTest.INTERACTIVE=INTERACTIVE;
        Rendering2DTest.showRender("testPointLabeling", renderer, timout, env);
	}

	static Style loadStyle(String sldFilename) throws IOException {
        StyleFactory factory = StyleFactory.createStyleFactory();

        java.net.URL surl = TestData.getResource(LabelingTest.class, sldFilename);
        SLDParser stylereader = new SLDParser(factory, surl);

        Style style = stylereader.readXML()[0];
        return style;
	}

	private FeatureCollection createPointFeatureCollection() throws Exception {
        AttributeType[] types = new AttributeType[2];

        
        GeometryFactory geomFac=new GeometryFactory();
		CoordinateReferenceSystem crs=GeographicCRS.WGS84;

        MemoryDataStore data = new MemoryDataStore();
        data.addFeature(createPointFeature(0,0,"LongLabel1",crs, geomFac, types));
        data.addFeature(createPointFeature(2,2,"LongLabel2",crs, geomFac, types));
        data.addFeature(createPointFeature(0,2,"LongLabel3",crs, geomFac, types));
//        data.addFeature(createPointFeature(2,0,"Label4",crs, geomFac, types));
        data.addFeature(createPointFeature(0,4,"LongLabel6",crs, geomFac, types));

        return data.getFeatureSource(Rendering2DTest.POINT).getFeatures().collection();
	}


	private Feature createPointFeature(int x, int y, String name, CoordinateReferenceSystem crs, GeometryFactory geomFac, AttributeType[] types) throws Exception{
        Coordinate c = new Coordinate(x, y);
        Point point = geomFac.createPoint(c);
		if (crs != null)
            types[0] = AttributeTypeFactory.newAttributeType("point", point.getClass(), false, 0,
                    null, crs);
        else
            types[0] = AttributeTypeFactory.newAttributeType("centre", point.getClass());
		types[1] = AttributeTypeFactory.newAttributeType("name", String.class);
		FeatureType pointType = FeatureTypeFactory.newFeatureType(types, Rendering2DTest.POINT);
		Feature pointFeature = pointType.create(new Object[]{point, name});
		return pointFeature;
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

	private FeatureCollection createLineFeatureCollection() throws Exception {
        AttributeType[] types = new AttributeType[2];

        
        GeometryFactory geomFac=new GeometryFactory();
		CoordinateReferenceSystem crs=GeographicCRS.WGS84;

        MemoryDataStore data = new MemoryDataStore();
        data.addFeature(createLineFeature(10,0,0,10,"LongLabel1",crs, geomFac, types));
        data.addFeature(createLineFeature(10,10,0,0,"LongLabel2",crs, geomFac, types));
//        data.addFeature(createPointFeature(0,2,"LongLabel3",crs, geomFac, types));
//        data.addFeature(createPointFeature(2,0,"Label4",crs, geomFac, types));
//        data.addFeature(createPointFeature(0,4,"LongLabel6",crs, geomFac, types));

        return data.getFeatureSource(Rendering2DTest.LINE).getFeatures().collection();
	}


	private Feature createLineFeature(int startx, int starty,int endx, int endy, String name, CoordinateReferenceSystem crs, GeometryFactory geomFac, AttributeType[] types) throws Exception{
        Coordinate[] c = new Coordinate[]{new Coordinate(startx, starty),
        		new Coordinate(endx, endy)
        };
        LineString line= geomFac.createLineString(c);
		if (crs != null)
            types[0] = AttributeTypeFactory.newAttributeType("line", line.getClass(), false, 0,
                    null, crs);
        else
            types[0] = AttributeTypeFactory.newAttributeType("centre", line.getClass());
		types[1] = AttributeTypeFactory.newAttributeType("name", String.class);
		FeatureType pointType = FeatureTypeFactory.newFeatureType(types, Rendering2DTest.LINE);
		Feature pointFeature = pointType.create(new Object[]{line, name});
		
		return pointFeature;
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
        int boundary=10;
        env = new Envelope(env.getMinX() - boundary, env.getMaxX() + boundary, 
        		env.getMinY() - boundary, env.getMaxY() + boundary);
//        Rendering2DTest.INTERACTIVE=INTERACTIVE;
        Rendering2DTest.showRender("testPolyLabeling", renderer, timout, env);
	}

	private FeatureCollection createPolyFeatureCollection() throws Exception {
        AttributeType[] types = new AttributeType[2];

        
        GeometryFactory geomFac=new GeometryFactory();
		CoordinateReferenceSystem crs=GeographicCRS.WGS84;

        MemoryDataStore data = new MemoryDataStore();
        data.addFeature(createPolyFeature(CENTERX+5,CENTERY+0,CENTERX+10,CENTERY+10,"LongLabel1",crs, geomFac, types));
        data.addFeature(createPolyFeature(CENTERX+0,CENTERY+0,CENTERX+10,CENTERY+10,"LongLabel2",crs, geomFac, types));

        return data.getFeatureSource(Rendering2DTest.POLYGON).getFeatures().collection();
	}


	private Feature createPolyFeature(int startx, int starty,int width, int height, String name, CoordinateReferenceSystem crs, GeometryFactory geomFac, AttributeType[] types) throws Exception{
        Coordinate[] c = new Coordinate[]{new Coordinate(startx, starty),
        		new Coordinate(startx+width, starty),
        		new Coordinate(startx+width, starty+height),
        		new Coordinate(startx, starty),
        };
        LinearRing line= geomFac.createLinearRing(c);
		if (crs != null)
            types[0] = AttributeTypeFactory.newAttributeType("polygon", line.getClass(), false, 0,
                    null, crs);
        else
            types[0] = AttributeTypeFactory.newAttributeType("centre", line.getClass());
		types[1] = AttributeTypeFactory.newAttributeType("name", String.class);
		FeatureType pointType = FeatureTypeFactory.newFeatureType(types, Rendering2DTest.POLYGON);
		Feature pointFeature = pointType.create(new Object[]{line, name});
		
		return pointFeature;
	}
}
