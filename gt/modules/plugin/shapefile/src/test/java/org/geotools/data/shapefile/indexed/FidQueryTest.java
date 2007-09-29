/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
 */
package org.geotools.data.shapefile.indexed;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.filter.BBoxExpression;

import org.geotools.filter.FidFilter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.FilterType;
import org.geotools.filter.GeometryFilter;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.Id;
import org.opengis.filter.spatial.BBOX;

import com.vividsolutions.jts.geom.Geometry;

public class FidQueryTest extends FIDTestCase {
	private IndexedShapefileDataStore ds;

	
	Map<String,SimpleFeature> fids = new HashMap<String,SimpleFeature>();

	FeatureStore featureStore;


	private int numFeatures;


	protected void setUp() throws Exception {

		super.setUp();
		
		URL url = backshp.toURL();
		ds = new IndexedShapefileDataStore(url, null, false, true,
				IndexedShapefileDataStore.TREE_QIX);
		numFeatures = 0;
		featureStore = (FeatureStore) ds.getFeatureSource();
		{
			FeatureIterator features = featureStore.getFeatures().features();
			try {
				while (features.hasNext()) {
					numFeatures++;
					SimpleFeature feature = features.next();
					fids.put(feature.getID(), feature);
				}
			} finally {
				if (features != null)
					features.close();
			}
			assertEquals(numFeatures, fids.size());
		}

	}


	public void testGetByFID() throws Exception {

		assertFidsMatch();

	}

	public void testAddFeature() throws Exception {
	    
		SimpleFeature feature = fids.values().iterator().next();
		SimpleFeatureType schema = ds.getSchema();
		
		SimpleFeatureBuilder build = new SimpleFeatureBuilder(schema);
		
		SimpleFeature newFeature = build.buildFeature(null);
		
		FeatureCollection collection = FeatureCollections.newCollection();
		collection.add(newFeature);
		
		Set newFids = featureStore.addFeatures(collection);
		assertEquals(1, newFids.size());
		this.assertFidsMatch();
		FilterFactory fac = FilterFactoryFinder.createFilterFactory();
                
		DefaultQuery query = new DefaultQuery( schema.getTypeName() );
		String fid = (String) newFids.iterator().next();
		query.setFilter(fac.createFidFilter(fid));
		FeatureIterator features = featureStore.getFeatures(query)
				.features();
		try {
			feature = features.next();
			for( int i=0; i<schema.getAttributeCount(); i++){
			    Object value = feature.getAttribute(i);
			    Object newValue = newFeature.getAttribute(i);
			    
				if( value instanceof Geometry ){
					assertTrue(((Geometry)newValue).equals((Geometry)value));
				} else {
					assertEquals( newValue, value );
				}
			}
			assertFalse(features.hasNext());
		} finally {
			if (features != null)
				features.close();
		}
	}

	public void testModifyFeature() throws Exception {
		SimpleFeature feature = this.fids.values().iterator().next();
		int newId = 237594123;
		
		FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
		
		Id createFidFilter = ff.id( Collections.singleton( ff.featureId( feature.getID() )));
		
		SimpleFeatureType schema = feature.getFeatureType();
        featureStore.modifyFeatures(schema.getAttribute("ID"), new Integer(newId), createFidFilter);
        
		FeatureIterator features = featureStore.getFeatures(createFidFilter).features();
		try {
			assertFalse(feature.equals(features.next()));
		} finally {
			if (features != null){
				features.close();
			}
		}
		feature.setAttribute("ID", new Integer(newId));
		this.assertFidsMatch();
	}

	public void testDeleteFeature() throws Exception {
		FeatureIterator features = featureStore.getFeatures().features();
		SimpleFeature feature;
		try {
		    feature = features.next();
		} finally {
			if (features != null)
				features.close();
		}
		FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
		Id createFidFilter = ff.id( Collections.singleton( ff.featureId( feature.getID() )));
		
		featureStore.removeFeatures(createFidFilter);
		fids.remove(feature.getID());

		assertEquals(fids.size(), featureStore.getCount(Query.ALL));
		
		features = featureStore.getFeatures(createFidFilter)
				.features();
		try {
			assertFalse(features.hasNext());
		} finally {
			if (features != null)
				features.close();
		}

		this.assertFidsMatch();

	}

	public void testFIDBBoxQuery() throws Exception {
		FeatureIterator features = featureStore.getFeatures().features();
		SimpleFeature feature;
		try {
			feature = features.next();
			feature = features.next();
			feature = features.next();
		} finally {
			if (features != null)
				features.close();
		}
//		FilterFactory factory = FilterFactoryFinder.createFilterFactory();
//		BBoxExpression bb = factory.createBBoxExpression(feature.getBounds());
//
//		GeometryFilter bboxFilter = factory.createGeometryFilter(FilterType.GEOMETRY_INTERSECTS);
//        bboxFilter.addRightGeometry(bb);
//
//        String geom = ds.getSchema().getDefaultGeometry().getLocalName();
//
//        bboxFilter.addLeftGeometry(factory.createAttributeExpression(geom));
        
        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);        
        BBOX bbox = ff.bbox(ff.property(""),feature.getBounds() );
        
        features = featureStore.getFeatures(bbox).features();

		try {
			while(features.hasNext()){
				SimpleFeature newFeature = features.next();
				assertEquals(newFeature, fids.get(newFeature.getID()));
			}
		} finally {
			if (features != null)
				features.close();
		}
	}
	
	private void assertFidsMatch() throws IOException {
		//long start = System.currentTimeMillis();
		FilterFactory fac = FilterFactoryFinder.createFilterFactory();
        
		DefaultQuery query = new DefaultQuery( featureStore.getSchema().getTypeName());

		int i=0;
		
		for (Iterator iter = fids.entrySet().iterator(); iter.hasNext();) {
			i++;
			Map.Entry entry = (Map.Entry) iter.next();
			String fid = (String) entry.getKey();
			query.setFilter(fac.createFidFilter(fid));
			FeatureIterator features = featureStore.getFeatures(query)
					.features();
			try {
				SimpleFeature feature = features.next();
				assertFalse(features.hasNext());
				assertEquals(i+"th feature",entry.getValue(), feature);
			} finally {
				if (features != null)
					features.close();
			}

		}
		long end = System.currentTimeMillis();
		//System.out.println("Time to search by fid=" + (end - start) + "ms");
	}

}
