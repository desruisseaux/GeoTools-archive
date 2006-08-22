package org.geotools.gml3.bindings;

import org.geotools.gml3.GML3TestSupport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.vividsolutions.jts.geom.MultiLineString;

public class MultiLineStringTypeBindingTest extends GML3TestSupport {

	protected Element createRootElement(Document doc) {
		return doc.createElementNS( 
			GML.MULTILINESTRING.getNamespaceURI(), GML.MULTILINESTRING.getLocalPart()
		);
	}
	
	public void test() throws Exception {
		Element posList = document.createElementNS( 
			GML.POSLIST.getNamespaceURI(), GML.POSLIST.getLocalPart() 
		);
		posList.appendChild( document.createTextNode( "1 2 3 4" ) );
		
		Element lineString = document.createElementNS( 
			GML.LINESTRING.getNamespaceURI(), GML.LINESTRING.getLocalPart()	
		);
		lineString.appendChild( posList );
		
		Element lineStringMember = document.createElementNS( 
			GML.LINESTRINGMEMBER.getNamespaceURI(), GML.LINESTRINGMEMBER.getLocalPart()	
		);
		lineStringMember.appendChild( lineString );
		
		document.getDocumentElement().appendChild( lineStringMember );
		
		posList = document.createElementNS( 
			GML.POSLIST.getNamespaceURI(), GML.POSLIST.getLocalPart() 
		);
		posList.appendChild( document.createTextNode( "5 6 7 8" ) );
		
		lineString = document.createElementNS( 
			GML.LINESTRING.getNamespaceURI(), GML.LINESTRING.getLocalPart()	
		);
		lineString.appendChild( posList );
		
		lineStringMember = document.createElementNS( 
			GML.LINESTRINGMEMBER.getNamespaceURI(), GML.LINESTRINGMEMBER.getLocalPart()	
		);
		lineStringMember.appendChild( lineString );
		
		document.getDocumentElement().appendChild( lineStringMember );
		
		MultiLineString multiLineString = (MultiLineString) parse();
		assertNotNull( multiLineString );
		
		assertEquals( 2, multiLineString.getNumGeometries() );
	}

}
