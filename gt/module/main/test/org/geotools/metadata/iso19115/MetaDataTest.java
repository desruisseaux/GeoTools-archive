package org.geotools.metadata.iso19115;

import java.util.List;

import org.geotools.metadata.Metadata.Entity;

import junit.framework.TestCase;

public class MetaDataTest extends TestCase {

	public void testMetadataEntity(){
		MetaData mdata = new MetaData();
		Entity entity = mdata.getEntity(); 
		assertNotNull( entity );
		List elements = entity.getElements();
		assertEquals( 21, elements.size() );
	}
	
	public void testMetadata(){
		MetaData mdata = new MetaData();
		mdata.setCharacterSet( "unicode" );
		
		assertEquals( "unicode", mdata.getElement( "characterSet" ) );
	}
}
