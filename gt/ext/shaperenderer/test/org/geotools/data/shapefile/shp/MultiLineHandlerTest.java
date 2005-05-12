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
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.shapefile.ShapefileRendererUtil;
import org.geotools.referencing.CRS;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.crs.GeographicCRS;
import org.geotools.referencing.operation.GeneralMatrix;
import org.geotools.renderer.shape.Geometry;
import org.geotools.renderer.shape.LabelingTest;
import org.geotools.renderer.shape.MultiLineHandler;
import org.geotools.renderer.shape.ShapeRenderer;
import org.geotools.resources.TestData;
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

	public void testRead() throws Exception{
		URL url=TestData.getResource(LabelingTest.class, "streams.shp");
		ShapefileDataStore ds=(ShapefileDataStore) new ShapefileDataStoreFactory().createDataStore(url);
		
		Envelope env=ds.getFeatureSource().getBounds();
//		Envelope env=new Envelope(-180,180,-90,90);
		CoordinateReferenceSystem crs=ds.getSchema().getDefaultGeometry().getCoordinateSystem();
//		CoordinateReferenceSystem crs=GeographicCRS.WGS84;
		MathTransform2D mt=(MathTransform2D) CRS.transform(crs, GeographicCRS.WGS84);
		
		ShapefileReader reader=new ShapefileReader(ShapefileRendererUtil.getShpReadChannel(ds));
		reader.setHandler(new MultiLineHandler(reader.getHeader().getShapeType(), env, mt));
		Object shape=reader.nextRecord().shape();
		assertNotNull( shape );
		assertTrue( shape instanceof Geometry);
		int i=0;
		while( reader.hasNext() ){
			i++;
			shape=reader.nextRecord().shape();
			assertNotNull( shape );
			assertTrue( shape instanceof Geometry);
			if( i==0 ){
				Geometry geom=(Geometry) shape;
				assertEquals(13, geom.coords[0].length);
			}
		}
		assertEquals(ds.getFeatureSource().getCount(Query.ALL)-1, i);
	}
	public void testDecimation() throws Exception{
		URL url=TestData.getResource(LabelingTest.class, "theme1.shp");
		ShapefileDataStore ds=(ShapefileDataStore) new ShapefileDataStoreFactory().createDataStore(url);
		
//		Envelope env=ds.getFeatureSource().getBounds();
		Envelope env=new Envelope(-7.105552354197932,8.20555235419793,-3.239388966356115,4.191388966388683);
//		CoordinateReferenceSystem crs=ds.getSchema().getDefaultGeometry().getCoordinateSystem();
		CoordinateReferenceSystem crs=GeographicCRS.WGS84;
		MathTransform mt=CRS.transform(crs, GeographicCRS.WGS84);
		ShapeRenderer renderer=new ShapeRenderer(null);
		AffineTransform at=renderer.worldToScreenTransform(env,new Rectangle(300,300));
		MathTransform worldToScreen=FactoryFinder.getMathTransformFactory(null)
		.createAffineTransform(new GeneralMatrix(at));
		mt = FactoryFinder.getMathTransformFactory(null)
		.createConcatenatedTransform(mt,worldToScreen);
		ShapefileReader reader=new ShapefileReader(ShapefileRendererUtil.getShpReadChannel(ds));
		reader.setHandler(new MultiLineHandler(reader.getHeader().getShapeType(), env, mt));
		Geometry shape=(Geometry) reader.nextRecord().shape();
		assertEquals( 6, shape.coords[0].length );
//		assertEquals( shape.coords[0][0], -5.828066634497234, 0.00001 );
//		assertEquals( shape.coords[0][1], -1.480529972741367, 0.00001 );
//		assertEquals( shape.coords[0][4], 6.729097484720893, 0.00001 );
//		assertEquals( shape.coords[0][5], -1.6573914392057159, 0.00001 );
		
		shape=(Geometry) reader.nextRecord().shape();
		assertEquals( 4, shape.coords[0].length );

		shape=(Geometry) reader.nextRecord().shape();
		assertEquals( 4, shape.coords[0].length);
//	
//		assertEquals( shape.coords[0][0], 0, 0.00001 );
//		assertEquals( shape.coords[0][1], 0, 0.00001 );
	}

}
