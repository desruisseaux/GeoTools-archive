package org.geotools.data.gml;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;

import junit.framework.TestCase;

public class GMLDataStoreSimpleFeatureProfileTest extends TestCase {

	GMLDataStore dataStore;
	
	protected void setUp() throws Exception {
		String namespace = "http://cite.opengeospatial.org/gmlsf";
		String location = getClass().getResource( "dataset-sf0.xml" ).toString();
		String schemaLocation = getClass().getResource( "cite-gmlsf0.xsd" ).toString();
		
		dataStore = new GMLDataStore( namespace, location, schemaLocation );
	}
	
	public void testGetTypeNames() {
		List names = Arrays.asList( dataStore.getTypeNames() ); 
		assertTrue( names.contains( "PrimitiveGeoFeature" ) );
		assertTrue( names.contains( "AggregateGeoFeature" ) );
	}
	
	public void testGetSchema() throws IOException {
		FeatureType featureType = 
			dataStore.getSchema( "PrimitiveGeoFeature" );
		assertNotNull( featureType );
		
		assertNotNull( featureType.getAttributeType( "pointProperty" ) );
		assertNotNull( featureType.getAttributeType( "curveProperty" ) );
		assertNotNull( featureType.getAttributeType( "surfaceProperty" ) );
	}
	
	public void testGetFeatures() throws Exception {
		FeatureCollection features = dataStore.getFeatureSource( "PrimitiveGeoFeature" ).getFeatures();
		assertEquals( 4, features.size() );
		
		Iterator iterator = features.iterator();
		assertTrue( iterator.hasNext() );
		try {
			Feature f = (Feature) iterator.next();
			assertEquals( "f001", f.getID() );
		}
		finally {
			features.close( iterator );
		}
	}
}
