/*
 * FilterBuilderTest.java
 * JUnit based test
 *
 * Created on 14 December 2004, 11:56
 */

package org.geotools.filter.cql;

import junit.framework.TestCase;

import org.geotools.filter.FilterFactoryImpl;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.PropertyIsLessThan;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.spatial.BBOX;
import org.opengis.filter.spatial.BinarySpatialOperator;
import org.opengis.filter.spatial.Contains;
import org.opengis.filter.spatial.Crosses;
import org.opengis.filter.spatial.Disjoint;
import org.opengis.filter.spatial.DistanceBufferOperator;
import org.opengis.filter.spatial.Equals;
import org.opengis.filter.spatial.Intersects;
import org.opengis.filter.spatial.Overlaps;
import org.opengis.filter.spatial.Touches;
import org.opengis.filter.spatial.Within;

/**
 * <b>FilterBuilderTest</b>
 * <p>
 * Unit test for cql.
 * </p>
 * <p>
 * This test was extended to cover all cql rules.
 * </p>
 * 
 * @author Created by: James
 * 
 * @author Extended by: Mauricio Pazos - Axios Engineering
 */
public class FilterBuilderTest extends TestCase {

    public static junit.framework.Test suite() {
        junit.framework.TestSuite suite = new junit.framework.TestSuite(
                FilterBuilderTest.class);

        return suite;
    }

    public FilterBuilderTest(String testName) {
        super(testName);
    }

    /**
     * Verify the parser uses the provided FilterFactory implementation
     * 
     * @throws ParseException
     */
    public void testUsesProvidedFilterFactory() throws ParseException{
        final boolean[] called = {false};
        FilterFactory ff = new FilterFactoryImpl(){
            public PropertyName property(String propName){
                called[0] = true;
                return super.property(propName);
            }
        };
        
        FilterBuilder.parse(ff, "attName > 20");
        assertTrue("Provided FilterFactory was not called", called[0]);
    }
    
    private void testEqualsExpressions(String inputExpression) throws Exception {

        Filter expected = FilterSample.getSample(inputExpression);
        Filter actual = FilterBuilder.parse(inputExpression);

        assertNotNull("expects filter not null", actual);
        assertEquals("greater with math expresion", expected, actual);

    }

    /**
     * Test Comparation Predicate
     * <p>
     * 
     * <pre>
     *   &lt;comparison predicate &gt; ::= 
     * 	       &lt;attribute name &gt;  &lt;comp op &gt;  &lt;literal &gt;
     * </pre>
     * 
     * </p>
     * 
     */
    public void testComparationPredicate() throws Exception {

        Filter expected;
        Filter actual;
        // attr1 < 5
        expected = FilterSample.getSample(FilterSample.LESS_FILTER_SAMPLE);

        actual = FilterBuilder.parse(FilterSample.LESS_FILTER_SAMPLE);

        assertNotNull("expects filter not null", actual);

        assertEquals("less than compare filter error", expected, actual);

        // attr1 <= 5
        expected = FilterSample.getSample(FilterSample.LESS_EQ_FILTER_SAMPLE);

        actual = FilterBuilder.parse(FilterSample.LESS_EQ_FILTER_SAMPLE);

        assertNotNull("expects filter not null", actual);

        assertEquals("less or equal compare filter error", expected, actual);

        // attr <> 5
        expected = FilterSample.getSample(FilterSample.NOT_EQ_FILTER);

        actual = FilterBuilder.parse(FilterSample.NOT_EQ_FILTER);

        assertNotNull("expects filter not null", actual);

        assertEquals("not equal compare filter error", expected, actual);

        // "gmd:aa:bb.gmd:cc.gmd:dd"
        final String prop = "gmd:aa:bb.gmd:cc.gmd:dd";
        final String propExpected = "gmd:aa:bb/gmd:cc/gmd:dd";
        actual = FilterBuilder.parse(prop + " < 100");

        assertTrue("PropertyIsLessThan filter was expected",
                actual instanceof PropertyIsLessThan);

        PropertyIsLessThan lessFilter = (PropertyIsLessThan) actual;
        Expression property = lessFilter.getExpression1();

        assertEquals(propExpected, property.toString());
    }

    /**
     * Test Comparation Predicate.
     * <p>
     * 
     * <pre>
     *    &lt;comparison predicate &gt; ::= 
     *   	    &lt;attrsibute name &gt;  &lt;comp op &gt;  &lt;literal &gt;
     * </pre>
     * 
     * </p>
     */
    public void testComparationPredicateWithSimpleExpressions()
            throws Exception {

        Filter expected;
        Filter actual;

        expected = FilterSample.getSample(FilterSample.FILTER_SIMPLE_EXPR);
        actual = FilterBuilder.parse(FilterSample.FILTER_SIMPLE_EXPR);

        assertNotNull("expects filter not null", actual);

        assertEquals("not equal compare filter error", expected, actual);

    }

    /**
     * Test of getFormattedErrorMessage method, of class
     * org.geotools.filter.ExpressionBuilder.
     */
    public void testGetFormattedErrorMessage() {
        final String exp = "12 / ] + 4";
        try {
            FilterBuilder.parse(exp);
            fail("expected ParseException");

        } catch (ParseException pe) {
            String error = FilterBuilder.getFormattedErrorMessage(pe, exp);
            assertFalse("".equals(error));
            // LOGGER.info(error);
        }
    }

    /**
     * Test Existnce Predicate.
     * <p>
     * EXIST: evaluates as true for all record instances where the
     * attribute_name is a member of the record schema. DOES-NOT-EXIST: opposite
     * to EXISTS
     * </p>
     * <p>
     * 
     * <pre>
     *   &lt;existence_predicate &gt; ::= 
     *  	  &lt;attribute_name &gt; EXISTS
     *  |  &lt;attribute_name &gt; DOES-NOT-EXIST
     * </pre>
     * 
     * </p>
     */
    public void testExistencePredicate() throws Exception {

        Filter resultFilter;
        Filter expected;
        PropertyIsEqualTo eqToResultFilter;

        // -------------------------------------------------------------
        // <attribute_name> DOES-NOT-EXIST
        // -------------------------------------------------------------

        resultFilter = FilterBuilder
                .parse(FilterSample.ATTRIBUTE_NAME_DOES_NOT_EXIST);

        assertNotNull("Filter expected", resultFilter);

        expected = FilterSample
                .getSample(FilterSample.ATTRIBUTE_NAME_DOES_NOT_EXIST);

        assertEquals(expected, resultFilter);

        // -------------------------------------------------------------
        // <attribute_name> EXISTS
        // -------------------------------------------------------------
        resultFilter = FilterBuilder.parse(FilterSample.ATTRIBUTE_NAME_EXISTS);

        assertNotNull("Filter expected", resultFilter);

        assertTrue(resultFilter instanceof PropertyIsEqualTo);

        eqToResultFilter = (PropertyIsEqualTo) resultFilter;

        expected = FilterSample.getSample(FilterSample.ATTRIBUTE_NAME_EXISTS);

        assertEquals(expected, eqToResultFilter);

        assertNotNull("implementation of function was expected",
                eqToResultFilter.getExpression1()); // TODO this function must
                                                    // be implemented in
                                                    // Geotools

    }

    /**
     * Test Null Predicate:
     * <p>
     * 
     * <pre>
     *   &lt;null predicate &gt; ::=  &lt;attribute name &gt; IS [ NOT ] NULL
     * </pre>
     * 
     * </p>
     */
    public void testNullPredicate() throws Exception {

        Filter expected;
        Filter resultFilter;
        // -------------------------------------------------------------
        // ATTR1 IS NULL
        // -------------------------------------------------------------
        expected = FilterSample.getSample(FilterSample.PROPERTY_IS_NULL);
        resultFilter = FilterBuilder.parse(FilterSample.PROPERTY_IS_NULL);

        assertNotNull("Filter expected", resultFilter);

        assertEquals("PropertyIsNull filter was expected", resultFilter,
                expected);

        // -------------------------------------------------------------
        // ATTR1 IS NOT NULL
        // -------------------------------------------------------------
        expected = FilterSample.getSample(FilterSample.PROPERTY_IS_NOT_NULL);
        resultFilter = FilterBuilder.parse(FilterSample.PROPERTY_IS_NOT_NULL);

        assertNotNull("Filter expected", resultFilter);

        assertEquals("Not PropertyIsNull filter was expected", resultFilter,
                expected);

    }

    public void testParenRoundtripExpression() throws Exception {

        // ATTR1 > ((1 + 2) / 3)
        testEqualsExpressions(FilterSample.FILTER_WITH_PAREN_ROUNDTRIP_EXPR);

        // "ATTR1 < (1 + ((2 / 3) * 4))"
        testEqualsExpressions(FilterSample.FILTER_WITH_NESTED_PAREN_EXPR);
    }

    public void testBracketRoundtripFilter() throws Exception {

        // ATTR1 > [[1 + 2] / 3]
        testEqualsExpressions(FilterSample.FILTER_WITH_BRACKET_ROUNDTRIP_EXPR);

        // TODO more test
        // roundtripFilter("[[[ 3 < 4 ] AND NOT [ 2 < 4 ]] AND [ 5 < 4 ]]");
        // roundtripFilter("[3<4 AND 2<4 ] OR 5<4");
        // roundtripFilter("3<4 && 2<4");
    }

    /**
     * Test temporal predicate. This test <b>BEFORE</b> rule [*]
     * <p>
     * 
     * <pre>
     *   &lt;temporal predicate  &gt;::= 
     *     &lt;attribute_name &gt; &lt;b&gt;BEFORE&lt;/b&gt;  &lt;date-time expression &gt; [*]
     *  |  &lt;attribute_name &gt; BEFORE OR DURING  &lt;period &gt;
     *  |  &lt;attribute_name &gt; DURING  &lt;period &gt;
     *  |  &lt;attribute_name &gt; DURING OR AFTER  &lt;period &gt;
     *  |  &lt;attribute_name &gt; AFTER  &lt;date-time expression &gt;     
     *  
     *   &lt;date-time expression &gt; ::=  &lt;date-time &gt; |  &lt;period &gt;[*]
     *  
     *   &lt;period &gt; ::= 
     *     &lt;date-time &gt; &quot;/&quot;  &lt;date-time &gt;[*]
     *  |  &lt;date-time &gt; &quot;/&quot;  &lt;duration &gt; [*]	
     *  |  &lt;duration &gt; &quot;/&quot;  &lt;date-time &gt; [*]
     * </pre>
     * 
     * </p>
     */
    public void testTemporalPredicateBefore() throws Exception {

        Filter resultFilter;
        Filter expected;
        // -------------------------------------------------------------
        // <attribute_name> BEFORE <date-time expression>
        // -------------------------------------------------------------
        // ATTR1 BEFORE 2006-12-31T01:30:00Z
        resultFilter = FilterBuilder.parse(FilterSample.FILTER_BEFORE_DATE);

        assertNotNull("not null expected", resultFilter);

        expected = FilterSample.getSample(FilterSample.FILTER_BEFORE_DATE);
        assertEquals("less filter ", expected, resultFilter);

        // ATTR1 BEFORE 2006-11-31T01:30:00Z/2006-12-31T01:30:00Z
        resultFilter = FilterBuilder
                .parse(FilterSample.FILTER_BEFORE_PERIOD_BETWEEN_DATES);

        assertNotNull("Filter expected", resultFilter);

        expected = FilterSample
                .getSample(FilterSample.FILTER_BEFORE_PERIOD_BETWEEN_DATES);

        assertEquals("less than first date of period ", expected, resultFilter);

        // ATTR1 BEFORE 2006-11-31T01:30:00Z/P30D
        resultFilter = (Filter) FilterBuilder
                .parse(FilterSample.FILTER_BEFORE_PERIOD_DATE_AND_DAYS);

        assertNotNull("Filter expected", resultFilter);

        expected = FilterSample
                .getSample(FilterSample.FILTER_BEFORE_PERIOD_DATE_AND_DAYS);

        assertEquals("less than first date of period ", expected, resultFilter);

        // "ATTR1 BEFORE 2006-11-31T01:30:00Z/P1Y"
        resultFilter = (Filter) FilterBuilder
                .parse(FilterSample.FILTER_BEFORE_PERIOD_DATE_AND_YEARS);

        assertNotNull("Filter expected", resultFilter);

        expected = FilterSample
                .getSample(FilterSample.FILTER_BEFORE_PERIOD_DATE_AND_YEARS);

        assertEquals("less than first date of period ", expected, resultFilter);

        // "ATTR1 BEFORE 2006-11-31T01:30:00Z/P12M"
        resultFilter = (Filter) FilterBuilder
                .parse(FilterSample.FILTER_BEFORE_PERIOD_DATE_AND_MONTHS);

        assertNotNull("Filter expected", resultFilter);

        expected = FilterSample
                .getSample(FilterSample.FILTER_BEFORE_PERIOD_DATE_AND_MONTHS);

        assertEquals("less than first date of period ", expected, resultFilter);

        // ATTR1 BEFORE P10Y10M10DT5H5M5S/2006-11-30T01:30:00Z
        resultFilter = (Filter) FilterBuilder
                .parse(FilterSample.FILTER_BEFORE_PERIOD_YMD_HMS_DATE);

        assertNotNull("Filter expected", resultFilter);

        expected = FilterSample
                .getSample(FilterSample.FILTER_BEFORE_PERIOD_YMD_HMS_DATE);

        assertEquals("greater filter", expected, resultFilter);

        // test compound attribute gmd:aa:bb.gmd:cc.gmd:dd
        final String prop = "gmd:aa:bb.gmd:cc.gmd:dd";
        final String propExpected = "gmd:aa:bb/gmd:cc/gmd:dd";
        resultFilter = FilterBuilder.parse(prop
                + " BEFORE P10Y10M10DT5H5M5S/2006-11-30T01:30:00Z ");

        assertTrue("PropertyIsLessThan filter was expected",
                resultFilter instanceof PropertyIsLessThan);

        PropertyIsLessThan lessFilter = (PropertyIsLessThan) resultFilter;
        Expression property = lessFilter.getExpression1();

        assertEquals(propExpected, property.toString());

    }

    /**
     * Test temporal predicate. This tests <b>BEFORE or DURING</b> rule[*]
     * <p>
     * 
     * <pre>
     *   &lt;temporal predicate  &gt;::= 
     *     &lt;attribute_name &gt; BEFORE  &lt;date-time expression &gt;
     *  | &lt;b&gt; &lt;attribute_name &gt; BEFORE OR DURING  &lt;period &gt;[*]&lt;/b&gt;
     *  |  &lt;attribute_name &gt; DURING  &lt;period &gt;
     *  |  &lt;attribute_name &gt; DURING OR AFTER  &lt;period &gt;
     *  |  &lt;attribute_name &gt; AFTER  &lt;date-time expression &gt;     
     *  
     *   &lt;date-time expression &gt; ::=  &lt;date-time &gt; |  &lt;period &gt;
     *  
     *   &lt;period &gt; ::= 
     *     &lt;date-time &gt; &quot;/&quot;  &lt;date-time &gt;[*]
     *  |  &lt;date-time &gt; &quot;/&quot;  &lt;duration &gt;[*]
     *  |  &lt;duration &gt; &quot;/&quot;  &lt;date-time &gt;[*]
     * </pre>
     * 
     * </p>
     */
    public void testTemporalPredicateBeforeOrDuring() throws Exception {

        Filter resultFilter;
        Filter expected;
        // -------------------------------------------------------------
        // <attribute_name> BEFORE OR DURING <period>
        // -------------------------------------------------------------
        // ATTR1 BEFORE OR DURING 2006-11-31T01:30:00Z/2006-12-31T01:30:00Z
        resultFilter = FilterBuilder
                .parse(FilterSample.FILTER_BEFORE_OR_DURING_PERIOD_BETWEEN_DATES);

        assertNotNull("Filter expected", resultFilter);

        expected = FilterSample
                .getSample(FilterSample.FILTER_BEFORE_OR_DURING_PERIOD_BETWEEN_DATES);

        assertEquals("less than or equal the last date of period ", expected,
                resultFilter);

        // ATTR1 BEFORE OR DURING P10Y10M10DT5H5M5S/2006-11-30T01:30:00Z
        resultFilter = (Filter) FilterBuilder
                .parse(FilterSample.FILTER_BEFORE_OR_DURING_PERIOD_YMD_HMS_DATE);

        assertNotNull("Filter expected", resultFilter);

        expected = FilterSample
                .getSample(FilterSample.FILTER_BEFORE_OR_DURING_PERIOD_YMD_HMS_DATE);

        assertEquals(" filter", expected, resultFilter);

        // ATTR1 BEFORE OR DURING 2006-11-30T01:30:00Z/P10Y10M10DT5H5M5S
        resultFilter = (Filter) FilterBuilder
                .parse(FilterSample.FILTER_BEFORE_OR_DURING_PERIOD_DATE_YMD_HMS);

        assertNotNull("Filter expected", resultFilter);

        expected = FilterSample
                .getSample(FilterSample.FILTER_BEFORE_OR_DURING_PERIOD_DATE_YMD_HMS);

        assertEquals(" filter", expected, resultFilter);

    }

    /**
     * Test temporal predicate. This tests <b>DURING OR AFTER</b> rule[*]
     * <p>
     * 
     * <pre>
     *   &lt;temporal predicate  &gt;::= 
     *     &lt;attribute_name &gt; BEFORE  &lt;date-time expression &gt;
     *  | &lt;b&gt; &lt;attribute_name &gt; BEFORE OR DURING  &lt;period &gt;&lt;/b&gt;
     *  |  &lt;attribute_name &gt; DURING  &lt;period &gt;
     *  |  &lt;attribute_name &gt; DURING OR AFTER  &lt;period &gt;[*]
     *  |  &lt;attribute_name &gt; AFTER  &lt;date-time expression &gt;     
     *  
     *   &lt;date-time expression &gt; ::=  &lt;date-time &gt; |  &lt;period &gt;
     *  
     *   &lt;period &gt; ::= 
     *     &lt;date-time &gt; &quot;/&quot;  &lt;date-time &gt;[*]
     *  |  &lt;date-time &gt; &quot;/&quot;  &lt;duration &gt;[*]
     *  |  &lt;duration &gt; &quot;/&quot;  &lt;date-time &gt;[*]
     * </pre>
     * 
     * </p>
     */
    public void testTemporalPredicateDuringOrAfter() throws Exception {

        Filter resultFilter;
        Filter expected;
        // -------------------------------------------------------------
        // <attribute_name> BEFORE OR DURING <period>
        // -------------------------------------------------------------
        // ATTR1 DURING OF AFTER 2006-11-31T01:30:00Z/2006-12-31T01:30:00Z
        resultFilter = FilterBuilder
                .parse(FilterSample.FILTER_DURING_OR_AFTER_PERIOD_BETWEEN_DATES);

        assertNotNull("Filter expected", resultFilter);

        expected = FilterSample
                .getSample(FilterSample.FILTER_DURING_OR_AFTER_PERIOD_BETWEEN_DATES);

        assertEquals("greater than or equal the first date of period ",
                expected, resultFilter);

        // ATTR1 DURING OR AFTER P10Y10M10DT5H5M5S/2006-11-30T01:30:00Z
        resultFilter = (Filter) FilterBuilder
                .parse(FilterSample.FILTER_DURING_OR_AFTER_PERIOD_YMD_HMS_DATE);

        assertNotNull("Filter expected", resultFilter);

        expected = FilterSample
                .getSample(FilterSample.FILTER_DURING_OR_AFTER_PERIOD_YMD_HMS_DATE);

        assertEquals(
                "greater than or equal the first date (is calculated subtract period to last date) of period",
                expected, resultFilter);

        // ATTR1 DURING OR AFTER 2006-11-30T01:30:00Z/P10Y10M10DT5H5M5S
        resultFilter = (Filter) FilterBuilder
                .parse(FilterSample.FILTER_DURING_OR_AFTER_PERIOD_DATE_YMD_HMS);

        assertNotNull("Filter expected", resultFilter);

        expected = FilterSample
                .getSample(FilterSample.FILTER_DURING_OR_AFTER_PERIOD_DATE_YMD_HMS);

        assertEquals("greater than or equal the first date", expected,
                resultFilter);

    }

    /**
     * Test temporal predicate. This tests <b>DURING</b> rule[*]
     * <p>
     * 
     * <pre>
     *   &lt;temporal predicate  &gt;::= 
     *     &lt;attribute_name &gt; BEFORE  &lt;date-time expression &gt;
     *  | &lt;b&gt; &lt;attribute_name &gt; BEFORE OR DURING  &lt;period &gt;&lt;/b&gt;
     *  |  &lt;attribute_name &gt; DURING  &lt;period &gt;[*]
     *  |  &lt;attribute_name &gt; DURING OR AFTER  &lt;period &gt;
     *  |  &lt;attribute_name &gt; AFTER  &lt;date-time expression &gt;     
     *  
     *   &lt;date-time expression &gt; ::=  &lt;date-time &gt; |  &lt;period &gt;
     *  
     *   &lt;period &gt; ::= 
     *     &lt;date-time &gt; &quot;/&quot;  &lt;date-time &gt;[*]
     *  |  &lt;date-time &gt; &quot;/&quot;  &lt;duration &gt;[*]
     *  |  &lt;duration &gt; &quot;/&quot;  &lt;date-time &gt;[*]
     * </pre>
     * 
     * </p>
     */
    public void testTemporalPredicateDuring() throws Exception {

        Filter resultFilter;
        Filter expected;

        // ATTR1 DURING 2006-11-30T01:30:00Z/2006-12-31T01:30:00Z
        resultFilter = FilterBuilder
                .parse(FilterSample.FILTER_DURING_PERIOD_BETWEEN_DATES);

        assertNotNull("Filter expected", resultFilter);

        expected = FilterSample
                .getSample(FilterSample.FILTER_DURING_PERIOD_BETWEEN_DATES);

        assertEquals("greater filter ", expected, resultFilter);

        // ATTR1 DURING 2006-11-30T01:30:00Z/P10Y10M10DT5H5M5S
        resultFilter = (Filter) FilterBuilder
                .parse(FilterSample.FILTER_DURING_PERIOD_DATE_YMD_HMS);

        assertNotNull("Filter expected", resultFilter);

        expected = FilterSample
                .getSample(FilterSample.FILTER_DURING_PERIOD_DATE_YMD_HMS);

        assertEquals("greater filter", expected, resultFilter);

        // ATTR1 DURING P10Y10M10DT5H5M5S/2006-11-30T01:30:00Z
        resultFilter = (Filter) FilterBuilder
                .parse(FilterSample.FILTER_DURING_PERIOD_YMD_HMS_DATE);

        assertNotNull("Filter expected", resultFilter);

        expected = FilterSample
                .getSample(FilterSample.FILTER_DURING_PERIOD_YMD_HMS_DATE);

        assertEquals("greater filter", expected, resultFilter);

    }

    /**
     * Test temporal predicate. This tests <B>AFTER</B> or during rule[*]
     * <p>
     * 
     * <pre>
     *   &lt;temporal predicate  &gt;::= 
     *     &lt;attribute_name &gt; BEFORE  &lt;date-time expression &gt;
     *  |  &lt;attribute_name &gt; BEFORE OR DURING  &lt;period &gt;
     *  |  &lt;attribute_name &gt; DURING  &lt;period &gt;
     *  |  &lt;attribute_name &gt; DURING OR AFTER  &lt;period &gt;
     *  | &lt;B&gt;  &lt;attribute_name &gt; AFTER  &lt;date-time expression &gt;[*]&lt;/B&gt;     
     *  
     *   &lt;date-time expression &gt; ::=  &lt;date-time &gt; |  &lt;period &gt;
     *  
     *   &lt;period &gt; ::= 
     *     &lt;date-time &gt; &quot;/&quot;  &lt;date-time &gt;[*]
     *  |  &lt;date-time &gt; &quot;/&quot;  &lt;duration &gt;  [*]
     *  |  &lt;duration &gt; &quot;/&quot;  &lt;date-time &gt;  [*]
     * </pre>
     * 
     * </p>
     */
    public void testTemporalPredicateAfter() throws Exception {

        Filter resultFilter;
        Filter expected;

        // -------------------------------------------------------------
        // <attribute_name> AFTER <date-time expression>
        // -------------------------------------------------------------
        //
        resultFilter = (Filter) FilterBuilder
                .parse(FilterSample.FILTER_AFTER_DATE);

        assertNotNull("Filter expected", resultFilter);

        expected = FilterSample.getSample(FilterSample.FILTER_AFTER_DATE);

        assertEquals("greater filter ", expected, resultFilter);

        // -------------------------------------------------------------
        // <attribute_name> AFTER <period>
        // -------------------------------------------------------------
        // ATTR1 BEFORE 2006-11-31T01:30:00Z/2006-12-31T01:30:00Z
        resultFilter = FilterBuilder
                .parse(FilterSample.FILTER_AFTER_PERIOD_BETWEEN_DATES);

        assertNotNull("Filter expected", resultFilter);

        expected = FilterSample
                .getSample(FilterSample.FILTER_AFTER_PERIOD_BETWEEN_DATES);

        assertEquals("greater filter ", expected, resultFilter);

        // ATTR1 AFTER 2006-11-30T01:30:00Z/P10D
        resultFilter = (Filter) FilterBuilder
                .parse(FilterSample.FILTER_AFTER_PERIOD_DATE_DAYS);

        assertNotNull("Filter expected", resultFilter);

        expected = FilterSample
                .getSample(FilterSample.FILTER_AFTER_PERIOD_DATE_DAYS);

        assertEquals("greater filter", expected, resultFilter);

        // ATTR1 AFTER 2006-11-30T01:30:00Z/P10M
        resultFilter = (Filter) FilterBuilder
                .parse(FilterSample.FILTER_AFTER_PERIOD_DATE_MONTH);

        assertNotNull("Filter expected", resultFilter);

        expected = FilterSample
                .getSample(FilterSample.FILTER_AFTER_PERIOD_DATE_MONTH);

        assertEquals("greater filter", expected, resultFilter);

        // ATTR1 AFTER 2006-11-30T01:30:00Z/P10Y
        resultFilter = (Filter) FilterBuilder
                .parse(FilterSample.FILTER_AFTER_PERIOD_DATE_YEARS);

        assertNotNull("Filter expected", resultFilter);

        expected = FilterSample
                .getSample(FilterSample.FILTER_AFTER_PERIOD_DATE_YEARS);

        assertEquals("greater filter", expected, resultFilter);

        // ATTR1 AFTER 2006-11-30T01:30:00Z/P10Y10M
        resultFilter = (Filter) FilterBuilder
                .parse(FilterSample.FILTER_AFTER_PERIOD_DATE_YEARS_MONTH);

        assertNotNull("Filter expected", resultFilter);

        expected = FilterSample
                .getSample(FilterSample.FILTER_AFTER_PERIOD_DATE_YEARS_MONTH);

        assertEquals("greater filter", expected, resultFilter);

        // ATTR1 AFTER 2006-11-30T01:30:00Z/T5H
        resultFilter = (Filter) FilterBuilder
                .parse(FilterSample.FILTER_AFTER_PERIOD_DATE_HOURS);

        assertNotNull("Filter expected", resultFilter);

        expected = FilterSample
                .getSample(FilterSample.FILTER_AFTER_PERIOD_DATE_HOURS);

        assertEquals("greater filter", expected, resultFilter);

        // ATTR1 AFTER 2006-11-30T01:30:00Z/T5M
        resultFilter = (Filter) FilterBuilder
                .parse(FilterSample.FILTER_AFTER_PERIOD_DATE_MINUTES);

        assertNotNull("FilSter expected", resultFilter);

        expected = FilterSample
                .getSample(FilterSample.FILTER_AFTER_PERIOD_DATE_MINUTES);

        assertEquals("greater filter", expected, resultFilter);

        // ATTR1 AFTER 2006-11-30T01:30:00Z/T5S
        resultFilter = (Filter) FilterBuilder
                .parse(FilterSample.FILTER_AFTER_PERIOD_DATE_SECONDS);

        assertNotNull("Filter expected", resultFilter);

        expected = FilterSample
                .getSample(FilterSample.FILTER_AFTER_PERIOD_DATE_SECONDS);

        assertEquals("greater filter", expected, resultFilter);

        // ATTR1 AFTER 2006-11-30T01:30:00Z/P10Y10M10DT5H5M5S
        resultFilter = (Filter) FilterBuilder
                .parse(FilterSample.FILTER_AFTER_PERIOD_DATE_YMD_HMS);

        assertNotNull("Filter expected", resultFilter);

        expected = FilterSample
                .getSample(FilterSample.FILTER_AFTER_PERIOD_DATE_YMD_HMS);

        assertEquals("greater filter", expected, resultFilter);

    }

    /**
     * Test Text Predicate
     * <p>
     * 
     * <pre>
     *   &lt;text predicate &gt; ::= 
     *        &lt;attribute name &gt; [ NOT ] LIKE  &lt;character pattern &gt; 
     *  
     *  For example:
     *  
     *  attribute like '%contains_this%'
     *  attribute like 'begins_with_this%'
     *  attribute like '%ends_with_this'
     *  attribute like 'd_ve' will match 'dave' or 'dove'
     *  attribute not like '%will_not_contain_this%'
     *  attribute not like 'will_not_begin_with_this%'
     *  attribute not like '%will_not_end_with_this'
     * </pre>
     * 
     * </p>
     */
    public void testTextPredicate() throws Exception {

        Filter resultFilter;
        Filter expected;

        // Like
        resultFilter = FilterBuilder.parse(FilterSample.LIKE_FILTER);

        assertNotNull("Filter expected", resultFilter);

        expected = FilterSample.getSample(FilterSample.LIKE_FILTER);

        assertEquals("like filter was expected", expected, resultFilter);

        // not Like
        resultFilter = FilterBuilder.parse(FilterSample.NOT_LIKE_FILTER);

        assertNotNull("Filter expected", resultFilter);

        expected = FilterSample.getSample(FilterSample.NOT_LIKE_FILTER);

        assertEquals("like filter was expected", expected, resultFilter);

    }

    /**
     * Test Between Predicate.
     * <p>
     * 
     * <pre>
     *  This cql clause is an extension for convenience.
     *  
     *   &lt;between predicate &gt; ::= 
     *        &lt;attribute name &gt; [ NOT ] BETWEEN  &lt;literal&amp; #62; AND  &lt; literal  &gt; 
     * </pre>
     * 
     * </p>
     */
    public void testBetweenPredicate() throws Exception {

        Filter resultFilter;
        Filter expected;

        // between
        resultFilter = FilterBuilder.parse(FilterSample.BETWEEN_FILTER);

        assertNotNull("Filter expected", resultFilter);

        expected = FilterSample.getSample(FilterSample.BETWEEN_FILTER);

        assertEquals("Between filter was expected", expected, resultFilter);

        // not between
        resultFilter = FilterBuilder.parse(FilterSample.NOT_BETWEEN_FILTER);

        assertNotNull("Filter expected", resultFilter);

        expected = FilterSample.getSample(FilterSample.NOT_BETWEEN_FILTER);

        assertEquals("Between filter was expected", expected, resultFilter);

        // test compound attribute gmd:aa:bb.gmd:cc.gmd:dd
        final String prop = "gmd:aa:bb.gmd:cc.gmd:dd";
        final String propExpected = "gmd:aa:bb/gmd:cc/gmd:dd";
        resultFilter = FilterBuilder.parse(prop + " BETWEEN 100 AND 200 ");

        assertTrue("PropertyIsBetween filter was expected",
                resultFilter instanceof PropertyIsBetween);

        PropertyIsBetween filter = (PropertyIsBetween) resultFilter;
        Expression property = filter.getExpression();

        assertEquals(propExpected, property.toString());

    }

    /**
     * Test Attribute
     * 
     * <p>
     * 
     * <pre>
     *   &lt;attribute name &gt; ::= 
     *            &lt;simple attribute name &gt; 
     *       |    &lt;compound attribute name &gt;
     *       
     *   &lt;simple attribute name &gt; ::=  &lt;identifier &gt;
     *   &lt;compound attribute name &gt; ::=  &lt;identifier &gt; &lt;period &gt; [{ &lt;identifier &gt; &lt;period &gt;}...] &lt;simple attribute name &gt;     
     *  
     *   &lt;identifier &gt; ::=  &lt;identifier start [ {  &lt;colon &gt; |  &lt;identifier part &gt; }... ]
     *   &lt;identifier start &gt; ::=  &lt;simple Latin letter &gt;
     *   &lt;identifier part &gt; ::=  &lt;simple Latin letter &gt; |  &lt;digit &gt;
     * </pre>
     * 
     * </p>
     */
    public void testAttribute() {

        // Simple attribute name
        testAttribute("startPart");

        testAttribute("startpart:part1:part2");

        // Compound attribute name
        testAttribute("s11:p12:p13.s21:p22.s31:p32");

        testAttribute("gmd:MD_Metadata.gmd:identificationInfo.gmd:MD_DataIdentification.gmd:abstract");

    }

    private void testAttribute(final String attSample) {

        PropertyIsLike result;
        PropertyName attResult = null;
        try {

            String expected = attSample.replace('.', '/');

            result = (PropertyIsLike) FilterBuilder.parse(attSample
                    + " LIKE 'abc%'");

            attResult = (PropertyName) result.getExpression();

            assertEquals(expected, attResult.getPropertyName());

        } catch (ParseException e) {
            throw new RuntimeException();
        }

    }

    /**
     * Test boolean value expressions.
     * <p>
     * 
     * <pre>
     *   &lt;boolean value expression &gt; ::= 
     *        &lt;boolean term &gt;
     *   |    &lt;boolean value expression &gt; OR  &lt;boolean term &gt;
     *   
     *   &lt;boolean term &gt; ::= 
     *        &lt;boolean factor &gt;
     *  |     &lt;boolean term &gt; AND  &lt;boolean factor&gt;
     * </pre>
     * 
     * </p>
     */
    public void testBooleanValueExpression() throws Exception {

        Filter result;
        Filter expected;

        // ATTR1 < 10 AND ATTR2 < 2
        result = FilterBuilder.parse(FilterSample.FILTER_AND);

        assertNotNull("filter expected", result);

        expected = FilterSample.getSample(FilterSample.FILTER_AND);

        assertEquals("ATTR1 < 10 AND ATTR2 < 2 was expected", expected, result);

        // "ATTR1 > 10 OR ATTR2 < 2"
        result = FilterBuilder.parse(FilterSample.FILTER_OR);

        assertNotNull("filter expected", result);

        expected = FilterSample.getSample(FilterSample.FILTER_OR);

        assertEquals("ATTR1 > 10 OR ATTR2 < 2 was expected", expected, result);

        // ATTR1 < 10 AND ATTR2 < 2 OR ATTR3 > 10
        result = FilterBuilder.parse(FilterSample.FILTER_OR_AND);

        assertNotNull("filter expected", result);

        expected = FilterSample.getSample(FilterSample.FILTER_OR_AND);

        assertEquals("(ATTR1 < 10 AND ATTR2 < 2 OR ATTR3 > 10) was expected",
                expected, result);

        // ATTR3 < 4 AND (ATT1 > 10 OR ATT2 < 2)
        result = FilterBuilder.parse(FilterSample.FILTER_OR_AND_PARENTHESIS);

        assertNotNull("filter expected", result);

        expected = FilterSample
                .getSample(FilterSample.FILTER_OR_AND_PARENTHESIS);

        assertEquals("ATTR3 < 4 AND (ATT1 > 10 OR ATT2 < 2) was expected",
                expected, result);

        // ATTR3 < 4 AND (NOT( ATTR1 < 10 AND ATTR2 < 2))
        result = FilterBuilder.parse(FilterSample.FILTER_AND_NOT_AND);

        assertNotNull("filter expected", result);

        expected = FilterSample.getSample(FilterSample.FILTER_AND_NOT_AND);

        assertEquals(
                "ATTR3 < 4 AND (NOT( ATTR1 < 10 AND ATTR2 < 2)) was expected",
                expected, result);

        // "ATTR1 < 1 AND (NOT (ATTR2 < 2)) AND ATTR3 < 3"
        result = FilterBuilder.parse(FilterSample.FILTER_AND_NOT_COMPARASION);

        assertNotNull("filter expected", result);

        expected = FilterSample
                .getSample(FilterSample.FILTER_AND_NOT_COMPARASION);

        assertEquals(
                "ATTR1 < 4 AND (NOT (ATTR2 < 4)) AND ATTR3 < 4 was expected",
                expected, result);

    }

    /**
     * Test for Unary Expressions.
     * <p>
     * 
     * <pre>
     *  
     *   &lt;unary expression &gt; ::= 
     *            &lt;Literal &gt;
     *       |    &lt;Function &gt;
     *       |    &lt;Attribute &gt;
     *       |   ( &lt;Expression &gt;)
     *       |   [ &lt;Expression &gt;]
     * </pre>
     * 
     * </p>
     * TODO require more test }
     */
    public void testUnaryExpression() throws Exception {

        Filter result;
        Filter expected;

        result = FilterBuilder.parse(FilterSample.FILTER_WITH_FUNCTION_ABS);

        assertNotNull("filter expected", result);

        expected = FilterSample
                .getSample(FilterSample.FILTER_WITH_FUNCTION_ABS);

        // TODO BUG in Geotools method equals in Functions
        assertEquals(
                "fails due to a BUG in Geotools method equals in Functions",
                expected, result);

    }

    /**
     * Test Geo Operations.
     * 
     * <p>
     * 
     * <pre>
     *   &lt;routine invocation &gt; ::= 
     *          &lt;geoop name &gt; &lt;georoutine argument list &gt;[*]
     *       |  &lt;relgeoop name &gt; &lt;relgeoop argument list &gt;
     *       |  &lt;routine name &gt; &lt;argument list &gt;
     *       
     *   &lt;geoop name &gt; ::= 
     *       EQUAL | DISJOINT | INTERSECT | TOUCH | CROSS | [*]
     *       WITHIN | CONTAINS |OVERLAP | RELATE [*]
     *  
     *  That rule is extended with bbox for convenience.
     * 
     *   &lt;bbox argument list &gt;::= 
     *   &quot;(&quot;  &lt;attribute &gt; &quot;,&quot; &lt;min X &gt; &quot;,&quot; &lt;min Y &gt; &quot;,&quot; &lt;max X &gt; &quot;,&quot; &lt;max Y &gt;[&quot;,&quot;  &lt;srs &gt;] &quot;)&quot;
     *  
     *   &lt;min X &gt; ::=  &lt;signed numerical literal &gt; 
     *   &lt;min Y &gt; ::=  &lt;signed numerical literal &gt; 
     *   &lt;max X &gt; ::=  &lt;signed numerical literal &gt; 
     *   &lt;max Y &gt; ::=  &lt;signed numerical literal &gt;
     *   &lt;srs &gt; ::=  
     * </pre>
     * 
     * </p>
     * 
     * Note: RELATE is not supported (implementation in GeoTools is required)
     * 
     */
    public void testRoutineInvocationGeoOp() throws Exception {
        Filter resultFilter;

        resultFilter = FilterBuilder.parse("DISJOINT(ATTR1, POINT(1 2))");

        assertTrue("Disjoint was expected", resultFilter instanceof Disjoint);

        resultFilter = FilterBuilder.parse("INTERSECT(ATTR1, POINT(1 2))");

        assertTrue("Intersects was expected",
                resultFilter instanceof Intersects);

        resultFilter = FilterBuilder.parse("TOUCH(ATTR1, POINT(1 2))");

        assertTrue("Touches was expected", resultFilter instanceof Touches);

        resultFilter = FilterBuilder.parse("CROSS(ATTR1, POINT(1 2))");

        assertTrue("Crosses was expected", resultFilter instanceof Crosses);

        resultFilter = FilterBuilder.parse("CONTAINS(ATTR1, POINT(1 2))");

        assertTrue("Contains was expected", resultFilter instanceof Contains);

        resultFilter = FilterBuilder.parse("OVERLAP(ATTR1, POINT(1 2))");

        assertTrue("Overlaps was expected", resultFilter instanceof Overlaps);

        // BBOX
        resultFilter = FilterBuilder.parse("BBOX(ATTR1, 10,20,30,40)");

        assertTrue("BBox was expected", resultFilter instanceof BBOX);

        resultFilter = FilterBuilder
                .parse("BBOX(ATTR1, 10,20,30,40, 'EPSG:4326')");

        assertTrue("BBox was expected", resultFilter instanceof BBOX);

        // EQUALS
        resultFilter = FilterBuilder.parse("EQUAL(ATTR1, POINT(1 2))");

        assertTrue("not an instance of Equals", resultFilter instanceof Equals); // TODO Bug in
                                                                // geotools.filter.spatial.Equals

        resultFilter = FilterBuilder.parse("WITHIN(ATTR1, POINT(1 2))");

        assertTrue("Within was expected", resultFilter instanceof Within);

    }

    /**
     * Test RelGeo Operations [*]
     * 
     * <p>
     * 
     * <pre>
     *   &lt;routine invocation &gt; ::= 
     *          &lt;geoop name &gt; &lt;georoutine argument list &gt;
     *       |  &lt;relgeoop name &gt; &lt;relgeoop argument list &gt; [*]
     *       |  &lt;routine name &gt; &lt;argument list &gt;
     *       
     *   &lt;relgeoop name &gt; ::= 
     *       DWITHIN | BEYON [*]
     * </pre>
     * 
     * </p>
     */
    public void testRoutineInvocationRelGeoOp() throws Exception {

        Filter resultFilter;

        resultFilter = FilterBuilder
                .parse("DWITHIN(ATTR1, POINT(1 2), 10, kilometers)");

        assertTrue(resultFilter instanceof DistanceBufferOperator);

        // test compound attribute gmd:aa:bb.gmd:cc.gmd:dd
        final String prop = "gmd:aa:bb.gmd:cc.gmd:dd";
        final String propExpected = "gmd:aa:bb/gmd:cc/gmd:dd";
        resultFilter = FilterBuilder.parse("DWITHIN(" + prop
                + ", POINT(1 2), 10, kilometers) ");

        assertTrue("DistanceBufferOperator filter was expected",
                resultFilter instanceof DistanceBufferOperator);

        DistanceBufferOperator filter = (DistanceBufferOperator) resultFilter;
        Expression property = filter.getExpression1();

        assertEquals(propExpected, property.toString());

    }

    /**
     * Tests Geometry Literals
     * 
     * <p>
     * 
     * <pre>
     *   &lt;geometry literal &gt; := 
     *          &lt;Point Tagged Text &gt; 
     *       |  &lt;LineString Tagged Text &gt;
     *       |  &lt;Polygon Tagged Text &gt;
     *       |  &lt;MultiPoint Tagged Text &gt;
     *       |  &lt;MultiLineString Tagged Text &gt;
     *       |  &lt;MultiPolygon Tagged Text &gt;
     *       |  &lt;GeometryCollection Tagged Text &gt;
     *       |  &lt;Envelope Tagged Text &gt;     
     * </pre>
     * 
     * </p>
     */
    public void testGeometryLiterals() throws Exception {

        BinarySpatialOperator result;
        Literal geom;

        // Point
        result = (BinarySpatialOperator) FilterBuilder
                .parse("CROSS(ATTR1, POINT(1 2))");

        geom = (Literal) result.getExpression2();

        assertNotNull(geom.getValue());
        assertTrue(geom.getValue() instanceof com.vividsolutions.jts.geom.Point);

        // LineString
        result = (BinarySpatialOperator) FilterBuilder
                .parse("CROSS(ATTR1, LINESTRING(1 2, 10 15))");

        geom = (Literal) result.getExpression2();

        assertNotNull(geom.getValue());
        assertTrue(geom.getValue() instanceof com.vividsolutions.jts.geom.LineString);

        // Poligon
        result = (BinarySpatialOperator) FilterBuilder
                .parse("CROSS(ATTR1, POLYGON((1 2, 15 2, 15 20, 15 21, 1 2)))");

        geom = (Literal) result.getExpression2();

        assertNotNull(geom.getValue());
        assertTrue(geom.getValue() instanceof com.vividsolutions.jts.geom.Polygon);

        // MultiPoint
        result = (BinarySpatialOperator) FilterBuilder
                .parse("CROSS(ATTR1, MULTIPOINT( (1 2), (15 2), (15 20), (15 21), (1 2) ))");

        geom = (Literal) result.getExpression2();

        assertNotNull(geom.getValue());
        assertTrue(geom.getValue() instanceof com.vividsolutions.jts.geom.MultiPoint);

        // MultiLineString
        result = (BinarySpatialOperator) FilterBuilder
                .parse("CROSS(ATTR1, MULTILINESTRING((10 10, 20 20),(15 15,30 15)) )");

        geom = (Literal) result.getExpression2();

        assertNotNull(geom.getValue());
        assertTrue(geom.getValue() instanceof com.vividsolutions.jts.geom.MultiLineString);

        // MultiPolygon
        result = (BinarySpatialOperator) FilterBuilder
                .parse("CROSS(ATTR1, MULTIPOLYGON( ((10 10, 10 20, 20 20, 20 15, 10 10)),((60 60, 70 70, 80 60, 60 60 )) ) )");

        geom = (Literal) result.getExpression2();

        assertNotNull(geom.getValue());
        assertTrue(geom.getValue() instanceof com.vividsolutions.jts.geom.MultiPolygon);

        // GEOMETRYCOLLECTION
        result = (BinarySpatialOperator) FilterBuilder
                .parse("CROSS(ATTR1, GEOMETRYCOLLECTION (POINT (10 10),POINT (30 30),LINESTRING (15 15, 20 20)) )");

        geom = (Literal) result.getExpression2();

        assertNotNull(geom.getValue());
        assertTrue(geom.getValue() instanceof com.vividsolutions.jts.geom.GeometryCollection);

        // ENVELOPE
        result = (BinarySpatialOperator) FilterBuilder
                .parse("CROSS(ATTR1, ENVELOPE( 10, 20, 30, 40) )");

        geom = (Literal) result.getExpression2();

        assertNotNull(geom.getValue());
        assertTrue(geom.getValue() instanceof com.vividsolutions.jts.geom.Polygon);

    }

    public void testGeometryLiteralsError() {

        final String filterError = "WITHIN(ATTR1, POLYGON((1 2, 10 15), (10 15, 1 2)))";

        try {
            FilterBuilder.parse(filterError);

            fail("polygon error was expected");

        } catch (ParseException e) {

            String error = FilterBuilder.getFormattedErrorMessage(e,
                    filterError);
            assertFalse("".equals(error));
            // LOGGER.info( error);
        }

    }

}
