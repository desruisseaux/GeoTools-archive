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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.Expression;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.FilterType;
import org.geotools.filter.IllegalFilterException;
import org.geotools.filter.LogicFilter;
import org.geotools.filter.function.ClassificationFunction;
import org.geotools.filter.function.EqualIntervalFunction;
import org.geotools.filter.function.QuantileFunction;
import org.geotools.filter.function.UniqueIntervalFunction;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.Rule;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.StyleFactoryFinder;
import org.geotools.styling.Symbolizer;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Generates a style/featureTypeStyle using ColorBrewer.
 * <br>
 * WARNING: this is unstable and subject to change.
 *
 * @author Cory Horner, Refractions Research Inc.
 */
public class StyleGenerator {
    private ColorBrewer colorBrewer;
    private Color[] colors;
    private String paletteName;
    private int numClasses;
    private Expression expression;
    private FeatureCollection collection;
    private FilterFactory ff;
    private StyleFactory sf;
    private StyleBuilder sb;
    private ClassificationFunction function;
    private double opacity = 0.5;
    private Stroke defaultStroke;
    private String id;
    
    /**
     * @deprecated
     * 
     * @param colorBrewer
     * @param paletteName
     * @param numClasses
     * @param expression
     * @param collection
     */
    public StyleGenerator(ColorBrewer colorBrewer, String paletteName,
        int numClasses, Expression expression, FeatureCollection collection) {
        this.colorBrewer = colorBrewer;
        this.paletteName = paletteName;
        this.numClasses = numClasses;
        this.expression = expression;
        this.collection = collection;
        ff = FilterFactoryFinder.createFilterFactory();
        sf = StyleFactoryFinder.createStyleFactory();
        sb = new StyleBuilder(sf, ff);
        defaultStroke = sb.createStroke();
    }
    
    /**
     * 
     * @param colors 
     * @param paletteName
     * @param expression
     * @param collection 
     * @param function classificationFunction containing our desired breaks
     * @param id unique string to tag the FTS with
     */
    public StyleGenerator(Color[] colors, ClassificationFunction function, String id) {
        this.colors = colors;
        this.numClasses = function.getNumberOfClasses();
        this.expression = function.getExpression();
        this.collection = function.getCollection();
        this.function = function;
        this.id = id;
        ff = FilterFactoryFinder.createFilterFactory();
        sf = StyleFactoryFinder.createStyleFactory();
        sb = new StyleBuilder(sf, ff);
        defaultStroke = sb.createStroke();
    }

    public FeatureCollection getCollection() {
        return collection;
    }

    public void setCollection(FeatureCollection collection) {
        this.collection = collection;
    }

    public Color[] getColors() {
    	return colors;
    }
    
    public void setColors(Color[] colors) {
    	this.colors = colors;
    }
    
    /** @deprecated */
    public ColorBrewer getColorBrewer() {
        return colorBrewer;
    }

    /** @deprecated */
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

    public double getOpacity() {
        return opacity;
    }

    public void setOpacity(double opacity) {
        this.opacity = opacity;
    }

    public Stroke getDefaultStroke() {
        return defaultStroke;
    }

    public void setDefaultStroke(Stroke defaultStroke) {
        this.defaultStroke = defaultStroke;
    }

    /** @deprecated */
    public String getPaletteName() {
        return paletteName;
    }

    /** @deprecated */
    public void setPaletteName(String paletteName) {
        this.paletteName = paletteName;
    }

    public ClassificationFunction getClassifier() {
    	return function;
    }
    
    public void setClassifier(ClassificationFunction function) {
    	this.function = function;
    }
    
    public FeatureTypeStyle createFeatureTypeStyle() throws IllegalFilterException {
    	//answer goes here
    	FeatureTypeStyle fts = sf.createFeatureTypeStyle();
        // update the number of classes
        numClasses = function.getNumberOfClasses();

        // determine the geometry
        FeatureIterator it = collection.features();
        Feature firstFeature = it.next();
        Geometry geometry = firstFeature.getDefaultGeometry();

        //numeric
    	if (function instanceof EqualIntervalFunction) {
    		EqualIntervalFunction eiClassifier = (EqualIntervalFunction) function;

    		Object localMin = null;
    		Object localMax = null;

            // for each class
            for (int i = 0; i < numClasses; i++) {
                // obtain min/max values
                localMin = eiClassifier.getMin(i);
                localMax = eiClassifier.getMax(i);

                // 1.0 --> 1
                // (this makes our styleExpressions more readable. Note that the
                // filter always converts to double, so it doesn't care what we
                // do).
                localMin = chopInteger(localMin);
                localMax = chopInteger(localMax);

                // generate a title
                String title = localMin + " to " + localMax;

                // construct filters
                Filter filter = null;

                if (localMin == localMax) {
                    // build filter: =
                    CompareFilter eqFilter = ff.createCompareFilter(CompareFilter.COMPARE_EQUALS);
                    eqFilter.addLeftValue(expression);
                    eqFilter.addRightValue(ff.createLiteralExpression(localMax));
                    filter = eqFilter;
                } else {
                    // build filter: [min <= x] AND [x < max]
                    LogicFilter andFilter = null;
                    CompareFilter lowBoundFilter = null; // less than or

                    // equal
                    CompareFilter hiBoundFilter = null; // less than
                    lowBoundFilter = ff.createCompareFilter(CompareFilter.COMPARE_LESS_THAN_EQUAL);
                    lowBoundFilter.addLeftValue(ff.createLiteralExpression(
                            localMin)); // min
                    lowBoundFilter.addRightValue(expression); // x

                    // if this is the global maximum, include the max value
                    if (i == (numClasses - 1)) {
                        hiBoundFilter = ff.createCompareFilter(CompareFilter.COMPARE_LESS_THAN_EQUAL);
                    } else {
                        hiBoundFilter = ff.createCompareFilter(CompareFilter.COMPARE_LESS_THAN);
                    }

                    hiBoundFilter.addLeftValue(expression); // x
                    hiBoundFilter.addRightValue(ff.createLiteralExpression(
                            localMax)); // max
                    andFilter = ff.createLogicFilter(lowBoundFilter,
                            hiBoundFilter, LogicFilter.LOGIC_AND);
                    filter = andFilter;
                }

                // create a symbolizer
                Symbolizer symb = null;
                Color color = colors[i];
                symb = createSymbolizer(sb, geometry, color, opacity, defaultStroke);

                // create a rule
                Rule rule = sb.createRule(symb);
                rule.setFilter(filter);
                rule.setTitle(title);
                rule.setName(getRuleName(i + 1));
                fts.addRule(rule);
            }
        } else if (function instanceof UniqueIntervalFunction) {
            UniqueIntervalFunction uniqueClassifier = (UniqueIntervalFunction) function;
            LogicFilter orFilter = null;
            CompareFilter filter = null;
            Rule rule = null;

            // for each class
            for (int i = 0; i < numClasses; i++) {
                // obtain the set of values for the current bin
                Set value = (Set) uniqueClassifier.getValue(i);

                // create a sub filter for each unique value, and merge them
                // into the logic filter
                Object[] items = value.toArray();
                Arrays.sort(items);
                orFilter = ff.createLogicFilter(FilterType.LOGIC_OR);

                String title = "";

                for (int item = 0; item < items.length; item++) {
                    // construct a filter
                    try {
                        filter = ff.createCompareFilter(FilterType.COMPARE_EQUALS);
                        filter.addLeftValue(expression); // the attribute

                        // we're looking at
                        filter.addRightValue(ff.createLiteralExpression(
                                items[item]));
                    } catch (IllegalFilterException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();

                        return null;
                    }

                    // add to the title
                    title += items[item].toString();

                    if ((item + 1) != items.length) {
                        title += ", ";
                    }

                    // add the filter to the logicFilter
                    orFilter.addFilter(filter);
                }

                // create the symbolizer
                Symbolizer symb = null;
                Color color = colors[i];
                symb = createSymbolizer(sb, geometry, color, opacity,
                        defaultStroke);

                // create the rule
                rule = sb.createRule(symb);

                if (items.length > 1) {
                    rule.setFilter(orFilter);
                } else {
                    rule.setFilter(filter);
                }

                rule.setTitle(title);
                rule.setName(getRuleName(i + 1));
                fts.addRule(rule);
            }
        } else if (function instanceof QuantileFunction) {
        	QuantileFunction quantile = (QuantileFunction) function;

            // for each class
            for (int i = 0; i < numClasses; i++) {
                // obtain values
            	Object localMin = quantile.getMin(i);
                Object localMax = quantile.getMax(i);

                if (localMin instanceof Number && localMax instanceof Number) {
	                // 1.0 --> 1
	                // (this makes our styleExpressions more readable. Note that the
	                // filter always converts to double, so it doesn't care what we
	                // do).
	                localMin = chopInteger(localMin);
	                localMax = chopInteger(localMax);
                }
                
                // generate a title
                String title = localMin + " to " + localMax;

                // construct filters
                Filter filter = null;

                if (localMin == localMax) {
                    // build filter: =
                    CompareFilter eqFilter = ff.createCompareFilter(CompareFilter.COMPARE_EQUALS);
                    eqFilter.addLeftValue(expression);
                    eqFilter.addRightValue(ff.createLiteralExpression(localMax));
                    filter = eqFilter;
                } else {
                    // build filter: [min <= x] AND [x < max]
                    LogicFilter andFilter = null;
                    CompareFilter lowBoundFilter = null; // less than or

                    // equal
                    CompareFilter hiBoundFilter = null; // less than
                    lowBoundFilter = ff.createCompareFilter(CompareFilter.COMPARE_LESS_THAN_EQUAL);
                    lowBoundFilter.addLeftValue(ff.createLiteralExpression(
                            localMin)); // min
                    lowBoundFilter.addRightValue(expression); // x

                    // if this is the global maximum, include the max value
                    if (i == (numClasses - 1)) {
                        hiBoundFilter = ff.createCompareFilter(CompareFilter.COMPARE_LESS_THAN_EQUAL);
                    } else {
                        hiBoundFilter = ff.createCompareFilter(CompareFilter.COMPARE_LESS_THAN);
                    }

                    hiBoundFilter.addLeftValue(expression); // x
                    hiBoundFilter.addRightValue(ff.createLiteralExpression(
                            localMax)); // max
                    andFilter = ff.createLogicFilter(lowBoundFilter,
                            hiBoundFilter, LogicFilter.LOGIC_AND);
                    filter = andFilter;
                }

                // create a symbolizer
                Symbolizer symb = null;
                Color color = colors[i];
                symb = createSymbolizer(sb, geometry, color, opacity, defaultStroke);

                // create a rule
                Rule rule = sb.createRule(symb);
                rule.setFilter(filter);
                rule.setTitle(title);
                rule.setName(getRuleName(i + 1));
                fts.addRule(rule);
            }

        }

        // sort the FeatureTypeStyle rules
        Rule[] rule = fts.getRules();

        for (int i = 0; i < rule.length; i++) {
            String properRuleName = getRuleName(i + 1);

            if (!rule[i].getName().equals(properRuleName)) {
                // is in incorrect order, find where the rule for this index
                // actually is
                for (int j = i + 1; j < rule.length; j++) {
                    if (rule[j].getName().equals(properRuleName)) {
                        // switch the 2 rules
                        Rule tempRule = rule[i];
                        rule[i] = rule[j];
                        rule[j] = tempRule;

                        break;
                    }
                }
            }
        }
        
        //our syntax will be: ColorBrewer:id
        fts.setSemanticTypeIdentifiers(new String[] {"generic:geometry", "colorbrewer:"+id});

        return fts;
    }
    
    /**
     * @deprecated
     * @return
     * @throws IllegalFilterException
     */
    public Style createStyle() throws IllegalFilterException {
        // create our classifier
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

        // update the number of classes
        numClasses = classifier.getNumberOfClasses();

        // extract the palette
        BrewerPalette pal = colorBrewer.getPalette(paletteName);
        Color[] colors = pal.getColors(numClasses);

        // determine the geometry
        FeatureIterator it = collection.features();
        Feature firstFeature = it.next();
        Geometry geometry = firstFeature.getDefaultGeometry();

        // play with styles
        FeatureTypeStyle fts = sf.createFeatureTypeStyle();

        if (colorBrewer.getLegendType().equals(ColorBrewer.SEQUENTIAL)
                || colorBrewer.getLegendType().equals(ColorBrewer.DIVERGING)) {
            // TODO: create a ClassificationFunction for Diverging sets
            EqualIntervalFunction eiClassifier = (EqualIntervalFunction) classifier;

            Object localMin = null;
            Object localMax = null;

            // for each class
            for (int i = 0; i < numClasses; i++) {
                // obtain min/max values
                localMin = eiClassifier.getMin(i);
                localMax = eiClassifier.getMax(i);

                // 1.0 --> 1
                // (this makes our styleExpressions more readable. Note that the
                // filter always converts to double, so it doesn't care what we
                // do).
                localMin = chopInteger(localMin);
                localMax = chopInteger(localMax);

                // generate a title
                String title = localMin + " to " + localMax;

                // construct filters
                Filter filter = null;

                if (localMin == localMax) {
                    // build filter: =
                    CompareFilter eqFilter = ff.createCompareFilter(CompareFilter.COMPARE_EQUALS);
                    eqFilter.addLeftValue(expression);
                    eqFilter.addRightValue(ff.createLiteralExpression(localMax));
                    filter = eqFilter;
                } else {
                    // build filter: [min <= x] AND [x < max]
                    LogicFilter andFilter = null;
                    CompareFilter lowBoundFilter = null; // less than or

                    // equal
                    CompareFilter hiBoundFilter = null; // less than
                    lowBoundFilter = ff.createCompareFilter(CompareFilter.COMPARE_LESS_THAN_EQUAL);
                    lowBoundFilter.addLeftValue(ff.createLiteralExpression(
                            localMin)); // min
                    lowBoundFilter.addRightValue(expression); // x

                    // if this is the global maximum, include the max value
                    if (i == (numClasses - 1)) {
                        hiBoundFilter = ff.createCompareFilter(CompareFilter.COMPARE_LESS_THAN_EQUAL);
                    } else {
                        hiBoundFilter = ff.createCompareFilter(CompareFilter.COMPARE_LESS_THAN);
                    }

                    hiBoundFilter.addLeftValue(expression); // x
                    hiBoundFilter.addRightValue(ff.createLiteralExpression(
                            localMax)); // max
                    andFilter = ff.createLogicFilter(lowBoundFilter,
                            hiBoundFilter, LogicFilter.LOGIC_AND);
                    filter = andFilter;
                }

                // create a symbolizer
                Symbolizer symb = null;
                Color color = colors[i];
                symb = createSymbolizer(sb, geometry, color, opacity,
                        defaultStroke);

                // create a rule
                Rule rule = sb.createRule(symb);
                rule.setFilter(filter);
                rule.setTitle(title);
                rule.setName(getRuleName(i + 1));
                fts.addRule(rule);
            }
        } else if (colorBrewer.getLegendType().equals(ColorBrewer.QUALITATIVE)) {
            UniqueIntervalFunction uniqueClassifier = (UniqueIntervalFunction) classifier;
            LogicFilter orFilter = null;
            CompareFilter filter = null;
            Rule rule = null;

            // for each class
            for (int i = 0; i < numClasses; i++) {
                // obtain the set of values for the current bin
                Set value = (Set) uniqueClassifier.getValue(i);

                // create a sub filter for each unique value, and merge them
                // into the logic filter
                Object[] items = value.toArray();
                Arrays.sort(items);
                orFilter = ff.createLogicFilter(FilterType.LOGIC_OR);

                String title = "";

                for (int item = 0; item < items.length; item++) {
                    // construct a filter
                    try {
                        filter = ff.createCompareFilter(FilterType.COMPARE_EQUALS);
                        filter.addLeftValue(expression); // the attribute

                        // we're looking at
                        filter.addRightValue(ff.createLiteralExpression(
                                items[item]));
                    } catch (IllegalFilterException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();

                        return null;
                    }

                    // add to the title
                    title += items[item].toString();

                    if ((item + 1) != items.length) {
                        title += ", ";
                    }

                    // add the filter to the logicFilter
                    orFilter.addFilter(filter);
                }

                // create the symbolizer
                Symbolizer symb = null;
                Color color = colors[i];
                symb = createSymbolizer(sb, geometry, color, opacity,
                        defaultStroke);

                // create the rule
                rule = sb.createRule(symb);

                if (items.length > 1) {
                    rule.setFilter(orFilter);
                } else {
                    rule.setFilter(filter);
                }

                rule.setTitle(title);
                rule.setName(getRuleName(i + 1));
                fts.addRule(rule);
            }
        }

        // sort the FeatureTypeStyle rules
        Rule[] rule = fts.getRules();

        for (int i = 0; i < rule.length; i++) {
            String properRuleName = getRuleName(i + 1);

            if (!rule[i].getName().equals(properRuleName)) {
                // is in incorrect order, find where the rule for this index
                // actually is
                for (int j = i + 1; j < rule.length; j++) {
                    if (rule[j].getName().equals(properRuleName)) {
                        // switch the 2 rules
                        Rule tempRule = rule[i];
                        rule[i] = rule[j];
                        rule[j] = tempRule;

                        break;
                    }
                }
            }
        }

        // create the style
        Style style = sf.createStyle();
        style.addFeatureTypeStyle(fts);

        return style;
    }

    /**
     * Creates a symbolizer for the given geometry
     *
     * @param sb
     * @param geometry
     * @param color
     * @param opacity
     * @param defaultStroke stroke used for borders
     *
     * @return
     */
    private Symbolizer createSymbolizer(StyleBuilder sb, Geometry geometry,
        Color color, double opacity, Stroke defaultStroke) {
        Symbolizer symb;

        if (geometry instanceof MultiPolygon || geometry instanceof Polygon) {
            //symb = sb.createPolygonSymbolizer(color);
            Fill fill = sb.createFill(color, opacity);
            symb = sb.createPolygonSymbolizer(defaultStroke, fill);
        } else if (geometry instanceof LineString) {
            symb = sb.createLineSymbolizer(color);
        } else if (geometry instanceof MultiPoint || geometry instanceof Point) {
            Mark square = sb.createMark(StyleBuilder.MARK_SQUARE, color);
            Graphic graphic = sb.createGraphic(null, square, null); //, 1, 4, 0);
            symb = sb.createPointSymbolizer(graphic);

            //} else if (geometry instanceof ?Text) {
            //symb = sb.createTextSymbolizer(colors[i], ?, "");
            //} else if (geometry instanceof ?Raster) {
            //symb = sb.createRasterSymbolizer(?, ?);
        } else {
            //we don't know what the heck you are, *snip snip* you're a line.
            symb = sb.createLineSymbolizer(color);
        }

        return symb;
    }

    /**
     * Truncates an unneeded trailing decimal zero (1.0 --> 1) by converting to
     * an Integer object.
     *
     * @param value
     *
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
     *
     * @param count
     *
     * @return
     */
    private String getRuleName(int count) {
        String strVal = new Integer(count).toString();

        if (strVal.length() == 1) {
            return "rule0" + strVal;
        } else {
            return "rule" + strVal;
        }
    }
}
