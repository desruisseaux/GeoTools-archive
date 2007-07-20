package org.geotools.data.jdbc;

import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;

import org.opengis.filter.FilterFactory;
import org.opengis.filter.PropertyIsEqualTo;

import com.vividsolutions.jts.geom.Geometry;

public class JDBCFeatureSourceTest extends JDBCTestSupport {

	FeatureSource fs;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		fs = dataStore.getFeatureSource("ft1");
	}
	
	public void testGetDataStore() {
		assertEquals( dataStore, fs.getDataStore() );
	}
	
	public void testGetSchema() {
		FeatureType ft1 = fs.getSchema();
		assertEquals( "ft1", ft1.getTypeName() );
		
		assertNotNull( ft1.getAttributeType("geometry") );
		assertNotNull( ft1.getAttributeType("intProperty") );
		assertNotNull( ft1.getAttributeType("doubleProperty") );
		assertNotNull( ft1.getAttributeType("stringProperty") );
		
		assertEquals( Geometry.class, ft1.getAttributeType("geometry").getBinding() );
		assertEquals( Integer.class, ft1.getAttributeType("intProperty").getBinding()  );
		assertEquals( Double.class, ft1.getAttributeType("doubleProperty").getBinding()  );
		assertEquals( String.class, ft1.getAttributeType("stringProperty").getBinding()  );
	}
	
	public void testGetFeatures() throws Exception {
		FeatureCollection features = fs.getFeatures();
		assertNotNull( features );
		
		assertEquals( 3, features.size() );
	}
	
	public void testGetFeaturesWithFilter() throws Exception {
		FilterFactory ff = dataStore.getFilterFactory();
		PropertyIsEqualTo filter = ff.equal(
			ff.property( "intProperty" ), ff.literal( 1 ), false
		);
		
		FeatureCollection features = fs.getFeatures( filter );
		assertNotNull( features );
		
		assertEquals( 1, features.size() );
	}
}
