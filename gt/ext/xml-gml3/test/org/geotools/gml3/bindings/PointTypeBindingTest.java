package org.geotools.gml3.bindings;

import org.geotools.gml3.GML3TestSupport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

public class PointTypeBindingTest extends GML3TestSupport {

	protected Element createRootElement( Document doc ) {
		return document.createElementNS( 
			GML.POINT.getNamespaceURI(), GML.POINT.getLocalPart()
		);
	}
	
	public void testPos() throws Exception {
		Element pos = document.createElementNS( GML.POS.getNamespaceURI(), GML.POS.getLocalPart() );
		pos.appendChild( document.createTextNode( "1.0 2.0 ") );
		document.getDocumentElement().appendChild( pos );
		
		Point p = (Point) parse();
		assertNotNull( p );
		
		assertEquals( new Coordinate( 1d, 2d ), p.getCoordinate() );
	}

}
