package org.geotools.styling;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.geotools.event.GTComponent;
import org.geotools.event.GTRoot;
import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureType;
import org.geotools.filter.AttributeExpression;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.Expression;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterAttributeExtractor;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.IllegalFilterException;
import org.geotools.filter.LiteralExpression;
import org.geotools.filter.LogicFilter;

/**
 * Utility class for Styles.
 * <p>
 * Warning: these methods are subject to change in the near future. 
 * <p>
 * @author Cory Horner, Refractions Research
 */
public class Styles {

	/** @deprecated */
	public static AttributeType[] getAttributeTypes(Filter[] filter, AttributeType[] availableAttributeTypes) throws IllegalFilterException {
		//visit each attribute in the filter and find the attributeType of any AttributeExpressions
		AttributeType[] attrib = new AttributeType[filter.length];
		for (int i = 0; i < filter.length; i++) {
			FilterAttributeExtractor filterVisitor = new FilterAttributeExtractor();
			filter[0].accept(filterVisitor);
			String[] attributeNames = filterVisitor.getAttributeNames();
			if (attributeNames.length != 1) {
				throw new IllegalFilterException("More than one attribute found in the filter");
			}
			for (int j = 0; j < availableAttributeTypes.length; j++) {
				if (availableAttributeTypes[j].getName().toLowerCase().equals(attributeNames[0].toLowerCase())) {
					attrib[i] = availableAttributeTypes[j];
					break;
				}
			}
		}
		return attrib;
	}
	
	public static String[] getColors(Rule rule) {
		Set colorSet = new HashSet();
		Symbolizer[] symbolizer = rule.getSymbolizers();
		for (int i = 0; i < symbolizer.length; i++) {
			if (symbolizer[i] instanceof PolygonSymbolizer) {
				PolygonSymbolizer symb = (PolygonSymbolizer) symbolizer[i];
				colorSet.add(symb.getFill().getColor().toString());
			} else if (symbolizer[i] instanceof LineSymbolizer) {
				LineSymbolizer symb = (LineSymbolizer) symbolizer[i];
				colorSet.add(symb.getStroke().getColor().toString());		
			} else if (symbolizer[i] instanceof PointSymbolizer) {
				PointSymbolizer symb = (PointSymbolizer) symbolizer[i];
				colorSet.add(symb.getGraphic().getMarks()[0].getFill().getColor().toString());	
			}
		}
		if (colorSet.size() > 0) {
			return toStringArray(colorSet.toArray());
		} else {
			return new String[0];
		}
	}

	public static String[] getColors(Style style) {
		Set colorSet = new HashSet();
		Rule[] rules = getRules(style);
		for (int i = 0; i < rules.length; i++) {
			String[] colors = getColors(rules[i]);
			for (int j = 0; j < colors.length; j++) {
				colorSet.add(colors[j]);
			}
		}
		if (colorSet.size() > 0) {
			return toStringArray(colorSet.toArray());
		} else {
			return new String[0];
		}
	}

	public static Color toColor(String htmlColor) {
		return new Color(Integer.parseInt(htmlColor.substring(1), 16));
	}
	
	public static Filter[] getFilters(Rule[] rule) {
		Filter[] filter = new Filter[rule.length];
		for (int i = 0; i < rule.length; i++) {
			filter[i] = rule[0].getFilter();
		}
		return filter;
	}
	
	public static Filter[] getFilters(Style style) {
		Rule[] rule = getRules(style);
		return getFilters(rule);
	}
	
	public static Rule[] getRules(Style style) {
		Set ruleSet = new HashSet();
		FeatureTypeStyle[] fts = style.getFeatureTypeStyles();
		for (int i = 0; i < fts.length; i++) {
			Rule[] ftsRules = fts[i].getRules();
			for (int j = 0; j < ftsRules.length; j++) {
				ruleSet.add(ftsRules[j]);
			}
		}
		if (ruleSet.size() > 0) {
			return toRuleArray(ruleSet.toArray());
		} else {
			return new Rule[0];
		}
	}
	
	
	/**
	 * Climbs the style hierarchy until null or an SLD is found.
	 * 
	 * @param object
	 * @return SLD
	 */
	public static StyledLayerDescriptor getStyledLayerDescriptor(Object gtComponent) {
		if (!(gtComponent instanceof GTComponent)) return null;
		GTComponent component = (GTComponent) gtComponent;
		while (component.getNote().getParent() != GTRoot.NO_PARENT) {
			component = component.getNote().getParent();
			if (component instanceof StyledLayerDescriptor) {
				return (StyledLayerDescriptor) component;
			}
		}
		return null;
	}
	
	private static boolean isRanged(String styleExpression) {
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
		AttributeExpression attribExpr = ff
				.createAttributeExpression(featureType, attributeTypeName);
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
	 * Converts a java.awt.Color into an HTML Colour
	 * 
	 * @param color
	 * @return HTML Color (fill) in hex #RRGGBB
	 */
	public static String toHTMLColor(Color color) {
		String red = "0" + Integer.toHexString(color.getRed());
		red = red.substring(red.length() - 2);
		String grn = "0" + Integer.toHexString(color.getGreen());
		grn = grn.substring(grn.length() - 2);
		String blu = "0" + Integer.toHexString(color.getBlue());
		blu = blu.substring(blu.length() - 2);
		return ("#" + red + grn + blu).toUpperCase();
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
		AttributeExpression attrib = ff.createAttributeExpression(featureType, attributeTypeName);
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

	private static Rule[] toRuleArray(Object[] object) {
		Rule[] result = new Rule[object.length];
		for (int i = 0; i < object.length; i++) {
			result[i] = (Rule) object[i];
		}
		return result;
	}

	private static String[] toStringArray(Object[] object) {
		String[] result = new String[object.length];
		for (int i = 0; i < object.length; i++) {
			result[i] = (String) object[i];
		}
		return result;
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
	
	public static void modifyFTS(FeatureTypeStyle fts, int ruleIndex, String styleExpression) throws IllegalFilterException {
		FilterFactory ff = FilterFactoryFinder.createFilterFactory();
		
		Rule[] rule = fts.getRules();
		Rule thisRule = rule[ruleIndex];
		Filter filter = thisRule.getFilter();
		short filterType = filter.getFilterType();
//		boolean isFirst = false;
//		if (ruleIndex == 1) isFirst = true;
//		boolean isLast = false;
//		if (ruleIndex == rule.length-1) isLast = true;
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
	
}
