package org.geotools.xml;

import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


import org.geotools.xs.XSConfiguration;
import org.w3c.dom.Document;

import junit.framework.TestCase;

public class FacetTest extends TestCase {

    public void testList() throws Exception {
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware( true );
        
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse( getClass().getResourceAsStream("list.xml") );
        
        String schemaLocation = "http://geotools.org/test " + getClass().getResource("list.xsd" ).getFile();
        
        doc.getDocumentElement().setAttributeNS( 
            "http://www.w3.org/2001/XMLSchema-instance", "schemaLocation", schemaLocation 
        );
        
        DOMParser parser = new DOMParser( new XSConfiguration(), doc );
        Object o = parser.parse();
        assertTrue( o instanceof List );
        
        List list = (List) o;
        assertEquals( 3, list.size() );
        
        assertEquals( new Integer( 1 ), list.get( 0 ) );
        assertEquals( new Integer( 2 ), list.get( 1 ) );
        assertEquals( new Integer( 3 ), list.get( 2 ) );
    }
}
