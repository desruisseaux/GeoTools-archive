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
package org.geotools.renderer.shape;

import com.vividsolutions.jts.geom.Envelope;
import junit.framework.TestCase;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.Feature;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.IllegalFilterException;
import org.geotools.renderer.RenderListener;
import org.geotools.resources.TestData;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbolizer;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;


/**
 * Utitility methods for unit tests
 *
 * @author jeichar
 *
 * @since 2.1.x
 */
public class TestUtilites {
    static final FilterFactory filterFactory = FilterFactory
        .createFilterFactory();
    public static boolean INTERACTIVE = false;

    public static ShapefileDataStore getPolygons() throws IOException {
        return TestUtilites.getDataStore("lakes.shp");
    }

    public static ShapefileDataStore getDataStore(String filename)
        throws IOException {
        URL url = TestData.getResource(Rendering2DTest.class, filename);
        ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();

        return (ShapefileDataStore) factory.createDataStore(url);
    }

    public static ShapefileDataStore getLines() throws IOException {
        return getDataStore("streams.shp");
    }

    public static ShapefileDataStore getPoints() throws IOException {
        URL url = TestData.getResource(Rendering2DTest.class, "pointtest.shp");
        ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();

        return (ShapefileDataStore) factory.createDataStore(url);
    }

    public static Style createTestStyle(String polyName, String lineName)
        throws IllegalFilterException {
        return createTestStyle(polyName, lineName, null);
    }

    public static Style createTestStyle(String polyName, String lineName,
        String pointName) throws IllegalFilterException {
        if (polyName == null) {
            polyName = "lakes";
        }

        if (lineName == null) {
            lineName = "streams";
        }

        if (pointName == null) {
            pointName = "pointtest";
        }

        StyleFactory sFac = StyleFactory.createStyleFactory();

        // The following is complex, and should be built from
        // an SLD document and not by hand
        PointSymbolizer pointsym = sFac.createPointSymbolizer();
        pointsym.setGraphic(sFac.getDefaultGraphic());

        LineSymbolizer linesym = sFac.createLineSymbolizer();
        Stroke myStroke = sFac.getDefaultStroke();
        myStroke.setColor(TestUtilites.filterFactory.createLiteralExpression(
                "#0000ff"));
        myStroke.setWidth(TestUtilites.filterFactory.createLiteralExpression(
                new Integer(5)));
        Rendering2DTest.LOGGER.info("got new Stroke " + myStroke);
        linesym.setStroke(myStroke);

        PolygonSymbolizer polysym = sFac.createPolygonSymbolizer();
        Fill myFill = sFac.getDefaultFill();
        myFill.setColor(TestUtilites.filterFactory.createLiteralExpression(
                "#ff0000"));
        polysym.setFill(myFill);
        myStroke = sFac.getDefaultStroke();
        myStroke.setColor(TestUtilites.filterFactory.createLiteralExpression(
                "#0000ff"));
        myStroke.setWidth(TestUtilites.filterFactory.createLiteralExpression(
                new Integer(2)));
        polysym.setStroke(myStroke);

        Rule rule = sFac.createRule();
        rule.setSymbolizers(new Symbolizer[] { polysym });

        FeatureTypeStyle fts = sFac.createFeatureTypeStyle(new Rule[] { rule });
        fts.setFeatureTypeName(polyName);

        Rule rule2 = sFac.createRule();
        rule2.setSymbolizers(new Symbolizer[] { linesym });

        FeatureTypeStyle fts2 = sFac.createFeatureTypeStyle();
        fts2.setRules(new Rule[] { rule2 });
        fts2.setFeatureTypeName(lineName);

        Rule rule3 = sFac.createRule();
        rule3.setSymbolizers(new Symbolizer[] { pointsym });

        FeatureTypeStyle fts3 = sFac.createFeatureTypeStyle();
        fts3.setRules(new Rule[] { rule3 });
        fts3.setFeatureTypeName(pointName);

        Style style = sFac.createStyle();
        style.setFeatureTypeStyles(new FeatureTypeStyle[] { fts, fts2, fts3 });

        return style;
    }

    /**
     * bounds may be null
     *
     * @param testName DOCUMENT ME!
     * @param renderer DOCUMENT ME!
     * @param timeOut DOCUMENT ME!
     * @param bounds DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public static void showRender(String testName, ShapefileRenderer renderer,
        long timeOut, Envelope bounds) throws Exception {
        showRender(testName, renderer, timeOut, bounds, -1);
    }

    /**
     * bounds may be null
     *
     * @param testName DOCUMENT ME!
     * @param renderer DOCUMENT ME!
     * @param timeOut DOCUMENT ME!
     * @param bounds DOCUMENT ME!
     * @param expectedFeatureCount DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public static void showRender(String testName, ShapefileRenderer renderer,
        long timeOut, Envelope bounds, int expectedFeatureCount)
        throws Exception {
        CountingRenderListener listener = new CountingRenderListener();

        if (expectedFeatureCount > -1) {
            renderer.addRenderListener(listener);
        }

        int w = 300;
        int h = 300;
        final BufferedImage image = new BufferedImage(w, h,
                BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.getGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, w, h);
        TestUtilites.render(renderer, g, new Rectangle(w, h), bounds);

        if (((System.getProperty("java.awt.headless") == null)
                || !System.getProperty("java.awt.headless").equals("true"))
                && TestUtilites.INTERACTIVE) {
            Frame frame = new Frame(testName);
            frame.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        e.getWindow().dispose();
                    }
                });

            Panel p = new Panel() {
                    /** <code>serialVersionUID</code> field */
                    private static final long serialVersionUID = 1L;

                    public void paint(Graphics g) {
                        g.drawImage(image, 0, 0, this);
                    }
                };

            frame.add(p);
            frame.setSize(w, h);
            frame.setVisible(true);

            Thread.sleep(timeOut);
            frame.dispose();
        }

        boolean hasData = false; //All I can seem to check reliably.

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (image.getRGB(x, y) != -1) {
                    hasData = true;
                }
            }
        }

        TestCase.assertTrue("image is blank and should not be", hasData);

        if (expectedFeatureCount > -1) {
            renderer.removeRenderListener(listener);
            TestCase.assertEquals(expectedFeatureCount, listener.count);
        }
    }

    /**
     * responsible for actually rendering.
     *
     * @param obj DOCUMENT ME!
     * @param g
     * @param rect DOCUMENT ME!
     * @param bounds
     *
     * @throws IOException
     */
    static void render(Object obj, Graphics g, Rectangle rect, Envelope bounds)
        throws IOException {
        if (obj instanceof ShapefileRenderer) {
            ShapefileRenderer renderer = (ShapefileRenderer) obj;

            if (bounds == null) {
                bounds = renderer.getContext().getLayerBounds();
            }

            renderer.paint((Graphics2D) g, rect, bounds);
        }
    }

    public static class CountingRenderListener implements RenderListener {
        public int count = 0;

        /* (non-Javadoc)
         * @see org.geotools.renderer.lite.RenderListener#featureRenderer(org.geotools.feature.Feature)
         */
        public void featureRenderer(Feature feature) {
            count++;
        }

        /* (non-Javadoc)
         * @see org.geotools.renderer.lite.RenderListener#errorOccurred(java.lang.Exception)
         */
        public void errorOccurred(Exception e) {
            // TODO Auto-generated method stub
        }
    }
}
