package org.geotools.data.jdbc;

import org.geotools.data.Transaction;
import org.geotools.data.jdbc.collection.JDBCFeatureCollection;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureIterator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class JDBCFeatureCollectionTest extends JDBCTestSupport {

	JDBCFeatureCollection collection;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		JDBCFeatureSource source = (JDBCFeatureSource) dataStore.getFeatureSource("ft1");
		JDBCState state = (JDBCState) source.getEntry().getState( Transaction.AUTO_COMMIT );
		
		collection = new JDBCFeatureCollection( source, state );
	}
	
	public void testBounds() throws Exception {
		Envelope e = collection.getBounds();
		assertNotNull( e );
		
		assertEquals( 0d, e.getMinX(), 0.1 );
		assertEquals( 0d, e.getMinY(), 0.1 );
		assertEquals( 2d, e.getMaxX(), 0.1 );
		assertEquals( 2d, e.getMaxY(), 0.1 );
	}
	
	public void testSize() throws Exception {
		
		int size = collection.size();
		assertEquals( 3, size );
	}
	
	public void testFeatures() throws Exception {
		FeatureIterator features = collection.features();
		
		for ( int i = 0; i < 3; i++ ) {
			assertTrue( features.hasNext() );
			
			Feature feature = (Feature) features.next();
			assertNotNull( feature );
			
			assertEquals( i+ "", feature.getID() );
			assertTrue( new GeometryFactory().createPoint( new Coordinate( i, i ) ).equals( (Geometry) feature.getAttribute( "geometry") ) );
			assertEquals( new Integer( i ), feature.getAttribute("intProperty") );
			
			assertNotNull( feature.getDefaultGeometry() );
			assertEquals( feature.getAttribute("geometry"),feature.getDefaultGeometry() );
		}
		
		assertFalse( features.hasNext() );
	}
}
