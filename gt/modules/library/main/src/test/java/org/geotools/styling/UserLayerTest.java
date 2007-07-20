/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.styling;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.geotools.data.DataStore;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.data.memory.MemoryFeatureCollection;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.factory.Hints;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypes;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.type.GeometricAttributeType;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

public class UserLayerTest extends TestCase {

    private static final String CRS_WKT = "GEOGCS[\"WGS 84\", "
            + "  DATUM[\"WGS_1984\","
            + "    SPHEROID[\"WGS 84\", 6378137.0, 298.257223563, AUTHORITY[\"EPSG\",\"7030\"]],"
            + "    AUTHORITY[\"EPSG\",\"6326\"]],"
            + "  PRIMEM[\"Greenwich\", 0.0, AUTHORITY[\"EPSG\",\"8901\"]],"
            + "  UNIT[\"degree\", 0.017453292519943295],"
            + "  AXIS[\"Lon\", EAST]," + "  AXIS[\"Lat\", NORTH],"
            + "  AUTHORITY[\"EPSG\",\"4326\"]]";

    private static final int SRID = 4326;

    private static final String LAYER_NAME = "user-layer-1";

    private static final String ID_COLUMN = "id";

    private static final String GEOMETRY_COLUMN = "geometry";

    private static final String LABEL_COLUMN = "label";

    private static final String ID_1 = "point-1";

    private static final double X_1 = -180.0;

    private static final double Y_1 = -90.0;

    private static final String LABEL_1 = "point location #1";

    private static final String ID_2 = "point-2";

    private static final double X_2 = +180.0;

    private static final double Y_2 = +90.0;

    private static final String LABEL_2 = "point location #2";

    private static final double OPACITY = 0.75;

    private static final String IMAGE_EXT = "jpg";

    private static final String IMAGE_URL = "file:/somewhere/image."
            + IMAGE_EXT;

    private static final String MY_NAMESPACE = "integeo";

    private static final URI MY_URI;

    private static final String MY_FEATURE = "myFeature";
    static {
        try {
            MY_URI = new URI("http://geotools.org");
        } catch (URISyntaxException x) {
            throw new ExceptionInInitializerError(x);
        }
    }

    public void testUserLayerWithInlineFeatures() throws Exception {
        // create the feature's schema ----------------------------------------
        final CoordinateReferenceSystem crs = CRS.parseWKT(CRS_WKT);
        final AttributeType id = AttributeTypeFactory.newAttributeType(
                ID_COLUMN, Integer.class);
        final GeometryAttributeType geom = new GeometricAttributeType(
                GEOMETRY_COLUMN, Point.class, false, null, crs, null);
        final AttributeType label = AttributeTypeFactory.newAttributeType(
                LABEL_COLUMN, String.class);
        final AttributeType[] attributes = new AttributeType[] { id, geom,
                label };
        final FeatureType schema = FeatureTypes.newFeatureType(attributes,
                MY_FEATURE, MY_URI, false, null, geom);

        // create a feature collection ----------------------------------------
        final FeatureCollection fc = new MemoryFeatureCollection(schema);

        // populate the collection --------------------------------------------
        final PrecisionModel pm = new PrecisionModel(PrecisionModel.FLOATING);
        final GeometryFactory jtsFactory = new GeometryFactory(pm, SRID);

        // create 1st point
        final Point g1 = jtsFactory.createPoint(new Coordinate(X_1, Y_1));
        fc.add(schema
                .create(new Object[] { new Integer(1), g1, LABEL_1 }, ID_1));

        // create 2nd point
        final Point g2 = jtsFactory.createPoint(new Coordinate(X_2, Y_2));
        fc.add(schema
                .create(new Object[] { new Integer(2), g2, LABEL_2 }, ID_2));

        final DataStore ds = new MemoryDataStore(fc);

        // create and populate the layer --------------------------------------
        final StyleFactory sf = CommonFactoryFinder.getStyleFactory(GeoTools.getDefaultHints());

        final UserLayer layer = sf.createUserLayer();
        layer.setName(LAYER_NAME);
        layer.setInlineFeatureType(schema);
        layer.setInlineFeatureDatastore(ds);

        // create a user style and add it to that layer -----------------------
        final Style style = sf.createStyle();

        final StyleBuilder sb = new StyleBuilder(sf);
        final ExternalGraphic overlay = sb.createExternalGraphic(IMAGE_URL,
                "image/" + IMAGE_EXT);
        final Graphic g = sb.createGraphic(overlay, null, null, OPACITY,
                Double.NaN, 0.0);
        final PointSymbolizer ps = sb.createPointSymbolizer(g);
        final FeatureTypeStyle fts = sb.createFeatureTypeStyle(ps);
        fts.setFeatureTypeName(MY_NAMESPACE + ":" + MY_FEATURE);

        style.addFeatureTypeStyle(fts);
        layer.addUserStyle(style);

        // create an SLD and populate it with that styled layer ---------------
        final StyledLayerDescriptor sld1 = sf.createStyledLayerDescriptor();
        sld1.addStyledLayer(layer);

        // marshal the SLD to XML ---------------------------------------------
        final Map nsMap = new HashMap();
        nsMap.put(MY_URI, MY_NAMESPACE);
        final SLDTransformer sldTransformer = new SLDTransformer(nsMap);
        sldTransformer.setIndentation(2);
        String xml = sldTransformer.transform(sld1);

        // unmarshal it back to an SLD instance -------------------------------
        final InputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
        final SLDParser parser = new SLDParser(sf);
        parser.setInput(is);
        final StyledLayerDescriptor sld2 = parser.parseSLD();
        xml = sldTransformer.transform(sld2);

        // check both SLDs ----------------------------------------------------
        final StyledLayer[] layers = sld2.getStyledLayers();
        assertNotNull("Styled layers array MUST NOT be null", layers);
        assertEquals("Styled layers array MUST be 1-element long", 1,
                layers.length);
        final StyledLayer sLayer = layers[0];
        assertNotNull("Single styled layer MUST NOT be null", sLayer);
        assertTrue("Single layer MUST be a UserLayer", UserLayer.class
                .isAssignableFrom(sLayer.getClass()));
        final UserLayer uLayer = (UserLayer) sLayer;
        final String lName = uLayer.getName();
        assertEquals("Read layer name MUST match", LAYER_NAME, lName);
        final FeatureType ft = uLayer.getInlineFeatureType();
        assertNotNull("Unmarshalled feature type MUST NOT be null", ft);
        final String fName = ft.getTypeName();
        assertEquals("Read feature type name MUST match", MY_FEATURE, fName);
        assertEquals(CRS.decode("EPSG:4326"), ft.getPrimaryGeometry().getCoordinateSystem());
    }
}
