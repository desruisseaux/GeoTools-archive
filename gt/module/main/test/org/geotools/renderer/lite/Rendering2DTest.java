/*
 * StylingTest.java JUnit based test Created on April 12, 2002, 1:18 PM
 */

package org.geotools.renderer.lite;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureResults;
import org.geotools.data.Query;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypes;
import org.geotools.filter.AbstractFilter;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.IllegalFilterException;
import org.geotools.geometry.JTS;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.crs.DefaultProjectedCRS;
import org.geotools.referencing.wkt.Parser;
import org.geotools.renderer.GTRenderer;
import org.geotools.resources.TestData;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.SLDParser;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.StyledLayerDescriptor;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.UserLayer;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequenceFactory;

/**
 * @author jamesm
 */
public class Rendering2DTest extends TestCase {
    /** path for test data */
    // private java.net.URL base = getClass().getResource("/testData/");
    /**
     * The logger for the rendering module.
     */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.rendering");
    public static boolean INTERACTIVE=false;
    private static final FilterFactory filterFactory = FilterFactory.createFilterFactory();
    static final String LINE = "linefeature";
    static final String POLYGON = "polygonfeature";
    static final String POINT = "pointfeature";
    static final String RING = "ringfeature";
    static final String COLLECTION = "collfeature";
    static final Map rendererHints=new HashMap();
    {
        rendererHints.put("optimizedDataLoadingEnabled", new Boolean(true));
    }

    public Rendering2DTest( java.lang.String testName ) {
        super(testName);
    }

    public static void main( java.lang.String[] args ) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(Rendering2DTest.class);
        return suite;
    }
     
    Style loadTestStyle() throws IOException {
        StyleFactory factory = StyleFactory.createStyleFactory();

        java.net.URL surl = TestData.getResource(this, "test-sld.xml");
        SLDParser stylereader = new SLDParser(factory, surl);
        StyledLayerDescriptor sld = stylereader.parseSLD();

        UserLayer layer = (UserLayer)sld.getStyledLayers()[0];

        Style style = layer.getUserStyles()[0];
        return style;
    }
    
    Style createTestStyle() throws IllegalFilterException {
        StyleFactory sFac = StyleFactory.createStyleFactory();
        // The following is complex, and should be built from
        // an SLD document and not by hand
        PointSymbolizer pointsym = sFac.createPointSymbolizer();
        pointsym.setGraphic(sFac.getDefaultGraphic());

        LineSymbolizer linesym = sFac.createLineSymbolizer();
        Stroke myStroke = sFac.getDefaultStroke();
        myStroke.setColor(filterFactory.createLiteralExpression("#0000ff"));
        myStroke.setWidth(filterFactory.createLiteralExpression(new Integer(5)));
        LOGGER.info("got new Stroke " + myStroke);
        linesym.setStroke(myStroke);

        PolygonSymbolizer polysym = sFac.createPolygonSymbolizer();
        Fill myFill = sFac.getDefaultFill();
        myFill.setColor(filterFactory.createLiteralExpression("#ff0000"));
        polysym.setFill(myFill);
        myStroke = sFac.getDefaultStroke();
        myStroke.setColor(filterFactory.createLiteralExpression("#0000ff"));
        myStroke.setWidth(filterFactory.createLiteralExpression(new Integer(2)));
        polysym.setStroke(myStroke);
        Rule rule = sFac.createRule();
        rule.setSymbolizers(new Symbolizer[]{polysym});
        FeatureTypeStyle fts = sFac.createFeatureTypeStyle(new Rule[]{rule});
        fts.setFeatureTypeName("polygonfeature");

        Rule rule2 = sFac.createRule();
        rule2.setSymbolizers(new Symbolizer[]{linesym});
        FeatureTypeStyle fts2 = sFac.createFeatureTypeStyle();
        fts2.setRules(new Rule[]{rule2});
        fts2.setFeatureTypeName("linefeature");

        Rule rule3 = sFac.createRule();
        rule3.setSymbolizers(new Symbolizer[]{pointsym});
        FeatureTypeStyle fts3 = sFac.createFeatureTypeStyle();
        fts3.setRules(new Rule[]{rule3});
        fts3.setFeatureTypeName("pointfeature");

        Rule rule4 = sFac.createRule();
        rule4.setSymbolizers(new Symbolizer[]{polysym, linesym});
        FeatureTypeStyle fts4 = sFac.createFeatureTypeStyle();
        fts4.setRules(new Rule[]{rule4});
        fts4.setFeatureTypeName("collFeature");

        Rule rule5 = sFac.createRule();
        rule5.setSymbolizers(new Symbolizer[]{linesym});
        FeatureTypeStyle fts5 = sFac.createFeatureTypeStyle();
        fts5.setRules(new Rule[]{rule5});
        fts5.setFeatureTypeName("ringFeature");

        Style style = sFac.createStyle();
        style.setFeatureTypeStyles(new FeatureTypeStyle[]{fts, fts2, fts3, fts4, fts5});

        return style;
    }

    FeatureCollection createTestFeatureCollection( CoordinateReferenceSystem crs, String typeName  ) throws Exception {
        GeometryFactory geomFac = new GeometryFactory();
        return createTestFeatureCollection(crs, geomFac, typeName);
    }
    
    FeatureCollection createTestFeatureCollection( CoordinateReferenceSystem crs, GeometryFactory geomFac, String typeName ) throws Exception {
        // Request extent
        // Envelope ex = new Envelope(5, 15, 5, 15);

        AttributeType[] types = new AttributeType[2];

        LineString line = makeSampleLineString(geomFac);
        if (crs != null)
            types[0] = AttributeTypeFactory.newAttributeType("collection", line.getClass(), false, 0,
                    null, crs);
        else
            types[0] = AttributeTypeFactory.newAttributeType("centerline", line.getClass());
        types[1] = AttributeTypeFactory.newAttributeType("name", String.class);
        FeatureType lineType = FeatureTypes.newFeatureType(types, LINE);
        Feature lineFeature = lineType.create(new Object[]{line, "centerline"});

        Polygon polygon = makeSamplePolygon(geomFac);

        if (crs != null)
            types[0] = AttributeTypeFactory.newAttributeType("collection", polygon.getClass(), false,
                    0, null, crs);
        else
            types[0] = AttributeTypeFactory.newAttributeType("edge", polygon.getClass());
        types[1] = AttributeTypeFactory.newAttributeType("name", String.class);
        FeatureType polygonType = FeatureTypes.newFeatureType(types, POLYGON);

        Feature polygonFeature = polygonType.create(new Object[]{polygon, "edge"});

        Point point = makeSamplePoint(geomFac);
        if (crs != null)
            types[0] = AttributeTypeFactory.newAttributeType("collection", point.getClass(), false, 0,
                    null, crs);
        else
            types[0] = AttributeTypeFactory.newAttributeType("centre", point.getClass());
        types[1] = AttributeTypeFactory.newAttributeType("name", String.class);
        FeatureType pointType = FeatureTypes.newFeatureType(types, POINT);

        Feature pointFeature = pointType.create(new Object[]{point, "centre"});

        LinearRing ring = makeSampleLinearRing(geomFac);
        if (crs != null)
            types[0] = AttributeTypeFactory.newAttributeType("collection", line.getClass(), false, 0,
                    null, crs);
        else
            types[0] = AttributeTypeFactory.newAttributeType("centerline", line.getClass());
        types[1] = AttributeTypeFactory.newAttributeType("name", String.class);
        FeatureType lrType = FeatureTypes.newFeatureType(types, RING);
        Feature ringFeature = lrType.create(new Object[]{ring, "centerline"});

        GeometryCollection coll = makeSampleGeometryCollection(geomFac);
        if (crs != null)
            types[0] = AttributeTypeFactory.newAttributeType("collection", coll.getClass(), false, 0,
                    null, crs);
        else
            types[0] = AttributeTypeFactory.newAttributeType("collection", coll.getClass());
        types[1] = AttributeTypeFactory.newAttributeType("name", String.class);
        FeatureType collType = FeatureTypes.newFeatureType(types, COLLECTION);
        Feature collFeature = collType.create(new Object[]{coll, "collection"});

        MemoryDataStore data = new MemoryDataStore();
        data.addFeature(lineFeature);
        data.addFeature(polygonFeature);
        data.addFeature(pointFeature);
        data.addFeature(ringFeature);
        data.addFeature(collFeature);

        return data.getFeatureSource(typeName).getFeatures().collection();
    }

    public void testSimpleRender() throws Exception {

        // same as the datasource test, load in some features into a table
        System.err.println("starting rendering2DTest");

        FeatureCollection ft = createTestFeatureCollection(null, POLYGON);
        Style style = createTestStyle();

        MapContext map = new DefaultMapContext();
        map.addLayer(ft, style);
        StreamingRenderer renderer=new StreamingRenderer();
        renderer.setContext(map);
        renderer.setRendererHints(rendererHints);
        Envelope env = map.getLayerBounds();
        env = new Envelope(env.getMinX() - 20, env.getMaxX() + 20, env.getMinY() - 20, env
                .getMaxY() + 20);
        showRender("testSimpleRender", renderer, 1000, env);

    }
    
//    public void testRenderLoadedStyle() throws Exception {
//
//        // same as the datasource test, load in some features into a table
//        System.err.println("starting RenderLoadedStyle");
//
//        FeatureCollection ft = createTestFeatureCollection(null, POLYGON);
//        Style style = loadTestStyle();
//
//        MapContext map = new DefaultMapContext();
//        map.addLayer(ft, style);
//        LiteRenderer2 renderer = new LiteRenderer2(map);
//        Envelope env = map.getLayerBounds();
//        env = new Envelope(env.getMinX() - 20, env.getMaxX() + 20, env.getMinY() - 20, env
//                .getMaxY() + 20);
//        showRender("RenderLoadedStyle", renderer, 5000, env);
//
//    }

    public void testSimpleLineRender() throws Exception {

        // same as the datasource test, load in some features into a table
        System.err.println("starting rendering2DTest");

        FeatureCollection ft = createTestFeatureCollection(null, LINE);
        Style style = createTestStyle();

        MapContext map = new DefaultMapContext();
        map.addLayer(ft, style);
        StreamingRenderer renderer = new StreamingRenderer();
        renderer.setContext(map);
        renderer.setRendererHints(rendererHints);

        Envelope env = map.getLayerBounds();
        env = new Envelope(env.getMinX() - 20, env.getMaxX() + 20, env.getMinY() - 20, env
                .getMaxY() + 20);
        showRender("testSimpleLineRender", renderer, 1000, env);

    }

    public void testSimplePointRender() throws Exception {

        // same as the datasource test, load in some features into a table
        System.err.println("starting rendering2DTest");

        FeatureCollection ft = createTestFeatureCollection(null, POINT);
        Style style = createTestStyle();

        MapContext map = new DefaultMapContext();
        map.addLayer(ft, style);
        StreamingRenderer renderer = new StreamingRenderer();
        renderer.setContext(map);
        renderer.setRendererHints(rendererHints);

        Envelope env = map.getLayerBounds();
        env = new Envelope(env.getMinX() - 20, env.getMaxX() + 20, env.getMinY() - 20, env
                .getMaxY() + 20);
        showRender("testSimplePointRender", renderer, 1000, env);

    }

    public void testReprojectionWithPackedCoordinateSequence() throws Exception {

        // same as the datasource test, load in some features into a table
        System.err.println("starting testLiteRender2");
        GeometryFactory geomFac = new GeometryFactory(
                PackedCoordinateSequenceFactory.DOUBLE_FACTORY);
        FeatureCollection ft = createTestFeatureCollection(DefaultGeographicCRS.WGS84, geomFac, POLYGON);
        Style style = createTestStyle();

        StringBuffer stringBuffer = new StringBuffer();
        for( FeatureIterator reader = ft.features(); reader.hasNext(); ) {
            Coordinate[] coords = reader.next().getDefaultGeometry().getCoordinates();
            for( int i = 0; i < coords.length; i++ ) {
                stringBuffer.append(coords[i]);
            }
        }

        System.out.println(stringBuffer);

        MapContext map = new DefaultMapContext();
        map.addLayer(ft, style);
        final BufferedImage image = new BufferedImage(400, 400, BufferedImage.TYPE_4BYTE_ABGR);
        StreamingRenderer renderer = new StreamingRenderer();
        renderer.setContext(map);
        renderer.setRendererHints(rendererHints);

        CoordinateReferenceSystem crs = FactoryFinder.getCRSFactory(null).createFromWKT(
                        "PROJCS[\"NAD_1983_UTM_Zone_10N\",GEOGCS[\"GCS_North_American_1983\",DATUM[\"D_North_American_1983\",TOWGS84[0,0,0,0,0,0,0],SPHEROID[\"GRS_1980\",6378137,298.257222101]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.017453292519943295]],PROJECTION[\"Transverse_Mercator\"],PARAMETER[\"False_Easting\",500000],PARAMETER[\"False_Northing\",0],PARAMETER[\"Central_Meridian\",-123],PARAMETER[\"Scale_Factor\",0.9996],PARAMETER[\"Latitude_Of_Origin\",0],UNIT[\"Meter\",1]]");

        MathTransform t = FactoryFinder.getCoordinateOperationFactory(null).createOperation(
                DefaultGeographicCRS.WGS84, crs).getMathTransform();

        Envelope env = map.getLayerBounds();

        Envelope bounds = JTS.transform(env, t);
        map.setAreaOfInterest(bounds, crs);

        Rectangle rect = new Rectangle(400, 400);
//        renderer.setOptimizedDataLoadingEnabled(true);

        env = new Envelope(bounds.getMinX() - 2000000, bounds.getMaxX() + 2000000,
                bounds.getMinY() - 2000000, bounds.getMaxY() + 2000000);
        showRender("testReprojection", renderer, 1000, env);

        // System.in.read();

    }

    public void testReprojectionWithNonPackedCoordinateSequence() throws Exception {

        // same as the datasource test, load in some features into a table
        System.err.println("starting testLiteRender2");
        
        FeatureCollection ft = createTestFeatureCollection(DefaultGeographicCRS.WGS84, POLYGON);
        Style style = createTestStyle();

        StringBuffer stringBuffer = new StringBuffer();
        for( FeatureIterator reader = ft.features(); reader.hasNext(); ) {
            Coordinate[] coords = reader.next().getDefaultGeometry().getCoordinates();
            for( int i = 0; i < coords.length; i++ ) {
                stringBuffer.append(coords[i]);
            }
        }

        System.out.println(stringBuffer);

        MapContext map = new DefaultMapContext();
        map.addLayer(ft, style);
        final BufferedImage image = new BufferedImage(400, 400, BufferedImage.TYPE_4BYTE_ABGR);
        StreamingRenderer renderer = new StreamingRenderer();
        renderer.setContext(map);
        renderer.setRendererHints(rendererHints);


        CoordinateReferenceSystem crs = FactoryFinder.getCRSFactory(null).createFromWKT(
                        "PROJCS[\"NAD_1983_UTM_Zone_10N\",GEOGCS[\"GCS_North_American_1983\",DATUM[\"D_North_American_1983\",TOWGS84[0,0,0,0,0,0,0],SPHEROID[\"GRS_1980\",6378137,298.257222101]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.017453292519943295]],PROJECTION[\"Transverse_Mercator\"],PARAMETER[\"False_Easting\",500000],PARAMETER[\"False_Northing\",0],PARAMETER[\"Central_Meridian\",-123],PARAMETER[\"Scale_Factor\",0.9996],PARAMETER[\"Latitude_Of_Origin\",0],UNIT[\"Meter\",1]]");

        MathTransform t = FactoryFinder.getCoordinateOperationFactory(null).createOperation(
                DefaultGeographicCRS.WGS84, crs).getMathTransform();

        Envelope env = map.getLayerBounds();

        Envelope bounds = JTS.transform(env, t);
        map.setAreaOfInterest(bounds, crs);

        Rectangle rect = new Rectangle(400, 400);
//        renderer.setOptimizedDataLoadingEnabled(true);

        env = new Envelope(bounds.getMinX() - 2000000, bounds.getMaxX() + 2000000,
                bounds.getMinY() - 2000000, bounds.getMaxY() + 2000000);
        showRender("testReprojection", renderer, 1000, env);

        // System.in.read();

    }
    
    public void testLineReprojection() throws Exception {

        // same as the datasource test, load in some features into a table
        System.err.println("starting testLiteRender2");
        GeometryFactory geomFac = new GeometryFactory(
                PackedCoordinateSequenceFactory.DOUBLE_FACTORY);
        FeatureCollection ft = createTestFeatureCollection(DefaultGeographicCRS.WGS84, geomFac, LINE);
        Style style = createTestStyle();

        StringBuffer stringBuffer = new StringBuffer();
        for( FeatureIterator reader = ft.features(); reader.hasNext(); ) {
            Coordinate[] coords = reader.next().getDefaultGeometry().getCoordinates();
            for( int i = 0; i < coords.length; i++ ) {
                stringBuffer.append(coords[i]);
            }
        }

        System.out.println(stringBuffer);

        MapContext map = new DefaultMapContext();
        map.addLayer(ft, style);
        final BufferedImage image = new BufferedImage(400, 400, BufferedImage.TYPE_4BYTE_ABGR);
        StreamingRenderer renderer = new StreamingRenderer();
        renderer.setContext(map);
        renderer.setRendererHints(rendererHints);

        
        Envelope env = map.getLayerBounds();
        env = new Envelope(env.getMinX() - 20, env.getMaxX() + 20, env.getMinY() - 20, env
                .getMaxY() + 20);
        showRender("testSimpleLineRender", renderer, 1000, env);
        
        CoordinateReferenceSystem crs = FactoryFinder.getCRSFactory(null).createFromWKT(
                        "PROJCS[\"NAD_1983_UTM_Zone_10N\",GEOGCS[\"GCS_North_American_1983\",DATUM[\"D_North_American_1983\",TOWGS84[0,0,0,0,0,0,0],SPHEROID[\"GRS_1980\",6378137,298.257222101]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.017453292519943295]],PROJECTION[\"Transverse_Mercator\"],PARAMETER[\"False_Easting\",500000],PARAMETER[\"False_Northing\",0],PARAMETER[\"Central_Meridian\",-123],PARAMETER[\"Scale_Factor\",0.9996],PARAMETER[\"Latitude_Of_Origin\",0],UNIT[\"Meter\",1]]");

        MathTransform t = FactoryFinder.getCoordinateOperationFactory(null).createOperation(
                DefaultGeographicCRS.WGS84, crs).getMathTransform();

        env = map.getLayerBounds();

        Envelope bounds = JTS.transform(env, t);
        map.setAreaOfInterest(bounds, crs);

        Rectangle rect = new Rectangle(400, 400);
//        renderer.setOptimizedDataLoadingEnabled(true);

        env = new Envelope(bounds.getMinX() - 2000000, bounds.getMaxX() + 2000000,
                bounds.getMinY() - 2000000, bounds.getMaxY() + 2000000);
        showRender("testLineReprojection", renderer, 1000, env);

        // System.in.read();

    }
    public void testPointReprojection() throws Exception {

        // same as the datasource test, load in some features into a table
        System.err.println("starting testLiteRender2");
        GeometryFactory geomFac = new GeometryFactory(
                PackedCoordinateSequenceFactory.DOUBLE_FACTORY);
        FeatureCollection ft = createTestFeatureCollection(DefaultGeographicCRS.WGS84, geomFac, POINT);
        Style style = createTestStyle();

        StringBuffer stringBuffer = new StringBuffer();
        for( FeatureIterator reader = ft.features(); reader.hasNext(); ) {
            Coordinate[] coords = reader.next().getDefaultGeometry().getCoordinates();
            for( int i = 0; i < coords.length; i++ ) {
                stringBuffer.append(coords[i]);
            }
        }

        System.out.println(stringBuffer);

        MapContext map = new DefaultMapContext();
        map.addLayer(ft, style);
        final BufferedImage image = new BufferedImage(400, 400, BufferedImage.TYPE_4BYTE_ABGR);
        StreamingRenderer renderer = new StreamingRenderer();
        renderer.setContext(map);
        renderer.setRendererHints(rendererHints);

        CoordinateReferenceSystem crs = FactoryFinder.getCRSFactory(null).createFromWKT(
                        "PROJCS[\"NAD_1983_UTM_Zone_10N\",GEOGCS[\"GCS_North_American_1983\",DATUM[\"D_North_American_1983\",TOWGS84[0,0,0,0,0,0,0],SPHEROID[\"GRS_1980\",6378137,298.257222101]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.017453292519943295]],PROJECTION[\"Transverse_Mercator\"],PARAMETER[\"False_Easting\",500000],PARAMETER[\"False_Northing\",0],PARAMETER[\"Central_Meridian\",-123],PARAMETER[\"Scale_Factor\",0.9996],PARAMETER[\"Latitude_Of_Origin\",0],UNIT[\"Meter\",1]]");

        MathTransform t = FactoryFinder.getCoordinateOperationFactory(null).createOperation(
                DefaultGeographicCRS.WGS84, crs).getMathTransform();

        Envelope env = map.getLayerBounds();

        Envelope bounds = JTS.transform(env, t);
        map.setAreaOfInterest(bounds, crs);

        Rectangle rect = new Rectangle(400, 400);
//        renderer.setOptimizedDataLoadingEnabled(true);

        env = new Envelope(bounds.getMinX() - 2000000, bounds.getMaxX() + 2000000,
                bounds.getMinY() - 2000000, bounds.getMaxY() + 2000000);
        showRender("testPointReprojection", renderer, 1000, env);

        // System.in.read();

    }

    
    /**
     * Tests the layer definition query behavior as implemented by StreamingRenderer.
     * <p>
     * This method relies on the features created on createTestFeatureCollection()
     * </p>
     * 
     * @throws Exception
     */
    public void testDefinitionQuery() throws Exception {

        System.err.println("starting definition query test");
        final FeatureCollection ft = createTestDefQueryFeatureCollection();
        final Style style = createDefQueryTestStyle();
        FeatureResults results;
        Envelope envelope = ft.getBounds();

        // we'll use this as the definition query for the layer
        Query layerQuery;

        MapLayer layer = new DefaultMapLayer(ft, style);
        MapContext map = new DefaultMapContext(new MapLayer[]{layer});
        map.setAreaOfInterest(envelope);
        StreamingRenderer renderer = new StreamingRenderer();
        renderer.setContext(map);
        renderer.setRendererHints(rendererHints);


        // this is the reader that StreamingRenderer obtains after applying
        // the mixed filter to a given layer.
        FeatureReader reader;
        Filter filter = Filter.NONE;
        FilterFactory ffac = FilterFactory.createFilterFactory();

        // test maxFeatures, render just the first 2 features
        layerQuery = new DefaultQuery("querytest", filter, 2, null, "handle");
        layer.setQuery(layerQuery);

        results = renderer.queryLayer(layer, envelope, DefaultGeographicCRS.WGS84);
        assertEquals(2, results.getCount());
        // just the 3 geometric atts should get be loaded
        assertEquals(3, results.getSchema().getAttributeCount());

        showRender("testDefinitionQuery1", renderer, 1000, null);

        // test attribute based filter
        FeatureType schema = ft.features().next().getFeatureType();
        filter = ffac.createCompareFilter(AbstractFilter.COMPARE_EQUALS);
        ((CompareFilter) filter).addLeftValue(ffac.createAttributeExpression(schema, "id"));
        ((CompareFilter) filter).addRightValue(ffac.createLiteralExpression("ft1"));

        // note we include the "id" field in the layer query. Bad practice, since it goes
        // against
        // the performance gain of renderer.setOptimizedDataLoadingEnabled(true),
        // but we should test it anyway
        layerQuery = new DefaultQuery("querytest", filter, Integer.MAX_VALUE, new String[]{"id"},
                "handle");
        layer.setQuery(layerQuery);

        results = renderer.queryLayer(layer, envelope, DefaultGeographicCRS.WGS84);
        assertEquals(1, results.getCount());
        // the 4 atts should be loaded since the definition query includes "id"
        assertEquals(4, results.getSchema().getAttributeCount());
        // we can check this since we explicitly requested the "id" attribute. If we not,
        // it would be not loaded
        String val = (String) results.reader().next().getAttribute("id");
        assertEquals("ft1", val);

        showRender("testDefinitionQuery2", renderer, 1000, null);

        // try a bbox filter as definition query for the layer
        filter = null;
        GeometryFilter gfilter;
        // contains the first 2 features
        Envelope env = new Envelope(20, 130, 20, 130);
        gfilter = ffac.createGeometryFilter(AbstractFilter.GEOMETRY_BBOX);
        gfilter.addLeftGeometry(ffac.createAttributeExpression(schema, "point"));
        gfilter.addRightGeometry(ffac.createBBoxExpression(env));
        filter = gfilter;

        gfilter = ffac.createGeometryFilter(AbstractFilter.GEOMETRY_BBOX);
        gfilter.addLeftGeometry(ffac.createAttributeExpression(schema, "line"));
        gfilter.addRightGeometry(ffac.createBBoxExpression(env));
        filter = filter.or(gfilter);

        gfilter = ffac.createGeometryFilter(AbstractFilter.GEOMETRY_BBOX);
        gfilter.addLeftGeometry(ffac.createAttributeExpression(schema, "polygon"));
        gfilter.addRightGeometry(ffac.createBBoxExpression(env));
        filter = filter.or(gfilter);

        System.err.println("trying with filter: " + filter);

        layerQuery = new DefaultQuery("querytest", filter, Integer.MAX_VALUE, null, "handle");
        layer.setQuery(layerQuery);

        results = renderer.queryLayer(layer, envelope, DefaultGeographicCRS.WGS84);
        assertEquals(2, results.getCount());
        // the 4 atts should be loaded since the definition query includes "id"
        assertEquals(3, results.getSchema().getAttributeCount());

        showRender("testDefinitionQuery3", renderer, 1000, null);

    }

    /**
     * bounds may be null
     */
    static void showRender( String testName, Object renderer, long timeOut, Envelope bounds )
            throws Exception {

        int w = 300, h = 300;
        final BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.getGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, w, h);
        render(renderer, g, new Rectangle(w, h), bounds);
        if ( (System.getProperty("java.awt.headless") == null
                || !System.getProperty("java.awt.headless").equals("true"))
                && INTERACTIVE) {
            Frame frame = new Frame(testName);
            frame.addWindowListener(new WindowAdapter(){
                public void windowClosing( WindowEvent e ) {
                    e.getWindow().dispose();
                }
            });

            Panel p = new Panel(){
                /** <code>serialVersionUID</code> field */
                private static final long serialVersionUID = 1L;

                public void paint( Graphics g ) {
                    g.drawImage(image, 0, 0, this);
                }
            };
            frame.add(p);
            frame.setSize(w, h);
            frame.setVisible(true);

            Thread.sleep(timeOut);
            frame.dispose();
        }

        boolean hasData=false; //All I can seem to check reliably.

        for( int y=0; y<h;y++ ){
            for( int x=0; x<w;x++ ){
            	if( image.getRGB(x,y)!=-1 ){
            		hasData=true;
            	}
            }
        }
        assertTrue("image is blank and should not be", hasData);


    }

    /**
     * responsible for actually rendering.
     * 
     * @param g
     * @param bounds
     */
    private static void render( Object obj, Graphics g, Rectangle rect, Envelope bounds ) {
        if (obj instanceof GTRenderer) {
            GTRenderer renderer = (GTRenderer) obj;
            if (bounds == null)
                renderer.paint((Graphics2D) g, rect, new AffineTransform());
            else
                renderer.paint((Graphics2D) g, rect, RendererUtilities.worldToScreenTransform(bounds, rect));
        }
    }

    private FeatureCollection createTestDefQueryFeatureCollection() throws Exception {
        MemoryDataStore data = new MemoryDataStore();
        AttributeType[] types = new AttributeType[4];

        types[0] = AttributeTypeFactory.newAttributeType("id", String.class);
        types[1] = AttributeTypeFactory.newAttributeType("point", Point.class);
        types[2] = AttributeTypeFactory.newAttributeType("line", LineString.class);
        types[3] = AttributeTypeFactory.newAttributeType("polygon", Polygon.class);

        FeatureType type = FeatureTypes.newFeatureType(types, "querytest");

        GeometryFactory gf = new GeometryFactory();
        Feature f;
        LineString l;
        Polygon p;

        l = line(gf, new int[]{20, 20, 100, 20, 100, 100});
        p = (Polygon) l.convexHull();
        f = type.create(new Object[]{"ft1", point(gf, 20, 20), l, p}, "test.1");
        data.addFeature(f);

        l = line(gf, new int[]{130, 130, 110, 110, 110, 130, 30, 130});
        p = (Polygon) l.convexHull();
        f = type.create(new Object[]{"ft2", point(gf, 130, 130), l, p}, "test.2");
        data.addFeature(f);

        l = line(gf, new int[]{150, 150, 190, 140, 190, 190});
        p = (Polygon) l.convexHull();
        f = type.create(new Object[]{"ft3", point(gf, 150, 150), l, p}, "test.3");
        data.addFeature(f);

        String typeName = type.getTypeName();
        return data.getFeatureSource(typeName).getFeatures().collection();
    }

    private Style createDefQueryTestStyle() throws IllegalFilterException {
        StyleFactory sFac = StyleFactory.createStyleFactory();

        PointSymbolizer pointsym = sFac.createPointSymbolizer();
        pointsym.setGraphic(sFac.getDefaultGraphic());
        pointsym.setGeometryPropertyName("point");

        Rule rulep = sFac.createRule();
        rulep.setSymbolizers(new Symbolizer[]{pointsym});
        FeatureTypeStyle ftsP = sFac.createFeatureTypeStyle();
        ftsP.setRules(new Rule[]{rulep});
        ftsP.setFeatureTypeName("querytest");

        LineSymbolizer linesym = sFac.createLineSymbolizer();
        linesym.setGeometryPropertyName("line");

        Stroke myStroke = sFac.getDefaultStroke();
        myStroke.setColor(filterFactory.createLiteralExpression("#0000ff"));
        myStroke.setWidth(filterFactory.createLiteralExpression(new Integer(3)));
        LOGGER.info("got new Stroke " + myStroke);
        linesym.setStroke(myStroke);

        Rule rule2 = sFac.createRule();
        rule2.setSymbolizers(new Symbolizer[]{linesym});
        FeatureTypeStyle ftsL = sFac.createFeatureTypeStyle();
        ftsL.setRules(new Rule[]{rule2});
        ftsL.setFeatureTypeName("querytest");

        PolygonSymbolizer polysym = sFac.createPolygonSymbolizer();
        polysym.setGeometryPropertyName("polygon");
        Fill myFill = sFac.getDefaultFill();
        myFill.setColor(filterFactory.createLiteralExpression("#ff0000"));
        polysym.setFill(myFill);
        polysym.setStroke(sFac.getDefaultStroke());
        Rule rule = sFac.createRule();
        rule.setSymbolizers(new Symbolizer[]{polysym});
        FeatureTypeStyle ftsPoly = sFac.createFeatureTypeStyle(new Rule[]{rule});
        // ftsPoly.setRules(new Rule[]{rule});
        ftsPoly.setFeatureTypeName("querytest");

        Style style = sFac.createStyle();
        style.setFeatureTypeStyles(new FeatureTypeStyle[]{ftsPoly, ftsL, ftsP});

        return style;
    }

    public LineString line( final GeometryFactory gf, int[] xy ) {
        Coordinate[] coords = new Coordinate[xy.length / 2];

        for( int i = 0; i < xy.length; i += 2 ) {
            coords[i / 2] = new Coordinate(xy[i], xy[i + 1]);
        }

        return gf.createLineString(coords);
    }

    private int xCenter = -123, yCenter = 30;

    public Point point( final GeometryFactory gf, int x, int y ) {
        Coordinate coord = new Coordinate(x, y);
        return gf.createPoint(coord);
    }

    private Point makeSamplePoint( final GeometryFactory geomFac ) {
        Coordinate c = new Coordinate(xCenter - 14.0d, yCenter - 14.0d);
        Point point = geomFac.createPoint(c);
        return point;
    }

    private LineString makeSampleLineString( final GeometryFactory geomFac ) {
        Coordinate[] linestringCoordinates = new Coordinate[7];
        linestringCoordinates[0] = new Coordinate(xCenter - 5.0d, yCenter - 5.0d);
        linestringCoordinates[1] = new Coordinate(xCenter - 6.0d, yCenter - 5.0d);
        linestringCoordinates[2] = new Coordinate(xCenter - 6.0d, yCenter - 6.0d);
        linestringCoordinates[3] = new Coordinate(xCenter - 7.0d, yCenter - 6.0d);
        linestringCoordinates[4] = new Coordinate(xCenter - 7.0d, yCenter - 7.0d);
        linestringCoordinates[5] = new Coordinate(xCenter - 8.0d, yCenter - 7.0d);
        linestringCoordinates[6] = new Coordinate(xCenter - 8.0d, yCenter - 8.0d);
        LineString line = geomFac.createLineString(linestringCoordinates);

        return line;
    }

    private Polygon makeSamplePolygon( final GeometryFactory geomFac ) {
        Coordinate[] polygonCoordinates = new Coordinate[10];
        polygonCoordinates[0] = new Coordinate(xCenter - 7, yCenter - 7);
        polygonCoordinates[1] = new Coordinate(xCenter - 6, yCenter - 9);
        polygonCoordinates[2] = new Coordinate(xCenter - 6, yCenter - 11);
        polygonCoordinates[3] = new Coordinate(xCenter - 7, yCenter - 12);
        polygonCoordinates[4] = new Coordinate(xCenter - 9, yCenter - 11);
        polygonCoordinates[5] = new Coordinate(xCenter - 11, yCenter - 12);
        polygonCoordinates[6] = new Coordinate(xCenter - 13, yCenter - 11);
        polygonCoordinates[7] = new Coordinate(xCenter - 13, yCenter - 9);
        polygonCoordinates[8] = new Coordinate(xCenter - 11, yCenter - 7);
        polygonCoordinates[9] = new Coordinate(xCenter - 7, yCenter - 7);
        try {
            LinearRing ring = geomFac.createLinearRing(polygonCoordinates);
            Polygon polyg = geomFac.createPolygon(ring, null);
            return polyg;
        } catch (TopologyException te) {
            fail("Error creating sample polygon for testing " + te);
        }
        return null;
    }

    private GeometryCollection makeSampleGeometryCollection( final GeometryFactory geomFac ) {
        try {
            Geometry polyg = buildShiftedGeometry(makeSamplePolygon(geomFac), 50, 50);
            Geometry lineString = buildShiftedGeometry(makeSampleLineString(geomFac), 50, 50);
            return geomFac.createGeometryCollection(new Geometry[]{polyg, lineString});
        } catch (TopologyException te) {
            fail("Error creating sample polygon for testing " + te);
        }
        return null;
    }

    private LinearRing makeSampleLinearRing( final GeometryFactory geomFac ) {
        try {
            Polygon polyg = (Polygon) buildShiftedGeometry(makeSamplePolygon(geomFac), 0, 100);
            return (LinearRing) polyg.getExteriorRing();
        } catch (TopologyException te) {
            fail("Error creating sample polygon for testing " + te);
        }
        return null;
    }

    private Geometry buildShiftedGeometry( Geometry g, double shiftX, double shiftY ) {
        Geometry clone = (Geometry) g.clone();
        Coordinate[] coords = clone.getCoordinates();
        for( int i = 0; i < coords.length; i++ ) {
            Coordinate coord = coords[i];
            coord.x += shiftX;
            coord.y += shiftY;
        }

        return clone;
    }

    public void testScaleCalc() throws Exception 
	{
    	//1388422.8746916912, 639551.3924667436
    	//1407342.5139777814, 650162.7155794351
    	//655,368
                  //some location in bc albers
    	Envelope envelope = new Envelope(1388422.8746916912,1407342.5139777814,639551.3924667438,650162.715579435);
    	
    	final Parser parser = new Parser();
    	
    	String wkt = "PROJCS[\"NAD83 / BC Albers\",GEOGCS[\"NAD83\",DATUM[\"North_American_Datum_1983\",SPHEROID[\"GRS 1980\",6378137,298.257222101,AUTHORITY[\"EPSG\",\"7019\"]],TOWGS84[0,0,0],AUTHORITY[\"EPSG\",\"6269\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.01745329251994328,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4269\"]],PROJECTION[\"Albers_Conic_Equal_Area\"],PARAMETER[\"standard_parallel_1\",50],PARAMETER[\"standard_parallel_2\",58.5],PARAMETER[\"latitude_of_center\",45],PARAMETER[\"longitude_of_center\",-126],PARAMETER[\"false_easting\",1000000],PARAMETER[\"false_northing\",0],UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]],AUTHORITY[\"EPSG\",\"3005\"]]";
    	DefaultProjectedCRS crs  = (DefaultProjectedCRS) parser.parseObject(wkt);

    	
    	double s = RendererUtilities.calculateScale( envelope, crs,
    			       655, 368, 90.0) ;
    	
    	assertTrue( Math.abs(102355-s) < 10 ); //102355.1639202933
	}
}
