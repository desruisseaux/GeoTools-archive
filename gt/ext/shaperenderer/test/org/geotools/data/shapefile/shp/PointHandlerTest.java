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
import org.geotools.referencing.CRS;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.operation.GeneralMatrix;
import org.geotools.renderer.shape.LabelingTest;
import org.geotools.renderer.shape.PointHandler;
import org.geotools.renderer.shape.ShapefileRenderer;
import org.geotools.renderer.shape.SimpleGeometry;
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
public class PointHandlerTest extends TestCase {

	public void testRead() throws Exception{
		URL url=TestData.getResource(LabelingTest.class, "pointtest.shp");
		ShapefileDataStore ds=(ShapefileDataStore) new ShapefileDataStoreFactory().createDataStore(url);
		
		Envelope env=ds.getFeatureSource().getBounds();
//		Envelope env=new Envelope(-180,180,-90,90);
//		CoordinateReferenceSystem crs=ds.getSchema().getDefaultGeometry().getCoordinateSystem();
		CoordinateReferenceSystem crs=DefaultGeographicCRS.WGS84;
		MathTransform mt=CRS.transform(crs, DefaultGeographicCRS.WGS84);
		
		AffineTransform transform=ShapefileRenderer.worldToScreenTransform(env, new Rectangle(300,300));
		GeneralMatrix matrix=new GeneralMatrix(transform);
		MathTransform at=FactoryFinder.getMathTransformFactory(null).createAffineTransform(matrix);
		mt=FactoryFinder.getMathTransformFactory(null).createConcatenatedTransform(mt,at);
		
		ShapefileReader reader=new ShapefileReader(ShapefileRendererUtil.getShpReadChannel(ds), new Lock());
		reader.setHandler(new PointHandler(reader.getHeader().getShapeType(), env, mt));

		Object shape=reader.nextRecord().shape();
		assertNotNull( shape );
		assertTrue( shape instanceof SimpleGeometry);
		int i=0;
		while( reader.hasNext() ){
			i++;
			shape=reader.nextRecord().shape();
			assertNotNull( shape );
			assertTrue( shape instanceof SimpleGeometry);
		}
		assertEquals(ds.getFeatureSource().getCount(Query.ALL)-1, i);
	}

}
