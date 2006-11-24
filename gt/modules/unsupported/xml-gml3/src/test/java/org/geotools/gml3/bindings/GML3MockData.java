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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import javax.xml.namespace.QName;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import org.geotools.gml3.Curve;


/**
 * Utility class for creating test xml data for gml3 bindings.
 *
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class GML3MockData {
    static GeometryFactory gf = new GeometryFactory();

    static Element point(Document document, Node parent) {
        Element point = element(GML.Point, document, parent);

        Element pos = element(GML.pos, document, point);
        pos.appendChild(document.createTextNode("1.0 2.0 "));

        return point;
    }

    static LineString lineString() {
        return gf.createLineString(new Coordinate[] { new Coordinate(1, 2), new Coordinate(3, 4) });
    }

    static Element lineString(Document document, Node parent) {
        return lineStringWithPos(document, parent);
    }

    static Element lineStringWithPos(Document document, Node parent) {
        Element lineString = element(GML.LineString, document, parent);

        Element pos = element(GML.pos, document, lineString);
        pos.appendChild(document.createTextNode("1.0 2.0"));

        pos = element(GML.pos, document, lineString);
        pos.appendChild(document.createTextNode("3.0 4.0"));

        return lineString;
    }

    static Element lineStringWithPosList(Document document, Node parent) {
        Element lineString = element(GML.LineString, document, parent);
        Element posList = element(GML.posList, document, lineString);
        posList.appendChild(document.createTextNode("1.0 2.0 3.0 4.0"));

        return lineString;
    }

    static Element linearRing(Document document, Node parent) {
        return linearRingWithPos(document, parent);
    }

    static Element linearRingWithPos(Document document, Node parent) {
        Element linearRing = element(GML.LinearRing, document, parent);

        Element pos = element(GML.pos, document, linearRing);
        pos.appendChild(document.createTextNode("1.0 2.0"));

        pos = element(GML.pos, document, linearRing);
        pos.appendChild(document.createTextNode("3.0 4.0"));

        pos = element(GML.pos, document, linearRing);
        pos.appendChild(document.createTextNode("5.0 6.0"));

        pos = element(GML.pos, document, linearRing);
        pos.appendChild(document.createTextNode("1.0 2.0"));

        return linearRing;
    }

    static Element linearRingWithPosList(Document document, Node parent) {
        Element linearRing = element(GML.LinearRing, document, parent);

        Element posList = element(GML.posList, document, linearRing);

        linearRing.appendChild(posList);
        posList.appendChild(document.createTextNode("1.0 2.0 3.0 4.0 5.0 6.0 1.0 2.0"));

        return linearRing;
    }

    static Curve curve() {
        return new Curve(new LineString[] { lineString() }, gf);
    }

    static Element polygonWithNoInterior(Document document, Node parent) {
        Element polygon = element(GML.Polygon, document, parent);

        Element exterior = element(GML.exterior, document, polygon);
        linearRing(document, exterior);

        return polygon;
    }

    static Element multiPoint(Document document, Node parent) {
        Element multiPoint = element(GML.MultiPoint, document, parent);

        // 2 pointMember elements
        Element pointMember = element(GML.pointMember, document, multiPoint);
        point(document, pointMember);

        pointMember = element(GML.pointMember, document, multiPoint);
        point(document, pointMember);

        //1 pointMembers elmenet with 2 members
        Element pointMembers = element(GML.pointMembers, document, multiPoint);
        point(document, pointMembers);
        point(document, pointMembers);

        return multiPoint;
    }

    static Element multiLineString(Document document, Node parent) {
        Element multiLineString = element(GML.MultiLineString, document, parent);

        Element lineStringMember = element(GML.lineStringMember, document, multiLineString);
        lineString(document, lineStringMember);

        lineStringMember = element(GML.lineStringMember, document, multiLineString);
        lineString(document, lineStringMember);

        return multiLineString;
    }

    static Element multiPolygon(Document document, Node parent) {
        Element multiPolygon = element(GML.MultiPolygon, document, parent);

        Element polygonMember = element(GML.polygonMember, document, multiPolygon);
        polygonWithNoInterior(document, polygonMember);

        polygonMember = element(GML.polygonMember, document, multiPolygon);
        polygonWithNoInterior(document, polygonMember);

        return multiPolygon;
    }

    static Element feature(Document document, Node parent) {
        Element feature = element(TEST.TestFeature, document, parent);
        Element geom = element(new QName(TEST.NAMESPACE, "geom"), document, feature);
        point(document, geom);

        Element count = GML3MockData.element(new QName(TEST.NAMESPACE, "count"), document, feature);
        count.appendChild(document.createTextNode("1"));

        return feature;
    }

    static Element featureMember(Document document, Node parent) {
        Element featureMember = element(GML.featureMember, document, parent);
        feature(document, featureMember);

        return featureMember;
    }

    static Element element(QName name, Document document, Node parent) {
        Element element = document.createElementNS(name.getNamespaceURI(), name.getLocalPart());

        if (parent != null) {
            parent.appendChild(element);
        }

        return element;
    }
}
