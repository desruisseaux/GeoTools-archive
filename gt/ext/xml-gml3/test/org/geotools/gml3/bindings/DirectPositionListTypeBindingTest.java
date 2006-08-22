package org.geotools.gml3.bindings;

import org.geotools.geometry.DirectPosition1D;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.gml3.GML3TestSupport;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DirectPositionListTypeBindingTest extends GML3TestSupport {

	protected Element createRootElement(Document doc) {
		return doc.createElementNS( GML.NAMESPACE, GML.POSLIST.getLocalPart() );
	}

	public void test1D() throws Exception {
		document.getDocumentElement().setAttribute( "count", "2" );
		document.getDocumentElement().appendChild( document.createTextNode( "1.0 2.0 " ) );
		
		DirectPosition[] dps = (DirectPosition[]) parse();
		assertNotNull( dps );
		
		assertEquals( 2, dps.length );
		assertTrue( dps[0] instanceof DirectPosition1D );
		assertTrue( dps[1] instanceof DirectPosition1D );
	
		assertEquals( 1d, dps[0].getOrdinate( 0 ), 0d );
		assertEquals( 2d, dps[1].getOrdinate( 0 ), 0d );
	}
	
	public void test2D() throws Exception {
		document.getDocumentElement().setAttribute( "count", "1" );
		document.getDocumentElement().appendChild( document.createTextNode( "1.0 2.0 " ) );
		
		DirectPosition[] dps = (DirectPosition[]) parse();
		assertNotNull( dps );
		
		assertEquals( 1, dps.length );
		assertTrue( dps[0] instanceof DirectPosition2D );
		
		assertEquals( 1d, dps[0].getOrdinate( 0 ), 0d );
		assertEquals( 2d, dps[0].getOrdinate( 1 ), 0d );
	}
}
