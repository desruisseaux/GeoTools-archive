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
/*
 * package net.refractionsresearch.cwb.editor.util; WKBParser.java,v 1.3 Jan 2, 2004 kneufeld
 *
 * Copyright (c) 2004, Refractions Research Inc.
 */
package org.geotools.data.postgis.attributeio;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.wkb4j.engine.WKBGeometryTypes;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * This class is a JTS <->WKB converter that will convert JTS Geometry objects
 * into Strings of hexadecimal characters that represent their Well Known Binary
 * (WKB) equivalents.
 * <p>
 * 
 * For example, in a Postgres database with PostGIS capabilities, the following
 * statements can be executed:
 * </p>
 * 
 * <pre>
 * 
 *  
 *    select asbinary('POINT(1 1)'::geometry, 'XDR');
 *                     asbinary                  
 *    --------------------------------------------
 *     00000000013FF00000000000003FF0000000000000
 *    (1 row)
 *   
 *    select geomfromwkb('00000000013FF00000000000003FF0000000000000'::wkb);
 *        geomfromwkb    
 *    -------------------
 *     SRID=0;POINT(1 1)
 *    (1 row)
 *   
 *  
 * </pre>
 * 
 * </p>
 * <p>
 * The WKBParser#WKB2Geometry(String) method will create a new JTS POINT object
 * when passed "00000000013FF00000000000003FF0000000000000" as a parameter.
 * Further, the WKBParser#Geometry2WKB(Geometry) method will create the string
 * "00000000013FF00000000000003FF0000000000000" when passed a JTS POINT(1 1)
 * geometry object.
 * </p>
 * <p>
 * Note: when using PostGIS's asbinary(...) method, use 'XDR' as a parameter to
 * use BigEndian format in the WKB representation.
 * </p>
 * @source $URL$
 */
public class WKBEncoder implements WKBGeometryTypes {
    private static byte wkbXDR = 0;

    private static int wkbNDR = 1;

    private static char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    /**
     * This method will convert a JTS Geometry to a an array of bytes that
     * represent Well Known Binary representation of the geometry object in big
     * endian encoding.
     * 
     * @param geom -
     *            a standard JTS geometry subclass, custom extensions are not
     *            supported
     * 
     * @return a byte array that represent the Well Know Binary representation
     *         of the param geom in big endian encoding.
     * 
     * @throws IOException
     *             if an I/O error occurs with the data output stream
     */
    public static byte[] encodeGeometry(Geometry geom) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        encodeWKBGeometry(geom, dos);

        dos.flush();

        return baos.toByteArray();
    }

    /**
     * Encodes a generic geometry to WKB by forwarding it to the supporting
     * methods
     * 
     * @param geom -
     *            the Geometry that will be encoded to WKB
     * @param dos -
     *            the DataOutputStream that will be
     * 
     * @throws IOException
     *             if an I/O error occurs with the data output stream
     * @throws IllegalArgumentException
     *             on unsupported Geometry subclasses
     */
    private static void encodeWKBGeometry(Geometry geom, DataOutputStream dos)
            throws IOException {
        if (geom instanceof Point) {
            encodeWKBPoint((Point) geom, dos);
        } else if (geom instanceof LineString) {
            encodeWKBLineString((LineString) geom, dos);
        } else if (geom instanceof Polygon) {
            encodeWKBPolygon((Polygon) geom, dos);
        } else if (geom instanceof MultiPoint) {
            encodeWKBMultiPoint((MultiPoint) geom, dos);
        } else if (geom instanceof MultiLineString) {
            encodeWKBMultiLineString((MultiLineString) geom, dos);
        } else if (geom instanceof MultiPolygon) {
            encodeWKBMultiPolygon((MultiPolygon) geom, dos);
        } else if (geom instanceof GeometryCollection) {
            encodeWBKGeometryCollection((GeometryCollection) geom, dos);
        } else {
            throw new IllegalArgumentException("A parser for "
                    + geom.getClass().getName()
                    + " has not been implemented yet.");
        }
    }

    private static void encodeWBKGeometryCollection(GeometryCollection gc,
            DataOutputStream dos) throws IOException {
        // write byteOrder
        dos.writeByte(wkbXDR);

        // write wkbType
        dos.writeInt(wkbGeometryCollection);

        // write num_wkbLineStrings
        dos.writeInt(gc.getNumGeometries());

        // write WKBLineString[num_wkbLineStrings]
        for (int i = 0; i < gc.getNumGeometries(); i++) {
            encodeWKBGeometry(gc.getGeometryN(i), dos);
        }
    }

    /**
     * This method will convert a JTS Geometry to a string of hex characters
     * that represent Well Known Binary representation of the geometry object.
     * 
     * @param geom
     *            Either Point, LineString, Polygon, MultiPoint,
     *            MultiLineString, or MultiPolygon
     * 
     * @return a String of characters that represent the Well Know Binary
     *         representation of the param geom. In particular, each character
     *         is a value of 0-9, A, B ,C, D, E, or F.
     * 
     * @throws Exception
     *             if an I/O error occurs with the EndianDataOutputStream or the
     *             parser doesn't have any implementation for the specifed
     *             Geometry.
     */
    public static String encodeGeometryHex(Geometry geom) throws IOException {
        return bytesToString(encodeGeometry(geom));
    }

    private static void writePoint(Point p, DataOutputStream edos)
            throws IOException {
        edos.writeDouble(p.getX());
        edos.writeDouble(p.getY());
    }

    private static void writeLinearRing(LineString lr, DataOutputStream edos)
            throws IOException {
        int numPoints = lr.getNumPoints();
        edos.writeInt(numPoints);

        for (int i = 0; i < numPoints; i++) {
            writePoint(lr.getPointN(i), edos);
        }
    }

    private static void encodeWKBPoint(Point p, DataOutputStream edos)
            throws IOException {
        // write byteOrder
        edos.writeByte(wkbXDR);

        // write wkbType
        edos.writeInt(wkbPoint);

        // write point
        writePoint(p, edos);
    }

    private static void encodeWKBLineString(LineString ls, DataOutputStream edos)
            throws IOException {
        // write byteOrder
        edos.writeByte(wkbXDR);

        // write wkbType
        edos.writeInt(wkbLineString);

        // write numPoints
        edos.writeInt(ls.getNumPoints());

        // write points[numPoints]
        for (int i = 0; i < ls.getNumPoints(); i++) {
            writePoint(ls.getPointN(i), edos);
        }
    }

    private static void encodeWKBPolygon(Polygon p, DataOutputStream edos)
            throws IOException {
        // write byteOrder
        edos.writeByte(wkbXDR);

        // write wkbType
        edos.writeInt(wkbPolygon);

        // write num_Rings
        edos.writeInt(p.getNumInteriorRing() + 1); // num_interior_rings +
        // exterior ring

        writeLinearRing(p.getExteriorRing(), edos);

        // write rings[numRings]
        for (int i = 0; i < p.getNumInteriorRing(); i++) {
            writeLinearRing(p.getInteriorRingN(i), edos);
        }
    }

    private static void encodeWKBMultiPoint(MultiPoint mp, DataOutputStream edos)
            throws IOException {
        // write byteOrder
        edos.writeByte(wkbXDR);

        // write wkbType
        edos.writeInt(wkbMultiPoint);

        // write num_wkbPoints
        edos.writeInt(mp.getNumGeometries());

        // write WKBPoints[num_wkbPoints]
        for (int i = 0; i < mp.getNumGeometries(); i++) {
            encodeWKBPoint((Point) mp.getGeometryN(i), edos);
        }
    }

    private static void encodeWKBMultiLineString(MultiLineString mls,
            DataOutputStream edos) throws IOException {
        // write byteOrder
        edos.writeByte(wkbXDR);

        // write wkbType
        edos.writeInt(wkbMultiLineString);

        // write num_wkbLineStrings
        edos.writeInt(mls.getNumGeometries());

        // write WKBLineString[num_wkbLineStrings]
        for (int i = 0; i < mls.getNumGeometries(); i++) {
            encodeWKBLineString((LineString) mls.getGeometryN(i), edos);
        }
    }

    private static void encodeWKBMultiPolygon(MultiPolygon mp,
            DataOutputStream edos) throws IOException {
        // write byteOrder
        edos.writeByte(wkbXDR);

        // write wkbType
        edos.writeInt(wkbMultiPolygon);

        // write num_wkbLineStrings
        edos.writeInt(mp.getNumGeometries());

        // write WKBLineString[num_wkbLineStrings]
        for (int i = 0; i < mp.getNumGeometries(); i++) {
            encodeWKBPolygon((Polygon) mp.getGeometryN(i), edos);
        }
    }

    /**
     * This method will convert a byte[] to a string of hex characters that
     * represent the byte[]
     * 
     * @param bytes
     *            an array of bytes
     * 
     * @return a String that represents the byte[]. In particular, each
     *         character has a value of 0-9, A, B, C, D, E, or F. The characters
     *         can be read by grouping them by pairs (ie. the first two
     *         characters represent the first byte in the array).
     */
    public static String bytesToString(byte[] bytes) {
        int length = bytes.length;
        StringBuffer sb = new StringBuffer(length * 2);

        for (int i = 0; i < length; i++) {
            byte b = bytes[i];
            sb.append(hexDigits[(b >> 4) & 0x0f]);
            sb.append(hexDigits[b & 0x0f]);
        }

        // copy-less string conversion
        return sb.toString();
    }

//    /**
//     * This method will convert a byte[] to a string of hex characters that
//     * represent the byte[]
//     * 
//     * @param b
//     *            an array of bytes
//     * @return a String that represents the byte[]. In particular, each
//     *         character has a value of 0-9, A, B, C, D, E, or F. The characters
//     *         can be read by grouping them by pairs (ie. the first two
//     *         characters represent the first byte in the array).
//     */
//    private static String bytesToStringOld(byte[] b) {
//        char[] array = new char[b.length * 2];
//        for (int i = 0; i < b.length; i++) {
//            array[i * 2] = hexDigits[(b[i] >> 4) & 0x0f];
//            array[i * 2 + 1] = hexDigits[b[i] & 0x0f];
//        }
//        return new String(array);
//    }
//    
//    public static void main(String[] args) throws IOException {
//        GeometryFactory gf = new GeometryFactory();
//        Polygon[] polygons = new Polygon[20000];
//        for(int i = 0; i < polygons.length; i++) {
//            polygons[i] = gf.createPolygon(gf.createLinearRing(new Coordinate[] {new Coordinate(0, 0), new Coordinate(10, 0), new Coordinate(10, 10), new Coordinate(0, 10), new Coordinate(0, 0)}), null);
//        }
//        MultiPolygon mp = gf.createMultiPolygon(polygons);
//        
//        byte[] bytes = GeometryToWKB(mp);
//        
//        long t1 = System.currentTimeMillis();
//        String hex1 = bytesToString(bytes);
//        long t2 = System.currentTimeMillis();
//        String hex2 = bytesToStringOld(bytes);
//        long t3 = System.currentTimeMillis();
//        
//        System.out.println("T1: " + (t2 - t1) + " T2: " + (t3 - t1));
//        
//        System.out.println("Equals: " + hex1.equals(hex2));
//    }
}
