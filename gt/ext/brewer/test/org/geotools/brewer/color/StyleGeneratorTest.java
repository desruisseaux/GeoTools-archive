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
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.IllegalFilterException;
import org.geotools.filter.expression.Expression;
import org.geotools.filter.expression.MathExpression;
import org.geotools.filter.function.ClassificationFunction;
import org.geotools.filter.function.EqualIntervalFunction;
import org.geotools.filter.function.UniqueIntervalFunction;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;


/**
 *
 * @source $URL$
 */
public class StyleGeneratorTest extends DataTestCase {
    public StyleGeneratorTest(String arg0) {
        super(arg0);
    }

    public void xtestSequential() throws Exception {
        System.out.println("Sequential");
        ColorBrewer brewer = new ColorBrewer();
        brewer.loadPalettes("SEQUENTIAL");

        FilterFactory ff = FilterFactoryFinder.createFilterFactory();
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
        
        //test the sort order of the items
        assertEquals("rule01", rule[0].getName());
        assertEquals("rule02", rule[1].getName());
    }

    public void xtestDiverging() throws Exception {
        System.out.println("Diverging");
    	ColorBrewer brewer = new ColorBrewer();
        brewer.loadPalettes("DIVERGING");

        FilterFactory ff = FilterFactoryFinder.createFilterFactory();
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

    public void xtestQualitative() throws Exception {
        System.out.println("Qualitative");
        ColorBrewer brewer = new ColorBrewer();
        brewer.loadPalettes("QUALITATIVE");

        FilterFactory ff = FilterFactoryFinder.createFilterFactory();
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
        	Symbolizer symb = rules[i].getSymbolizers()[0];
		if (symb instanceof PolygonSymbolizer) {
			PolygonSymbolizer ps = (PolygonSymbolizer) symb;
			colors.add(ps.getFill().getColor());
		} else if (symb instanceof LineSymbolizer) {
			LineSymbolizer ls = (LineSymbolizer) symb;
			colors.add(ls.getStroke().getColor());			
		} else if (symb instanceof PointSymbolizer) {
			PointSymbolizer ps = (PointSymbolizer) symb;
			//colors.add(ps.getFillColor());				
		}
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
        	Symbolizer symb = rules[i].getSymbolizers()[0];
		if (symb instanceof PolygonSymbolizer) {
			PolygonSymbolizer ps = (PolygonSymbolizer) symb;
			colors.add(ps.getFill().getColor());
		} else if (symb instanceof LineSymbolizer) {
			LineSymbolizer ls = (LineSymbolizer) symb;
			colors.add(ls.getStroke().getColor());			
		} else if (symb instanceof PointSymbolizer) {
			PointSymbolizer ps = (PointSymbolizer) symb;
			//colors.add(ps.getFillColor());				
		}
        }
        assertEquals(3, colors.size()); //# colors == # classes

        //test a case where there are more unique values than classes
        sg = new StyleGenerator(brewer, paletteName, 2, expr, fc);
        style = null;
        style = sg.createStyle();
        assertNotNull(style);
        assertEquals(2, style.getFeatureTypeStyles()[0].getRules().length);
        rules = style.getFeatureTypeStyles()[0].getRules();
        colors = new HashSet();
        for (int i = 0; i < rules.length; i++) {
        	Symbolizer symb = rules[i].getSymbolizers()[0];
		if (symb instanceof PolygonSymbolizer) {
			PolygonSymbolizer ps = (PolygonSymbolizer) symb;
			colors.add(ps.getFill().getColor());
		} else if (symb instanceof LineSymbolizer) {
			LineSymbolizer ls = (LineSymbolizer) symb;
			colors.add(ls.getStroke().getColor());			
		} else if (symb instanceof PointSymbolizer) {
			PointSymbolizer ps = (PointSymbolizer) symb;
			//colors.add(ps.getFillColor());				
		}
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
    
    public void testComplexExpression() throws Exception {
        System.out.println("Complex Expression (using Sequential)");
        ColorBrewer brewer = new ColorBrewer();
        brewer.loadPalettes();

        FilterFactory ff = FilterFactoryFinder.createFilterFactory();
        MathExpression expr = null;
        MathExpression expr2 = null;
        FeatureType type = roadType;
        String attribName = type.getAttributeType(0).getName();
        FeatureCollection fc = DataUtilities.collection(roadFeatures);
        FeatureSource fs = DataUtilities.source(fc);

        try {
            expr = ff.createMathExpression(MathExpression.MATH_MULTIPLY);
            expr.addLeftValue(ff.createAttributeExpression(type, attribName));
            expr.addRightValue(ff.createAttributeExpression(type, attribName));
            expr2 = ff.createMathExpression(MathExpression.MATH_ADD);
            expr2.addLeftValue(expr);
            expr2.addRightValue(ff.createLiteralExpression(3));
        } catch (IllegalFilterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String paletteName = "YlGn"; //type = Sequential

        //create the classification function
        ClassificationFunction classifier = new EqualIntervalFunction();
        classifier.setNumberOfClasses(2);
        classifier.setCollection(fc);
        classifier.setExpression(expr2);
        classifier.getValue(0); //recalc classes? (only useful for UniqueInterval) 

        //get the fts
        StyleGenerator sg = new StyleGenerator(brewer.getPalette(paletteName).getColors(2), classifier, "myfts");
        FeatureTypeStyle fts = sg.createFeatureTypeStyle();
        assertNotNull(fts);
        
        //test each filter
        Rule[] rule = fts.getRules();
        //do a preliminary test to make sure each rule's filter returns some results
        checkFilteredResultNotEmpty(rule, fs, attribName);
    }
}
