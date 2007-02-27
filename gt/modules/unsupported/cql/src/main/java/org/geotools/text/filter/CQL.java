package org.geotools.text.filter;

import java.util.List;

import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;

/**
 * Utility class to parse <b>CQL</b> predicates and expressions to GeoAPI
 * {@link Filter}s and {@link Expression}s, respectively.
 * 
 * <p>
 * <b>CQL</b> is an acronym for OGC Common Query Language, a query predicate
 * language whose syntax is similar to a SQL WHERE clause, defined in clause
 * 6.2.2 of the OGC Catalog Service for Web, version 2.0.1 implementation
 * specification.
 * </p>
 * <p>
 * This class provides three methods, {@link #toFilter(String)},
 * {@link #toExpression(String)} and {@link #toFilterList(String)}; and an
 * overloaded version of each one for the user to provide a
 * {@link FilterFactory2} implementation to use.
 * </p>
 * <p>
 * <h2>Usage</h2>
 * Here are some usage examples. Refer to the <a
 * href="http://docs.codehaus.org/display/GEOTOOLS/CQL+Parser+Design">complete
 * grammar</a> to see what exactly you can do.
 * 
 * <pre>
 * <code>
 * Expression expr1 = CQL.toExpression("attName");
 * Expression expr2 = CQL.toExpression("attName * 2");
 * Expression expr3 = CQL.toExpression("strConcat(attName, 'suffix')");
 * 
 * Filter f1 = CQL.toFilter("ATTR1 < 10 AND ATTR2 < 2 OR ATTR3 > 10" );
 * Filter f2 = CQL.toFilter( "ATTR1 IS NULL" );
 * Filter f3 = CQL.toFilter( "ATTR1 BEFORE 2006-11-30T01:30:00Z" );
 * Filter f4 = CQL.toFilter( "ATTR1 DOES-NOT-EXIST" );
 * Filter f5 = CQL.toFilter( "ATTR1 BETWEEN 10 AND 20" );
 * Filter f6 = CQL.toFilter( "CROSS(ATTR1, LINESTRING(1 2, 10 15))" );
 * Filter f7 = CQL.toFilter( "BBOX(ATTR1, 10,20,30,40)" );
 * 
 * List filters = CQL.toFilterList("ATTR1 IS NULL|BBOX(ATTR1, 10,20,30,40)|INCLUDE");
 * </code>
 * </pre>
 * 
 * </p>
 * 
 * @since 2.4
 * @author Mauricio Pazos - Axios Engineering
 * @author Gabriel Roldan - Axios Engineering
 * @version $Id$
 * @source $URL$
 */
public class CQL {

    private CQL() {
        // do nothing, private constructor
        // to indicate it is a pure utility class
    }

    /**
     * Parses the input string in OGC CQL format into a Filter, using the
     * systems default FilterFactory implementation.
     * 
     * @param cqlPredicate
     *            a string containing a query predicate in OGC CQL format.
     * @return a {@link Filter} equivalent to the constraint specified in
     *         <code>cqlPredicate</code>.
     */
    public static Filter toFilter(String cqlPredicate) throws ParseException {
        Filter filter = FilterBuilder.parse(cqlPredicate);
        return filter;
    }

    /**
     * Parses the input string in OGC CQL format into a Filter, using the
     * provided FilterFactory.
     * 
     * @param cqlPredicate
     *            a string containing a query predicate in OGC CQL format.
     * @param filterFactory
     *            the {@link FilterFactory} to use for the creation of the
     *            Filter.
     * @return a {@link Filter} equivalent to the constraint specified in
     *         <code>Predicate</code>.
     */
    public static Filter toFilter(String cqlPredicate, FilterFactory2 filterFactory)
            throws ParseException {
        Filter filter = FilterBuilder.parse(filterFactory, cqlPredicate);
        return filter;
    }

    /**
     * Parses the input string in OGC CQL format into an Expression, using the
     * systems default FilterFactory implementation.
     * 
     * @param cqlExpression
     *            a string containing an OGC CQL expression.
     * @return a {@link Expression} equivalent to the one specified in
     *         <code>cqlExpression</code>.
     */
    public static Expression toExpression(String cqlExpression) throws ParseException {
        Expression expression = FilterBuilder.parseExpression(cqlExpression);
        return expression;
    }

    /**
     * Parses the input string in OGC CQL format into an Expression, using the
     * provided FilterFactory.
     * 
     * @param cqlExpression
     *            a string containing a OGC CQL expression.
     * 
     * @param filterFactory
     *            the {@link FilterFactory} to use for the creation of the
     *            Expression.
     * @return a {@link Filter} equivalent to the constraint specified in
     *         <code>cqlExpression</code>.
     */
    public static Expression toExpression(String cqlExpression, FilterFactory2 filterFactory)
            throws ParseException {
        Expression expression = FilterBuilder.parseExpression(filterFactory, cqlExpression);
        return expression;
    }

    /**
     * Parses the input string, which has to be a list of OGC CQL predicates
     * separated by <code>|</code> into a <code>List</code> of
     * <code>Filter</code>s, using the provided FilterFactory.
     * 
     * @param cqlFilterList
     *            a list of OGC CQL predicates separated by <code>|</code>
     * 
     * @return a List of {@link Filter}, one for each input CQL statement
     */
    public static List toFilterList(String cqlFilterList) throws ParseException {
        List filters = FilterBuilder.parseFilterList(null, cqlFilterList);
        return filters;
    }

    /**
     * Parses the input string, which has to be a list of OGC CQL predicates
     * separated by <code>|</code> into a <code>List</code> of
     * <code>Filter</code>s, using the provided FilterFactory.
     * 
     * @param cqlFilterList
     *            a list of OGC CQL predicates separated by <code>|</code>
     * 
     * @param filterFactory
     *            the {@link FilterFactory} to use for the creation of the
     *            Expression.
     * @return a List of {@link Filter}, one for each input CQL statement
     */
    public static List toFilterList(String cqlFilterList, FilterFactory2 filterFactory)
            throws ParseException {
        List filters = FilterBuilder.parseFilterList(filterFactory, cqlFilterList);
        return filters;
    }

}
