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
package org.geotools.gml3.bindings;

import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import org.geotools.gml3.GML3TestSupport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class MultiPolygonTypeBindingTest extends GML3TestSupport {
    protected Element createRootElement(Document doc) {
        return doc.createElementNS(GML.MULTIPOLYGON.getNamespaceURI(),
            GML.MULTIPOLYGON.getLocalPart());
    }

    public void test() throws Exception {
        Element posList = document.createElementNS(GML.POSLIST.getNamespaceURI(),
                GML.POSLIST.getLocalPart());
        posList.appendChild(document.createTextNode("1 2 3 4 5 6 1 2"));

        Element linearRing = document.createElementNS(GML.LINEARRING
                .getNamespaceURI(), GML.LINEARRING.getLocalPart());
        linearRing.appendChild(posList);

        Element exterior = document.createElementNS(GML.EXTERIOR.getNamespaceURI(),
                GML.EXTERIOR.getLocalPart());
        exterior.appendChild(linearRing);

        Element polygon = document.createElementNS(GML.POLYGON.getNamespaceURI(),
                GML.POLYGON.getLocalPart());
        polygon.appendChild(exterior);

        Element polygonMember = document.createElementNS(GML.POLYGONMEMBER
                .getNamespaceURI(), GML.POLYGONMEMBER.getLocalPart());
        polygonMember.appendChild(polygon);
        document.getDocumentElement().appendChild(polygonMember);

        posList = document.createElementNS(GML.POSLIST.getNamespaceURI(),
                GML.POSLIST.getLocalPart());
        posList.appendChild(document.createTextNode("1 2 3 4 5 6 1 2"));

        linearRing = document.createElementNS(GML.LINEARRING.getNamespaceURI(),
                GML.LINEARRING.getLocalPart());
        linearRing.appendChild(posList);

        exterior = document.createElementNS(GML.EXTERIOR.getNamespaceURI(),
                GML.EXTERIOR.getLocalPart());
        exterior.appendChild(linearRing);

        polygon = document.createElementNS(GML.POLYGON.getNamespaceURI(),
                GML.POLYGON.getLocalPart());
        polygon.appendChild(exterior);

        polygonMember = document.createElementNS(GML.POLYGONMEMBER
                .getNamespaceURI(), GML.POLYGONMEMBER.getLocalPart());
        polygonMember.appendChild(polygon);
        document.getDocumentElement().appendChild(polygonMember);

        MultiPolygon multiPolygon = (MultiPolygon) parse();
        assertNotNull(multiPolygon);
        assertEquals(2, multiPolygon.getNumGeometries());
    }
}
