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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.geotools.data.DataTestCase;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureSource;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.filter.Expression;
import org.geotools.filter.Filter;
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
        System.out.println("Sequential");
        ColorBrewer brewer = new ColorBrewer();
        brewer.loadPalettes(ColorBrewer.SEQUENTIAL);

        FilterFactory ff = FilterFactory.createFilterFactory();
        Expression expr = null;
        FeatureType type = roadType;
        String attribName = type.getAttributeType(0).getName();
        FeatureCollection fc = DataUtilities.collection(roadFeatures);
        FeatureSource fs = DataUtilities.source(fc);

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
        
        //test each filter
        Rule[] rule = style.getFeatureTypeStyles()[0].getRules();
        //do a preliminary test to make sure each rule's filter returns some results
        checkFilteredResultNotEmpty(rule, fs, attribName);
        //do a proper test to ensure that the results we get are dead on:
        
        //Filter "[[ id < 2.0 ] AND [ 1.0 >= id ]]" contains 1 element(s) ('1')
        Filter filter = rule[0].getFilter();
        FeatureCollection filteredCollection = fs.getFeatures(filter).collection();
        assertEquals(1, filteredCollection.size());
        FeatureIterator it = filteredCollection.features();
        Feature feature1 = it.next();
        assertEquals("1", feature1.getAttribute(attribName).toString());
        //Filter "[[ id < 3.0 ] AND [ 2.0 >= id ]]" contains 2 element(s) ('1', '2')
        filter = rule[1].getFilter();
        filteredCollection = fs.getFeatures(filter).collection();
        assertEquals(2, filteredCollection.size());
        it = filteredCollection.features();
        feature1 = it.next();
        Feature feature2 = it.next();
        assertEquals("2", feature1.getAttribute(attribName).toString());
        assertEquals("3", feature2.getAttribute(attribName).toString());
    }

    public void testDiverging() throws Exception {
        System.out.println("Diverging");
    	ColorBrewer brewer = new ColorBrewer();
        brewer.loadPalettes(ColorBrewer.DIVERGING);

        FilterFactory ff = FilterFactory.createFilterFactory();
        Expression expr = null;
        FeatureType type = roadType;
        String attribName = type.getAttributeType(0).getName();
        FeatureCollection fc = DataUtilities.collection(roadFeatures);
        FeatureSource fs = DataUtilities.source(fc);

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

        //test each filter
        Rule[] rule = style.getFeatureTypeStyles()[0].getRules();
        //do a preliminary test to make sure each rule's filter returns some results
        checkFilteredResultNotEmpty(rule, fs, attribName);
        //do a proper test to ensure that the results we get are dead on:
        
        //Filter "[[ id < 2.0 ] AND [ 1.0 >= id ]]" contains 1 element(s) ('1')
        Filter filter = rule[0].getFilter();
        FeatureCollection filteredCollection = fs.getFeatures(filter).collection();
        assertEquals(1, filteredCollection.size());
        FeatureIterator it = filteredCollection.features();
        Feature feature1 = it.next();
        assertEquals("1", feature1.getAttribute(attribName).toString());
        //Filter "[[ id < 3.0 ] AND [ 2.0 >= id ]]" contains 2 element(s) ('1', '2')
        filter = rule[1].getFilter();
        filteredCollection = fs.getFeatures(filter).collection();
        assertEquals(2, filteredCollection.size());
        it = filteredCollection.features();
        feature1 = it.next();
        Feature feature2 = it.next();
        assertEquals("2", feature1.getAttribute(attribName).toString());
        assertEquals("3", feature2.getAttribute(attribName).toString());
    }

    public void testQualitative() throws Exception {
        System.out.println("Qualitative");
        ColorBrewer brewer = new ColorBrewer();
        brewer.loadPalettes(ColorBrewer.QUALITATIVE);

        FilterFactory ff = FilterFactory.createFilterFactory();
        Expression expr = null;
        Style style = null;
        FeatureType type = roadType;
        String attribName = type.getAttributeType(2).getName();
        FeatureCollection fc = DataUtilities.collection(roadFeatures);
        FeatureSource fs = DataUtilities.source(fc);

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
        
        //test each filter
        Rule[] rule = style.getFeatureTypeStyles()[0].getRules();
        //do a preliminary test to make sure each rule's filter returns some results
        checkFilteredResultNotEmpty(rule, fs, attribName);
    }
    
    public void checkFilteredResultNotEmpty(Rule[] rule, FeatureSource fs, String attribName) throws IOException {
        for (int i = 0; i < rule.length; i++) {
        	Filter filter = rule[i].getFilter();
        	FeatureCollection filteredCollection = fs.getFeatures(filter).collection();
        	assertTrue(filteredCollection.size() > 0); 
        	String filterInfo = "Filter \""+filter.toString()+"\" contains "+filteredCollection.size()+" element(s) (";
        	FeatureIterator it = filteredCollection.features();
        	while (it.hasNext()) {
        		Feature feature = it.next();
        		filterInfo+="'"+feature.getAttribute(attribName)+"'";
        		if (it.hasNext()) filterInfo+=", ";
        	}
        	System.out.println(filterInfo+")");
        }
    }
}
