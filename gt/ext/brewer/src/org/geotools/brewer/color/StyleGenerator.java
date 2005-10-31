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

import java.awt.Color;
import java.util.Set;

import org.geotools.feature.FeatureCollection;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.Expression;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterType;
import org.geotools.filter.IllegalFilterException;
import org.geotools.filter.LogicFilter;
import org.geotools.filter.MathExpression;
import org.geotools.filter.function.ClassificationFunction;
import org.geotools.filter.function.EqualIntervalFunction;
import org.geotools.filter.function.UniqueIntervalFunction;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.StyleFactory;


/**
 * Generates a style using ColorBrewer
 * 
 * @author Cory Horner, Refractions Research
 */
public class StyleGenerator {
    private ColorBrewer colorBrewer;
    private String paletteName;
    private int numClasses;
    private Expression expression;
    private FeatureCollection collection;

    public StyleGenerator(ColorBrewer colorBrewer, String paletteName,
        int numClasses, Expression expression, FeatureCollection collection) {
        this.colorBrewer = colorBrewer;
        this.paletteName = paletteName;
        this.numClasses = numClasses;
        this.expression = expression;
        this.collection = collection;
    }

    public Style createStyle() {
        //make our factories and builders
        StyleBuilder sb = new StyleBuilder();
        FilterFactory ff = FilterFactory.createFilterFactory();
        StyleFactory sf = StyleFactory.createStyleFactory();

        //create our classifier
        ClassificationFunction classifier = null;

        if (colorBrewer.getLegendType().equals(ColorBrewer.SEQUENTIAL)) {
            classifier = new EqualIntervalFunction();
        } else if (colorBrewer.getLegendType().equals(ColorBrewer.DIVERGING)) {
            classifier = new EqualIntervalFunction();
        } else if (colorBrewer.getLegendType().equals(ColorBrewer.QUALITATIVE)) {
        	classifier = new UniqueIntervalFunction();
        } else {
            return null;
        }

        classifier.setNumberOfClasses(numClasses);
        classifier.setExpression(expression);
        classifier.setCollection(collection);

        //extract the palette
        int classNum = classifier.getNumberOfClasses();
        BrewerPalette pal = colorBrewer.getPalette(paletteName);
        Color[] colors = pal.getColors(classNum);

        //play with styles
        FeatureTypeStyle fts = sf.createFeatureTypeStyle();

        if (colorBrewer.getLegendType().equals(ColorBrewer.SEQUENTIAL) || 
        		colorBrewer.getLegendType().equals(ColorBrewer.DIVERGING)) {
        	//TODO: create a ClassicationFunction for Diverging sets
        	EqualIntervalFunction eiClassifier = (EqualIntervalFunction) classifier;
        	
        	//TODO: don't assume double
	        Object localMin = null;
	        Object localMax = null;
	
	        //for each class
	        for (int i = 0; i < numClasses; i++) {
	            //obtain min/max values
	        	localMin = eiClassifier.getMin(i);
	        	localMax = eiClassifier.getMax(i);
	
	            System.out.println("min="+localMin);
	            System.out.println("max="+localMax);
	
	            // construct a filter (this was a between filter, but now it's a
				// LogicFilter + >= + <
	            LogicFilter andFilter = null;
	            CompareFilter gteFilter = null;  //greater than or equal
	            CompareFilter ltFilter = null; //less than 
	            MathExpression math;
	            try {
					gteFilter = ff.createCompareFilter(CompareFilter.COMPARE_GREATER_THAN_EQUAL);
					gteFilter.addLeftValue(ff.createLiteralExpression(localMin));
					gteFilter.addRightValue(expression); 
					ltFilter = ff.createCompareFilter(CompareFilter.COMPARE_LESS_THAN);
					ltFilter.addLeftValue(expression);
					ltFilter.addRightValue(ff.createLiteralExpression(localMax));
					andFilter = ff.createLogicFilter(ltFilter, gteFilter, LogicFilter.LOGIC_AND);
				} catch (IllegalFilterException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					return null;
				}
	
	            //construct a symbolizer
	            PolygonSymbolizer ps = sb.createPolygonSymbolizer(colors[i]);
	
	            //create a rule
	            Rule rule = sb.createRule(ps);
	            rule.setFilter(andFilter);
	            fts.addRule(rule);
	        }
        } else if (colorBrewer.getLegendType().equals(ColorBrewer.QUALITATIVE)) {
	        UniqueIntervalFunction uniqueClassifier = (UniqueIntervalFunction) classifier;
        	CompareFilter filter = null;
        	PolygonSymbolizer ps = null;
        	Rule rule = null;
        	//for each class
	        for (int i = 0; i < numClasses; i++) {
	        	//obtain the set of values for the current bin
	            Set value = (Set) uniqueClassifier.getValue(i);
	            //create a rule for each unique value
	            Object[] items = value.toArray();
	            for (int item = 0; item < value.size(); item++) {
	            	//construct a filter
	            	try {
		                filter = ff.createCompareFilter(FilterType.COMPARE_EQUALS);
		                filter.addLeftValue(ff.createLiteralExpression(items[item]));
		                filter.addRightValue(expression); //the attribute we're looking at
		            } catch (IllegalFilterException e) {
		                // TODO Auto-generated catch block
		                e.printStackTrace();
		                return null;
		            }
		            //construct a symbolizer
		            ps = sb.createPolygonSymbolizer(colors[i]);
		            //create a rule
		            rule = sb.createRule(ps);
		            rule.setFilter(filter);
		            fts.addRule(rule);
	            }	
	        }
        }

        //create the style
        Style style = sf.createStyle();
        style.addFeatureTypeStyle(fts);

        return style;
    }
}
