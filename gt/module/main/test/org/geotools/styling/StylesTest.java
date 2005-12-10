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
package org.geotools.styling;

import junit.framework.TestCase;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.filter.AttributeExpression;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.IllegalFilterException;
import org.geotools.filter.LogicFilter;
import java.awt.Color;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


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
        sf = StyleFactoryFinder.createStyleFactory();
        ff = FilterFactoryFinder.createFilterFactory();
        sb = new StyleBuilder();

        style = sf.createStyle();
        attribType = AttributeTypeFactory.newAttributeType("SPEED_LIMIT",
                Integer.class);
        assertNotNull(attribType);

        FeatureTypeFactory feaTypeFactory = FeatureTypeFactory.newInstance(
                "test");
        feaTypeFactory.addType(attribType);
        testSchema = feaTypeFactory.getFeatureType();

        //create explicit filter
        explicitFilter = ff.createCompareFilter(Filter.COMPARE_EQUALS);

        AttributeExpression attribExpr = ff.createAttributeExpression(testSchema,
                attribType.getName());

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

        rules[1] = sb.createRule(ps2);
        rules[1].setFilter(rangedFilter);

        style.addFeatureTypeStyle(sf.createFeatureTypeStyle(rules));
    }

    public void testGetRules() {
        Rule[] rule = Styles.getRules(style);
        assertNotNull(rule);
        assertEquals(2, rule.length);

        Set rules = new HashSet();
        rules.add(rule[0].getFilter().toString());
        rules.add(rule[1].getFilter().toString());
        assertTrue(rules.contains(
                "[[ 50 <= SPEED_LIMIT ] AND [ SPEED_LIMIT < 80 ]]"));
        assertTrue(rules.contains("[ SPEED_LIMIT = 90 ]"));
    }

    public void testGetColors_Rule() {
        Rule[] rule = Styles.getRules(style);
        String[] colors = Styles.getColors(rule[0]);
        assertNotNull(colors);
        assertEquals(1, colors.length);

        String[] colors2 = Styles.getColors(rule[1]);
        assertNotNull(colors2);
        assertEquals(1, colors2.length);

        if (colors[0].startsWith("#FF")) {
            assertEquals("#FF0000", colors[0]);
            assertEquals("#808080", colors2[0]);
        } else {
            assertEquals("#808080", colors[0]);
            assertEquals("#FF0000", colors2[0]);
        }
    }

    public void testGetColors_Style() {
        String[] color = Styles.getColors(style);
        assertNotNull(color);
        assertEquals(2, color.length);
    }

    public void testToFilter() throws IllegalFilterException {
        Filter filter = (Filter) Styles.toRangedFilter("40..50", testSchema,
                attribType.getName(), true);
        assertNotNull(filter);
        assertEquals("[[ 40 <= SPEED_LIMIT ] AND [ SPEED_LIMIT <= 50 ]]",
            filter.toString());

        Filter filter2 = (Filter) Styles.toExplicitFilter("40, 50,60",
                testSchema, attribType.getName());
        assertNotNull(filter2);
        assertEquals("[[ SPEED_LIMIT = 40 ] OR [ SPEED_LIMIT = 50 ] OR [ SPEED_LIMIT = 60 ]]",
            filter2.toString());

        Filter[] filters = Styles.toFilter(new String[] { "50..90", "90..110" },
                new FeatureType[] { testSchema, testSchema },
                new String[] { attribType.getName(), attribType.getName() });
        assertEquals(2, filters.length);
        assertEquals("[[ 50 <= SPEED_LIMIT ] AND [ SPEED_LIMIT < 90 ]]",
            filters[0].toString());
        assertEquals("[[ 90 <= SPEED_LIMIT ] AND [ SPEED_LIMIT <= 110 ]]",
            filters[1].toString());
    }

    public void testGetFilters_Style() {
        Filter[] filters = Styles.getFilters(style);
        assertEquals(2, filters.length);
    }

    public void testToRangedFilter() throws IllegalFilterException {
        Filter filter = (Filter) Styles.toRangedFilter("40..50", testSchema,
                attribType.getName(), true);
        assertNotNull(filter);
        assertEquals("[[ 40 <= SPEED_LIMIT ] AND [ SPEED_LIMIT <= 50 ]]",
            filter.toString());
    }

    public void testToStyleExpression() throws IllegalFilterException {
        Filter filter = (Filter) Styles.toRangedFilter("40..50", testSchema,
                attribType.getName(), true);
        String styleExpr = Styles.toStyleExpression(filter);
        assertEquals("40..50", styleExpr);

        Filter filter2 = (Filter) Styles.toExplicitFilter("40, 50,60",
                testSchema, attribType.getName());
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
    public void testToStyleExpression_FilterArray()
        throws IllegalFilterException {
        Filter newFilter1 = (Filter) Styles.toRangedFilter("40..50",
                testSchema, attribType.getName(), true);
        Filter newFilter2 = (Filter) Styles.toExplicitFilter("66", testSchema,
                attribType.getName());
        String[] styleExpr = Styles.toStyleExpression(new Filter[] {
                    newFilter1, newFilter2
                });
        assertEquals("40..50", styleExpr[0]);
        assertEquals("66", styleExpr[1]);
    }

    public void testModifyFTS() throws Exception {
        System.out.println("testModifyFTS_Ranged");

        FeatureTypeStyle fts = style.getFeatureTypeStyles()[0];
        System.out.println("old="
            + Styles.toStyleExpression(fts.getRules()[1].getFilter()));
        Styles.modifyFTS(fts, 1, "45..80");

        Rule[] newRules = fts.getRules();
        System.out.println("new="
            + Styles.toStyleExpression(newRules[1].getFilter()));
        assertEquals("45..80", Styles.toStyleExpression(newRules[1].getFilter()));
        assertTrue(newRules[1].getFilter().getFilterType() == Filter.LOGIC_AND);

        Iterator iterator = ((LogicFilter) newRules[1].getFilter())
            .getFilterIterator();

        // we're expecting 2 compare subfilters
        CompareFilter filter1 = (CompareFilter) iterator.next();
        CompareFilter filter2 = (CompareFilter) iterator.next();
        assertFalse(iterator.hasNext());

        //filter1
        System.out.println("filter1=" + filter1.toString());

        //filter2
        System.out.println("filter2=" + filter2.toString());

        System.out.println("testModifyFTS_Explicit");
        System.out.println("old="
            + Styles.toStyleExpression(fts.getRules()[0].getFilter()));
        Styles.modifyFTS(fts, 0, "90,91, 92, 93");
        newRules = fts.getRules();
        System.out.println("new="
            + Styles.toStyleExpression(newRules[0].getFilter()));
        assertEquals("90, 91, 92, 93",
            Styles.toStyleExpression(newRules[0].getFilter()));
    }

    public void testGetAttributeTypes() {
        //Styles.getAttributeTypes(Filter[], AttributeType[])
    }
}
