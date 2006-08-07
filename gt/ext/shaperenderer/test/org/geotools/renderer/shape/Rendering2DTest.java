/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, Geotools Project Managment Committee (PMC)
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
 *
 *    StylingTest.java JUnit based test Created on April 12, 2002, 1:18 PM
 */
package org.geotools.renderer.shape;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequenceFactory;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.filter.IllegalFilterException;
import org.geotools.geometry.JTS;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
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
import org.geotools.styling.StyleFactoryFinder;
import org.geotools.styling.StyledLayerDescriptor;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.UserLayer;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Logger;


/**
 * DOCUMENT ME!
 *
 * @author jamesm
 * @source $URL$
 */
public class Rendering2DTest extends TestCase {
    /** path for test data */

    // private java.net.URL base = getClass().getResource("/testData/");

    /** The logger for the rendering module. */
    public static final Logger LOGGER = Logger.getLogger(
            "org.geotools.rendering");
    static final String LINE = "linefeature";
    static final String POLYGON = "polygonfeature";
    static final String POINT = "pointfeature";
    static final String RING = "ringfeature";
    static final String COLLECTION = "collfeature";
    private Object transform;

    public Rendering2DTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(Rendering2DTest.class);

        return suite;
    }

    Style loadTestStyle() throws IOException {
        StyleFactory factory = StyleFactoryFinder.createStyleFactory();

        java.net.URL surl = TestData.getResource(this, "test-sld.xml");
        SLDParser stylereader = new SLDParser(factory, surl);
        StyledLayerDescriptor sld = stylereader.parseSLD();

        UserLayer layer = (UserLayer) sld.getStyledLayers()[0];

        Style style = layer.getUserStyles()[0];

        return style;
    }

    Style createTestStyle() throws IllegalFilterException {
        return TestUtilites.createTestStyle(null, null);
    }

    public void testSimpleRender() throws Exception {
        // same as the datasource test, load in some features into a table
        System.err.println("starting rendering2DTest");

        DataStore ds = TestUtilites.getPolygons();
        FeatureSource source = ds.getFeatureSource(ds.getTypeNames()[0]);
        Style style = createTestStyle();

        MapContext map = new DefaultMapContext();
        map.addLayer(source, style);

        ShapefileRenderer renderer = new ShapefileRenderer(map);
        Envelope env = map.getLayerBounds();
        env = new Envelope(env.getMinX(), env.getMaxX(), env.getMinY(),
                env.getMaxY());
        TestUtilites.showRender("testSimpleRender", renderer, 1000, env);
    }

    public void testSimpleLineRender() throws Exception {
        // same as the datasource test, load in some features into a table
        System.err.println("starting rendering2DTest");

        ShapefileRenderer renderer = createLineRenderer(TestUtilites.getLines());
        MapContext map = renderer.getContext();

        map.setAreaOfInterest(map.getLayer(0).getFeatureSource().getBounds(),
            map.getLayer(0).getFeatureSource().getSchema().getDefaultGeometry()
               .getCoordinateSystem());

        Envelope env = map.getLayerBounds();
        env = new Envelope(env.getMinX() - 20, env.getMaxX() + 20,
                env.getMinY() - 20, env.getMaxY() + 20);
        map.setAreaOfInterest(env);
        TestUtilites.showRender("testSimpleLineRender", renderer, 3000, env);
    }

    public void testSimplePolygonRender() throws Exception {
        // same as the datasource test, load in some features into a table
        System.err.println("starting rendering2DTest");

        ShapefileRenderer renderer = createLineRenderer(TestUtilites
                .getPolygons());
        MapContext map = renderer.getContext();

        map.setAreaOfInterest(map.getLayer(0).getFeatureSource().getBounds(),
            map.getLayer(0).getFeatureSource().getSchema().getDefaultGeometry()
               .getCoordinateSystem());

        Envelope env = map.getLayerBounds();
        env = new Envelope(env.getMinX() - 20, env.getMaxX() + 20,
                env.getMinY() - 20, env.getMaxY() + 20);
        map.setAreaOfInterest(env);
        TestUtilites.showRender("testSimpleLineRender", renderer, 3000, env);
    }

    public void testSimplePolygonRenderZoomedOut() throws Exception {
        // same as the datasource test, load in some features into a table
        System.err.println("starting rendering2DTest");

        ShapefileRenderer renderer = createLineRenderer(TestUtilites
                .getPolygons());
        MapContext map = renderer.getContext();

        map.setAreaOfInterest(map.getLayer(0).getFeatureSource().getBounds(),
            map.getLayer(0).getFeatureSource().getSchema().getDefaultGeometry()
               .getCoordinateSystem());

        Envelope env = map.getLayerBounds();
        env = new Envelope(env.getMinX() - 200000, env.getMaxX() + 200000,
                env.getMinY() - 200000, env.getMaxY() + 200000);
        map.setAreaOfInterest(env);
        TestUtilites.showRender("testSimpleLineRender", renderer, 3000, env);
    }

    public void testSimpleLineRenderLargebbox() throws Exception {
        // same as the datasource test, load in some features into a table
        System.err.println("starting rendering2DTest");

        ShapefileRenderer renderer = createLineRenderer(TestUtilites.getLines());
        MapContext map = renderer.getContext();

        map.setAreaOfInterest(map.getLayer(0).getFeatureSource().getBounds(),
            map.getLayer(0).getFeatureSource().getSchema().getDefaultGeometry()
               .getCoordinateSystem());

        Envelope env = map.getLayerBounds();
        env = new Envelope(env.getMinX() - env.getWidth(),
                env.getMaxX() + env.getWidth(),
                env.getMinY() - env.getHeight(), env.getMaxY()
                + env.getHeight());
        map.setAreaOfInterest(env);
        TestUtilites.showRender("testSimpleLineRender", renderer, 3000, env);
    }

    public void testSimplePointRender() throws Exception {
        // same as the datasource test, load in some features into a table
        System.err.println("starting rendering2DTest");

        DataStore ds = TestUtilites.getPoints();
        FeatureSource source = ds.getFeatureSource(ds.getTypeNames()[0]);
        Style style = createTestStyle();

        MapContext map = new DefaultMapContext();
        map.addLayer(source, style);

        ShapefileRenderer renderer = new ShapefileRenderer(map);
        Envelope env = map.getLayerBounds();
        env = new Envelope(env.getMinX() - 20, env.getMaxX() + 20,
                env.getMinY() - 20, env.getMaxY() + 20);
        TestUtilites.showRender("testSimplePointRender", renderer, 1000, env);
    }

    public void testReprojection() throws Exception {
        DataStore ds = TestUtilites.getPolygons();
        FeatureSource source = ds.getFeatureSource(ds.getTypeNames()[0]);
        Style style = createTestStyle();

        MapContext map = new DefaultMapContext();
        map.addLayer(source, style);

        ShapefileRenderer renderer = new ShapefileRenderer(map);
        CoordinateReferenceSystem crs = FactoryFinder.getCRSFactory(null)
                                                     .createFromWKT("PROJCS[\"NAD_1983_UTM_Zone_10N\",GEOGCS[\"GCS_North_American_1983\",DATUM[\"D_North_American_1983\",TOWGS84[0,0,0,0,0,0,0],SPHEROID[\"GRS_1980\",6378137,298.257222101]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.017453292519943295]],PROJECTION[\"Transverse_Mercator\"],PARAMETER[\"False_Easting\",500000],PARAMETER[\"False_Northing\",0],PARAMETER[\"Central_Meridian\",-123],PARAMETER[\"Scale_Factor\",0.9996],PARAMETER[\"Latitude_Of_Origin\",0],UNIT[\"Meter\",1]]");

        MathTransform t = FactoryFinder.getCoordinateOperationFactory(null)
                                       .createOperation(DefaultGeographicCRS.WGS84,
                crs).getMathTransform();

        Envelope env = map.getLayerBounds();

        Envelope bounds = JTS.transform(env, t);
        map.setAreaOfInterest(bounds, crs);

        Rectangle rect = new Rectangle(400, 400);

        //        renderer.setOptimizedDataLoadingEnabled(true);
        env = new Envelope(bounds.getMinX(), bounds.getMaxX(),
                bounds.getMinY(), bounds.getMaxY());
        TestUtilites.showRender("testReprojection", renderer, 1000, env);

        // System.in.read();
    }

    public void testLineReprojection() throws Exception {
        // same as the datasource test, load in some features into a table
        System.err.println("starting rendering2DTest");

        ShapefileRenderer renderer = createLineRenderer(TestUtilites.getLines());
        Envelope env = renderer.getContext().getAreaOfInterest();

        //        INTERACTIVE=true;
        TestUtilites.showRender("testSimpleLineRender", renderer, 3000, env);
    }

    public void testNullCRSPoly() throws Exception {
        // same as the datasource test, load in some features into a table
        System.err.println("starting testLiteRender2");

        ShapefileDataStore ds = TestUtilites.getDataStore("smallMultiPoly.shp");
        FeatureSource source = ds.getFeatureSource(ds.getTypeNames()[0]);
        Style style = TestUtilites.createTestStyle(ds.getSchema().getTypeName(),
                null);

        MapContext map = new DefaultMapContext();
        map.addLayer(source, style);

        final BufferedImage image = new BufferedImage(400, 400,
                BufferedImage.TYPE_4BYTE_ABGR);
        ShapefileRenderer renderer = new ShapefileRenderer(map);
        Envelope env = map.getLayerBounds();

        Rectangle rect = new Rectangle(400, 400);

        //        renderer.setOptimizedDataLoadingEnabled(true);
        env = new Envelope(env.getMinX() - 1, env.getMaxX() + 1,
                env.getMinY() - 1, env.getMaxY() + 1);
        TestUtilites.showRender("testReprojection", renderer, 1000, env);

        // System.in.read();
    }

    public void testNullCRSLine() throws Exception {
        // same as the datasource test, load in some features into a table
        System.err.println("starting rendering2DTest");

        ShapefileRenderer renderer = createLineRenderer(TestUtilites
                .getDataStore("lineNoCRS.shp"));
        Envelope env = renderer.getContext().getAreaOfInterest();

        //        INTERACTIVE=true;
        TestUtilites.showRender("testSimpleLineRender", renderer, 3000, env);
    }

    public void testEnvelopePerformance() throws Exception {
        ShapefileRenderer renderer = createLineRenderer(TestUtilites.getLines());
        MapContext context = renderer.getContext();

        context.setAreaOfInterest(context.getLayerBounds());

        TestUtilites.CountingRenderListener l1 = new TestUtilites.CountingRenderListener();
        renderer.addRenderListener(l1);

        BufferedImage image = new BufferedImage(300, 300,
                BufferedImage.TYPE_3BYTE_BGR);
        renderer.paint(image.createGraphics(), new Rectangle(300, 300),
            context.getAreaOfInterest());
        renderer.removeRenderListener(l1);

        TestUtilites.CountingRenderListener l2 = new TestUtilites.CountingRenderListener();
        renderer.addRenderListener(l2);

        Envelope old = context.getAreaOfInterest();
        Envelope env = new Envelope(old.getMinX() + (old.getWidth() / 2),
                old.getMaxX() - (old.getWidth() / 2),
                old.getMinY() + (old.getHeight() / 2),
                old.getMaxY() - (old.getHeight() / 2));
        if (true) {
            // TODO: The remaining of this test is disabled because the CRS used is way outside
            //       its area of validity, which cause an AssertionError in projection code.
            return;
        }
        renderer.paint(image.createGraphics(), new Rectangle(300, 300), env);
        assertTrue(l1.count > l2.count);
    }

    /**
     * DOCUMENT ME!
     *
     * @param ds DOCUMENT ME!
     *
     * @return
     *
     * @throws Exception
     */
    private ShapefileRenderer createLineRenderer(ShapefileDataStore ds)
        throws Exception {
        FeatureSource source = ds.getFeatureSource(ds.getTypeNames()[0]);
        Style style = TestUtilites.createTestStyle(null, ds.getTypeNames()[0]);

        MapContext map = new DefaultMapContext();
        map.addLayer(source, style);

        ShapefileRenderer renderer = new ShapefileRenderer(map);
        Envelope env = map.getLayerBounds();
        env = new Envelope(env.getMinX(), env.getMaxX(), env.getMinY(),
                env.getMaxY());
        map.setAreaOfInterest(env);

        return renderer;
    }
}
