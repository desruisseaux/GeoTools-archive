/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.data.postgis;

import java.io.IOException;

import junit.framework.TestCase;

import org.geotools.data.postgis.attributeio.WKBEncoder;
import org.wkb4j.engine.WKBParser;
import org.wkb4j.jts.JTSFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;


/**
 * Simple test suite for the WKBEncoder class
 *
 * @author Andrea Aime
 */
public class WKBEncoderTest extends TestCase {
    GeometryFactory gf = new GeometryFactory();
    JTSFactory jtsFactory = new JTSFactory();
    WKBParser parser = new WKBParser(jtsFactory);

    private void encodeAndCompare(Geometry g) throws IOException {
        byte[] wkb = WKBEncoder.encodeGeometry(g);
        parser.parseData(wkb, 0);

        Geometry g1 = (Geometry) jtsFactory.getGeometries().get(0);
        assertTrue(g.equalsExact(g1));
    }

    public void testEncodePoint() throws IOException {
        Point p = createPoint();
        encodeAndCompare(p);
    }

    private Point createPoint() {
        Point p = gf.createPoint(new Coordinate(10, 0));

        return p;
    }

    public void testEncodeLine() throws IOException {
        LineString ls = createLineString();
        encodeAndCompare(ls);
    }

    public void testEncodePolygon() throws IOException {
        Polygon polygon = createPolygon();
        encodeAndCompare(polygon);
    }

    private Polygon createPolygon() {
        LinearRing border = gf.createLinearRing(new Coordinate[] {
                    new Coordinate(0, 0), new Coordinate(10, 0),
                    new Coordinate(10, 10), new Coordinate(0, 10),
                    new Coordinate(0, 0)
                });
        LinearRing hole = gf.createLinearRing(new Coordinate[] {
                    new Coordinate(1, 1), new Coordinate(2, 1),
                    new Coordinate(1, 2), new Coordinate(1, 1)
                });
        Polygon polygon = gf.createPolygon(border, new LinearRing[] { hole });

        return polygon;
    }

    public void testEncodeMultiPoint() throws IOException {
        Point p = createPoint();
        Point[] points = new Point[10];

        for (int i = 0; i < points.length; i++) {
            points[i] = (Point) cloneAndOffset(p, i * 100, 0);
        }

        encodeAndCompare(gf.createMultiPoint(points));
    }

    public void testEncodeMultiLine() throws IOException {
        LineString ls = createLineString();
        LineString[] geoms = new LineString[10];

        for (int i = 0; i < geoms.length; i++) {
            geoms[i] = (LineString) cloneAndOffset(ls, i * 100, 0);
        }

        encodeAndCompare(gf.createMultiLineString(geoms));
    }

   
    private LineString createLineString() {
        LineString ls = gf.createLineString(new Coordinate[] {
                    new Coordinate(0, 0), new Coordinate(10, 0),
                    new Coordinate(10, 10)
                });

        return ls;
    }

    public void testEncodeMultiPolygon() throws IOException {
        Polygon p = createPolygon();
        Polygon[] geoms = new Polygon[10];

        for (int i = 0; i < geoms.length; i++) {
            geoms[i] = (Polygon) cloneAndOffset(p, i * 100, 0);
        }

        encodeAndCompare(gf.createMultiPolygon(geoms));
    }
    
    public void testEncodeGeometryCollection() throws IOException {
        Polygon p = createPolygon();
        Geometry[] geoms = new Geometry[] {createPoint(), createLineString(), createPolygon()};

        encodeAndCompare(gf.createGeometryCollection(geoms));
    }

    private Geometry cloneAndOffset(Geometry g, double offsetX, double offsetY) {
        Geometry g1 = (Geometry) g.clone();
        g1.apply(new CoordinateMover(offsetX, offsetY));

        return g1;
    }

    private class CoordinateMover implements CoordinateFilter {
        private double offsetY;
        private double offsetX;

        public CoordinateMover(double offsetX, double offsetY) {
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }

        /**
         * @see com.vividsolutions.jts.geom.CoordinateFilter#filter(com.vividsolutions.jts.geom.Coordinate)
         */
        public void filter(Coordinate c) {
            c.x += offsetX;
            c.y += offsetY;
        }
    }
}
