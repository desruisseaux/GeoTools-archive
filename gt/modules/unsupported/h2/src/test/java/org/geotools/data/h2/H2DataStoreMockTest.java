package org.geotools.data.h2;

import java.sql.ResultSet;

import org.geotools.feature.FeatureType;

public class H2DataStoreMockTest extends H2MockTestSupport {

	public void testGetTypeNames() throws Exception {
		String[] typeNames = dataStore.getTypeNames();
		int count = 0;
		
		ResultSet tables = createTableMetaDataResultSet();
		while( tables.next() ) {
			assertEquals( tables.getString( "TABLE_NAME"), typeNames[count++] );
		}
	
		assertEquals( count, typeNames.length );
	}
	
	public void testGetSchema() throws Exception {
		FeatureType ft1 = dataStore.getSchema( "ft1" );
		assertNotNull( ft1 );
		
		assertEquals( "ft1", ft1.getTypeName() );
		assertEquals( 4, ft1.getAttributeCount() );
		
		assertEquals( Integer.class, ft1.getAttributeType( "int" ).getType() );
		assertEquals( Boolean.class, ft1.getAttributeType( "bool" ).getType() );
		assertEquals( Double.class, ft1.getAttributeType( "double" ).getType() );
		assertEquals( String.class, ft1.getAttributeType( "string" ).getType() );
	}
	
	
}
