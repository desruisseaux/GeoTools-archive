package org.geotools.gml3.bindings;

import org.geotools.geometry.DirectPosition1D;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.gml3.GML3TestSupport;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DirectPositionTypeBindingTest extends GML3TestSupport {

	protected Element createRootElement(Document doc) {
		return doc.createElementNS( GML.NAMESPACE, GML.POS.getLocalPart() );
	}
	
	public void test1D() throws Exception {
		document.getDocumentElement().appendChild( document.createTextNode( "1.0" ) );
		DirectPosition pos = (DirectPosition) parse();
		
		assertNotNull( pos );
		assertTrue( pos instanceof DirectPosition1D );
		
		assertEquals( pos.getOrdinate( 0 ), 1.0, 0 );
	}
	
	public void test2D() throws Exception {
		document.getDocumentElement().appendChild( document.createTextNode( "1.0 2.0" ) );
		DirectPosition pos = (DirectPosition) parse();
		
		assertNotNull( pos );
		assertTrue( pos instanceof DirectPosition2D );
		
		assertEquals( pos.getOrdinate( 0 ), 1.0, 0 );
		assertEquals( pos.getOrdinate( 1 ), 2.0, 0 );
	}

	
}
