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
package org.geotools.gml.producer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.logging.Logger;


//import org.geotools.feature.*;

/*
 * Utilities for gml and xml;
 * @author Chris Holmes, TOPP
 */
class GMLUtils {
    /** The logger for the filter module. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.gml.producer");
    public static final String GML_URL = "http://www.opengis.net/gml";

    /** Internal representation of OGC SF Point */
    protected static final int POINT = 1;

    /** Internal representation of OGC SF LineString */
    protected static final int LINESTRING = 2;

    /** Internal representation of OGC SF Polygon */
    protected static final int POLYGON = 3;

    /** Internal representation of OGC SF MultiPoint */
    protected static final int MULTIPOINT = 4;

    /** Internal representation of OGC SF MultiLineString */
    protected static final int MULTILINESTRING = 5;

    /** Internal representation of OGC SF MultiPolygon */
    protected static final int MULTIPOLYGON = 6;

    /** Internal representation of OGC SF MultiGeometry */
    protected static final int MULTIGEOMETRY = 7;

    public static String getGeometryEnd(String geomType) {
        return "<gml:" + geomType;
    }

    public static String getGeometryStart(String gid, String srs,
        String geomType) {
        return "<gml:" + geomType + " gid=\"" + gid + "\" srsName=\"" +
        "http://www.opengis.net/gml/srs/epsg.xml#" + srs + "\">";
    }

    public static String getGeometryName(Geometry geometry) {
        Class geomClass = geometry.getClass();

        if (geomClass.equals(Point.class)) {
            return "Point";
        } else if (geomClass.equals(LineString.class)) {
            return "LineString";
        } else if (geomClass.equals(Polygon.class)) {
            return "Polygon";
        } else if (geomClass.equals(MultiPoint.class)) {
            return "MultiPoint";
        } else if (geomClass.equals(MultiLineString.class)) {
            return "MultiLineString";
        } else if (geomClass.equals(MultiPolygon.class)) {
            return "MultiPolygon";
        } else if (geomClass.equals(GeometryCollection.class)) {
            return "GeometryCollection";
        } else {
            //HACK!!! throw exception
            return null;
        }
    }

    public static int getGeometryType(Geometry geometry) {
        Class geomClass = geometry.getClass();

        if (geomClass.equals(Point.class)) {
            LOGGER.finest("found point");

            return POINT;
        } else if (geomClass.equals(LineString.class)) {
            LOGGER.finest("found linestring");

            return LINESTRING;
        } else if (geomClass.equals(Polygon.class)) {
            LOGGER.finest("found polygon");

            return POLYGON;
        } else if (geomClass.equals(MultiPoint.class)) {
            LOGGER.finest("found multiPoint");

            return MULTIPOINT;
        } else if (geomClass.equals(MultiLineString.class)) {
            return MULTILINESTRING;
        } else if (geomClass.equals(MultiPolygon.class)) {
            return MULTIPOLYGON;
        } else if (geomClass.equals(GeometryCollection.class)) {
            return MULTIGEOMETRY;
        } else {
            return -1;

            //HACK!!! throw exception.
        }
    }

    /**
     * Parses the passed string, and encodes the special characters (used in
     * xml for special purposes) with the appropriate codes. e.g. right
     * bracket char is changed to '&lt;'
     *
     * @param inData the string to encode.
     *
     * @return the encoded string. Returns null, if null is passed as argument
     *
     * @task TODO: Take output as a param, write directly to out, send the
     *       characters straight out, doing translation on the fly.
     */
    public static String encodeXML(String inData) {
        //return null, if null is passed as argument
        if (inData == null) {
            return null;
        }

        //if no special characters, just return
        //(for optimization. Though may be an overhead, but for most of the
        //strings, this will save time)
        if ((inData.indexOf('&') == -1) && (inData.indexOf('<') == -1) &&
                (inData.indexOf('>') == -1) && (inData.indexOf('\'') == -1) &&
                (inData.indexOf('\"') == -1)) {
            return inData;
        }

        //get the length of input String
        int length = inData.length();

        //create a StringBuffer of double the size (size is just for guidance
        //so as to reduce increase-capacity operations. The actual size of
        //the resulting string may be even greater than we specified, but is
        //extremely rare)
        StringBuffer buffer = new StringBuffer(2 * length);

        char charToCompare;

        //iterate over the input String
        for (int i = 0; i < length; i++) {
            charToCompare = inData.charAt(i);

            //if the ith character is special character, replace by code
            if (charToCompare == '&') {
                buffer.append("&amp;");
            } else if (charToCompare == '<') {
                buffer.append("&lt;");
            } else if (charToCompare == '>') {
                buffer.append("&gt;");
            } else if (charToCompare == '\"') {
                buffer.append("&quot;");
            } else if (charToCompare == '\'') {
                buffer.append("&apos;");
            } else {
                buffer.append(charToCompare);
            }
        }

        //return the encoded string
        return buffer.toString();
    }
}
