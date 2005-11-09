package org.geotools.styling;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.filter.AttributeExpression;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.IllegalFilterException;
import org.geotools.filter.LogicFilter;

public class StylesTest extends TestCase {
	
    private StyleFactory sf;
    private FilterFactory ff;
    private StyleBuilder sb;
    private Style style;
    private Rule[] rules;
    private CompareFilter explicitFilter;
    private LogicFilter rangedFilter;
    private AttributeType attribType;
    private FeatureType testSchema;
    
    public StylesTest(String arg0) {
        super(arg0);
    }

    protected void setUp() throws Exception {
        sf = StyleFactory.createStyleFactory();
        ff = FilterFactory.createFilterFactory();
        sb = new StyleBuilder();
        
        style = sf.createStyle();
        attribType = AttributeTypeFactory.newAttributeType("SPEED_LIMIT", Integer.class);
        assertNotNull(attribType);

    	FeatureTypeFactory feaTypeFactory = FeatureTypeFactory.newInstance("test");
    	feaTypeFactory.addType(attribType);
    	testSchema = feaTypeFactory.getFeatureType();

        //create explicit filter
        explicitFilter = ff.createCompareFilter(Filter.COMPARE_EQUALS);
        AttributeExpression attribExpr = ff.createAttributeExpression(testSchema, attribType.getName());
        //attribExpr.setAttributePath(attribType.getName());
        explicitFilter.addLeftValue(attribExpr);
        explicitFilter.addRightValue(ff.createLiteralExpression(90));
        
        //create ranged filter
        rangedFilter = ff.createLogicFilter(Filter.LOGIC_AND);
        CompareFilter minFilter = ff.createCompareFilter(Filter.COMPARE_LESS_THAN_EQUAL);
        minFilter.addLeftValue(ff.createLiteralExpression(50));
        minFilter.addRightValue(attribExpr);
        CompareFilter maxFilter = ff.createCompareFilter(Filter.COMPARE_LESS_THAN);
        maxFilter.addLeftValue(attribExpr);
        maxFilter.addRightValue(ff.createLiteralExpression(80));
        rangedFilter.addFilter(minFilter);
        rangedFilter.addFilter(maxFilter);
        
        //create polygonSymbolizers
        Color color1 = new Color(128, 128, 128); 
        Color color2 = new Color(255, 0, 0);
        PolygonSymbolizer ps1 = sb.createPolygonSymbolizer(color1);
        PolygonSymbolizer ps2 = sb.createPolygonSymbolizer(color2);
       
        //setup the rules
        rules = new Rule[2];
        rules[0] = sb.createRule(ps1);
        rules[0].setFilter(explicitFilter);
        rules[0].setSymbolizers(new Symbolizer[] {ps1});
        rules[1] = sb.createRule(ps2);
        rules[1].setFilter(rangedFilter);
        rules[1].setSymbolizers(new Symbolizer[] {ps2});
        style.addFeatureTypeStyle(sf.createFeatureTypeStyle(rules));
    }
	
	public void testGetRules() {
		Rule[] rule = Styles.getRules(style);
	    assertNotNull(rule);
	    assertEquals(2, rule.length);
	    Set rules = new HashSet();
	    rules.add(rule[0].getFilter().toString());
	    rules.add(rule[1].getFilter().toString());
	    assertTrue(rules.contains("[[ 50 <= SPEED_LIMIT ] AND [ SPEED_LIMIT < 80 ]]"));
	    assertTrue(rules.contains("[ SPEED_LIMIT = 90 ]"));
	}

	public void testGetColors_Rule() {
		Rule[] rule = Styles.getRules(style);
		Color[] colors = Styles.getColors(rule[0]);
		assertNotNull(colors);
		assertEquals(1, colors.length);
		Color[] colors2 = Styles.getColors(rule[1]);
		assertNotNull(colors2);
		assertEquals(1, colors2.length);
		String color0 = Styles.toHTMLColor(colors[0]); 
		String color1 = Styles.toHTMLColor(colors2[0]); 
		if (color0.startsWith("#FF")) {
			assertEquals("#FF0000", color0);
			assertEquals("#808080", color1);
		} else {
			assertEquals("#808080", color0);
			assertEquals("#FF0000", color1);
		}
	}
	
	public void testGetColors_Style() {
		Color[] color = Styles.getColors(style);
		assertNotNull(color);
		assertEquals(2, color.length);
	}
	
	public void testToFilter() throws IllegalFilterException {
		Filter filter = (Filter) Styles.toRangedFilter("40..50", testSchema, attribType.getName(), true);
		assertNotNull(filter);
		assertEquals("[[ 40 <= SPEED_LIMIT ] AND [ SPEED_LIMIT <= 50 ]]", filter.toString());

		Filter filter2 = (Filter) Styles.toExplicitFilter("40, 50,60", testSchema, attribType.getName());
		assertNotNull(filter2);
		assertEquals("[[ SPEED_LIMIT = 40 ] OR [ SPEED_LIMIT = 50 ] OR [ SPEED_LIMIT = 60 ]]", filter2.toString());

		Filter[] filters = Styles.toFilter(new String[] {"50..90","90..110"}, new FeatureType[] {testSchema, testSchema}, new String[] {attribType.getName(),attribType.getName()});
		assertEquals(2, filters.length);
		assertEquals("[[ 50 <= SPEED_LIMIT ] AND [ SPEED_LIMIT < 90 ]]", filters[0].toString());
		assertEquals("[[ 90 <= SPEED_LIMIT ] AND [ SPEED_LIMIT <= 110 ]]", filters[1].toString());
	}

	public void testGetFilters_Style() {
		Filter[] filters = Styles.getFilters(style);
		assertEquals(2, filters.length);
	}

	public void testToRangedFilter() throws IllegalFilterException {
		Filter filter = (Filter) Styles.toRangedFilter("40..50", testSchema, attribType.getName(), true);
		assertNotNull(filter);
		assertEquals("[[ 40 <= SPEED_LIMIT ] AND [ SPEED_LIMIT <= 50 ]]", filter.toString());
	}
	
	public void testToStyleExpression() throws IllegalFilterException {
		Filter filter = (Filter) Styles.toRangedFilter("40..50", testSchema, attribType.getName(), true);
		String styleExpr = Styles.toStyleExpression(filter);
		assertEquals("40..50", styleExpr);

		Filter filter2 = (Filter) Styles.toExplicitFilter("40, 50,60", testSchema, attribType.getName());
		styleExpr = Styles.toStyleExpression(filter2);
		assertEquals("40, 50, 60", styleExpr);
	}

	//overwriteFilters doesn't work, need to create an alternate method
	//public void testOverwriteFilters() throws IllegalFilterException {
	//		Filter[] filters = Styles.getFilters(style);
	//		Filter newFilter1 = (Filter) Styles.toRangedFilter("40..50", testSchema, attribType.getName(), true);
	//		Filter newFilter2 = (Filter) Styles.toExplicitFilter("66", testSchema, attribType.getName());
	//		Styles.overwriteFilters(filters, new Filter[] {newFilter1, newFilter2});
	//		Filter[] newFilters = Styles.getFilters(style);
	//		System.out.println(newFilters.length);
	//}
	
	public void testToStyleExpression_FilterArray() throws IllegalFilterException {
		Filter newFilter1 = (Filter) Styles.toRangedFilter("40..50", testSchema, attribType.getName(), true);
		Filter newFilter2 = (Filter) Styles.toExplicitFilter("66", testSchema, attribType.getName());
		String[] styleExpr = Styles.toStyleExpression(new Filter[] {newFilter1, newFilter2});
		assertEquals("40..50", styleExpr[0]);
		assertEquals("66", styleExpr[1]);
	}
	
	public void testGetAttributeTypes() {
		//Styles.getAttributeTypes(Filter[], AttributeType[])
	}
}
