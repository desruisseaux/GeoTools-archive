package org.geotools.data.shapefile.indexed;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.filter.expression.BBoxExpression;
import org.geotools.filter.FidFilter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.FilterType;
import org.geotools.filter.GeometryFilter;

import com.vividsolutions.jts.geom.Geometry;

public class FidQueryTest extends FIDTestCase {
	private IndexedShapefileDataStore ds;

	Map fids = new HashMap();

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
					Feature feature = features.next();
					fids.put(feature.getID(), feature);
				}
			} finally {
				if (features != null)
					features.close();
			}
			assertEquals(numFeatures, fids.size());
			System.out.println("Number of Features=" + numFeatures);
		}

	}


	public void testGetByFID() throws Exception {

		assertFidsMatch();

	}

	public void testAddFeature() throws Exception {
		Feature feature = ((Feature) this.fids.values().iterator().next());
		FeatureType schema = ds.getSchema();
		Feature newfeature = schema.create(
				feature.getAttributes(new Object[feature.getFeatureType()
						.getAttributeCount()]));
		FeatureCollection collection = FeatureCollections.newCollection();
		collection.add(newfeature);
		Set newFids = featureStore.addFeatures(collection.reader());
		assertEquals(1, newFids.size());
		this.assertFidsMatch();
		FilterFactory fac = FilterFactoryFinder.createFilterFactory();
		DefaultQuery query = new DefaultQuery(TYPE_NAME);
		String fid = (String) newFids.iterator().next();
		query.setFilter(fac.createFidFilter(fid));
		FeatureIterator features = featureStore.getFeatures(query)
				.features();
		try {
			feature = features.next();
			for( int i=0; i<schema.getAttributeCount(); i++){
				if( newfeature.getAttribute(i) instanceof Geometry ){
					assertTrue(((Geometry)newfeature.getAttribute(i)).equals((Geometry)feature.getAttribute(i)));
				}else{
					assertEquals(newfeature.getAttribute(i), feature.getAttribute(i));
				}
			}
			assertFalse(features.hasNext());
		} finally {
			if (features != null)
				features.close();
		}
	}

	public void testModifyFeature() throws Exception {
		Feature feature = ((Feature) this.fids.values().iterator().next());
		int newId = 237594123;
		FilterFactory fac = FilterFactoryFinder.createFilterFactory();
		FidFilter createFidFilter = fac.createFidFilter(feature.getID());
		featureStore.modifyFeatures(feature.getFeatureType().getAttributeType(
				"ID"), new Integer(newId), createFidFilter);
		FeatureIterator features = featureStore.getFeatures(createFidFilter)
				.features();
		try {
			assertFalse(feature.equals(features.next()));
		} finally {
			if (features != null)
				features.close();
		}
		feature.setAttribute("ID", new Integer(newId));
		this.assertFidsMatch();
	}

	public void testDeleteFeature() throws Exception {
		FeatureIterator features = featureStore.getFeatures().features();
		Feature feature;
		try {
				feature = features.next();
		} finally {
			if (features != null)
				features.close();
		}
		FilterFactory fac = FilterFactoryFinder.createFilterFactory();
		FidFilter createFidFilter = fac.createFidFilter(feature.getID());
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
		Feature feature;
		try {
			feature = features.next();
			feature = features.next();
			feature = features.next();
		} finally {
			if (features != null)
				features.close();
		}

		FilterFactory factory = FilterFactoryFinder.createFilterFactory();
		BBoxExpression bb = factory.createBBoxExpression(feature.getBounds());

		GeometryFilter bboxFilter = factory.createGeometryFilter(FilterType.GEOMETRY_INTERSECTS);
        bboxFilter.addRightGeometry(bb);

        String geom = ds.getSchema().getDefaultGeometry().getName();

        bboxFilter.addLeftGeometry(factory.createAttributeExpression(geom));
        
        features = featureStore.getFeatures(bboxFilter).features();

		try {
			while(features.hasNext()){
				Feature newFeature = features.next();
				assertEquals(newFeature, fids.get(newFeature.getID()));
			}
		} finally {
			if (features != null)
				features.close();
		}
	}
	
	private void assertFidsMatch() throws IOException {
		long start = System.currentTimeMillis();
		FilterFactory fac = FilterFactoryFinder.createFilterFactory();
		DefaultQuery query = new DefaultQuery(TYPE_NAME);

		int i=0;
		
		for (Iterator iter = fids.entrySet().iterator(); iter.hasNext();) {
			i++;
			Map.Entry entry = (Map.Entry) iter.next();
			String fid = (String) entry.getKey();
			query.setFilter(fac.createFidFilter(fid));
			FeatureIterator features = featureStore.getFeatures(query)
					.features();
			try {
				Feature feature = features.next();
				assertFalse(features.hasNext());
				assertEquals(i+"th feature",entry.getValue(), feature);
			} finally {
				if (features != null)
					features.close();
			}

		}
		long end = System.currentTimeMillis();
		System.out.println("Time to search by fid=" + (end - start) + "ms");
	}

}
