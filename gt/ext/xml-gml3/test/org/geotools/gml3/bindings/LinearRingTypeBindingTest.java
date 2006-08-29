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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import org.geotools.gml3.GML3TestSupport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class LinearRingTypeBindingTest extends GML3TestSupport {
    protected Element createRootElement(Document doc) {
        return document.createElementNS(GML.LINEARRING.getNamespaceURI(),
            GML.LINEARRING.getLocalPart());
    }

    public void testPos() throws Exception {
        Element pos = document.createElementNS(GML.POS.getNamespaceURI(),
                GML.POS.getLocalPart());
        pos.appendChild(document.createTextNode("1.0 2.0"));
        document.getDocumentElement().appendChild(pos);

        pos = document.createElementNS(GML.POS.getNamespaceURI(),
                GML.POS.getLocalPart());
        pos.appendChild(document.createTextNode("3.0 4.0"));
        document.getDocumentElement().appendChild(pos);

        pos = document.createElementNS(GML.POS.getNamespaceURI(),
                GML.POS.getLocalPart());
        pos.appendChild(document.createTextNode("5.0 6.0"));
        document.getDocumentElement().appendChild(pos);

        pos = document.createElementNS(GML.POS.getNamespaceURI(),
                GML.POS.getLocalPart());
        pos.appendChild(document.createTextNode("1.0 2.0"));
        document.getDocumentElement().appendChild(pos);

        LinearRing line = (LinearRing) parse();
        assertNotNull(line);

        assertEquals(new Coordinate(1d, 2d), line.getPointN(0).getCoordinate());
        assertEquals(new Coordinate(3d, 4d), line.getPointN(1).getCoordinate());
        assertEquals(new Coordinate(5d, 6d), line.getPointN(2).getCoordinate());
        assertEquals(new Coordinate(1d, 2d), line.getPointN(3).getCoordinate());
    }

    public void testPosList() throws Exception {
        Element posList = document.createElementNS(GML.POSLIST.getNamespaceURI(),
                GML.POSLIST.getLocalPart());
        posList.appendChild(document.createTextNode(
                "1.0 2.0 3.0 4.0 5.0 6.0 1.0 2.0"));
        document.getDocumentElement().appendChild(posList);

        LinearRing line = (LinearRing) parse();
        assertNotNull(line);

        assertEquals(new Coordinate(1d, 2d), line.getPointN(0).getCoordinate());
        assertEquals(new Coordinate(3d, 4d), line.getPointN(1).getCoordinate());
        assertEquals(new Coordinate(1d, 2d), line.getPointN(0).getCoordinate());
        assertEquals(new Coordinate(3d, 4d), line.getPointN(1).getCoordinate());
        assertEquals(new Coordinate(5d, 6d), line.getPointN(2).getCoordinate());
        assertEquals(new Coordinate(1d, 2d), line.getPointN(3).getCoordinate());
    }
}
