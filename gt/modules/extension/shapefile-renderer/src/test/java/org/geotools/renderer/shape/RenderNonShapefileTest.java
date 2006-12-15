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

import junit.framework.TestCase;

import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.data.shapefile.indexed.IndexedShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;

import com.vividsolutions.jts.geom.Envelope;


/**
 * Shapefile renderer delegates to Streaming Renderer if a layer is not a Shapefile layer.  This tests that. 
 * @author Jesse
 */
public class RenderNonShapefileTest extends TestCase {
	
	public void testRender() throws Exception {
		MemoryDataStore store=new MemoryDataStore();
		
		IndexedShapefileDataStore polys = TestUtilites.getPolygons();
		
		FeatureCollection featureCollection = polys.getFeatureSource().getFeatures();
		store.createSchema(polys.getSchema());
		
		FeatureSource target = store.getFeatureSource(store.getTypeNames()[0]);
		((FeatureStore)target).addFeatures(featureCollection);
		
		MapLayer layer=new DefaultMapLayer(target,TestUtilites.createTestStyle(target.getSchema().getTypeName(), null));
		MapContext context=new DefaultMapContext(new MapLayer[]{layer});
		
		ShapefileRenderer renderer=new ShapefileRenderer(context);
		
        Envelope env = context.getLayerBounds();
        env = new Envelope(env.getMinX(), env.getMaxX(), env.getMinY(),
                env.getMaxY());
        TestUtilites.showRender("testSimpleRender", renderer, 1000, env);

	}
}
