/*
 * Created on 2005.07.04.
 *
 * $Id$
 *
 */
package org.geotools.data.gpx.memory;

import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.xml.XppReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Date;


public class GpxParser {
    private HierarchicalStreamReader xReader;
    private Gpx gpx;

    public GpxParser(InputStream stream, String charset)
        throws UnsupportedEncodingException {
        xReader = new XppReader(new InputStreamReader(stream, charset));
    }

    public Gpx parse() throws GpxFormatException {
        gpx = new Gpx();

        String responseType = xReader.getNodeName();

        if (!"gpx".equals(responseType)) {
            throw new GpxFormatException("root element not 'gpx', but '" + responseType + "'");
        }

        while (xReader.hasMoreChildren()) {
            xReader.moveDown();

            String name = xReader.getNodeName();

            if ("metadata".equals(name)) {
                gpx.setMetadata(translateHeader());
            } else if ("wpt".equals(name)) {
                gpx.addWaypoint(translateWaypoint());
            } else if ("trk".equals(name)) {
                gpx.addTrack(translateTrack());
            }

            xReader.moveUp();
        }

        return gpx;
    }

    private GpxMetadata translateHeader() throws GpxFormatException {
        GpxMetadata hdr = new GpxMetadata();

        while (xReader.hasMoreChildren()) {
            xReader.moveDown();

            String name = xReader.getNodeName();

            // not yet implemented
            xReader.moveUp();
        }

        return hdr;
    }

    private GpxTrack translateTrack() throws GpxFormatException {
        GpxTrack track = new GpxTrack();

        while (xReader.hasMoreChildren()) {
            xReader.moveDown();

            String name = xReader.getNodeName();

            if ("name".equals(name)) {
                track.setName(xReader.getValue().trim());
            } else if ("trkseg".equals(name)) {
                track.addSegment(translateTrackSegment());
            }

            xReader.moveUp();
        }

        return track;
    }

    private GpxTrackSegment translateTrackSegment() throws GpxFormatException {
        GpxTrackSegment segment = new GpxTrackSegment();

        while (xReader.hasMoreChildren()) {
            xReader.moveDown();

            String name = xReader.getNodeName();

            if ("trkpt".equals(name)) {
                segment.addPoint(translateWaypoint());
            }

            xReader.moveUp();
        }

        return segment;
    }

    private GpxPoint translateWaypoint() throws GpxFormatException {
        GpxPoint point = new GpxPoint();

        double lat = parseDouble(xReader.getAttribute("lat"));
        point.setLat(lat);

        double lon = parseDouble(xReader.getAttribute("lon"));
        point.setLon(lon);

        while (xReader.hasMoreChildren()) {
            xReader.moveDown();

            String name = xReader.getNodeName();

            if ("ele".equals(name)) {
                point.setElevation(parseDouble(xReader.getValue().trim()));
            } else if ("time".equals(name)) {
                point.setDate(parseGPXTime(xReader.getValue().trim()));
            } else if ("name".equals(name)) {
                point.setName(xReader.getValue().trim());
            } else if ("cmd".equals(name)) {
                point.setComment(xReader.getValue().trim());
            } else if ("desc".equals(name)) {
                point.setDescription(xReader.getValue().trim());
            }

            xReader.moveUp();
        }

        return point;
    }

    /*
     * segedmetodusok, melyek maguk kezelik az exceptionokat es sajat fajtat csinalnak beloluk
     */
    private static double parseDouble(String value) throws GpxFormatException {
        if ((value == null) || (value.trim().length() == 0)) {
            return 0;
        }

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new GpxFormatException("not a valid response", e);
        }
    }

    private static Date parseGPXTime(String value) throws GpxFormatException {
        if ((value == null) || (value.trim().length() == 0)) {
            return null;
        }

        try {
            return DateUtil.parseGpxTime(value);
        } catch (ParseException e) {
            throw new GpxFormatException("not a valid response!", e);
        }
    }
}
