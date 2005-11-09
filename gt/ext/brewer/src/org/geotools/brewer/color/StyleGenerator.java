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
import java.util.Arrays;
import java.util.Set;

import org.geotools.feature.FeatureCollection;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.Expression;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterType;
import org.geotools.filter.IllegalFilterException;
import org.geotools.filter.LogicFilter;
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

    public FeatureCollection getCollection() {
		return collection;
	}

	public void setCollection(FeatureCollection collection) {
		this.collection = collection;
	}

	public ColorBrewer getColorBrewer() {
		return colorBrewer;
	}

	public void setColorBrewer(ColorBrewer colorBrewer) {
		this.colorBrewer = colorBrewer;
	}

	public Expression getExpression() {
		return expression;
	}

	public void setExpression(Expression expression) {
		this.expression = expression;
	}

	public int getNumClasses() {
		return numClasses;
	}

	public void setNumClasses(int numClasses) {
		this.numClasses = numClasses;
	}

	public String getPaletteName() {
		return paletteName;
	}

	public void setPaletteName(String paletteName) {
		this.paletteName = paletteName;
	}

	public StyleGenerator(ColorBrewer colorBrewer, String paletteName,
        int numClasses, Expression expression, FeatureCollection collection) {
        this.colorBrewer = colorBrewer;
        this.paletteName = paletteName;
        this.numClasses = numClasses;
        this.expression = expression;
        this.collection = collection;
    }

    public Style createStyle() throws IllegalFilterException {
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

        // attempt to calculate the first value (this is done so any changes to
        // the number of classes are made before we grab our colour data, etc)
        classifier.getValue(0);

        //extract the palette
        numClasses = classifier.getNumberOfClasses(); //update the number of classes

        BrewerPalette pal = colorBrewer.getPalette(paletteName);
        Color[] colors = pal.getColors(numClasses);

        //play with styles
        FeatureTypeStyle fts = sf.createFeatureTypeStyle();

        if (colorBrewer.getLegendType().equals(ColorBrewer.SEQUENTIAL)
                || colorBrewer.getLegendType().equals(ColorBrewer.DIVERGING)) {
            //TODO: create a ClassificationFunction for Diverging sets
            EqualIntervalFunction eiClassifier = (EqualIntervalFunction) classifier;

            Object localMin = null;
            Object localMax = null;

            //for each class
            for (int i = 0; i < numClasses; i++) {
                //obtain min/max values
                localMin = eiClassifier.getMin(i);
                localMax = eiClassifier.getMax(i);
                // 1.0 --> 1 
                //(this makes our styleExpressions more readable. Note that the
				//filter always converts to double, so it doesn't care what we do).
                localMin = chopInteger(localMin);
                localMax = chopInteger(localMax);
                
                //generate a title
                String title = localMin + ".." + localMax;

                //construct filters
                Filter filter = null;

                if (localMin == localMax) {
                    //build filter: =
                    CompareFilter eqFilter = ff.createCompareFilter(CompareFilter.COMPARE_EQUALS);
                    eqFilter.addLeftValue(expression);
                    eqFilter.addRightValue(ff.createLiteralExpression(localMax));
                    filter = eqFilter;
                } else {
                    //build filter: [min <= x] AND [x < max]
                    LogicFilter andFilter = null;
                    CompareFilter lowBoundFilter = null; //less than or equal
                    CompareFilter hiBoundFilter = null; //less than 
                    lowBoundFilter = ff.createCompareFilter(CompareFilter.COMPARE_LESS_THAN_EQUAL);
                    lowBoundFilter.addLeftValue(ff.createLiteralExpression(
                            localMin)); //min
                    lowBoundFilter.addRightValue(expression); //x
                    //if this is the global maximum, include the max value
                    if (i == numClasses - 1) {
                    	hiBoundFilter = ff.createCompareFilter(CompareFilter.COMPARE_LESS_THAN_EQUAL);
                    } else {
                    	hiBoundFilter = ff.createCompareFilter(CompareFilter.COMPARE_LESS_THAN);
                    }
                    hiBoundFilter.addLeftValue(expression); //x
                    hiBoundFilter.addRightValue(ff.createLiteralExpression(
                            localMax)); //max
                    andFilter = ff.createLogicFilter(lowBoundFilter,
                            hiBoundFilter, LogicFilter.LOGIC_AND);
                    filter = andFilter;
                }

                //construct a symbolizer
                PolygonSymbolizer ps = sb.createPolygonSymbolizer(colors[i]);

                //create a rule
                Rule rule = sb.createRule(ps);
                rule.setFilter(filter);
                rule.setTitle(title);
                rule.setName(getRuleName(i+1));
                fts.addRule(rule);
            }
        } else if (colorBrewer.getLegendType().equals(ColorBrewer.QUALITATIVE)) {
            UniqueIntervalFunction uniqueClassifier = (UniqueIntervalFunction) classifier;
            LogicFilter orFilter = null;
            CompareFilter filter = null;
            PolygonSymbolizer ps = null;
            Rule rule = null;

            //for each class
            for (int i = 0; i < numClasses; i++) {
                //obtain the set of values for the current bin
                Set value = (Set) uniqueClassifier.getValue(i);

                // create a sub filter for each unique value, and merge them
				// into the logic filter
                Object[] items = value.toArray();
                Arrays.sort(items);
                orFilter = ff.createLogicFilter(FilterType.LOGIC_OR);
                String title = "";
                
                for (int item = 0; item < items.length; item++) {
                    //construct a filter
                    try {
                        filter = ff.createCompareFilter(FilterType.COMPARE_EQUALS);
                        filter.addLeftValue(expression); //the attribute we're looking at
                        filter.addRightValue(ff.createLiteralExpression(
                                items[item]));
                    } catch (IllegalFilterException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();

                        return null;
                    }

                    //add to the title
                    title+=items[item].toString();
                    if (item + 1 != items.length) title+=", ";
                    
                    //add the filter to the logicFilter
                    orFilter.addFilter(filter);
                }

                //construct a symbolizer
                ps = sb.createPolygonSymbolizer(colors[i]);

                //create the rule
                rule = sb.createRule(ps);
                if (items.length > 1) {
                    rule.setFilter(orFilter);
                } else {
                    rule.setFilter(filter);
                }
                rule.setTitle(title);
                rule.setName(getRuleName(i+1));
                fts.addRule(rule);

            }
        }

        //create the style
        Style style = sf.createStyle();
        style.addFeatureTypeStyle(fts);

        return style;
    }
    
    /**
	 * Truncates an unneeded trailing decimal zero (1.0 --> 1) by converting to
	 * an Integer object.
	 * 
	 * @param value
	 * @return Integer(value) if applicable
	 */
    private Object chopInteger(Object value) {
    	if ((value instanceof Number) && (value.toString().endsWith(".0"))) {
    		return new Integer(((Number) value).intValue());
    	} else {
    		return value;
    	}
    }
    
    /**
     * Generates a quick name for each rule with a leading zero.
     * @param count
     * @return
     */
    private String getRuleName(int count) {
    	String strVal = new Integer(count).toString();
    	if (strVal.length() == 1) return "rule0"+strVal;
    	else return "rule"+strVal;
    }
}
