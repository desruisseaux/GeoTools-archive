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
package org.geotools.data.shapefile.shp;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.net.URL;

import junit.framework.TestCase;

import org.geotools.data.Query;
import org.geotools.data.shapefile.Lock;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.shapefile.ShapefileRendererUtil;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.referencing.CRS;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.operation.GeneralMatrix;
import org.geotools.renderer.shape.LabelingTest;
import org.geotools.renderer.shape.MultiLineHandler;
import org.geotools.renderer.shape.ShapefileRenderer;
import org.geotools.renderer.shape.SimpleGeometry;
import org.geotools.renderer.shape.TestUtilites;
import org.geotools.resources.TestData;
import org.geotools.styling.Style;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform2D;

import com.vividsolutions.jts.geom.Envelope;

/**
 * @TODO class description
 * 
 * @author jeichar
 * @since 2.1.x
 */
public class MultiLineHandlerTest extends TestCase {

	private static final boolean INTERACTIVE = false;

	public void testRead() throws Exception{
		URL url=TestData.getResource(LabelingTest.class, "streams.shp");
		ShapefileDataStore ds=(ShapefileDataStore) new ShapefileDataStoreFactory().createDataStore(url);
		
		Envelope env=ds.getFeatureSource().getBounds();
//		Envelope env=new Envelope(-180,180,-90,90);
		CoordinateReferenceSystem crs=ds.getSchema().getDefaultGeometry().getCoordinateSystem();
//		CoordinateReferenceSystem crs=DefaultGeographicCRS.WGS84;
		MathTransform2D mt=(MathTransform2D) CRS.transform(crs, DefaultGeographicCRS.WGS84);
		
		ShapefileReader reader=new ShapefileReader(ShapefileRendererUtil.getShpReadChannel(ds), new Lock());
		reader.setHandler(new MultiLineHandler(reader.getHeader().getShapeType(), env, mt));
		Object shape=reader.nextRecord().shape();
		assertNotNull( shape );
		assertTrue( shape instanceof SimpleGeometry);
		int i=0;
		while( reader.hasNext() ){
			i++;
			shape=reader.nextRecord().shape();
			assertNotNull( shape );
			assertTrue( shape instanceof SimpleGeometry);
			if( i==0 ){
				SimpleGeometry geom=(SimpleGeometry) shape;
				assertEquals(13, geom.coords[0].length);
			}
		}
		assertEquals(ds.getFeatureSource().getCount(Query.ALL)-1, i);
	}
	public void testDecimation() throws Exception{
		URL url=TestData.getResource(LabelingTest.class, "theme1.shp");
		ShapefileDataStore ds=(ShapefileDataStore) new ShapefileDataStoreFactory().createDataStore(url);
		
		Envelope env=new Envelope(-7.105552354197932,8.20555235419793,-3.239388966356115,4.191388966388683);
		CoordinateReferenceSystem crs=DefaultGeographicCRS.WGS84;
		MathTransform mt=CRS.transform(crs, DefaultGeographicCRS.WGS84);
		ShapefileRenderer renderer=new ShapefileRenderer(null);
		AffineTransform at=renderer.worldToScreenTransform(env,new Rectangle(300,300));
		MathTransform worldToScreen=FactoryFinder.getMathTransformFactory(null)
		.createAffineTransform(new GeneralMatrix(at));
		mt = FactoryFinder.getMathTransformFactory(null)
		.createConcatenatedTransform(mt,worldToScreen);
		ShapefileReader reader=new ShapefileReader(ShapefileRendererUtil.getShpReadChannel(ds), new Lock());
		reader.setHandler(new MultiLineHandler(reader.getHeader().getShapeType(), env, mt));
		SimpleGeometry shape=(SimpleGeometry) reader.nextRecord().shape();
		assertEquals( 6, shape.coords[0].length );
		
		shape=(SimpleGeometry) reader.nextRecord().shape();
		assertEquals( 4, shape.coords[0].length );

		shape=(SimpleGeometry) reader.nextRecord().shape();
		assertEquals( 4, shape.coords[0].length);
//	
//		assertEquals( shape.coords[0][0], 0, 0.00001 );
//		assertEquals( shape.coords[0][1], 0, 0.00001 );
	}

	public void testFeatureNearBoundry() throws Exception{		
        ShapefileDataStore ds=(ShapefileDataStore) TestUtilites.getDataStore("theme1.shp");
		Style style=TestUtilites.createTestStyle(null,"theme1");
		assertNotNull(style);
		MapContext map = new DefaultMapContext();
        map.addLayer(ds.getFeatureSource(), style);
        ShapefileRenderer renderer = new ShapefileRenderer(map);
        Envelope env = new Envelope(-5,6,-1.4,0);
        TestUtilites.INTERACTIVE=INTERACTIVE;
        TestUtilites.showRender("testLineLabeling", renderer, 2000, env);
	}

	public void testBBoxIntersectSegment() throws Exception{
		MultiLineHandler handler=new MultiLineHandler(ShapeType.ARC, new Envelope(0,10,0,10), null);
		assertTrue("point contained in bbox", handler.bboxIntersectSegment(false, new double[]{1,1}, 2));
		assertFalse("point outside of bbox",handler.bboxIntersectSegment(false, new double[]{-1,1}, 2));
		assertTrue("Line enters bbox", handler.bboxIntersectSegment(false, new double[]{-1,1, 1,1}, 4));
		assertTrue("line crosses bbox, no vertices contained",
				handler.bboxIntersectSegment(false, new double[]{-1,1, 11,1}, 4));
		assertFalse("line misses bbox", handler.bboxIntersectSegment(false, new double[]{-1,-1, 11,-1}, 4));
		assertTrue("line diagonally crosses bbox, no vertices contained", 
				handler.bboxIntersectSegment(false, new double[]{2,-2, 12,6}, 4));
		assertFalse("diagonal line misses bbox, no vertices contained", 
				handler.bboxIntersectSegment(false, new double[]{8,-4, 14,2}, 4));
	}
}
