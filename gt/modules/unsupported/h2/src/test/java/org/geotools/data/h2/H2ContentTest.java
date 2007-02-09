package org.geotools.data.h2;

import java.sql.ResultSet;
import java.util.Iterator;

import org.geotools.feature.type.TypeName;

public class H2ContentTest extends H2MockTestSupport {

	H2Content content;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		content = dataStore.getContent();
	}
	
	public void testGetTypeNames() throws Exception {
		Iterator typeNames = content.getTypeNames().iterator();
		assertTrue( typeNames.hasNext() );
		
		ResultSet tables = createTableMetaDataResultSet();
		
		while( tables.next() ) {
			assertTrue( typeNames.hasNext() );
			TypeName typeName = (TypeName) typeNames.next();
			
			String tableName =  tables.getString( "TABLE_NAME" );
			assertEquals( tableName, typeName.getLocalPart() );
		}
		
		assertFalse( typeNames.hasNext() );
	}
	
	
}
