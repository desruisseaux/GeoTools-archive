package org.geotools.gml3.bindings;

import org.geotools.gml3.GML3TestSupport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.vividsolutions.jts.geom.Polygon;

public class PolygonTypeBindingTest extends GML3TestSupport {

	protected Element createRootElement(Document doc) {
		return doc.createElementNS( 
			GML.POLYGON.getNamespaceURI(), GML.POLYGON.getLocalPart() 
		);
	}

	public void testNoInterior() throws Exception {
		Element posList = document.createElementNS(
			GML.POSLIST.getNamespaceURI(), GML.POSLIST.getLocalPart()	
		);
		posList.appendChild( document.createTextNode( "1 2 3 4 5 6 1 2") );
		
		Element linearRing = document.createElementNS( 
			GML.LINEARRING.getNamespaceURI(), GML.LINEARRING.getLocalPart()
		);
		linearRing.appendChild( posList );
		
		Element exterior = document.createElementNS(
			GML.EXTERIOR.getNamespaceURI(), GML.EXTERIOR.getLocalPart() 	
		);
		exterior.appendChild( linearRing );
		document.getDocumentElement().appendChild( exterior );
		
		Polygon polygon = (Polygon) parse();
		assertNotNull( polygon ) ;
		
	}
}
