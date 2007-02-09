package org.geotools.data.h2;

import java.util.List;

import org.geotools.feature.type.TypeName;

public class H2ContentTest extends H2TestSupport {

	H2Content content;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		content = dataStore.getContent();
	}
	
	public void testGetTypeNames() throws Exception {
		List typeNames = content.getTypeNames();
		assertEquals( 1, typeNames.size() );
		
		TypeName typeName = (TypeName) typeNames.get( 0 );
		assertEquals( "featureType1", typeName.getLocalPart() );
	}
	
	
}
