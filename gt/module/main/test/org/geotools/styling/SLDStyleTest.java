/*
 * SLDStyleTest.java
 * JUnit based test
 *
 * Created on November 6, 2003, 11:32 AM
 */
package org.geotools.styling;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.geotools.resources.TestData;


/**
 * DOCUMENT ME!
 *
 * @author jamesm
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

    //
    //    /** Test of setInput method, of class org.geotools.styling.SLDStyle. */
    //    public void testSetInput() {
    //        System.out.println("testSetInput");
    //
    //        // Add your test code below by replacing the default call to fail.
    //        fail("The test case is empty.");
    //    }
    //
    //    /** Test of readXML method, of class org.geotools.styling.SLDStyle. */
    //    public void testReadXML() {
    //        System.out.println("testReadXML");
    //
    //        // Add your test code below by replacing the default call to fail.
    //        fail("The test case is empty.");
    //    }
    //
    //    /** Test of readDOM method, of class org.geotools.styling.SLDStyle. */
    //    public void testReadDOM() {
    //        System.out.println("testReadDOM");
    //
    //        // Add your test code below by replacing the default call to fail.
    //        fail("The test case is empty.");
    //    }
    //

    /**
     * Test of parseStyle method, of class org.geotools.styling.SLDStyle.
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testParseStyle() throws Exception {
        //java.net.URL base = getClass().getResource("testData/");
        // base = getClass().getResource("testData");
        // base = getClass().getResource("/testData");
        StyleFactory factory = StyleFactory.createStyleFactory();

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

    /**
     * Test of parseSLD method to ensure NamedLayer/Name and
     * NamedLayer/NamedStyle are parsed correctly
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testParseSLDNamedLayersOnly() throws Exception {
        StyleFactory factory = StyleFactory.createStyleFactory();
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
        StyleFactory factory = StyleFactory.createStyleFactory();
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
    
    // Add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
