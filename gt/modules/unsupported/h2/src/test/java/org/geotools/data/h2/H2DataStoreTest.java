package org.geotools.data.h2;

import org.geotools.feature.FeatureType;

public class H2DataStoreTest extends H2TestSupport {

	public void testGetTypeNames() throws Exception {
		String[] typeNames = dataStore.getTypeNames();
		
		assertEquals( 1, typeNames.length );
	}
	
	public void testGetSchema() throws Exception {
		FeatureType ft1 = dataStore.getSchema( "featureType1" );
		assertNotNull( ft1 );
		
		assertEquals( "featureType1", ft1.getTypeName() );
		assertEquals( 3, ft1.getAttributeCount() );
		
		assertEquals( Integer.class, ft1.getAttributeType( "intProperty" ).getType() );
		assertEquals( Double.class, ft1.getAttributeType( "doubleProperty" ).getType() );
		assertEquals( String.class, ft1.getAttributeType( "stringProperty" ).getType() );
	}
	
}
