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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.filter.expression.AttributeExpression;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.FilterType;
import org.geotools.filter.IllegalFilterException;
import org.geotools.filter.expression.LiteralExpression;
import org.geotools.filter.LogicFilter;
import org.geotools.filter.expression.Expression;
import org.geotools.filter.function.ClassificationFunction;
import org.geotools.filter.function.CustomClassifierFunction;
import org.geotools.filter.function.ExplicitClassificationFunction;
import org.geotools.filter.function.RangedClassificationFunction;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.Rule;
import org.geotools.styling.Stroke;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.StyleFactoryFinder;
import org.geotools.styling.Symbolizer;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Generates a style/featureTypeStyle using ColorBrewer.
 * <br>
 * WARNING: this is unstable and subject to change.
 *
 * @author Cory Horner, Refractions Research Inc.
 * @source $URL$
 */
public class StyleGenerator {
	
	private static final java.util.logging.Logger LOGGER = java.util.logging.Logger
	.getLogger("org.geotools.brewer.color");

    private Color[] colors;
    private int numClasses;
    private Expression expression;
    private FeatureCollection collection;
    private FilterFactory ff;
    private StyleFactory sf;
    private StyleBuilder sb;
    private ClassificationFunction function;
    private double opacity = 0.5;
    private Stroke defaultStroke;
    private String typeId;
    private String titleSpacer = " to ";
    private int elseMode = ELSEMODE_IGNORE;
    
    public final static int ELSEMODE_IGNORE = 0;
    public final static int ELSEMODE_INCLUDEASMIN = 1;
    public final static int ELSEMODE_INCLUDEASMAX = 2;
    
    /**
     * Creates an instance of the StyleGenerator with the components it needs.
     *  
     * @param colors 
     * @param function classificationFunction containing our desired breaks
     * @param typeId "colorbrewer:"+typeId = SemanticTypeIdentifier
     */
    public StyleGenerator(Color[] colors, ClassificationFunction function, String typeId) {
        this.colors = colors;
        this.numClasses = function.getNumberOfClasses();
        this.expression = function.getExpression();
        this.collection = function.getCollection();
        this.function = function;
        this.typeId = typeId;
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

    public ClassificationFunction getClassifier() {
    	return function;
    }
    
    public void setClassifier(ClassificationFunction function) {
    	this.function = function;
    }
    
    /**
     * Sets the semantic type identifier, which will be prefixed with "colorbrewer:"
     * @param typeId
     */
    public void setTypeId(String typeId) {
    	this.typeId = typeId;
    }
    
    /**
     * Sets the text displayed between ranged values (by default " to ").
     * @param titleSpacer
     */
    public void setTitleSpacer(String titleSpacer) {
    	this.titleSpacer = titleSpacer;
    }
    
    public void setElseMode(int elseMode) {
    	this.elseMode = elseMode;
    }
    
    /**
	 * Obtains the colour for the indexed rule. If an else rule is also to be
	 * created from the colour palette, the appropriate offset is applied.
	 * 
	 * @param index
	 * @return
	 */
    private Color getColor(int index) {
    	if (elseMode == ELSEMODE_IGNORE) return colors[index];
    	else if (elseMode == ELSEMODE_INCLUDEASMIN) return colors[index+1];
    	else if (elseMode == ELSEMODE_INCLUDEASMAX) return colors[index];
    	else return null;
    }
    
    private Color getElseColor() {
    	if (elseMode == ELSEMODE_INCLUDEASMIN) return colors[0];
    	else if (elseMode == ELSEMODE_INCLUDEASMAX) return colors[colors.length-1];
    	else return null;
    }
    
    public FeatureTypeStyle createFeatureTypeStyle() throws IllegalFilterException {
    	//answer goes here
    	FeatureTypeStyle fts = sf.createFeatureTypeStyle();
        // update the number of classes
        if (elseMode == ELSEMODE_IGNORE) {
        	numClasses = function.getNumberOfClasses();
        } else {
        	numClasses = function.getNumberOfClasses() + 1;
        }

        // determine the geometry
        FeatureIterator it = collection.features();
        Feature firstFeature = it.next();
        Geometry geometry = firstFeature.getDefaultGeometry();
        it.close();
        
        //numeric
        if (function instanceof RangedClassificationFunction) {
        	RangedClassificationFunction ranged = (RangedClassificationFunction) function;
        	
    		Object localMin = null;
    		Object localMax = null;

            // for each class
            for (int i = 0; i < function.getNumberOfClasses(); i++) {
                // obtain min/max values
                localMin = ranged.getMin(i);
                localMax = ranged.getMax(i);
                Rule rule = createRuleRanged(localMin, localMax, geometry, i);
                fts.addRule(rule);
            }
        } else if (function instanceof ExplicitClassificationFunction) {
        	ExplicitClassificationFunction explicit = (ExplicitClassificationFunction) function;
            // for each class
            for (int i = 0; i < function.getNumberOfClasses(); i++) {
                Set value = (Set) explicit.getValue(i);
            	Rule rule = createRuleExplicit(value, geometry, i);
            	fts.addRule(rule);
            }
        } else if (function instanceof CustomClassifierFunction) {
        	CustomClassifierFunction custom = (CustomClassifierFunction) function;
        	// for each class
            for (int i = 0; i < function.getNumberOfClasses(); i++) {
                // obtain the set of values for the current bin
                Rule rule = null;
                if (custom.hasExplicit(i)) {
                	rule = createRuleExplicit((Set) custom.getValue(i), geometry, i);
                } else if (custom.hasRanged(i)) {
                	rule = createRuleRanged(custom.getMin(i), custom.getMax(i), geometry, i);
                }
                if (rule != null)
                	fts.addRule(rule);
            }
        } else {
        	LOGGER.log(Level.SEVERE, "Error: Classifier not found");
        }

    	// add an else rule to capture any missing features?
    	if (elseMode != ELSEMODE_IGNORE) {
    		Symbolizer symb = createSymbolizer(sb, geometry, getElseColor(), opacity, defaultStroke); 
	    	Rule elseRule = sb.createRule(symb);
	    	elseRule.setIsElseFilter(true);
	    	elseRule.setTitle("Else");
	    	elseRule.setName("else");
	    	fts.addRule(elseRule);
    	}
    	
        // sort the FeatureTypeStyle rules
        Rule[] rule = fts.getRules();
        
        if (elseMode == ELSEMODE_INCLUDEASMIN) {
        	//move last rule to the front
        	for (int i = rule.length-1; i > 0; i--) {
        		Rule tempRule = rule[i];
                rule[i] = rule[i-1];
                rule[i-1] = tempRule;
        	}
        }
        
        //our syntax will be: ColorBrewer:id
        fts.setSemanticTypeIdentifiers(new String[] {"generic:geometry", "colorbrewer:"+typeId});

        return fts;
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

    private Rule createRuleRanged(Object localMin, Object localMax, Geometry geometry, int i) throws IllegalFilterException {
        // 1.0 --> 1
        // (this makes our styleExpressions more readable. Note that the
        // filter always converts to double, so it doesn't care what we
        // do).
        localMin = chopInteger(localMin);
        localMax = chopInteger(localMax);

        // generate a title
        String title = localMin + titleSpacer + localMax;

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
            if (i == (function.getNumberOfClasses() - 1)) {
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
        Symbolizer symb = createSymbolizer(sb, geometry, getColor(i), opacity, defaultStroke);

        // create a rule
        Rule rule = sb.createRule(symb);
        rule.setFilter(filter);
        rule.setTitle(title);
        rule.setName(getRuleName(i + 1));
        return rule;
    }
    
    private Rule createRuleExplicit(Set value, Geometry geometry, int i) {
        // create a sub filter for each unique value, and merge them
        // into the logic filter
        Object[] items = value.toArray();
        Arrays.sort(items);
        LogicFilter orFilter;
		try {
			orFilter = ff.createLogicFilter(FilterType.LOGIC_OR);
		} catch (IllegalFilterException e1) {
			LOGGER.log(Level.SEVERE, "Couldn't create filter", e1);
			return null;
		}
        CompareFilter filter = null;
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
            	LOGGER.log(Level.SEVERE, "Error during rule filter construction", e);
                return null;
            }

            // add to the title
            title += items[item].toString();

            if ((item + 1) != items.length) {
                title += ", ";
            }

            // add the filter to the logicFilter
            try {
				orFilter.addFilter(filter);
			} catch (IllegalFilterException e) {
				LOGGER.log(Level.SEVERE, "Couldn't add filter to logicFilter", e);
			}
        }

        // create the symbolizer
        Symbolizer symb = createSymbolizer(sb, geometry, getColor(i), opacity, defaultStroke);

        // create the rule
        Rule rule = sb.createRule(symb);

        if (items.length > 1) {
            rule.setFilter(orFilter);
        } else {
            rule.setFilter(filter);
        }

        rule.setTitle(title);
        rule.setName(getRuleName(i + 1));
        return rule;
    }
    
	public static void modifyFTS(FeatureTypeStyle fts, int ruleIndex, String styleExpression) throws IllegalFilterException {
		FilterFactory ff = FilterFactoryFinder.createFilterFactory();
		
		Rule[] rule = fts.getRules();
		Rule thisRule = rule[ruleIndex];
		Filter filter = thisRule.getFilter();
		short filterType = filter.getFilterType();
		if (filterType == Filter.LOGIC_AND) { //ranged expression
			//figure out the appropriate values
			String[] newValue = styleExpression.split("\\.\\."); //$NON-NLS-1$
			if (newValue.length != 2) {
				throw new IllegalArgumentException("StyleExpression has incorrect syntax; min..max expected.");
			}
			Iterator iterator = ((LogicFilter) filter).getFilterIterator();
			// we're expecting 2 compare subfilters
			CompareFilter filter1 = (CompareFilter) iterator.next();
			CompareFilter filter2 = (CompareFilter) iterator.next();
			if (iterator.hasNext())
				throw new IllegalArgumentException(
					"This method currently only supports logical filters with exactly 2 children.");
			//filter1 should be 1 <= x and filter2 should be x <(=) 5
			if (!(filter1.getRightValue().equals(filter2.getLeftValue()))) {
				throw new IllegalArgumentException("Subfilters or subExpressions in incorrect order");
			}
			if (filter1.getLeftValue().toString() != newValue[0]) {
				//lower bound value has changed, update
				filter1.addLeftValue(ff.createLiteralExpression(newValue[0]));
			}
			if (filter2.getRightValue().toString() != newValue[1]) {
				//upper bound value has changed, update
				filter2.addRightValue(ff.createLiteralExpression(newValue[1]));
			}
			thisRule.setFilter( filter ); // style events don't handle filters yet, so fire the change event for filter
			 
			//TODO: adjust the previous and next filters (uses isFirst, isLast)
		} else if ((filterType == Filter.LOGIC_OR) || (filterType == Filter.COMPARE_EQUALS)) { //explicit expression 
			//obtain the expression containing the attribute
			Expression attrExpression;
			if (filterType == Filter.LOGIC_OR) {
				Iterator iterator = ((LogicFilter) filter).getFilterIterator();
				attrExpression = ((CompareFilter) iterator.next()).getLeftValue();
			} else { //COMPARE_EQUALS (simple explicit expression)
				attrExpression = ((CompareFilter) filter).getLeftValue();
			}
			//recreate the filter with the new values
			rule[ruleIndex].setFilter(toExplicitFilter(styleExpression, attrExpression));
			//TODO: remove duplicate values from other filters
		} else {
			throw new IllegalArgumentException("Unrecognized filter type.");
		}
	}

	public static String toStyleExpression(Filter filter) {
		short filterType = filter.getFilterType();
		if (filterType == Filter.LOGIC_AND) { //looks like a ranged filter
			return toRangedStyleExpression((LogicFilter) filter);
		} else { //it's probably a filter with explicitly defined values
			return toExplicitStyleExpression(filter);
		}
	}

	public static String[] toStyleExpression(Filter[] filter) {
		String[] styleExpression = new String[filter.length];
		for (int i = 0; i < filter.length; i++) {
			styleExpression[i] = toStyleExpression(filter[i]);
		}
		return styleExpression;
	}

	/**
	 * <p>
	 * Converts an array of styleExpressions and attributes into Filters
	 * </p>
	 * <p>
	 * <code>styleExpression[0] = "1..5";</code><br>
	 * <code>styleExpression[1] = "5..10";</code><br>
	 * <code>styleExpression[2] = "11, -13";</code><br>
	 * <code>---></code><br>
	 * <code>filter[0] = [[1 <= attr] AND [attr < 5]]</code><br>
	 * <code>filter[1] = [[6 <= attr] AND [attr <= 10]]</code><br>
	 * <code>filter[2] = [[attr = 11] OR [attr = -13]]</code>
	 * </p>
	 * 
	 * @param styleExpression
	 *            strings of ranged expressions "lowValue..highValue" or
	 *            explicit values "value1, value2"
	 * @param attribute
	 * @return all the filters
	 * @throws IllegalFilterException
	 */
	public static Filter[] toFilter(String[] styleExpression,
			FeatureType[] featureType, String[] attributeTypeName) throws IllegalFilterException {
		Filter[] filter = new Filter[styleExpression.length];
		// prepare the styleExpressions (fix out if they are ranged, and if so
		// their min and max values too
		boolean[] isRangedExpr = new boolean[styleExpression.length];
		List min = new ArrayList();
		String[] max = new String[styleExpression.length];
		for (int i = 0; i < styleExpression.length; i++) {
			if (isRanged(styleExpression[i])) {
				isRangedExpr[i] = true;
				String[] exprPart = styleExpression[i].split("\\.\\."); //$NON-NLS-1$
				min.add(exprPart[0]);
				max[i] = exprPart[1];
			} else {
				isRangedExpr[i] = false;
			}
		}
		// create each filter
		for (int i = 0; i < styleExpression.length; i++) {
			// is it ranged or specific?
			if (isRangedExpr[i]) {
				boolean upperBoundClosed = true;
				// check for lower bounds of the same value as the current upper
				// bound
				if (min.contains(max[i])) {
					upperBoundClosed = false;
				}
				filter[i] = toRangedFilter(styleExpression[i], featureType[i], attributeTypeName[i],
						upperBoundClosed);
			} else { // specific
				filter[i] = toExplicitFilter(styleExpression[i], featureType[i], attributeTypeName[i]);
			}
		}
		return filter;
	}

	
	/**
	 * <p>
	 * Creates a filter for a range of values.
	 * </p>
	 * <p>
	 * Examples:<br>
	 * "1..5", closed=true --> [[1 <= attr] AND [attr <= 5]]<br>
	 * "1..10", closed=false --> [[1 <= attr] AND [attr < 10]]
	 * </p>
	 * 
	 * @param styleExpression
	 *            the ranged style expression (minValue..maxValue)
	 * @param attribute
	 *            the attributeType the values correspond to
	 * @param upperBoundClosed
	 *            is the upper bound include the max value? (true: <=, false: <)
	 * @return a filter
	 * @throws IllegalFilterException
	 */
	public static Filter toRangedFilter(String styleExpression,
			FeatureType featureType, String attributeTypeName, boolean upperBoundClosed)
			throws IllegalFilterException {
		FilterFactory ff = FilterFactoryFinder.createFilterFactory();
		AttributeExpression attrib = ff.createAttributeExpression(attributeTypeName);
		String[] strs = styleExpression.split("\\.\\."); //$NON-NLS-1$
		if (strs.length != 2) {
			throw new IllegalArgumentException(
					"A ranged filter could not be created from the styleExpression given.");
		}
		LiteralExpression localMin = ff.createLiteralExpression(strs[0]);
		LiteralExpression localMax = ff.createLiteralExpression(strs[1]);
		CompareFilter lowerBound = ff
				.createCompareFilter(Filter.COMPARE_LESS_THAN_EQUAL);
		lowerBound.addLeftValue(localMin);
		lowerBound.addRightValue(attrib);
		CompareFilter upperBound;
		if (upperBoundClosed) {
			upperBound = ff.createCompareFilter(Filter.COMPARE_LESS_THAN_EQUAL);
		} else {
			upperBound = ff.createCompareFilter(Filter.COMPARE_LESS_THAN);
		}
		upperBound.addLeftValue(attrib);
		upperBound.addRightValue(localMax);
		LogicFilter filter = ff.createLogicFilter(lowerBound, upperBound,
				Filter.LOGIC_AND);
		return filter;
	}

	/**
	 * <p>Converts a filter into a styleExpression with ranged values.</p>
	 * <p>Example:<br>
	 * <code>[[1 <= attr] AND [attr < 5]] --> "1..5"</code></p>
	 * 
	 * @param filter A LOGIC_AND filter containing 2 CompareFilters.
	 * @return a styleExpression of the syntax "min..max"
	 */
	private static String toRangedStyleExpression(LogicFilter filter) {
		if (filter.getFilterType() != Filter.LOGIC_AND) {
			throw new IllegalArgumentException(
					"Only logic filters constructed using the LOGIC_AND filterType are currently supported by this method.");
		}
		Iterator iterator = filter.getFilterIterator();
		// we're expecting 2 subfilters
		Filter filter1 = (Filter) iterator.next();
		Filter filter2 = (Filter) iterator.next();
		if (iterator.hasNext())
			throw new IllegalArgumentException(
				"This method currently only supports logical filters with exactly 2 children.");
		if (!(filter1 instanceof CompareFilter) || !(filter2 instanceof CompareFilter)) {
			throw new IllegalArgumentException(
				"Only compare filters as logical filter children are currently supported by this method.");
		}
		//find min and max values
		short filterType1 = filter1.getFilterType();
		short filterType2 = filter2.getFilterType();
		Expression min1; Expression min2;
		Expression max1; Expression max2;
		if ((filterType1 == Filter.COMPARE_LESS_THAN) || (filterType1 == Filter.COMPARE_LESS_THAN_EQUAL)) {
			min1 = ((CompareFilter) filter1).getLeftValue();
			max1 = ((CompareFilter) filter1).getRightValue();
		} else if ((filterType1 == Filter.COMPARE_GREATER_THAN) || (filterType1 == Filter.COMPARE_GREATER_THAN_EQUAL)) {
			min1 = ((CompareFilter) filter1).getRightValue();
			max1 = ((CompareFilter) filter1).getLeftValue();
		} else {
			throw new IllegalArgumentException("Unsupported FilterType");
		}
		if ((filterType2 == Filter.COMPARE_LESS_THAN) || (filterType1 == Filter.COMPARE_LESS_THAN_EQUAL)) {
			min2 = ((CompareFilter) filter2).getLeftValue();
			max2 = ((CompareFilter) filter2).getRightValue();
		} else if ((filterType2 == Filter.COMPARE_GREATER_THAN) || (filterType2 == Filter.COMPARE_GREATER_THAN_EQUAL)) {
			min2 = ((CompareFilter) filter2).getRightValue();
			max2 = ((CompareFilter) filter2).getLeftValue();
		} else {
			throw new IllegalArgumentException("Unsupported FilterType");
		}
		//look for 2 equal expressions
		if (max1.equals(min2)) {
			return min1.toString()+".."+max2.toString();
		} else if (max2.equals(min1)) {
			return min2.toString()+".."+max1.toString();
		} else {
			throw new IllegalArgumentException("Couldn't find the expected arrangement of Expressions");
		}
	}

	/**
	 * Determines if a string is an instance of a ranged expression or unique values.
	 */
	public static boolean isRanged(String styleExpression) {
		return styleExpression.matches(".+\\.{2}.+");
	}
	
	/**
	 * <p>
	 * Creates a filter with each value explicitly defined.
	 * </p>
	 * <p>
	 * Examples:<br>
	 * "LIB" --> [PARTY = LIB]<br>
	 * "LIB, NDP" --> [[PARTY = LIB] OR [PARTY = NDP]]
	 * </p>
	 * 
	 * @param styleExpression
	 *            the list of attribute values, separated by commas (and
	 *            optional spaces)
	 * @param attribute
	 *            the attributeType the values correspond to
	 * @return a filter
	 * @throws IllegalFilterException
	 */
	public static Filter toExplicitFilter(String styleExpression,
			FeatureType featureType, String attributeTypeName) throws IllegalFilterException {
		// eliminate spaces after commas
		String expr = styleExpression.replaceAll(",\\s+", ","); //$NON-NLS-1$//$NON-NLS-2$
		String[] attribValue = expr.split(","); //$NON-NLS-1$
		FilterFactory ff = FilterFactoryFinder.createFilterFactory();
		// create the first filter
		CompareFilter cFilter = ff.createCompareFilter(Filter.COMPARE_EQUALS);
		AttributeExpression attribExpr = ff.createAttributeExpression(attributeTypeName);
		cFilter.addLeftValue(attribExpr);
		cFilter.addRightValue(ff.createLiteralExpression(attribValue[0]));
		if (attribValue.length == 1) {
			return cFilter;
		}
		// more than one value exists, so wrap them inside a logical OR
		LogicFilter lFilter = ff.createLogicFilter(Filter.LOGIC_OR);
		lFilter.addFilter(cFilter);
		for (int i = 1; i < attribValue.length; i++) {
			cFilter = ff.createCompareFilter(Filter.COMPARE_EQUALS);
			cFilter.addLeftValue(attribExpr);
			cFilter.addRightValue(ff.createLiteralExpression(attribValue[i]));
			lFilter.addFilter(cFilter);
		}
		return lFilter;
	}

	/**
	 * <p>
	 * Creates a filter with each value explicitly defined.
	 * </p>
	 * <p>
	 * Examples:<br>
	 * "LIB" --> [PARTY = LIB]<br>
	 * "LIB, NDP" --> [[PARTY = LIB] OR [PARTY = NDP]]
	 * </p>
	 * 
	 * @param styleExpression
	 *            the list of attribute values, separated by commas (and
	 *            optional spaces)
	 * @param attribExpr 
	 *            an Expression to compare each value with (simple case = attributeExpression)
	 * @return a filter
	 * @throws IllegalFilterException
	 */
	public static Filter toExplicitFilter(String styleExpression, Expression attribExpr) throws IllegalFilterException {
		// eliminate spaces after commas
		String expr = styleExpression.replaceAll(",\\s+", ","); //$NON-NLS-1$//$NON-NLS-2$
		String[] attribValue = expr.split(","); //$NON-NLS-1$
		FilterFactory ff = FilterFactoryFinder.createFilterFactory();
		// create the first filter
		CompareFilter cFilter = ff.createCompareFilter(Filter.COMPARE_EQUALS);
		cFilter.addLeftValue(attribExpr);
		cFilter.addRightValue(ff.createLiteralExpression(attribValue[0]));
		if (attribValue.length == 1) {
			return cFilter;
		}
		// more than one value exists, so wrap them inside a logical OR
		LogicFilter lFilter = ff.createLogicFilter(Filter.LOGIC_OR);
		lFilter.addFilter(cFilter);
		for (int i = 1; i < attribValue.length; i++) {
			cFilter = ff.createCompareFilter(Filter.COMPARE_EQUALS);
			cFilter.addLeftValue(attribExpr);
			cFilter.addRightValue(ff.createLiteralExpression(attribValue[i]));
			lFilter.addFilter(cFilter);
		}
		return lFilter;
	}
	
	/**
	 * <p>
	 * Converts a filter into a styleExpression with explicitly defined values.
	 * </p>
	 * <p>
	 * Example:<br>
	 * <code>[[attr = 49] OR [attr = 92]] --> "49, 92"</code>
	 * </p>
	 * 
	 * @param filter
	 * @return
	 */
	private static String toExplicitStyleExpression(Filter filter) {
		short filterType = filter.getFilterType();
		String styleExpression = "";
		if (filterType == Filter.COMPARE_EQUALS) {
			// figure out which side is the attributeExpression, and which side
			// is the LiteralExpression
			CompareFilter compareFilter = (CompareFilter) filter;
			Expression leftExpression = compareFilter.getLeftValue();
			Expression rightExpression = compareFilter.getRightValue();
			if ((leftExpression instanceof AttributeExpression) && (rightExpression instanceof LiteralExpression)) {
				styleExpression = rightExpression.toString();
			} else if ((leftExpression instanceof LiteralExpression) && (rightExpression instanceof AttributeExpression)) {
				styleExpression = leftExpression.toString();
			} else {
				throw new IllegalArgumentException("Could not extract an Explicit Style Expression from the CompareFilter");
			}
				 
		} else if (filterType == Filter.LOGIC_OR) {
			// descend into the child elements of this filter
			LogicFilter parentFilter = (LogicFilter) filter;
			Iterator iterator = parentFilter.getFilterIterator();
			while (iterator.hasNext()) {
				// recursive call
				styleExpression+=toExplicitStyleExpression((Filter) iterator.next());
				if (iterator.hasNext()) {
					styleExpression+=", ";
				}
			}
		}
		return styleExpression;
	}
}
