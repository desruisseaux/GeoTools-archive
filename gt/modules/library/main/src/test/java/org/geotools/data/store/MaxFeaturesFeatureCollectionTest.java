package org.geotools.data.store;

import java.util.Iterator;

public class MaxFeaturesFeatureCollectionTest extends
		FeatureCollectionWrapperTestSupport {

	MaxFeaturesFeatureCollection max;
	
	protected void setUp() throws Exception {
		super.setUp();
		max = new MaxFeaturesFeatureCollection( delegate, 2 );
	}
	
	public void testSize() throws Exception {
		assertEquals( 2, max.size() );
	}
	
	public void testIterator() throws Exception {
	
		Iterator i = max.iterator();
		for ( int x = 0; x < 2; x++ ) {
			assertTrue( i.hasNext() );
			i.next();
		}
		
		assertFalse( i.hasNext() );
	}
}
