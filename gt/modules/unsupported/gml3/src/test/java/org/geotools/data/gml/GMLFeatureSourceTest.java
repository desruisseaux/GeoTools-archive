package org.geotools.data.gml;

import java.util.HashSet;
import java.util.Iterator;

import org.geotools.data.DefaultQuery;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.identity.FeatureId;


public class GMLFeatureSourceTest extends GMLDataStoreTestSupport {

	GMLFeatureSource featureSource;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		featureSource = (GMLFeatureSource) dataStore.getFeatureSource( "TestFeature" );
	}
	
	public void testGetFeatures() throws Exception {
		FeatureCollection features = featureSource.getFeatures();
		assertTrue( features instanceof GMLFeatureCollection );
		
		assertEquals( 3, features.size() );
	}
	
	public void testGetFeaturesWithFilter() throws Exception {
		FilterFactory ff = CommonFactoryFinder.getFilterFactory( null );
		HashSet fids = new HashSet();
		FeatureId id = ff.featureId( "1" );
		fids.add( id );
		
		Filter idFilter = ff.id( fids );
		FeatureCollection features = featureSource.getFeatures( idFilter );
		assertEquals( 1, features.size() );
	}
	
	public void testGetFeaturesWithQuery() throws Exception {
		FilterFactory ff = CommonFactoryFinder.getFilterFactory( null );
		HashSet fids = new HashSet();
		FeatureId id = ff.featureId( "1" );
		fids.add( id );
		
		Filter idFilter = ff.id( fids );
		DefaultQuery query = new DefaultQuery( 
			"TestFeature", idFilter, new String[] { "count" } 
		);
		FeatureCollection features = featureSource.getFeatures( query );
		assertEquals( 1, features.size() );
		
		Iterator i = features.iterator();
		assertTrue( i.hasNext() );
		
		Feature f = (Feature) i.next();
		assertNotNull( f );
		
		assertEquals( 1, f.getFeatureType().getAttributeCount() );
		assertNull( f.getDefaultGeometry() );
		assertEquals( new Integer( 1 ), f.getAttribute( "count" ) );
		
		features.close( i );
	}
	
}
