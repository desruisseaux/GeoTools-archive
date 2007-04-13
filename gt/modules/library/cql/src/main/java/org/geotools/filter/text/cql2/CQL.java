/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.Hints;
import org.geotools.filter.FilterTransformer;
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
 * Expression expr1 = CQL.toExpression(&quot;attName&quot;);
 *
 * Expression expr2 = CQL.toExpression(&quot;attName * 2&quot;);
 *
 * Expression expr3 = CQL.toExpression(&quot;strConcat(attName, 'suffix')&quot;);
 *
 * Filter f1 = CQL.toFilter(&quot;ATTR1 &lt; 10 AND ATTR2 &lt; 2 OR ATTR3 &gt; 10&quot;);
 *
 * Filter f2 = CQL.toFilter(&quot;ATTR1 IS NULL&quot;);
 *
 * Filter f3 = CQL.toFilter(&quot;ATTR1 BEFORE 2006-11-30T01:30:00Z&quot;);
 *
 * Filter f4 = CQL.toFilter(&quot;ATTR1 DOES-NOT-EXIST&quot;);
 *
 * Filter f5 = CQL.toFilter(&quot;ATTR1 BETWEEN 10 AND 20&quot;);
 *
 * Filter f6 = CQL.toFilter(&quot;CROSS(ATTR1, LINESTRING(1 2, 10 15))&quot;);
 *
 * Filter f7 = CQL.toFilter(&quot;BBOX(ATTR1, 10,20,30,40)&quot;);
 *
 * List filters = CQL
 *                 .toFilterList(&quot;ATTR1 IS NULL|BBOX(ATTR1, 10,20,30,40)|INCLUDE&quot;);
 * </code>
 * </pre>
 *
 * </p>
 *
 * @since 2.4
 * @author Mauricio Pazos - Axios Engineering
 * @author Gabriel Roldan - Axios Engineering
 * @version $Id$
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/library/cql/src/main/java/org/geotools/text/filter/CQL.java $
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
    public static Filter toFilter(final String cqlPredicate)
        throws CQLException {
        Filter filter = CQL.toFilter(cqlPredicate, null);

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
    public static Filter toFilter(String cqlPredicate, FilterFactory filterFactory)
        throws CQLException {
        if (filterFactory == null) {
            filterFactory = CommonFactoryFinder.getFilterFactory((Hints) null);
        }

        CQLCompiler compiler = new CQLCompiler(cqlPredicate, filterFactory);

        try {
            compiler.CompilationUnit();
        } catch (ParseException e) {
            throw new CQLException(e.getMessage(), compiler.getToken(0));
        }

        if (compiler.getException() != null) {
            throw compiler.getException();
        }

        Object result = compiler.getResult();
        Filter builtFilter = (Filter) result;

        return builtFilter;
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
    public static Expression toExpression(String cqlExpression)
        throws CQLException {
        return toExpression(cqlExpression, null);
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
    public static Expression toExpression(final String cqlExpression,
        final FilterFactory filterFactory) throws CQLException {
        FilterFactory factory = filterFactory;

        if (factory == null) {
            factory = CommonFactoryFinder.getFilterFactory((Hints) null);
        }

        CQLCompiler c = new CQLCompiler(cqlExpression, factory);

        try {
            c.ExpressionCompilationUnit();
        } catch (ParseException e) {
            throw new CQLException(e.getMessage(), c.getToken(0));
        }

        if (c.getException() != null) {
            throw c.getException();
        }

        Expression builtFilter = (Expression) c.getResult();

        return builtFilter;
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
    public static List toFilterList(final String cqlFilterList)
        throws CQLException {
        List filters = CQL.toFilterList(cqlFilterList, null);

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
    public static List toFilterList(final String cqlFilterList, final FilterFactory filterFactory)
        throws CQLException {
        FilterFactory factory = filterFactory;

        if (factory == null) {
            factory = CommonFactoryFinder.getFilterFactory((Hints) null);
        }

        CQLCompiler compiler = new CQLCompiler(cqlFilterList, factory);

        try {
            compiler.MultipleCompilationUnit();
        } catch (ParseException e) {
            throw new CQLException(e.getMessage() + ": " + cqlFilterList,
                    compiler.getToken(0));
        }

        if (compiler.getException() != null) {
            throw compiler.getException();
        }

        List results = compiler.getResults();

        return results;
    }

    public static String getFormattedErrorMessage(CQLException pe, String input) {
        StringBuffer sb = new StringBuffer(input);
        sb.append('\n');

        Token t = pe.currentToken;

        while (t.next != null)
            t = t.next;

        int column = t.beginColumn - 1;

        for (int i = 0; i < column; i++) {
            sb.append(' ');
        }

        sb.append('^').append('\n');
        sb.append(pe.getMessage());

        return sb.toString();
    }

    public static final void main(String[] args) {
        System.out.println("Expression Tester");

        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
        FilterTransformer filterTransformer = new FilterTransformer();
        filterTransformer.setIndentation(4);

        while (true) {
            System.out.print(">");

            String line = null;
            try {
                line = r.readLine();
                if (line.equals("quit")) {
                    break;
                }
                Object b = CQL.toFilter(line);
                filterTransformer.transform(b, System.out);
                System.out.println();
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (CQLException pe) {
                System.out.println(CQL.getFormattedErrorMessage(pe, line));
            } catch (TransformerException e) {
                e.printStackTrace();
            }
        }
    }
}
