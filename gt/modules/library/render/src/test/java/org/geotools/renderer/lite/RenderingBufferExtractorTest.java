package org.geotools.renderer.lite;

import java.awt.Color;

import junit.framework.TestCase;

import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.Symbolizer;

public class RenderingBufferExtractorTest extends TestCase {
    StyleBuilder sb = new StyleBuilder();

    public void testNoStroke() {
        Style style = sb.createStyle(sb.createPointSymbolizer());
        MaxStrokeWidthEstimator rbe = new MaxStrokeWidthEstimator();
        assertEquals(0, rbe.getBuffer());
        assertTrue(rbe.isEstimateAccurate());
        rbe.visit(style);
        assertEquals(0, rbe.getBuffer());
        assertTrue(rbe.isEstimateAccurate());
    }

    public void testSimpleStroke() {
        Style style = sb.createStyle(sb.createLineSymbolizer(sb.createStroke(10.0)));
        MaxStrokeWidthEstimator rbe = new MaxStrokeWidthEstimator();
        rbe.visit(style);
        assertEquals(10, rbe.getBuffer());
        assertTrue(rbe.isEstimateAccurate());
    }

    public void testNonIntegerStroke() {
        Style style = sb.createStyle(sb.createLineSymbolizer(sb.createStroke(10.8)));
        MaxStrokeWidthEstimator rbe = new MaxStrokeWidthEstimator();
        rbe.visit(style);
        assertEquals(11, rbe.getBuffer());
        assertTrue(rbe.isEstimateAccurate());
    }

    public void testMultiSymbolizers() {
        Symbolizer ls = sb.createLineSymbolizer(sb.createStroke(10.8));
        Symbolizer ps = sb.createPolygonSymbolizer(sb.createStroke(12), sb.createFill());
        Rule r = sb.createRule(new Symbolizer[] { ls, ps });
        MaxStrokeWidthEstimator rbe = new MaxStrokeWidthEstimator();
        rbe.visit(r);
        assertEquals(12, rbe.getBuffer());
        assertTrue(rbe.isEstimateAccurate());
    }

    public void testPropertyWidth() {
        Symbolizer ls = sb.createLineSymbolizer(sb.createStroke(sb.colorExpression(Color.BLACK), sb
                .attributeExpression("gimbo")));
        Symbolizer ps = sb.createPolygonSymbolizer(sb.createStroke(12), sb.createFill());
        Rule r = sb.createRule(new Symbolizer[] { ls, ps });
        MaxStrokeWidthEstimator rbe = new MaxStrokeWidthEstimator();
        rbe.visit(r);
        assertEquals(12, rbe.getBuffer());
        assertTrue(!rbe.isEstimateAccurate());
    }

    public void testLiteralParse() {
        Style style = sb.createStyle(sb.createLineSymbolizer(sb.createStroke(sb.colorExpression(Color.BLACK), sb
                .literalExpression("10.0"))));
        MaxStrokeWidthEstimator rbe = new MaxStrokeWidthEstimator();
        rbe.visit(style);
        assertEquals(10, rbe.getBuffer());
        assertTrue(rbe.isEstimateAccurate());
    }
}
