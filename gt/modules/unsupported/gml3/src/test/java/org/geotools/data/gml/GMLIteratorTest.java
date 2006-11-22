package org.geotools.data.gml;

import java.util.ArrayList;
import java.util.List;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;

public class GMLIteratorTest extends GMLDataStoreTestSupport {

	public void test() throws Exception {
		GMLIterator iterator = new GMLIterator( (GMLTypeEntry) dataStore.entry( "TestFeature" ) );
		
		List features = new ArrayList();
		while( iterator.hasNext() ) {
			features.add( iterator.next() );
		}
		
		assertEquals( 3, features.size() );
		for ( int i = 0; i < features.size(); i++ ) {
			Feature feature = (Feature) features.get( i );
			assertNotNull( feature );
			assertEquals( "" + i, feature.getID() );
		}
	}
}
