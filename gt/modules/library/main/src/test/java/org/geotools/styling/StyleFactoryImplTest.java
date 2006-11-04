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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.geotools.feature.Feature;
import org.geotools.filter.Expression;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;


/**
 * DOCUMENT ME!
 *
 * @author iant
 * @source $URL$
 */
public class StyleFactoryImplTest extends TestCase {
    static StyleFactory styleFactory;
    static FilterFactory filterFactory = FilterFactoryFinder
        .createFilterFactory();
    static Feature feature;
    protected static final Logger LOGGER = Logger
    .getLogger("org.geotools.styling");

    public StyleFactoryImplTest(java.lang.String testName) {
        super(testName);

        feature = null;
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(StyleFactoryImplTest.class);

        return suite;
    }

    /**
     * Test of createStyle method, of class
     * org.geotools.styling.StyleFactoryImpl.
     */
    public void testCreateStyle() {
        LOGGER.finer("testCreateStyle");

        styleFactory = StyleFactoryFinder.createStyleFactory();

        assertNotNull("Failed to build styleFactory", styleFactory);
    }

    /**
     * Test of createPointSymbolizer method, of class
     * org.geotools.styling.StyleFactoryImpl.
     */
    public void testCreatePointSymbolizer() {
        LOGGER.finer("testCreatePointSymbolizer");

        PointSymbolizer ps = styleFactory.createPointSymbolizer();

        assertNotNull("Failed to create PointSymbolizer", ps);
    }

    /**
     * Test of createPolygonSymbolizer method, of class
     * org.geotools.styling.StyleFactoryImpl.
     */
    public void testCreatePolygonSymbolizer() {
        LOGGER.finer("testCreatePolygonSymbolizer");

        PolygonSymbolizer ps = styleFactory.createPolygonSymbolizer();

        assertNotNull("Failed to create PolygonSymbolizer", ps);
    }

    /**
     * Test of createLineSymbolizer method, of class
     * org.geotools.styling.StyleFactoryImpl.
     */
    public void testCreateLineSymbolizer() {
        LOGGER.finer("testCreateLineSymbolizer");

        LineSymbolizer ls = styleFactory.createLineSymbolizer();

        assertNotNull("Failed to create PolygonSymbolizer", ls);
    }

    /**
     * Test of createTextSymbolizer method, of class
     * org.geotools.styling.StyleFactoryImpl.
     */
    public void testCreateTextSymbolizer() {
        LOGGER.finer("testCreateTextSymbolizer");

        TextSymbolizer ts = styleFactory.createTextSymbolizer();

        assertNotNull("Failed to create TextSymbolizer", ts);
    }

    /**
     * Test of createFeatureTypeStyle method, of class
     * org.geotools.styling.StyleFactoryImpl.
     */
    public void testCreateFeatureTypeStyle() {
        LOGGER.finer("testCreateFeatureTypeStyle");

        FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle();

        assertNotNull("failed to create featureTypeStyle", fts);
    }

    /**
     * Test of createRule method, of class
     * org.geotools.styling.StyleFactoryImpl.
     */
    public void testCreateRule() {
        LOGGER.finer("testCreateRule");

        Rule r = styleFactory.createRule();

        assertNotNull("failed to create Rule", r);
    }

    /**
     * Test of createStroke method, of class
     * org.geotools.styling.StyleFactoryImpl.
     */
    public void testCreateStroke() {
        LOGGER.finer("testCreateStroke");

        Stroke s = styleFactory.createStroke(filterFactory
                .createLiteralExpression("#000000"),
                filterFactory.createLiteralExpression(2.0));

        assertNotNull("Failed to build stroke ", s);

        s = styleFactory.createStroke(filterFactory.createLiteralExpression(
                    "#000000"), filterFactory.createLiteralExpression(2.0),
                filterFactory.createLiteralExpression(0.5));

        assertNotNull("Failed to build stroke ", s);

        s = styleFactory.createStroke(filterFactory.createLiteralExpression(
                    "#000000"), filterFactory.createLiteralExpression(2.0),
                filterFactory.createLiteralExpression(0.5),
                filterFactory.createLiteralExpression("bevel"),
                filterFactory.createLiteralExpression("square"),
                new float[] { 1.1f, 2.1f, 6f, 2.1f, 1.1f, 5f },
                filterFactory.createLiteralExpression(3), null, null);

        assertNotNull("Failed to build stroke ", s);

        assertEquals("Wrong color ", "#000000",
            s.getColor().getValue(feature).toString());
        assertEquals("Wrong width ", "2.0",
            s.getWidth().getValue(feature).toString());
        assertEquals("Wrong opacity ", "0.5",
            s.getOpacity().getValue(feature).toString());
        assertEquals("Wrong linejoin ", "bevel",
            s.getLineJoin().getValue(feature).toString());
        assertEquals("Wrong linejoin ", "square",
            s.getLineCap().getValue(feature).toString());
        assertEquals("Broken dash array", 2.1f, s.getDashArray()[1], 0.001f);
        assertEquals("Wrong dash offset ", "3",
            s.getDashOffset().getValue(feature).toString());
    }

    /**
     * Test of createFill method, of class
     * org.geotools.styling.StyleFactoryImpl.
     */
    public void testCreateFill() {
        LOGGER.finer("testCreateFill");

        Fill f = styleFactory.createFill(filterFactory.createLiteralExpression(
                    "#808080"));

        assertNotNull("Failed to build fill", f);

        f = styleFactory.createFill(filterFactory.createLiteralExpression(
                    "#808080"), filterFactory.createLiteralExpression(1.0));
        assertNotNull("Failed to build fill", f);

        f = styleFactory.createFill(null);
        assertEquals( f.getColor(), Fill.DEFAULT.getColor() );
        assertSame( f.getColor(), Fill.DEFAULT.getColor() );
        
        assertEquals( f.getBackgroundColor(), Fill.DEFAULT.getBackgroundColor() );
        assertSame( f.getBackgroundColor(), Fill.DEFAULT.getBackgroundColor() );        
    }

    /**
     * Test of createMark method, of class
     * org.geotools.styling.StyleFactoryImpl.
     */
    public void testCreateMark() {
        LOGGER.finer("testCreateMark");

        Mark m = styleFactory.createMark();

        assertNotNull("Failed to build mark ", m);
    }

    /**
     * Test of getSquareMark method, of class
     * org.geotools.styling.StyleFactoryImpl.
     */
    public void testGetNamedMarks() {
        LOGGER.finer("testGetNamedMarks");

        Mark m;
        String[] names = { "Square", "Circle", "Triangle", "Star", "X", "Cross" };

        for (int i = 0; i < names.length; i++) {
            try {
                Class target = styleFactory.getClass();

                //                LOGGER.finer("About to load get"+names[i]+"Mark");
                Method method = target.getMethod("get" + names[i] + "Mark",
                        (Class[]) null);

                //                LOGGER.finer("got method back " + method.toString());
                m = (Mark) method.invoke(styleFactory, (Object[]) null);
                assertNotNull("Failed to get " + names[i] + " mark ", m);

                Expression exp = filterFactory.createLiteralExpression(names[i]);
                assertEquals("Wrong sort of mark returned ", exp,
                    m.getWellKnownName());
                assertEquals("Wrong size of mark returned ", "6",
                    m.getSize().getValue(feature).toString());
            } catch (InvocationTargetException ite) {
                ite.getTargetException().printStackTrace();
                fail("InvocationTargetException " + ite.getTargetException());
            } catch (Exception e) {
                e.printStackTrace();
                fail("Exception " + e.toString());
            }
        }
    }

    /**
     * Test of createGraphic method, of class
     * org.geotools.styling.StyleFactoryImpl.
     */
    public void testCreateGraphic() {
        LOGGER.finer("testCreateGraphic");

        ExternalGraphic[] externalGraphics = new ExternalGraphic[] {
                styleFactory.createExternalGraphic("http://www.ccg.leeds.ac.uk/ian/geotools/icons/rail.gif",
                    "image/gif")
            };
        Mark[] marks = new Mark[] { styleFactory.getCircleMark() };
        Mark[] symbols = new Mark[0];
        Expression opacity = filterFactory.createLiteralExpression(0.5);
        Expression size = filterFactory.createLiteralExpression(10);
        Expression rotation = filterFactory.createLiteralExpression(145.0);
        Graphic g = styleFactory.createGraphic(externalGraphics, marks,
                symbols, opacity, size, rotation);

        assertNotNull("failed to build graphic ", g);
    }

    /**
     * Test of createFont method, of class
     * org.geotools.styling.StyleFactoryImpl.
     */
    public void testCreateFont() {
        LOGGER.finer("testCreateFont");

        Expression fontFamily = filterFactory.createLiteralExpression("Times");
        Expression fontStyle = filterFactory.createLiteralExpression("Italic");
        Expression fontWeight = filterFactory.createLiteralExpression("Bold");
        Expression fontSize = filterFactory.createLiteralExpression("12");
        Font f = styleFactory.createFont(fontFamily, fontStyle, fontWeight,
                fontSize);

        assertNotNull("Failed to build font", f);

        assertEquals("Wrong font type ", "Times",
            f.getFontFamily().getValue(feature).toString());
        assertEquals("Wrong font Style ", "Italic",
            f.getFontStyle().getValue(feature).toString());
        assertEquals("Wrong font weight ", "Bold",
            f.getFontWeight().getValue(feature).toString());
        assertEquals("Wrong font size ", "12",
            f.getFontSize().getValue(feature).toString());
    }

    /**
     * Test of createLinePlacement method, of class
     * org.geotools.styling.StyleFactoryImpl.
     */
    public void testCreateLinePlacement() {
        LOGGER.finer("testCreateLinePlacement");

        LinePlacement lp = styleFactory.createLinePlacement(filterFactory
                .createLiteralExpression(10));

        assertNotNull("failed to create LinePlacement", lp);
    }

    /**
     * Test of createPointPlacement method, of class
     * org.geotools.styling.StyleFactoryImpl.
     */
    public void testCreatePointPlacement() {
        LOGGER.finer("testCreatePointPlacement");

        AnchorPoint anchorPoint = styleFactory.createAnchorPoint(filterFactory
                .createLiteralExpression(1.0),
                filterFactory.createLiteralExpression(0.5));
        Displacement displacement = styleFactory.createDisplacement(filterFactory
                .createLiteralExpression(10.0),
                filterFactory.createLiteralExpression(5.0));
        Expression rotation = filterFactory.createLiteralExpression(90.0);
        PointPlacement pp = styleFactory.createPointPlacement(anchorPoint,
                displacement, rotation);

        assertNotNull("failed to create PointPlacement", pp);

        assertEquals("Wrong X anchorPoint ", "1.0",
            pp.getAnchorPoint().getAnchorPointX().getValue(feature).toString());
        assertEquals("Wrong Y anchorPoint ", "0.5",
            pp.getAnchorPoint().getAnchorPointY().getValue(feature).toString());
        assertEquals("Wrong X displacement ", "10.0",
            pp.getDisplacement().getDisplacementX().getValue(feature).toString());
        assertEquals("Wrong Y displacement ", "5.0",
            pp.getDisplacement().getDisplacementY().getValue(feature).toString());
        assertEquals("Wrong Rotation ", "90.0",
            pp.getRotation().getValue(feature).toString());
    }

    /**
     * Test of createHalo method, of class
     * org.geotools.styling.StyleFactoryImpl.
     */
    public void testCreateHalo() {
        LOGGER.finer("testCreateHalo");

        Halo h = styleFactory.createHalo(styleFactory.getDefaultFill(),
                filterFactory.createLiteralExpression(4));

        assertNotNull("Failed to build halo", h);

        assertEquals("Wrong radius", 4,
            ((Number) h.getRadius().getValue(feature)).intValue());
    }

    //    
    //    /** Test of getDefaultFill method, of class org.geotools.styling.StyleFactoryImpl. */
    //    public void testGetDefaultFill() {
    //        LOGGER.finer("testGetDefaultFill");
    //        
    //    }
    //    
    //    /** Test of getDefaultLineSymbolizer method, of class org.geotools.styling.StyleFactoryImpl. */
    //    public void testGetDefaultLineSymbolizer() {
    //        LOGGER.finer("testGetDefaultLineSymbolizer");
    //        
    //        
    //    }
    //    
    //    /** Test of getDefaultMark method, of class org.geotools.styling.StyleFactoryImpl. */
    //    public void testGetDefaultMark() {
    //        LOGGER.finer("testGetDefaultMark");
    //        
    //        
    //    }
    //    
    //    /** Test of getDefaultPointSymbolizer method, of class org.geotools.styling.StyleFactoryImpl. */
    //    public void testGetDefaultPointSymbolizer() {
    //        LOGGER.finer("testGetDefaultPointSymbolizer");
    //        
    //        
    //    }
    //    
    //    /** Test of getDefaultPolygonSymbolizer method, of class org.geotools.styling.StyleFactoryImpl. */
    //    public void testGetDefaultPolygonSymbolizer() {
    //        LOGGER.finer("testGetDefaultPolygonSymbolizer");
    //        
    //    }
    //    
    //    /** Test of getDefaultStroke method, of class org.geotools.styling.StyleFactoryImpl. */
    //    public void testGetDefaultStroke() {
    //        LOGGER.finer("testGetDefaultStroke");
    //        
    //        
    //    }
    //    
    //    /** Test of getDefaultStyle method, of class org.geotools.styling.StyleFactoryImpl. */
    //    public void testGetDefaultStyle() {
    //        LOGGER.finer("testGetDefaultStyle");
    //        
    //        
    //    }
    //    
    //    /** Test of getDefaultTextSymbolizer method, of class org.geotools.styling.StyleFactoryImpl. */
    //    public void testGetDefaultTextSymbolizer() {
    //        LOGGER.finer("testGetDefaultTextSymbolizer");
    //        
    //        
    //    }
    //    
    //    /** Test of getDefaultFont method, of class org.geotools.styling.StyleFactoryImpl. */
    //    public void testGetDefaultFont() {
    //        LOGGER.finer("testGetDefaultFont");
    //        
    //        
    //    }
    //    
    //    /** Test of getDefaultGraphic method, of class org.geotools.styling.StyleFactoryImpl. */
    //    public void testGetDefaultGraphic() {
    //        LOGGER.finer("testGetDefaultGraphic");
    //        
    //        
    //    }
    //    
    //    /** Test of createRasterSymbolizer method, of class org.geotools.styling.StyleFactoryImpl. */
    //    public void testCreateRasterSymbolizer() {
    //        LOGGER.finer("testCreateRasterSymbolizer");
    //        
    //        
    //    }
}
