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

import com.vividsolutions.jts.geom.MultiPoint;
import org.geotools.gml3.GML3TestSupport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class MultiPointTypeBindingTest extends GML3TestSupport {
    protected Element createRootElement(Document doc) {
        return doc.createElementNS(GML.MULTIPOINT.getNamespaceURI(),
            GML.MULTIPOINT.getLocalPart());
    }

    public void test() throws Exception {
        // 2 pointMember elements
        Element pos = document.createElementNS(GML.POS.getNamespaceURI(),
                GML.POS.getLocalPart());
        pos.appendChild(document.createTextNode("1.0 2.0"));

        Element point = document.createElementNS(GML.POINT.getNamespaceURI(),
                GML.POINT.getLocalPart());
        point.appendChild(pos);

        Element pointMember = document.createElementNS(GML.POINTMEMBER
                .getNamespaceURI(), GML.POINTMEMBER.getLocalPart());
        pointMember.appendChild(point);
        document.getDocumentElement().appendChild(pointMember);

        pos = document.createElementNS(GML.POS.getNamespaceURI(),
                GML.POS.getLocalPart());
        pos.appendChild(document.createTextNode("3.0 4.0"));

        point = document.createElementNS(GML.POINT.getNamespaceURI(),
                GML.POINT.getLocalPart());
        point.appendChild(pos);

        pointMember = document.createElementNS(GML.POINTMEMBER.getNamespaceURI(),
                GML.POINTMEMBER.getLocalPart());
        pointMember.appendChild(point);
        document.getDocumentElement().appendChild(pointMember);

        //1 pointMembers elmenet with 2 members
        Element pointMembers = document.createElementNS(GML.POINTMEMBERS
                .getNamespaceURI(), GML.POINTMEMBERS.getLocalPart());
        pos = document.createElementNS(GML.POS.getNamespaceURI(),
                GML.POS.getLocalPart());
        pos.appendChild(document.createTextNode("5.0 6.0"));

        point = document.createElementNS(GML.POINT.getNamespaceURI(),
                GML.POINT.getLocalPart());
        point.appendChild(pos);
        pointMembers.appendChild(point);

        pos = document.createElementNS(GML.POS.getNamespaceURI(),
                GML.POS.getLocalPart());
        pos.appendChild(document.createTextNode("7.0 8.0"));

        point = document.createElementNS(GML.POINT.getNamespaceURI(),
                GML.POINT.getLocalPart());
        point.appendChild(pos);
        pointMembers.appendChild(point);

        document.getDocumentElement().appendChild(pointMembers);

        MultiPoint multiPoint = (MultiPoint) parse();
        assertNotNull(multiPoint);

        assertEquals(4, multiPoint.getNumPoints());
    }
}
