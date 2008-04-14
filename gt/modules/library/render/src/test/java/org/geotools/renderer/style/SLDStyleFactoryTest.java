/*
 * SLDStyleFactoryTest.java
 * JUnit based test
 *
 * Created on September 29, 2005, 3:10 PM
 */

package org.geotools.renderer.style;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Transparency;
import java.net.URL;

import junit.framework.TestCase;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Mark;
import org.geotools.styling.NamedStyle;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.SLDParser;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.StyleFactoryFinder;
import org.geotools.styling.StyleFactoryImpl;
import org.geotools.util.NumberRange;
import org.opengis.filter.FilterFactory2;

/**
 *
 * @author jamesm
 * @source $URL$
 */
public class SLDStyleFactoryTest extends TestCase {
    
    private StyleFactory styleFactory;
    private SLDStyleFactory sldFactory;
    private FilterFactory2 ff;
    
    public SLDStyleFactoryTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        styleFactory = CommonFactoryFinder.getStyleFactory( null );
        ff = CommonFactoryFinder.getFilterFactory2( null );
        sldFactory = new SLDStyleFactory();        
    }
    
    protected void tearDown() throws Exception {
    }
    
    /**
     * Test of createPolygonStyle method, of class org.geotools.renderer.style.SLDStyleFactory.
     */
    public void testCreateIncompletePolygonStyle() {
        NumberRange range = new NumberRange(1,1);
        PolygonSymbolizer symbolizer;
    
        //full symbolizer
        symbolizer = styleFactory.createPolygonSymbolizer();
        //symb.setFill(fac.createFill(FilterFactoryFinder.createFilterFactory().createLiteralExpression("#ffff00")));
        
        Style2D style = sldFactory.createPolygonStyle(null, symbolizer,range);
        assertNotNull( style );
    }
            
    /**
     * Test of createPointStyle method, of class org.geotools.renderer.style.SLDStyleFactory.
     */
    public void testCreateCompletePointStyle() {
        NumberRange range = new NumberRange(1,1);
        
        PointSymbolizer symb;
        Mark myMark;
        //full symbolizer
        symb = styleFactory.createPointSymbolizer();       
        myMark = styleFactory.createMark();
        myMark.setFill(styleFactory.createFill(ff.literal("#ffff00")));        
        symb.getGraphic().setSize(ff.literal(10));
        symb.getGraphic().addMark(myMark);
        symb.getGraphic().setOpacity(ff.literal(1));
        symb.getGraphic().setRotation(ff.literal(0));

        MarkStyle2D style = (MarkStyle2D)  sldFactory.createPointStyle(null, symb,range);
        assertEquals( Color.BLACK, style.getContour() ); // default is black
        assertNotNull( style );
        assertEquals( new Color( 255,255,00), style.getFill() );        
    }
    
    public void testCreateIncompletePointStyle() {
        NumberRange range = new NumberRange(1,1);
        
        PointSymbolizer symb;
        Mark myMark;
        //full symbolizer
        symb = styleFactory.createPointSymbolizer();
        myMark = styleFactory.createMark();        
        symb.getGraphic().addMark(myMark);        
        
        sldFactory.createPointStyle(null, symb,range);
    }
    
    /**
     * For GeoTools 2.5.x we have had a couple email reports
     * of styling not working when working from a parsed SLD
     * document.
     */
    public void testPointSymbolizerParsed() throws Exception {
        URL sld = SLDStyleFactoryTest.class.getResource("test-data/star.sld");
        SLDParser parser = new SLDParser( styleFactory );
        parser.setInput(sld);
        Style namedStyle = parser.readXML()[0];
        FeatureTypeStyle featureTypeStyle = namedStyle.getFeatureTypeStyles()[0];
        Rule rule = featureTypeStyle.getRules()[0];
        PointSymbolizer symbolizer = (PointSymbolizer) rule.getSymbolizers()[0];

        MarkStyle2D markStyle = (MarkStyle2D) sldFactory.createPointStyle(null, symbolizer, new NumberRange(1,1) );
        Color paint = (Color) markStyle.getFill();
        assertEquals( Color.RED.getRed(), paint.getRed() );
        
        AlphaComposite composite = (AlphaComposite) markStyle.getFillComposite();        
        assertEquals( 0.3f, composite.getAlpha() );
    }
    public void testPointSymbolizerCoded() throws Exception {
        Fill fill = styleFactory.createFill( ff.literal(Color.RED), ff.literal(0.3));
        PointSymbolizer symbolizer = styleFactory.getDefaultPointSymbolizer();
        Mark mark = styleFactory.createMark( ff.literal("start"), null, fill, ff.literal(5), ff.literal(0));
        symbolizer.getGraphic().addMark( mark );

        MarkStyle2D markStyle = (MarkStyle2D) sldFactory.createPointStyle(null, symbolizer, new NumberRange(1,1) );
        Color paint = (Color) markStyle.getFill();
        assertEquals( Color.RED.getRed(), paint.getRed() );

        AlphaComposite composite = (AlphaComposite) markStyle.getFillComposite();        
        assertEquals( 0.3f, composite.getAlpha() );
    }
}
