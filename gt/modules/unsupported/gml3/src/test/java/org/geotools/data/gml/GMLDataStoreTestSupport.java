package org.geotools.data.gml;

import junit.framework.TestCase;

public class GMLDataStoreTestSupport extends TestCase {

	GMLDataStore dataStore;
	
	protected void setUp() throws Exception {
		String location = getClass().getResource( "test.xml" ).toString();
		String schemaLocation = getClass().getResource( "test.xsd" ).toString();
		
		dataStore = new GMLDataStore( "http://www.geotools.org/test", location, schemaLocation );
	}
	
}
