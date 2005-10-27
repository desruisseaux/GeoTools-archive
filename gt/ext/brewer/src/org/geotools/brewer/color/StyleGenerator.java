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
import java.util.HashSet;

import org.geotools.feature.FeatureCollection;
import org.geotools.filter.BetweenFilter;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.Expression;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterType;
import org.geotools.filter.IllegalFilterException;
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
        	EqualIntervalFunction eiClassifier = (EqualIntervalFunction) classifier;
        	
        	//TODO: don't assume double
	        double localMin = 0;
	        double localMax = 0;
	
	        //for each class
	        for (int i = 0; i < numClasses; i++) {
	            //obtain min/max values
	            Object range = eiClassifier.getRange(i);
	
	            if (range instanceof HashSet) {
	                HashSet rangeSet = (HashSet) range;
	
	                if (rangeSet.size() != 2) {
	                    return null;
	                }
	
	                Object[] minmax = rangeSet.toArray();
	                localMin = ((Double) minmax[0]).doubleValue();
	                localMax = ((Double) minmax[1]).doubleValue();
	
	                if (localMax < localMin) {
	                    double temp = localMin;
	                    localMin = localMax;
	                    localMax = temp;
	                }
	            } else {
	                return null;
	            }
	
	            System.out.println(localMin);
	            System.out.println(localMax);
	
	            //construct a filter
	            BetweenFilter filter = null;
	
	            try {
	                filter = ff.createBetweenFilter();
	                filter.addLeftValue(ff.createLiteralExpression(localMin));
	                filter.addMiddleValue(expression); //the attribute we're looking at
	                filter.addRightValue(ff.createLiteralExpression(localMax));
	            } catch (IllegalFilterException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
	
	                return null;
	            }
	
	            //construct a symbolizer
	            PolygonSymbolizer ps = sb.createPolygonSymbolizer(colors[i]);
	
	            //create a rule
	            Rule rule = sb.createRule(ps);
	            rule.setFilter(filter);
	            fts.addRule(rule);
	        }
        } else if (colorBrewer.getLegendType().equals(ColorBrewer.QUALITATIVE)) {
	        UniqueIntervalFunction uniqueClassifier = (UniqueIntervalFunction) classifier;
        	//for each class
	        for (int i = 0; i < numClasses; i++) {

	        	//obtain the current unique value
	            Object value = uniqueClassifier.getValue(i);
	
	            //TODO: check if a single value or multiple were returned
	            if (!((value instanceof String) || (value instanceof Double) || value instanceof Integer)) {
	            	System.out.println("UniqueVisitor value improperly handled...");
	            	return null;
	            }
	            
	            //construct a filter
	            CompareFilter filter = null;
	
	            try {
	                filter = ff.createCompareFilter(FilterType.COMPARE_EQUALS);
	                filter.addLeftValue(ff.createLiteralExpression(value));
	                filter.addRightValue(expression); //the attribute we're looking at
	            } catch (IllegalFilterException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
	
	                return null;
	            }
	
	            //construct a symbolizer
	            PolygonSymbolizer ps = sb.createPolygonSymbolizer(colors[i]);
	
	            //create a rule
	            Rule rule = sb.createRule(ps);
	            rule.setFilter(filter);
	            fts.addRule(rule);
	        }

        }

        //create the style
        Style style = sf.createStyle();
        style.addFeatureTypeStyle(fts);

        return style;
    }
}
