package org.geotools.gml2;

import java.net.URI;

import junit.framework.TestCase;

import org.geotools.gml2.bindings.GML2ParsingUtils;
import org.geotools.xml.impl.AttributeImpl;
import org.geotools.xml.impl.NodeImpl;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class GML2ParsingUtilsTest extends TestCase {

	public void testCRS() throws Exception {
	
		NodeImpl node = new NodeImpl( null );
		
		AttributeImpl att = new AttributeImpl( null );
		att.setName( "srsName" );
		
		NodeImpl attNode = new NodeImpl( att );
		attNode.setValue( new URI ( "EPSG:4326" ) );
		node.addAttribute( attNode );
		
		CoordinateReferenceSystem crs = GML2ParsingUtils.crs( node );
		assertNotNull( crs );
		
		attNode.setValue( new URI( "http://www.opengis.net/gml/srs/epsg.xml#4326") );
		crs = GML2ParsingUtils.crs( node );
		assertNotNull( crs );
	}
}
