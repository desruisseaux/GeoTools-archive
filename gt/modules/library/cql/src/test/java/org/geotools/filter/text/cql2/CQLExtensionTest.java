/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.filter.text.cql2;

import junit.framework.TestCase;
import java.util.List;

import org.geotools.filter.FilterFactoryImpl;
import org.opengis.filter.ExcludeFilter;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.IncludeFilter;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.PropertyIsGreaterThan;
import org.opengis.filter.PropertyIsLessThan;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;


/**
 * 
 * Test extensions in CQL.
 * 
 * <p>
 * We adds some extension to Common CQL thinking in convenient uses. 
 * In the future we could have different dialects of CQL. That will 
 * required extend the interface  to allow selecting the dialect to use. 
 * </p>
 *
 * @author Mauricio Pazos - www.axios.es
 * @version Revision: 1.9
 */
public class CQLExtensionTest extends TestCase {
    private static final String DELIMITER = ";";

    /**
     * An INCLUDE token is parsed as {@link Filter#INCLUDE}
     *
     * @throws Exception
     */
    public void testIncludeFilter() throws Exception {
        Filter filter = CQL.toFilter("INCLUDE");
        assertNotNull(filter);
        assertTrue(Filter.INCLUDE.equals(filter));

        filter = CQL.toFilter("INCLUDE and a < 1");
        assertNotNull(filter);
        assertTrue(filter instanceof PropertyIsLessThan);

        filter = CQL.toFilter("INCLUDE or a < 1");
        assertNotNull(filter);
        assertTrue(Filter.INCLUDE.equals(filter));
    }

    /**
     * An EXCLUDE token is parsed as {@link Filter#EXCLUDE}
     *
     * @throws Exception
     */
    public void testExcludeFilter() throws Exception {
        Filter filter = CQL.toFilter("EXCLUDE");
        assertNotNull(filter);
        assertTrue(Filter.EXCLUDE.equals(filter));

        filter = CQL.toFilter("EXCLUDE and a < 1");
        assertNotNull(filter);
        assertTrue(Filter.EXCLUDE.equals(filter));

        filter = CQL.toFilter("EXCLUDE or a < 1");
        assertNotNull(filter);
        assertTrue(filter instanceof PropertyIsLessThan);
    }

    /**
     * Simple test for sequence of search conditions with only one filter [*]
     * <p>
     * <pre>
     * &lt;SequenceOfSearchConditions &gt; ::= 
     *          &lt;search condition&gt; [*]
     *     |    &lt;SequenceOfSearchConditions&gt; ; &lt;search condition&gt;
     *
     * </pre>
     * <p> 
     * 
     * @throws Exception
     */
    public void testSequenceOfSearchConditionsWithOneFilter() throws Exception {
        String valueWithDelimiter = "text" + DELIMITER + "with" + DELIMITER + "delimiter";
        final String singleFilterStr = "attr3 = '" + valueWithDelimiter + "'";
        List filters = CQL.toFilterList(singleFilterStr);

        assertNotNull(filters);
        assertEquals(1, filters.size());
        assertTrue(filters.get(0) instanceof PropertyIsEqualTo);

        PropertyIsEqualTo filter = (PropertyIsEqualTo) filters.get(0);
        assertEquals("attr3", ((PropertyName) filter.getExpression1()).getPropertyName());
        assertEquals(valueWithDelimiter, ((Literal) filter.getExpression2()).getValue());
    }

    /**
     * Simple test for sequence of search conditions with only one filter [*]
     * <p>
     * <pre>
     * &lt;SequenceOfSearchConditions &gt; ::= 
     *          &lt;search condition&gt; 
     *     |    &lt;SequenceOfSearchConditions&gt; ; &lt;search condition&gt; [*]
     *
     * </pre>
     * <p> 
     * Sample: attr1 > 5;attr2 between 1 and 7;attr3
     *
     * @throws Exception
     */
    public void testSequenceOfSearchConditionsWithManyFilters() throws Exception {
        String valueWithDelimiter = "text" + DELIMITER + "with" + DELIMITER + "delimiter";

        // "attr1 > 5; attr2 between 1 and 7; attr3 = 'text;with;delimiter
        final String filterListStr = 
                "attr1 > 5" + DELIMITER + 
                "attr2 between 1 and 7" + DELIMITER +   
                "attr3 = '" + valueWithDelimiter + "'";
        
        List filters = CQL.toFilterList(filterListStr);
        assertNotNull(filters);
        assertEquals(3, filters.size());
        assertTrue(filters.get(0) instanceof PropertyIsGreaterThan);
        assertTrue(filters.get(1) instanceof PropertyIsBetween);
        assertTrue(filters.get(2) instanceof PropertyIsEqualTo);

        PropertyIsGreaterThan gt = (PropertyIsGreaterThan) filters.get(0);
        assertEquals("attr1", ((PropertyName) gt.getExpression1()).getPropertyName());
        assertEquals(new Integer(5), ((Literal) gt.getExpression2()).getValue());

        PropertyIsBetween btw = (PropertyIsBetween) filters.get(1);
        assertEquals("attr2", ((PropertyName) btw.getExpression()).getPropertyName());
        assertEquals(new Integer(1), ((Literal) btw.getLowerBoundary()).getValue());
        assertEquals(new Integer(7), ((Literal) btw.getUpperBoundary()).getValue());

        PropertyIsEqualTo equals = (PropertyIsEqualTo) filters.get(2);
        assertEquals("attr3", ((PropertyName) equals.getExpression1()).getPropertyName());
        assertEquals(valueWithDelimiter, ((Literal) equals.getExpression2()).getValue());
    }

    /**
     * An empty filter int the constraints list shall be parsed as
     * {@link Filter#INCLUDE}
     *
     * @throws Exception
     */
    public void testParseFilterListWithEmptyFilter() throws Exception {
        String valueWithDelimiter = "text" + DELIMITER + "with" + DELIMITER + "delimiter";

        // "attr1 > 5;INCLUDE;attr3 = 'text;with;delimiter'"
        String filterListStr = "attr1 > 5" + DELIMITER + "INCLUDE" + DELIMITER + " attr3 = '"
            + valueWithDelimiter + "'";
        List filters = CQL.toFilterList(filterListStr);
        assertNotNull(filters);
        assertEquals(3, filters.size());
        assertTrue(filters.get(0) instanceof PropertyIsGreaterThan);
        assertTrue(filters.get(1) instanceof IncludeFilter);
        assertTrue(filters.get(2) instanceof PropertyIsEqualTo);

        PropertyIsGreaterThan gt = (PropertyIsGreaterThan) filters.get(0);
        assertEquals("attr1", ((PropertyName) gt.getExpression1()).getPropertyName());
        assertEquals(new Integer(5), ((Literal) gt.getExpression2()).getValue());

        PropertyIsEqualTo equals = (PropertyIsEqualTo) filters.get(2);
        assertEquals("attr3", ((PropertyName) equals.getExpression1()).getPropertyName());
        assertEquals(valueWithDelimiter, ((Literal) equals.getExpression2()).getValue());

        filterListStr = "EXCLUDE" + DELIMITER + "INCLUDE" + DELIMITER + "attr3 = '"
            + valueWithDelimiter + "'";

        filters = CQL.toFilterList(filterListStr);
        assertTrue(filters.get(0) instanceof ExcludeFilter);
        assertTrue(filters.get(1) instanceof IncludeFilter);
        assertTrue(filters.get(2) instanceof PropertyIsEqualTo);
    }
    
    /**
     * Verify the parser uses the provided FilterFactory implementation
     * @throws ParseException
     */
    public void testUsesProvidedFilterFactory() throws Exception {
        final boolean[] called = { false };
        FilterFactory ff = new FilterFactoryImpl() {
                public PropertyName property(String propName) {
                    called[0] = true;

                    return super.property(propName);
                }
            };

        CQL.toFilter("attName > 20", ff);
        assertTrue("Provided FilterFactory was not called", called[0]);
    }
    /**
     * Tests null factory as parameter.
     * 
     * @throws Exception
     */
    public void testNullFilterFactory() throws Exception {
        
        CQL.toFilter( "attName > 20", null );
        
        CQL.toExpression( "2+2", null);
    }
    
}
