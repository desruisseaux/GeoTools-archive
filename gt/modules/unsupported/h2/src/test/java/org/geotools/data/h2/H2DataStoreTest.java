package org.geotools.data.h2;

import java.sql.Types;

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

	public void testCreateSchema() throws Exception {
		
		assertEquals( 1, dataStore.getTypeNames().length );
		
		//create a new feature type
		H2TypeBuilder builder = 
			new H2TypeBuilder( dataStore.getTypeFactory() );
		builder.setName( "featureType2" );
		builder.setNamespaceURI( dataStore.getNamespaceURI() );
		
		builder.attribute( "dateProperty", Types.DATE );
		builder.attribute( "boolProperty", Types.BOOLEAN );
		
		FeatureType ft2 = builder.feature();
		dataStore.createSchema( ft2 );
		
		assertEquals( 2,  dataStore.getTypeNames().length );
		
	}
}
