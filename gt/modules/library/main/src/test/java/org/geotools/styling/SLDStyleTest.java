/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, Geotools Project Managment Committee (PMC)
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

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.filter.AttributeExpressionImpl;
import org.geotools.filter.Expression;
import org.geotools.filter.ExpressionType;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.FilterType;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.LogicFilter;
import org.geotools.resources.TestData;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Polygon;


/**
 * DOCUMENT ME!
 *
 * @author jamesm
 * @source $URL$
 */
public class SLDStyleTest extends TestCase {
    StyleFactory sf = StyleFactoryFinder.createStyleFactory();
    FilterFactory ff = FilterFactoryFinder.createFilterFactory();
    StyleBuilder sb = new StyleBuilder(sf, ff);

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

        //java.net.URL surl = new java.net.URL(base + "/test-sld.xml");
        java.net.URL surl = TestData.getResource(this, "test-sld.xml");
        SLDParser stylereader = new SLDParser(sf, surl);
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
     * XML --> SLD --> XML 
     * @throws Exception
     */
    public void testSLDParser() throws Exception {
        java.net.URL surl = TestData.getResource(this, "example-sld.xml");
        SLDParser stylereader = new SLDParser(sf, surl);
        StyledLayerDescriptor sld = stylereader.parseSLD();
        
        //convert back to xml again
        SLDTransformer aTransformer = new SLDTransformer();
        String xml = aTransformer.transform(sld);

        assertNotNull(xml);
        //we're content for the moment if this didn't throw an exception...
        //TODO: convert the buffer/resource to a string and compare
    }

    /**
	 * SLD --> XML --> SLD
	 * @throws Exception
	 */
    public void testSLDTransformer() throws Exception {
    	//create an SLD
    	StyledLayerDescriptor sld = sf.createStyledLayerDescriptor();
    	StyledLayerDescriptor sld2;
    	sld.setName("SLD Name");
    	sld.setTitle("SLD Title");
    	UserLayer layer = sf.createUserLayer();
    	layer.setName("UserLayer Name");

    	Style style = sf.createStyle();
    	style.setName("Style Name");
    	style.setTitle("Style Title");
    	Rule rule1 = sb.createRule(sb.createLineSymbolizer(new Color(0), 2));
    	// note: symbolizers containing a fill will likely fail, as the SLD
		// transformation loses a little data (background colour?)
    	FeatureTypeStyle fts1 = sf.createFeatureTypeStyle(new Rule[] {rule1});
    	fts1.setSemanticTypeIdentifiers(new String[] {"generic:geometry"});
    	style.setFeatureTypeStyles(new FeatureTypeStyle[] {fts1});
    	layer.setUserStyles(new Style[] {style});
    	sld.setStyledLayers(new UserLayer[] {layer});
    	
    	//convert it to XML
        SLDTransformer aTransformer = new SLDTransformer();
        String xml = aTransformer.transform(sld);

        //back to SLD
        InputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
        
        SLDParser stylereader = new SLDParser(sf, is);
        
        sld2 = stylereader.parseSLD();
// UNCOMMENT FOR DEBUGGING
//        assertEquals(SLD.rules(SLD.styles(sld)[0]).length, SLD.rules(SLD.styles(sld2)[0]).length);
//        for (int i = 0; i < SLD.rules(SLD.styles(sld)[0]).length; i++) {
//            Rule aRule = SLD.rules(SLD.styles(sld)[0])[i];
//            Rule bRule = SLD.rules(SLD.styles(sld2)[0])[i];
//            System.out.println(i+":"+aRule);
//        	Symbolizer[] symb1 = SLD.symbolizers(aRule);
//        	Symbolizer[] symb2 = SLD.symbolizers(bRule);
//        	for (int j = 0; j < symb1.length; j++) {
//        		//symbolizers are equal
//        		assertTrue(symb1[j].equals(symb2[j]));
//        	}
//        	//rules are equal
//            assertTrue(aRule.equals(bRule));
//        }
//        //feature type styles are equal
//        assertTrue(SLD.featureTypeStyles(sld)[0].equals(SLD.featureTypeStyles(sld2)[0]));
//        //styles are equal
//        assertTrue(SLD.styles(sld)[0].equals(SLD.styles(sld2)[0]));
//        //layers are equal
//        StyledLayer layer1 = sld.getStyledLayers()[0];
//        StyledLayer layer2 = sld2.getStyledLayers()[0];
//        boolean result = layer1.equals(layer2); 
//        assertTrue(result);
        
        //everything is equal
        assertTrue(sld2.equals(sld));
    }

    public void testSLDTransformerIndentation() throws Exception {
    	//create a simple object
    	StyledLayerDescriptor sld = sf.createStyledLayerDescriptor();
    	NamedLayer nl = sf.createNamedLayer();
    	nl.setName("named_layer_1");
    	sld.addStyledLayer(nl);
    	//convert it to XML
        SLDTransformer aTransformer = new SLDTransformer();
        aTransformer.setIndentation(3); //3 spaces
        String xml1 = aTransformer.transform(sld);
        aTransformer.setIndentation(4); //4 spaces
        String xml2 = aTransformer.transform(sld);
        //generated xml contains 4 indents, so if indentation is working, the difference should be 4
        assertEquals(xml1.length() + 4, xml2.length());
    }
    
    public void testParseSLD_NameSpaceAware() throws Exception {
        URL surl = TestData.getResource(this, "test-ns.sld");
        StyleFactory factory = StyleFactoryFinder.createStyleFactory();
        SLDParser stylereader = new SLDParser(factory, surl);
        StyledLayerDescriptor sld = stylereader.parseSLD();
        
        assertEquals(1, sld.getStyledLayers().length);
        FeatureTypeStyle[] fts = SLD.featureTypeStyles(sld);
        assertEquals(2, fts.length);
        assertEquals(1, fts[0].getSemanticTypeIdentifiers().length);
        assertEquals(2, fts[1].getSemanticTypeIdentifiers().length);
        assertEquals("colorbrewer:default", fts[1].getSemanticTypeIdentifiers()[1]);
    }
    
    /**
     * Test of parseSLD method to ensure NamedLayer/Name and
     * NamedLayer/NamedStyle are parsed correctly
     *
     * @throws Exception boom
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
     * @throws Exception boom
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
     * @throws IOException boom
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

    /**
     * Verifies that a FID Filter is correctly parsed (GEOT-992).
     *
     * @throws IOException boom
     */
    public void testParseFidFilter() throws IOException {
        StyleFactory factory = StyleFactoryFinder.createStyleFactory();
        java.net.URL surl = TestData.getResource(this, "fidFilter.xml");
        SLDParser stylereader = new SLDParser(factory, surl);

        Style[] styles = stylereader.readXML();

        final int expectedStyleCount = 1;
        assertEquals(expectedStyleCount, styles.length);

        Style style = styles[0];
        assertEquals(1, style.getFeatureTypeStyles().length);

        FeatureTypeStyle fts = style.getFeatureTypeStyles()[0];
        assertEquals("Feature", fts.getFeatureTypeName());
        assertEquals(1, fts.getRules().length);

        
        Filter filter = fts.getRules()[0].getFilter();
        assertEquals(FilterType.FID, filter.getFilterType());

        FidFilter fidFilter = (FidFilter) filter;
        String[] fids = fidFilter.getFids();
        assertEquals("Wrong number of fids", 5, fids.length);
        
        Arrays.sort(fids);
        
        assertEquals("fid.0", fids[0]);
        assertEquals("fid.1", fids[1]);
        assertEquals("fid.2", fids[2]);
        assertEquals("fid.3", fids[3]);
        assertEquals("fid.4", fids[4]);
    }
    
}
