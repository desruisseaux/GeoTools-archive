/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.gml2.bindings;

import org.geotools.gml2.GML;
import org.geotools.xml.Binding;
import org.w3c.dom.Document;

import com.vividsolutions.jts.geom.MultiLineString;


public class GMLMultiLineStringPropertyTypeBinding2Test extends GMLTestSupport {
    public void testType() {
        assertEquals(MultiLineString.class, binding(GML.MultiLineStringPropertyType).getType());
    }

    public void testExecutionMode() {
        assertEquals(Binding.AFTER, binding(GML.MultiLineStringPropertyType).getExecutionMode());
    }

    public void testParse() throws Exception {
        GML2MockData.multiLineStringProperty(document, document);

        MultiLineString mp = (MultiLineString) parse();
        assertNotNull(mp);
    }

    public void testEncode() throws Exception {
        Document doc = encode(GML2MockData.multiLineString(), GML.multiLineStringProperty);

        assertEquals(1,
            doc.getElementsByTagNameNS(GML.NAMESPACE, GML.MultiLineString.getLocalPart()).getLength());
    }
}
