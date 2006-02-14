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
package org.geotools.styling;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Polygon;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.geotools.filter.AttributeExpressionImpl;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterType;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.LogicFilter;
import org.geotools.filter.expression.Expression;
import org.geotools.filter.expression.ExpressionType;
import org.geotools.resources.TestData;
import java.io.IOException;
import java.net.URL;


/**
 * DOCUMENT ME!
 *
 * @author jamesm
 * @source $URL$
 */
public class SLDStyleTest extends TestCase {
    /**
     * Creates a new SLDStyleTest object.
     *
     * @param testName DOCUMENT ME!
     */
    public SLDStyleTest(java.lang.String testName) {
        super(testName);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(SLDStyleTest.class);

        return suite;
    }

    /**
     * Test of parseStyle method, of class org.geotools.styling.SLDStyle.
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testParseStyle() throws Exception {
        //java.net.URL base = getClass().getResource("testData/");
        // base = getClass().getResource("testData");
        // base = getClass().getResource("/testData");
        StyleFactory factory = StyleFactoryFinder.createStyleFactory();

        //java.net.URL surl = new java.net.URL(base + "/test-sld.xml");
        java.net.URL surl = TestData.getResource(this, "test-sld.xml");
        SLDParser stylereader = new SLDParser(factory, surl);
        StyledLayerDescriptor sld = stylereader.parseSLD();
        assertEquals("My Layer", sld.getName());
        assertEquals("A layer by me", sld.getTitle());
        assertEquals("this is a sample layer", sld.getAbstract());
        assertEquals(1, sld.getStyledLayers().length);

        UserLayer layer = (UserLayer) sld.getStyledLayers()[0];
        assertNull(layer.getName());
        assertEquals(1, layer.getUserStyles().length);

        Style style = layer.getUserStyles()[0];
        assertEquals(1, style.getFeatureTypeStyles().length);
        assertEquals("My User Style", style.getName());
        assertEquals("A style by me", style.getTitle());
        assertEquals("this is a sample style", style.getAbstract());

        FeatureTypeStyle fts = style.getFeatureTypeStyles()[0];
        Rule rule = fts.getRules()[0];
        LineSymbolizer lineSym = (LineSymbolizer) rule.getSymbolizers()[0];
        assertEquals(4,
            ((Number) lineSym.getStroke().getWidth().getValue(null)).intValue());
    }

    public void testParseSLD() throws Exception {
        StyleFactory factory = StyleFactoryFinder.createStyleFactory();
        java.net.URL surl = TestData.getResource(this, "example-sld.xml");
        SLDParser stylereader = new SLDParser(factory, surl);
        StyledLayerDescriptor sld = stylereader.parseSLD();

        //convert back to xml again
        SLDTransformer aTransformer = new SLDTransformer();
        String xml = aTransformer.transform(sld);

        //System.out.println(xml);
        assertNotNull(xml);
        //we're content if this didn't throw an exception...
        //TODO: add a real test case
    }

    public void testParseSLD_NameSpaceAware() throws Exception {
        URL surl = TestData.getResource(this, "test-ns.sld");
        StyleFactory factory = StyleFactoryFinder.createStyleFactory();
        SLDParser stylereader = new SLDParser(factory, surl);
        StyledLayerDescriptor sld = stylereader.parseSLD();
        
        assertEquals(1, sld.getStyledLayers().length);
        FeatureTypeStyle[] fts = SLD.getFeatureTypeStyles(sld);
        assertEquals(2, fts.length);
        assertEquals(1, fts[0].getSemanticTypeIdentifiers().length);
        assertEquals(2, fts[1].getSemanticTypeIdentifiers().length);
        assertEquals("colorbrewer:default", fts[1].getSemanticTypeIdentifiers()[1]);
    }
    
    /**
     * Test of parseSLD method to ensure NamedLayer/Name and
     * NamedLayer/NamedStyle are parsed correctly
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testParseSLDNamedLayersOnly() throws Exception {
        StyleFactory factory = StyleFactoryFinder.createStyleFactory();
        java.net.URL surl = TestData.getResource(this, "namedLayers.sld");
        SLDParser stylereader = new SLDParser(factory, surl);

        StyledLayerDescriptor sld = stylereader.parseSLD();

        final int expectedLayerCount = 3;
        final String[] layerNames = { "Rivers", "Roads", "Houses" };
        final String[] namedStyleNames = { "CenterLine", "CenterLine", "Outline" };
        StyledLayer[] layers = sld.getStyledLayers();

        assertEquals(expectedLayerCount, layers.length);

        for (int i = 0; i < expectedLayerCount; i++) {
            assertTrue(layers[i] instanceof NamedLayer);
        }

        for (int i = 0; i < expectedLayerCount; i++) {
            assertEquals(layerNames[i], layers[i].getName());
        }

        for (int i = 0; i < expectedLayerCount; i++) {
            NamedLayer layer = (NamedLayer) layers[i];
            assertEquals(1, layer.getStyles().length);
            assertTrue(layer.getStyles()[0] instanceof NamedStyle);
            assertEquals(namedStyleNames[i], layer.getStyles()[0].getName());
        }
    }

    /**
     * Test of parseSLD method to ensure NamedLayer/Name and
     * NamedLayer/NamedStyle are parsed correctly
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testParseSLDNamedAndUserLayers() throws Exception {
        StyleFactory factory = StyleFactoryFinder.createStyleFactory();
        java.net.URL surl = TestData.getResource(this, "mixedLayerTypes.sld");
        SLDParser stylereader = new SLDParser(factory, surl);

        StyledLayerDescriptor sld = stylereader.parseSLD();

        final int expectedLayerCount = 4;

        StyledLayer[] layers = sld.getStyledLayers();

        assertEquals(expectedLayerCount, layers.length);
        assertTrue(layers[0] instanceof NamedLayer);
        assertTrue(layers[1] instanceof UserLayer);
        assertTrue(layers[2] instanceof NamedLayer);
        assertTrue(layers[3] instanceof UserLayer);
    }

    /**
     * Verifies that geometry filters inside SLD documents are correctly
     * parsed.
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testParseGeometryFilters() throws IOException {
        final String TYPE_NAME = "testType";
        final String GEOMETRY_ATTR = "Polygons";
        StyleFactory factory = StyleFactoryFinder.createStyleFactory();
        java.net.URL surl = TestData.getResource(this, "spatialFilter.xml");
        SLDParser stylereader = new SLDParser(factory, surl);

        Style[] styles = stylereader.readXML();

        final int expectedStyleCount = 1;
        assertEquals(expectedStyleCount, styles.length);

        Style notDisjoint = styles[0];
        assertEquals(1, notDisjoint.getFeatureTypeStyles().length);

        FeatureTypeStyle fts = notDisjoint.getFeatureTypeStyles()[0];
        assertEquals(TYPE_NAME, fts.getFeatureTypeName());
        assertEquals(1, fts.getRules().length);

        Filter filter = fts.getRules()[0].getFilter();
        assertEquals(FilterType.LOGIC_NOT, filter.getFilterType());

        Filter spatialFilter = (Filter) ((LogicFilter) filter).getFilterIterator()
                                         .next();
        assertEquals(FilterType.GEOMETRY_DISJOINT, spatialFilter.getFilterType());

        Expression left = ((GeometryFilter) spatialFilter).getLeftGeometry();
        Expression right = ((GeometryFilter) spatialFilter).getRightGeometry();
        assertEquals(ExpressionType.ATTRIBUTE, left.getType());
        assertEquals(ExpressionType.LITERAL_GEOMETRY, right.getType());

        assertEquals(GEOMETRY_ATTR,
            ((AttributeExpressionImpl) left).getAttributePath());
        assertTrue(right.getValue(null) instanceof Polygon);

        Envelope bbox = ((Polygon) right.getValue(null)).getEnvelopeInternal();
        assertEquals(-10D, bbox.getMinX(), 0);
        assertEquals(-10D, bbox.getMinY(), 0);
        assertEquals(10D, bbox.getMaxX(), 0);
        assertEquals(10D, bbox.getMaxY(), 0);
    }

    // Add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
