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
package org.geotools.kml.bindings;

import java.util.Collection;
import org.geotools.feature.Feature;
import org.geotools.kml.KML;
import org.geotools.kml.KMLTestSupport;
import org.geotools.xml.Binding;


public class DocumentTypeBindingTest extends KMLTestSupport {
    public void testType() throws Exception {
        assertEquals(Feature.class, binding(KML.DocumentType).getType());
    }

    public void testExecutionMode() throws Exception {
        assertEquals(Binding.AFTER, binding(KML.DocumentType).getExecutionMode());
    }

    public void testParse() throws Exception {
        String xml = "<Document>" + "<name>document</name>" + "<Placemark>" + "<Point>"
            + "<coordinates>0,0</coordinates>" + "</Point>" + "</Placemark>" + "</Document>";
        buildDocument(xml);

        Feature document = (Feature) parse();
        assertEquals("document", document.getAttribute("name"));

        Collection features = (Collection) document.getAttribute("Feature");
        assertEquals(1, features.size());
    }
}
