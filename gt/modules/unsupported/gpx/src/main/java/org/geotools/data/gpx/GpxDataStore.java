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
package org.geotools.data.gpx;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;
import org.geotools.data.AbstractDataStore;
import org.geotools.data.FeatureReader;
import org.geotools.data.gpx.memory.Gpx;
import org.geotools.data.gpx.memory.GpxFormatException;
import org.geotools.data.gpx.memory.GpxParser;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeBuilder;
import org.geotools.feature.SchemaException;
import org.geotools.referencing.crs.DefaultGeographicCRS;


public class GpxDataStore extends AbstractDataStore {
    public static final String TYPE_NAME_POINT = "point";
    public static final String TYPE_NAME_TRACK = "track";
    private final URL gpxURL;
    protected URI namespace;
    private final Gpx gpxData;
    private FeatureType pointType = null;
    private FeatureType trackType = null;

    public GpxDataStore(URL url) throws IOException {
        super(false);

        String filename = null;

        if (url == null) {
            throw new NullPointerException("Null URL for GpxDataStore");
        }

        try {
            filename = java.net.URLDecoder.decode(url.getFile(), "US-ASCII");
        } catch (java.io.UnsupportedEncodingException use) {
            throw new java.net.MalformedURLException("Unable to decode " + url + " cause "
                + use.getMessage());
        }

        gpxURL = new URL(url.getProtocol(), url.getHost(), url.getPort(), filename);
        init();
        gpxData = loadGpx();
    }

    public GpxDataStore(URL url, URI namespace) throws IOException {
        this(url);
        this.namespace = namespace;
    }

    private void init() {
        FeatureTypeBuilder ftb = FeatureTypeBuilder.newInstance(TYPE_NAME_POINT);
        ftb.addType(AttributeTypeFactory.newAttributeType("the_geom", Point.class, true, 0, null,
                DefaultGeographicCRS.WGS84));
        ftb.addType(AttributeTypeFactory.newAttributeType("name", String.class, true, -1, null));
        ftb.addType(AttributeTypeFactory.newAttributeType("description", String.class, true, -1,
                null));
        ftb.addType(AttributeTypeFactory.newAttributeType("comment", String.class, true, -1, null));
        ftb.setNamespace(namespace);

        try {
            pointType = ftb.getFeatureType(); // TODO simpleFeatureType ??
        } catch (SchemaException e) {
            throw new RuntimeException(e);
        }

        ftb = FeatureTypeBuilder.newInstance(TYPE_NAME_TRACK);
        ftb.addType(AttributeTypeFactory.newAttributeType("the_geom", MultiLineString.class, true,
                0, null, DefaultGeographicCRS.WGS84));
        ftb.addType(AttributeTypeFactory.newAttributeType("name", String.class, true, -1, null));
        ftb.addType(AttributeTypeFactory.newAttributeType("description", String.class, true, -1,
                null));
        ftb.addType(AttributeTypeFactory.newAttributeType("comment", String.class, true, -1, null));
        ftb.setNamespace(namespace);

        try {
            trackType = ftb.getFeatureType(); // TODO simpleFeatureType ??
        } catch (SchemaException e) {
            throw new RuntimeException(e);
        }
    }

    private Gpx loadGpx() throws IOException {
        Gpx gpx;

        File f = new File(gpxURL.getFile());
        FileInputStream is = new FileInputStream(f);

        GpxParser p;

        try {
            p = new GpxParser(is, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        try {
            gpx = p.parse();
        } catch (GpxFormatException e1) {
            throw new IOException("File format error!" + e1.getMessage());
        }

        try {
            is.close();
        } catch (IOException e) {
            // standard geotools logging needed here, low level message.
        }

        return gpx;
    }

    @Override
    protected FeatureReader getFeatureReader(String typeName) {
        if (TYPE_NAME_POINT.equals(typeName)) {
            return new GpxPointsReader(gpxData, pointType);
        } else if (TYPE_NAME_TRACK.equals(typeName)) {
            return new GpxTracksReader(gpxData, trackType);
        } else {
            throw new IllegalArgumentException("No such type: " + typeName);
        }
    }

    @Override
    public FeatureType getSchema(String typeName) {
        if (TYPE_NAME_POINT.equals(typeName)) {
            return pointType;
        } else if (TYPE_NAME_TRACK.equals(typeName)) {
            return trackType;
        } else {
            throw new IllegalArgumentException("No such type: " + typeName);
        }
    }

    @Override
    public String[] getTypeNames() throws IOException {
        return new String[] { TYPE_NAME_POINT, TYPE_NAME_TRACK };
    }
}
