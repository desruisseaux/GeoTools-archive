package org.geotools.gml3.bindings;

import org.geotools.gml3.GML;
import org.geotools.gml3.GML3TestSupport;
import org.w3c.dom.Document;

public class LocationPropertyTypeBindingTest extends GML3TestSupport {

    public void testEncode() throws Exception {
        Document dom = encode(GML3MockData.point(), GML.location);
        assertEquals(1, dom.getElementsByTagName("gml:Point").getLength());
    }
}
