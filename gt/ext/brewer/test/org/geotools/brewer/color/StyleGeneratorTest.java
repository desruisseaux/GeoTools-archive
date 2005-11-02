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
package org.geotools.brewer.color;

import java.util.HashSet;
import java.util.Set;

import org.geotools.data.DataTestCase;
import org.geotools.data.DataUtilities;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.filter.Expression;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.IllegalFilterException;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;


public class StyleGeneratorTest extends DataTestCase {
    public StyleGeneratorTest(String arg0) {
        super(arg0);
    }

    public void testSequential() throws Exception {
        ColorBrewer brewer = new ColorBrewer();
        brewer.loadPalettes(ColorBrewer.SEQUENTIAL);

        FilterFactory ff = FilterFactory.createFilterFactory();
        Expression expr = null;
        FeatureType type = roadType;
        String attribName = type.getAttributeType(0).getName();
        FeatureCollection fc = DataUtilities.collection(roadFeatures);

        try {
            expr = ff.createAttributeExpression(type, attribName);
        } catch (IllegalFilterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String paletteName = "YlGn"; //type = Sequential

        //get the style
        StyleGenerator sg = new StyleGenerator(brewer, paletteName, 2, expr, fc);
        Style style = sg.createStyle();
        assertNotNull(style);
        //String filter = style.getFeatureTypeStyles()[0].getRules()[0].getFilter().toString();
        // if we put in an int, we have to get an int back in the filter
		// expression (otherwise the style doesn't work and we won't know why)
        //assertEquals("[[ id < 2 ] AND [ 1 >= id ]]", filter);
        //System.out.println("Filter="+filter);
    }

    public void testDiverging() throws Exception {
        ColorBrewer brewer = new ColorBrewer();
        brewer.loadPalettes(ColorBrewer.DIVERGING);

        FilterFactory ff = FilterFactory.createFilterFactory();
        Expression expr = null;
        FeatureType type = roadType;
        String attribName = type.getAttributeType(0).getName();
        FeatureCollection fc = DataUtilities.collection(roadFeatures);

        try {
            expr = ff.createAttributeExpression(type, attribName);
        } catch (IllegalFilterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String paletteName = "BrBG"; //type = Diverging

        //get the style
        StyleGenerator sg = new StyleGenerator(brewer, paletteName, 2, expr, fc);
        Style style = sg.createStyle();
        assertNotNull(style);
        //String filter = style.getFeatureTypeStyles()[0].getRules()[0].getFilter().toString();
        // if we put in an int, we have to get an int back in the filter
		// expression (otherwise the style doesn't work and we won't know why)
        //assertEquals("[[ id < 2 ] AND [ 1 >= id ]]", filter);
        //System.out.println("Filter="+filter);
    }

    public void testQualitative() throws Exception {
        ColorBrewer brewer = new ColorBrewer();
        brewer.loadPalettes(ColorBrewer.QUALITATIVE);

        FilterFactory ff = FilterFactory.createFilterFactory();
        Expression expr = null;
        Style style = null;
        FeatureType type = roadType;
        String attribName = type.getAttributeType(2).getName();
        FeatureCollection fc = DataUtilities.collection(roadFeatures);

        try {
            expr = ff.createAttributeExpression(type, attribName);
        } catch (IllegalFilterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String paletteName = "Set3"; //type = Qualitative

        //test a typical case (#classes == #unique values)
        StyleGenerator sg = new StyleGenerator(brewer, paletteName, 3, expr, fc);
        style = null;
        style = sg.createStyle();
        assertNotNull(style);
        assertEquals(3, style.getFeatureTypeStyles()[0].getRules().length);
        Rule[] rules = style.getFeatureTypeStyles()[0].getRules();
        Set colors = new HashSet();
        for (int i = 0; i < rules.length; i++) {
        	PolygonSymbolizer ps = (PolygonSymbolizer) rules[i].getSymbolizers()[0];
        	colors.add(ps.getFill().getColor());
        }
        assertEquals(3, colors.size()); //# colors == # classes

        //test a case where there are more classes than unique values
        sg = new StyleGenerator(brewer, paletteName, 4, expr, fc);
        style = null;
        style = sg.createStyle();
        assertNotNull(style);
        assertEquals(3, style.getFeatureTypeStyles()[0].getRules().length);
        rules = style.getFeatureTypeStyles()[0].getRules();
        colors = new HashSet();
        for (int i = 0; i < rules.length; i++) {
        	PolygonSymbolizer ps = (PolygonSymbolizer) rules[i].getSymbolizers()[0];
        	colors.add(ps.getFill().getColor());
        }
        assertEquals(3, colors.size()); //# colors == # classes

        //test a case where there are more unique values than classes
        sg = new StyleGenerator(brewer, paletteName, 2, expr, fc);
        style = null;
        style = sg.createStyle();
        assertNotNull(style);
        assertEquals(3, style.getFeatureTypeStyles()[0].getRules().length); //three rules are created, even though there are only 2 classes
        rules = style.getFeatureTypeStyles()[0].getRules();
        colors = new HashSet();
        for (int i = 0; i < rules.length; i++) {
        	PolygonSymbolizer ps = (PolygonSymbolizer) rules[i].getSymbolizers()[0];
        	colors.add(ps.getFill().getColor());
        }
        assertEquals(2, colors.size()); //# colors == # classes
    }
}
