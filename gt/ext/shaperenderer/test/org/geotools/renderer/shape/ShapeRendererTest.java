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

import junit.framework.TestCase;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileRendererUtil;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.styling.Style;

/**
 * @TODO class description
 * 
 * @author jeichar
 * @since 2.1.x
 */
public class ShapeRendererTest extends TestCase {

	public void testPaint() {
	}

	public void testCreateFeature() throws Exception{
		ShapeRenderer renderer=new ShapeRenderer(null);
		Style style=LabelingTest.loadStyle("LineStyle.sld");
		ShapefileDataStore ds=Rendering2DTest.getLines();
		renderer.dbfheader=ShapefileRendererUtil.getDBFReader(ds).getHeader();
		FeatureType type=renderer.createFeatureType(style,ds.getSchema());
		assertEquals( "LINE", type.getAttributeType(0).getName() );
		assertEquals( 2, type.getAttributeCount() );
		Feature feature=renderer.createFeature(type, ShapefileRendererUtil.getShpReader(ds, ds.getFeatureSource().getBounds(), null).nextRecord(),ShapefileRendererUtil.getDBFReader(ds), "id");
		assertEquals( "id", feature.getID());
		assertEquals("LE01_8",feature.getAttribute(0));
	}


	public void testWorldToScreenTransform() {
	}

}
