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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;

import org.geotools.data.AbstractDataStore;
import org.geotools.data.FeatureReader;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeBuilder;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.gpx.GPXConfiguration;
import org.geotools.gpx.bean.GpxType;
import org.geotools.gpx.bean.RteType;
import org.geotools.gpx.bean.TrkType;
import org.geotools.gpx.bean.WptType;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.xml.Parser;
import org.opengis.feature.simple.SimpleFeatureType;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;


public class GpxDataStore extends AbstractDataStore {
    /*
     * org.geotools.feature.simple.SimpleFeatureTypeBuilder
     * org.geotools.feature.SimpleFeatureType
     * org.opengis.feature.simple.SimpleFeatureType
     */
    public static final String TYPE_NAME_POINT = "waypoint";
    public static final String TYPE_NAME_TRACK = "track";
    public static final String TYPE_NAME_ROUTE = "route";
    
    private final URL url;
    private final String namespace;
    private final GpxType gpxData;
    private SimpleFeatureType pointType;
    private SimpleFeatureType trackType;
    private SimpleFeatureType routeType;

    public GpxDataStore(URL url, String namespace) throws IOException, SAXException, ParserConfigurationException, URISyntaxException {
        super(false);  //   we are an r/o data store

        
        // decode the URL, if it contains %xx notation
        String filename = null;

        if (url == null) {
            throw new NullPointerException("Null URL for GpxDataStore");
        }

        try {
            filename = URLDecoder.decode(url.getFile(), "UTF-8");
        } catch (java.io.UnsupportedEncodingException use) {
            throw new MalformedURLException("Unable to decode " + url + " cause " + use.getMessage());
        }

        this.url = new URL(url.getProtocol(), url.getHost(), url.getPort(), filename);
        
        // specify namespace. If not supplied, filename used by default
        if(namespace == null) {
            int slashIndex = filename.lastIndexOf('/');
            if(slashIndex == -1)
                namespace = filename;
            else
                namespace = filename.substring(slashIndex);
        }
            
        this.namespace = namespace;
        
        // inint and load
        buildFeatureTypes();
        gpxData = loadGpx();
    }
    private void init() throws URISyntaxException {
        FeatureTypeBuilder ftb = FeatureTypeBuilder.newInstance(TYPE_NAME_POINT);
        ftb.addType(AttributeTypeFactory.newAttributeType("geometry", Point.class, true, 0, null,
                DefaultGeographicCRS.WGS84));
        ftb.addType(AttributeTypeFactory.newAttributeType("name", String.class, true, -1, null));
        ftb.addType(AttributeTypeFactory.newAttributeType("description", String.class, true, -1,
                null));
        ftb.addType(AttributeTypeFactory.newAttributeType("comment", String.class, true, -1, null));
        ftb.setNamespace(new URI(namespace));

        try {
            pointType = ftb.getFeatureType(); // TODO simpleFeatureType ??
        } catch (SchemaException e) {
            throw new RuntimeException(e);
        }

        ftb = FeatureTypeBuilder.newInstance(TYPE_NAME_TRACK);
        ftb.addType(AttributeTypeFactory.newAttributeType("geometry", MultiLineString.class, true,
                0, null, DefaultGeographicCRS.WGS84));
        ftb.addType(AttributeTypeFactory.newAttributeType("name", String.class, true, -1, null));
        ftb.addType(AttributeTypeFactory.newAttributeType("description", String.class, true, -1,
                null));
        ftb.addType(AttributeTypeFactory.newAttributeType("comment", String.class, true, -1, null));
        ftb.setNamespace(new URI(namespace));

        try {
            trackType = ftb.getFeatureType(); // TODO simpleFeatureType ??
        } catch (SchemaException e) {
            throw new RuntimeException(e);
        }
        
        ftb = FeatureTypeBuilder.newInstance(TYPE_NAME_ROUTE);
        ftb.addType(AttributeTypeFactory.newAttributeType("geometry", MultiLineString.class, true,
                0, null, DefaultGeographicCRS.WGS84));
        ftb.addType(AttributeTypeFactory.newAttributeType("name", String.class, true, -1, null));
        ftb.addType(AttributeTypeFactory.newAttributeType("description", String.class, true, -1,
                null));
        ftb.addType(AttributeTypeFactory.newAttributeType("comment", String.class, true, -1, null));
        ftb.setNamespace(new URI(namespace));

        try {
            routeType = ftb.getFeatureType(); // TODO simpleFeatureType ??
        } catch (SchemaException e) {
            throw new RuntimeException(e);
        }
    }

    private void buildFeatureTypes() throws URISyntaxException {
        if(true) {
            // The init() method does the same as this, but using the old style FeatureType generation.
            // which generates compatibel classes...
            init();
            return;
        }
            
        SimpleFeatureTypeBuilder ftb = new SimpleFeatureTypeBuilder();
        
        /*
    protected double lat;
    protected double lon;
    protected double ele;
    protected Calendar time;

    protected String name;
    protected String desc;
    protected String cmt;

    protected double magvar;
    protected double geoidheight;
    protected String src;
    protected List<LinkType> link;
    protected String sym;
    protected String type;
    protected String fix;
    protected int sat;
    protected double hdop;
    protected double vdop;
    protected double pdop;
    protected double ageofdgpsdata;
    protected int dgpsid;
    protected ExtensionsType extensions;
         */
        ftb.setName(TYPE_NAME_POINT);
        ftb.add("geometry", Point.class, DefaultGeographicCRS.WGS84);
        ftb.add("name", String.class);
        ftb.add("description", String.class);
        ftb.add("comment", String.class);
        ftb.setNamespaceURI(namespace);

        pointType = ftb.buildFeatureType();

        /*
    protected List<TrksegType> trkseg;

    protected String name;
    protected String desc;
    protected String cmt;

    protected String src;
    protected List<LinkType> link;
    protected int number;
    protected String type;
    protected ExtensionsType extensions;
        */
        ftb.setName(TYPE_NAME_TRACK);
        ftb.add("geometry", MultiLineString.class, DefaultGeographicCRS.WGS84);
        ftb.add("name", String.class);
        ftb.add("description", String.class);
        ftb.add("comment", String.class);
        ftb.setNamespaceURI(namespace);

        trackType = ftb.buildFeatureType();
        
        /*
    protected List<WptType> rtept;

    protected String name;
    protected String desc;
    protected String cmt;

    protected String src;
    protected List<LinkType> link;
    protected int number;
    protected String type;
    protected ExtensionsType extensions;
        */
        ftb.setName(TYPE_NAME_ROUTE);
        ftb.add("geometry", LineString.class, DefaultGeographicCRS.WGS84);
        ftb.add("name", String.class);
        ftb.add("description", String.class);
        ftb.add("comment", String.class);
        ftb.setNamespaceURI(namespace);

        routeType = ftb.buildFeatureType();
    }

    private GpxType loadGpx() throws IOException, SAXException, ParserConfigurationException {
        
        // build a parser // can it be a singleton parser?
        GPXConfiguration configuration = new GPXConfiguration();
        Parser parser = new Parser(configuration);
        
        // TODO: locking!!!
        
        InputStream in = null;
        GpxType gpx;

        try {
            in = url.openStream();
            gpx = (GpxType) parser.parse(in);
        } finally {
            if( in != null )
                in.close();
        }
        
        // we have to check the fields, that we're going to use as ID.
        // ther is no constraint in the format, that it should be unique,
        // so we have to check for it, and modify if needed.
        
        // 1. - waypoints. ID: name property
        Set<String> wptIds = new TreeSet<String>();
        Iterator<WptType> wpts = gpx.getWpt().iterator();
        while (wpts.hasNext()) {
            WptType wpt = wpts.next();
            
            String id = wpt.getName();
            if(wptIds.contains(id)) {
                id = extendId(wptIds, id);
                wpt.setName(id);
            }
            wptIds.add(id);
        }

        // 2. - tracks. ID: name property
        Set<String> trkIds = new TreeSet<String>();
        Iterator<TrkType> trks = gpx.getTrk().iterator();
        while (trks.hasNext()) {
            TrkType trk = trks.next();
            
            String id = trk.getName();
            if(wptIds.contains(id)) {
                id = extendId(trkIds, id);
                trk.setName(id);
            }
            trkIds.add(id);
        }
        
        // 3. - routes. ID: name property
        Set<String> rteIds = new TreeSet<String>();
        Iterator<RteType> rtes = gpx.getRte().iterator();
        while (rtes.hasNext()) {
            RteType rte = rtes.next();
            
            String id = rte.getName();
            if(rteIds.contains(id)) {
                id = extendId(rteIds, id);
                rte.setName(id);
            }
            rteIds.add(id);
        }
        
        return gpx;
    }
    
    private String extendId(Set<String> ids, String base) {
        String id = base;
        int suffix = 1;
        while(ids.contains(id)) {
            id = base + "_" + suffix;
        }
        return id;
    }

    @Override
    protected FeatureReader getFeatureReader(String typeName) {
        if (TYPE_NAME_POINT.equals(typeName)) {
            return new GpxFeatureReader(this, TYPE_NAME_POINT);
        } else if (TYPE_NAME_TRACK.equals(typeName)) {
            return new GpxFeatureReader(this, TYPE_NAME_TRACK);
        } else if (TYPE_NAME_ROUTE.equals(typeName)) {
            return new GpxFeatureReader(this, TYPE_NAME_ROUTE);
        } else {
            throw new IllegalArgumentException("No such type: " + typeName);
        }
    }

    @Override
    public FeatureType getSchema(String typeName) {
        if (TYPE_NAME_POINT.equals(typeName)) {
            return (FeatureType) pointType;
        } else if (TYPE_NAME_TRACK.equals(typeName)) {
            return (FeatureType) trackType;
        } else if (TYPE_NAME_ROUTE.equals(typeName)) {
            return (FeatureType) routeType;
        } else {
            throw new IllegalArgumentException("No such type: " + typeName);
        }
    }

    @Override
    public String[] getTypeNames() throws IOException {
        return new String[] { TYPE_NAME_POINT, TYPE_NAME_TRACK, TYPE_NAME_ROUTE };
    }
    
    GpxType getGpxData() {
        return gpxData;
    }
}
